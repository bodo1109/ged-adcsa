package cm.adcsa.auth.service;

import cm.adcsa.auth.dto.*;
import cm.adcsa.auth.entity.PasswordResetToken;
import cm.adcsa.auth.entity.Utilisateur;
import cm.adcsa.auth.entity.UtilisateurPasswordHistory;
import cm.adcsa.auth.enums.StatutUtilisateur;
import cm.adcsa.auth.repository.PasswordResetTokenRepository;
import cm.adcsa.auth.repository.UtilisateurPasswordHistoryRepository;
import cm.adcsa.auth.repository.UtilisateurRepository;
import cm.adcsa.auth.security.JwtTokenProvider;
import cm.adcsa.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UtilisateurPasswordHistoryRepository passwordHistoryRepository;

    @Value("${app.security.max-login-attempts}")
    private int maxLoginAttempts;

    @Value("${app.security.account-lock-duration}")
    private long accountLockDuration;

    @Value("${app.security.password-expiration-days}")
    private int passwordExpirationDays;

    @Value("${app.security.first-login-expiration-days}")
    private int firstLoginExpirationDays;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        logger.debug("Tentative de connexion pour l'utilisateur: {}", loginRequest.getUsername());
        
        try {
            // Authentification avec Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Récupération de l'utilisateur après authentification réussie
            Utilisateur utilisateur = utilisateurRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> {
                        logger.error("Utilisateur non trouvé après authentification: {}", loginRequest.getUsername());
                        return new RuntimeException("Utilisateur non trouvé");
                    });

            logger.debug("Authentification réussie pour l'utilisateur: {}", utilisateur.getUsername());

            // Vérification du statut du compte
            if (Boolean.TRUE.equals(utilisateur.getCompteVerrouille())) {
                if (utilisateur.getDateVerrouillage() != null && 
                    utilisateur.getDateVerrouillage().plusMinutes(accountLockDuration / 60000).isAfter(LocalDateTime.now())) {
                    logger.error("Compte verrouillé pour l'utilisateur: {}", utilisateur.getUsername());
                    throw new RuntimeException("Compte verrouillé. Veuillez réessayer plus tard.");
                } else {
                    logger.debug("Déverrouillage du compte pour l'utilisateur: {}", utilisateur.getUsername());
                    utilisateur.resetTentativesEchec();
                    utilisateurRepository.save(utilisateur);
                }
            }

            // Génération du token JWT
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String jwt = tokenProvider.generateAccessToken(userPrincipal);
            String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);
            logger.debug("Token JWT généré avec succès pour l'utilisateur: {}", utilisateur.getUsername());

            // Mise à jour des informations de connexion
            utilisateur.resetTentativesEchec();
            utilisateur.updateDerniereConnexion();
            utilisateurRepository.save(utilisateur);

            // Préparation de la réponse
            boolean isFirstLogin = Boolean.TRUE.equals(utilisateur.getIsFirstLogin());
            boolean isPasswordExpired = utilisateur.isPasswordExpired(passwordExpirationDays);
            boolean isFirstLoginExpired = utilisateur.isFirstLoginExpired();

            return LoginResponse.builder()
                    .accessToken(jwt)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .utilisateur(LoginResponse.UtilisateurDto.builder()
                            .id(utilisateur.getId())
                            .username(utilisateur.getUsername())
                            .email(utilisateur.getEmail())
                            .nom(utilisateur.getNom())
                            .prenom(utilisateur.getPrenom())
                            .statut(utilisateur.getStatut())
                            .isFirstLogin(isFirstLogin)
                            .isPasswordExpired(isPasswordExpired)
                            .isFirstLoginExpired(isFirstLoginExpired)
                            .build())
                    .build();

        } catch (Exception e) {
            logger.error("Erreur lors de la connexion: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la connexion: " + e.getMessage());
        }
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), utilisateur.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        if (isPasswordInHistory(utilisateur, request.getNewPassword())) {
            throw new RuntimeException("Le nouveau mot de passe ne peut pas être identique à un des 3 derniers mots de passe");
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        utilisateur.updatePassword(encodedPassword);
        utilisateurRepository.save(utilisateur);

        // Sauvegarder l'ancien mot de passe dans l'historique
        UtilisateurPasswordHistory passwordHistory = UtilisateurPasswordHistory.builder()
                .utilisateur(utilisateur)
                .password(utilisateur.getPassword())
                .changedAt(LocalDateTime.now())
                .build();
        passwordHistoryRepository.save(passwordHistory);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(24);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .utilisateur(utilisateur)
                .dateExpiration(expirationDate)
                .build();

        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (resetToken.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        Utilisateur utilisateur = resetToken.getUtilisateur();
        String encodedPassword = passwordEncoder.encode(newPassword);
        utilisateur.updatePassword(encodedPassword);
        utilisateurRepository.save(utilisateur);

        passwordResetTokenRepository.delete(resetToken);
    }

    private boolean isPasswordInHistory(Utilisateur utilisateur, String newPassword) {
        return utilisateur.getPasswordHistory().stream()
                .limit(3)
                .anyMatch(history -> passwordEncoder.matches(newPassword, history.getPassword()));
    }

    @Transactional
    public TokenRefreshResponse refreshToken(String refreshToken) {
        try {
            if (!tokenProvider.validateToken(refreshToken)) {
                throw new RuntimeException("Token de rafraîchissement invalide");
            }

            String email = tokenProvider.getEmailFromJWT(refreshToken);
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            UserPrincipal userPrincipal = UserPrincipal.create(utilisateur);
            String newToken = tokenProvider.generateAccessToken(userPrincipal);
            String newRefreshToken = tokenProvider.generateRefreshToken(userPrincipal);

            return TokenRefreshResponse.builder()
                    .accessToken(newToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .build();

        } catch (Exception e) {
            logger.error("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du rafraîchissement du token");
        }
    }
}