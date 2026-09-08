package com.odontosystem.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Pago (modo sandbox, RF-08). Cada cita admite como máximo
 * un pago (UNIQUE(appointment_id)). No se integra ningún proveedor
 * real: el "cobro" se simula y siempre queda registrado con su
 * resultado (PAID/FAILED) para fines de auditoría y demo.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String method = "SANDBOX_CARD";

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
