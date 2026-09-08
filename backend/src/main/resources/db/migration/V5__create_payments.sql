-- ============================================================
-- OdontoSystem — V5: pagos en línea (modo sandbox)
-- Motor: PostgreSQL (Supabase)
--
-- RF-08 pide una pasarela de pagos SIMULADA para cobrar la seña o
-- costo de reserva. No se integra ningún proveedor real (Stripe,
-- Culqi, etc.) porque el requisito es explícitamente sandbox; el
-- backend simula la aprobación del cobro y deja registro auditable.
-- ============================================================

CREATE TABLE IF NOT EXISTS payments (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
    amount         NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    status         VARCHAR(20) NOT NULL CHECK (status IN ('PAID', 'FAILED')),
    method         VARCHAR(30) NOT NULL DEFAULT 'SANDBOX_CARD',
    reference      VARCHAR(50) NOT NULL UNIQUE,
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_payments_appointment ON payments(appointment_id);
