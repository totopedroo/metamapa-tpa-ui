package ar.utn.ba.ddsi.metamapa.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HechoDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String categoria;
    @JsonAlias("provincia")
    private String lugar;
    @JsonAlias("contenidoMultimedia")
    private String urlMultimedia;
    private List<String> consensos;
    private Boolean consensuado;
    private Double latitud;
    private Double longitud;
    private LocalDate fechaCarga;
    private LocalDate fecha; // fechaAcontecimiento
    private List<String> etiquetas;
    private Long idContribuyente;
    private List<String> fuentes;
}