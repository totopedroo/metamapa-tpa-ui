package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.SolicitudDTO;
import ar.utn.ba.ddsi.metamapa.dto.SolicitudModificacionInputDto; // Asegúrate de tener este DTO
import ar.utn.ba.ddsi.metamapa.dto.SolicitudModificacionInputDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitudesService {

    private final RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:8080}")
    private String apiBaseUrl;

    // --- HELPER PRIVADO ---
    private HttpEntity<Void> entityWithToken(HttpSession session) {
        // CORRECCIÓN: Usamos "jwt" que es como lo guarda tu LoginController
        String token = (String) session.getAttribute("jwt");

        if (token == null) {
            System.err.println(">>> WARNING: Token 'jwt' es nulo en sesión.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token != null ? token : "");

        return new HttpEntity<>(headers);
    }

    // =========================================================
    //  SECCIÓN 1: SOLICITUDES DE ELIMINACIÓN (Tab 1)
    // =========================================================

    /**
     * Trae las solicitudes de BAJA de hechos (Eliminación).
     * Endpoint Backend: GET /solicitudes/admin
     */
    public List<SolicitudDTO> obtenerSolicitudesEliminacion(HttpSession session) {
        try {
            ResponseEntity<SolicitudDTO[]> response = restTemplate.exchange(
                    apiBaseUrl + "/solicitudes/admin",
                    HttpMethod.GET,
                    entityWithToken(session),
                    SolicitudDTO[].class
            );
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("Error listando eliminaciones: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Aprueba o Rechaza una eliminación.
     * Endpoint Backend: POST /solicitudes/{id}/{accion}
     */
    public void actualizarEstadoEliminacion(Long id, String accion, HttpSession session) {
        try {
            restTemplate.exchange(
                    apiBaseUrl + "/solicitudes/" + id + "/" + accion,
                    HttpMethod.POST,
                    entityWithToken(session),
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Error gestionando eliminación (" + accion + "): " + e.getMessage());
        }
    }

    // =========================================================
    //  SECCIÓN 2: SOLICITUDES DE MODIFICACIÓN (Tab 2 - NUEVO)
    // =========================================================

    /**
     * Trae las solicitudes de CAMBIO de campos (Modificación).
     * Endpoint Backend: GET /api/solicitudes-modificacion/pendientes (o el que hayas definido)
     */
    public List<SolicitudModificacionInputDto> obtenerSolicitudesModificacion(HttpSession session) {
        try {
            // Asegúrate que esta URL coincida con tu Backend Controller de Modificaciones
            String url = apiBaseUrl + "/api/solicitudes-modificacion/pendientes";

            ResponseEntity<SolicitudModificacionInputDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entityWithToken(session),
                    SolicitudModificacionInputDto[].class
            );
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("Error listando modificaciones: " + e.getMessage());
            return List.of();
        }
    }

    public List<SolicitudDTO> listarPorUsuario(Long usuarioId, HttpSession session) {
        try {
            ResponseEntity<SolicitudDTO[]> response =
                    restTemplate.exchange(
                            apiBaseUrl + "/solicitudes/usuarios/" + usuarioId,
                            HttpMethod.GET,
                            entityWithToken(session),
                            SolicitudDTO[].class
                    );
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            System.err.println("Error listando solicitudes usuario: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Aprueba o Rechaza una modificación.
     * Endpoint Backend: POST /api/solicitudes-modificacion/{id}/{accion}
     */
    public void gestionarModificacion(Long id, String accion, HttpSession session) {
        try {
            String url = apiBaseUrl + "/api/solicitudes-modificacion/" + id + "/" + accion;

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entityWithToken(session),
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Error gestionando modificación (" + accion + "): " + e.getMessage());
        }
    }

    // =========================================================
    //  SECCIÓN 3: MÉTODOS DEL CONTRIBUYENTE (Creación)
    // =========================================================

    public void crearSolicitudEliminacion(Long idHecho, Long usuarioId, HttpSession session) {
        try {
            var body = Map.of(
                    "idHecho", idHecho,
                    "idUsuario", usuarioId,
                    "justificacion", "Solicitud de eliminación"
            );

            // Reutilizamos el helper para los headers, pero agregamos el Content-Type JSON
            HttpHeaders headers = entityWithToken(session).getHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    apiBaseUrl + "/solicitudes",
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Error creando solicitud eliminación: " + e.getMessage());
        }
    }
}