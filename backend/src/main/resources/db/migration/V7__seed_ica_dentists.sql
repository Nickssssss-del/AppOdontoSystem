-- ============================================================
-- OdontoSystem — V7: odontólogos en distritos de la provincia de Ica
-- Motor: PostgreSQL (Supabase)
--
-- RF-02 exige explícitamente "Fase Piloto Ica": los odontólogos
-- sembrados en V1 (Miraflores, San Isidro, Surco) son distritos de
-- LIMA, no de Ica — un desajuste real con el requerimiento.
--
-- Esta migración AGREGA odontólogos en distritos reales de la
-- provincia de Ica, sin tocar ni borrar los datos de V1 (regla de
-- oro: nunca editar una migración ya aplicada).
-- ============================================================

INSERT INTO dentists (name, specialty, district, address, rating, reviews_count, price, available_days, phone, avatar_initials)
VALUES
 ('Dr. Jorge Huamán',   'Odontología General', 'Ica',              'Av. Grau 412, Ica',              4.7, 64,  80.00,  ARRAY['Lunes','Martes','Miércoles','Jueves','Viernes'], '51956123001', 'JH'),
 ('Dra. Rosa Ochoa',    'Ortodoncia',          'Parcona',          'Av. Los Maestros 220, Parcona',  4.5, 41,  120.00, ARRAY['Lunes','Miércoles','Viernes'],                    '51956123002', 'RO'),
 ('Dr. Miguel Cárdenas','Endodoncia',          'Los Aquijes',      'Calle Real 88, Los Aquijes',     4.9, 77,  150.00, ARRAY['Martes','Jueves','Sábado'],                       '51956123003', 'MC'),
 ('Dra. Fiorella Pinto','Odontopediatría',     'Subtanjalla',      'Av. Panamericana Sur Km 4, Subtanjalla', 4.6, 53, 90.00, ARRAY['Lunes','Martes','Jueves'],                    '51956123004', 'FP'),
 ('Dr. Renzo Vargas',   'Cirugía Maxilofacial','La Tinguiña',      'Av. Cutervo 305, La Tinguiña',   4.8, 39,  200.00, ARRAY['Miércoles','Viernes','Sábado'],                   '51956123005', 'RV'),
 ('Dra. Milagros Solís','Odontología General', 'Santiago',         'Av. Ayabaca 150, Santiago',      4.4, 28,  75.00,  ARRAY['Lunes','Miércoles','Viernes'],                    '51956123006', 'MS'),
 ('Dr. Álvaro Núñez',   'Ortodoncia',          'San Juan Bautista','Jr. Lima 210, San Juan Bautista', 4.7, 46, 130.00, ARRAY['Martes','Jueves','Sábado'],                       '51956123007', 'AN'),
 ('Dra. Carmen Reyes',  'Periodoncia',         'Pueblo Nuevo',     'Av. San Martín 77, Pueblo Nuevo', 4.6, 34, 110.00, ARRAY['Lunes','Jueves'],                                 '51956123008', 'CR')
ON CONFLICT DO NOTHING;
