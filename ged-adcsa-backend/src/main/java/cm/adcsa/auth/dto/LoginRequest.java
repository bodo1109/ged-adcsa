package cm.adcsa.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    @NotNull(message = "Le nom d'utilisateur ne peut pas être null")
    @NotBlank(message = "Le nom d'utilisateur ne peut pas être vide")
    private String username;

    @NotNull(message = "Le mot de passe ne peut pas être null")
    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    private String password;
} 