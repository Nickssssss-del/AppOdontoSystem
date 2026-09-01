package com.odontosystem.api.service;

import com.odontosystem.api.dto.AppointmentDtos.AppointmentDto;
import com.odontosystem.api.dto.AppointmentDtos.CreateAppointmentRequest;
import com.odontosystem.api.entity.Appointment;
import com.odontosystem.api.entity.AppointmentStatus;
import com.odontosystem.api.entity.Dentist;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.AppointmentRepository;
import com.odontosystem.api.repository.DentistRepository;
import com.odontosystem.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio del módulo de citas.
 * Corresponde a los endpoints:
 *  - GET  /api/v1/appointments/my-appointments (AppointmentListViewModel)
 *  - POST /api/v1/appointments                 (BookAppointmentViewModel)
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AppointmentDto> findMyAppointments(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        return appointmentRepository.findByPatientIdOrderByDateDescTimeDesc(patient.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AppointmentDto create(String patientEmail, CreateAppointmentRequest request) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        Dentist dentist = dentistRepository.findById(request.getDentistId())
                .orElseThrow(() -> ApiException.notFound("Odontólogo no encontrado"));

        LocalDate date = LocalDate.parse(request.getDate());
        if (date.isBefore(LocalDate.now())) {
            throw ApiException.badRequest("No es posible reservar una cita en una fecha pasada");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .dentist(dentist)
                .date(date)
                .time(request.getTime())
                .reason(request.getReason())
                .status(AppointmentStatus.Pendiente)
                .build();

        appointmentRepository.save(appointment);
        return toDto(appointment);
    }

    private AppointmentDto toDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .dentistId(a.getDentist().getId())
                .dentistName(a.getDentist().getName())
                .specialty(a.getDentist().getSpecialty())
                .district(a.getDentist().getDistrict())
                .date(a.getDate().toString())
                .time(a.getTime())
                .reason(a.getReason())
                .status(a.getStatus().name())
                .build();
    }
}
