package cm.adcsa.auth.repository;

import cm.adcsa.auth.entity.Utilisateur;
import cm.adcsa.auth.entity.UtilisateurPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilisateurPasswordHistoryRepository extends JpaRepository<UtilisateurPasswordHistory, Long> {
    @Query("SELECT h FROM UtilisateurPasswordHistory h WHERE h.utilisateur = :utilisateur ORDER BY h.changedAt DESC")
    List<UtilisateurPasswordHistory> findTop5ByUtilisateurOrderByChangedAtDesc(@Param("utilisateur") Utilisateur utilisateur);
} 