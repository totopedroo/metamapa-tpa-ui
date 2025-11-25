package ar.utn.ba.ddsi.metamapa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 👇 ESTA LÍNEA ES LA MAGIA: Evita que falle si el backend manda campos extra
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColeccionDTO {
    private Long id;
    private String titulo;
    private String descripcion;

    // No necesitamos mapear 'administradorId' ni listas de IDs aquí para la landing
}