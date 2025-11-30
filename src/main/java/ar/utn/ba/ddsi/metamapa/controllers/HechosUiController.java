
package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
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
    @Value("${backend.api.baseurl:http://localhost:8080}")

    private String backendBaseUrl;


    @GetMapping
    public String listarHechos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteHasta,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoDesde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoHasta,
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
                categoria,
                fechaReporteDesde,
                fechaReporteHasta,
                fechaAcontecimientoDesde,
                fechaAcontecimientoHasta,
                latitud,
                longitud,
                page,
                size
        );

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

        return "hechos/hechos";
    }

    /**
     * Muestra el formulario para crear un nuevo hecho.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioDeHecho(Model model) {
        // Objeto "vacío" para que Thymeleaf pueda enlazar con th:object
        model.addAttribute("nuevoHecho", new HechoDTO());
        return "formulario-hecho"; // Nombre del archivo .html
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

  /*  @GetMapping("/hechoss")
    public String verHechos(
            @RequestParam(required = false) Long coleccionId,
            @RequestParam(required = false) String titulo,   // título del hecho
            Model model
    ) {
        // Traemos colecciones para llenar el combo
        List<ColeccionDTO> colecciones = coleccionService.listarTodas();

        // Si no mandan coleccionId, podés:
        // - o mostrar lista vacía
        // - o elegir una por defecto (ej: la primera)
        List<HechoDTO> hechos;
        if (coleccionId != null) {
            hechos = hechosService.filtrarHechos(coleccionId, titulo);
        } else {
            // si querés mostrar nada hasta que elijan colección:
            hechos = List.of();
            // o si tenés un endpoint para listar todo, llamalo acá
            // hechos = hechosUiService.listarTodos();
        }

        model.addAttribute("colecciones", colecciones);
        model.addAttribute("hechos", hechos);
        model.addAttribute("coleccionId", coleccionId);

        return "hechos/hechos";
    }*/


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



// NO HAY OTROS MÉTODOS @GetMapping("/hechos")
// Esto soluciona el error "Ambiguous mapping"
