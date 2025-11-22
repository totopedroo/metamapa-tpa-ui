/*package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.HechoDTO;
import ar.utn.ba.ddsi.metamapa.dto.AuthResponseDTO;
import ar.utn.ba.ddsi.metamapa.dto.RolesPermisosDTO;
import ar.utn.ba.ddsi.metamapa.exceptions.NotFoundException;
import ar.utn.ba.ddsi.metamapa.services.internal.WebApiCallerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
@Service
public class GestionHechosApiService {
    private static final Logger log = LoggerFactory.getLogger(GestionHechosApiService.class);
    private final WebClient webClient;
    private final WebApiCallerService webApiCallerService;
    private final String authServiceUrl;
    private final String hechosServiceUrl;

    @Autowired
    public GestionHechosApiService(
            WebApiCallerService webApiCallerService,
            @Value("${auth.service.url}") String authServiceUrl,
            @Value("${hechos.service.url}") String hechosServiceUrl) {
        this.webClient = WebClient.builder().build();
        this.webApiCallerService = webApiCallerService;
        this.authServiceUrl = authServiceUrl;
        this.hechosServiceUrl = hechosServiceUrl;
    }

    public AuthResponseDTO login(String username, String password) {
        try {
            AuthResponseDTO response = webClient
                    .post()
                    .uri(authServiceUrl + "/auth")
                    .bodyValue(Map.of(
                            "username", username,
                            "password", password
                    ))
                    .retrieve()
                    .bodyToMono(AuthResponseDTO.class)
                    .block();
            return response;
        } catch (WebClientResponseException e) {
            log.error(e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Login fallido - credenciales incorrectas
                return null;
            }
            // Otros errores HTTP
            throw new RuntimeException("Error en el servicio de autenticación: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con el servicio de autenticación: " + e.getMessage(), e);
        }
    }

    public RolesPermisosDTO getRolesPermisos(String accessToken) {
        try {
            RolesPermisosDTO response = webApiCallerService.getWithAuth(
                    authServiceUrl + "/auth/user/roles-permisos",
                    accessToken,
                    RolesPermisosDTO.class
            );
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Error al obtener roles y permisos: " + e.getMessage(), e);
        }
    }

    public List<HechoDTO> obtenerTodosLosHechos() {
        List<HechoDTO> response = webApiCallerService.getList(hechosServiceUrl + "/Hechos", HechoDTO.class);
        return response != null ? response : List.of();
    }

    public HechoDTO obtenerHechoPorId(long id) {
        HechoDTO response = webApiCallerService.get(hechosServiceUrl + "/Hechos/" + id, HechoDTO.class);
        if (response == null) {
            throw new NotFoundException("Hecho", id);
        }
        return response;
    }

    public HechoDTO crearHecho(HechoDTO hechoDTO) {
        HechoDTO response = webApiCallerService.post(hechosServiceUrl + "/alumnos", hechoDTO, HechoDTO.class);
        if (response == null) {
            throw new RuntimeException("Error al crear alumno en el servicio externo");
        }
        return response;
    }

    public HechoDTO actualizarHecho(long id, HechoDTO hechoDTO) {
        HechoDTO response = webApiCallerService.put(hechosServiceUrl + "/alumnos/" + id, hechoDTO, HechoDTO.class);
        if (response == null) {
            throw new RuntimeException("Error al actualizar alumno en el servicio externo");
        }
        return response;
    }

    public void eliminarHecho(long id) {
        webApiCallerService.delete(hechosServiceUrl + "/alumnos/" + id);
    }

    public boolean existeHecho(long id) {
        try {
            obtenerHechoPorId(id);
            return true;
        } catch (NotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar existencia del alumno: " + e.getMessage(), e);
        }
    }
}
*/