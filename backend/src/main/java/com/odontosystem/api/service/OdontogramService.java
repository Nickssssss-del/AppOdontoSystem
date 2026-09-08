package com.odontosystem.api.service;

import com.odontosystem.api.dto.OdontogramDtos.ToothDto;
import com.odontosystem.api.dto.OdontogramDtos.UpsertToothRequest;
import com.odontosystem.api.entity.Dentist;
import com.odontosystem.api.entity.OdontogramTooth;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.DentistRepository;
import com.odontosystem.api.repository.OdontogramToothRepository;
import com.odontosystem.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Odontograma digital (RF-05). Módulo exclusivo para odontólogos.
 * Cada pieza (1-32) se actualiza de forma independiente; una pieza
 * sin fila registrada se considera SANO por convención del cliente.
 */
@Service
@RequiredArgsConstructor
public class OdontogramService {

    private final OdontogramToothRepository odontogramToothRepository;
    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;

    @Transactional
    public ToothDto upsertTooth(String dentistEmail, UUID patientId, short toothNumber, UpsertToothRequest request) {
        Dentist dentist = resolveDentistProfile(dentistEmail);
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> ApiException.notFound("Paciente no encontrado"));

        OdontogramTooth tooth = odontogramToothRepository.findByPatientIdAndToothNumber(patientId, toothNumber)
                .orElse(OdontogramTooth.builder()
                        .patient(patient)
                        .toothNumber(toothNumber)
                        .build());

        tooth.setCondition(request.getCondition());
        tooth.setNotes(request.getNotes());
        tooth.setUpdatedByDentist(dentist);
        tooth.setUpdatedAt(LocalDateTime.now());

        odontogramToothRepository.save(tooth);
        return toDto(tooth);
    }

    @Transactional(readOnly = true)
    public List<ToothDto> findByPatient(String dentistEmail, UUID patientId) {
        resolveDentistProfile(dentistEmail);

        if (!userRepository.existsById(patientId)) {
            throw ApiException.notFound("Paciente no encontrado");
        }

        return odontogramToothRepository.findByPatientIdOrderByToothNumberAsc(patientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Dentist resolveDentistProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        return dentistRepository.findByUserId(user.getId())
                .orElseThrow(() -> ApiException.notFound(
                        "Este módulo es exclusivo para odontólogos. Tu cuenta no está vinculada a un perfil de odontólogo."));
    }

    private ToothDto toDto(OdontogramTooth t) {
        return ToothDto.builder()
                .toothNumber(t.getToothNumber().intValue())
                .condition(t.getCondition())
                .notes(t.getNotes())
                .updatedByDentistName(t.getUpdatedByDentist() != null ? t.getUpdatedByDentist().getName() : null)
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
