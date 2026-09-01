package com.odontosystem.api.controller;

import com.odontosystem.api.dto.ChatDtos.ChatRequest;
import com.odontosystem.api.dto.ChatDtos.ChatResponse;
import com.odontosystem.api.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * POST /api/v1/chatbot/message
 * Ruta protegida (requiere JWT). Consumida por el widget de chat en el cliente Android.
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        String reply = chatbotService.processMessage(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
