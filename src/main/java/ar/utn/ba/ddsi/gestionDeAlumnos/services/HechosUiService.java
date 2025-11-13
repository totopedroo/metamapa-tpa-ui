package ar.utn.ba.ddsi.gestionDeAlumnos.services;

import ar.utn.ba.ddsi.gestionDeAlumnos.API.CookieForwarder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HechosUiService {
    private final RestClient api;
    private final CookieForwarder cookies;

    public List<Map<String,Object>> listar(HttpServletRequest req) {
        return api.get().uri("/hechos")
                .headers(cookies.from(req))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String,Object> crear(HttpServletRequest req, Map<String,Object> body) {
        return api.post().uri("/hechos/crear")
                .headers(cookies.from(req))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void eliminar(HttpServletRequest req, Long id) {
        api.delete().uri("/hechos/{id}", id)
                .headers(cookies.from(req))
                .retrieve()
                .toBodilessEntity();
    }
}