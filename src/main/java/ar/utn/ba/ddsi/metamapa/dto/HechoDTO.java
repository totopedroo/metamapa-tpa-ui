package ar.utn.ba.ddsi.metamapa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Importar
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
@JsonIgnoreProperties(ignoreUnknown = true) // 👈 ESTO EVITA ERRORES SI EL JSON TIENE CAMPOS EXTRA
public class HechoDTO {
    private Long idHecho;
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDate fechaAcontecimiento;
    private List<String> etiquetas;
}