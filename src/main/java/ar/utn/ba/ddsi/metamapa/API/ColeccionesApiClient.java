package ar.utn.ba.ddsi.metamapa.API;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColeccionesApiClient {

    private final RestClient backendClient;

    private void addAuth(org.springframework.http.HttpHeaders h, HttpServletRequest req) {
        String cookie = req.getHeader(HttpHeaders.COOKIE);
        if (cookie != null) h.add(HttpHeaders.COOKIE, cookie);
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) h.add(HttpHeaders.AUTHORIZATION, auth);
    }

    public List<ColeccionDTO> listar(HttpServletRequest req) {
        ColeccionDTO[] arr = backendClient.get()
                .uri("/colecciones")
                .headers(h -> addAuth(h, req))
                .retrieve()
                .body(ColeccionDTO[].class);
        return arr == null ? List.of() : Arrays.asList(arr);
    }

    public ColeccionDTO editar(Long id, ColeccionDTO dto, HttpServletRequest req) {
        return backendClient.patch()
                .uri("/colecciones/editar/{id}", id)
                .headers(h -> addAuth(h, req))
                .body(dto)
                .retrieve()
                .body(ColeccionDTO.class);
    }

    public void eliminar(Long id, HttpServletRequest req) {
        backendClient.delete()
                .uri("/colecciones/eliminar/{id}", id)
                .headers(h -> addAuth(h, req))
                .retrieve()
                .toBodilessEntity();
    }
}