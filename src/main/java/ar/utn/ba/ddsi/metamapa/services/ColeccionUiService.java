package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.AlgoritmoDTO;
import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.ColeccionFormDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColeccionUiService {

    private final RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:8080}")
    private String apiBaseUrl;

    private String url(String path) {
        return apiBaseUrl + path;
    }

    // --- MÉTODOS PARA ALGORITMOS DE CONSENSO (MODAL) ---

    /**
     * Obtiene la lista de algoritmos disponibles desde el backend.
     * GET /api/colecciones/algoritmos
     */
    public List<AlgoritmoDTO> listarAlgoritmos() {
        try {
            AlgoritmoDTO[] resp = restTemplate.getForObject(url("/api/colecciones/algoritmos"), AlgoritmoDTO[].class);
            return resp != null ? Arrays.asList(resp) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error listando algoritmos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Asocia un algoritmo a una colección.
     * PUT /api/colecciones/{id}/asociar-algoritmo?algoritmoId={id}
     */
    public void asociarAlgoritmo(Long coleccionId, Long algoritmoId) {
        String queryParam = (algoritmoId != null) ? "?algoritmoId=" + algoritmoId : "";
        String uri = url("/api/colecciones/" + coleccionId + "/asociar-algoritmo" + queryParam);

        try {
            restTemplate.put(uri, null);
        } catch (Exception e) {
            System.err.println("Error asociando algoritmo: " + e.getMessage());
            throw new RuntimeException("Error al asociar algoritmo");
        }
    }

    // --- Últimas ---
    public List<ColeccionDTO> obtenerUltimasColecciones() {
        try {
            var arr = restTemplate.getForObject(url("/api/colecciones/ultimas"), ColeccionDTO[].class);
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // --- Listar ---
    public List<ColeccionDTO> listarColecciones() {
        try {
            var arr = restTemplate.getForObject(url("/api/colecciones"), ColeccionDTO[].class);
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // --- Obtener una ---
    public ColeccionDTO getColeccionPorId(Long id) {
        try {
            return restTemplate.getForObject(url("/api/colecciones/" + id), ColeccionDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    // --- Crear ---
    public ColeccionDTO crearColeccion(ColeccionFormDTO form) {
        try {
            return restTemplate.postForObject(url("/api/colecciones"), form, ColeccionDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    // --- Editar ---
    public ColeccionDTO editarColeccion(Long id, ColeccionFormDTO form) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ColeccionFormDTO> req = new HttpEntity<>(form, headers);

            var resp = restTemplate.exchange(
                    url("/api/colecciones/editar/" + id),
                    HttpMethod.PATCH,
                    req,
                    ColeccionDTO.class
            );

            return resp.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    // --- Eliminar ---
    public void eliminarColeccion(Long id) {
        try {
            restTemplate.exchange(
                    url("/api/colecciones/eliminar/" + id),
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );
        } catch (Exception ignored) {}
    }
}