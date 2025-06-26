package cm.adcsa.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleCreateRequest {
    @NotBlank(message = "Le nom du rôle est obligatoire")
    @Size(max = 100, message = "Le nom du rôle ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 200, message = "La description ne doit pas dépasser 200 caractères")
    private String description;
} 