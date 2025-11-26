package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.SolicitudDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitudesService {

    private final RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:8080}")
    private String apiBaseUrl;

    private HttpEntity<Void> entityWithToken(HttpSession session) {
        String token = (String) session.getAttribute("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        return new HttpEntity<>(headers); // sin body
    }

    // =============================
    // ADMIN - listar todas
    // =============================
    public List<SolicitudDTO> listarTodas(HttpSession session) {
        try {
            ResponseEntity<SolicitudDTO[]> response =
                    restTemplate.exchange(
                            apiBaseUrl + "/solicitudes/admin",
                            HttpMethod.GET,
                            entityWithToken(session),
                            SolicitudDTO[].class
                    );

            return Arrays.asList(response.getBody());

        } catch (Exception e) {
            System.err.println("Error obteniendo solicitudes de admin: " + e.getMessage());
            return List.of();
        }
    }

    // =============================
    // CONTRIBUYENTE - listar por usuario
    // =============================
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

    // =============================
    // CONTRIBUYENTE - crear solicitud
    // =============================
    public void crearSolicitud(Long idHecho, Long usuarioId, HttpSession session) {
        try {
            var body = Map.of(
                    "idHecho", idHecho,
                    "idUsuario", usuarioId,
                    "justificacion", "Solicitud de eliminación"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + session.getAttribute("accessToken"));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    apiBaseUrl + "/solicitudes",
                    entity,
                    Void.class
            );

        } catch (Exception e) {
            System.err.println("Error creando solicitud: " + e.getMessage());
        }
    }

    // =============================
    // ADMIN - actualizar estado
    // =============================
    public void actualizarEstado(Long id, String accion, HttpSession session) {
        try {
            restTemplate.exchange(
                    apiBaseUrl + "/solicitudes/" + id + "/" + accion,
                    HttpMethod.POST,
                    entityWithToken(session),
                    Void.class
            );

        } catch (Exception e) {
            System.err.println("Error actualizando estado: " + e.getMessage());
        }
    }
}