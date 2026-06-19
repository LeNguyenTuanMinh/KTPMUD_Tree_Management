package com.beepollen.controller;

import com.beepollen.dto.ForgotPasswordRequest;
import com.beepollen.dto.ResetPasswordRequest;
import com.beepollen.exception.InvalidTokenException;
import com.beepollen.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@ModelAttribute ForgotPasswordRequest request) {
        try {
            passwordResetService.initiatePasswordReset(request.getEmail());
        } catch (Exception e) {
            log.error("Error processing forgot password request", e);
            return "redirect:/forgot-password?error=true";
        }
        
        // Always redirect to success to prevent email enumeration
        return "redirect:/forgot-password?sent=true";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        try {
            passwordResetService.validateToken(token);
            
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken(token);
            model.addAttribute("token", token);
            model.addAttribute("resetPasswordRequest", request);
            
            return "auth/reset-password";
        } catch (InvalidTokenException e) {
            String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/forgot-password?tokenError=" + encodedMessage;
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@ModelAttribute ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());
            return "redirect:/login?passwordReset=true";
        } catch (InvalidTokenException e) {
            String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/reset-password?token=" + request.getToken() + "&error=" + encodedMessage;
        } catch (IllegalArgumentException e) {
            String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/reset-password?token=" + request.getToken() + "&error=" + encodedMessage;
        } catch (Exception e) {
            log.error("Error resetting password", e);
            String encodedMessage = URLEncoder.encode("Có lỗi xảy ra. Vui lòng thử lại.", StandardCharsets.UTF_8);
            return "redirect:/reset-password?token=" + request.getToken() + "&error=" + encodedMessage;
        }
    }
}
