package com.odontosystem.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentDtos {

    /** Body de POST /api/v1/payments. Datos de tarjeta ficticios (sandbox). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePaymentRequest {
        @NotNull
        private UUID appointmentId;

        @NotNull
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        private BigDecimal amount;

        /** Número de tarjeta simulado, 16 dígitos. Solo se usa para decidir PAID/FAILED en el sandbox. */
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe tener 16 dígitos")
        private String cardNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentDto {
        private UUID id;
        private UUID appointmentId;
        private BigDecimal amount;
        private String status;
        private String method;
        private String reference;
        private LocalDateTime createdAt;
    }
}
