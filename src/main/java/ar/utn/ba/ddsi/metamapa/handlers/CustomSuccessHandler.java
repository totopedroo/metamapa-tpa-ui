package ar.utn.ba.ddsi.metamapa.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        HttpSession session = request.getSession();
        String rol = (String) session.getAttribute("rol");

        if (rol == null) {
            response.sendRedirect("/login?error");
            return;
        }

        switch (rol.toUpperCase()) {
            case "ADMIN":
                response.sendRedirect("/admin/inicio");
                break;
            case "CONTRIBUYENTE":
                response.sendRedirect("/contribuyente/inicio");
                break;
            default:
                response.sendRedirect("/hechos");
        }
    }
}
