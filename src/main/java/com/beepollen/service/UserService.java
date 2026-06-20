package com.beepollen.service;

import com.beepollen.entity.User;
import com.beepollen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public java.util.List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void updateUserRoleAndStatus(Long id, com.beepollen.entity.Role newRole, boolean enabled) {
        User user = getUserById(id);

        // Check if we are demoting or disabling the last active ADMIN
        if (user.getRole() == com.beepollen.entity.Role.ADMIN && Boolean.TRUE.equals(user.getEnabled())) {
            if (newRole != com.beepollen.entity.Role.ADMIN || !enabled) {
                long activeAdmins = userRepository.countByRoleAndEnabled(com.beepollen.entity.Role.ADMIN, true);
                if (activeAdmins <= 1) {
                    throw new IllegalArgumentException("Không thể thực hiện vì đây là Admin cuối cùng đang hoạt động.");
                }
            }
        }

        user.setRole(newRole);
        user.setEnabled(enabled);
        userRepository.save(user);
    }
}
