package com.beepollen.controller;

import com.beepollen.entity.Role;
import com.beepollen.entity.User;
import com.beepollen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserWebController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("activeMenu", "users");
        return "users/list";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, Authentication authentication) {
        User targetUser = userService.getUserById(id);
        
        // Check if Admin is editing their own account
        boolean isSelf = targetUser.getUsername().equals(authentication.getName());
        
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("isSelf", isSelf);
        model.addAttribute("roles", Role.values());
        model.addAttribute("activeMenu", "users");
        return "users/form";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id, 
                             @RequestParam Role role, 
                             @RequestParam(required = false, defaultValue = "false") boolean enabled,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRoleAndStatus(id, role, enabled);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/users";
    }
}
