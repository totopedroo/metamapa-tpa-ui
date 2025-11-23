package ar.utn.ba.ddsi.metamapa.services.internal;

import ar.utn.ba.ddsi.metamapa.dto.AuthResponseDTO; // Asegúrate de tener este DTO
import ar.utn.ba.ddsi.metamapa.dto.RefreshTokenDTO; // Asegúrate de tener este DTO
import ar.utn.ba.ddsi.metamapa.exceptions.NotFoundException; // Asegúrate de tener esta Exception
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
public class WebApiCallerService {

  private final WebClient webClient;
  private final String authServiceUrl;

  public WebApiCallerService(@Value("${auth.service.url:http://localhost:8080}") String authServiceUrl) {
    this.webClient = WebClient.builder().build();
    this.authServiceUrl = authServiceUrl;
  }

  /**
   * Ejecuta una llamada HTTP POST PÚBLICA (sin token).
   * Ideal para Registro y Login.
   */
  public <T> T postPublic(String url, Object body, Class<T> responseType) {
    try {
      return webClient
          .post()
          .uri(url)
          .bodyValue(body)
          .retrieve()
          .bodyToMono(responseType)
          .block();
    } catch (WebClientResponseException e) {
      throw new RuntimeException("Error del servidor (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      throw new RuntimeException("Error de conexión al registrar: " + e.getMessage(), e);
    }
  }

  /**
   * Ejecuta una llamada HTTP GET pública (sin token) que retorna un Map
   */
  public Map getPublicMap(String url) {
    try {
      return webClient
          .get()
          .uri(url)
          .retrieve()
          .bodyToMono(Map.class)
          .block();
    } catch (Exception e) {
      // Manejo silencioso o logueo, dependiendo de tu necesidad
      System.err.println("Error en getPublicMap: " + e.getMessage());
      return null;
    }
  }

  /**
   * Ejecuta una llamada HTTP GET pública (sin token) que retorna una Lista
   */
  public <T> java.util.List<T> getPublicList(String url, Class<T> responseType) {
    try {
      return webClient
          .get()
          .uri(url)
          .retrieve()
          .bodyToFlux(responseType)
          .collectList()
          .block();
    } catch (Exception e) {
      System.err.println("Error en getPublicList: " + e.getMessage());
      return java.util.List.of();
    }
  }

  // --- AQUÍ ABAJO IRÍAN LOS MÉTODOS PRIVADOS (executeWithTokenRetry) ---
  // Si no los estás usando para el registro, no son estrictamente necesarios ahora,
  // pero idealmente deberías mantener la lógica de sesión que tenías antes si vas a loguear usuarios.
}