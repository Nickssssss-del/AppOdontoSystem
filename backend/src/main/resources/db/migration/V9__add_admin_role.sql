-- OdontoSystem - V9: habilita el rol administrativo para RBAC.

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('PATIENT', 'DENTIST', 'ADMIN'));