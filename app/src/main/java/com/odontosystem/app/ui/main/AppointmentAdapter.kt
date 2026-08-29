package com.odontosystem.app.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odontosystem.app.data.model.Appointment
import com.odontosystem.app.databinding.ItemAppointmentBinding

class AppointmentAdapter(
    private val onCancelClick: ((Appointment) -> Unit)? = null
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    private var items: List<Appointment> = emptyList()

    fun submitList(newList: List<Appointment>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            binding.tvDentistName.text = appointment.dentistName
            binding.tvSpecialtyDistrict.text = "${appointment.specialty} • ${appointment.district}"
            binding.tvDateTime.text = "${appointment.date} · ${appointment.time}"
            binding.tvStatus.text = appointment.status
            binding.tvReason.text = "Motivo: ${appointment.reason ?: "Consulta general"}"

            // RF04: Habilitar colores según el estado (Atendida, Cancelada, Pendiente, Pausada)
            when (appointment.status.lowercase()) {
                "confirmada" -> {
                    binding.tvStatus.setBackgroundResource(com.odontosystem.app.R.drawable.bg_status_confirmed)
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#065F46"))
                }
                "cancelada" -> {
                    binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"))
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#B91C1C"))
                }
                "atendida" -> {
                    binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#DBEAFE"))
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#1E40AF"))
                }
                "pausada por emergencia" -> {
                    binding.tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FEF3C7"))
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#92400E"))
                }
                else -> {
                    binding.tvStatus.setBackgroundColor(android.graphics.Color.LTGRAY)
                    binding.tvStatus.setTextColor(android.graphics.Color.BLACK)
                }
            }

            val canCancel = appointment.status.equals("Confirmada", ignoreCase = true) && onCancelClick != null
            binding.btnCancelAppointment.visibility = if (canCancel) View.VISIBLE else View.GONE
            binding.btnCancelAppointment.setOnClickListener { onCancelClick?.invoke(appointment) }
        }
    }
}
