-- ============================================================
-- OdontoSystem — V6: historial clínico y odontograma digital
-- Motor: PostgreSQL (Supabase)
--
-- RF-05: módulo de uso EXCLUSIVO para el odontólogo. Se compone de
-- dos tablas:
--
-- 1) clinical_records: una nota de atención por cita ya completada
--    (1 cita = como máximo 1 registro clínico).
-- 2) odontogram_teeth: estado actual de cada pieza dental del
--    paciente (numeración FDI-simplificada 1-32). Se actualiza
--    pieza por pieza; si una pieza nunca fue tocada, simplemente no
--    tiene fila (se asume sana por defecto en el cliente).
-- ============================================================

CREATE TABLE IF NOT EXISTS clinical_records (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
    patient_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dentist_id     UUID NOT NULL REFERENCES dentists(id) ON DELETE CASCADE,
    notes          TEXT NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_clinical_records_patient ON clinical_records(patient_id);

CREATE TABLE IF NOT EXISTS odontogram_teeth (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tooth_number      SMALLINT NOT NULL CHECK (tooth_number BETWEEN 1 AND 32),
    condition         VARCHAR(20) NOT NULL CHECK (condition IN
                       ('SANO', 'CARIES', 'OBTURADO', 'EXTRAIDO', 'CORONA', 'ENDODONCIA', 'IMPLANTE')),
    notes             VARCHAR(500),
    updated_by_dentist_id UUID REFERENCES dentists(id) ON DELETE SET NULL,
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (patient_id, tooth_number)
);

CREATE INDEX IF NOT EXISTS idx_odontogram_patient ON odontogram_teeth(patient_id);
