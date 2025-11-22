package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.API.CookieForwarder;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HechosUiService {
    private final RestClient api;
    private final CookieForwarder cookies;
    private final RestTemplate restTemplate;

    // URL base de tu API de backend
    private final String BACKEND_API_URL = "http://localhost:8080";

    /**
     * Llama a GET /hechos en el backend
     */
    public List<HechoDTO> filtrarHechos(String categoria,
                                               LocalDate fechaReporteDesde,
                                               LocalDate fechaReporteHasta,
                                               LocalDate fechaAcontecimientoDesde,
                                               LocalDate fechaAcontecimientoHasta,
                                               Double latitud,
                                               Double longitud) {

        String url = UriComponentsBuilder.fromHttpUrl(BACKEND_API_URL + "/hechos")
                .queryParam("categoria", categoria)
                .queryParam("fechaReporteDesde", fechaReporteDesde)
                .queryParam("fechaReporteHasta", fechaReporteHasta)
                .queryParam("fechaAcontecimientoDesde", fechaAcontecimientoDesde)
                .queryParam("fechaAcontecimientoHasta", fechaAcontecimientoHasta)
                .queryParam("latitud", latitud)
                .queryParam("longitud", longitud)
                .toUriString();

        try {
            HechoDTO[] respuesta = restTemplate.getForObject(url, HechoDTO[].class);
            return Arrays.asList(respuesta != null ? respuesta : new HechoDTO[0]);
        } catch (Exception e) {
            System.err.println("Error al filtrar hechos: " + e.getMessage());
            return Collections.emptyList(); // Devuelve lista vacía si falla
        }
    }

    /**
     * Llama a POST /crear en el backend
     */
    public HechoDTO crearHecho(HechoDTO nuevoHecho) {
        String url = BACKEND_API_URL + "/crear";
        try {
            return restTemplate.postForObject(url, nuevoHecho, HechoDTO.class);
        } catch (Exception e) {
            System.err.println("Error al crear hecho: " + e.getMessage());
            return null; // Devuelve null si falla
        }
    }

    /**
     * Llama a POST /importar-api en el backend
     */
    public void importarHechosDesdeApi() {
        String url = BACKEND_API_URL + "/importar-api";
        try {
            // postForObject también funciona si no esperas un cuerpo de respuesta
            restTemplate.postForObject(url, null, String.class);
        } catch (Exception e) {
            System.err.println("Error al importar desde API: " + e.getMessage());
        }
    }
}
