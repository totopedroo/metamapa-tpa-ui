package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hechos")
public class HechosUiController {

    private final HechosUiService hechosService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ColeccionUiService coleccionService;

    /**
     * Muestra la lista de hechos y el formulario de filtro.
     */
    @Value("${api.base.url:http://localhost:8080}")
    private String backendBaseUrl;


    @GetMapping
    public String listarHechos(
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteHasta,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoHasta,
        @RequestParam(required = false) Double latitud,
        @RequestParam(required = false) Double longitud,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Boolean pick,
        @RequestParam(required = false) String returnTo,
        @RequestParam(required = false) String hechos,
        Model model
    ) {
        Map<String, Object> resultado = hechosService.filtrarHechos(
            categoria, fechaReporteDesde, fechaReporteHasta,
            fechaAcontecimientoDesde, fechaAcontecimientoHasta,
            latitud, longitud, page, size
        );

        // List<HechoDTO> items = (List<HechoDTO>) resultado.get("items");
        // Si resultado es null o vacío, maneja el error
        if (resultado != null && resultado.get("items") != null) {
            List<HechoDTO> items = (List<HechoDTO>) resultado.get("items");
            model.addAttribute("hechos", items);
            model.addAttribute("hechosLista", items);
            model.addAttribute("page", (int) resultado.get("page") + 1);
            model.addAttribute("totalPages", resultado.get("totalPages"));
            model.addAttribute("size", resultado.get("size"));
            model.addAttribute("totalItems", resultado.get("totalItems"));
            model.addAttribute("pick", pick);
            model.addAttribute("returnTo", returnTo);
            model.addAttribute("hechosSel", hechos);
        } else {
            model.addAttribute("hechos", Collections.emptyList());
        }

        return "hechos/hechos";
    }

    /**
     * Muestra el formulario para crear un nuevo hecho.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioDeHecho(Model model) {
        model.addAttribute("nuevoHecho", new HechoDTO());
        return "formulario-hecho";
    }

    /**
     * Procesa el envío del formulario para crear un nuevo hecho.
     */
    @PostMapping("/crear")
    @ResponseBody   // 👈 responde JSON al fetch
    public ResponseEntity<HechoDTO> crearHechoDesdeUi(@RequestBody HechoDTO input) {
        HechoDTO creado = hechosService.crearHecho(input);
        return ResponseEntity.ok(creado);
    }

    /**
     * Maneja el botón de "Importar desde API".
     */

    @PostMapping("/importar-api")
    public String importarHechos(RedirectAttributes redirectAttributes) {
        try {
            hechosService.importarHechosDesdeApi();
            redirectAttributes.addFlashAttribute("mensajeExito", "Importación iniciada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al importar: " + e.getMessage());
        }
        return "redirect:/hechos";
    }

    /**
     * Metodos que hacen llamada al back.
     */

    @PostMapping("/ui/crear")
    @ResponseBody
    public ResponseEntity<?> crearHechoUI(@RequestBody HechoDTO input, HttpServletRequest request) {
        try {
            String jwt = (String) request.getSession().getAttribute("accessToken");
            if (jwt == null) return ResponseEntity.status(401).body("No autenticado (Sesión expirada o inválida)");

            String url = backendBaseUrl + "/api/hechos/crear";

            var headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.set("Content-Type", "application/json");

            var entity = new HttpEntity<>(input, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error UI al crear hecho: " + e.getMessage());
        }
    }

    @PatchMapping("/ui/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> editarHechoUI(@PathVariable Long id, @RequestBody Map<String, Object> campos, HttpServletRequest request) {
        try {
            String jwt = (String) request.getSession().getAttribute("accessToken");
            if (jwt == null) return ResponseEntity.status(401).body("No autenticado");

            String url = backendBaseUrl + "/api/hechos/editar/" + id;

            var headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.set("Content-Type", "application/json");

            var entity = new HttpEntity<>(campos, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.PATCH, entity, String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error UI al editar hecho: " + e.getMessage());
        }
    }

    @DeleteMapping("/ui/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarHechoUI(@PathVariable Long id, HttpServletRequest request) {
        try {
            String jwt = (String) request.getSession().getAttribute("accessToken");
            if (jwt == null) return ResponseEntity.status(401).body("No autenticado");

            String url = backendBaseUrl + "/api/hechos/eliminar/" + id;

            var headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);

            var entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.DELETE, entity, String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error UI al eliminar hecho: " + e.getMessage());
        }
    }

    @PostMapping("/ui/solicitud-eliminacion")
    @ResponseBody
    public ResponseEntity<?> crearSolicitudEliminacionUI(
            @RequestBody Map<String, Object> input,
            HttpServletRequest request
    ) {
        try {
            String jwt = (String) request.getSession().getAttribute("jwt");
            if (jwt == null) return ResponseEntity.status(401).body("No autenticado");

            String url = backendBaseUrl + "/solicitudes";

            var headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.set("Content-Type", "application/json");

            var entity = new HttpEntity<>(input, headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error UI al crear solicitud: " + e.getMessage());
        }
    }

    /**
     * Llama al Backend (8080) para obtener todas las colecciones para el dropdown.
     */
    private List<ColeccionDTO> obtenerColecciones() {
        String url = backendBaseUrl + "/colecciones";
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            // Usamos el DTO de Coleccion corregido
            return objectMapper.readValue(jsonResponse, new TypeReference<List<ColeccionDTO>>() {
            });
        } catch (Exception e) {
            System.err.println("Error al obtener colecciones: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Llama al Backend (8080) para obtener los HECHOS según los filtros.
     */
    private List<HechoDTO> obtenerHechosFiltrados(String tituloColeccion, Long coleccionId, String nombre, String nav) {

        UriComponentsBuilder builder;

        // --- Lógica para decidir qué endpoint del backend usar ---
        if (tituloColeccion != null && !tituloColeccion.isEmpty()) {
            // ENDPOINT 1: Por Título de Colección
            builder = UriComponentsBuilder.fromHttpUrl(backendBaseUrl + "/colecciones/hechos")
                    .queryParam("tituloColeccion", tituloColeccion);
        } else if (coleccionId != null) {
            // ENDPOINT 2: Por ID de Colección
            builder = UriComponentsBuilder.fromHttpUrl(backendBaseUrl + "/colecciones/colecciones/" + coleccionId + "/hechos");
        } else {
            // ENDPOINT 3: (Asumido) Filtros generales de hechos
            builder = UriComponentsBuilder.fromHttpUrl(backendBaseUrl + "/hechos")
                    .queryParam("nombre", nombre)
                    .queryParam("nav", nav);
        }

        String url = builder.toUriString();

        try {
            // Obtenemos el JSON y lo convertimos a Lista de HechoDto
            String jsonResponse = restTemplate.getForObject(url, String.class);
            // Usamos el DTO de Hecho corregido (con @JsonIgnoreProperties y 'etiquetas')
            return objectMapper.readValue(jsonResponse, new TypeReference<List<HechoDTO>>() {
            });
        } catch (Exception e) {
            System.err.println("Error al obtener hechos (" + url + "): " + e.getMessage());
            return Collections.emptyList(); // Devuelve lista vacía en caso de error
        }
    }
}
