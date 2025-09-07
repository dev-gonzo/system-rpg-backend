-- V1: Create Auth Tables (roles, token_blacklist)
-- Domain: Authentication and Authorization
-- Created: 2025-09-07

-- Create roles table (matching Hibernate structure)
CREATE TABLE roles (
    is_active boolean not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    id uuid not null,
    name varchar(50) not null unique,
    description varchar(255),
    primary key (id)
);

-- Create token_blacklist table (matching Hibernate structure)
CREATE TABLE token_blacklist (
    created_at timestamp(6) not null,
    expires_at timestamp(6) not null,
    id uuid not null,
    user_id uuid,
    token_hash varchar(64) not null unique,
    reason varchar(100),
    primary key (id)
);

-- Create indexes for better performance
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_active ON roles(is_active);
CREATE INDEX idx_token_blacklist_hash ON token_blacklist(token_hash);
CREATE INDEX idx_token_blacklist_user ON token_blacklist(user_id);
CREATE INDEX idx_token_blacklist_expires ON token_blacklist(expires_at);

-- Insert default roles
INSERT INTO roles (id, name, description, is_active, created_at, updated_at) VALUES 
(gen_random_uuid(), 'ADMIN', 'Administrator with full system access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'USER', 'Regular user with basic access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'MODERATOR', 'Moderator with limited administrative access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);