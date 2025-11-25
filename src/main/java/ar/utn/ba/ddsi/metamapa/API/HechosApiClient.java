package ar.utn.ba.ddsi.metamapa.API;

import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;


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
/*
    public void importarHechosCsv(MultipartFile file) {
        // 1. EL TOKEN HARDCODEADO
        // (Cópialo fresco de Postman porque si expiró te dará 403 igual)
        String tokenHardcodeado = "<JWT_REDACTED>";

        try {
            // 2. Preparamos el cuerpo "Multipart"
            // Spring necesita un MultiValueMap para enviar archivos
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // ¡OJO! Usamos .getResource(). RestClient necesita el recurso, no el MultipartFile directo.
            body.add("file", file.getResource());

            // 3. Construimos y ejecutamos la petición
            DefaultRestClientBuilder restClientBuilder;
            restClientBuilder
                    .baseUrl(backendUrl) // O pon la URL completa abajo: .uri("http://localhost...")
                    .build()
                    .post()
                    .uri("/hechos/importar") // Ajusta esto a tu endpoint real del Backend
                    .contentType(MediaType.MULTIPART_FORM_DATA) // <--- Clave para subir archivos
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHardcodeado) // <--- Token pegado aquí
                    .body(body)
                    .retrieve()
                    .toBodilessEntity(); // Esperamos un 200 OK sin cuerpo

            System.out.println(">>> CSV enviado al backend correctamente.");

        } catch (Exception e) {
            // Imprimimos el error completo para que veas si es 403, 400 o 500
            System.err.println(">>> Error al subir CSV: " + e.getMessage());
            throw new RuntimeException("Fallo al importar: " + e.getMessage());
        }
    }

 */
}
