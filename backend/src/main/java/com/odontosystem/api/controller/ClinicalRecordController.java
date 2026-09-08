package com.odontosystem.api.controller;

import com.odontosystem.api.dto.ClinicalRecordDtos.ClinicalRecordDto;
import com.odontosystem.api.dto.ClinicalRecordDtos.UpsertClinicalRecordRequest;
import com.odontosystem.api.service.ClinicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Rutas protegidas, exclusivas para usuarios con perfil de
 * odontólogo vinculado (ver DentistService.findMyProfile):
 *  - POST /api/v1/clinical-records
 *  - GET  /api/v1/clinical-records/appointment/{appointmentId}
 *  - GET  /api/v1/clinical-records/patient/{patientId}
 * Corresponden a RF-05 (Historial Clínico Digital).
 */
@RestController
@RequestMapping("/api/v1/clinical-records")
@RequiredArgsConstructor
public class ClinicalRecordController {

    private final ClinicalRecordService clinicalRecordService;

    @PostMapping
    public ResponseEntity<ClinicalRecordDto> upsert(
            Authentication authentication,
            @Valid @RequestBody UpsertClinicalRecordRequest request) {
        return ResponseEntity.ok(clinicalRecordService.upsert(authentication.getName(), request));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ClinicalRecordDto> byAppointment(
            Authentication authentication,
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(clinicalRecordService.findByAppointment(authentication.getName(), appointmentId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ClinicalRecordDto>> byPatient(
            Authentication authentication,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(clinicalRecordService.findByPatient(authentication.getName(), patientId));
    }
}
