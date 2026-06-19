package com.beepollen.security;

import com.beepollen.entity.User;
import com.beepollen.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 *
 * <p>Loads user-specific data from the database via {@link UserRepository} and
 * maps the application's {@link User} entity to a Spring Security
 * {@link org.springframework.security.core.userdetails.User} that the framework
 * can use for authentication and authorization.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username from the database.
     *
     * <p>The returned {@link UserDetails} includes:
     * <ul>
     *   <li>The user's encoded password</li>
     *   <li>A single granted authority of the form {@code ROLE_<ROLE_NAME>}</li>
     *   <li>The user's enabled/disabled status</li>
     * </ul>
     *
     * @param username the username to look up
     * @return a fully populated {@link UserDetails} instance
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),           // enabled
                true,                        // accountNonExpired
                true,                        // credentialsNonExpired
                true,                        // accountNonLocked
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }
}
