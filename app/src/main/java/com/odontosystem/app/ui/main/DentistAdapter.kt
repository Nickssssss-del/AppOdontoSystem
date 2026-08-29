package com.odontosystem.app.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.odontosystem.app.R
import com.odontosystem.app.data.local.AppointmentStore
import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.databinding.ItemDentistBinding

class DentistAdapter(
    private val onDentistClick: (Dentist) -> Unit,
    private val onBookClick: (Dentist) -> Unit,
    private val onSlotClick: (Dentist, String) -> Unit,
    private val onWhatsAppClick: (Dentist) -> Unit
) : RecyclerView.Adapter<DentistAdapter.DentistViewHolder>() {

    private var items: List<Dentist> = emptyList()

    fun submitList(newList: List<Dentist>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DentistViewHolder {
        val binding = ItemDentistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DentistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DentistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DentistViewHolder(private val binding: ItemDentistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dentist: Dentist) {
            binding.tvDentistName.text = dentist.name
            binding.tvSpecialty.text = dentist.specialty
            binding.tvDistrict.text = dentist.district
            binding.tvRating.text = "★ ${dentist.rating} (${dentist.reviewsCount})"
            binding.tvPrice.text = "S/ ${String.format("%.2f", dentist.price)} consulta"
            binding.tvInitials.text = dentist.avatarInitials
            binding.tvInitials.visibility = View.VISIBLE

            val slots = AppointmentStore.visibleSlots(dentist.id, dentist.availableSlots)
            binding.chipSlots.removeAllViews()
            if (slots.isEmpty()) {
                binding.tvSlotsHint.text = "Sin turnos de hoy (bloqueo o agenda llena). Revisa próximos días."
                binding.chipSlots.visibility = View.GONE
            } else {
                binding.tvSlotsHint.text = "Turnos libres · toca uno para reservar"
                binding.chipSlots.visibility = View.VISIBLE
                slots.forEach { slot ->
                    val chip = Chip(binding.root.context).apply {
                        text = slot
                        isCheckable = false
                        setChipBackgroundColorResource(R.color.primary_light)
                        setTextColor(binding.root.context.getColor(R.color.brand_navy))
                        setOnClickListener { onSlotClick(dentist, slot) }
                    }
                    binding.chipSlots.addView(chip)
                }
            }

            binding.root.setOnClickListener { onDentistClick(dentist) }
            binding.btnBook.setOnClickListener { onBookClick(dentist) }
            binding.btnWhatsApp.setOnClickListener { onWhatsAppClick(dentist) }
        }
    }
}
