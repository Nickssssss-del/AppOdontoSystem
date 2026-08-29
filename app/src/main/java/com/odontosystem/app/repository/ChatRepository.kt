package com.odontosystem.app.repository

import com.odontosystem.app.data.model.ChatRequest
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val apiService: ApiService) {

    suspend fun sendMessage(messageText: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendChatMessage(ChatRequest(messageText))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.reply)
            } else {
                Result.success(getSmartBotFallback(messageText))
            }
        } catch (e: Exception) {
            Result.success(getSmartBotFallback(messageText))
        }
    }

    private fun getSmartBotFallback(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("cita") || lower.contains("agendar") || lower.contains("reserva") ->
                "Puedes agendar una cita directamente seleccionando un especialista de la lista por distrito (Miraflores, San Isidro, Surco, San Borja) y presionando 'Reservar Cita'."

            lower.contains("distrito") || lower.contains("donde") || lower.contains("ubicacion") || lower.contains("ubicar") ->
                "Contamos con clínicas y odontólogos asociados en Miraflores, San Isidro, Surco y San Borja. Utiliza el filtro por distrito en la pantalla principal para encontrarlos rápidamente."

            lower.contains("precio") || lower.contains("costo") || lower.contains("tarifa") ->
                "Las consultas odontológicas van desde S/ 120 hasta S/ 220 según la especialidad (Ortodoncia, Endodoncia, Cirugía). En la app puedes ver la tarifa exacta de cada especialista."

            lower.contains("horario") || lower.contains("atencion") ->
                "Nuestra atención en clínicas es de Lunes a Sábado de 8:00 AM a 8:00 PM. Como asistente virtual de OdontoSystem, ¡estoy disponible para atenderte 24/7!"

            else ->
                "Entendido. En OdontoSystem te conectamos con los mejores cirujanos dentistas por distrito. ¿Deseas buscar un especialista por especialidad u obtener información sobre precios?"
        }
    }
}
