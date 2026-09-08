package com.odontosystem.api.service;

import com.odontosystem.api.dto.PaymentDtos.CreatePaymentRequest;
import com.odontosystem.api.dto.PaymentDtos.PaymentDto;
import com.odontosystem.api.entity.Appointment;
import com.odontosystem.api.entity.Payment;
import com.odontosystem.api.entity.User;
import com.odontosystem.api.exception.ApiException;
import com.odontosystem.api.repository.AppointmentRepository;
import com.odontosystem.api.repository.PaymentRepository;
import com.odontosystem.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Pasarela de pagos SIMULADA (RF-08, modo sandbox). No se integra
 * ningún proveedor real: el resultado del "cobro" se decide con una
 * regla de prueba, igual que hacen las pasarelas reales en modo test
 * (p. ej. Stripe usa el número 4000000000000002 para simular un
 * rechazo). Aquí: cualquier tarjeta que termine en "0000" simula un
 * pago rechazado; el resto simula un pago aprobado.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentDto create(String patientEmail, CreatePaymentRequest request) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findByIdAndPatientId(request.getAppointmentId(), patient.getId())
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));

        if (paymentRepository.existsByAppointmentId(appointment.getId())) {
            throw ApiException.conflict("Esta cita ya tiene un pago registrado");
        }

        boolean approved = !request.getCardNumber().endsWith("0000");

        Payment payment = Payment.builder()
                .appointment(appointment)
                .amount(request.getAmount())
                .status(approved ? "PAID" : "FAILED")
                .method("SANDBOX_CARD")
                .reference(generateReference())
                .build();

        paymentRepository.save(payment);

        // Importante: NO lanzamos excepción aquí aunque el pago haya sido
        // rechazado. Si lo hiciéramos, @Transactional revertiría el save
        // de arriba y el intento fallido no quedaría registrado. En su
        // lugar devolvemos el DTO con status "FAILED" y el cliente decide
        // qué mostrar (igual que hacen las pasarelas reales).
        return toDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto findByAppointment(String patientEmail, UUID appointmentId) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));

        appointmentRepository.findByIdAndPatientId(appointmentId, patient.getId())
                .orElseThrow(() -> ApiException.notFound("Cita no encontrada"));

        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> ApiException.notFound("Esta cita no tiene pagos registrados"));

        return toDto(payment);
    }

    private String generateReference() {
        return "SANDBOX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentDto toDto(Payment p) {
        return PaymentDto.builder()
                .id(p.getId())
                .appointmentId(p.getAppointment().getId())
                .amount(p.getAmount())
                .status(p.getStatus())
                .method(p.getMethod())
                .reference(p.getReference())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
