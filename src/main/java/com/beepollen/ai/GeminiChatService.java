package com.beepollen.ai;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiChatService {

    private final GeminiClient geminiClient;
    private static final int MAX_MESSAGES_PER_SESSION = 30;
    private static final int MAX_CONTEXT_MESSAGES = 10;
    
    private static final String SYSTEM_PROMPT = "Bạn là trợ lý AI chuyên gia về nuôi ong, thực vật, phấn hoa, mùa hoa, và sức khoẻ bầy ong của Hệ thống Bee Pollen Management. " +
            "Hãy trả lời bằng tiếng Việt một cách thân thiện, ngắn gọn và chính xác. " +
            "Nếu người dùng hỏi các chủ đề ngoài phạm vi này, hãy lịch sự từ chối và cho biết bạn chỉ có thể hỗ trợ các chủ đề liên quan đến hệ sinh thái nuôi ong.";

    public String sendMessage(HttpSession session, String userMessage) {
        Integer messageCount = (Integer) session.getAttribute("messageCount");
        if (messageCount == null) {
            messageCount = 0;
        }

        if (messageCount >= MAX_MESSAGES_PER_SESSION) {
            return "Bạn đã đạt giới hạn tin nhắn cho phiên này, vui lòng tải lại trang để tiếp tục.";
        }

        List<Map<String, Object>> chatHistory = (List<Map<String, Object>>) session.getAttribute("chatHistory");
        if (chatHistory == null) {
            chatHistory = new ArrayList<>();
        }

        // Add user message to history
        chatHistory.add(createMessage("user", userMessage));
        messageCount++;
        session.setAttribute("messageCount", messageCount);

        // Keep only the last MAX_CONTEXT_MESSAGES
        List<Map<String, Object>> contextHistory = new ArrayList<>();
        int startIndex = Math.max(0, chatHistory.size() - MAX_CONTEXT_MESSAGES);
        for (int i = startIndex; i < chatHistory.size(); i++) {
            contextHistory.add(chatHistory.get(i));
        }

        // Build Payload
        Map<String, Object> payload = new HashMap<>();
        
        // System Instruction (top-level)
        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", List.of(Map.of("text", SYSTEM_PROMPT)));
        payload.put("systemInstruction", systemInstruction);

        // Contents
        payload.put("contents", contextHistory);

        // Generation Config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxOutputTokens", 800);
        payload.put("generationConfig", generationConfig);

        // Call API
        String reply = geminiClient.generateContent(payload);

        // Add bot reply to history using role "model"
        chatHistory.add(createMessage("model", reply));
        session.setAttribute("chatHistory", chatHistory);

        return reply;
    }

    private Map<String, Object> createMessage(String role, String text) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", role);
        message.put("parts", List.of(Map.of("text", text)));
        return message;
    }
}
