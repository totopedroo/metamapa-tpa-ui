package ar.utn.ba.ddsi.gestionDeAlumnos.dto;

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
public class ColeccionDTO {
    private Long id;
    private String titulo; // Usamos 'titulo' para que coincida con el JSON del Backend

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
}