package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.API.CookieForwarder;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HechosUiService {
    private final RestClient api;
    private final CookieForwarder cookies;
    private final RestTemplate restTemplate;
    // URL base de tu API de backend
    private final String BACKEND_API_URL = "http://localhost:8080";
    private final RestClient backendClient;

    /**
     * Llama a GET /hechos en el backend
     */

    public List<HechoDTO> filtrarHechos(
            String categoria,
            LocalDate fechaReporteDesde,
            LocalDate fechaReporteHasta,
            LocalDate fechaAcontecimientoDesde,
            LocalDate fechaAcontecimientoHasta,
            Double latitud,
            Double longitud
    ) {
        try {
            // Si backendClient tiene baseUrl configurado, fromPath está bien
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/hechos");

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

            // 🔑 Acá el cambio importante: sacamos el true y agregamos encode()
            URI uri = builder
                    .encode()   // encodea espacios, acentos, etc. en los query params
                    .build()
                    .toUri();

            HechoDTO[] respuesta = backendClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(HechoDTO[].class);

            return respuesta != null ? Arrays.asList(respuesta) : List.of();

        } catch (RestClientException e) {
            // Para que la vista no reviente si el back falla
            System.err.println("Error al filtrar hechos contra el backend: " + e.getMessage());
            return List.of();
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
