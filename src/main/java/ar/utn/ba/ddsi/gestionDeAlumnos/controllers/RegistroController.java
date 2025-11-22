package ar.utn.ba.ddsi.gestionDeAlumnos.controllers;

// 👇 CORRECCIÓN: Cambiamos .api por .services
import ar.utn.ba.ddsi.gestionDeAlumnos.services.GestionAlumnosApiService;
import ar.utn.ba.ddsi.gestionDeAlumnos.dto.UsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegistroController {

  private final GestionAlumnosApiService apiService;

  @GetMapping("/registro")
  public String mostrarFormularioDeRegistro(Model model) {
    model.addAttribute("usuarioDTO", new UsuarioDTO());
    return "login/registro";
  }

  @PostMapping("/registro")
  public String registrarUsuario(@ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO, Model model) {
    try {
      System.out.println("Registrando a: " + usuarioDTO.getEmail());

      apiService.registrarUsuario(usuarioDTO);

      return "redirect:/login?registroExitoso=true";

    } catch (Exception e) {
      e.printStackTrace();
      model.addAttribute("error", "Error al registrar: " + e.getMessage());
      model.addAttribute("usuarioDTO", usuarioDTO);
      return "login/registro";
    }
  }
}