package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.CrearHechoUiRequest;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.*;
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
        @RequestParam(required = false) String modo,
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

        System.out.println(">>> [FRONT] Buscando Hechos | Filtros recibidos:");
        System.out.println("    Modo (Tipo): " + modo);
        System.out.println("    Categoría: " + categoria);
        System.out.println("    Página: " + page + " | Tamaño: " + size);


        Map<String, Object> resultado = hechosService.filtrarHechos(
            categoria, modo, fechaReporteDesde, fechaReporteHasta,
            fechaAcontecimientoDesde, fechaAcontecimientoHasta,
            latitud, longitud, page, size
        );

        // List<HechoDTO> items = (List<HechoDTO>) resultado.get("items");
        // Si resultado es null o vacío, maneja el error
        if (resultado != null && resultado.get("items") != null) {
            List<HechoDTO> items = (List<HechoDTO>) resultado.get("items");
            System.out.println(">>> [FRONT] Backend respondió OK.");
            System.out.println("    Cantidad de items recibidos: " + items.size());
            if(!items.isEmpty()) {
                System.out.println("    Ejemplo Item 0 Titulo: " + items.get(0).getTitulo());
                System.out.println("    Ejemplo Item 0 Consensuado: " + items.get(0).getConsensuado());
            }

            model.addAttribute("hechos", items);
            model.addAttribute("hechosLista", items);

            // Datos de paginación seguros (con defaults por si vienen null)
            model.addAttribute("page", (int) resultado.getOrDefault("page", 0) + 1);
            model.addAttribute("totalPages", resultado.getOrDefault("totalPages", 0));
            model.addAttribute("size", resultado.getOrDefault("size", size));
            model.addAttribute("totalItems", resultado.getOrDefault("totalItems", 0));

            // Datos de estado para la vista
            model.addAttribute("pick", pick);
            model.addAttribute("returnTo", returnTo);
            model.addAttribute("hechosSel", hechos);
        } else {
            System.out.println(">>> [FRONT] Backend respondió vacío o null.");
            model.addAttribute("hechos", Collections.emptyList());
            model.addAttribute("page", 1);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
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
   /* @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<?> crearHechoDesdeUi(@RequestBody HechoDTO input, HttpServletRequest request) {
        try {
            System.out.println(">>> 1. INICIANDO CREACIÓN HECHO UI");
            System.out.println(">>> INPUT RECIBIDO: " + input);

            // 1. OBTENER ID DE SESIÓN DE FORMA SEGURA
            Object sessionVal = request.getSession().getAttribute("usuarioId");
            System.out.println(">>> VALOR EN SESIÓN 'usuarioId': " + sessionVal);

            if (sessionVal == null) {
                System.out.println(">>> ERROR: usuarioId es NULL. El LoginSuccessHandler no lo guardó.");
                return ResponseEntity.status(401).body("Error: No hay usuario en sesión. Re-logueate.");
            }

            // 2. CASTING A PRUEBA DE BALAS (Maneja Integer, Long y String)
            Long idUsuario;

            idUsuario = Long.valueOf(sessionVal.toString());



            // 3. SETEAR Y LLAMAR AL SERVICIO
            input.setIdContribuyente(idUsuario);

            System.out.println(">>> HECHO ENVIADO: " + input);

            HechoDTO creado = hechosService.crearHecho(input);
            System.out.println(">>> HECHO CREADO CON ÉXITO: ID " + creado.getId());

            return ResponseEntity.ok(creado);

        } catch (Exception e) {
            System.out.println(">>> EXCEPCIÓN FATAL EN UI CONTROLLER:");
            e.printStackTrace(); // ¡Esto imprimirá el error real en la consola!
            return ResponseEntity.internalServerError().body("Error en servidor: " + e.getMessage());
        }
    }*/
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
    public ResponseEntity<HechoDTO> crear(@RequestBody CrearHechoUiRequest dto) {
        HechoDTO creado = hechosService.crearHecho(dto);
        return ResponseEntity.ok(creado);
    }

    @PatchMapping("/ui/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> editarHechoUI(@PathVariable Long id,
                                           @RequestBody Map<String, Object> payload,
                                           HttpServletRequest request) {

        String token = getSessionToken(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado (sesión expirada o inválida)");
        }

        // Back real (según tu mensaje)
        String url = backendBaseUrl + "/fuente-dinamica/hechos/editar/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            // OJO: new RestTemplate() usa HttpURLConnection => NO soporta PATCH.
            // Usamos JDK HttpClient request factory (soporta PATCH).
            RestTemplate patchRt = new RestTemplate(new JdkClientHttpRequestFactory(HttpClient.newHttpClient()));

            ResponseEntity<String> response = patchRt.exchange(url, HttpMethod.PATCH, entity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (ResourceAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("No se pudo conectar al backend: " + ex.getMessage());
        }
    }

    private String getSessionToken(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) return null;

        Object jwt = session.getAttribute("jwt");
        if (jwt instanceof String s && !s.isBlank()) return s;

        Object access = session.getAttribute("accessToken");
        if (access instanceof String s2 && !s2.isBlank()) return s2;

        return null;
    }


    @DeleteMapping("/ui/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarHechoUI(@PathVariable Long id, HttpServletRequest request) {
        String url = ""; // La declaramos afuera para imprimirla en caso de error
        try {
            String jwt = (String) request.getSession().getAttribute("jwt");
            if (jwt == null) return ResponseEntity.status(401).body("Error: No hay Token en sesión");

            url = backendBaseUrl + "/api/hechos/eliminar/" + id;

            // LOG DE DEBUG (Mirá la consola de IntelliJ/Eclipse cuando le des click)

            var headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);

            var entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, String.class
            );

            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // ACA ESTA LA CLAVE: Si el backend devuelve error, lo atrapamos acá
            System.out.println("ERROR DEL BACKEND: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body("Backend dijo: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            // Error de conexión (Backend apagado, URL mal, etc)
            e.printStackTrace(); // Imprime el error completo en consola
            return ResponseEntity.status(500).body("Error interno Front: " + e.getMessage() + " intentando pegar a: " + url);
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
