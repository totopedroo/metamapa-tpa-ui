package ar.utn.ba.ddsi.gestionDeAlumnos.controllers; // (o donde estén tus controllers)

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

  @GetMapping("/legal/privacidad")
  public String mostrarPaginaPrivacidad() {
    return "legal/privacidad";
  }
}