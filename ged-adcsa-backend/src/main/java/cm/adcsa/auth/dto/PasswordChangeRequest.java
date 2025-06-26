package cm.adcsa.auth.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private Long userId;
    private String currentPassword;
    private String newPassword;
} 