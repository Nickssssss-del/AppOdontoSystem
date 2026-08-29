package com.odontosystem.app.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.odontosystem.app.OdontoApplication
import com.odontosystem.app.data.model.Appointment
import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.data.remote.RetrofitClient
import com.odontosystem.app.databinding.ActivityMainBinding
import com.odontosystem.app.repository.AppointmentRepository
import com.odontosystem.app.repository.DentistRepository
import com.odontosystem.app.ui.chatbot.ChatbotBottomSheetDialogFragment
import com.odontosystem.app.ui.dentist.BookAppointmentDialogFragment
import com.odontosystem.app.ui.dentist.DentistDetailActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var dentistAdapter: DentistAdapter
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var currentTab = 0
    private var nextAppointment: Appointment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = (application as OdontoApplication).sessionManager
        val dentistRepo = DentistRepository(RetrofitClient.apiService)
        val appointmentRepo = AppointmentRepository(RetrofitClient.apiService)
        viewModel = MainViewModel(dentistRepo, appointmentRepo)

        val userName = sessionManager.fetchUserName() ?: "Paciente"
        binding.tvUserWelcome.text = "Hola, $userName"

        setupToolbar(sessionManager)
        setupRecyclerViews()
        setupSpinner()
        setupTabs()
        setupListeners()
        observeViewModel()

        viewModel.loadDentists()
        viewModel.loadAppointments()
    }

    private fun setupToolbar(sessionManager: com.odontosystem.app.data.local.SessionManager) {
        binding.toolbar.inflateMenu(com.odontosystem.app.R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == com.odontosystem.app.R.id.action_logout) {
                sessionManager.clearSession()
                startActivity(Intent(this, com.odontosystem.app.ui.auth.LoginActivity::class.java))
                finish()
                true
            } else false
        }
    }

    private fun setupRecyclerViews() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        dentistAdapter = DentistAdapter(
            onDentistClick = { dentist -> openDentistDetail(dentist) },
            onBookClick = { dentist -> openBookAppointmentDialog(dentist, null) },
            onSlotClick = { dentist, slot -> openBookAppointmentDialog(dentist, slot) },
            onWhatsAppClick = { dentist -> openWhatsApp(dentist) }
        )
        appointmentAdapter = AppointmentAdapter { appointment ->
            confirmCancel(appointment)
        }
        binding.recyclerView.adapter = dentistAdapter
    }

    private fun setupSpinner() {
        val districts = listOf(
            "Todos los distritos",
            "Ica",
            "Parcona",
            "Los Aquijes",
            "La Tinguiña",
            "Subtanjalla"
        )
        binding.spinnerDistrict.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, districts)
        binding.spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setDistrictFilter(districts[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                if (currentTab == 0) {
                    binding.spinnerDistrict.visibility = View.VISIBLE
                    binding.cardNextAppointment.visibility =
                        if (nextAppointment != null) View.VISIBLE else View.GONE
                    binding.recyclerView.adapter = dentistAdapter
                    viewModel.loadDentists()
                } else {
                    binding.spinnerDistrict.visibility = View.GONE
                    binding.cardNextAppointment.visibility = View.GONE
                    binding.recyclerView.adapter = appointmentAdapter
                    viewModel.loadAppointments()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            if (currentTab == 0) viewModel.loadDentists() else viewModel.loadAppointments()
            viewModel.loadAppointments()
        }
        binding.fabChatbot.setOnClickListener {
            ChatbotBottomSheetDialogFragment().show(supportFragmentManager, "ChatbotWidget")
        }
        binding.btnCancelNext.setOnClickListener {
            nextAppointment?.let { confirmCancel(it) }
        }
    }

    private fun observeViewModel() {
        viewModel.dentists.observe(this) { list ->
            if (currentTab == 0) {
                dentistAdapter.submitList(list)
                binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        viewModel.appointments.observe(this) { list ->
            if (currentTab == 1) {
                appointmentAdapter.submitList(list)
                binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        viewModel.nextAppointment.observe(this) { appointment ->
            nextAppointment = appointment
            if (appointment != null && currentTab == 0) {
                binding.cardNextAppointment.visibility = View.VISIBLE
                binding.tvNextDentist.text = appointment.dentistName
                binding.tvNextDateTime.text =
                    "${appointment.date} · ${appointment.time} · ${appointment.district}"
            } else if (currentTab == 0) {
                binding.cardNextAppointment.visibility = View.GONE
            }
        }
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun confirmCancel(appointment: Appointment) {
        if (!appointment.status.equals("Confirmada", ignoreCase = true)) return
        AlertDialog.Builder(this)
            .setTitle("Liberar turno")
            .setMessage("¿Cancelar la cita con ${appointment.dentistName}? El horario quedará libre para otro paciente.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                viewModel.cancelAppointment(appointment.id)
                Toast.makeText(this, "Turno liberado. Recordatorio cancelado.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Volver", null)
            .show()
    }

    private fun openDentistDetail(dentist: Dentist) {
        startActivity(Intent(this, DentistDetailActivity::class.java).apply {
            putExtra("dentist", dentist)
        })
    }

    private fun openBookAppointmentDialog(dentist: Dentist, slot: String?) {
        BookAppointmentDialogFragment.newInstance(dentist, slot) {
            viewModel.loadAppointments()
        }.show(supportFragmentManager, "BookAppointment")
    }

    private fun openWhatsApp(dentist: Dentist) {
        val message =
            "Hola ${dentist.name}, te contacto desde OdontoSystem para una consulta en ${dentist.district}."
        val url = "https://api.whatsapp.com/send?phone=${dentist.phone}&text=${Uri.encode(message)}"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAppointments()
        if (currentTab == 0) viewModel.loadDentists()
    }
}
