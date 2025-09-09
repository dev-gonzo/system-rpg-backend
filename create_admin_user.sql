-- Criar usuário admin com senha 'admin123'
INSERT INTO users (id, username, email, first_name, last_name, password_hash, is_active, is_email_verified, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@systemrpg.com',
    'Admin',
    'User',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye/Zo1VW9y2cpLchcUSC/JYKEJxeOlW9q', -- BCrypt hash para 'admin123'
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Obter IDs necessários
DO $$
DECLARE
    admin_user_id uuid;
    admin_role_id uuid;
BEGIN
    -- Buscar ID do usuário admin
    SELECT id INTO admin_user_id FROM users WHERE username = 'admin';
    
    -- Buscar ID do role ADMIN
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN';
    
    -- Inserir relacionamento user_roles
    INSERT INTO user_roles (user_id, role_id) VALUES (admin_user_id, admin_role_id);
    
    RAISE NOTICE 'Usuário admin criado com sucesso!';
END $$;

-- Verificar se o usuário foi criado
SELECT u.username, u.email, u.is_active, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';
