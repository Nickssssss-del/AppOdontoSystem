-- ============================================================
-- OdontoSystem — Migración inicial del esquema (Flyway)
-- Motor: PostgreSQL (Supabase)
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(150)        NOT NULL,
    email         VARCHAR(150)        NOT NULL UNIQUE,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(20)         NOT NULL DEFAULT 'PATIENT' CHECK (role IN ('PATIENT', 'DENTIST')),
    phone         VARCHAR(20)         DEFAULT '+51987654321',
    created_at    TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS dentists (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150)     NOT NULL,
    specialty       VARCHAR(120)     NOT NULL,
    district        VARCHAR(100)     NOT NULL,
    address         VARCHAR(255),
    rating          REAL             DEFAULT 0,
    reviews_count   INTEGER          DEFAULT 0,
    price           NUMERIC(10, 2)   DEFAULT 0,
    available_days  TEXT[]           NOT NULL DEFAULT '{}',
    phone           VARCHAR(20)      DEFAULT '51987654321',
    image_url       VARCHAR(500),
    avatar_initials VARCHAR(5)       DEFAULT 'DR'
);

CREATE TABLE IF NOT EXISTS appointments (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id    UUID             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dentist_id    UUID             NOT NULL REFERENCES dentists(id) ON DELETE RESTRICT,
    date          DATE             NOT NULL,
    time          VARCHAR(10)      NOT NULL,
    reason        VARCHAR(500),
    status        VARCHAR(20)      NOT NULL DEFAULT 'Pendiente'
                     CHECK (status IN ('Confirmada', 'Pendiente', 'Cancelada')),
    created_at    TIMESTAMP        NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_dentist ON appointments(dentist_id);
CREATE INDEX IF NOT EXISTS idx_dentists_district ON dentists(district);

-- ------------------------------------------------------------
-- Datos semilla mínimos para probar la app Android de inmediato
-- (contraseña de ejemplo: "123456", ya cifrada con BCrypt)
-- ------------------------------------------------------------
INSERT INTO users (name, email, password_hash, role, phone)
VALUES ('Paciente Demo', 'paciente@demo.com',
        '$2a$10$DowQGpAn7c6H4wcMcSDPAeXe3Q1MlrJZmzHu0Bqf9J3g4XkzXk1Zi',
        'PATIENT', '+51987654321')
ON CONFLICT (email) DO NOTHING;

INSERT INTO dentists (name, specialty, district, address, rating, reviews_count, price, available_days, phone, avatar_initials)
VALUES
 ('Dr. Carlos Mendoza', 'Ortodoncia', 'Miraflores', 'Av. Larco 345', 4.8, 132, 150.00, ARRAY['Lunes','Miércoles','Viernes'], '51987654321', 'CM'),
 ('Dra. Ana Torres', 'Odontología General', 'San Isidro', 'Av. Javier Prado 210', 4.6, 98, 100.00, ARRAY['Martes','Jueves'], '51987654322', 'AT'),
 ('Dr. Luis Ramírez', 'Endodoncia', 'Surco', 'Av. Primavera 550', 4.9, 210, 180.00, ARRAY['Lunes','Martes','Jueves'], '51987654323', 'LR')
ON CONFLICT DO NOTHING;
