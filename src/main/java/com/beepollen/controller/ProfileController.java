package com.beepollen.controller;

import com.beepollen.dto.PasswordChangeRequest;
import com.beepollen.entity.User;
import com.beepollen.service.ActivityLogService;
import com.beepollen.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;
    private final ActivityLogService activityLogService;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        model.addAttribute("user", user);
        model.addAttribute("recentActivities", activityLogService.getRecentActivityForUser(username, 10));

        if (!model.containsAttribute("passwordChangeRequest")) {
            model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
        }

        model.addAttribute("activeMenu", "profile");
        return "profile/index";
    }

    @PostMapping("/password")
    public String changePassword(
            Authentication authentication,
            @Valid @ModelAttribute("passwordChangeRequest") PasswordChangeRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordChangeRequest", bindingResult);
            redirectAttributes.addFlashAttribute("passwordChangeRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng sửa các lỗi trong form mật khẩu.");
            return "redirect:/profile";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("passwordChangeRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("passwordChangeRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error changing password", e);
            redirectAttributes.addFlashAttribute("passwordChangeRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình đổi mật khẩu.");
        }

        return "redirect:/profile";
    }
}
