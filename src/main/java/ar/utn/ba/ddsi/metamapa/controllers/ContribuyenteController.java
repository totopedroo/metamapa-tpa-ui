package ar.utn.ba.ddsi.metamapa.controllers;

import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.services.HechosUiService;
import ar.utn.ba.ddsi.metamapa.services.SolicitudesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contribuyente")
@RequiredArgsConstructor
public class ContribuyenteController {

    private final SolicitudesService solicitudService;
    private final HechosUiService hechosService;

    @GetMapping("/inicio")
    public String inicio() {
        return "contribuyente/inicio";
    }

    @GetMapping("/solicitudes")
    public String verSolicitudesPropias(Model model, HttpSession session) {
        Long id = (Long) session.getAttribute("usuarioId");
        model.addAttribute("solicitudes", solicitudService.listarPorUsuario(id, session));
        return "contribuyente/solicitudes";
    }

    @GetMapping("/solicitar-eliminacion/{idHecho}")
    public String solicitarEliminacion(@PathVariable Long idHecho, HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("usuarioId");
        solicitudService.crearSolicitudEliminacion(idHecho, idUsuario, session);
        return "redirect:/contribuyente/solicitudes";
    }

    @GetMapping("/nueva-solicitud")
    public String nuevaSolicitud(Model model,
                                 HttpSession session,
                                 @RequestParam(required = false) String categoria,
                                 @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteDesde,
                                 @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReporteHasta,
                                 @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoDesde,
                                 @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAcontecimientoHasta,
                                 @RequestParam(required = false) Double latitud,
                                 @RequestParam(required = false) Double longitud) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        List<HechoDTO> hechos = hechosService.filtrarHechos(
                categoria,
                fechaReporteDesde,
                fechaReporteHasta,
                fechaAcontecimientoDesde,
                fechaAcontecimientoHasta,
                latitud,
                longitud
        );

        model.addAttribute("hechosParaEliminar", hechos);

        model.addAttribute("misHechos", hechosService.listarHechosDelUsuario(usuarioId));

        return "contribuyente/nueva-solicitud";
    }

    @GetMapping("/subir-hecho")
    public String subirHechoForm() {
        return "contribuyente/subir-hecho";
    }

    @GetMapping("/mis-hechos")
    public String misHechos(Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        model.addAttribute("hechos", hechosService.listarHechosDelUsuario(usuarioId));

        return "contribuyente/mis-hechos";
    }
}

