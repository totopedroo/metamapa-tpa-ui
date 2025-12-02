package ar.utn.ba.ddsi.metamapa.dto;

import lombok.Data;
import lombok.Getter;

@Getter
public enum CampoHecho {
    TITULO("titulo", "Título"),
    DESCRIPCION("descripcion", "Descripción"),
    CATEGORIA("categoria", "Categoría"),
    CONTENIDO_MULTIMEDIA("contenidoMultimedia", "Contenido Multimedia"),
    LATITUD("latitud", "Latitud"),
    LONGITUD("longitud", "Longitud"),
    FECHA_ACONTECIMIENTO("fechaAcontecimiento", "Fecha del Hecho"),
    FECHA_CARGA("fechaCarga", "Fecha de Carga"),
    PROVINCIA("provincia", "Provincia"),
    HORA_ACONTECIMIENTO("horaAcontecimiento", "Hora del Hecho");

    private final String nombreCampo; // El nombre de la variable en Java
    private final String etiqueta;    // Nombre legible para CSV o UI

    CampoHecho(String nombreCampo, String etiqueta) {
        this.nombreCampo = nombreCampo;
        this.etiqueta = etiqueta;
    }
}
