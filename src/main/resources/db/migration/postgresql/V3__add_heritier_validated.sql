-- ============================================
-- HériConsent - Migration V3 : Ajouter le champ validated aux héritiers (PostgreSQL)
-- ============================================

ALTER TABLE heritiers
    ADD COLUMN IF NOT EXISTS validated BOOLEAN NOT NULL DEFAULT FALSE;
