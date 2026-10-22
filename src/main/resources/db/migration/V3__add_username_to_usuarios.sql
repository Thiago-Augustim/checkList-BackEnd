-- ============================================================================
-- V3__add_username_to_usuarios.sql
-- Adiciona coluna username única na tabela usuarios para autenticação por username e senha
-- ============================================================================

ALTER TABLE usuarios ADD COLUMN username VARCHAR(80);

UPDATE usuarios SET username = 'gestor' WHERE LOWER(email) = 'gestor@checklist.com.br' AND username IS NULL;
UPDATE usuarios SET username = 'motorista' WHERE LOWER(email) = 'motorista@checklist.com.br' AND username IS NULL;
UPDATE usuarios SET username = LOWER(email) WHERE username IS NULL;

ALTER TABLE usuarios ALTER COLUMN username SET NOT NULL;
ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_username UNIQUE (username);

CREATE INDEX idx_usuarios_username ON usuarios(username);
