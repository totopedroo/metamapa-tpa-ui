package ar.utn.ba.ddsi.gestionDeAlumnos.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.util.List;

@Controller
public class LandingController {

    // Simulación de DTOs (Data Transfer Objects) que vendrían de tu capa de servicio
    // En una aplicación real, estas clases estarían en su propio paquete.
    public static class ColeccionDTO {
        private String titulo, descripcion, handle;
        private int fuentes, hechos;
        // Constructor, Getters y Setters
        public ColeccionDTO(String t, String d, String h, int f, int he){ this.titulo=t; this.descripcion=d; this.handle=h; this.fuentes=f; this.hechos=he; }
        public String getTitulo() { return titulo; }
        public String getDescripcion() { return descripcion; }
        public String getHandle() { return handle; }
        public int getFuentes() { return fuentes; }
        public int getHechos() { return hechos; }
    }

    public static class HechoDTO {
        private String titulo, descripcion, categoria, lugar;
        private LocalDate fecha;
        // Constructor, Getters y Setters
        public HechoDTO(String t, String d, String c, String l, String f){ this.titulo=t; this.descripcion=d; this.categoria=c; this.lugar=l; this.fecha=LocalDate.parse(f); }
        public String getTitulo() { return titulo; }
        public String getDescripcion() { return descripcion; }
        public String getCategoria() { return categoria; }
        public String getLugar() { return lugar; }
        public LocalDate getFecha() { return fecha; }
    }


    @GetMapping("/landing.html")
    public String showLandingPage(Model model) {

        // 1. Obtener datos (esto vendría de tus servicios y repositorios)
        List<ColeccionDTO> colecciones = obtenerColeccionesDestacadas();
        List<HechoDTO> hechos = obtenerHechosDestacadosIrrestrictos();

        // 2. Agregar los datos al modelo para que Thymeleaf los pueda usar
        model.addAttribute("coleccionesDestacadas", colecciones);
        model.addAttribute("hechosDestacados", hechos);

        // 3. Devolver la ruta a la plantilla
        return "landing/landing";
    }

    // --- MÉTODOS DE SIMULACIÓN (reemplazar con llamadas a tus servicios) ---

    private List<ColeccionDTO> obtenerColeccionesDestacadas() {
        return List.of(
            new ColeccionDTO("Incendios forestales 2025", "Seguimiento de focos activos y prevención.", "incendios-arg-2025", 3, 128),
            new ColeccionDTO("Desapariciones por crímenes de odio", "Registro y visibilización para impulsar acciones.", "desapariciones-odio", 4, 56),
            new ColeccionDTO("Contaminación hídrica Patagonia", "Reportes de efluentes y derrames.", "contaminacion-hidrica-patagonia", 2, 73),
            new ColeccionDTO("Violencia institucional", "Hechos reportados por organizaciones.", "violencia-institucional", 5, 91)
        );
    }

    private List<HechoDTO> obtenerHechosDestacadosIrrestrictos() {
        return List.of(
            new HechoDTO("Foco activo en cordón serrano", "Columna de humo visible desde ruta provincial.", "Incendio forestal", "Córdoba", "2025-08-01"),
            new HechoDTO("Derrame en arroyo local", "Vecinos reportan manchas y olor fuerte.", "Contaminación", "Neuquén", "2025-07-18"),
            new HechoDTO("Avistaje de quema de pastizales", "Imágenes aportadas por voluntariado.", "Quema", "Corrientes", "2025-06-30"),
            new HechoDTO("Denuncia por desaparición", "Solicitud de colaboración de ONG local.", "Desaparición", "Santa Fe", "2025-08-10")
        );
    }
}