package com.beepollen.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String model;
    private final String apiKey;

    public GeminiClient(@Value("${gemini.api.key:}") String apiKey,
                        @Value("${gemini.model:gemini-2.5-flash}") String model,
                        RestClient.Builder restClientBuilder) {
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 5s
        requestFactory.setReadTimeout(20000);   // 20s

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
    }

    public String generateContent(Map<String, Object> payload) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API Key is missing. Returning fallback message.");
            return "Hệ thống AI hiện đang bận hoặc thiếu cấu hình (API Key). Vui lòng thử lại sau.";
        }

        try {
            String fullUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
            Map<String, Object> response = restClient.post()
                    .uri(fullUrl)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("candidates")) {
                java.util.List<Map<String, Object>> candidates = (java.util.List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null && content.containsKey("parts")) {
                        java.util.List<Map<String, Object>> parts = (java.util.List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            return "Xin lỗi, tôi không thể xử lý câu trả lời vào lúc này.";
        } catch (org.springframework.web.client.RestClientResponseException e) {
            log.warn("Gemini API HTTP Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Lỗi từ Gemini API (" + e.getStatusCode() + "): " + e.getResponseBodyAsString();
        } catch (RestClientException e) {
            log.warn("Error calling Gemini API: {}", e.getMessage());
            return "Hệ thống AI hiện đang bận hoặc gặp sự cố kết nối. Chi tiết: " + e.getMessage();
        }
    }
}
