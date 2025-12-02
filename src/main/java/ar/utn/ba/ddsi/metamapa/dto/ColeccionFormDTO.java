package ar.utn.ba.ddsi.metamapa.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ColeccionFormDTO {

    private Long id;

    private String titulo;
    private String descripcion;

    private Long administradorId;

    private List<Long> hechosIds = new ArrayList<>();
    private List<Long> criteriosIds = new ArrayList<>();

    private Long algoritmoId;
    private Long fuenteId;

}