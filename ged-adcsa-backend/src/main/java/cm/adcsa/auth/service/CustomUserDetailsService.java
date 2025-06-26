package cm.adcsa.auth.service;

import cm.adcsa.auth.entity.Utilisateur;
import cm.adcsa.auth.repository.UtilisateurRepository;
import cm.adcsa.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentative de chargement de l'utilisateur avec le nom d'utilisateur: {}", username);
        
        Utilisateur utilisateur = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec le nom d'utilisateur: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });

        log.debug("Utilisateur trouvé: {}", utilisateur.getUsername());
        
        return UserPrincipal.create(utilisateur);
    }
}