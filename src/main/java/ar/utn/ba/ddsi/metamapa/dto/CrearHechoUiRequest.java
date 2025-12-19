package ar.utn.ba.ddsi.metamapa.dto;

import java.time.LocalDate;
import java.util.List;

public record CrearHechoUiRequest(
        String titulo,
        String descripcion,
        String categoria,
        String provincia,
        Double latitud,
        Double longitud,
        LocalDate fechaAcontecimiento,
        String contenidoMultimedia,
        List<String> etiquetas
) {}
