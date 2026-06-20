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

    @NotBlank(message = "Tên đăng nhập bắt buộc nhập")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu bắt buộc nhập")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Email bắt buộc nhập")
    @Email(message = "Vui lòng nhập địa chỉ email hợp lệ")
    private String email;

    @NotBlank(message = "Họ và tên bắt buộc nhập")
    private String fullName;

    /**
     * Optional role for the new user. Defaults to {@link Role#STUDENT} if not provided.
     */
    private Role role;
}
