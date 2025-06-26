package cm.adcsa.auth.repository;

import cm.adcsa.auth.entity.Utilisateur;
import cm.adcsa.auth.enums.StatutUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsername(String username);
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Utilisateur> findByStatut(StatutUtilisateur statut);
    List<Utilisateur> findByCompteVerrouilleTrue();
    List<Utilisateur> findByIsFirstLoginTrue();
    List<Utilisateur> findByPasswordChangedAtBefore(LocalDateTime date);

    @Query("SELECT u FROM Utilisateur u WHERE u.tentativesEchec >= :maxAttempts AND u.compteVerrouille = true")
    List<Utilisateur> findLockedAccounts(@Param("maxAttempts") int maxAttempts);

    @Modifying
    @Query("UPDATE Utilisateur u SET u.tentativesEchec = 0, u.compteVerrouille = false, u.dateVerrouillage = null WHERE u.id = :id")
    void resetLoginAttempts(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Utilisateur u SET u.password = :newPassword, u.passwordChangedAt = :now WHERE u.id = :id")
    void updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Utilisateur u SET u.derniereConnexion = :now WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.statut = :statut")
    long countByStatut(@Param("statut") StatutUtilisateur statut);

    @Query("SELECT u FROM Utilisateur u WHERE u.firstLoginExpiresAt < :now")
    List<Utilisateur> findExpiredFirstLogins(@Param("now") LocalDateTime now);
}