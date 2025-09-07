-- V2: Create Users Tables (users, user_roles)
-- Domain: User Management
-- Created: 2025-09-07

-- Create users table (matching Hibernate structure)
CREATE TABLE users (
    is_active boolean not null,
    is_email_verified boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    last_login_at timestamp(6),
    password_changed_at timestamp(6),
    updated_at timestamp(6) not null,
    id uuid not null,
    username varchar(50) not null unique,
    email varchar(100) not null unique,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    password_hash varchar(255) not null,
    primary key (id)
);

-- Create user_roles junction table (matching Hibernate structure)
CREATE TABLE user_roles (
    role_id uuid not null,
    user_id uuid not null,
    primary key (role_id, user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_created ON users(created_at);
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- Add foreign key constraints
ALTER TABLE user_roles 
ADD CONSTRAINT fk_user_roles_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_roles 
ADD CONSTRAINT fk_user_roles_role 
FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

-- Add foreign key constraint to token_blacklist (now that users table exists)
ALTER TABLE token_blacklist 
ADD CONSTRAINT fk_token_blacklist_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;