package ar.utn.ba.ddsi.gestionDeAlumnos.controllers;

// Importa tu nuevo servicio
import ar.utn.ba.ddsi.gestionDeAlumnos.services.ColeccionService;
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

    // Simulación de HechoDTO (tal como estaba antes, para no romper nada)
    public static class HechoDTO {
        private String titulo, descripcion, categoria, lugar;
        private LocalDate fecha;
        public HechoDTO(String t, String d, String c, String l, String f){ this.titulo=t; this.descripcion=d; this.categoria=c; this.lugar=l; this.fecha=LocalDate.parse(f); }
        public String getTitulo() { return titulo; }
        public String getDescripcion() { return descripcion; }
        public String getCategoria() { return categoria; }
        public String getLugar() { return lugar; }
        public LocalDate getFecha() { return fecha; }
    }


    @GetMapping("/landing.html")
    public String showLandingPage(Model model) {

        // 2. Obtenemos colecciones REALES desde el servicio
        List<ColeccionDTO> colecciones = coleccionService.obtenerTodasLasColecciones();

        // Obtenemos hechos (aún simulados, como antes)
        List<HechoDTO> hechos = obtenerHechosDestacadosIrrestrictos();

        // 3. Agregar los datos al modelo
        model.addAttribute("coleccionesDestacadas", colecciones);
        model.addAttribute("hechosDestacados", hechos);

        return "landing/landing";
    }

    // Método de simulación de Hechos (lo dejamos por ahora)
    private List<HechoDTO> obtenerHechosDestacadosIrrestrictos() {
        return List.of(
            new HechoDTO("Foco activo en cordón serrano", "Columna de humo visible desde ruta provincial.", "Incendio forestal", "Córdoba", "2025-08-01"),
            new HechoDTO("Derrame en arroyo local", "Vecinos reportan manchas y olor fuerte.", "Contaminación", "Neuquén", "2025-07-18"),
            new HechoDTO("Avistaje de quema de pastizales", "Imágenes aportadas por voluntariado.", "Quema", "Corrientes", "2025-06-30"),
            new HechoDTO("Denuncia por desaparición", "Solicitud de colaboración de ONG local.", "Desaparición", "Santa Fe", "2025-08-10")
        );
    }
}