package com.beepollen.service;

import com.beepollen.entity.Role;
import com.beepollen.entity.User;
import com.beepollen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setEnabled(true);
    }

    @Test
    void updateUserRoleAndStatus_shouldThrowException_whenDemotingLastAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndEnabled(Role.ADMIN, true)).thenReturn(1L);

        // Try to change role to BEEKEEPER
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRoleAndStatus(1L, Role.BEEKEEPER, true);
        });

        // Try to disable the admin
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRoleAndStatus(1L, Role.ADMIN, false);
        });
        
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRoleAndStatus_shouldSucceed_whenOtherAdminsExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndEnabled(Role.ADMIN, true)).thenReturn(2L); // 2 active admins exist

        // Try to change role to BEEKEEPER
        userService.updateUserRoleAndStatus(1L, Role.BEEKEEPER, true);

        verify(userRepository, times(1)).save(adminUser);
    }
}
