package com.beepollen.config;

import com.beepollen.security.CustomUserDetailsService;
import com.beepollen.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for the Bee Pollen & Plant Management System.
 *
 * <p>This is a hybrid application that serves both:
 * <ul>
 *   <li><b>REST API</b> endpoints under {@code /api/**} secured with JWT Bearer tokens</li>
 *   <li><b>Thymeleaf web views</b> secured with form-based login and server-side sessions</li>
 * </ul>
 *
 * <p>Key design decisions:</p>
 * <ul>
 *   <li>CSRF is disabled globally for simplicity; re-enable for production web forms</li>
 *   <li>Session policy is {@code IF_REQUIRED} to support both stateless API and stateful web</li>
 *   <li>The JWT filter runs before {@link UsernamePasswordAuthenticationFilter} so that
 *       API requests can authenticate via Bearer token without hitting form login</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Configures the security filter chain with URL-based authorization rules,
     * form login for web views, and JWT filter registration for API endpoints.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for simplicity (hybrid API + web app)
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/iot/**",
                                "/login",
                                "/register",
                                "/forgot-password",
                                "/forgot-password/**",
                                "/reset-password",
                                "/reset-password/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // Admin-only API endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Research-related API endpoints
                        .requestMatchers("/api/plants/**", "/api/pollens/**")
                        .hasAnyRole("ADMIN", "RESEARCHER")

                        // Beekeeping-related API endpoints
                        .requestMatchers("/api/colonies/**", "/api/tracking/**")
                        .hasAnyRole("ADMIN", "BEEKEEPER")

                        // Web UI Research Endpoints (Read-only for Researcher)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/plants", "/plants/{id}", "/pollens", "/pollens/{id}").hasAnyRole("ADMIN", "RESEARCHER", "BEEKEEPER")
                        
                        // Web UI Write Endpoints (Blocked for Researcher)
                        .requestMatchers("/plants/**", "/pollens/**").hasAnyRole("ADMIN", "BEEKEEPER")
                        
                        // Web UI other endpoints (colonies, tracking)
                        .requestMatchers("/colonies/**", "/collections/**").hasAnyRole("ADMIN", "BEEKEEPER")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Session management: IF_REQUIRED supports both stateless API and stateful web
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // Set the custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before the default username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Form login for Thymeleaf web views
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * BCrypt password encoder for hashing and verifying passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean so it can be injected
     * into services (e.g., {@code AuthService}) for programmatic authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * DAO-based authentication provider wired to the custom {@link CustomUserDetailsService}
     * and BCrypt password encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
