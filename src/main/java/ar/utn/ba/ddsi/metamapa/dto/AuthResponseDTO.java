package ar.utn.ba.ddsi.metamapa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
    private String tokenType;
    private Integer expiresIn;

    // Agregar bloque usuario
    private UsuarioInfo usuario;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        private Long id;
        private String username;
        private String rol;          // “ADMIN”, “CONTRIBUYENTE”, “VISUALIZADOR”
        private java.util.List<String> permisos;
    }
}