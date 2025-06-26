package cm.adcsa.auth.entity;

import cm.adcsa.auth.enums.StatutUtilisateur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"utilisateur\"")
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutUtilisateur statut;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "date_expiration_token")
    private LocalDateTime dateExpirationToken;

    @Column(name = "date_verrouillage")
    private LocalDateTime dateVerrouillage;

    @Column(name = "tentatives_connexion")
    private Integer tentativesEchec;

    @Column(name = "compte_verrouille")
    private Boolean compteVerrouille;

    @Column(name = "token_reset_password", length = 255)
    private String tokenResetPassword;

    @Column(name = "is_first_login", nullable = false)
    private Boolean isFirstLogin;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "first_login_expires_at")
    private LocalDateTime firstLoginExpiresAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "utilisateur_role",
        joinColumns = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UtilisateurPasswordHistory> passwordHistory = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PasswordResetToken> resetTokens = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNom()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(compteVerrouille);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return statut.isActif();
    }

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public void incrementTentativesEchec() {
        this.tentativesEchec = (this.tentativesEchec == null ? 0 : this.tentativesEchec) + 1;
    }

    public void resetTentativesEchec() {
        this.tentativesEchec = 0;
        this.compteVerrouille = false;
        this.dateVerrouillage = null;
    }

    public void verrouillerCompte() {
        this.compteVerrouille = true;
        this.dateVerrouillage = LocalDateTime.now();
    }

    public void updateDerniereConnexion() {
        this.derniereConnexion = LocalDateTime.now();
    }

    public void setFirstLogin() {
        this.isFirstLogin = true;
        this.firstLoginExpiresAt = LocalDateTime.now().plusDays(7); // 7 jours par défaut
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.isFirstLogin = false;
        this.firstLoginExpiresAt = null;
    }

    public boolean isPasswordExpired(int expirationDays) {
        if (this.passwordChangedAt == null) {
            return true;
        }
        return this.passwordChangedAt.plusDays(expirationDays).isBefore(LocalDateTime.now());
    }

    public boolean isFirstLoginExpired() {
        if (this.firstLoginExpiresAt == null) {
            return false;
        }
        return this.firstLoginExpiresAt.isBefore(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StatutUtilisateur getStatut() {
        return statut;
    }

    public void setStatut(StatutUtilisateur statut) {
        this.statut = statut;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public Integer getTentativesEchec() {
        return tentativesEchec;
    }

    public void setTentativesEchec(Integer tentativesEchec) {
        this.tentativesEchec = tentativesEchec;
    }

    public LocalDateTime getDateVerrouillage() {
        return dateVerrouillage;
    }

    public void setDateVerrouillage(LocalDateTime dateVerrouillage) {
        this.dateVerrouillage = dateVerrouillage;
    }

    public String getTokenResetPassword() {
        return tokenResetPassword;
    }

    public void setTokenResetPassword(String tokenResetPassword) {
        this.tokenResetPassword = tokenResetPassword;
    }

    public LocalDateTime getDateExpirationToken() {
        return dateExpirationToken;
    }

    public void setDateExpirationToken(LocalDateTime dateExpirationToken) {
        this.dateExpirationToken = dateExpirationToken;
    }

    public Boolean getIsFirstLogin() {
        return isFirstLogin;
    }

    public void setIsFirstLogin(Boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public LocalDateTime getFirstLoginExpiresAt() {
        return firstLoginExpiresAt;
    }

    public void setFirstLoginExpiresAt(LocalDateTime firstLoginExpiresAt) {
        this.firstLoginExpiresAt = firstLoginExpiresAt;
    }

    public Set<UtilisateurPasswordHistory> getPasswordHistory() {
        return new HashSet<>(passwordHistory);
    }

    public void setPasswordHistory(Set<UtilisateurPasswordHistory> passwordHistory) {
        this.passwordHistory.clear();
        this.passwordHistory.addAll(passwordHistory);
    }

    public Set<PasswordResetToken> getResetTokens() {
        return new HashSet<>(resetTokens);
    }

    public void setResetTokens(Set<PasswordResetToken> resetTokens) {
        this.resetTokens.clear();
        this.resetTokens.addAll(resetTokens);
    }

    public Set<Role> getRoles() {
        return new HashSet<>(roles);
    }

    public void setRoles(Set<Role> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean isAccountLocked() {
        if (this.statut == StatutUtilisateur.BLOQUE) {
            return true;
        }
        
        if (this.dateVerrouillage != null) {
            return LocalDateTime.now().isBefore(this.dateVerrouillage.plusMinutes(30));
        }
        
        return false;
    }

    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur)) return false;
        Utilisateur that = (Utilisateur) o;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", statut=" + statut +
                '}';
    }

    public Boolean getCompteVerrouille() {
        return compteVerrouille;
    }
}