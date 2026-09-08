-- ============================================================
-- OdontoSystem — V8: reasignar dentistas originales de V1 a Ica
-- Motor: PostgreSQL (Supabase)
--
-- Los 3 odontólogos sembrados en V1 quedaron en distritos de Lima
-- (Miraflores, San Isidro, Surco), incompatible con RF-02 ("Fase
-- Piloto Ica"). Se reasignan por nombre (no se borran) para no
-- romper las citas, reseñas y pagos ya creados sobre ellos durante
-- las pruebas.
-- ============================================================

UPDATE dentists SET district = 'Ica' WHERE name = 'Dr. Carlos Mendoza';
UPDATE dentists SET district = 'Parcona' WHERE name = 'Dra. Ana Torres';
UPDATE dentists SET district = 'Los Aquijes' WHERE name = 'Dr. Luis Ramírez';
