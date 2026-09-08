package com.odontosystem.api.controller;

import com.odontosystem.api.dto.PaymentDtos.CreatePaymentRequest;
import com.odontosystem.api.dto.PaymentDtos.PaymentDto;
import com.odontosystem.api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Rutas protegidas (requieren JWT):
 *  - POST /api/v1/payments
 *  - GET  /api/v1/payments/appointment/{appointmentId}
 * Corresponden a RF-08 (Pagos en Línea, modo Sandbox).
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDto> create(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentDto created = paymentService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<PaymentDto> byAppointment(
            Authentication authentication,
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(paymentService.findByAppointment(authentication.getName(), appointmentId));
    }
}
