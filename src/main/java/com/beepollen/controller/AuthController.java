package com.beepollen.controller;

import com.beepollen.dto.LoginRequest;
import com.beepollen.dto.LoginResponse;
import com.beepollen.dto.RegisterRequest;
import com.beepollen.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authentication endpoints for API clients.
 *
 * <p>All endpoints are publicly accessible (configured in {@code SecurityConfig})
 * and return JSON responses containing JWT tokens upon successful authentication.</p>
 *
 * <table>
 *   <tr><th>Method</th><th>Endpoint</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/api/auth/login</td><td>Authenticate and obtain JWT</td></tr>
 *   <tr><td>POST</td><td>/api/auth/register</td><td>Register a new user and obtain JWT</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user with username and password credentials.
     *
     * @param request the login credentials
     * @return 200 OK with a {@link LoginResponse} containing the JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Registers a new user account and returns a JWT token for immediate use.
     *
     * @param request the registration data
     * @return 201 Created with a {@link LoginResponse} containing the JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
