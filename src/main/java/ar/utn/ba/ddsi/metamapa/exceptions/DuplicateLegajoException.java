package ar.utn.ba.ddsi.metamapa.exceptions;

public class DuplicateLegajoException extends RuntimeException {

    public DuplicateLegajoException(String legajo) {
        super("El legajo " + legajo + " ya existe");
    }
}
