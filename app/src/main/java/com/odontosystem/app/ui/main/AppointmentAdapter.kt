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

            val canCancel = appointment.status.equals("Confirmada", ignoreCase = true) && onCancelClick != null
            binding.btnCancelAppointment.visibility = if (canCancel) View.VISIBLE else View.GONE
            binding.btnCancelAppointment.setOnClickListener { onCancelClick?.invoke(appointment) }
        }
    }
}
