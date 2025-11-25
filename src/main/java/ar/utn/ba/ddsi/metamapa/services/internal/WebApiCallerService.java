package ar.utn.ba.ddsi.metamapa.services.internal;

import ar.utn.ba.ddsi.metamapa.dto.AuthResponseDTO; // Asegúrate de tener este DTO
import ar.utn.ba.ddsi.metamapa.dto.RefreshTokenDTO; // Asegúrate de tener este DTO
import ar.utn.ba.ddsi.metamapa.exceptions.NotFoundException; // Asegúrate de tener esta Exception
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

  public <T> java.util.List<T> getListWithToken(String url, String token, Class<T> responseType) {
    try {
      return webClient
              .get()
              .uri(url)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // <--- AQUÍ ESTÁ LA CLAVE
              .retrieve()
              .bodyToFlux(responseType)
              .collectList()
              .block(); // Bloqueamos para esperar la respuesta (estilo sincrónico)

    } catch (WebClientResponseException e) {
      // Si el token venció (401) o no tiene permiso (403)
      System.err.println("Error de permisos: " + e.getStatusCode());
      throw e; // O podrías devolver lista vacía
    } catch (Exception e) {
      throw new RuntimeException("Error al consultar API protegida: " + e.getMessage(), e);
    }
  }

  /**
   * Ejecuta un POST enviando el Token Bearer.
   * Se usa para aprobar/rechazar solicitudes.
   */
  public void postWithToken(String url, String token) {
    try {
      webClient
              .post()
              .uri(url)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .retrieve()
              .toBodilessEntity() // No esperamos cuerpo de respuesta, solo que no de error
              .block();

    } catch (Exception e) {
      throw new RuntimeException("Error al ejecutar acción protegida: " + e.getMessage(), e);
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