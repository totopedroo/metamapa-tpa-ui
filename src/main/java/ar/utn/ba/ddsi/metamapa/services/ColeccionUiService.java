package ar.utn.ba.ddsi.metamapa.services;

import ar.utn.ba.ddsi.metamapa.dto.AlgoritmoDTO;
import ar.utn.ba.ddsi.metamapa.dto.ColeccionDTO;
import ar.utn.ba.ddsi.metamapa.dto.ColeccionFormDTO;
import ar.utn.ba.ddsi.metamapa.dto.FuenteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColeccionUiService {

    private final RestTemplate restTemplate;

    @Value("${api.base.url:http://localhost:8080}")
    private String apiBaseUrl;

    private String url(String path) {
        return apiBaseUrl + path;
    }

    // --- MÉTODOS PARA ALGORITMOS DE CONSENSO (MODAL) ---

    /**
     * Obtiene la lista de algoritmos disponibles desde el backend.
     * GET /api/colecciones/algoritmos
     */
    public List<AlgoritmoDTO> listarAlgoritmos() {
        try {
            AlgoritmoDTO[] resp = restTemplate.getForObject(url("/api/colecciones/algoritmos"), AlgoritmoDTO[].class);
            return resp != null ? Arrays.asList(resp) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error listando algoritmos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Asocia un algoritmo a una colección.
     * PUT /api/colecciones/{id}/asociar-algoritmo?algoritmoId={id}
     */
    public void asociarAlgoritmo(Long coleccionId, Long algoritmoId) {
        String queryParam = (algoritmoId != null) ? "?algoritmoId=" + algoritmoId : "";
        String uri = url("/api/colecciones/" + coleccionId + "/asociar-algoritmo" + queryParam);

        try {
            restTemplate.put(uri, null);
        } catch (Exception e) {
            System.err.println("Error asociando algoritmo: " + e.getMessage());
            throw new RuntimeException("Error al asociar algoritmo");
        }
    }

    public Long buscarAlgoritmoIdPorNombre(String nombre) {
        String url = url("/api/colecciones/algoritmos/buscar?nombre=" + nombre);
        return restTemplate.getForObject(url, Long.class);
    }


    // --- Últimas ---
    public List<ColeccionDTO> obtenerUltimasColecciones() {
        try {
            var arr = restTemplate.getForObject(url("/api/colecciones/ultimas"), ColeccionDTO[].class);
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // --- Listar ---
    public List<ColeccionDTO> listarColecciones() {
        try {
            var arr = restTemplate.getForObject(url("/api/colecciones"), ColeccionDTO[].class);
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // --- Obtener una ---
    public ColeccionDTO getColeccionPorId(Long id) {
        try {
            return restTemplate.getForObject(url("/api/colecciones/" + id), ColeccionDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    // --- Crear ---
    public ColeccionDTO crearColeccion(ColeccionFormDTO form) {
        try {
            return restTemplate.postForObject(url("/api/colecciones"), form, ColeccionDTO.class);
        } catch (Exception e) {
            System.err.println(">>> ERROR EN crearColeccion:");
            e.printStackTrace();
            throw e;
        }
    }

    // --- Editar ---
    public ColeccionDTO editarColeccion(Long id, ColeccionFormDTO form) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ColeccionFormDTO> req = new HttpEntity<>(form, headers);

            var resp = restTemplate.exchange(
                    url("/api/colecciones/editar/" + id),
                    HttpMethod.PATCH,
                    req,
                    ColeccionDTO.class
            );

            return resp.getBody();
        } catch (Exception e) {
            return null;
        }
    }


    //Importar desde csv
    // En HechosApiClient (Frontend)

    public void importarHechosCsv(MultipartFile file) {
        try {
            // 1. Auth Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 2. Preparar el Recurso CON NOMBRE (El truco mágico)
            // Leemos los bytes del archivo y sobreescribimos getFilename()
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    // Forzamos a que envíe el nombre original.
                    // Sin esto, a veces llega como null y el backend lo ignora.
                    return file.getOriginalFilename();
                }
            };

            // 3. Cuerpo
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource); // <--- Usamos nuestro recurso especial

            // 4. Entidad
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            System.out.println(">>> [FRONT] Enviando archivo: " + file.getOriginalFilename());

            // 5. Ejecutar
            ResponseEntity<String> response = restTemplate.exchange(
                    url("/api/colecciones/importar"),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println(">>> [FRONT] Respuesta del Backend: " + response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Fallo al enviar CSV: " + e.getMessage());
        }
    }


    // --- Eliminar ---
    public void eliminarColeccion(Long id) {
        try {
            restTemplate.exchange(
                    url("/api/colecciones/eliminar/" + id),
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );
        } catch (Exception ignored) {}
    }

    public List<FuenteDTO> listarFuentes() {
        String endpoint = url("/api/colecciones/fuentes");
        try {

            FuenteDTO[] resp = restTemplate.getForObject(endpoint, FuenteDTO[].class);

            if (resp != null) {
                return Arrays.asList(resp);
            } else {
                return Collections.emptyList();
            }

        } catch (Exception e) {
            e.printStackTrace(); // Ver el error completo
            return Collections.emptyList();
        }
    }

    public Long buscarFuenteIdPorTipo(String tipo) {
        String url = url("/api/colecciones/fuentes/buscar?tipo=" + tipo);
        return restTemplate.getForObject(url, Long.class);
    }

    public void asociarFuente(Long coleccionId, Long fuenteId) {
        String queryParam = (fuenteId != null) ? "?fuenteId=" + fuenteId : "";
        String uri = url("/api/colecciones/" + coleccionId + "/asociar-fuente" + queryParam);

        try {
            restTemplate.put(uri, null);
        } catch (Exception e) {
            System.err.println("Error asociando fuente: " + e.getMessage());
            throw new RuntimeException("Error al asociar fuente");
        }
    }

}