package cm.adcsa.auth.service;

import cm.adcsa.auth.dto.*;
import cm.adcsa.auth.entity.Role;
import cm.adcsa.auth.entity.Utilisateur;
import cm.adcsa.auth.enums.StatutUtilisateur;
import cm.adcsa.auth.repository.RoleRepository;
import cm.adcsa.auth.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setUsername(request.getUsername());
        utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        utilisateur.setStatut(StatutUtilisateur.ACTIF);
        utilisateur.setIsFirstLogin(true);

        return mapToUserResponse(utilisateurRepository.save(utilisateur));
    }

    public List<UserResponse> getAllUsers() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return utilisateurRepository.findById(id)
                .map(this::mapToUserResponse)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserCreateRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + id));

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToUserResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        Role role = new Role();
        role.setNom(request.getNom());
        role.setDescription(request.getDescription());
        return mapToRoleResponse(roleRepository.save(role));
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse assignRoleToUser(Long userId, Long roleId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé avec l'id: " + roleId));

        utilisateur.getRoles().add(role);
        return mapToUserResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public UserResponse removeRoleFromUser(Long userId, Long roleId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Rôle non trouvé avec l'id: " + roleId));

        utilisateur.getRoles().remove(role);
        return mapToUserResponse(utilisateurRepository.save(utilisateur));
    }

    private UserResponse mapToUserResponse(Utilisateur utilisateur) {
        UserResponse response = new UserResponse();
        response.setId(utilisateur.getId());
        response.setNom(utilisateur.getNom());
        response.setPrenom(utilisateur.getPrenom());
        response.setEmail(utilisateur.getEmail());
        response.setUsername(utilisateur.getUsername());
        response.setStatut(utilisateur.getStatut());
        response.setDerniereConnexion(utilisateur.getDerniereConnexion());
        response.setDateCreation(utilisateur.getDateCreation());
        response.setDateModification(utilisateur.getDateModification());
        response.setCompteVerrouille(utilisateur.getCompteVerrouille());
        response.setIsFirstLogin(utilisateur.getIsFirstLogin());
        response.setRoles(utilisateur.getRoles().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toSet()));
        return response;
    }

    private RoleResponse mapToRoleResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setNom(role.getNom());
        response.setDescription(role.getDescription());
        return response;
    }
} 