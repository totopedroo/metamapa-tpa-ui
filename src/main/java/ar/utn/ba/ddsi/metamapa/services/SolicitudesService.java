package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.CampoHecho;
import ar.utn.ba.ddsi.metamapa.dto.SolicitudDTO;
import ar.utn.ba.ddsi.metamapa.dto.SolicitudModificacionInputDto; // Asegúrate de tener este DTO
import ar.utn.ba.ddsi.metamapa.dto.SolicitudModificacionInputDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    public List<SolicitudModificacionInputDto> obtenerSolicitudesModificacionUsuario(HttpSession session) {
        try {

            Long idUsuario = (Long) session.getAttribute("usuarioId");
            if (idUsuario == null) throw new RuntimeException("Usuario no encontrado en sesión");

            String url = apiBaseUrl + "/api/solicitudes-modificacion/usuario/" + idUsuario;

            ResponseEntity<SolicitudModificacionInputDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entityWithToken(session),
                    SolicitudModificacionInputDto[].class
            );
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("Error listando modificaciones para el Usuario: " + e.getMessage());
            return List.of();
        }
    }



    public List<SolicitudDTO> listarPorUsuario(HttpSession session) {
        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) throw new RuntimeException("Usuario no encontrado en sesión");
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

    public void crearSolicitudEliminacion(Long idHecho,  String justificacion, HttpSession session) {
        try {
            Long idUsuario = (Long) session.getAttribute("usuarioId");
            if (idUsuario == null) throw new RuntimeException("Usuario no encontrado en sesión");
            // Armamos el JSON exacto que espera tu SolicitudInputDto del Backend
            var body = Map.of(
                    "idHecho", idHecho,
                    "idUsuario", idUsuario,
                    "justificacion", justificacion // <--- Usamos el valor real
            );

            String token = (String) session.getAttribute("jwt");


            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token); // Le ponemos el token
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    apiBaseUrl + "/solicitudes", // Asegurate que el controller del back mapee a esto
                    entity,
                    Void.class
            );

            System.out.println(">>> Solicitud de eliminación enviada.");

        } catch (Exception e) {
            System.err.println("Error creando solicitud eliminación: " + e.getMessage());
            throw new RuntimeException("Error al enviar solicitud: " + e.getMessage());
        }
    }
        // En SolicitudesService.java

    public void crearSolicitudModificacion(Long idHecho, String campoStr, String valorNuevo, String justificacion, HttpSession session) {
        try {

            Long idUsuario = (Long) session.getAttribute("usuarioId");
            if (idUsuario == null) throw new RuntimeException("Usuario no encontrado en sesión");

            SolicitudModificacionInputDto dto = new SolicitudModificacionInputDto();
            dto.setIdHecho(idHecho);
            dto.setIdContribuyente(idUsuario);
            // --- PUNTO CRÍTICO: CONVERSIÓN DEL ENUM ---
            try {
                // Aseguramos que esté en mayúsculas y sin espacios
                String campoNormalizado = campoStr.trim().toUpperCase();

                // Usamos el Enum del FRONTEND (asegúrate de importar el correcto)
                ar.utn.ba.ddsi.metamapa.dto.CampoHecho campoEnum =
                        ar.utn.ba.ddsi.metamapa.dto.CampoHecho.valueOf(campoNormalizado);

                dto.setCampo(campoEnum);

            } catch (IllegalArgumentException e) {
                throw new RuntimeException("El campo '" + campoStr + "' no es válido. Valores permitidos: TITULO, DESCRIPCION, etc.");
            }
            // ------------------------------------------

            dto.setValorNuevo(valorNuevo);
            dto.setJustificacion(justificacion);

            // 3. Headers
            String token = (String) session.getAttribute("jwt");

            // 2. Creamos HEADERS NUEVOS (Modificables)
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token); // Le ponemos el token
            headers.setContentType(MediaType.APPLICATION_JSON); // Le ponemos el tipo JSON

            // 3. Creamos la entidad con estos headers nuevos
            HttpEntity<SolicitudModificacionInputDto> request = new HttpEntity<>(dto, headers);

            // 5. Enviar
            restTemplate.postForEntity(
                    apiBaseUrl + "/api/solicitudes-modificacion", // Chequea que esta URL sea real en el Back
                    request,
                    String.class
            );


        } catch (HttpClientErrorException e) {
            // Captura errores 400/403/500 del Backend y muestra el mensaje real
            System.err.println(">>> ERROR BACKEND: " + e.getResponseBodyAsString());
            throw new RuntimeException("El backend rechazó la solicitud: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println(">>> ERROR INTERNO FRONT: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error local: " + e.getMessage());
        }
    }

}