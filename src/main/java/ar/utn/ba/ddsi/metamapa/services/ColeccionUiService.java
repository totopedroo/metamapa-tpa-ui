package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColeccionUiService {

    private final RestTemplate restTemplate;

    // Se conecta a tu backend (puerto 8080)
    @Value("${api.base.url:http://localhost:8080}")
    private String apiBaseUrl;

    /**
     * Obtiene las últimas 5 colecciones (para la Landing Page).
     * GET /api/colecciones/ultimas
     */
    public List<ColeccionDTO> obtenerUltimasColecciones() {
        String url = apiBaseUrl + "/api/colecciones/ultimas";
        try {
            ColeccionDTO[] respuesta = restTemplate.getForObject(url, ColeccionDTO[].class);
            return respuesta != null ? Arrays.asList(respuesta) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error obteniendo últimas colecciones: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Trae TODAS las colecciones (para la vista de administración/lista).
     * GET /api/colecciones
     */
    public List<ColeccionDTO> listarColecciones() {
        String url = apiBaseUrl + "/api/colecciones";
        try {
            ColeccionDTO[] respuesta = restTemplate.getForObject(url, ColeccionDTO[].class);
            return respuesta != null ? Arrays.asList(respuesta) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error al listar colecciones: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene el detalle de una colección específica.
     * GET /api/colecciones/{id}
     */
    public ColeccionDTO getColeccionPorId(Long id) {
        String url = apiBaseUrl + "/api/colecciones/" + id;
        try {
            return restTemplate.getForObject(url, ColeccionDTO.class);
        } catch (Exception e) {
            System.err.println("Error obteniendo colección ID " + id + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea una nueva colección.
     * POST /api/colecciones
     */
    public ColeccionDTO crearColeccion(ColeccionDTO dto) {
        String url = apiBaseUrl + "/api/colecciones";
        try {
            return restTemplate.postForObject(url, dto, ColeccionDTO.class);
        } catch (Exception e) {
            System.err.println("Error al crear colección: " + e.getMessage());
            return null;
        }
    }

    /**
     * Elimina una colección.
     * DELETE /api/colecciones/{id}
     */
    public void eliminarColeccion(Long id) {
        String url = apiBaseUrl + "/api/colecciones/" + id;
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (Exception e) {
            System.err.println("Error eliminando colección ID " + id + ": " + e.getMessage());
        }
    }
}