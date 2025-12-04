package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.ColeccionFormDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/colecciones")
@RequiredArgsConstructor
public class ColeccionesUiController {

    private final ColeccionUiService coleccionService;

    private final RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:8080}")
    private String backendBaseUrl;

    // --- LISTAR ---
    @GetMapping
    public String listarColecciones(Model model) {
        model.addAttribute("colecciones", coleccionService.listarColecciones());
        return "colecciones/inicio";
    }

    // --- DETALLE ---
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("coleccion", coleccionService.getColeccionPorId(id));
        return "colecciones/detalle";
    }

    // --- FORM NUEVA COLECCIÓN ---
    @GetMapping("/nueva")
    public String mostrarFormNueva(Model model,
                                   HttpSession session,
                                   @RequestParam(required = false) String hechos,
                                   @RequestParam(required = false) String titulo,
                                   @RequestParam(required = false) String descripcion,
                                   @RequestParam(required = false) Long algoritmo) {

        validarAdmin(session);

        ColeccionFormDTO form = new ColeccionFormDTO();

        // Si venís del pick mode
        if (hechos != null) {
            form.setHechosIds(
                    Arrays.stream(hechos.split(","))
                            .filter(s -> !s.isBlank())
                            .map(Long::parseLong)
                            .toList()
            );
        }

        // Si venís con datos ya cargados del front (título, desc, etc.)
        form.setTitulo(titulo);
        form.setDescripcion(descripcion);

        if (algoritmo != null)
            form.setCriteriosIds(List.of(algoritmo));

        form.setAdministradorId((Long) session.getAttribute("usuarioId"));

        model.addAttribute("form", form);
        model.addAttribute("listaAlgoritmos", coleccionService.listarAlgoritmos());
        model.addAttribute("listaFuentes", coleccionService.listarFuentes());

        return "colecciones/nueva";
    }

    // --- CREAR ---
    @PostMapping("/crear")
    public String crear(@ModelAttribute("form") ColeccionFormDTO form,
                        RedirectAttributes redirect,
                        HttpSession session) {

        validarAdmin(session);

        form.setAdministradorId((Long) session.getAttribute("usuarioId"));
        ColeccionDTO creada = coleccionService.crearColeccion(form);

        if (form.getAlgoritmoId() != null)
            coleccionService.asociarAlgoritmo(creada.getId(), form.getAlgoritmoId());

        if (form.getFuenteId() != null)
            coleccionService.asociarFuente(creada.getId(), form.getFuenteId());

        redirect.addFlashAttribute("ok", "Colección creada correctamente");
        return "redirect:/colecciones";
    }
    //importar hechos desde el csv
    @PostMapping("/importar-csv-upload")
    public ResponseEntity<?> subirCsv(@RequestParam("file") MultipartFile file) {

        // --- AGREGA ESTO ---
        System.out.println(">>> [FRONT-CONTROLLER] ¡Llegó la petición del navegador!");
        System.out.println(">>> Archivo recibido: " + file.getOriginalFilename());
        // -------------------

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Archivo vacío");
        }
        try {
            // Llamamos al servicio
            System.out.println(">>> [FRONT-CONTROLLER] Llamando al servicio...");
            coleccionService.importarHechosCsv(file);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error en consola
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    // --- FORM EDITAR ---
    @GetMapping("/editar/{id}")
    public String mostrarFormEditar(@PathVariable Long id,
                                    Model model,
                                    HttpSession session) {

        validarAdmin(session);

        ColeccionDTO dto = coleccionService.getColeccionPorId(id);

        ColeccionFormDTO form = new ColeccionFormDTO();
        form.setId(dto.getId());
        form.setTitulo(dto.getTitulo());
        form.setDescripcion(dto.getDescripcion());
        form.setAdministradorId((Long) session.getAttribute("usuarioId"));

        form.setHechosIds(dto.getHechos().stream().map(h -> h.getIdHecho()).toList());
        form.setCriteriosIds(dto.getCriterios().stream().map(c -> c.getId()).toList());

        model.addAttribute("form", form);
        return "colecciones/editar";
    }

    // --- EDITAR ---
    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id,
                         @ModelAttribute("form") ColeccionFormDTO form,
                         RedirectAttributes redirect,
                         HttpSession session) {

        validarAdmin(session);

        form.setAdministradorId((Long) session.getAttribute("usuarioId"));
        coleccionService.editarColeccion(id, form);

        redirect.addFlashAttribute("ok", "Colección actualizada");
        return "redirect:/colecciones";
    }

    // --- ELIMINAR ---
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           RedirectAttributes redirect,
                           HttpSession session) {

        validarAdmin(session);

        coleccionService.eliminarColeccion(id);

        redirect.addFlashAttribute("ok", "Colección eliminada");
        return "redirect:/colecciones";
    }

    // =======================================================
    // ENDPOINTS UI PARA COLECCIONES (FETCH SEGUROS)
    // =======================================================

    @GetMapping("/ui/hechos/titulos")
    @ResponseBody
    public ResponseEntity<?> uiObtenerTitulosHechos(
            @RequestParam String ids,
            HttpSession session
    ) {
        try {
            String jwt = (String) session.getAttribute("jwt");
            if (jwt == null) return ResponseEntity.status(401).body("No autenticado");

            String url = backendBaseUrl + "/api/hechos/titulos?ids=" + ids;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error UI al obtener títulos: " + e.getMessage());
        }
    }

    @GetMapping("/ui/criterios")
    @ResponseBody
    public ResponseEntity<?> uiObtenerCriterios(HttpSession session) {

        try {
            String jwt = (String) session.getAttribute("jwt");
            if (jwt == null) return ResponseEntity.status(401).body("No autenticado");

            String url = backendBaseUrl + "/api/criterios";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error UI al obtener criterios: " + e.getMessage());
        }
    }

    @PostMapping("/ui/criterios")
    @ResponseBody
    public ResponseEntity<?> uiCrearCriterio(
            @RequestBody Map<String, Object> criterio,
            HttpSession session
    ) {

        try {
            String jwt = (String) session.getAttribute("jwt");
            if (jwt == null) return ResponseEntity.status(401).body("No autenticado");

            String url = backendBaseUrl + "/api/criterios";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(criterio, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error UI al crear criterio: " + e.getMessage());
        }
    }

    // --- Utilidad ---
    private void validarAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("rol");
        if (rol == null || !rol.equals("ADMIN")) {
            throw new RuntimeException("403 - Acceso denegado");
        }
    }
}