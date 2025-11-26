
package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.API.CookieForwarder;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HechosUiService {
    private final RestClient api;
    private final CookieForwarder cookies;
    private final RestTemplate restTemplate;

    // Usamos la configuración de HEAD que inyecta la URL desde properties
    @Value("${api.base.url:http://localhost:8080}")
    private String apiBaseUrl;

    /**
     * Obtiene hechos destacados (irrestrictos/curados).
     */
    public List<HechoDTO> obtenerHechosDestacados(String modo) {
        String url = apiBaseUrl + "/api/hechos?modo=" + modo + "&limit=5";
        return obtenerListaDesdeApi(url);
    }

    /**
     * Llama a GET /api/hechos en el backend con filtros.
     * FUSIONADO: Usa la lógica de construcción de URL de tu compañero (verificando nulos),
     * pero llama a 'obtenerListaDesdeApi' para manejar el JSON correctamente.
     */
    public List<HechoDTO> filtrarHechos(String categoria,
                                        LocalDate fechaReporteDesde,
                                        LocalDate fechaReporteHasta,
                                        LocalDate fechaAcontecimientoDesde,
                                        LocalDate fechaAcontecimientoHasta,
                                        Double latitud,
                                        Double longitud) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/api/hechos");

        if (categoria != null && !categoria.isBlank()) {
            builder.queryParam("categoria", categoria);
        }
        if (fechaReporteDesde != null) {
            builder.queryParam("fechaReporteDesde", fechaReporteDesde);
        }
        if (fechaReporteHasta != null) {
            builder.queryParam("fechaReporteHasta", fechaReporteHasta);
        }
        if (fechaAcontecimientoDesde != null) {
            builder.queryParam("fechaAcontecimientoDesde", fechaAcontecimientoDesde);
        }
        if (fechaAcontecimientoHasta != null) {
            builder.queryParam("fechaAcontecimientoHasta", fechaAcontecimientoHasta);
        }
        if (latitud != null) {
            builder.queryParam("latitud", latitud);
        }
        if (longitud != null) {
            builder.queryParam("longitud", longitud);
        }

        // Construimos la URL y usamos el método que sabe leer { items: ... }
        return obtenerListaDesdeApi(builder.toUriString());
    }

    /**
     * Llama a POST /api/hechos/crear en el backend
     */
    public HechoDTO crearHecho(HechoDTO nuevoHecho) {
        // Usamos tu ruta (/api/hechos/crear) que es consistente con el backend actual,
        // en lugar de /fuente-dinamica/... que parece ser de otra versión.
        String url = apiBaseUrl + "/api/hechos/crear";
        try {
            return restTemplate.postForObject(url, nuevoHecho, HechoDTO.class);
        } catch (Exception e) {
            System.err.println("Error al crear hecho: " + e.getMessage());
            return null;
        }
    }

    /**
     * Llama a POST /api/hechos/importar-api en el backend
     */
    public void importarHechosDesdeApi() {
        String url = apiBaseUrl + "/api/hechos/importar-api";
        try {
            restTemplate.postForObject(url, null, String.class);
        } catch (Exception e) {
            System.err.println("Error al importar desde API: " + e.getMessage());
        }
    }

    /**
     * Llama a GET /api/hechos/usuario/{id} en el backend
     */
    public List<HechoDTO> listarHechosDelUsuario(Long idUsuario) {
        String url = apiBaseUrl + "/hechos/usuario/" + idUsuario;

        try {
            HechoDTO[] response = restTemplate.getForObject(url, HechoDTO[].class);
            return response != null ? Arrays.asList(response) : List.of();

        } catch (Exception e) {
            System.err.println("Error obteniendo hechos del usuario: " + e.getMessage());
            return List.of();
        }
    }

    // --- MÉTODO PRIVADO AUXILIAR PARA EVITAR REPETIR LÓGICA DE MAPEO ---
    private List<HechoDTO> obtenerListaDesdeApi(String url) {
        try {
            // Solicitamos un Map porque el backend devuelve { "items": [...] }
            Map response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("items")) {
                List<?> itemsRaw = (List<?>) response.get("items");

                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                List<HechoDTO> hechos = itemsRaw.stream()
                        .map(item -> mapper.convertValue(item, HechoDTO.class))
                        .collect(Collectors.toList());

                System.out.println(">>> FRONTEND: Hechos recuperados de " + url + ": " + hechos.size());
                return hechos;
            }
        } catch (Exception e) {
            System.err.println(">>> ERROR FRONTEND (Hechos) en " + url + ": " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
