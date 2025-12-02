package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.API.HechosApiClient;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HechosUiService hechosService;
    private final ColeccionUiService coleccionService;

    @GetMapping(value = {"", "/", "/inicio"})
    public String inicioAdmin(Model model, HttpSession session) {
        // validarAdmin(session); // Descomentar si usas validación por sesión manual

        // Cargar listas para los selects del Modal de Consenso
        model.addAttribute("listaColecciones", coleccionService.listarColecciones());
        model.addAttribute("listaAlgoritmos", coleccionService.listarAlgoritmos());
        model.addAttribute("listaFuentes", coleccionService.listarFuentes());
        // Datos dummy para dashboard (o conectar a servicio real)
        model.addAttribute("numeroSolicitudes", 3);

        return "admin/inicio";
    }

    // Procesa el formulario de fuentes
    @PostMapping("/fuentes/asociar")
    public String asociarFuente(@RequestParam Long coleccionId,
                                @RequestParam(required = false) Long fuenteId,
                                RedirectAttributes redirectAttrs) {
        try {
            coleccionService.asociarFuente(coleccionId, fuenteId);
            redirectAttrs.addFlashAttribute("mensajeExito", "Fuente de colección actualizada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al actualizar fuente: " + e.getMessage());
        }
        return "redirect:/admin/inicio";
    }

    // Procesa el formulario del modal para asociar un algoritmo
    @PostMapping("/consenso/asociar")
    public String asociarAlgoritmo(@RequestParam Long coleccionId,
                                   @RequestParam(required = false) Long algoritmoId,
                                   RedirectAttributes redirectAttrs) {
        try {
            coleccionService.asociarAlgoritmo(coleccionId, algoritmoId);
            redirectAttrs.addFlashAttribute("mensajeExito", "Algoritmo actualizado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al actualizar algoritmo: " + e.getMessage());
        }
        return "redirect:/admin/inicio";
    }

    @GetMapping("/colecciones")
    public String administrarColecciones(Model model, HttpSession session) {
        validarAdmin(session);
        model.addAttribute("colecciones", coleccionService.listarColecciones());
        return "admin/colecciones";
    }

    @PostMapping("/importar-csv-upload")
    public ResponseEntity<?> subirCsv(@RequestParam("file") MultipartFile file) {

        int importados = hechosService.importarCsv(file); //debe devolver cantidad

        return ResponseEntity.ok(Map.of(
                "success", true,
                "importados", importados
        ));
    }

    private void validarAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("rol");
        if (!"ADMIN".equals(rol)) {
            throw new RuntimeException("403");
        }
    }
}