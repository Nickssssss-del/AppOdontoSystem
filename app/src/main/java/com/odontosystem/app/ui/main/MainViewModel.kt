package com.odontosystem.app.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odontosystem.app.data.local.AppointmentStore
import com.odontosystem.app.data.model.Appointment
import com.odontosystem.app.data.model.Dentist
import com.odontosystem.app.repository.AppointmentRepository
import com.odontosystem.app.repository.DentistRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val dentistRepository: DentistRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _dentists = MutableLiveData<List<Dentist>>()
    val dentists: LiveData<List<Dentist>> = _dentists

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    private val _nextAppointment = MutableLiveData<Appointment?>()
    val nextAppointment: LiveData<Appointment?> = _nextAppointment

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var selectedDistrict: String? = null

    fun setDistrictFilter(district: String) {
        selectedDistrict = if (district == "Todos los distritos") null else district
        loadDentists()
    }

    fun loadDentists() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = dentistRepository.getDentists(selectedDistrict)
            result.onSuccess { list ->
                _dentists.value = list
            }.onFailure {
                _dentists.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun loadAppointments() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = appointmentRepository.getAppointments()
            result.onSuccess { list ->
                _appointments.value = list
                _nextAppointment.value = AppointmentStore.nextConfirmed(list)
            }.onFailure {
                _appointments.value = emptyList()
                _nextAppointment.value = null
            }
            _isLoading.value = false
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            appointmentRepository.cancelAppointment(appointmentId)
            loadAppointments()
        }
    }
}
