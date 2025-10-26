package ar.utn.ba.ddsi.gestionDeAlumnos.controllers;

// Importa tu nuevo servicio
import ar.utn.ba.ddsi.gestionDeAlumnos.dto.HechoDTO;
import ar.utn.ba.ddsi.gestionDeAlumnos.services.ColeccionService;
import ar.utn.ba.ddsi.gestionDeAlumnos.services.HechoService;
import ar.utn.ba.ddsi.gestionDeAlumnos.dto.ColeccionDTO; // Importa el DTO

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class LandingController {

    // 1. Inyectamos el nuevo servicio
    private final ColeccionService coleccionService;
    private final HechoService hechoService;


    @GetMapping("/landing.html")
    public String showLandingPage(Model model) {

        // Carga colecciones (como antes)
        List<ColeccionDTO> colecciones = coleccionService.obtenerTodasLasColecciones();

        // 6. Carga hechos "irrestrictos" reales desde la API
        List<HechoDTO> hechos = hechoService.obtenerHechosDestacados("irrestricto");

        // Agregar los datos al modelo
        model.addAttribute("coleccionesDestacadas", colecciones);
        model.addAttribute("hechosDestacados", hechos); // Ahora contiene datos reales

        return "landing/landing";
    }
}