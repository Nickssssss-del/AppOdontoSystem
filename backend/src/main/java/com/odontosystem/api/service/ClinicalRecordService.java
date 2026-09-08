package com.odontosystem.api.service;

import com.odontosystem.api.dto.ClinicalRecordDtos.ClinicalRecordDto;
import com.odontosystem.api.dto.ClinicalRecordDtos.UpsertClinicalRecordRequest;
import com.odontosystem.api.entity.Appointment;
import com.odontosystem.api.entity.AppointmentStatus;
import com.odontosystem.api.entity.ClinicalRecord;
import com.odontosystem.api.entity.Dentist;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.AppointmentRepository;
import com.odontosystem.api.repository.ClinicalRecordRepository;
import com.odontosystem.api.repository.DentistRepository;
import com.odontosystem.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Historial clínico (RF-05). Módulo de uso EXCLUSIVO para el
 * odontólogo: toda operación exige que el usuario autenticado tenga
 * un perfil Dentist vinculado (ver V4__link_dentist_user.sql).
 *
 * Solo se puede registrar una nota sobre una cita que:
 *  - pertenece al odontólogo autenticado, y
 *  - ya está en estado Completada (la nota se escribe "tras cada
 *    consulta", según RF-05).
 */
@Service
@RequiredArgsConstructor
public class ClinicalRecordService {

    private final ClinicalRecordRepository clinicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClinicalRecordDto upsert(String dentistEmail, UpsertClinicalRecordRequest request) {
        Dentist dentist = resolveDentistProfile(dentistEmail);

        Appointment appointment = appointmentRepository.findByIdAndDentistId(request.getAppointmentId(), dentist.getId())
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.Completada) {
            throw ApiException.badRequest("Solo puedes registrar notas clínicas de citas ya completadas");
        }

        ClinicalRecord record = clinicalRecordRepository.findByAppointmentId(appointment.getId())
                .orElse(ClinicalRecord.builder()
                        .appointment(appointment)
                        .patient(appointment.getPatient())
                        .dentist(dentist)
                        .build());

        record.setNotes(request.getNotes());
        record.setUpdatedAt(LocalDateTime.now());

        clinicalRecordRepository.save(record);
        return toDto(record);
    }

    @Transactional(readOnly = true)
    public ClinicalRecordDto findByAppointment(String dentistEmail, UUID appointmentId) {
        Dentist dentist = resolveDentistProfile(dentistEmail);

        appointmentRepository.findByIdAndDentistId(appointmentId, dentist.getId())
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));

        ClinicalRecord record = clinicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> ApiException.notFound("Esta cita no tiene una nota clínica registrada"));

        return toDto(record);
    }

    /**
     * Historial completo de un paciente, sin importar qué odontólogo
     * atendió cada cita (continuidad de la atención dentro de la
     * misma clínica). Solo requiere que quien consulta sea odontólogo.
     */
    @Transactional(readOnly = true)
    public List<ClinicalRecordDto> findByPatient(String dentistEmail, UUID patientId) {
        resolveDentistProfile(dentistEmail);

        if (!userRepository.existsById(patientId)) {
            throw ApiException.notFound("Paciente no encontrado");
        }

        return clinicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Dentist resolveDentistProfile(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        return dentistRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound(
                        "Este módulo es exclusivo para odontólogos. Tu cuenta no está vinculada a un perfil de odontólogo."));
    }

    private ClinicalRecordDto toDto(ClinicalRecord r) {
        return ClinicalRecordDto.builder()
                .id(r.getId())
                .appointmentId(r.getAppointment().getId())
                .patientId(r.getPatient().getId())
                .patientName(r.getPatient().getName())
                .dentistId(r.getDentist().getId())
                .dentistName(r.getDentist().getName())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
