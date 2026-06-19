package com.beepollen.controller;

import com.beepollen.dto.PlantDTO;
import com.beepollen.dto.PlantRequest;
import com.beepollen.entity.Pollen;
import com.beepollen.repository.PollenRepository;
import com.beepollen.service.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

/**
 * Thymeleaf MVC controller for Plant management views.
 * Provides server-rendered HTML pages under /plants.
 */
@Controller
@RequestMapping("/plants")
@RequiredArgsConstructor
@Slf4j
public class PlantWebController {

    private final PlantService plantService;
    private final PollenRepository pollenRepository;

    /**
     * GET /plants
     * Displays the list of all plants, with optional keyword search.
     *
     * @param keyword optional search keyword
     * @param model   the Spring MVC model
     * @return the plants/list view
     */
    @GetMapping
    public String listPlants(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Model model) {
        log.debug("WEB: Listing plants with keyword: {}", keyword);

        Page<PlantDTO> pageData;
        if (keyword != null && !keyword.isBlank()) {
            pageData = plantService.searchPlants(keyword, pageable);
        } else {
            pageData = plantService.getAllPlants(pageable);
        }

        model.addAttribute("plants", pageData.getContent());
        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "plants");
        return "plants/list";
    }

    /**
     * GET /plants/new
     * Displays the form for creating a new plant.
     *
     * @param model the Spring MVC model
     * @return the plants/form view
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.debug("WEB: Showing create plant form");
        model.addAttribute("plant", new PlantRequest());
        model.addAttribute("allPollens", pollenRepository.findAll());
        model.addAttribute("activeMenu", "plants");
        return "plants/form";
    }

    /**
     * POST /plants
     * Handles the form submission for creating a new plant.
     *
     * @param request            the plant creation request
     * @param bindingResult      validation results
     * @param redirectAttributes for flash messages
     * @param model              the Spring MVC model
     * @return redirect to /plants on success, or plants/form on validation errors
     */
    @PostMapping
    public String createPlant(
            @Valid @ModelAttribute("plant") PlantRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        log.debug("WEB: Creating plant with scientific name: {}", request.getScientificName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("allPollens", pollenRepository.findAll());
            return "plants/form";
        }

        plantService.createPlant(request);
        redirectAttributes.addFlashAttribute("successMessage", "Plant created successfully!");
        return "redirect:/plants";
    }

    /**
     * GET /plants/{id}/edit
     * Displays the form for editing an existing plant.
     *
     * @param id    the plant ID
     * @param model the Spring MVC model
     * @return the plants/form view pre-populated with plant data
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("WEB: Showing edit form for plant id: {}", id);

        PlantDTO plantDTO = plantService.getPlantById(id);

        // Populate PlantRequest from the existing PlantDTO
        PlantRequest request = new PlantRequest();
        request.setCommonName(plantDTO.getCommonName());
        request.setScientificName(plantDTO.getScientificName());
        request.setFamily(plantDTO.getFamily());
        request.setGenus(plantDTO.getGenus());
        request.setFloweringSeason(plantDTO.getFloweringSeason());
        request.setRegion(plantDTO.getRegion());
        request.setDescription(plantDTO.getDescription());
        request.setImageUrl(plantDTO.getImageUrl());

        // Extract pollen IDs from the associated pollens
        if (plantDTO.getPollens() != null) {
            List<Long> pollenIds = plantDTO.getPollens()
                    .stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toList());
            request.setPollenIds(pollenIds);
        }

        model.addAttribute("plant", request);
        model.addAttribute("plantId", id);
        model.addAttribute("allPollens", pollenRepository.findAll());
        model.addAttribute("activeMenu", "plants");
        return "plants/form";
    }

    /**
     * POST /plants/{id}/edit
     * Handles the form submission for updating an existing plant.
     *
     * @param id                 the plant ID
     * @param request            the plant update request
     * @param bindingResult      validation results
     * @param redirectAttributes for flash messages
     * @param model              the Spring MVC model
     * @return redirect to /plants on success, or plants/form on validation errors
     */
    @PostMapping("/{id}/edit")
    public String updatePlant(
            @PathVariable Long id,
            @Valid @ModelAttribute("plant") PlantRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        log.debug("WEB: Updating plant with id: {}", id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("plantId", id);
            model.addAttribute("allPollens", pollenRepository.findAll());
            return "plants/form";
        }

        plantService.updatePlant(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Plant updated successfully!");
        return "redirect:/plants";
    }

    /**
     * GET /plants/{id}
     * Displays the detail view for a specific plant.
     *
     * @param id    the plant ID
     * @param model the Spring MVC model
     * @return the plants/detail view
     */
    @GetMapping("/{id}")
    public String viewPlant(@PathVariable Long id, Model model) {
        log.debug("WEB: Viewing plant with id: {}", id);

        PlantDTO plant = plantService.getPlantById(id);
        model.addAttribute("plant", plant);
        model.addAttribute("pollens", plant.getPollens());
        model.addAttribute("activeMenu", "plants");
        return "plants/detail";
    }

    /**
     * GET /plants/{id}/delete
     * Deletes a plant and redirects to the plant list.
     *
     * @param id                 the plant ID
     * @param redirectAttributes for flash messages
     * @return redirect to /plants
     */
    @GetMapping("/{id}/delete")
    public String deletePlant(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        log.debug("WEB: Deleting plant with id: {}", id);
        plantService.deletePlant(id);
        redirectAttributes.addFlashAttribute("successMessage", "Plant deleted successfully!");
        return "redirect:/plants";
    }

    /**
     * GET /plants/export/csv
     * Exports all plants to a CSV file.
     */
    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        log.debug("WEB: Exporting plants to CSV");
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"plants_export.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Common Name,Scientific Name,Description,Linked Pollens Count");
            
            Page<PlantDTO> plants = plantService.getAllPlants(Pageable.unpaged());
            for (PlantDTO plant : plants.getContent()) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",%d%n",
                        plant.getId(),
                        escapeCsv(plant.getCommonName()),
                        escapeCsv(plant.getScientificName()),
                        escapeCsv(plant.getDescription()),
                        plant.getPollens() != null ? plant.getPollens().size() : 0
                );
            }
        }
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        return data.replace("\"", "\"\"");
    }
}
