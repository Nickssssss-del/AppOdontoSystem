package com.odontosystem.api.entity;

/**
 * Estados posibles de una cita, alineados con los valores esperados
 * por el cliente Android en `Appointment.kt` (campo `status`).
 */
public enum AppointmentStatus {
    Confirmada,
    Pendiente,
    EnAtencion,
    Cancelada,
    Completada
}