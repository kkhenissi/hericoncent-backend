-- ============================================
-- HériConsent - Migration V4 : Ajouter le champ is_heir aux héritiers (PostgreSQL)
-- ============================================

ALTER TABLE heritiers
    ADD COLUMN IF NOT EXISTS is_heir BOOLEAN NOT NULL DEFAULT FALSE;
