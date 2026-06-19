package com.beepollen.dto;

import com.beepollen.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for user registration requests.
 * Used by both the REST API ({@code /api/auth/register}) and the Thymeleaf registration form.
 *
 * <p>If {@code role} is not specified, it defaults to {@link Role#STUDENT} during registration.</p>
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    /**
     * Optional role for the new user. Defaults to {@link Role#STUDENT} if not provided.
     */
    private Role role;
}
