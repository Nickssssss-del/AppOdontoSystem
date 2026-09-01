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

/**
 * Rutas protegidas (requieren JWT):
 *  - GET  /api/v1/appointments/my-appointments
 *  - POST /api/v1/appointments
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
}
