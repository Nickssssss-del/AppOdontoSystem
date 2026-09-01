package com.odontosystem.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChatDtos {

    /** Espejo exacto de `ChatRequest` en ChatMessage.kt */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        @NotBlank
        private String message;
    }

    /** Espejo exacto de `ChatResponse` en ChatMessage.kt */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private String reply;

        public ChatResponse(String reply) {
            this.reply = reply;
        }
    }
}
