package com.odontosystem.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Estado de una pieza dental dentro del odontograma digital de un
 * paciente (RF-05). Numeración simplificada 1-32. Si una pieza nunca
 * fue registrada, no existe fila para ella (se asume SANO).
 */
@Entity
@Table(name = "odontogram_teeth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdontogramTooth {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(name = "tooth_number", nullable = false)
    private Short toothNumber;

    /** SANO, CARIES, OBTURADO, EXTRAIDO, CORONA, ENDODONCIA, IMPLANTE */
    @Column(nullable = false, length = 20)
    private String condition;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_dentist_id")
    private Dentist updatedByDentist;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
