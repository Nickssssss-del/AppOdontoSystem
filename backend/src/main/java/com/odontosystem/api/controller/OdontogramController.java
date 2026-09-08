package com.odontosystem.api.controller;

import com.odontosystem.api.dto.OdontogramDtos.ToothDto;
import com.odontosystem.api.dto.OdontogramDtos.UpsertToothRequest;
import com.odontosystem.api.service.OdontogramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Rutas protegidas, exclusivas para odontólogos:
 *  - PUT /api/v1/odontogram/{patientId}/tooth/{toothNumber}
 *  - GET /api/v1/odontogram/{patientId}
 * Corresponden a RF-05 (Odontograma Digital).
 */
@RestController
@RequestMapping("/api/v1/odontogram")
@PreAuthorize("hasRole('DENTIST')")
@RequiredArgsConstructor
public class OdontogramController {

    private final OdontogramService odontogramService;

    @PutMapping("/{patientId}/tooth/{toothNumber}")
    public ResponseEntity<ToothDto> upsertTooth(
            Authentication authentication,
            @PathVariable UUID patientId,
            @PathVariable short toothNumber,
            @Valid @RequestBody UpsertToothRequest request) {
        return ResponseEntity.ok(
                odontogramService.upsertTooth(authentication.getName(), patientId, toothNumber, request));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<ToothDto>> byPatient(
            Authentication authentication,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(odontogramService.findByPatient(authentication.getName(), patientId));
    }
}
