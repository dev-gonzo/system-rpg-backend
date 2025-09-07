-- V7__Add_deleted_at_column_to_game_group.sql
-- Adiciona coluna deleted_at para soft delete na tabela game_group

ALTER TABLE game_group ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Criar índice para performance em consultas de soft delete
CREATE INDEX idx_game_group_deleted_at ON game_group(deleted_at);

-- Adicionar coluna deleted_at na tabela game_group_participant também
ALTER TABLE game_group_participant ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Criar índice para performance em consultas de soft delete
CREATE INDEX idx_game_group_participant_deleted_at ON game_group_participant(deleted_at);