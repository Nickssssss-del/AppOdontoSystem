-- ============================================================
-- OdontoSystem — V4: vincular odontólogos con su cuenta de usuario
-- Motor: PostgreSQL (Supabase)
--
-- Hasta ahora `dentists` era solo un directorio (nombre, distrito,
-- rating, etc.) sin relación con `users`. Esto impedía que un
-- usuario con rol DENTIST supiera "cuál fila de dentists soy yo",
-- bloqueando cualquier acción del odontólogo sobre sus propias
-- citas (confirmar, completar) — parte pendiente de RF-04, y
-- requisito para RF-05 (historial clínico).
--
-- user_id es NULLABLE porque los odontólogos sembrados en V1
-- (Carlos Mendoza, Ana Torres, Luis Ramírez) no tienen cuenta de
-- login real; se irán vinculando manualmente o a futuro con un
-- flujo de "reclamar perfil" al registrarse como DENTIST.
-- ============================================================

ALTER TABLE dentists
    ADD COLUMN IF NOT EXISTS user_id UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_dentists_user ON dentists(user_id);
