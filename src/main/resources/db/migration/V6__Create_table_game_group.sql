-- V6__Create_table_game_group.sql
-- Criação das tabelas para sistema de grupos de jogos

-- Tabela de grupos de jogos
CREATE TABLE game_group (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    campaign_name VARCHAR(100) NOT NULL,
    short_description VARCHAR(200) NOT NULL,
    campaign_overview TEXT,
    game_system VARCHAR(100) NOT NULL,
    setting_world VARCHAR(100) NOT NULL,
    min_players INT NOT NULL CHECK (min_players > 0),
    max_players INT NOT NULL CHECK (max_players >= min_players),
    visibility_public BOOLEAN DEFAULT FALSE,
    access_rule INT NOT NULL CHECK (access_rule IN (0, 1, 2)), -- 0=free, 1=friends, 2=approval
    modality INT NOT NULL CHECK (modality IN (0, 1)), -- 0=online, 1=presencial
    location_or_virtual_tabletop VARCHAR(200) NOT NULL,
    country VARCHAR(100),
    state VARCHAR(100),
    city VARCHAR(100),
    conduct TEXT,
    punctuality_attendance TEXT,
    house_role TEXT,
    behavioral_expectations TEXT,
    commitment BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de participantes do grupo de jogos
CREATE TABLE game_group_participant (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role VARCHAR(10) NOT NULL CHECK (role IN ('MASTER', 'PLAYER', 'GUEST')),
    game_group_id UUID NOT NULL REFERENCES game_group(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_group_id, user_id)
);

-- Índices para performance
CREATE INDEX idx_game_group_campaign_name ON game_group(campaign_name);
CREATE INDEX idx_game_group_game_system ON game_group(game_system);
CREATE INDEX idx_game_group_setting_world ON game_group(setting_world);
CREATE INDEX idx_game_group_user_id ON game_group(user_id);
CREATE INDEX idx_game_group_is_active ON game_group(is_active);
CREATE INDEX idx_game_group_access_rule ON game_group(access_rule);
CREATE INDEX idx_game_group_modality ON game_group(modality);
CREATE INDEX idx_game_group_created_at ON game_group(created_at);

CREATE INDEX idx_game_group_participant_game_group_id ON game_group_participant(game_group_id);
CREATE INDEX idx_game_group_participant_user_id ON game_group_participant(user_id);
CREATE INDEX idx_game_group_participant_role ON game_group_participant(role);
CREATE INDEX idx_game_group_participant_is_active ON game_group_participant(is_active);

-- Triggers para atualizar updated_at automaticamente
CREATE TRIGGER update_game_group_updated_at BEFORE UPDATE ON game_group
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_game_group_participant_updated_at BEFORE UPDATE ON game_group_participant
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comentários nas tabelas
COMMENT ON TABLE game_group IS 'Tabela de grupos de jogos/campanhas';
COMMENT ON COLUMN game_group.access_rule IS '0=free, 1=friends, 2=approval';
COMMENT ON COLUMN game_group.modality IS '0=online, 1=presencial';
COMMENT ON TABLE game_group_participant IS 'Tabela de participantes dos grupos de jogos';
COMMENT ON COLUMN game_group_participant.role IS 'MASTER, PLAYER ou GUEST';

-- Log de execução
DO $$
BEGIN
    RAISE NOTICE 'Migração V6 executada com sucesso: Tabelas game_group e game_group_participant criadas';
END $$;