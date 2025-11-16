package ar.utn.ba.ddsi.gestionDeAlumnos.services;
import ar.utn.ba.ddsi.gestionDeAlumnos.dto.ColeccionDTO;
import lombok.RequiredArgsConstructor;
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
    private final String BACKEND_API_URL = "http://localhost:8080/colecciones";

    /**
     * Llama a GET /colecciones en el backend
     */
    public List<ColeccionDTO> listarColecciones() {
        String url = BACKEND_API_URL;
        try {
            ColeccionDTO[] respuesta = restTemplate.getForObject(url, ColeccionDTO[].class);
            return Arrays.asList(respuesta != null ? respuesta : new ColeccionDTO[0]);
        } catch (Exception e) {
            System.err.println("Error al listar colecciones: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Llama a POST /colecciones en el backend
     */
    public ColeccionDTO crearColeccion(ColeccionDTO nuevaColeccion) {
        String url = BACKEND_API_URL;
        try {
            return restTemplate.postForObject(url, nuevaColeccion, ColeccionDTO.class);
        } catch (Exception e) {
            System.err.println("Error al crear colección: " + e.getMessage());
            return null;
        }
    }

    /**
     * Llama a DELETE /colecciones/eliminar/{id} en el backend
     */
    public void eliminarColeccion(Long id) {
        String url = BACKEND_API_URL + "/eliminar/" + id;
        try {
            // Usamos exchange para el método DELETE
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (Exception e) {
            System.err.println("Error al eliminar colección: " + e.getMessage());
        }
    }

    /**
     * Llama a GET /colecciones/{id} en el backend
     */
    public ColeccionDTO getColeccionPorId(Long id) {
        String url = BACKEND_API_URL + "/" + id;
        try {
            return restTemplate.getForObject(url, ColeccionDTO.class);
        } catch (Exception e) {
            System.err.println("Error al obtener colección: " + e.getMessage());
            return null;
        }
    }

    // ... Aquí puedes agregar métodos para los otros endpoints
    // ej: editar, agregarHechoAColeccion, etc.
}