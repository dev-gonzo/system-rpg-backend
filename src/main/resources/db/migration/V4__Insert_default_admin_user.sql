-- V4__Insert_default_admin_user.sql
-- Inserção do usuário administrador padrão para testes

-- Inserir usuário admin padrão (senha: admin123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, is_active, is_email_verified, created_at, updated_at, password_changed_at) VALUES
    (uuid_generate_v4(), 'admin', 'admin@systemrpg.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'System', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Associar o usuário admin à role ADMIN
INSERT INTO user_roles (id, user_id, role_id)
SELECT 
    uuid_generate_v4(),
    u.id,
    r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Log de execução
DO $$
BEGIN
    RAISE NOTICE 'Migração V4 executada com sucesso: Usuário admin criado';
END $$;