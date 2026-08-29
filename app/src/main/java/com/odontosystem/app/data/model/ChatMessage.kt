package com.odontosystem.app.data.model

enum class SenderType {
    USER, BOT
}

data class ChatMessage(
    val id: String,
    val text: String,
    val sender: SenderType,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val reply: String
)
