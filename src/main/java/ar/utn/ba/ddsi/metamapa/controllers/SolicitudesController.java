package ar.utn.ba.ddsi.metamapa.controllers;
import ar.utn.ba.ddsi.metamapa.dto.SolicitudDTO;
import ar.utn.ba.ddsi.metamapa.dto.SolicitudModificacionInputDto;
import ar.utn.ba.ddsi.metamapa.services.SolicitudesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@Controller
public class SolicitudesController {

    @Autowired
    private SolicitudesService solicitudesService;

    @GetMapping("/admin/solicitudes")
    public String verSolicitudes(Model model, HttpSession session) {
        List<SolicitudDTO> eliminaciones = solicitudesService.obtenerSolicitudesEliminacion(session);
        model.addAttribute("listaEliminacion", eliminaciones);

        // 2. Cargar Modificaciones
        List<SolicitudModificacionInputDto> modificaciones = solicitudesService.obtenerSolicitudesModificacion(session);
        model.addAttribute("listaModificacion", modificaciones);

        return "admin/solicitudes";
    }


    @PostMapping("/admin/solicitud/aprobar")
    public String aprobarSolicitud(@RequestParam("id") Long id, HttpSession session) {
        // 1. Llamamos al servicio
        solicitudesService.actualizarEstadoEliminacion(id, "aprobar", session);

        // 2. REDIRECCIONAMOS (Patrón Post-Redirect-Get)
        // Esto es clave: recarga la página limpia para ver el cambio de estado
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/admin/solicitud/rechazar")
    public String rechazarSolicitud(@RequestParam("id") Long id, HttpSession session) {
        // 1. Llamamos al servicio
        solicitudesService.actualizarEstadoEliminacion(id, "rechazar", session);

        // 2. Redireccionamos
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/admin/modificacion/aprobar")
    public String aprobarModif(@RequestParam Long id, HttpSession session) {
        solicitudesService.gestionarModificacion(id, "aprobar", session);
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/admin/modificacion/rechazar")
    public String rechazarModif(@RequestParam Long id, HttpSession session) {
        solicitudesService.gestionarModificacion(id, "rechazar", session);
        return "redirect:/admin/solicitudes";
    }

}
