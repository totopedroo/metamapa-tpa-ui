package ar.utn.ba.ddsi.metamapa.dto;

import lombok.Data;

@Data
public class NuevaSolicitudForm {
    private String tipo;
    private Long idHecho;
    private String campo;
    private String valorNuevo;
    private String justificacion;
}