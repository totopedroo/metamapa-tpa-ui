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
import java.util.Optional;

@Controller
@RequestMapping("/hechos")
public class HechosUiController {


    @Autowired
    private HechosUiService hechosService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Muestra la lista de hechos y el formulario de filtro.
     */
    @Value("${backend.api.baseurl:http://localhost:8080}")

    private String backendBaseUrl;

    @GetMapping("/hechosS")
    public String mostrarHechos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoHasta,
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            Model model) {

        List<HechoDTO> hechos = hechosService.filtrarHechos(
                categoria, fechaReporteDesde, fechaReporteHasta,
                fechaAcontecimientoDesde, fechaAcontecimientoHasta, latitud, longitud);

        model.addAttribute("hechos", hechos);
        // Añadimos los filtros al modelo para que el formulario los "recuerde"
        model.addAttribute("filtros", new java.util.HashMap<String, Object>() {{
            put("categoria", categoria);
            put("fechaReporteDesde", fechaReporteDesde);
            // ... agregar los demás filtros
        }});

        return "hechos/hechos"; // Nombre del archivo .html de Thymeleaf
    }

    /**
     * Muestra el formulario para crear un nuevo hecho.
     */
    @GetMapping("/hechos/nuevo")
    public String mostrarFormularioDeHecho(Model model) {
        // Objeto "vacío" para que Thymeleaf pueda enlazar con th:object
        model.addAttribute("nuevoHecho", new HechoDTO());
        return "formulario-hecho"; // Nombre del archivo .html
    }

    /**
     * Procesa el envío del formulario para crear un nuevo hecho.
     */
    @PostMapping("/hechos/crear")
    public String crearHecho(@ModelAttribute HechoDTO nuevoHecho, RedirectAttributes redirectAttributes) {
        HechoDTO hechoCreado = hechosService.crearHecho(nuevoHecho);

        if (hechoCreado != null) {
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Hecho creado con éxito!");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear el hecho.");
        }

        return "redirect:/hechos"; // Redirige a la lista de hechos
    }

    /**
     * Maneja el botón de "Importar desde API".
     */
    @PostMapping("/hechos/importar-api")
    public String importarHechos(RedirectAttributes redirectAttributes) {
        try {
            hechosService.importarHechosDesdeApi();
            redirectAttributes.addFlashAttribute("mensajeExito", "Importación iniciada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al importar: " + e.getMessage());
        }
        return "redirect:/hechos";
    }

    @GetMapping("/hechos")
    public String verHechos(
            // --- Recibimos todos los filtros del formulario ---
            @RequestParam(required = false) String tituloColeccion,
            @RequestParam(required = false) Long coleccionId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String nav,
            Model model) {

        // --- 1. Pasamos los filtros de vuelta al Model ---
        // ESTO SOLUCIONA EL ERROR de "IllegalArgumentException: The 'request'..."
        model.addAttribute("tituloColeccionParam", tituloColeccion);
        model.addAttribute("coleccionIdParam", coleccionId);
        model.addAttribute("nombreParam", nombre);
        model.addAttribute("navParam", nav);

        // --- 2. Obtenemos las colecciones para el dropdown ---
        model.addAttribute("colecciones", obtenerColecciones());

        // --- 3. Obtenemos los hechos filtrados ---
        model.addAttribute("hechos", obtenerHechosFiltrados(tituloColeccion, coleccionId, nombre, nav));

        // --- 4. Devolvemos la plantilla HTML ---
        return "hechos/hechos"; // Busca /templates/hechos/hechos.html
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

// NO HAY OTROS MÉTODOS @GetMapping("/hechos")
// Esto soluciona el error "Ambiguous mapping"