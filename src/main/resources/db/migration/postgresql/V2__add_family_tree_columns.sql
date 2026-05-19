-- ============================================
-- HériConsent - Migration V2 : Ajouter colonnes arbre généalogique (PostgreSQL)
-- ============================================

-- Ajouter les colonnes pour l'arbre généalogique à la table personnes
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS birth_year INTEGER;
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS death_year INTEGER;
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS profession VARCHAR(255);
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS city VARCHAR(255);
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS spouse_id UUID REFERENCES personnes(id) ON DELETE SET NULL;
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS parent_ids TEXT DEFAULT '[]';
ALTER TABLE personnes ADD COLUMN IF NOT EXISTS photo_initials VARCHAR(10);
