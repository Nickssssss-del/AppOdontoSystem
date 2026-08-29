package com.odontosystem.app.ui.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odontosystem.app.data.model.ChatMessage
import com.odontosystem.app.data.model.SenderType
import com.odontosystem.app.repository.ChatRepository
import kotlinx.coroutines.launch
import java.util.UUID

class ChatbotViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val currentList = mutableListOf<ChatMessage>()

    init {
        // Welcome message from the bot
        currentList.add(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "¡Hola! Soy tu asistente virtual de OdontoSystem 🤖. ¿En qué puedo ayudarte hoy? Puedo asistirte en la búsqueda de especialistas por distrito, consulta de tarifas o asesoramiento para agendar citas.",
                sender = SenderType.BOT
            )
        )
        _messages.value = currentList.toList()
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText,
            sender = SenderType.USER
        )
        currentList.add(userMsg)
        _messages.value = currentList.toList()

        viewModelScope.launch {
            val result = chatRepository.sendMessage(userText)
            result.onSuccess { reply ->
                val botMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = reply,
                    sender = SenderType.BOT
                )
                currentList.add(botMsg)
                _messages.value = currentList.toList()
            }
        }
    }
}
