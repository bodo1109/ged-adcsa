package cm.adcsa.auth.repository;

import cm.adcsa.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.id = :id")
    void markAsUsed(@Param("id") Long id);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.dateExpiration < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);
} 