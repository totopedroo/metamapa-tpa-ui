package ar.utn.ba.ddsi.gestionDeAlumnos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColeccionDTO {
    private Long id;
    private String titulo;
    private String descripcion;
}