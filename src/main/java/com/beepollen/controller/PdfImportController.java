package com.beepollen.controller;

import com.beepollen.dto.PlantRequest;
import com.beepollen.dto.PollenRequest;
import com.beepollen.repository.PlantRepository;
import com.beepollen.repository.PollenRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Controller
@RequestMapping("/import")
@PreAuthorize("hasAnyRole('ADMIN','BEEKEEPER','RESEARCHER')")
@RequiredArgsConstructor
@Slf4j
public class PdfImportController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final PollenRepository pollenRepository;
    private final PlantRepository plantRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @PostMapping("/plant/preview")
    public String previewPlantImport(@RequestParam("pdfFile") MultipartFile file, 
                                     Model model, 
                                     RedirectAttributes redirectAttributes) {
        try {
            String base64Pdf = Base64.getEncoder().encodeToString(file.getBytes());
            String prompt = "Extract data from this document. Return ONLY a JSON object with these exact fields: commonName, scientificName, description. No markdown, no explanation, no code fences.";
            
            String jsonResponse = callGeminiApi(base64Pdf, prompt);
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.isArray() && root.size() > 0) {
                root = root.get(0);
            } else if (root.has("plants") && root.get("plants").isArray() && root.get("plants").size() > 0) {
                root = root.get("plants").get(0);
            } else if (root.has("plant")) {
                root = root.get("plant");
            }
            PlantRequest plantRequest = objectMapper.treeToValue(root, PlantRequest.class);
            
            model.addAttribute("plant", plantRequest);
            model.addAttribute("allPollens", pollenRepository.findAll());
            model.addAttribute("extractedFrom", "pdf");
            model.addAttribute("activeMenu", "plants");
            return "plants/import-preview";
        } catch (Exception e) {
            log.error("Error importing plant from PDF", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể trích xuất dữ liệu từ PDF: " + e.getMessage());
            return "redirect:/plants";
        }
    }

    @PostMapping("/pollen/preview")
    public String previewPollenImport(@RequestParam("pdfFile") MultipartFile file, 
                                      Model model, 
                                      RedirectAttributes redirectAttributes) {
        try {
            String base64Pdf = Base64.getEncoder().encodeToString(file.getBytes());
            String prompt = "Extract data from this document. Return ONLY a JSON object with these exact fields: name, shape, sizeMicron (number), surfaceCharacteristic, description. No markdown, no explanation, no code fences.";
            
            String jsonResponse = callGeminiApi(base64Pdf, prompt);
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.isArray() && root.size() > 0) {
                root = root.get(0);
            } else if (root.has("pollens") && root.get("pollens").isArray() && root.get("pollens").size() > 0) {
                root = root.get("pollens").get(0);
            } else if (root.has("pollen")) {
                root = root.get("pollen");
            }
            PollenRequest pollenRequest = objectMapper.treeToValue(root, PollenRequest.class);
            
            model.addAttribute("pollen", pollenRequest);
            model.addAttribute("allPlants", plantRepository.findAll());
            model.addAttribute("extractedFrom", "pdf");
            model.addAttribute("activeMenu", "pollens");
            return "pollens/import-preview";
        } catch (Exception e) {
            log.error("Error importing pollen from PDF", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể trích xuất dữ liệu từ PDF: " + e.getMessage());
            return "redirect:/pollens";
        }
    }

    private String callGeminiApi(String base64Pdf, String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

        String requestBody = "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\": [\n" +
                "      {\"inline_data\": {\"mime_type\": \"application/pdf\", \"data\": \"" + base64Pdf + "\"}},\n" +
                "      {\"text\": \"" + prompt.replace("\"", "\\\"") + "\"}\n" +
                "    ]\n" +
                "  }]\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Gemini API error: " + response.body());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        String text = rootNode.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Strip ```json fences
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }
}
