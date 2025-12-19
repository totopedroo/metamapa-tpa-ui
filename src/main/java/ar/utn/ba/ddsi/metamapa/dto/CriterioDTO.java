package ar.utn.ba.ddsi.metamapa.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CriterioDTO {
    @JsonAlias({"id_criterio", "idCriterio"})
    private Long id;

    private String columna;
    private String tipo;   // "texto" | "fecha"
    private String valor;

    private String desde;
    private String hasta;
}