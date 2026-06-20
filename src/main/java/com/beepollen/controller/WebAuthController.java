package com.beepollen.controller;

import com.beepollen.dto.RegisterRequest;
import com.beepollen.exception.DuplicateResourceException;
import com.beepollen.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC controller serving Thymeleaf-rendered authentication pages.
 *
 * <p>Handles the login and registration views for browser-based users.
 * The actual login POST is handled by Spring Security's form login mechanism;
 * this controller only provides the login page view.</p>
 */
@Controller
public class WebAuthController {

    private static final Logger log = LoggerFactory.getLogger(WebAuthController.class);

    private final AuthService authService;

    public WebAuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Displays the login page.
     *
     * @return the "login" Thymeleaf template name
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * Displays the registration page with an empty form-backing object.
     *
     * @param model the Spring MVC model
     * @return the "register" Thymeleaf template name
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    /**
     * Handles the registration form submission.
     *
     * <p>On success, redirects to the login page with a "registered" flag.
     * On validation failure, re-renders the registration form with error messages.
     * On duplicate username/email, re-renders with an appropriate error message.</p>
     *
     * @param registerRequest    the form-bound registration data
     * @param bindingResult      validation results
     * @param model              the Spring MVC model
     * @param redirectAttributes attributes to pass to the redirect target
     * @return a view name or redirect URL
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Return to form if validation errors exist
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            log.info("New user '{}' registered via web form", registerRequest.getUsername());
            return "redirect:/login?registered";
        } catch (DuplicateResourceException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }
}
