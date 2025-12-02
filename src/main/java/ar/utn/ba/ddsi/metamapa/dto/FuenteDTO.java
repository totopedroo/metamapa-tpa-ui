package ar.utn.ba.ddsi.metamapa.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos extra del backend para evitar errores
public class FuenteDTO {

  private Long id;

  // El backend envía este campo como "tipoFuente" (o "tipo_fuente").
  // Usamos @JsonAlias para que funcione sin importar cómo lo mande el backend (snake_case o camelCase)
  @JsonProperty("tipoFuente")
  @JsonAlias({"tipo_fuente", "tipo"})
  private String nombre;
}