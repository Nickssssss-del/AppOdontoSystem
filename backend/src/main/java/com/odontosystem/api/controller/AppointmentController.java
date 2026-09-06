package com.odontosystem.api.controller;

import com.odontosystem.api.dto.AppointmentDtos.AppointmentDto;
import com.odontosystem.api.dto.AppointmentDtos.CreateAppointmentRequest;
import com.odontosystem.api.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Rutas protegidas (requieren JWT):
 *  - GET   /api/v1/appointments/my-appointments
 *  - POST  /api/v1/appointments
 *  - PATCH /api/v1/appointments/{id}/cancel
 * Consumidas por AppointmentListViewModel / BookAppointmentViewModel en el cliente Android.
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentDto>> myAppointments(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(appointmentService.findMyAppointments(email));
    }

    @PostMapping
    public ResponseEntity<AppointmentDto> create(
            Authentication authentication,
            @Valid @RequestBody CreateAppointmentRequest request) {
        String email = authentication.getName();
        AppointmentDto created = appointmentService.create(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDto> cancel(
            Authentication authentication,
            @PathVariable UUID id) {
        String email = authentication.getName();
        AppointmentDto cancelled = appointmentService.cancel(email, id);
        return ResponseEntity.ok(cancelled);
    }
}