
package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.API.CookieForwarder;
import ar.utn.ba.ddsi.metamapa.dto.CrearHechoUiRequest;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.utils.MultipartInputStreamFileResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HechosUiService {
    private final RestClient client;
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
    public Map<String, Object> filtrarHechos(String categoria,
                                             String modo, LocalDate fechaReporteDesde,
                                             LocalDate fechaReporteHasta,
                                             LocalDate fechaAcontecimientoDesde,
                                             LocalDate fechaAcontecimientoHasta,
                                             Double latitud,
                                             Double longitud,
                                             int page,
                                             int size) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(apiBaseUrl + "/api/hechos")
                .queryParam("page", page)
                .queryParam("size", size);

        if (categoria != null && !categoria.isBlank()) {
            builder.queryParam("categoria", categoria);
        }
        if (modo != null && !modo.isBlank()) builder.queryParam("modo", modo);
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

        String url = builder.toUriString();
        return obtenerHechosPaginadosDesdeApi(url);

    }

     /* Llama a POST /api/hechos/crear en el backend
     */
     public HechoDTO crearHecho(CrearHechoUiRequest req) {
         String url = apiBaseUrl + "/api/hechos/crear";

         String token = cookies.getTokenFromCurrentRequest();
         String cookieHeader = cookies.getCookieHeaderFromCurrentRequest();

         // ✅ Map mutable para permitir nulls (Map.of NO permite null)
         Map<String, Object> body = new java.util.LinkedHashMap<>();



         body.put("titulo", req.titulo());
         body.put("descripcion", req.descripcion());
         body.put("categoria", req.categoria());
         body.put("provincia", req.provincia());
         body.put("latitud", req.latitud());
         body.put("longitud", req.longitud());

         body.put("fechaAcontecimiento", req.fechaAcontecimiento());
         body.put("contenidoMultimedia", req.contenidoMultimedia());
         body.put("etiquetas", req.etiquetas());

         LocalDate fecha = req.fechaAcontecimiento();
         body.put("fechaAcontecimiento", fecha);

         // ✅ contenidoMultimedia: normalizar string vacío a null
         if (req.contenidoMultimedia() != null && !req.contenidoMultimedia().isBlank()) {
             body.put("contenidoMultimedia", req.contenidoMultimedia().trim()); // string
             // Si tu BACKEND en realidad espera objeto {url: "..."} usá esto en vez de la línea de arriba:
             // body.put("contenidoMultimedia", Map.of("url", req.contenidoMultimedia().trim()));
         } else {
             body.put("contenidoMultimedia", null);
         }

         body.put("etiquetas", req.etiquetas()); // lista (o lo que sea tu request)

         try {
             var reqBuilder = client.post()
                     .uri(url)
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(body);

             // ✅ headers solo si existen (evita Bearer null / Cookie null)
             if (token != null && !token.isBlank()) {
                 reqBuilder = reqBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
             }
             if (cookieHeader != null && !cookieHeader.isBlank()) {
                 reqBuilder = reqBuilder.header(HttpHeaders.COOKIE, cookieHeader);
             }

             Map<String, Object> response = reqBuilder
                     .retrieve()
                     .body(Map.class);

             if (response == null) {
                 throw new ResponseStatusException(
                         org.springframework.http.HttpStatus.BAD_GATEWAY,
                         "Backend respondió null al crear hecho"
                 );
             }

             // ✅ normalizás lo que vuelve para tu UI DTO
             normalizarHechoParaUi(response);

             ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
             return mapper.convertValue(response, HechoDTO.class);

         } catch (org.springframework.web.client.RestClientResponseException e) {
             // 🔥 esto te muestra el error REAL del backend (status + body)
             throw new ResponseStatusException(
                     org.springframework.http.HttpStatus.valueOf(e.getRawStatusCode()),
                     "Error desde backend: " + e.getResponseBodyAsString(),
                     e
             );
         } catch (Exception e) {
             throw new ResponseStatusException(
                     org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                     "Error UI creando hecho: " + e.getMessage(),
                     e
             );
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
        String url = apiBaseUrl + "/api/hechos/hechos/usuario/" + idUsuario;

        try {
            HechoDTO[] response = restTemplate.getForObject(url, HechoDTO[].class);
            return response != null ? Arrays.asList(response) : List.of();

        } catch (Exception e) {
            System.err.println("Error obteniendo hechos del usuario: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Llama a POST /api/hechos/importar-csv en el backend
     */
    public int importarCsv(MultipartFile file) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize()
            ));

            String token = cookies.getTokenFromCurrentRequest();

            // Extraer cookie JSESSIONID
            String cookieHeader = cookies.getCookieHeaderFromCurrentRequest();

            Map response = client.post()
                    .uri(apiBaseUrl + "/api/hechos/importar-csv")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("Cookie", cookieHeader)              // <-- ESTA ES LA CLAVE
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            return (int) response.getOrDefault("importados", 0);

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo archivo CSV", e);
        }
    }

    // --- METODO PRIVADO AUXILIAR PARA EVITAR REPETIR LÓGICA DE MAPEO ---
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

    // HechosUiService.java
    @SuppressWarnings("unchecked")
    private void normalizarHechoParaUi(Map<String, Object> raw) {

        // 1) idHecho -> id
        if (raw.containsKey("idHecho") && !raw.containsKey("id")) {
            raw.put("id", raw.get("idHecho"));
        }

        // 2) fechaAcontecimiento -> fecha
        if (raw.containsKey("fechaAcontecimiento") && !raw.containsKey("fecha")) {
            raw.put("fecha", raw.get("fechaAcontecimiento"));
        }

        // 3) contenidoMultimedia -> urlMultimedia (String)
        Object cm = raw.get("contenidoMultimedia");

        if (cm == null) {
            raw.put("urlMultimedia", null);
            raw.put("contenidoMultimedia", null);
            return;
        }

        // Caso A: viene como objeto JSON => Map con clave "url"
        if (cm instanceof Map<?, ?> cmMap) {
            Object urlObj = cmMap.get("url");
            String url = (urlObj != null) ? urlObj.toString().trim() : null;

            if (url == null || url.isBlank()) {
                raw.put("urlMultimedia", null);
                raw.put("contenidoMultimedia", null);
            } else {
                raw.put("urlMultimedia", url);
                raw.put("contenidoMultimedia", url); // también, por si el alias te juega en contra
            }
            return;
        }

        // Caso B: viene como string
        if (cm instanceof String s) {
            String v = s.trim();

            // si es el "toString" de una clase Java, NO sirve como url
            if (v.contains("ContenidoMultimedia@") || v.contains(".ContenidoMultimedia@")) {
                raw.put("urlMultimedia", null);
                raw.put("contenidoMultimedia", null);
            } else if (v.isBlank()) {
                raw.put("urlMultimedia", null);
                raw.put("contenidoMultimedia", null);
            } else {
                raw.put("urlMultimedia", v);
                raw.put("contenidoMultimedia", v);
            }
        }

        Object fuentesObj = raw.get("fuentes");
        if (fuentesObj instanceof List<?> list) {
            // si es lista, la dejamos (idealmente lista de strings)
            raw.put("fuentes", list.stream().map(Object::toString).toList());
            return;
        }

        // Caso 2: viene "fuente": "DINAMICO" (singular)
        Object fuenteObj = raw.get("fuente");
        if (fuenteObj != null) {
            raw.put("fuentes", List.of(fuenteObj.toString()));
        } else {
            raw.put("fuentes", List.of());
        }
    }

    public Map<String, Object> obtenerHechosPaginadosDesdeApi(String url) {
        try {
            Map response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("items")) {

                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                List<?> raw = (List<?>) response.get("items");

// ✅ normalizar ANTES del convertValue
                for (Object item : raw) {
                    if (item instanceof Map<?, ?> m) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> hecho = (Map<String, Object>) m;
                        normalizarHechoParaUi(hecho);
                    }
                }

                List<HechoDTO> hechos = raw.stream()
                        .map(item -> mapper.convertValue(item, HechoDTO.class))
                        .toList();

                Map<String, Object> result = new HashMap<>();
                result.put("items", hechos);
                result.put("page", response.getOrDefault("page", 0));
                result.put("totalPages", response.getOrDefault("totalPages", 0));
                result.put("totalItems", response.getOrDefault("totalItems", 0));
                result.put("size", response.getOrDefault("size", 10));

                return result;
            }
        } catch (Exception e) {
            System.err.println("ERROR FRONTEND (Hechos) " + e.getMessage());
            e.printStackTrace();
        }

        return Map.of(
                "items", List.of(),
                "page", 0,
                "totalPages", 0,
                "totalItems", 0,
                "size", 10
        );
    }

}
