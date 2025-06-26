package cm.adcsa.auth.dto.response;

import cm.adcsa.auth.entity.Utilisateur;
import cm.adcsa.auth.enums.StatutUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UtilisateurDto utilisateur;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UtilisateurDto {
        private Long id;
        private String username;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private StatutUtilisateur statut;
        private LocalDateTime derniereConnexion;
        private Boolean isFirstLogin;
        private LocalDateTime firstLoginExpiresAt;
        private Set<String> roles;

        public static UtilisateurDto fromEntity(Utilisateur utilisateur) {
            return UtilisateurDto.builder()
                    .id(utilisateur.getId())
                    .username(utilisateur.getUsername())
                    .nom(utilisateur.getNom())
                    .prenom(utilisateur.getPrenom())
                    .email(utilisateur.getEmail())
                    .statut(utilisateur.getStatut())
                    .derniereConnexion(utilisateur.getDerniereConnexion())
                    .isFirstLogin(utilisateur.getIsFirstLogin())
                    .firstLoginExpiresAt(utilisateur.getFirstLoginExpiresAt())
                    .roles(utilisateur.getRoles().stream()
                            .map(role -> role.getNom())
                            .collect(Collectors.toSet()))
                    .build();
        }
    }
}