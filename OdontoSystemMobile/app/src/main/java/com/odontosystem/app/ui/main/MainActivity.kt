package com.odontosystem.app.ui.main

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.odontosystem.app.OdontoApplication
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

    private var currentTab = 0 // 0: Dentists, 1: Appointments

    public override fun onCreate(savedInstanceState: android.os.Bundle?) {
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
    }

    private fun setupToolbar(sessionManager: com.odontosystem.app.data.local.SessionManager) {
        binding.toolbar.inflateMenu(com.odontosystem.app.R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == com.odontosystem.app.R.id.action_logout) {
                sessionManager.clearSession()
                val intent = Intent(this, com.odontosystem.app.ui.auth.LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            } else false
        }
    }

    private fun setupRecyclerViews() {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        dentistAdapter = DentistAdapter(
            onDentistClick = { dentist -> openDentistDetail(dentist) },
            onBookClick = { dentist -> openBookAppointmentDialog(dentist) }
        )

        appointmentAdapter = AppointmentAdapter()
        binding.recyclerView.adapter = dentistAdapter
    }

    private fun setupSpinner() {
        val districts = listOf(
            "Todos los distritos",
            "Miraflores",
            "San Isidro",
            "Surco",
            "San Borja"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, districts)
        binding.spinnerDistrict.adapter = spinnerAdapter

        binding.spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = districts[position]
                viewModel.setDistrictFilter(selected)
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
                    binding.recyclerView.adapter = dentistAdapter
                    viewModel.loadDentists()
                } else {
                    binding.spinnerDistrict.visibility = View.GONE
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
        }

        binding.fabChatbot.setOnClickListener {
            val chatbotBottomSheet = ChatbotBottomSheetDialogFragment()
            chatbotBottomSheet.show(supportFragmentManager, "ChatbotWidget")
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

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun openDentistDetail(dentist: Dentist) {
        val intent = Intent(this, DentistDetailActivity::class.java).apply {
            putExtra("dentist", dentist)
        }
        startActivity(intent)
    }

    private fun openBookAppointmentDialog(dentist: Dentist) {
        val dialog = BookAppointmentDialogFragment.newInstance(dentist) {
            viewModel.loadAppointments()
        }
        dialog.show(supportFragmentManager, "BookAppointment")
    }

    override fun onResume() {
        super.onResume()
        if (currentTab == 1) viewModel.loadAppointments()
    }
}
