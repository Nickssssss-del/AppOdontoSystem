package com.odontosystem.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClinicalRecordDtos {

    /** Body de POST /api/v1/clinical-records (crear o actualizar la nota de una cita) */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpsertClinicalRecordRequest {
        @NotNull
        private UUID appointmentId;

        @NotBlank
        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClinicalRecordDto {
        private UUID id;
        private UUID appointmentId;
        private UUID patientId;
        private String patientName;
        private UUID dentistId;
        private String dentistName;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
