package cm.adcsa.auth.dto;

import cm.adcsa.auth.enums.StatutUtilisateur;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UtilisateurDto utilisateur;

    @Data
    @Builder
    public static class UtilisateurDto {
        private Long id;
        private String username;
        private String email;
        private String nom;
        private String prenom;
        private StatutUtilisateur statut;
        private Boolean isFirstLogin;
        private Boolean isPasswordExpired;
        private Boolean isFirstLoginExpired;
    }
} 