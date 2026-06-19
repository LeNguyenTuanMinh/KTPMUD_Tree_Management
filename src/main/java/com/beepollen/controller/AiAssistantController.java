package com.beepollen.controller;

import com.beepollen.ai.GeminiChatService;
import com.beepollen.ai.GeminiVisionService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/ai-assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    private final GeminiChatService chatService;
    private final GeminiVisionService visionService;

    @GetMapping
    public String showAiAssistant(Model model) {
        model.addAttribute("activeMenu", "ai-assistant");
        return "ai-assistant";
    }

    @PostMapping("/chat")
    @ResponseBody
    public Map<String, String> chat(@RequestBody ChatRequest request, HttpSession session) {
        String reply = chatService.sendMessage(session, request.getMessage());
        return Map.of("reply", reply);
    }

    @PostMapping("/chat/clear")
    @ResponseBody
    public Map<String, String> clearChat(HttpSession session) {
        session.removeAttribute("chatHistory");
        session.removeAttribute("messageCount");
        return Map.of("status", "success");
    }

    @PostMapping(value = "/identify-plant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, Object> identifyPlant(@RequestParam("image") MultipartFile image, HttpSession session) {
        return visionService.identifyPlant(image, session);
    }

    @PostMapping(value = "/assess-colony", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, Object> assessColony(@RequestParam("image") MultipartFile image, HttpSession session) {
        return visionService.assessColony(image, session);
    }

    @Data
    public static class ChatRequest {
        private String message;
    }
}
