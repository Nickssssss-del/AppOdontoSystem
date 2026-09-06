package com.odontosystem.app.data.model

data class Appointment(
    val id: String,
    val dentistId: String,
    val dentistName: String,
    val specialty: String,
    val district: String,
    val date: String,
    val time: String,
    val reason: String?,
    val status: String = "Pendiente" // Backend: "Pendiente", "Confirmada", "Cancelada" (RF-04: + "Completada")
)

data class CreateAppointmentRequest(
    val dentistId: String,
    val date: String,
    val time: String,
    val reason: String?
)
