-- =====================================================================
-- seed-e2e.sql: datos de prueba de la Fase 5 (E2E manual con Postman).
-- SOLO para la base pulsepass_e2e. Nunca contra desarrollo ni produccion.
--
-- Se ejecuta UNA vez por corrida, despues de que la aplicacion creo el
-- esquema con Flyway y despues de registrar por POST /api/users las
-- cuentas e2e_admin, e2e_staff y e2e_inactivo (requests PREP-02 a 04).
-- No guarda contraseñas: cada cuenta se registra con la que elija quien
-- ejecuta las pruebas, y aqui solo se cambian sus roles.
-- Es idempotente: correrlo dos veces no duplica nada.
-- =====================================================================

\set ON_ERROR_STOP on

-- 0. Proteccion: si la base no es la de E2E, se detiene sin tocar nada
DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'seed-e2e.sql solo se ejecuta en pulsepass_e2e (base actual: %)', current_database();
END IF;
    IF (SELECT count(*) FROM users WHERE username IN ('e2e_admin', 'e2e_staff')) <> 2 THEN
        RAISE EXCEPTION 'Faltan e2e_admin o e2e_staff: registralos primero con PREP-02 y PREP-03';
END IF;
END
$$;

-- 1. Roles privilegiados: el registro publico solo crea USER, y aqui se reemplaza
DELETE FROM user_roles
WHERE role = 'USER'
  AND user_id IN (SELECT id FROM users WHERE username IN ('e2e_admin', 'e2e_staff'));

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'e2e_admin'
    ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF' FROM users WHERE username = 'e2e_staff'
    ON CONFLICT DO NOTHING;

-- 2. Venues: uno activo, uno inactivo y uno con capacidad 1 para probar el aforo
INSERT INTO venues (code, name, city, address, capacity, active) VALUES
                                                                     ('E2E-VEN-ACT', 'E2E Arena Activa',   'Santa Marta', 'Calle 10 # 1-01', 500, TRUE),
                                                                     ('E2E-VEN-INA', 'E2E Teatro Cerrado', 'Santa Marta', 'Calle 20 # 2-02', 100, FALSE),
                                                                     ('E2E-VEN-UNO', 'E2E Sala Unica',     'Santa Marta', 'Calle 30 # 3-03', 1,   TRUE)
    ON CONFLICT (code) DO NOTHING;

-- 3. Un artista inactivo, que nunca debe aparecer en /api/artists/active
INSERT INTO artists (stage_name, country, genre, active) VALUES
    ('E2E Retired Artist', 'Colombia', 'Pop', FALSE)
    ON CONFLICT (stage_name) DO NOTHING;

-- 4. Eventos (fechas en 2030 para que sigan siendo futuras)
INSERT INTO events (event_code, name, description, category, status, event_date, minimum_age, venue_id)
SELECT f.event_code, f.name, f.description, f.category, f.status, f.event_date, f.minimum_age, v.id
FROM (VALUES
          ('E2E-PUB-01',   'E2E Festival Publicado', 'Evento publicado para compras',      'MUSIC',      'PUBLISHED', TIMESTAMP '2030-06-01 20:00:00', 18, 'E2E-VEN-ACT'),
          ('E2E-DRAFT-01', 'E2E Borrador Privado',   'Nunca debe verse en publico',        'MUSIC',      'DRAFT',     TIMESTAMP '2030-07-01 20:00:00', 18, 'E2E-VEN-ACT'),
          ('E2E-TOPUB-01', 'E2E Por Publicar',       'Borrador que USER y STAFF no pueden publicar', 'CULTURE', 'DRAFT', TIMESTAMP '2030-08-01 20:00:00', 0, 'E2E-VEN-ACT'),
          ('E2E-EDAD-99',  'E2E Edad Imposible',     'Edad minima 99 para probar la regla de edad', 'EDUCATION', 'PUBLISHED', TIMESTAMP '2030-06-15 18:00:00', 99, 'E2E-VEN-ACT'),
          ('E2E-AFORO-1',  'E2E Aforo de Uno',       'Una sola entrada disponible',        'TECHNOLOGY', 'PUBLISHED', TIMESTAMP '2030-06-20 19:00:00', 0,  'E2E-VEN-UNO')
     ) AS f(event_code, name, description, category, status, event_date, minimum_age, venue_code)
         JOIN venues v ON v.code = f.venue_code
    ON CONFLICT (event_code) DO NOTHING;

-- 5. Neon Waves (artista de V2) en el publicado y en el borrador: sirve para probar
--    que /api/events/by-artist no filtra borradores
INSERT INTO event_artists (event_id, artist_id)
SELECT e.id, a.id
FROM events e
         JOIN artists a ON a.stage_name = 'Neon Waves'
WHERE e.event_code IN ('E2E-PUB-01', 'E2E-DRAFT-01')
    ON CONFLICT DO NOTHING;

-- 6. Comprobacion: debe mostrar e2e_admin con ADMIN y e2e_staff con STAFF, y nada mas
SELECT u.username, r.role
FROM users u
         JOIN user_roles r ON r.user_id = u.id
WHERE u.username IN ('e2e_admin', 'e2e_staff')
ORDER BY u.username, r.role;
