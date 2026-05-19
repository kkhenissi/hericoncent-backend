-- ============================================
-- HériConsent - Migration V1 : Schéma initial (PostgreSQL)
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- USERS (comptes applicatifs)
CREATE TABLE users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email       TEXT        UNIQUE NOT NULL,
    password    TEXT        NOT NULL,
    role        TEXT        NOT NULL DEFAULT 'ROLE_HEIR',  -- ROLE_HEIR, ROLE_NOTAIRE, ROLE_ADMIN
    enabled     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- PERSONNES (profil physique)
CREATE TABLE personnes (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        REFERENCES users(id) ON DELETE SET NULL,
    nom             TEXT        NOT NULL,
    prenom          TEXT        NOT NULL,
    email           TEXT,
    telephone       TEXT,
    date_naissance  DATE,
    adresse         TEXT,
    nationalite     TEXT,
    identity_verified BOOLEAN   DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- DOSSIERS (biens en indivision)
CREATE TABLE dossiers (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    reference           TEXT        UNIQUE NOT NULL,
    titre               TEXT        NOT NULL,
    description         TEXT,
    reference_cadastrale TEXT,
    adresse_bien        TEXT,
    statut              TEXT        NOT NULL DEFAULT 'OUVERT',
    -- OUVERT, EN_VENTE, BLOQUE, ARCHIVE, RESOLU
    valeur_estimee      NUMERIC(15,2),
    notaire_id          UUID        REFERENCES users(id),
    created_by          UUID        REFERENCES users(id),
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW()
);

-- HERITIERS (relation personne <-> dossier)
CREATE TABLE heritiers (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dossier_id          UUID        NOT NULL REFERENCES dossiers(id) ON DELETE CASCADE,
    personne_id         UUID        NOT NULL REFERENCES personnes(id) ON DELETE CASCADE,
    part                NUMERIC(10,6) NOT NULL DEFAULT 0,
    role                TEXT        NOT NULL DEFAULT 'HERITIER',
    -- HERITIER, MANDATAIRE, REPRESENTANT
    est_representant    BOOLEAN     DEFAULT FALSE,
    represente_par      UUID        REFERENCES heritiers(id),
    statut_contact      TEXT        DEFAULT 'NON_CONTACTE',
    -- NON_CONTACTE, CONTACTE, IDENTIFIE, INJOIGNABLE
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(dossier_id, personne_id)
);

-- RELATIONS FAMILIALES (arbre généalogique)
CREATE TABLE relations_familiales (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    personne_id UUID    NOT NULL REFERENCES personnes(id) ON DELETE CASCADE,
    lie_a       UUID    NOT NULL REFERENCES personnes(id) ON DELETE CASCADE,
    type_lien   TEXT    NOT NULL,
    -- PARENT, ENFANT, CONJOINT, FRERE_SOEUR
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- DOCUMENTS
CREATE TABLE documents (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dossier_id  UUID        NOT NULL REFERENCES dossiers(id) ON DELETE CASCADE,
    nom         TEXT        NOT NULL,
    type_doc    TEXT        NOT NULL,
    -- ACTE_PROPRIETE, PIECE_IDENTITE, ACTE_NAISSANCE, PROCURATION, AUTRE
    s3_key      TEXT        NOT NULL,
    mime_type   TEXT,
    taille      BIGINT,
    checksum    TEXT,
    upload_par  UUID        REFERENCES users(id),
    uploaded_at TIMESTAMPTZ DEFAULT NOW()
);

-- CONSENTEMENTS (demandes de vote/décision)
CREATE TABLE consentements (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dossier_id      UUID        NOT NULL REFERENCES dossiers(id) ON DELETE CASCADE,
    titre           TEXT        NOT NULL,
    description     TEXT,
    type_action     TEXT        NOT NULL,
    -- VENTE, PARTAGE, DONATION, MANDAT, AUTRE
    statut          TEXT        NOT NULL DEFAULT 'EN_ATTENTE',
    -- EN_ATTENTE, PARTIEL, VALIDE, REJETE, EXPIRE
    seuil_accord    NUMERIC(5,2) DEFAULT 100.0,
    -- % de parts requis pour valider (ex: 100 = unanimité)
    expire_le       TIMESTAMPTZ,
    cree_par        UUID        REFERENCES users(id),
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- REPONSES HERITIERS (vote individuel)
CREATE TABLE consentement_reponses (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    consentement_id UUID        NOT NULL REFERENCES consentements(id) ON DELETE CASCADE,
    heritier_id     UUID        NOT NULL REFERENCES heritiers(id) ON DELETE CASCADE,
    reponse         TEXT,
    -- ACCEPTE, REJETE, DELEGUE, EN_ATTENTE
    commentaire     TEXT,
    token_acces     TEXT        UNIQUE,
    token_expire_le TIMESTAMPTZ,
    repondu_le      TIMESTAMPTZ,
    signature_id    UUID,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(consentement_id, heritier_id)
);

-- SIGNATURES ELECTRONIQUES
CREATE TABLE signatures (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    reponse_id      UUID        REFERENCES consentement_reponses(id),
    signataire_id   UUID        REFERENCES personnes(id),
    type_signature  TEXT        NOT NULL DEFAULT 'SIMPLE',
    -- SIMPLE, AVANCEE, QUALIFIEE
    provider        TEXT,
    provider_tx_id  TEXT,
    document_signe_key TEXT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- MANDATS (procurations)
CREATE TABLE mandats (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dossier_id      UUID        NOT NULL REFERENCES dossiers(id),
    mandant_id      UUID        NOT NULL REFERENCES heritiers(id),
    mandataire_id   UUID        NOT NULL REFERENCES heritiers(id),
    type_mandat     TEXT        NOT NULL DEFAULT 'GENERAL',
    statut          TEXT        NOT NULL DEFAULT 'ACTIF',
    valide_jusqu_au DATE,
    document_id     UUID        REFERENCES documents(id),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- AUDIT LOGS (immuable)
CREATE TABLE audit_logs (
    id          BIGSERIAL   PRIMARY KEY,
    action      TEXT        NOT NULL,
    entite_type TEXT        NOT NULL,
    entite_id   UUID,
    acteur_id   UUID        REFERENCES users(id),
    ip_address  TEXT,
    payload     JSONB,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- NOTIFICATIONS
CREATE TABLE notifications (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        REFERENCES users(id),
    titre       TEXT        NOT NULL,
    message     TEXT        NOT NULL,
    type        TEXT        NOT NULL DEFAULT 'INFO',
    lu          BOOLEAN     DEFAULT FALSE,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- INDEXES
CREATE INDEX idx_dossiers_statut       ON dossiers(statut);
CREATE INDEX idx_dossiers_reference    ON dossiers(reference);
CREATE INDEX idx_heritiers_dossier     ON heritiers(dossier_id);
CREATE INDEX idx_heritiers_personne    ON heritiers(personne_id);
CREATE INDEX idx_consentements_dossier ON consentements(dossier_id);
CREATE INDEX idx_consentements_statut  ON consentements(statut);
CREATE INDEX idx_reponses_consentement ON consentement_reponses(consentement_id);
CREATE INDEX idx_reponses_token        ON consentement_reponses(token_acces);
CREATE INDEX idx_audit_logs_entite     ON audit_logs(entite_type, entite_id);
CREATE INDEX idx_audit_logs_acteur     ON audit_logs(acteur_id);
CREATE INDEX idx_personnes_user        ON personnes(user_id);

-- DONNEES INITIALES
INSERT INTO users (id, email, password, role) VALUES
(gen_random_uuid(), 'admin@hericonsent.fr', '$2b$12$3NNOkZ9fmEICShHs3rdTF.E4iL1t2kt6avMi/wn4RrhtOAEfkqGie', 'ROLE_ADMIN'),
-- password: admin123
(gen_random_uuid(), 'notaire@hericonsent.fr', '$2b$12$3NNOkZ9fmEICShHs3rdTF.E4iL1t2kt6avMi/wn4RrhtOAEfkqGie', 'ROLE_NOTAIRE');
-- password: admin123
