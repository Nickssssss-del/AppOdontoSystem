package com.odontosystem.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

public class AppointmentDtos {

    /** Espejo exacto de `Appointment` en Appointment.kt */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppointmentDto {
        private UUID id;
        private UUID dentistId;
        private String dentistName;
        private String specialty;
        private String district;
        private String date;   // formato yyyy-MM-dd
        private String time;
        private String reason;
        private String status; // "Confirmada" | "Pendiente" | "Cancelada"
    }

    /** Espejo exacto de `CreateAppointmentRequest` en Appointment.kt */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAppointmentRequest {
        @NotNull
        private UUID dentistId;

        @NotBlank
        private String date; // yyyy-MM-dd

        @NotBlank
        private String time;

        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RescheduleAppointmentRequest {
        @NotBlank
        private String date;

        @NotBlank
        private String time;
    }
}
