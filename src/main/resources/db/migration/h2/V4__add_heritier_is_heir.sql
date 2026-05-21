-- ============================================
-- HériConsent - Migration V4 : Ajouter le champ is_heir aux héritiers (H2)
-- ============================================

ALTER TABLE heritiers
    ADD COLUMN IF NOT EXISTS is_heir BOOLEAN DEFAULT FALSE;
