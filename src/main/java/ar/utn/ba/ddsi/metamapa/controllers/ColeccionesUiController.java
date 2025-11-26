package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.ColeccionFormDTO;
import ar.utn.ba.ddsi.metamapa.services.ColeccionUiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/colecciones")
@RequiredArgsConstructor
public class ColeccionesUiController {

    private final ColeccionUiService coleccionService;

    // --- LISTAR ---
    @GetMapping
    public String listarColecciones(Model model) {
        model.addAttribute("colecciones", coleccionService.listarColecciones());
        return "colecciones/inicio";
    }

    // --- DETALLE ---
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("coleccion", coleccionService.getColeccionPorId(id));
        return "colecciones/detalle";
    }

    // --- FORM NUEVA COLECCIÓN ---
    @GetMapping("/nueva")
    public String mostrarFormNueva(Model model, HttpSession session) {

        validarAdmin(session);

        ColeccionFormDTO form = new ColeccionFormDTO();
        form.setAdministradorId((Long) session.getAttribute("usuarioId"));

        model.addAttribute("form", form);
        return "colecciones/nueva";
    }

    // --- CREAR ---
    @PostMapping("/crear")
    public String crear(@ModelAttribute("form") ColeccionFormDTO form,
                        RedirectAttributes redirect,
                        HttpSession session) {

        validarAdmin(session);

        form.setAdministradorId((Long) session.getAttribute("usuarioId"));
        coleccionService.crearColeccion(form);

        redirect.addFlashAttribute("ok", "Colección creada correctamente");
        return "redirect:/colecciones";
    }

    // --- FORM EDITAR ---
    @GetMapping("/editar/{id}")
    public String mostrarFormEditar(@PathVariable Long id,
                                    Model model,
                                    HttpSession session) {

        validarAdmin(session);

        ColeccionDTO dto = coleccionService.getColeccionPorId(id);

        ColeccionFormDTO form = new ColeccionFormDTO();
        form.setId(dto.getId());
        form.setTitulo(dto.getTitulo());
        form.setDescripcion(dto.getDescripcion());
        form.setAdministradorId((Long) session.getAttribute("usuarioId"));

        form.setHechosIds(dto.getHechos().stream().map(h -> h.getIdHecho()).toList());
        form.setCriteriosIds(dto.getCriterios().stream().map(c -> c.getId()).toList());

        model.addAttribute("form", form);
        return "colecciones/editar";
    }

    // --- EDITAR ---
    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id,
                         @ModelAttribute("form") ColeccionFormDTO form,
                         RedirectAttributes redirect,
                         HttpSession session) {

        validarAdmin(session);

        form.setAdministradorId((Long) session.getAttribute("usuarioId"));
        coleccionService.editarColeccion(id, form);

        redirect.addFlashAttribute("ok", "Colección actualizada");
        return "redirect:/colecciones";
    }

    // --- ELIMINAR ---
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           RedirectAttributes redirect,
                           HttpSession session) {

        validarAdmin(session);

        coleccionService.eliminarColeccion(id);

        redirect.addFlashAttribute("ok", "Colección eliminada");
        return "redirect:/colecciones";
    }


    // --- Utilidad ---
    private void validarAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("rol");
        if (rol == null || !rol.equals("ADMIN")) {
            throw new RuntimeException("403 - Acceso denegado");
        }
    }
}