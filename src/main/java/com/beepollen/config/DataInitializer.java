package com.beepollen.config;

import com.beepollen.entity.Role;
import com.beepollen.entity.User;
import com.beepollen.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes default data on application startup.
 *
 * <p>Creates a default administrator account if one does not already exist.
 * This ensures the system is always accessible after a fresh deployment.</p>
 *
 * <p><strong>Default admin credentials:</strong></p>
 * <ul>
 *   <li>Username: {@code admin}</li>
 *   <li>Password: {@code admin123}</li>
 *   <li>Email: {@code admin@beepollen.com}</li>
 * </ul>
 *
 * <p><em>These credentials should be changed immediately in production.</em></p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            log.info("Default admin user already exists — skipping initialization");
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@beepollen.com");
        admin.setFullName("System Administrator");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        userRepository.save(admin);
        log.info("✅ Default admin user created (username: admin, password: admin123)");
        log.warn("⚠️  Change the default admin password in production!");

        // Seed default Researcher if not exists
        if (!userRepository.existsByUsername("researcher")) {
            User researcher = new User();
            researcher.setUsername("researcher");
            researcher.setPassword(passwordEncoder.encode("researcher123"));
            researcher.setEmail("researcher@beepollen.com");
            researcher.setFullName("Data Researcher");
            researcher.setRole(Role.RESEARCHER);
            researcher.setEnabled(true);

            userRepository.save(researcher);
            log.info("✅ Default researcher user created (username: researcher, password: researcher123)");
        }
    }
}
