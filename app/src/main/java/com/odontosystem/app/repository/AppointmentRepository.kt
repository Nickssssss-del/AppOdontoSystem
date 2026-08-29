package com.odontosystem.app.repository

import com.odontosystem.app.data.local.AppointmentStore
import com.odontosystem.app.data.model.Appointment
import com.odontosystem.app.data.model.CreateAppointmentRequest
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            Result.success(AppointmentStore.merge(emptyList()))
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
        val fallback = Appointment(
            id = "apt_" + System.currentTimeMillis(),
            dentistId = dentistId,
            dentistName = dentistName,
            specialty = specialty,
            district = district,
            date = date,
            time = time,
            reason = reason ?: "Reserva express",
            status = "Confirmada"
        )
        try {
            val req = CreateAppointmentRequest(dentistId, date, time, reason)
            val response = apiService.createAppointment(req)
            val created = if (response.isSuccessful && response.body() != null) {
                response.body()!!.copy(
                    dentistId = dentistId,
                    dentistName = dentistName,
                    specialty = specialty,
                    district = district,
                    date = date,
                    time = time,
                    reason = reason ?: "Reserva express",
                    status = "Confirmada"
                )
            } else {
                fallback
            }
            AppointmentStore.add(created)
            Result.success(created)
        } catch (e: Exception) {
            AppointmentStore.add(fallback)
            Result.success(fallback)
        }
    }

    suspend fun cancelAppointment(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        AppointmentStore.cancel(id)
        Result.success(Unit)
    }

    fun activateExpressBlock(dentistId: String) {
        AppointmentStore.activateExpressBlock(dentistId)
    }
}
