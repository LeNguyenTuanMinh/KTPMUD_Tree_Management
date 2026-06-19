package com.beepollen.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiVisionService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private static final int MAX_CALLS_PER_SESSION = 20;

    private static final String PLANT_PROMPT = "Bạn là một chuyên gia thực vật học. Hãy nhận diện loài cây/hoa trong bức ảnh và cung cấp thông tin. TRẢ VỀ ĐỊNH DẠNG JSON THEO ĐÚNG SCHEMA SAU, KHÔNG GIẢI THÍCH THÊM: {\"commonName\": \"...\", \"scientificName\": \"...\", \"family\": \"...\", \"description\": \"...\"}";
    private static final String COLONY_PROMPT = "Bạn là một chuyên gia nuôi ong mật. Hãy đánh giá sức khoẻ bầy ong qua khung cầu trong bức ảnh. TRẢ VỀ ĐỊNH DẠNG JSON THEO ĐÚNG SCHEMA SAU, KHÔNG GIẢI THÍCH THÊM: {\"healthLevel\": \"Tốt|Trung bình|Yếu\", \"observations\": \"...\", \"recommendations\": \"...\"}";

    public Map<String, Object> identifyPlant(MultipartFile file, HttpSession session) {
        return analyzeImage(file, session, PLANT_PROMPT);
    }

    public Map<String, Object> assessColony(MultipartFile file, HttpSession session) {
        return analyzeImage(file, session, COLONY_PROMPT);
    }

    private Map<String, Object> analyzeImage(MultipartFile file, HttpSession session, String systemPrompt) {
        // Rate limiting
        Integer count = (Integer) session.getAttribute("visionCallCount");
        if (count == null) count = 0;
        if (count >= MAX_CALLS_PER_SESSION) {
            return Map.of("error", "Bạn đã đạt giới hạn phân tích ảnh cho phiên làm việc này.");
        }

        // File validation (double check in service level)
        if (file == null || file.isEmpty()) {
            return Map.of("error", "File ảnh trống.");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Map.of("error", "File vượt quá giới hạn 5MB.");
        }
        String mimeType = file.getContentType();
        if (!"image/jpeg".equals(mimeType) && !"image/png".equals(mimeType)) {
            return Map.of("error", "Định dạng file không được hỗ trợ. Chỉ nhận JPEG và PNG.");
        }

        try {
            // Read file to Base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

            // Build payload
            Map<String, Object> payload = new HashMap<>();

            // System Instruction
            payload.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));

            // Generation Config for JSON mode
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("maxOutputTokens", 1024);
            payload.put("generationConfig", generationConfig);

            // Contents
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64Image);

            Map<String, Object> imagePart = Map.of("inlineData", inlineData);
            Map<String, Object> textPart = Map.of("text", "Hãy phân tích bức ảnh này.");

            payload.put("contents", List.of(
                    Map.of(
                            "role", "user",
                            "parts", List.of(imagePart, textPart)
                    )
            ));

            // Call API
            String rawResponse = geminiClient.generateContent(payload);

            // Update session count on success API call attempt
            session.setAttribute("visionCallCount", count + 1);

            // Check if response is error message from our fallback (starts with "Hệ thống AI..." or "Lỗi từ Gemini...")
            if (rawResponse.startsWith("Hệ thống AI") || rawResponse.startsWith("Lỗi từ Gemini") || rawResponse.startsWith("Xin lỗi")) {
                return Map.of("error", rawResponse);
            }

            // Parse JSON response
            try {
                // Sometime Gemini might still wrap JSON in markdown ```json ... ``` despite instructions. 
                // We'll clean it up just in case.
                String cleanJson = rawResponse.replaceAll("^```json\\n", "").replaceAll("\\n```$", "").trim();
                Map<String, Object> result = objectMapper.readValue(cleanJson, Map.class);
                return result;
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse Gemini JSON output: {}", rawResponse);
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("parseError", true);
                fallback.put("rawText", rawResponse);
                return fallback;
            }

        } catch (IOException e) {
            log.error("Error reading uploaded file", e);
            return Map.of("error", "Không thể đọc nội dung file ảnh.");
        }
    }
}
