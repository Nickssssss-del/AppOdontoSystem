package com.odontosystem.app.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.databinding.ItemDentistBinding

class DentistAdapter(
    private val onDentistClick: (Dentist) -> Unit,
    private val onBookClick: (Dentist) -> Unit,
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

            binding.root.setOnClickListener { onDentistClick(dentist) }
            binding.btnBook.setOnClickListener { onBookClick(dentist) }
            binding.btnWhatsApp.setOnClickListener { onWhatsAppClick(dentist) }
        }
    }
}
