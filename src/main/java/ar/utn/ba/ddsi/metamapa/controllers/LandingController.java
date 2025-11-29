package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class LandingController {

  private final ColeccionUiService coleccionService;
  private final HechosUiService hechoService;

  @GetMapping(value = {"/", "/landing"})
  public String showLandingPage(Model model) {
    try {
      // 1. Traer Últimas Colecciones (desde el backend)
      // Llamamos al servicio real en lugar de usar una lista vacía
      List<ColeccionDTO> colecciones = coleccionService.obtenerUltimasColecciones();
      model.addAttribute("coleccionesDestacadas", colecciones);

      // 2. Traer Últimos Hechos (desde el backend)
      // Pedimos hechos "irrestrictos" para la vista pública inicial
      List<HechoDTO> hechos = hechoService.obtenerHechosDestacados("irrestricto");
      model.addAttribute("hechosDestacados", hechos);

      // Logs para depuración en consola
      System.out.println(">>> LANDING: Colecciones cargadas: " + (colecciones != null ? colecciones.size() : 0));
      System.out.println(">>> LANDING: Hechos cargados: " + (hechos != null ? hechos.size() : 0));

    } catch (Exception e) {
      System.err.println("Error cargando datos de la landing: " + e.getMessage());
      e.printStackTrace();
      // Fallback: Si falla el backend, mostramos listas vacías para no romper la página HTML
      model.addAttribute("coleccionesDestacadas", new ArrayList<>());
      model.addAttribute("hechosDestacados", new ArrayList<>());
    }

    return "landing/landing";
  }
}