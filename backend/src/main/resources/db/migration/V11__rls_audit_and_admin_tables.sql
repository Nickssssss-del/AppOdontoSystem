-- OdontoSystem - V11: auditoría administrativa y políticas RLS de Supabase.

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_email VARCHAR(150) NOT NULL,
    action VARCHAR(80) NOT NULL,
    resource VARCHAR(80) NOT NULL,
    resource_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at DESC);

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE clinical_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE odontogram_teeth ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS users_self_access ON users;
CREATE POLICY users_self_access ON users
    USING (id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
        OR (current_setting('request.jwt.claims', true)::json ->> 'role') = 'ADMIN');

DROP POLICY IF EXISTS appointments_patient_or_dentist ON appointments;
CREATE POLICY appointments_patient_or_dentist ON appointments
    USING (
        patient_id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
        OR dentist_id IN (
            SELECT d.id FROM dentists d JOIN users u ON u.id = d.user_id
            WHERE u.id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
        )
        OR (current_setting('request.jwt.claims', true)::json ->> 'role') = 'ADMIN'
    );

DROP POLICY IF EXISTS clinical_records_dentist_access ON clinical_records;
CREATE POLICY clinical_records_dentist_access ON clinical_records
    USING (dentist_id IN (
        SELECT d.id FROM dentists d JOIN users u ON u.id = d.user_id
        WHERE u.id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
    ) OR patient_id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
    OR (current_setting('request.jwt.claims', true)::json ->> 'role') = 'ADMIN')
    WITH CHECK (dentist_id IN (
        SELECT d.id FROM dentists d JOIN users u ON u.id = d.user_id
        WHERE u.id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
    ) OR (current_setting('request.jwt.claims', true)::json ->> 'role') = 'ADMIN');

DROP POLICY IF EXISTS odontogram_dentist_access ON odontogram_teeth;
CREATE POLICY odontogram_dentist_access ON odontogram_teeth
    USING (updated_by_dentist_id IN (
        SELECT d.id FROM dentists d JOIN users u ON u.id = d.user_id
        WHERE u.id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
    ) OR patient_id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
    OR (current_setting('request.jwt.claims', true)::json ->> 'role') = 'ADMIN')
    WITH CHECK (updated_by_dentist_id IN (
        SELECT d.id FROM dentists d JOIN users u ON u.id = d.user_id
        WHERE u.id::text = current_setting('request.jwt.claims', true)::json ->> 'uid'
    ) OR (current_setting('request.jwt.claims', true)::json ->> 'role') = 'ADMIN');