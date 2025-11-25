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
     * Reutiliza la lógica de parseo de "items".
     */
    public List<HechoDTO> filtrarHechos(String categoria,
                                        LocalDate fechaReporteDesde,
                                        LocalDate fechaReporteHasta,
                                        LocalDate fechaAcontecimientoDesde,
                                        LocalDate fechaAcontecimientoHasta,
                                        Double latitud,
                                        Double longitud) {

        String url = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/api/hechos")
            .queryParam("categoria", categoria)
            .queryParam("fechaReporteDesde", fechaReporteDesde)
            .queryParam("fechaReporteHasta", fechaReporteHasta)
            .queryParam("fechaAcontecimientoDesde", fechaAcontecimientoDesde)
            .queryParam("fechaAcontecimientoHasta", fechaAcontecimientoHasta)
            .queryParam("latitud", latitud)
            .queryParam("longitud", longitud)
            .toUriString();

        return obtenerListaDesdeApi(url);
    }

    /**
     * Llama a POST /api/hechos/crear en el backend
     */
    public HechoDTO crearHecho(HechoDTO nuevoHecho) {
        // Asumiendo que en el futuro el backend tendrá este endpoint
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

    // --- MÉTODO PRIVADO AUXILIAR PARA EVITAR REPETIR LÓGICA DE MAPEO ---
    private List<HechoDTO> obtenerListaDesdeApi(String url) {
        try {
            // Solicitamos un Map porque el backend devuelve { "items": [...] }
            Map response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("items")) {
                List<?> itemsRaw = (List<?>) response.get("items");

                // Configuramos el mapper para entender fechas (LocalDate)
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                // Convertimos cada item del JSON a HechoDTO
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