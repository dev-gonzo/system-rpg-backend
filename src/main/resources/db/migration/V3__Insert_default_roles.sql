-- V3__Insert_default_roles.sql
-- Inserção das roles padrão do sistema

-- Inserir roles padrão
INSERT INTO roles (name, description, is_active) VALUES
    ('USER', 'Usuário padrão do sistema', true),
    ('ADMIN', 'Administrador do sistema', true),
    ('MANAGER', 'Gerente do sistema', true)
ON CONFLICT (name) DO NOTHING;