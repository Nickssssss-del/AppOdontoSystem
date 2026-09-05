-- ============================================================
-- OdontoSystem — V2: Mejoras de escalabilidad del esquema
-- Motor: PostgreSQL (Supabase)
--
-- Esta migración es aditiva: no elimina ni renombra ninguna
-- columna usada por el código Java actual (entidades User,
-- Dentist, Appointment), por lo que el backend sigue
-- funcionando exactamente igual sin cambios de código.
-- ============================================================

-- ------------------------------------------------------------
-- 1) Especialidades normalizadas (antes: texto libre repetido
--    en cada fila de `dentists`)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS specialties (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name    VARCHAR(120) NOT NULL UNIQUE
);

INSERT INTO specialties (name)
SELECT DISTINCT specialty FROM dentists
ON CONFLICT (name) DO NOTHING;

ALTER TABLE dentists
    ADD COLUMN IF NOT EXISTS specialty_id UUID REFERENCES specialties(id);

UPDATE dentists d
SET specialty_id = s.id
FROM specialties s
WHERE d.specialty = s.name
  AND d.specialty_id IS NULL;

-- La columna `specialty` (texto) se conserva para no romper el
-- mapeo actual de DentistDto <-> Dentist.kt. specialty_id queda
-- disponible para consultas normalizadas.

-- ------------------------------------------------------------
-- 2) Horarios reales por odontólogo
--    day_of_week usa SMALLINT (1=Lunes ... 7=Domingo) en vez de
--    texto en español, para evitar romperse si en el futuro se
--    mapea a java.time.DayOfWeek en Java (que serializa en inglés,
--    p. ej. "WEDNESDAY", incompatible con un CHECK en español).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dentist_schedules (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dentist_id   UUID NOT NULL REFERENCES dentists(id) ON DELETE CASCADE,
    day_of_week  SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    slot_minutes INTEGER NOT NULL DEFAULT 30,
    CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_dentist_schedules_dentist ON dentist_schedules(dentist_id);

-- Semilla: convierte los días de `available_days` (texto en español)
-- en horarios de ejemplo (9:00 a 13:00, turnos de 30 min), traduciendo
-- cada nombre de día a su número correspondiente (1=Lunes...7=Domingo).
INSERT INTO dentist_schedules (dentist_id, day_of_week, start_time, end_time, slot_minutes)
SELECT d.id,
       CASE day
           WHEN 'Lunes' THEN 1
           WHEN 'Martes' THEN 2
           WHEN 'Miércoles' THEN 3
           WHEN 'Jueves' THEN 4
           WHEN 'Viernes' THEN 5
           WHEN 'Sábado' THEN 6
           WHEN 'Domingo' THEN 7
       END,
       TIME '09:00', TIME '13:00', 30
FROM dentists d, unnest(d.available_days) AS day
WHERE NOT EXISTS (
    SELECT 1 FROM dentist_schedules ds
    WHERE ds.dentist_id = d.id
      AND ds.day_of_week = CASE day
          WHEN 'Lunes' THEN 1 WHEN 'Martes' THEN 2 WHEN 'Miércoles' THEN 3
          WHEN 'Jueves' THEN 4 WHEN 'Viernes' THEN 5 WHEN 'Sábado' THEN 6
          WHEN 'Domingo' THEN 7
      END
);

-- ------------------------------------------------------------
-- 3) Evitar doble reserva: mismo odontólogo, misma fecha y hora
-- ------------------------------------------------------------
ALTER TABLE appointments
    ADD CONSTRAINT uq_appointment_slot UNIQUE (dentist_id, date, time);

-- ------------------------------------------------------------
-- 4) Reseñas reales de pacientes
--    appointment_id es obligatorio (NOT NULL) porque una reseña
--    solo debe existir como fruto de una cita ya realizada. La
--    unicidad se aplica sobre appointment_id (una reseña por
--    cita), evitando el problema de múltiples NULLs en un UNIQUE
--    compuesto. ON DELETE CASCADE: si la cita se elimina, su
--    reseña asociada se elimina con ella.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reviews (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dentist_id     UUID NOT NULL REFERENCES dentists(id) ON DELETE CASCADE,
    patient_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    rating         SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment        VARCHAR(1000),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (appointment_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_dentist ON reviews(dentist_id);

-- ------------------------------------------------------------
-- 5) Auditoría: columna `updated_at` en las tablas principales
-- ------------------------------------------------------------
ALTER TABLE users        ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE dentists      ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE appointments  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_dentists_updated_at ON dentists;
CREATE TRIGGER trg_dentists_updated_at
    BEFORE UPDATE ON dentists
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_appointments_updated_at ON appointments;
CREATE TRIGGER trg_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ------------------------------------------------------------
-- 6) Refresh tokens: permite renovar la sesión sin pedir
--    contraseña de nuevo cada vez que expira el JWT de acceso.
--    Índice en expires_at: necesario para que las tareas
--    periódicas de limpieza (DELETE ... WHERE expires_at < now())
--    no hagan un escaneo completo de la tabla.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires ON refresh_tokens(expires_at);