-- ============================================
-- HériConsent - Migration V3 : Ajouter le champ validated aux héritiers (H2)
-- ============================================

ALTER TABLE heritiers
    ADD COLUMN IF NOT EXISTS validated BOOLEAN DEFAULT FALSE;
