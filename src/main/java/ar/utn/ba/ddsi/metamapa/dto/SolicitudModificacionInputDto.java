package ar.utn.ba.ddsi.metamapa.dto;

import lombok.Data;

@Data
public class SolicitudModificacionInputDto {
    private Long id;
    private Long idHecho;
    private Long idContribuyente;
    private CampoHecho campo;
    private EstadoDeSolicitud estado;
    private String valorNuevo; // El dato nuevo en formato string
    private String justificacion;
}
