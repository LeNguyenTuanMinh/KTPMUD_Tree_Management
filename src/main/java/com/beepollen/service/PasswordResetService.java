package com.beepollen.service;

import com.beepollen.entity.PasswordResetToken;
import com.beepollen.entity.User;
import com.beepollen.exception.InvalidTokenException;
import com.beepollen.repository.PasswordResetTokenRepository;
import com.beepollen.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Silently return to prevent email enumeration
            log.debug("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOpt.get();

        // Delete any existing token
        tokenRepository.deleteByUser(user);

        // Generate new token
        String tokenString = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenString)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepository.save(token);

        String resetUrl = baseUrl + "/reset-password?token=" + tokenString;

        // Send email
        sendResetEmail(user, resetUrl);
    }

    private void sendResetEmail(User user, String resetUrl) {
        String subject = "[Bee Pollen] Yêu cầu đặt lại mật khẩu";
        String body = String.format("Xin chào %s,\n\n" +
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" +
                "Click vào link sau để đặt lại mật khẩu (hết hạn sau 30 phút):\n" +
                "%s\n\n" +
                "Nếu bạn không yêu cầu điều này, hãy bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "Bee Pollen Management System", user.getFullName(), resetUrl);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Password reset email sent to: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send password reset email to: {}", user.getEmail(), e);
                // Fallback to console log in case of mail delivery failure
                log.warn("FALLBACK: Password reset URL for {}: {}", user.getEmail(), resetUrl);
            }
        } else {
            // Dev-friendly fallback
            log.warn("JavaMailSender is not configured. Password reset URL for {}: {}", user.getEmail(), resetUrl);
        }
    }

    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token không hợp lệ"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token đã được sử dụng");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token đã hết hạn");
        }

        return resetToken;
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        PasswordResetToken resetToken = validateToken(token);

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getUsername());
    }
}
