package com.odontosystem.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class OdontogramDtos {

    /** Body de PUT /api/v1/odontogram/{patientId}/tooth/{toothNumber} */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpsertToothRequest {
        @NotBlank
        @Pattern(regexp = "SANO|CARIES|OBTURADO|EXTRAIDO|CORONA|ENDODONCIA|IMPLANTE",
                message = "Condición inválida. Valores permitidos: SANO, CARIES, OBTURADO, EXTRAIDO, CORONA, ENDODONCIA, IMPLANTE")
        private String condition;

        @Size(max = 500)
        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToothDto {
        @NotNull
        @Min(1)
        @Max(32)
        private Integer toothNumber;

        private String condition;
        private String notes;
        private String updatedByDentistName;
        private LocalDateTime updatedAt;
    }
}
