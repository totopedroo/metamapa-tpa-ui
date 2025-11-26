package ar.utn.ba.ddsi.metamapa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColeccionDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private List<HechoDTO> hechos;
    private List<CriterioDTO> criterios;
    private String algoritmoDeConsenso;

    public int getCantidadHechos() {
        return hechos == null ? 0 : hechos.size();
    }
    // No necesitamos mapear 'administradorId' ni listas de IDs aquí para la landing
}