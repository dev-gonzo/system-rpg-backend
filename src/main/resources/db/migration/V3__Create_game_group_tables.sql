-- V3: Create Game Group Tables (game_group, game_group_invite, game_group_participant)
-- Domain: Game Groups Management
-- Created: 2025-09-07

-- Create game_group table (matching Hibernate structure)
CREATE TABLE game_group (
    id uuid not null,

    campaign_name varchar(100) not null,
    game_system varchar(100) not null,
    setting_world varchar(100) not null,
    short_description varchar(100) not null,
    description text,
    min_players integer,
    max_players integer,

    visibility integer not null,
    access_rule integer not null,
    modality integer not null,
    country varchar(100),
    state varchar(100),
    city varchar(100),

    themes_content varchar(500),
    punctuality_attendance varchar(500),
    house_rules varchar(500),
    behavioral_expectations varchar(500),

    is_active boolean not null,
    created_by uuid not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    deleted_at timestamp(6),
    primary key (id)
);

-- Create game_group_invite table (matching Hibernate structure)
CREATE TABLE game_group_invite (
    role integer not null,
    is_unique_use boolean not null,
    is_used boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    expires_at timestamp(6),
    updated_at timestamp(6) not null,
    used_at timestamp(6),
    created_by uuid not null,
    game_group_id uuid not null,
    id uuid not null,
    used_by uuid,
    invite_code varchar(32) not null unique,
    primary key (id)
);

-- Create game_group_participant table (matching Hibernate structure)
CREATE TABLE game_group_participant (
    is_active boolean not null,
    role integer not null check ((role<=2) and (role>=0)),
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6) not null,
    game_group_id uuid not null,
    id uuid not null,
    user_id uuid not null,
    primary key (id),
    unique (game_group_id, user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_game_group_active ON game_group(is_active);
CREATE INDEX idx_game_group_created ON game_group(created_at);
CREATE INDEX idx_game_group_created_by ON game_group(created_by);

CREATE INDEX idx_game_group_invite_group ON game_group_invite(game_group_id);
CREATE INDEX idx_game_group_invite_code ON game_group_invite(invite_code);
CREATE INDEX idx_game_group_invite_expires ON game_group_invite(expires_at);
CREATE INDEX idx_game_group_invite_created_by ON game_group_invite(created_by);

CREATE INDEX idx_game_group_participant_group ON game_group_participant(game_group_id);
CREATE INDEX idx_game_group_participant_user ON game_group_participant(user_id);
CREATE INDEX idx_game_group_participant_role ON game_group_participant(role);
CREATE INDEX idx_game_group_participant_active ON game_group_participant(is_active);

-- Add foreign key constraints
ALTER TABLE game_group_invite 
ADD CONSTRAINT fk_game_group_invite_group 
FOREIGN KEY (game_group_id) REFERENCES game_group(id) ON DELETE CASCADE;

ALTER TABLE game_group_participant 
ADD CONSTRAINT fk_game_group_participant_group 
FOREIGN KEY (game_group_id) REFERENCES game_group(id) ON DELETE CASCADE;

-- Note: Foreign keys to users table commented out temporarily
-- ALTER TABLE game_group 
-- ADD CONSTRAINT fk_game_group_created_by 
-- FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE;

-- ALTER TABLE game_group_invite 
-- ADD CONSTRAINT fk_game_group_invite_created_by 
-- FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE;

-- ALTER TABLE game_group_invite 
-- ADD CONSTRAINT fk_game_group_invite_used_by 
-- FOREIGN KEY (used_by) REFERENCES users(id) ON DELETE CASCADE;

-- ALTER TABLE game_group_participant 
-- ADD CONSTRAINT fk_game_group_participant_user 
-- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;