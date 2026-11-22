-- =====================================================================
-- seed-demo.sql: datos minimos para mostrar el frontend en la base de
-- desarrollo (pulsepass). Las migraciones solo traen artistas, asi que
-- sin esto no hay sedes para crear eventos ni cuentas STAFF/ADMIN.
--
-- Antes de correrlo, registra desde la pagina /registro (o con
-- POST /api/users) las cuentas demo_admin y demo_staff. Aqui solo se
-- cambian sus roles: este archivo no guarda ninguna contraseña.
-- Es idempotente.
-- =====================================================================

\set ON_ERROR_STOP on

DO $$
BEGIN
    IF current_database() <> 'pulsepass' THEN
        RAISE EXCEPTION 'seed-demo.sql es para la base pulsepass (base actual: %)', current_database();
END IF;
END
$$;

-- 1. Roles: el registro publico solo crea USER
DELETE FROM user_roles
WHERE role = 'USER'
  AND user_id IN (SELECT id FROM users WHERE username IN ('demo_admin', 'demo_staff'));

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'demo_admin'
    ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF' FROM users WHERE username = 'demo_staff'
    ON CONFLICT DO NOTHING;

-- 2. Sedes para el formulario "Nuevo evento"
INSERT INTO venues (code, name, city, address, capacity, active) VALUES
                                                                     ('VEN-SMR-01', 'Marina Convention Center', 'Santa Marta', 'Carrera 1 # 10-20', 3000, TRUE),
                                                                     ('VEN-BAQ-01', 'Coliseo del Norte',        'Barranquilla', 'Calle 72 # 45-10', 8000, TRUE)
    ON CONFLICT (code) DO NOTHING;
