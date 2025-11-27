package ar.utn.ba.ddsi.metamapa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Importar
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HechoDTO {
    private Long idHecho;
    private String titulo;
    private String descripcion;
    private String categoria;
    private String contenidoMultimedia;
    private LocalDate fechaAcontecimiento;
    private LocalTime horaAcontecimiento;
    private LocalDate fechaCarga;
    private Double latitud;
    private Double longitud;
    private String provincia;
    private List<String> etiquetas;
    private List<String> consensos;
    private Boolean consensuado;
    private List<String> fuentes;
}