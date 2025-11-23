package ar.utn.ba.ddsi.metamapa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String tokenType;   // "Bearer"
    private String accessToken; // El token largo
    private Integer expiresIn;
}