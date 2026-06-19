package com.beepollen.controller;

import com.beepollen.dto.BeeColonyDTO;
import com.beepollen.dto.BeeColonyRequest;
import com.beepollen.entity.HealthStatus;
import com.beepollen.exception.DuplicateResourceException;
import com.beepollen.service.BeeColonyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

/**
 * Thymeleaf MVC controller for Bee Colony management pages.
 */
@Controller
@RequestMapping("/colonies")
@RequiredArgsConstructor
@Slf4j
public class BeeColonyWebController {

    private final BeeColonyService beeColonyService;

    /**
     * List all colonies with optional keyword search.
     */
    @GetMapping
    public String listColonies(@RequestParam(value = "keyword", required = false) String keyword,
                               @PageableDefault(size = 10, sort = "id") Pageable pageable,
                               Model model) {
        Page<BeeColonyDTO> pageData = (keyword != null && !keyword.isBlank())
                ? beeColonyService.searchColonies(keyword, pageable)
                : beeColonyService.getAllColonies(pageable);
                
        model.addAttribute("colonies", pageData.getContent());
        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("healthStatuses", HealthStatus.values());
        model.addAttribute("activeMenu", "colonies");
        return "colonies/list";
    }

    /**
     * Show form to create a new colony.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("colony", new BeeColonyRequest());
        model.addAttribute("healthStatuses", HealthStatus.values());
        model.addAttribute("activeMenu", "colonies");
        return "colonies/form";
    }

    /**
     * Process colony creation form.
     */
    @PostMapping
    public String createColony(@Valid @ModelAttribute("colony") BeeColonyRequest request,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("healthStatuses", HealthStatus.values());
            model.addAttribute("activeMenu", "colonies");
            return "colonies/form";
        }
        try {
            beeColonyService.createColony(request);
            redirectAttributes.addFlashAttribute("successMessage", "Colony created successfully!");
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("healthStatuses", HealthStatus.values());
            model.addAttribute("activeMenu", "colonies");
            return "colonies/form";
        }
        return "redirect:/colonies";
    }

    /**
     * Show form to edit an existing colony.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        BeeColonyDTO colony = beeColonyService.getColonyById(id);

        BeeColonyRequest request = new BeeColonyRequest();
        request.setColonyCode(colony.getColonyCode());
        request.setBeeSpecies(colony.getBeeSpecies());
        request.setLatitude(colony.getLatitude());
        request.setLongitude(colony.getLongitude());
        request.setHealthStatus(colony.getHealthStatus());
        request.setEstimatedPopulation(colony.getEstimatedPopulation());

        model.addAttribute("colony", request);
        model.addAttribute("colonyId", id);
        model.addAttribute("healthStatuses", HealthStatus.values());
        model.addAttribute("activeMenu", "colonies");
        return "colonies/form";
    }

    /**
     * Process colony update form.
     */
    @PostMapping("/{id}/edit")
    public String updateColony(@PathVariable Long id,
                               @Valid @ModelAttribute("colony") BeeColonyRequest request,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("colonyId", id);
            model.addAttribute("healthStatuses", HealthStatus.values());
            model.addAttribute("activeMenu", "colonies");
            return "colonies/form";
        }
        try {
            beeColonyService.updateColony(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Colony updated successfully!");
        } catch (DuplicateResourceException e) {
            model.addAttribute("colonyId", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("healthStatuses", HealthStatus.values());
            model.addAttribute("activeMenu", "colonies");
            return "colonies/form";
        }
        return "redirect:/colonies";
    }

    /**
     * View colony details.
     */
    @GetMapping("/{id}")
    public String viewColony(@PathVariable Long id, Model model) {
        model.addAttribute("colony", beeColonyService.getColonyById(id));
        model.addAttribute("activeMenu", "colonies");
        return "colonies/detail";
    }

    /**
     * Delete a colony.
     */
    @GetMapping("/{id}/delete")
    public String deleteColony(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        beeColonyService.deleteColony(id);
        redirectAttributes.addFlashAttribute("successMessage", "Colony deleted successfully!");
        return "redirect:/colonies";
    }
}
