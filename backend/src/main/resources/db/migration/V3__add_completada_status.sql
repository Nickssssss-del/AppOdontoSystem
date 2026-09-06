-- ============================================================
-- OdontoSystem — V3: agregar el estado 'Completada' a citas
-- Motor: PostgreSQL (Supabase)
--
-- RF-04 requiere 4 estados (Pendiente, Confirmada, Cancelada,
-- Completada). V1 solo contemplaba 3. Esta migración reemplaza
-- el CHECK constraint existente para incluir el cuarto estado,
-- sin tocar filas ya existentes.
-- ============================================================

ALTER TABLE appointments DROP CONSTRAINT IF EXISTS appointments_status_check;

ALTER TABLE appointments
    ADD CONSTRAINT appointments_status_check
    CHECK (status IN ('Confirmada', 'Pendiente', 'Cancelada', 'Completada'));