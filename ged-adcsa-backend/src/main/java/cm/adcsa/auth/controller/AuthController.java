package cm.adcsa.auth.controller;

import cm.adcsa.auth.dto.*;
import cm.adcsa.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.debug("Réception d'une requête de connexion pour l'utilisateur: {}", loginRequest.getUsername());
        log.debug("Données de la requête - username: {}, password: {}", 
            loginRequest.getUsername(), 
            loginRequest.getPassword() != null ? "présent" : "absent");
        
        try {
            LoginResponse response = authService.login(loginRequest);
            log.debug("Connexion réussie pour l'utilisateur: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la connexion pour l'utilisateur: {}. Erreur: {}", 
                loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Identifiants invalides"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Réception d'une requête de rafraîchissement de token");
        try {
            TokenRefreshResponse response = authService.refreshToken(request.getRefreshToken());
            log.debug("Token rafraîchi avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement du token. Erreur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Token de rafraîchissement invalide"));
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        log.debug("Réception d'une requête de réinitialisation de mot de passe pour l'email: {}", request.getEmail());
        try {
            authService.requestPasswordReset(request.getEmail());
            log.debug("Email de réinitialisation envoyé avec succès à: {}", request.getEmail());
            return ResponseEntity.ok(new MessageResponse("Email de réinitialisation envoyé"));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation. Erreur: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Erreur lors de l'envoi de l'email de réinitialisation"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        log.debug("Réception d'une requête de confirmation de réinitialisation de mot de passe");
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            log.debug("Mot de passe réinitialisé avec succès");
            return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe. Erreur: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Erreur lors de la réinitialisation du mot de passe"));
        }
    }
}