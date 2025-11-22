package ar.utn.ba.ddsi.metamapa.dto; // Paquete de tu UI

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// SOLUCIÓN 1: Ignora campos desconocidos que envía el backend (como latitud, longitud, etc.)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HechoDTO {

    private Long id;
    private String titulo;
    private String descripcion;

    // SOLUCIÓN 2: Añadimos el campo 'etiquetas' que la tabla SÍ necesita
    // (Asegúrate que el JSON del backend envíe 'etiquetas' como una lista de strings)
    private List<String> etiquetas; // Para la columna "Tags"

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) { this.etiquetas = etiquetas; }
}