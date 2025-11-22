package ar.utn.ba.ddsi.metamapa.providers;

import ar.utn.ba.ddsi.metamapa.services.GestionAlumnosApiService;
import ar.utn.ba.ddsi.metamapa.dto.AuthResponseDTO;
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

    System.out.println(">>> Intentando loguear usuario: " + username);

    try {
      // 1. Llamamos al Backend real
      AuthResponseDTO response = apiService.login(username, password);
      System.out.println(">>> Login exitoso en Backend. Token recibido.");

      // 2. Guardamos el token en sesión
      HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
      session.setAttribute("accessToken", response.getAccessToken());

      // 3. Asignamos roles (Hardcodeado por ahora)
      List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CONTRIBUYENTE"));

      return new UsernamePasswordAuthenticationToken(username, password, authorities);

    } catch (Exception e) {
      System.err.println(">>> ERROR EN LOGIN: " + e.getMessage());
      e.printStackTrace();
      throw new BadCredentialsException("Error de autenticación: " + e.getMessage());
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}