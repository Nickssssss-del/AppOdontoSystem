package com.odontosystem.app.data.local

import com.odontosystem.app.data.model.Appointment
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Estado en memoria para la demo: citas creadas, cancelaciones y Bloqueo Express (RF05, RF06, RF10).
 */
object AppointmentStore {

    @Volatile
    var blockedDentistId: String? = null

    private val created = CopyOnWriteArrayList<Appointment>()
    private val cancelledIds = CopyOnWriteArraySet<String>()

    fun add(appointment: Appointment) {
        created.add(0, appointment)
    }

    fun cancel(id: String) {
        cancelledIds.add(id)
    }

    fun isExpressBlocked(dentistId: String): Boolean {
        return blockedDentistId == dentistId
    }

    fun activateExpressBlock(dentistId: String) {
        blockedDentistId = dentistId
    }

    fun clearExpressBlock() {
        blockedDentistId = null
    }

    fun merge(remote: List<Appointment>): List<Appointment> {
        return (created + remote)
            .distinctBy { it.id }
            .map { apt ->
                when {
                    apt.id in cancelledIds -> apt.copy(status = "Cancelada")
                    isExpressBlocked(apt.dentistId) && isTodaySlot(apt.date) ->
                        apt.copy(status = "Pausada por emergencia")
                    else -> apt
                }
            }
    }

    fun nextConfirmed(appointments: List<Appointment>): Appointment? {
        return appointments
            .filter { it.status.equals("Confirmada", ignoreCase = true) }
            .minByOrNull { "${it.date} ${it.time}" }
    }

    fun visibleSlots(dentistId: String, slots: List<String>?): List<String> {
        val list = slots.orEmpty().filter { it.isNotBlank() }
        if (!isExpressBlocked(dentistId)) return list
        return list.filter { !it.contains("Hoy", ignoreCase = true) }
    }

    private fun isTodaySlot(date: String): Boolean {
        return date.contains("Hoy", ignoreCase = true) ||
            date.contains("2026-08-29") ||
            date.equals("hoy", ignoreCase = true)
    }
}
