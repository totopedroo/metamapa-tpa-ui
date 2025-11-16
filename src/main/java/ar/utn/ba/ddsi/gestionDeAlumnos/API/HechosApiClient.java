package ar.utn.ba.ddsi.gestionDeAlumnos.API;

import ar.utn.ba.ddsi.gestionDeAlumnos.dto.HechoDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HechosApiClient {

    private final RestClient backendClient;

    private void copyAuth(RestClient.RequestHeadersSpec<?> spec, HttpServletRequest req) {
        String cookie = req.getHeader(HttpHeaders.COOKIE);
        if (cookie != null) spec.headers(h -> h.add(HttpHeaders.COOKIE, cookie));

        // Si tu backend, además de cookie, acepta Authorization:
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) spec.headers(h -> h.add(HttpHeaders.AUTHORIZATION, auth));
    }

    public List<HechoDTO> listar(HttpServletRequest req) {
        HechoDTO[] arr = backendClient.get()
                .uri("/hechos")
                .headers(headers -> copyAuth(headers, req))
                .retrieve()
                .body(HechoDTO[].class);  // RestClient

        return arr == null ? List.of() : Arrays.asList(arr);
    }

    private void copyAuth(HttpHeaders target, HttpServletRequest req) {
        // Copiar cookies (sesión, etc.)
        String cookie = req.getHeader(HttpHeaders.COOKIE);
        if (cookie != null && !cookie.isBlank()) {
            target.add(HttpHeaders.COOKIE, cookie);
        }

        // Si usás Authorization (Bearer, Basic, etc.)
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isBlank()) {
            target.add(HttpHeaders.AUTHORIZATION, auth);
        }
    }
    public HechoDTO crear(HechoDTO dto, HttpServletRequest req) {
        return backendClient.post()
                .uri("/hechos")
                .headers(h -> {
                    String cookie = req.getHeader(HttpHeaders.COOKIE);
                    if (cookie != null) h.add(HttpHeaders.COOKIE, cookie);
                    String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
                    if (auth != null) h.add(HttpHeaders.AUTHORIZATION, auth);
                })
                .body(dto)
                .retrieve()
                .body(HechoDTO.class);
    }
}
