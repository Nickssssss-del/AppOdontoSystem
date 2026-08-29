package com.odontosystem.app.repository

import com.odontosystem.app.data.model.ChatRequest
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val apiService: ApiService) {

    private val conversationMemory = mutableListOf<String>()

    suspend fun sendMessage(messageText: String): Result<String> = withContext(Dispatchers.IO) {
        conversationMemory.add(messageText)
        val contextWindow = conversationMemory.takeLast(6).joinToString(" | ")
        try {
            val response = apiService.sendChatMessage(ChatRequest(messageText))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.reply)
            } else {
                Result.success(getSmartBotFallback(messageText, contextWindow))
            }
        } catch (e: Exception) {
            Result.success(getSmartBotFallback(messageText, contextWindow))
        }
    }

    private fun getSmartBotFallback(text: String, context: String): String {
        val lower = "$text $context".lowercase()
        val districtHint = when {
            lower.contains("parcona") -> "Parcona"
            lower.contains("aquijes") -> "Los Aquijes"
            lower.contains("tinguiña") || lower.contains("tinguina") -> "La Tinguiña"
            lower.contains("subtanjalla") -> "Subtanjalla"
            lower.contains("ica") -> "Ica"
            else -> null
        }

        return when {
            lower.contains("cita") || lower.contains("agendar") || lower.contains("reserva") -> {
                val zone = districtHint ?: "tu distrito"
                "Recuerdo que buscas disponibilidad en $zone. Reserva express: filtra el distrito, toca un turno libre y confirma. Son menos de 4 toques, sin llamadas."
            }
            lower.contains("cancel") || lower.contains("liberar") || lower.contains("no puedo") ->
                "Puedes cancelar desde la tarjeta de Próxima Cita en inicio. Al liberar el turno, queda disponible para otro paciente (RF05 / RF10)."
            lower.contains("emergencia") || lower.contains("bloqueo") ->
                "Los odontólogos pueden activar Bloqueo Express para pausar solo los turnos de hoy, sin alterar la agenda de las próximas semanas."
            lower.contains("distrito") || lower.contains("donde") || lower.contains("ubicacion") || lower.contains("ubicar") || lower.contains("mapa") ->
                "OdontoSystem geolocaliza odontólogos independientes en Ica, Parcona, Los Aquijes, La Tinguiña y Subtanjalla. Usa el filtro de distrito en la pantalla principal."
            lower.contains("precio") || lower.contains("costo") || lower.contains("tarifa") ->
                "Las consultas van desde S/ 120 hasta S/ 220 según especialidad. Cada ficha muestra la tarifa antes de confirmar la reserva."
            lower.contains("horario") || lower.contains("atencion") ->
                "Atención presencial de lunes a sábado, 8:00 AM a 8:00 PM. El asistente virtual está disponible 24/7 y conserva el contexto de esta conversación."
            else ->
                "Soy el asistente de OdontoSystem con memoria de esta sesión. Puedo ayudarte a filtrar por distrito en Ica, reservar un turno libre o cancelar tu próxima cita."
        }
    }
}
