-- V3__Create_token_blacklist_table.sql
-- Criação da tabela de blacklist de tokens JWT

-- Tabela de tokens na blacklist (invalidados)
CREATE TABLE token_blacklist (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(100),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL
);

-- Índices para performance
CREATE INDEX idx_token_hash ON token_blacklist(token_hash);
CREATE INDEX idx_expires_at ON token_blacklist(expires_at);
CREATE INDEX idx_token_blacklist_user_id ON token_blacklist(user_id);
CREATE INDEX idx_token_blacklist_created_at ON token_blacklist(created_at);

-- Comentários na tabela
COMMENT ON TABLE token_blacklist IS 'Tabela de tokens JWT invalidados (blacklist)';
COMMENT ON COLUMN token_blacklist.token_hash IS 'Hash SHA-256 do token JWT para identificação única';
COMMENT ON COLUMN token_blacklist.expires_at IS 'Data/hora de expiração do token';
COMMENT ON COLUMN token_blacklist.reason IS 'Motivo da invalidação do token (logout, security, etc.)';
COMMENT ON COLUMN token_blacklist.user_id IS 'ID do usuário proprietário do token (pode ser nulo se usuário foi removido)';

-- Log de execução
DO $$
BEGIN
    RAISE NOTICE 'Migração V2 executada com sucesso: Tabela token_blacklist criada';
END $$;