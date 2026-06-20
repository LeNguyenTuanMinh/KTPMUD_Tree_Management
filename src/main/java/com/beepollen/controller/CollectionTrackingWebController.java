package com.beepollen.controller;

import com.beepollen.dto.CollectionTrackingDTO;
import com.beepollen.dto.CollectionTrackingRequest;
import com.beepollen.entity.BeeColony;
import com.beepollen.entity.Pollen;
import com.beepollen.repository.BeeColonyRepository;
import com.beepollen.repository.PollenRepository;
import com.beepollen.service.CollectionTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Thymeleaf MVC controller for Collection Tracking web pages.
 * Provides list, create, edit, detail, and delete views.
 */
@Controller
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionTrackingWebController {

    private final CollectionTrackingService collectionTrackingService;
    private final BeeColonyRepository beeColonyRepository;
    private final PollenRepository pollenRepository;

    /**
     * Displays the list of all collection tracking records.
     */
    @GetMapping
    public String listCollections(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Model model) {
        Page<CollectionTrackingDTO> pageData;
        if (keyword != null && !keyword.isBlank()) {
            pageData = collectionTrackingService.searchTrackings(keyword, pageable);
        } else {
            pageData = collectionTrackingService.getAllTrackings(pageable);
        }
        
        model.addAttribute("trackings", pageData.getContent());
        model.addAttribute("pageData", pageData);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "collections");
        return "collections/list";
    }

    /**
     * Shows the form for creating a new tracking record.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BEEKEEPER', 'RESEARCHER')")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("tracking", new CollectionTrackingRequest());
        populateFormDropdowns(model);
        model.addAttribute("activeMenu", "collections");
        return "collections/form";
    }

    /**
     * Processes the creation form submission.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BEEKEEPER', 'RESEARCHER')")
    @PostMapping
    public String createCollection(
            @Valid @ModelAttribute("tracking") CollectionTrackingRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            populateFormDropdowns(model);
            model.addAttribute("activeMenu", "collections");
            return "collections/form";
        }

        collectionTrackingService.createTracking(request);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhật ký thu hoạch thành công.");
        return "redirect:/collections";
    }

    /**
     * Shows the form for editing an existing tracking record.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BEEKEEPER', 'RESEARCHER')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        CollectionTrackingDTO tracking = collectionTrackingService.getTrackingById(id);

        CollectionTrackingRequest request = new CollectionTrackingRequest();
        request.setColonyId(tracking.getColonyId());
        request.setPollenId(tracking.getPollenId());
        request.setCollectedWeight(tracking.getCollectedWeight());
        request.setCollectionDate(tracking.getCollectionDate());
        request.setNote(tracking.getNote());

        model.addAttribute("tracking", request);
        model.addAttribute("trackingId", id);
        populateFormDropdowns(model);
        model.addAttribute("activeMenu", "collections");
        return "collections/form";
    }

    /**
     * Processes the edit form submission.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BEEKEEPER', 'RESEARCHER')")
    @PostMapping("/{id}/edit")
    public String updateCollection(
            @PathVariable Long id,
            @Valid @ModelAttribute("tracking") CollectionTrackingRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("trackingId", id);
            populateFormDropdowns(model);
            model.addAttribute("activeMenu", "collections");
            return "collections/form";
        }

        collectionTrackingService.updateTracking(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhật ký thu hoạch thành công.");
        return "redirect:/collections";
    }

    /**
     * Displays the detail view of a single tracking record.
     */
    @GetMapping("/{id}")
    public String showDetail(@PathVariable Long id, Model model) {
        CollectionTrackingDTO tracking = collectionTrackingService.getTrackingById(id);
        model.addAttribute("tracking", tracking);
        model.addAttribute("activeMenu", "collections");
        return "collections/detail";
    }

    /**
     * Deletes a tracking record and redirects to the list.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'BEEKEEPER', 'RESEARCHER')")
    @GetMapping("/{id}/delete")
    public String deleteCollection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        collectionTrackingService.deleteTracking(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xoá nhật ký thu hoạch thành công.");
        return "redirect:/collections";
    }

    /**
     * Populates model with colony and pollen lists for dropdown selectors.
     */
    private void populateFormDropdowns(Model model) {
        List<BeeColony> colonies = beeColonyRepository.findAll();
        List<Pollen> pollens = pollenRepository.findAll();
        model.addAttribute("colonies", colonies);
        model.addAttribute("pollens", pollens);
    }
}
