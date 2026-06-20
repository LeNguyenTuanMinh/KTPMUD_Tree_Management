package com.beepollen.controller;

import com.beepollen.dto.PollenDTO;
import com.beepollen.dto.PollenRequest;
import com.beepollen.entity.Plant;
import com.beepollen.repository.PlantRepository;
import com.beepollen.service.PollenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

/**
 * Web (Thymeleaf) controller for Pollen CRUD operations.
 * Provides server-rendered HTML pages under /pollens.
 */
@Controller
@RequestMapping("/pollens")
@RequiredArgsConstructor
public class PollenWebController {

    private final PollenService pollenService;
    private final PlantRepository plantRepository;

    /**
     * GET /pollens
     * Lists all pollens with optional keyword search.
     */
    @GetMapping
    public String listPollens(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Model model) {
        Page<PollenDTO> pageData;
        if (keyword != null && !keyword.isBlank()) {
            pageData = pollenService.searchPollens(keyword, pageable);
        } else {
            pageData = pollenService.getAllPollens(pageable);
        }
        model.addAttribute("pollens", pageData.getContent());
        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "pollens");
        return "pollens/list";
    }

    /**
     * GET /pollens/new
     * Shows the form for creating a new pollen.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pollen", new PollenRequest());
        model.addAttribute("allPlants", plantRepository.findAll());
        model.addAttribute("activeMenu", "pollens");
        return "pollens/form";
    }

    /**
     * POST /pollens
     * Handles form submission for creating a new pollen.
     */
    @PostMapping
    public String createPollen(
            @Valid @ModelAttribute("pollen") PollenRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allPlants", plantRepository.findAll());
            model.addAttribute("activeMenu", "pollens");
            return "pollens/form";
        }
        pollenService.createPollen(request);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm mới phấn hoa thành công!");
        return "redirect:/pollens";
    }

    /**
     * GET /pollens/{id}/edit
     * Shows the form for editing an existing pollen.
     * Pre-populates the PollenRequest from the existing entity.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        PollenDTO pollenDTO = pollenService.getPollenById(id);

        PollenRequest request = new PollenRequest();
        request.setName(pollenDTO.getName());
        request.setShape(pollenDTO.getShape());
        request.setSizeMicron(pollenDTO.getSizeMicron());
        request.setSurfaceCharacteristic(pollenDTO.getSurfaceCharacteristic());
        request.setMicroscopeImage(pollenDTO.getMicroscopeImage());

        // Populate associated plant IDs from the pollen's current associations
        List<Plant> associatedPlants = plantRepository.findByPollenId(id);
        List<Long> associatedPlantIds = associatedPlants.stream()
                .map(Plant::getId)
                .collect(Collectors.toList());
        request.setPlantIds(associatedPlantIds);

        List<Plant> allPlants = plantRepository.findAll();

        model.addAttribute("pollen", request);
        model.addAttribute("pollenId", id);
        model.addAttribute("allPlants", allPlants);
        model.addAttribute("activeMenu", "pollens");
        return "pollens/form";
    }

    /**
     * POST /pollens/{id}/edit
     * Handles form submission for updating an existing pollen.
     */
    @PostMapping("/{id}/edit")
    public String updatePollen(
            @PathVariable Long id,
            @Valid @ModelAttribute("pollen") PollenRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pollenId", id);
            model.addAttribute("allPlants", plantRepository.findAll());
            model.addAttribute("activeMenu", "pollens");
            return "pollens/form";
        }
        pollenService.updatePollen(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phấn hoa thành công!");
        return "redirect:/pollens";
    }

    /**
     * GET /pollens/{id}
     * Shows the detail view for a single pollen.
     */
    @GetMapping("/{id}")
    public String viewPollen(@PathVariable Long id, Model model) {
        PollenDTO pollenDTO = pollenService.getPollenById(id);

        // Retrieve associated plants for display
        List<Plant> associatedPlants = plantRepository.findByPollenId(id);

        model.addAttribute("pollen", pollenDTO);
        model.addAttribute("associatedPlants", associatedPlants);
        model.addAttribute("activeMenu", "pollens");
        return "pollens/detail";
    }

    /**
     * GET /pollens/{id}/delete
     * Deletes a pollen and redirects to the list page.
     */
    @GetMapping("/{id}/delete")
    public String deletePollen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pollenService.deletePollen(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xoá phấn hoa thành công!");
        return "redirect:/pollens";
    }

    /**
     * GET /pollens/export/csv
     * Exports all pollens to a CSV file.
     */
    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"pollens_export.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Name,Shape,Size (μm),Surface Characteristic");
            
            Page<PollenDTO> pollens = pollenService.getAllPollens(Pageable.unpaged());
            for (PollenDTO pollen : pollens.getContent()) {
                writer.printf("%d,\"%s\",\"%s\",%.2f,\"%s\"%n",
                        pollen.getId(),
                        escapeCsv(pollen.getName()),
                        escapeCsv(pollen.getShape()),
                        pollen.getSizeMicron() != null ? pollen.getSizeMicron() : 0.0,
                        escapeCsv(pollen.getSurfaceCharacteristic())
                );
            }
        }
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        return data.replace("\"", "\"\"");
    }
}
