package ar.utn.ba.ddsi.metamapa.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model,
                              @RequestParam(value = "code", required = false) Integer code) {

        int status;

        if (code != null) {
            status = code; // redirecciones de spring security
        } else {
            Object s = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            status = (s != null) ? Integer.parseInt(s.toString()) : 500;
        }

        switch (status) {

            case 401:
                model.addAttribute("titulo", "Debe iniciar sesión");
                model.addAttribute("mensaje", "Para continuar, ingrese a su cuenta.");
                return "error/401";

            case 403:
                model.addAttribute("titulo", "Acceso denegado");
                model.addAttribute("mensaje", "No posee permisos para acceder a este contenido.");
                return "error/403";

            case 404:
                model.addAttribute("titulo", "Página no encontrada");
                model.addAttribute("mensaje", "El recurso solicitado no existe o ha sido movido.");
                return "error/404";

            default:
                model.addAttribute("titulo", "Error interno");
                model.addAttribute("mensaje", "Ocurrió un error inesperado.");
                return "error/500";
        }
    }
}
