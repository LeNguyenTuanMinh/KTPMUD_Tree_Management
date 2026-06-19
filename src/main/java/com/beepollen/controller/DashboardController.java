package com.beepollen.controller;

import com.beepollen.dto.DashboardDTO;
import com.beepollen.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.beepollen.iot.IotDeviceSimulator;
import java.util.Optional;

/**
 * Controller for the dashboard view and REST API endpoint.
 * Serves both the Thymeleaf dashboard page and a JSON API.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final Optional<IotDeviceSimulator> iotSimulator;

    /**
     * Displays the dashboard page with aggregated statistics.
     *
     * @param model the Spring MVC model
     * @return the dashboard view name
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardDTO dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("activeMenu", "dashboard");
        
        boolean iotEnabled = iotSimulator.isPresent();
        boolean iotPaused = iotSimulator.map(IotDeviceSimulator::isPaused).orElse(false);
        model.addAttribute("iotEnabled", iotEnabled);
        model.addAttribute("iotPaused", iotPaused);
        
        return "dashboard";
    }

    /**
     * Toggles the IoT Simulator pause state.
     */
    @PostMapping("/iot/toggle")
    public String toggleIotSimulator() {
        iotSimulator.ifPresent(IotDeviceSimulator::togglePause);
        return "redirect:/dashboard";
    }

    /**
     * Redirects the root URL to the dashboard.
     *
     * @return redirect to /dashboard
     */
    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }

    /**
     * REST endpoint returning dashboard data as JSON.
     *
     * @return dashboard DTO wrapped in ResponseEntity
     */
    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<DashboardDTO> getDashboardData() {
        DashboardDTO dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}
