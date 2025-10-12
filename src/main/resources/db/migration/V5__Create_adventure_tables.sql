-- V5: Create Adventure Table
-- Domain: Adventures Management
-- Created: 2025-10-11

CREATE TABLE adventure (
    id uuid not null,

    title varchar(100) not null,
    description text,

    is_active boolean not null,
    created_by uuid not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    deleted_at timestamp(6),

    game_group_id uuid not null,
    primary key (id)
);

-- Indexes
CREATE INDEX idx_adventure_active ON adventure(is_active);
CREATE INDEX idx_adventure_created ON adventure(created_at);
CREATE INDEX idx_adventure_created_by ON adventure(created_by);
CREATE INDEX idx_adventure_group ON adventure(game_group_id);

-- Foreign key
ALTER TABLE adventure
ADD CONSTRAINT fk_adventure_game_group
FOREIGN KEY (game_group_id) REFERENCES game_group(id) ON DELETE CASCADE;