-- V6: Create Adventure Note Table
-- Domain: Adventure Notes Management
-- Created: 2025-11-24

CREATE TABLE adventure_note (
    id uuid not null,

    title varchar(150) not null,
    content text not null,

    is_active boolean not null,
    created_by uuid not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    deleted_at timestamp(6),

    adventure_id uuid not null,
    primary key (id)
);

-- Indexes
CREATE INDEX idx_adventure_note_active ON adventure_note(is_active);
CREATE INDEX idx_adventure_note_created ON adventure_note(created_at);
CREATE INDEX idx_adventure_note_created_by ON adventure_note(created_by);
CREATE INDEX idx_adventure_note_adventure ON adventure_note(adventure_id);

-- Foreign key
ALTER TABLE adventure_note
ADD CONSTRAINT fk_adventure_note_adventure
FOREIGN KEY (adventure_id) REFERENCES adventure(id) ON DELETE CASCADE;