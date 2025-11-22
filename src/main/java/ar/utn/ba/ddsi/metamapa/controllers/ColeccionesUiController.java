package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.API.ColeccionesApiClient;
import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/colecciones")

public class ColeccionesUiController {

    @Controller
    @RequestMapping("/colecciones") // Prefijo para todas las URLs de este controlador
    public class ColeccionViewController {

        @Autowired
        private ColeccionUiService coleccionService;

        /**
         * Muestra la lista de todas las colecciones.
         */
        @GetMapping
        public String listarColecciones(Model model) {
            List<ColeccionDTO> colecciones = coleccionService.listarColecciones();
            model.addAttribute("colecciones", colecciones);
            return "lista-colecciones"; // vista-colecciones.html
        }

        /**
         * Muestra el detalle de una colección (incluyendo sus hechos).
         */
        @GetMapping("/{id}")
        public String verColeccion(@PathVariable Long id, Model model) {
            ColeccionDTO coleccion = coleccionService.getColeccionPorId(id);
            if (coleccion == null) {
                return "redirect:/colecciones"; // Si no existe, vuelve a la lista
            }
            model.addAttribute("coleccion", coleccion);
            // NOTA: ColeccionOutputBD parece tener solo IDs de hechos.
            // Si necesitas los hechos completos, tu API de backend necesitaría
            // un endpoint como /colecciones/{id}/hechos y deberías llamarlo.
            return "detalle-coleccion"; // detalle-coleccion.html
        }


        /**
         * Muestra el formulario para crear una nueva colección.
         */
        @GetMapping("/nueva")
        public String mostrarFormularioDeColeccion(Model model) {
            model.addAttribute("nuevaColeccion", new ColeccionDTO());
            // Nota: Necesitarás cargar listas de Hechos y Criterios existentes
            // si quieres que el usuario los seleccione en el formulario.
            // model.addAttribute("hechosDisponibles", hechosService.filtrarHechos(null,...));
            return "formulario-coleccion"; // formulario-coleccion.html
        }

        /**
         * Procesa el envío del formulario para crear una nueva colección.
         */
        @PostMapping("/crear")
        public String crearColeccion(@ModelAttribute ColeccionDTO nuevaColeccion, RedirectAttributes redirectAttributes) {
            ColeccionDTO coleccionCreada = coleccionService.crearColeccion(nuevaColeccion);

            if (coleccionCreada != null) {
                redirectAttributes.addFlashAttribute("mensajeExito", "¡Colección creada!");
                return "redirect:/colecciones/" + coleccionCreada.getId(); // Redirige al detalle
            } else {
                redirectAttributes.addFlashAttribute("mensajeError", "Error al crear la colección.");
                return "redirect:/colecciones/nueva"; // Vuelve al formulario
            }
        }

        /**
         * Maneja la eliminación de una colección.
         * Se usa un GET por simplicidad, aunque un POST/DELETE sería más correcto (requiere un <form>).
         */
        @GetMapping("/eliminar/{id}")
        public String eliminarColeccion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                coleccionService.eliminarColeccion(id);
                redirectAttributes.addFlashAttribute("mensajeExito", "Colección eliminada.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar.");
            }
            return "redirect:/colecciones"; // Vuelve a la lista
        }
    }
}