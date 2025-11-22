package ar.utn.ba.ddsi.metamapa.controllers;

// import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService; // Comentado
// import ar.utn.ba.ddsi.metamapa.services.HechosUiService;    // Comentado
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList; // Importar ArrayList

@Controller
@RequiredArgsConstructor
public class LandingController {

  // Comentamos la inyección de dependencias por ahora
  // private final ColeccionUiService coleccionService;
  // private final HechosUiService hechoService;

  @GetMapping(value = {"/", "/landing.html"})
  public String showLandingPage(Model model) {

    // --- OPCIÓN TEMPORAL: LISTAS VACÍAS ---
    // Pasamos listas vacías para que el HTML no falle en los th:each
    model.addAttribute("coleccionesDestacadas", new ArrayList<>());
    model.addAttribute("hechosDestacados", new ArrayList<>());

        /* --- LÓGICA ORIGINAL COMENTADA ---
        try {
            List<ColeccionDTO> colecciones = coleccionService.obtenerTodasLasColecciones();
            model.addAttribute("coleccionesDestacadas", colecciones);

            List<HechoDTO> hechos = hechoService.obtenerHechosDestacados("irrestricto");
            model.addAttribute("hechosDestacados", hechos);
        } catch (Exception e) {
            model.addAttribute("coleccionesDestacadas", new ArrayList<>());
            model.addAttribute("hechosDestacados", new ArrayList<>());
        }
        */

    return "landing/landing";
  }
}