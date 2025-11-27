package ar.utn.ba.ddsi.metamapa.providers;

import ar.utn.ba.ddsi.metamapa.services.GestionAlumnosApiService;
import ar.utn.ba.ddsi.metamapa.dto.AuthResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomAuthProvider implements AuthenticationProvider {

  private final GestionAlumnosApiService apiService;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    try {
      // 1. Login contra el backend
      AuthResponseDTO response = apiService.login(username, password);

      if (response == null || response.getUsuario() == null) {
        throw new BadCredentialsException("Respuesta inválida del servidor");
      }

      // Usuario del backend
      var userData = response.getUsuario(); // ← contiene id, username, rol, permisos

      // 2. Guardar en sesión local
      HttpSession session = ((ServletRequestAttributes)
              RequestContextHolder.currentRequestAttributes())
              .getRequest()
              .getSession();

      session.setAttribute("jwt", response.getAccessToken());
      session.setAttribute("usuarioId", userData.getId());
      session.setAttribute("usuario", userData.getUsername());
      session.setAttribute("rol", userData.getRol());
      session.setAttribute("permisos", userData.getPermisos());

      // 3. Authorities para Spring Security
      List<SimpleGrantedAuthority> authorities =
              List.of(new SimpleGrantedAuthority("ROLE_" + userData.getRol().toUpperCase()));

      return new UsernamePasswordAuthenticationToken(username, password, authorities);

    } catch (Exception e) {
    e.printStackTrace();

    throw new BadCredentialsException(
            "Error al autenticar con backend: " + e.getMessage()
    );
  }
}

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}