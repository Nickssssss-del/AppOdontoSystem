package com.odontosystem.app.ui.dentist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.odontosystem.app.data.local.AppointmentStore
import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.databinding.DialogBookAppointmentBinding
import com.odontosystem.app.repository.AppointmentRepository
import kotlinx.coroutines.launch

class BookAppointmentDialogFragment : DialogFragment() {

    private var _binding: DialogBookAppointmentBinding? = null
    private val binding get() = _binding!!

    private var dentist: Dentist? = null
    private var selectedSlot: String? = null
    private var onAppointmentCreated: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dentist = arguments?.getSerializable(ARG_DENTIST) as? Dentist
        selectedSlot = arguments?.getString(ARG_SLOT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogBookAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentDentist = dentist ?: return
        binding.tvDentistName.text = "${currentDentist.name} · ${currentDentist.specialty}"

        if (selectedSlot.isNullOrBlank()) {
            val slots = AppointmentStore.visibleSlots(currentDentist.id, currentDentist.availableSlots)
            binding.chipDialogSlots.visibility = View.VISIBLE
            binding.tvSelectedSlot.text = "Elige un turno libre"
            binding.chipDialogSlots.removeAllViews()
            slots.forEach { slot ->
                val chip = Chip(requireContext()).apply {
                    text = slot
                    isCheckable = true
                    setOnClickListener {
                        selectedSlot = slot
                        binding.tvSelectedSlot.text = "Turno: $slot"
                    }
                }
                binding.chipDialogSlots.addView(chip)
            }
            selectedSlot = slots.firstOrNull()
            selectedSlot?.let { binding.tvSelectedSlot.text = "Turno: $it" }
        } else {
            binding.tvSelectedSlot.text = "Turno: $selectedSlot"
        }

        binding.btnConfirm.setOnClickListener {
            val slot = selectedSlot
            if (slot.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Selecciona un turno disponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val parts = parseSlot(slot)
            binding.btnConfirm.isEnabled = false
            lifecycleScope.launch {
                val repository = AppointmentRepository(RetrofitClient.apiService)
                val result = repository.createAppointment(
                    dentistId = currentDentist.id,
                    dentistName = currentDentist.name,
                    specialty = currentDentist.specialty,
                    district = currentDentist.district,
                    date = parts.first,
                    time = parts.second,
                    reason = "Reserva express"
                )
                result.onSuccess {
                    Toast.makeText(requireContext(), "Reserva confirmada: ${parts.first} · ${parts.second}", Toast.LENGTH_LONG).show()
                    onAppointmentCreated?.invoke()
                    dismiss()
                }.onFailure {
                    Toast.makeText(requireContext(), "No se pudo confirmar la reserva", Toast.LENGTH_SHORT).show()
                    binding.btnConfirm.isEnabled = true
                }
            }
        }
    }

    private fun parseSlot(slot: String): Pair<String, String> {
        val pieces = slot.split("·").map { it.trim() }
        return if (pieces.size >= 2) pieces[0] to pieces[1] else slot to "10:30 AM"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_DENTIST = "arg_dentist"
        private const val ARG_SLOT = "arg_slot"

        fun newInstance(
            dentist: Dentist,
            slot: String? = null,
            onCreated: () -> Unit
        ): BookAppointmentDialogFragment {
            return BookAppointmentDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DENTIST, dentist)
                    putString(ARG_SLOT, slot)
                }
                onAppointmentCreated = onCreated
            }
        }
    }
}
