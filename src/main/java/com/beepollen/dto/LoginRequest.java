package com.beepollen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for authentication login requests.
 * Used by both the REST API ({@code /api/auth/login}) and web form submission.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
