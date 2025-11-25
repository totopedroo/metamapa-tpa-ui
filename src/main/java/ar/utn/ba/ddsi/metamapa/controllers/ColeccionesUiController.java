package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/colecciones")
@RequiredArgsConstructor
public class ColeccionesUiController {

    private final ColeccionUiService coleccionService;

    /**
     * Muestra la lista de todas las colecciones.
     * GET /colecciones
     */
    @GetMapping
    public String listarColecciones(Model model) {
        // Llama al servicio para obtener la lista (asegúrate de tener este método en el servicio)
        List<ColeccionDTO> colecciones = coleccionService.listarColecciones();
        model.addAttribute("colecciones", colecciones);

        // Asegúrate de tener este template: src/main/resources/templates/lista-colecciones.html
        // O si está en carpeta: "colecciones/lista"
        return "lista-colecciones";
    }

    /**
     * Muestra el detalle de una colección.
     * GET /colecciones/{id}
     */
    @GetMapping("/{id}")
    public String verColeccion(@PathVariable Long id, Model model) {
        ColeccionDTO coleccion = coleccionService.getColeccionPorId(id);

        if (coleccion == null) {
            return "redirect:/colecciones"; // Si no existe, vuelve a la lista
        }

        model.addAttribute("coleccion", coleccion);
        return "detalle-coleccion"; // src/main/resources/templates/detalle-coleccion.html
    }

    /**
     * Formulario para nueva colección.
     * GET /colecciones/nueva
     */
    @GetMapping("/nueva")
    public String mostrarFormularioDeColeccion(Model model) {
        model.addAttribute("nuevaColeccion", new ColeccionDTO());
        return "formulario-coleccion"; // src/main/resources/templates/formulario-coleccion.html
    }

    /**
     * Procesa la creación.
     * POST /colecciones/crear
     */
    @PostMapping("/crear")
    public String crearColeccion(@ModelAttribute ColeccionDTO nuevaColeccion, RedirectAttributes redirectAttributes) {
        ColeccionDTO coleccionCreada = coleccionService.crearColeccion(nuevaColeccion);

        if (coleccionCreada != null) {
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Colección creada!");
            return "redirect:/colecciones/" + coleccionCreada.getId();
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al crear la colección.");
            return "redirect:/colecciones/nueva";
        }
    }

    /**
     * Eliminar colección.
     * GET /colecciones/eliminar/{id}
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarColeccion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            coleccionService.eliminarColeccion(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Colección eliminada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar.");
        }
        return "redirect:/colecciones";
    }
}