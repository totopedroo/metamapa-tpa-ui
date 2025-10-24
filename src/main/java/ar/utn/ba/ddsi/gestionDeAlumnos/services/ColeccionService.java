package ar.utn.ba.ddsi.gestionDeAlumnos.services;

import ar.utn.ba.ddsi.gestionDeAlumnos.dto.ColeccionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColeccionService {
    @Autowired
    private GestionAlumnosApiService gestionAlumnosApiService;

    public List<ColeccionDTO> obtenerTodasLasColecciones() {
        return gestionAlumnosApiService.obtenerTodasLasColecciones();
    }


}
