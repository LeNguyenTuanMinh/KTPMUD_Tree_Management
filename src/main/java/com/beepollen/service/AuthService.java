package com.beepollen.service;

import com.beepollen.dto.LoginRequest;
import com.beepollen.dto.LoginResponse;
import com.beepollen.dto.RegisterRequest;
import com.beepollen.entity.Role;
import com.beepollen.entity.User;
import com.beepollen.exception.DuplicateResourceException;
import com.beepollen.repository.UserRepository;
import com.beepollen.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling authentication and registration business logic.
 *
 * <p>Orchestrates user login (credential validation + JWT generation) and
 * registration (uniqueness checks, password hashing, persistence, and
 * automatic login after registration).</p>
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Authenticates a user with the provided credentials and returns a JWT token.
     *
     * @param request the login credentials
     * @return a {@link LoginResponse} containing the JWT token and user information
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        log.info("User '{}' logged in successfully", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Registers a new user, persists them to the database, and returns a JWT token.
     *
     * <p>Performs uniqueness validation on both username and email before creating
     * the account. The user is automatically authenticated after registration.</p>
     *
     * @param request the registration data
     * @return a {@link LoginResponse} containing the JWT token and new user information
     * @throws DuplicateResourceException if the username or email is already taken
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Validate Role (Allowlist)
        Role requestedRole = request.getRole() != null ? request.getRole() : Role.STUDENT;
        if (requestedRole != Role.STUDENT && 
            requestedRole != Role.FARMER) {
            throw new IllegalArgumentException("Chỉ cho phép đăng ký tài khoản Học viên hoặc Nông dân.");
        }

        // Create and persist the new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(requestedRole);
        user.setEnabled(true);

        user = userRepository.save(user);
        log.info("New user '{}' registered with role {}", user.getUsername(), user.getRole());

        // Automatically authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
