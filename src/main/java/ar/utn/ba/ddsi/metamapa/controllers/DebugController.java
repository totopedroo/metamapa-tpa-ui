package ar.utn.ba.ddsi.metamapa.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class DebugController {

  private final RestTemplate restTemplate;

  @GetMapping("/debug/conexion")
  public String probarConexion() {
    try {
      String url = "http://localhost:8080/api/colecciones/ultimas";
      // Traemos el JSON crudo como String
      String respuesta = restTemplate.getForObject(url, String.class);
      return "CONEXIÓN OK! Respuesta del Backend: <br>" + respuesta;
    } catch (Exception e) {
      return "ERROR DE CONEXIÓN: " + e.getMessage();
    }
  }
}