-- ============================================
-- HériConsent - Migration V1 : Schéma initial (H2)
-- Adapté pour H2 (UUID via RANDOM_UUID(), TIMESTAMP, CLOB pour JSON, AUTO_INCREMENT)
-- ============================================

-- USERS (comptes applicatifs)
CREATE TABLE users (
    id          UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    email       VARCHAR(255)        UNIQUE NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(50)        NOT NULL DEFAULT 'ROLE_HEIR',
    enabled     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PERSONNES (profil physique)
CREATE TABLE personnes (
    id              UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    user_id         UUID        REFERENCES users(id) ON DELETE SET NULL,
    nom             VARCHAR(255)        NOT NULL,
    prenom          VARCHAR(255)        NOT NULL,
    email           VARCHAR(255),
    telephone       VARCHAR(50),
    date_naissance  DATE,
    adresse         VARCHAR(1000),
    nationalite     VARCHAR(100),
    identity_verified BOOLEAN   DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- DOSSIERS (biens en indivision)
CREATE TABLE dossiers (
    id                  UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    reference           VARCHAR(255)        UNIQUE NOT NULL,
    titre               VARCHAR(255)        NOT NULL,
    description         CLOB,
    reference_cadastrale VARCHAR(255),
    adresse_bien        VARCHAR(1000),
    statut              VARCHAR(50)        NOT NULL DEFAULT 'OUVERT',
    valeur_estimee      DECIMAL(15,2),
    notaire_id          UUID        REFERENCES users(id),
    created_by          UUID        REFERENCES users(id),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- HERITIERS (relation personne <-> dossier)
CREATE TABLE heritiers (
    id                  UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    dossier_id          UUID        NOT NULL REFERENCES dossiers(id) ON DELETE CASCADE,
    personne_id         UUID        NOT NULL REFERENCES personnes(id) ON DELETE CASCADE,
    part                DECIMAL(10,6) NOT NULL DEFAULT 0,
    role                VARCHAR(50)        NOT NULL DEFAULT 'HERITIER',
    est_representant    BOOLEAN     DEFAULT FALSE,
    represente_par      UUID        REFERENCES heritiers(id),
    statut_contact      VARCHAR(50)        DEFAULT 'NON_CONTACTE',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(dossier_id, personne_id)
);

-- RELATIONS FAMILIALES (arbre généalogique)
CREATE TABLE relations_familiales (
    id          UUID    DEFAULT RANDOM_UUID() PRIMARY KEY,
    personne_id UUID    NOT NULL REFERENCES personnes(id) ON DELETE CASCADE,
    lie_a       UUID    NOT NULL REFERENCES personnes(id) ON DELETE CASCADE,
    type_lien   VARCHAR(50)    NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- DOCUMENTS
CREATE TABLE documents (
    id          UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    dossier_id  UUID        NOT NULL REFERENCES dossiers(id) ON DELETE CASCADE,
    nom         VARCHAR(255)        NOT NULL,
    type_doc    VARCHAR(100)        NOT NULL,
    s3_key      VARCHAR(1000)        NOT NULL,
    mime_type   VARCHAR(100),
    taille      BIGINT,
    checksum    VARCHAR(255),
    upload_par  UUID        REFERENCES users(id),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CONSENTEMENTS
CREATE TABLE consentements (
    id              UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    dossier_id      UUID        NOT NULL REFERENCES dossiers(id) ON DELETE CASCADE,
    titre           VARCHAR(255)        NOT NULL,
    description     CLOB,
    type_action     VARCHAR(100)        NOT NULL,
    statut          VARCHAR(50)        NOT NULL DEFAULT 'EN_ATTENTE',
    seuil_accord    DECIMAL(5,2) DEFAULT 100.0,
    expire_le       TIMESTAMP,
    cree_par        UUID        REFERENCES users(id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- REPONSES HERITIERS
CREATE TABLE consentement_reponses (
    id              UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    consentement_id UUID        NOT NULL REFERENCES consentements(id) ON DELETE CASCADE,
    heritier_id     UUID        NOT NULL REFERENCES heritiers(id) ON DELETE CASCADE,
    reponse         VARCHAR(50),
    commentaire     CLOB,
    token_acces     VARCHAR(255)        UNIQUE,
    token_expire_le TIMESTAMP,
    repondu_le      TIMESTAMP,
    signature_id    UUID,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(consentement_id, heritier_id)
);

-- SIGNATURES ELECTRONIQUES (metadata as CLOB)
CREATE TABLE signatures (
    id              UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    reponse_id      UUID        REFERENCES consentement_reponses(id),
    signataire_id   UUID        REFERENCES personnes(id),
    type_signature  VARCHAR(50)        NOT NULL DEFAULT 'SIMPLE',
    provider        VARCHAR(255),
    provider_tx_id  VARCHAR(255),
    document_signe_key VARCHAR(1000),
    metadata        CLOB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MANDATS
CREATE TABLE mandats (
    id              UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    dossier_id      UUID        NOT NULL REFERENCES dossiers(id),
    mandant_id      UUID        NOT NULL REFERENCES heritiers(id),
    mandataire_id   UUID        NOT NULL REFERENCES heritiers(id),
    type_mandat     VARCHAR(50)        NOT NULL DEFAULT 'GENERAL',
    statut          VARCHAR(50)        NOT NULL DEFAULT 'ACTIF',
    valide_jusqu_au DATE,
    document_id     UUID        REFERENCES documents(id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AUDIT LOGS (immuable) - use AUTO_INCREMENT for H2
CREATE TABLE audit_logs (
    id          BIGINT   PRIMARY KEY AUTO_INCREMENT,
    action      VARCHAR(255)        NOT NULL,
    entite_type VARCHAR(255)        NOT NULL,
    entite_id   UUID,
    acteur_id   UUID        REFERENCES users(id),
    ip_address  VARCHAR(255),
    payload     CLOB,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- NOTIFICATIONS
CREATE TABLE notifications (
    id          UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    user_id     UUID        REFERENCES users(id),
    titre       VARCHAR(255)        NOT NULL,
    message     CLOB        NOT NULL,
    type        VARCHAR(50)        NOT NULL DEFAULT 'INFO',
    lu          BOOLEAN     DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
(RANDOM_UUID(), 'admin@hericonsent.fr', '$2b$12$3NNOkZ9fmEICShHs3rdTF.E4iL1t2kt6avMi/wn4RrhtOAEfkqGie', 'ROLE_ADMIN'),
(RANDOM_UUID(), 'notaire@hericonsent.fr', '$2b$12$3NNOkZ9fmEICShHs3rdTF.E4iL1t2kt6avMi/wn4RrhtOAEfkqGie', 'ROLE_NOTAIRE');
