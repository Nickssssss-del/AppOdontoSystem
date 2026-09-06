package com.odontosystem.app.repository

import com.odontosystem.app.data.local.AppointmentStore
import com.odontosystem.app.data.model.Appointment
import com.odontosystem.app.data.model.CreateAppointmentRequest
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AppointmentRepository(private val apiService: ApiService) {

    suspend fun getAppointments(): Result<List<Appointment>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAppointments()
            val remote = if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
            Result.success(AppointmentStore.merge(remote))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppointment(
        dentistId: String,
        dentistName: String,
        specialty: String,
        district: String,
        date: String,
        time: String,
        reason: String?
    ): Result<Appointment> = withContext(Dispatchers.IO) {
        try {
            val req = CreateAppointmentRequest(dentistId, date, time, reason)
            val response = apiService.createAppointment(req)
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!
                AppointmentStore.add(created)
                Result.success(created)
            } else if (response.code() == 409) {
                // Concurrencia: el turno ya fue reservado (uq_appointment_slot)
                Result.failure(Exception("Ese horario ya fue reservado por otro paciente. Elige otro turno."))
            } else {
                Result.failure(Exception(errorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelAppointment(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val success = AppointmentStore.cancel(id)
        Result.success(success)
    }

    fun activateExpressBlock(dentistId: String) {
        AppointmentStore.activateExpressBlock(dentistId)
    }

    private fun errorMessage(response: Response<*>): String {
        return response.errorBody()?.string()?.let { body ->
            Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
                .find(body)
                ?.groupValues
                ?.getOrNull(1)
        } ?: "Error del servidor (${response.code()})."
    }
}