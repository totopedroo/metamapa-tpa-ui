package ar.utn.ba.ddsi.gestionDeAlumnos.controllers;

import ar.utn.ba.ddsi.gestionDeAlumnos.services.HechosUiService;
import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ui/hechos")
public class HechosUiController {
    private final HechosUiService service;

    @GetMapping
    public String pantalla(Model model, HttpServletRequest req) {
        model.addAttribute("listaHechos", service.listar(req));
        model.addAttribute("nuevoHecho", new HashMap<String,Object>());
        return "hechos"; // tu template
    }

    @PostMapping("/crear")
    public String crear(@RequestParam Map<String,String> form,
                        HttpServletRequest req, RedirectAttributes ra) {
        var body = new HashMap<String,Object>(form); // o mapear campos manualmente
        service.crear(req, body);
        ra.addFlashAttribute("ok","Hecho creado");
        return "redirect:/ui/hechos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, HttpServletRequest req, RedirectAttributes ra) {
        service.eliminar(req, id);
        ra.addFlashAttribute("ok","Hecho eliminado");
        return "redirect:/ui/hechos";
    }
}