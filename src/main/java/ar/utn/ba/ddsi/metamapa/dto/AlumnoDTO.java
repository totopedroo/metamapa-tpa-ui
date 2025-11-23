package ar.utn.ba.ddsi.metamapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoDTO {
    private String legajo;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private List<ContactoDTO> contactos = new ArrayList<>();
}
