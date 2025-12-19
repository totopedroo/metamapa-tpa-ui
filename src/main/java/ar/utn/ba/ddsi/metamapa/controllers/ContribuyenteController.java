package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.*;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import ar.utn.ba.ddsi.metamapa.services.SolicitudesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contribuyente")
@RequiredArgsConstructor
public class ContribuyenteController {

    private final SolicitudesService solicitudService;
    private final HechosUiService hechosService;

    @GetMapping(value = {"", "/", "/inicio"})
    public String inicio() {
        return "contribuyente/inicio";
    }

    @GetMapping("/solicitudes")
    public String verSolicitudesPropias(Model model, HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("usuarioId");


        if (idUsuario == null) return "redirect:/login";

        try {

            List<SolicitudDTO> eliminaciones = solicitudService.listarPorUsuario( session);
            model.addAttribute("misEliminaciones", eliminaciones);


            List<SolicitudModificacionInputDto> modificaciones = solicitudService.obtenerSolicitudesModificacionUsuario(session);
            model.addAttribute("misModificaciones", modificaciones);

        } catch (Exception e) {
            model.addAttribute("error", "Error cargando solicitudes");
            model.addAttribute("misEliminaciones", List.of());
            model.addAttribute("misModificaciones", List.of());
        }

        return "contribuyente/solicitudes";
    }

    @PostMapping("/nueva-solicitud")
    public String procesarNuevaSolicitud(NuevaSolicitudForm form, RedirectAttributes redirectAttrs, HttpSession session) {
        try {
            if ("ELIMINACION".equals(form.getTipo())) {
                // Lógica existente de eliminación
                Long idUsuario = (Long) session.getAttribute("usuarioId");
                solicitudService.crearSolicitudEliminacion(form.getIdHecho(),form.getJustificacion(), session);

                redirectAttrs.addFlashAttribute("mensajeExito", "Solicitud de eliminación enviada.");

            } else if ("MODIFICACION".equals(form.getTipo())) {
                // Lógica NUEVA de modificación
                solicitudService.crearSolicitudModificacion(
                        form.getIdHecho(),
                        form.getCampo(),
                        form.getValorNuevo(),
                        form.getJustificacion(),
                        session
                );

                redirectAttrs.addFlashAttribute("mensajeExito", "Solicitud de modificación enviada.");
            }

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensajeError", "Error al procesar la solicitud: " + e.getMessage());
        }

        return "contribuyente/inicio";
    }

    @GetMapping("/nueva-solicitud")
    public String nuevaSolicitud(Model model,
                                 HttpSession session,
                                 @RequestParam(required = false) String categoria,
                                 @RequestParam(required = false) String modo,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteDesde,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteHasta,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoDesde,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoHasta,
                                 @RequestParam(required = false) Double latitud,
                                 @RequestParam(required = false) Double longitud,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        // nuevo: ahora el servicio devuelve Map
        Map<String,Object> resultado = hechosService.filtrarHechos(
                categoria,
                modo,
                fechaReporteDesde,
                fechaReporteHasta,
                fechaAcontecimientoDesde,
                fechaAcontecimientoHasta,
                latitud,
                longitud,
                page,
                size
        );

        // lista real de hechos
        List<HechoDTO> hechos = (List<HechoDTO>) resultado.get("items");

        // para la tabla en la vista
        model.addAttribute("hechosParaEliminar", hechos);

        // tus hechos
        model.addAttribute("misHechos", hechosService.listarHechosDelUsuario(usuarioId));

        model.addAttribute("camposDisponibles", CampoHecho.values());

        return "contribuyente/nueva-solicitud";
    }

    @GetMapping("/mis-hechos")
    public String misHechos(Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        model.addAttribute("hechos", hechosService.listarHechosDelUsuario(usuarioId));

        return "contribuyente/mis-hechos";
    }
}

