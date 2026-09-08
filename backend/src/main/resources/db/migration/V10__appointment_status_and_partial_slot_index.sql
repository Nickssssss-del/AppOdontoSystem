-- OdontoSystem - V10: ciclo completo de atención y liberación de turnos cancelados.

ALTER TABLE appointments DROP CONSTRAINT IF EXISTS appointments_status_check;
ALTER TABLE appointments ADD CONSTRAINT appointments_status_check
    CHECK (status IN ('Confirmada', 'Pendiente', 'EnAtencion', 'Cancelada', 'Completada'));

ALTER TABLE appointments DROP CONSTRAINT IF EXISTS uq_appointment_slot;
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_appointment_slot
    ON appointments (dentist_id, date, time)
    WHERE status <> 'Cancelada';