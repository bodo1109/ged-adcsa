package cm.adcsa.auth.dto;

import cm.adcsa.auth.enums.StatutUtilisateur;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String username;
    private StatutUtilisateur statut;
    private LocalDateTime derniereConnexion;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Boolean compteVerrouille;
    private Boolean isFirstLogin;
    private Set<RoleResponse> roles;
} 