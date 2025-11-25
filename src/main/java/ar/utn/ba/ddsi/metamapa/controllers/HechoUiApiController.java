package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-proxy") // 👈 ESTA ES LA RUTA QUE LLAMA TU JS
@RequiredArgsConstructor
public class HechoUiApiController {

  private final HechosUiService hechosService;

  @GetMapping("/hechos")
  public ResponseEntity<Map<String, Object>> getHechos(@RequestParam(defaultValue = "irrestricto") String modo) {
    try {
      // 1. Llamamos al servicio (que llama al backend)
      List<HechoDTO> hechos = hechosService.obtenerHechosDestacados(modo);

      // 2. Devolvemos el formato JSON exacto que espera tu JS: { "items": [...] }
      return ResponseEntity.ok(Map.of("items", hechos));

    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }
}