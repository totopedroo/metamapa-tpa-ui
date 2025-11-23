package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.AuthResponseDTO; // Importar esto
import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.dto.UsuarioDTO;
import ar.utn.ba.ddsi.metamapa.services.internal.WebApiCallerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestionAlumnosApiService {

  private final WebApiCallerService webApiCallerService;

  // URL base de tu backend (ej: http://localhost:8080)
  @Value("${api.base.url:http://localhost:8080}")
  private String backendBaseUrl;

  /**
   * Realiza el login contra el Backend.
   * Método agregado para que funcione el CustomAuthProvider.
   */
  public AuthResponseDTO login(String username, String password) {
    String url = backendBaseUrl + "/api/auth/login";

    // El backend espera: { "username": "...", "password": "..." }
    var body = Map.of(
        "username", username,
        "password", password
    );

    // Llamada POST pública que devuelve el token
    return webApiCallerService.postPublic(url, body, AuthResponseDTO.class);
  }

  /**
   * Registra un nuevo usuario en el backend.
   */
  public void registrarUsuario(UsuarioDTO usuarioDTO) {
    // 1. Preparamos el cuerpo del request tal como lo espera el Backend
    // Backend espera: nombre, apellido, mail, password
    var body = Map.of(
        "nombre", usuarioDTO.getNombre(),
        "apellido", usuarioDTO.getApellido(),
        "mail", usuarioDTO.getEmail(),      // UI usa 'email', Backend usa 'mail'
        "password", usuarioDTO.getContrasena() // UI usa 'contrasena', Backend usa 'password'
    );

    // 2. Definimos la URL (según tu UsuariosController del backend)
    String url = backendBaseUrl + "/usuarios/register";

    // 3. Hacemos el POST público
    webApiCallerService.postPublic(url, body, Object.class);
  }

  /**
   * Obtiene todas las colecciones (Público)
   */
  public List<ColeccionDTO> obtenerTodasLasColecciones() {
    String url = backendBaseUrl + "/api/colecciones";
    return webApiCallerService.getPublicList(url, ColeccionDTO.class);
  }

  /**
   * Obtiene hechos destacados (Público)
   * Maneja la respuesta { "items": [...] } del backend
   */
  public List<HechoDTO> getPublicHechos(String modo, int limit) {
    String url = backendBaseUrl + "/api/hechos?modo=" + modo + "&limit=" + limit;

    Map responseMap = webApiCallerService.getPublicMap(url);

    if (responseMap == null || !responseMap.containsKey("items")) {
      return List.of();
    }

    List<?> rawList = (List<?>) responseMap.get("items");

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    return rawList.stream()
        .map(item -> mapper.convertValue(item, HechoDTO.class))
        .collect(Collectors.toList());
  }
}