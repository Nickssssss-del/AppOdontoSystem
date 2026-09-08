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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio del módulo de citas.
 * Lado paciente:
 *  - GET   /api/v1/appointments/my-appointments
 *  - POST  /api/v1/appointments
 *  - PATCH /api/v1/appointments/{id}/cancel
 * Lado odontólogo (requiere que el usuario esté vinculado a un
 * perfil de Dentist, ver V4__link_dentist_user.sql):
 *  - GET   /api/v1/appointments/dentist/my-appointments
 *  - PATCH /api/v1/appointments/{id}/confirm
 *  - PATCH /api/v1/appointments/{id}/complete
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;

    // ---------- Lado paciente ----------

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

        LocalDate date;
        try {
            date = LocalDate.parse(request.getDate());
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest(
                    "Formato de fecha inválido. Usa el formato AAAA-MM-DD, por ejemplo: 2026-09-15.");
        }

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

        try {
            appointmentRepository.save(appointment);
            appointmentRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("Ese horario ya fue reservado por otro paciente. Elige otro horario.");
        }

        return toDto(appointment);
    }

    @Transactional
    public AppointmentDto cancel(String patientEmail, UUID appointmentId) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patient.getId())
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));

        if (appointment.getStatus() == AppointmentStatus.Cancelada) {
            throw ApiException.conflict("Esta cita ya estaba cancelada");
        }
        if (appointment.getStatus() == AppointmentStatus.Completada) {
            throw ApiException.conflict("No se puede cancelar una cita ya completada");
        }

        appointment.setStatus(AppointmentStatus.Cancelada);
        appointmentRepository.save(appointment);

        return toDto(appointment);
    }

    // ---------- Lado odontólogo ----------

    @Transactional(readOnly = true)
    public List<AppointmentDto> findMyAppointmentsAsDentist(String dentistEmail) {
        Dentist dentist = resolveDentistProfile(dentistEmail);

        return appointmentRepository.findByDentistIdOrderByDateDescTimeDesc(dentist.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** Pendiente -> Confirmada */
    @Transactional
    public AppointmentDto confirm(String dentistEmail, UUID appointmentId) {
        Dentist dentist = resolveDentistProfile(dentistEmail);
        Appointment appointment = findOwnAppointmentOrThrow(dentist.getId(), appointmentId);

        if (appointment.getStatus() != AppointmentStatus.Pendiente) {
            throw ApiException.conflict("Solo se pueden confirmar citas en estado Pendiente");
        }

        appointment.setStatus(AppointmentStatus.Confirmada);
        appointmentRepository.save(appointment);
        return toDto(appointment);
    }

    /** Confirmada -> Completada */
    @Transactional
    public AppointmentDto complete(String dentistEmail, UUID appointmentId) {
        Dentist dentist = resolveDentistProfile(dentistEmail);
        Appointment appointment = findOwnAppointmentOrThrow(dentist.getId(), appointmentId);

        if (appointment.getStatus() != AppointmentStatus.Confirmada) {
            throw ApiException.conflict("Solo se pueden completar citas que ya estén Confirmadas");
        }

        appointment.setStatus(AppointmentStatus.Completada);
        appointmentRepository.save(appointment);
        return toDto(appointment);
    }

    private Dentist resolveDentistProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        return dentistRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound(
                        "Tu cuenta todavía no está vinculada a un perfil de odontólogo"));
    }

    private Appointment findOwnAppointmentOrThrow(UUID dentistId, UUID appointmentId) {
        return appointmentRepository.findByIdAndDentistId(appointmentId, dentistId)
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));
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
