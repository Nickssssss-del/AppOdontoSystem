package com.odontosystem.app.ui.dentist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.databinding.DialogBookAppointmentBinding
import com.odontosystem.app.repository.AppointmentRepository
import kotlinx.coroutines.launch

class BookAppointmentDialogFragment : DialogFragment() {

    private var _binding: DialogBookAppointmentBinding? = null
    private val binding get() = _binding!!

    private var dentist: Dentist? = null
    private var onAppointmentCreated: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dentist = arguments?.getSerializable(ARG_DENTIST) as? Dentist
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogBookAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dentist?.let {
            binding.tvDentistName.text = "${it.name} (${it.specialty})"
        }

        binding.btnConfirm.setOnClickListener {
            val date = binding.etDate.text.toString().trim()
            val time = binding.etTime.text.toString().trim()
            val reason = binding.etReason.text.toString().trim()

            if (dentist == null) return@setOnClickListener

            binding.btnConfirm.isEnabled = false
            lifecycleScope.launch {
                val repository = AppointmentRepository(RetrofitClient.apiService)
                val result = repository.createAppointment(
                    dentistId = dentist!!.id,
                    dentistName = dentist!!.name,
                    specialty = dentist!!.specialty,
                    district = dentist!!.district,
                    date = date,
                    time = time,
                    reason = reason
                )

                result.onSuccess {
                    Toast.makeText(requireContext(), "¡Cita agendada correctamente!", Toast.LENGTH_SHORT).show()
                    onAppointmentCreated?.invoke()
                    dismiss()
                }.onFailure {
                    Toast.makeText(requireContext(), "Error al agendar cita", Toast.LENGTH_SHORT).show()
                    binding.btnConfirm.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_DENTIST = "arg_dentist"

        fun newInstance(dentist: Dentist, onCreated: () -> Unit): BookAppointmentDialogFragment {
            val fragment = BookAppointmentDialogFragment()
            val args = Bundle().apply {
                putSerializable(ARG_DENTIST, dentist)
            }
            fragment.arguments = args
            fragment.onAppointmentCreated = onCreated
            return fragment
        }
    }
}
