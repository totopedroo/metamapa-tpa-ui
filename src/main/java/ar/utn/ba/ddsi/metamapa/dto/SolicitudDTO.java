package ar.utn.ba.ddsi.metamapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDTO {
    private Long id;
    private EstadoDeSolicitud estado;
    private String justificacion;
    private String tituloHecho;
}
