package com.odontosystem.app.ui.dentist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.odontosystem.app.OdontoApplication
import com.odontosystem.app.data.local.AppointmentStore
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.databinding.ActivityDentistDashboardBinding
import com.odontosystem.app.repository.AppointmentRepository
import com.odontosystem.app.ui.auth.LoginActivity
import com.odontosystem.app.ui.main.AppointmentAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DentistDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDentistDashboardBinding
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var dentistId: String = "dnt_1"
    private var dentistName: String = "Dr. Roberto Ramos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDentistDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = (application as OdontoApplication).sessionManager
        dentistId = sessionManager.fetchUserId() ?: "dnt_1"
        dentistName = sessionManager.fetchUserName() ?: "Dr. Roberto Ramos"
        binding.tvDentistWelcome.text = "Bienvenido, $dentistName"

        setupToolbar(sessionManager)
        setupRecyclerView()
        setupListeners()
        setupFrequencySpinner()
        refreshBlockButton()
        loadAgenda()
    }

    private fun setupFrequencySpinner() {
        val frequencies = listOf("Diaria", "Semanal")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)
        binding.spinnerFrequency.adapter = adapter
        binding.spinnerFrequency.setSelection(1) // Semanal por defecto
        
        binding.spinnerFrequency.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                Toast.makeText(this@DentistDashboardActivity, "Frecuencia cambiada a: ${frequencies[position]}", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupToolbar(sessionManager: com.odontosystem.app.data.local.SessionManager) {
        binding.toolbar.inflateMenu(com.odontosystem.app.R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == com.odontosystem.app.R.id.action_logout) {
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            } else false
        }
    }

    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter()
        binding.rvDentistAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvDentistAppointments.adapter = appointmentAdapter
    }

    private fun setupListeners() {
        binding.btnEmergencyBlock.setOnClickListener {
            if (AppointmentStore.isExpressBlocked(dentistId)) {
                AppointmentStore.clearExpressBlock()
                refreshBlockButton()
                loadAgenda()
                Toast.makeText(this, "Bloqueo Express desactivado. La agenda de hoy vuelve a publicarse.", Toast.LENGTH_LONG).show()
            } else {
                showEmergencyBlockConfirmation()
            }
        }
        
        binding.switchAutoConfirm.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) "Automática" else "Manual"
            Toast.makeText(this, "Confirmación de citas cambiada a: $mode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEmergencyBlockConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Bloqueo Express")
            .setMessage("Se pausarán solo los turnos de hoy. Los pacientes con cita serán notificados y esos horarios desaparecerán del buscador. La agenda de las próximas semanas no cambia.")
            .setPositiveButton("Pausar turnos de hoy") { _, _ ->
                executeEmergencyBlock()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun executeEmergencyBlock() {
        AppointmentRepository(RetrofitClient.apiService).activateExpressBlock(dentistId)
        refreshBlockButton()
        loadAgenda()
        Toast.makeText(this, "Bloqueo Express activo. Pacientes notificados y turnos de hoy retirados.", Toast.LENGTH_LONG).show()
    }

    private fun refreshBlockButton() {
        val blocked = AppointmentStore.isExpressBlocked(dentistId)
        binding.btnEmergencyBlock.text = if (blocked) {
            "Desactivar Bloqueo Express"
        } else {
            "Activar Bloqueo Express de Hoy"
        }
    }

    private fun loadAgenda() {
        CoroutineScope(Dispatchers.Main).launch {
            val repository = AppointmentRepository(RetrofitClient.apiService)
            val result = withContext(Dispatchers.IO) { repository.getAppointments() }
            result.onSuccess { list ->
                val mine = list.filter {
                    it.dentistId == dentistId || it.dentistName.equals(dentistName, ignoreCase = true)
                }
                appointmentAdapter.submitList(mine)
                val blocked = AppointmentStore.isExpressBlocked(dentistId)
                if (mine.isEmpty()) {
                    binding.tvEmptyDentistAgenda.visibility = View.VISIBLE
                    binding.tvEmptyDentistAgenda.text =
                        if (blocked) {
                            "Bloqueo Express activo: los turnos de hoy están pausados y no se muestran a los pacientes."
                        } else {
                            "No tienes citas pendientes para el día de hoy."
                        }
                } else {
                    binding.tvEmptyDentistAgenda.visibility = if (blocked) View.VISIBLE else View.GONE
                    if (blocked) {
                        binding.tvEmptyDentistAgenda.text =
                            "Bloqueo Express activo. Las citas de hoy figuran como pausadas; el horario base de próximas semanas se mantiene."
                    }
                }
            }
        }
    }
}
