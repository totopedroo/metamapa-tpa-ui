package ar.utn.ba.ddsi.metamapa.controllers;
import ar.utn.ba.ddsi.metamapa.dto.SolicitudDTO;
import ar.utn.ba.ddsi.metamapa.services.SolicitudesService;
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
    public String verSolicitudes(Model model) {
        // Usamos el servicio para buscar los datos
        List<SolicitudDTO> lista = solicitudesService.obtenerSolicitudes();

        // Los mandamos a la vista
        model.addAttribute("listaSolicitudes", lista);

        return "admin/solicitudesAdmin"; // Tu HTML
    }
    @PostMapping("/admin/solicitud/aprobar")
    public String aprobarSolicitud(@RequestParam("id") Long id) {
        // 1. Llamamos al servicio
        solicitudesService.actualizarEstadoSolicitud(id, "aprobar");

        // 2. REDIRECCIONAMOS (Patrón Post-Redirect-Get)
        // Esto es clave: recarga la página limpia para ver el cambio de estado
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/admin/solicitud/rechazar")
    public String rechazarSolicitud(@RequestParam("id") Long id) {
        // 1. Llamamos al servicio
        solicitudesService.actualizarEstadoSolicitud(id, "rechazar");

        // 2. Redireccionamos
        return "redirect:/admin/solicitudes";
    }
}
