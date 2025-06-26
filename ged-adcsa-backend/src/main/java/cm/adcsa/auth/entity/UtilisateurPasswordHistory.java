package cm.adcsa.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "utilisateur_password_history")
public class UtilisateurPasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private String password;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UtilisateurPasswordHistory)) return false;
        UtilisateurPasswordHistory that = (UtilisateurPasswordHistory) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(utilisateur, that.utilisateur) && 
               Objects.equals(changedAt, that.changedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, utilisateur, changedAt);
    }

    @Override
    public String toString() {
        return "UtilisateurPasswordHistory{" +
                "id=" + id +
                ", utilisateur=" + utilisateur +
                ", changedAt=" + changedAt +
                '}';
    }
} 