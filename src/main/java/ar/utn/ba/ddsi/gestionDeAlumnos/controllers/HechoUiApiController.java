package ar.utn.ba.ddsi.gestionDeAlumnos.controllers;

import ar.utn.ba.ddsi.gestionDeAlumnos.dto.HechoDTO;
import ar.utn.ba.ddsi.gestionDeAlumnos.services.HechoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-proxy") // Usamos un prefijo nuevo y público
@RequiredArgsConstructor
public class HechoUiApiController {

    private final HechoService hechoService; // El mismo servicio que usa LandingController

    @GetMapping("/hechos")
    public ResponseEntity<Map<String, Object>> getHechos(
        @RequestParam(defaultValue = "irrestricto") String modo) {

        // 1. Llama al servicio que ya arreglamos en el Paso 1
        List<HechoDTO> hechos = hechoService.obtenerHechosDestacados(modo);

        // 2. Envuelve la respuesta en el formato { "items": [...] }
        //    que espera el landing.js
        Map<String, Object> response = Map.of("items", hechos);

        return ResponseEntity.ok(response);
    }
}