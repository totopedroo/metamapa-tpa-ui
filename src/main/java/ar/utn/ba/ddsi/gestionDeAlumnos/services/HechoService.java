package ar.utn.ba.ddsi.gestionDeAlumnos.services;

import ar.utn.ba.ddsi.gestionDeAlumnos.dto.HechoDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HechoService {
    @Autowired
    private GestionAlumnosApiService gestionAlumnosApiService;

    public List<HechoDTO> obtenerHechosDestacados(String modo) {
        // El límite de 6 está harcodeado aquí,
        // ya que la landing siempre mostrará esa cantidad.
        return gestionAlumnosApiService.getPublicHechos(modo, 6);
    }


}
