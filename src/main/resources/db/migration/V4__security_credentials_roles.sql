-- Credenciales y roles para la capa de seguridad (Fase 4).
-- No modifica tablas existentes: los usuarios creados antes de esta migracion
-- quedan sin credencial y no pueden iniciar sesion hasta una activacion controlada.

CREATE TABLE user_credentials (
    user_id              BIGINT       PRIMARY KEY,
    password_hash        VARCHAR(100) NOT NULL,
    password_changed_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id)
      REFERENCES users (id)
);

CREATE TABLE user_roles (
    user_id  BIGINT      NOT NULL,
    role     VARCHAR(20) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT chk_user_roles_role CHECK (
        role IN ('USER', 'STAFF', 'ADMIN')
        )
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
