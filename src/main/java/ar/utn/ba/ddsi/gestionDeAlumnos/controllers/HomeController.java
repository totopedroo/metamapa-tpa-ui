/*package ar.utn.ba.ddsi.gestionDeAlumnos.controllers;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/landing.html";
    }

    @GetMapping("/inicio")
    public String paginaDeInicioSegunRol(Authentication authentication) {

        // 1. Obtenemos la lista de roles/permisos del usuario que se logueó.
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 2. Buscamos si en esa lista está el rol de ADMIN.
        boolean esAdmin = authorities.stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));

        // 3. Tomamos la decisión.
        if (esAdmin) {
            // Si es Admin, le devolvemos la vista del panel de admin.
            // Thymeleaf buscará el archivo /templates/admin/panel.html
            return "admin/inicioAdmin";
        } else {
            // Para cualquier otro rol, devolvemos la vista de inicio normal.
            // Thymeleaf buscará el archivo /templates/docente/inicio.html
            return "inicio";
        }
    }

    @GetMapping("/404")
    public String notFound(Model model) {
        model.addAttribute("titulo", "No encontrado");
        return "404";
    }

    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("titulo", "Acceso denegado");
        model.addAttribute("mensaje", "No tiene permiso para acceder a este recurso.");
        return "403";
    }
}
*/