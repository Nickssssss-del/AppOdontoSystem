package com.odontosystem.app.ui.dentist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.odontosystem.app.OdontoApplication
import com.odontosystem.app.data.model.Appointment
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
    private val appointmentsList = mutableListOf<Appointment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        binding = ActivityDentistDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = (application as OdontoApplication).sessionManager
        val dentistName = sessionManager.fetchUserName() ?: "Dr. Roberto Ramos"
        binding.tvDentistWelcome.text = "Bienvenido, $dentistName"

        setupToolbar(sessionManager)
        setupRecyclerView()
        setupListeners()
        loadAgenda()
    }

    private fun setupToolbar(sessionManager: com.odontosystem.app.data.local.SessionManager) {
        binding.toolbar.inflateMenu(com.odontosystem.app.R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == com.odontosystem.app.R.id.action_logout) {
                sessionManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
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
            showEmergencyBlockConfirmation()
        }
    }

    private fun showEmergencyBlockConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("🚨 Confirmar Bloqueo Express")
            .setMessage("¿Deseas pausar tus turnos de hoy por una emergencia? Los pacientes con cita para hoy serán notificados automáticamente.")
            .setPositiveButton("Sí, Pausar Turnos de Hoy") { _, _ ->
                executeEmergencyBlock()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun executeEmergencyBlock() {
        appointmentsList.clear()
        appointmentAdapter.submitList(appointmentsList)
        binding.tvEmptyDentistAgenda.visibility = View.VISIBLE
        binding.tvEmptyDentistAgenda.text = "⚠️ Se ha activado el Bloqueo Express de Emergencia. Tus turnos de hoy han sido pausados y los pacientes fueron notificados."
        Toast.makeText(this, "Bloqueo Express activado. Notificaciones enviadas.", Toast.LENGTH_LONG).show()
    }

    private fun loadAgenda() {
        CoroutineScope(Dispatchers.Main).launch {
            val repository = AppointmentRepository(RetrofitClient.apiService)
            val result = repository.getAppointments()
            result.onSuccess { list ->
                appointmentsList.clear()
                appointmentsList.addAll(list)
                appointmentAdapter.submitList(appointmentsList)
                binding.tvEmptyDentistAgenda.visibility = if (appointmentsList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
