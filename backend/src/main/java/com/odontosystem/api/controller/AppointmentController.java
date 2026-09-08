package com.odontosystem.api.controller;

import com.odontosystem.api.dto.AppointmentDtos.AppointmentDto;
import com.odontosystem.api.dto.AppointmentDtos.CreateAppointmentRequest;
import com.odontosystem.api.dto.AppointmentDtos.RescheduleAppointmentRequest;
import com.odontosystem.api.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Lado paciente:
 *  - GET   /api/v1/appointments/my-appointments
 *  - POST  /api/v1/appointments
 *  - PATCH /api/v1/appointments/{id}/cancel
 * Lado odontólogo:
 *  - GET   /api/v1/appointments/dentist/my-appointments
 *  - PATCH /api/v1/appointments/{id}/confirm
 *  - PATCH /api/v1/appointments/{id}/complete
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ---------- Lado paciente ----------

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentDto>> myAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.findMyAppointments(authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentDto> create(
            Authentication authentication,
            @Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentDto created = appointmentService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'DENTIST')")
    public ResponseEntity<AppointmentDto> cancel(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.cancel(authentication.getName(), id));
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT', 'DENTIST')")
    public ResponseEntity<AppointmentDto> reschedule(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.reschedule(authentication.getName(), id, request));
    }

    // ---------- Lado odontólogo ----------

    @GetMapping("/dentist/my-appointments")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<List<AppointmentDto>> myAppointmentsAsDentist(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.findMyAppointmentsAsDentist(authentication.getName()));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<AppointmentDto> confirm(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.confirm(authentication.getName(), id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<AppointmentDto> complete(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.complete(authentication.getName(), id));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<AppointmentDto> startAttention(Authentication authentication, @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.startAttention(authentication.getName(), id));
    }
}
