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

    fun cancel(id: String): Boolean {
        if (canCancel(id)) {
            cancelledIds.add(id)
            return true
        }
        return false
    }

    /**
     * RF05: Habilita la cancelación hasta 12 horas antes con liberación automática e inmediata del turno.
     * En esta demo, asumimos que si dice "Hoy" y faltan pocas horas, se bloquea.
     * Para efectos de demo, bloqueamos si la cita es "Hoy" y el slot es antes de las 6PM (asumiendo hora actual mediodía).
     */
    fun canCancel(id: String): Boolean {
        val apt = (created + emptyList<Appointment>()).find { it.id == id } ?: return true
        if (apt.date.contains("Hoy", ignoreCase = true)) {
            // Lógica simplificada demo: No se cancela si es hoy y falta menos de 12h (simulado)
            // Si la cita es hoy, no permitimos cancelar para demostrar la restricción de 12h
            return false
        }
        return true
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
        
        // Liberación automática e inmediata (RF05):
        // Filtramos slots que ya están en 'created' pero no en 'cancelledIds'
        val reservedSlots = created
            .filter { it.dentistId == dentistId && it.id !in cancelledIds }
            .map { "${it.date} · ${it.time}" }
            
        val available = list.filter { slot -> slot !in reservedSlots }
        
        if (!isExpressBlocked(dentistId)) return available
        return available.filter { !it.contains("Hoy", ignoreCase = true) }
    }

    private fun isTodaySlot(date: String): Boolean {
        return date.contains("Hoy", ignoreCase = true) ||
            date.contains("2026-08-29") ||
            date.equals("hoy", ignoreCase = true)
    }
}
