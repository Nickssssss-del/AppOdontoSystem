package com.odontosystem.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewDtos {

    /** Body de POST /api/v1/reviews */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewRequest {
        @NotNull
        private UUID appointmentId;

        @NotNull
        @Min(1)
        @Max(5)
        private Integer rating;

        @Size(max = 1000)
        private String comment;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDto {
        private UUID id;
        private UUID dentistId;
        private UUID appointmentId;
        private String patientName;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }
}
