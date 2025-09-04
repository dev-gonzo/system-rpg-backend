-- Migration para adicionar coluna deleted_at na tabela users para implementar soft delete

ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;

-- Criar índice para melhorar performance das consultas que filtram por deleted_at
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Comentário da coluna
COMMENT ON COLUMN users.deleted_at IS 'Data e hora em que o usuário foi deletado (soft delete). NULL indica que o usuário não foi deletado.';