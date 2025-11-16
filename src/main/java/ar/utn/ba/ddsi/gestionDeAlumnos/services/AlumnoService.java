/*package ar.utn.ba.ddsi.gestionDeAlumnos.services;

import ar.utn.ba.ddsi.gestionDeAlumnos.exceptions.DuplicateLegajoException;
import ar.utn.ba.ddsi.gestionDeAlumnos.exceptions.NotFoundException;
import ar.utn.ba.ddsi.gestionDeAlumnos.exceptions.ValidationException;
import ar.utn.ba.ddsi.gestionDeAlumnos.dto.AlumnoDTO;
import ar.utn.ba.ddsi.gestionDeAlumnos.dto.ContactoDTO;
import ar.utn.ba.ddsi.gestionDeAlumnos.dto.TipoContacto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlumnoService {
    @Autowired
    private GestionAlumnosApiService gestionAlumnosApiService;

    public List<AlumnoDTO> obtenerTodosLosAlumnos() {
        return gestionAlumnosApiService.obtenerTodosLosAlumnos();
    }

    public Optional<AlumnoDTO> obtenerAlumnoPorLegajo(String legajo) {
        try {
            AlumnoDTO alumno = gestionAlumnosApiService.obtenerAlumnoPorLegajo(legajo);
            return Optional.of(alumno);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public AlumnoDTO crearAlumno(AlumnoDTO alumnoDTO) {
        validarDatosBasicos(alumnoDTO);
        validarContactos(alumnoDTO);
        validarDuplicidadDeAlumno(alumnoDTO);
        
        return gestionAlumnosApiService.crearAlumno(alumnoDTO);
    }

    public AlumnoDTO actualizarAlumno(String legajo, AlumnoDTO alumnoDTO) {
        // Verificar que el alumno existe
        gestionAlumnosApiService.obtenerAlumnoPorLegajo(legajo);
        
        validarDatosBasicos(alumnoDTO);
        validarContactos(alumnoDTO);
        
        // Si el legajo cambió, verificar que no exista otro con el nuevo legajo
        if (!legajo.equals(alumnoDTO.getLegajo().trim())) {
            validarDuplicidadDeAlumno(alumnoDTO);
        }
        
        return gestionAlumnosApiService.actualizarAlumno(legajo, alumnoDTO);
    }

    public void eliminarAlumno(String legajo) {
        gestionAlumnosApiService.obtenerAlumnoPorLegajo(legajo); // Verificar que existe
        gestionAlumnosApiService.eliminarAlumno(legajo);
    }

    private void validarDatosBasicos(AlumnoDTO alumnoDTO) {
        ValidationException validationException = new ValidationException("Errores de validación");
        boolean tieneErrores = false;

        if (alumnoDTO.getLegajo() == null || alumnoDTO.getLegajo().trim().isEmpty()) {
            validationException.addFieldError("legajo", "El legajo es obligatorio");
            tieneErrores = true;
        }

        if (alumnoDTO.getNombre() == null || alumnoDTO.getNombre().trim().isEmpty()) {
            validationException.addFieldError("nombre", "El nombre es obligatorio");
            tieneErrores = true;
        }

        if (alumnoDTO.getApellido() == null || alumnoDTO.getApellido().trim().isEmpty()) {
            validationException.addFieldError("apellido", "El apellido es obligatorio");
            tieneErrores = true;
        }

        if (tieneErrores) {
            throw validationException;
        }
    }

    private void validarContactos(AlumnoDTO alumnoDTO) {
        if(alumnoDTO.getContactos().size() == 0) {
            return;
        }

        ValidationException validationException = new ValidationException("Errores de validación");
        boolean tieneErrores = false;

        for(int i = 0; i < alumnoDTO.getContactos().size(); i++) {
            ContactoDTO contacto = alumnoDTO.getContactos().get(i);

            // Si el contacto tiene algún campo completado, validar que esté completo
            boolean tieneTipo = contacto.getTipoContacto() != null;
            boolean tieneValor = contacto.getValor() != null && !contacto.getValor().trim().isEmpty();

            if (tieneTipo && !tieneValor) {
                validationException.addFieldError("contactos[" + i + "].valor", "El valor del contacto es obligatorio");
                tieneErrores = true;
            }

            if (tieneValor && !tieneTipo) {
                validationException.addFieldError("contactos[" + i + "].tipoContacto", "El tipo de contacto es obligatorio");
                tieneErrores = true;
            }

            // Validar formato de email
            if (tieneTipo && tieneValor && contacto.getTipoContacto().equals(TipoContacto.EMAIL)) {
                if (!contacto.getValor().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    validationException.addFieldError("contactos[" + i + "].valor", "El formato del email no es válido");
                    tieneErrores = true;
                }
            }
        }

        if (tieneErrores) {
            throw validationException;
        }
    }

    private void validarDuplicidadDeAlumno(AlumnoDTO alumnoDTO) {
        if (gestionAlumnosApiService.existeAlumno(alumnoDTO.getLegajo().trim())) {
            throw new DuplicateLegajoException(alumnoDTO.getLegajo().trim());
        }
    }
}*/
