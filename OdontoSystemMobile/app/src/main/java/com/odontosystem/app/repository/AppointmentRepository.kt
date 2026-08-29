package com.odontosystem.app.repository

import com.odontosystem.app.data.model.Appointment
import com.odontosystem.app.data.model.CreateAppointmentRequest
import com.odontosystem.app.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppointmentRepository(private val apiService: ApiService) {

    private val localAppointments = mutableListOf<Appointment>()

    suspend fun getAppointments(): Result<List<Appointment>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAppointments()
            if (response.isSuccessful && response.body() != null) {
                val remoteList = response.body()!!
                val combined = (localAppointments + remoteList).distinctBy { it.id }
                Result.success(combined)
            } else {
                Result.success(localAppointments.toList())
            }
        } catch (e: Exception) {
            Result.success(localAppointments.toList())
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
                val created = response.body()!!.copy(
                    dentistName = dentistName,
                    specialty = specialty,
                    district = district
                )
                localAppointments.add(0, created)
                Result.success(created)
            } else {
                val fallback = Appointment(
                    id = "apt_" + System.currentTimeMillis(),
                    dentistId = dentistId,
                    dentistName = dentistName,
                    specialty = specialty,
                    district = district,
                    date = date,
                    time = time,
                    reason = reason,
                    status = "Confirmada"
                )
                localAppointments.add(0, fallback)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = Appointment(
                id = "apt_" + System.currentTimeMillis(),
                dentistId = dentistId,
                dentistName = dentistName,
                specialty = specialty,
                district = district,
                date = date,
                time = time,
                reason = reason,
                status = "Confirmada"
            )
            localAppointments.add(0, fallback)
            Result.success(fallback)
        }
    }
}
