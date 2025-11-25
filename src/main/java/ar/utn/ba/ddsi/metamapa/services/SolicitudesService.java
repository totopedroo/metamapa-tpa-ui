package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.SolicitudDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SolicitudesService {

    @Autowired
    private RestTemplate restTemplate;

    /*
        public List<SolicitudDTO> obtenerSolicitudes() {
            try {
                String url = "http://localhost:8080/solicitudes/admin";
                ResponseEntity<SolicitudDTO[]> response = restTemplate.getForEntity(url, SolicitudDTO[].class);
                if (response.getBody() != null) {
                    return Arrays.asList(response.getBody());
                }

            } catch (Exception e) {
                System.err.println("Error al conectar con el backend: " + e.getMessage());
            }


            return new ArrayList<>();
        }

     */
    public List<SolicitudDTO> obtenerSolicitudes() {
        try {
            String url = "http://localhost:8080/solicitudes/admin";
            String token = "<JWT_REDACTED>";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token); // <--- La magia ocurre acá
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<SolicitudDTO[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,          // Pasamos el sobre
                    SolicitudDTO[].class
            );

            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public void actualizarEstadoSolicitud(Long id, String accion) {
        try {
            String url = "http://localhost:8080/solicitudes/" + id + "/" + accion;
            String token = "<JWT_REDACTED>";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token); // <--- La magia ocurre acá
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Solicitud " + id + " -> " + accion + " exitosa.");
        } catch (Exception e) {
            System.err.println("Error al " + accion + " la solicitud: " + e.getMessage());
            // Opcional: Lanzar una excepción para que el Controller se entere y muestre error en pantalla
        }
    }
}