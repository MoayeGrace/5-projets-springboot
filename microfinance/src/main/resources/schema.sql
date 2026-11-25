-- =====================================================================
--  PROJET 7 — LE CRÉDIT EN MICROFINANCE
--  Schéma de la base de données (MySQL 8)
--  À placer dans : src/main/resources/schema.sql
-- =====================================================================

-- On supprime dans l'ordre inverse des dépendances (enfants d'abord)
DROP TABLE IF EXISTS paiement;
DROP TABLE IF EXISTS echeance;
DROP TABLE IF EXISTS pret;
DROP TABLE IF EXISTS compte;
DROP TABLE IF EXISTS client;


-- ---------------------------------------------------------------------
-- 1. CLIENT
--    "nom, téléphone et adresse"
-- ---------------------------------------------------------------------
CREATE TABLE client (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(150)   NOT NULL,
    telephone       VARCHAR(30)    NOT NULL,
    adresse         VARCHAR(300)   NOT NULL,

    -- F12 : "le total emprunté ne peut pas dépasser le plafond attaché à son profil"
    plafond_emprunt DECIMAL(14,2)  NOT NULL,

    -- F16 : "une alerte est produite par agent" -> l'agent de crédit qui suit le client
    agent_credit    VARCHAR(150)   NOT NULL,

    CONSTRAINT pk_client            PRIMARY KEY (id),
    CONSTRAINT uk_client_telephone  UNIQUE (telephone),
    CONSTRAINT ck_client_plafond    CHECK (plafond_emprunt >= 0)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 2. COMPTE
--    "numéro, type et date d'ouverture, rattaché à un client"
-- ---------------------------------------------------------------------
CREATE TABLE compte (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    numero          VARCHAR(30)    NOT NULL,
    type            VARCHAR(20)    NOT NULL,   -- @Enumerated(EnumType.STRING) côté Java
    date_ouverture  DATE           NOT NULL,
    solde           DECIMAL(14,2)  NOT NULL DEFAULT 0,   -- F6 : "avec leur type et leur solde"
    client_id       BIGINT         NOT NULL,

    CONSTRAINT pk_compte          PRIMARY KEY (id),
    CONSTRAINT uk_compte_numero   UNIQUE (numero),
    CONSTRAINT ck_compte_type     CHECK (type IN ('COURANT', 'EPARGNE')),
    CONSTRAINT fk_compte_client   FOREIGN KEY (client_id)
        REFERENCES client (id)
        ON DELETE RESTRICT                       -- un client qui a des comptes ne peut pas être supprimé directement
) ENGINE = InnoDB;

CREATE INDEX idx_compte_client_date ON compte (client_id, date_ouverture);   -- F6 (tri par date d'ouverture)


-- ---------------------------------------------------------------------
-- 3. PRET
--    "montant, durée, taux et date d'accord, rattaché à un client"
-- ---------------------------------------------------------------------
CREATE TABLE pret (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    montant            DECIMAL(14,2)  NOT NULL,
    duree_mois         INT            NOT NULL,              -- F11 : une échéance par mois
    taux               DECIMAL(5,2)   NOT NULL,              -- taux annuel en %, ex. 12.00
    date_accord        DATE           NOT NULL,
    statut             VARCHAR(20)    NOT NULL DEFAULT 'ACCORDE',
    client_id          BIGINT         NOT NULL,

    -- F15 : "la date de solde est enregistrée" (NULL tant que le prêt n'est pas soldé)
    date_solde         DATE           NULL,

    -- F16 : "le nombre de jours de retard maximal atteint est conservé sur le prêt"
    jours_retard_max   INT            NOT NULL DEFAULT 0,

    CONSTRAINT pk_pret           PRIMARY KEY (id),
    CONSTRAINT ck_pret_montant   CHECK (montant > 0),
    CONSTRAINT ck_pret_duree     CHECK (duree_mois > 0),
    CONSTRAINT ck_pret_taux      CHECK (taux >= 0),
    CONSTRAINT ck_pret_statut    CHECK (statut IN ('ACCORDE', 'EN_COURS', 'SOLDE', 'ABANDONNE')),
    CONSTRAINT fk_pret_client    FOREIGN KEY (client_id)
        REFERENCES client (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_pret_client_date ON pret (client_id, date_accord);   -- F7 (tri par date d'accord)
CREATE INDEX idx_pret_statut      ON pret (statut);                   -- F10, F12


-- ---------------------------------------------------------------------
-- 4. ECHEANCE
--    "date, part de capital, part d'intérêts et montant total, rattachée à un prêt"
--    + ce qui a déjà été payé sur cette échéance (F8 : "le payé et le reste à payer")
--    Reste à payer = (capital + interets + penalites) - (capital_paye + interets_paye + penalites_paye)
-- ---------------------------------------------------------------------
CREATE TABLE echeance (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    pret_id          BIGINT         NOT NULL,
    date_echeance    DATE           NOT NULL,
    capital          DECIMAL(14,2)  NOT NULL,
    interets         DECIMAL(14,2)  NOT NULL,
    montant_total    DECIMAL(14,2)  NOT NULL,              -- capital + interets

    -- F14 : pénalités de retard calculées, ajoutées au dû
    penalites        DECIMAL(14,2)  NOT NULL DEFAULT 0,

    -- F13 : ce qui a été remboursé sur cette échéance, poste par poste
    capital_paye     DECIMAL(14,2)  NOT NULL DEFAULT 0,
    interets_paye    DECIMAL(14,2)  NOT NULL DEFAULT 0,
    penalites_paye   DECIMAL(14,2)  NOT NULL DEFAULT 0,

    CONSTRAINT pk_echeance           PRIMARY KEY (id),
    CONSTRAINT uk_echeance_pret_date UNIQUE (pret_id, date_echeance),   -- pas deux échéances le même jour pour un prêt
    CONSTRAINT ck_echeance_montants  CHECK (capital >= 0 AND interets >= 0 AND penalites >= 0),
    CONSTRAINT ck_echeance_total     CHECK (montant_total = capital + interets),
    CONSTRAINT ck_echeance_paye      CHECK (capital_paye   BETWEEN 0 AND capital
                                        AND interets_paye  BETWEEN 0 AND interets
                                        AND penalites_paye BETWEEN 0 AND penalites),   -- on ne paie jamais plus que le dû
    CONSTRAINT fk_echeance_pret      FOREIGN KEY (pret_id)
        REFERENCES pret (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_echeance_date ON echeance (date_echeance);   -- F16, F18 (échéances en retard à une date)


-- ---------------------------------------------------------------------
-- 5. PAIEMENT
--    "date, montant et mode de règlement, rattaché à un prêt"
--    + l'imputation retenue (F9, F13) : comment le montant a été réparti
--    Règle : part_penalites + part_interets + part_capital = montant
-- ---------------------------------------------------------------------
CREATE TABLE paiement (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    pret_id          BIGINT         NOT NULL,
    date_paiement    DATE           NOT NULL,
    montant          DECIMAL(14,2)  NOT NULL,
    mode_reglement   VARCHAR(20)    NOT NULL,

    -- F13 : d'abord les pénalités, puis les intérêts, puis le capital
    part_penalites   DECIMAL(14,2)  NOT NULL DEFAULT 0,
    part_interets    DECIMAL(14,2)  NOT NULL DEFAULT 0,
    part_capital     DECIMAL(14,2)  NOT NULL DEFAULT 0,

    CONSTRAINT pk_paiement          PRIMARY KEY (id),
    CONSTRAINT ck_paiement_montant  CHECK (montant > 0),                -- F13 : "400 si le montant n'est pas positif"
    CONSTRAINT ck_paiement_mode     CHECK (mode_reglement IN ('ESPECES', 'VIREMENT', 'CHEQUE', 'MOBILE_MONEY')),
    CONSTRAINT ck_paiement_parts    CHECK (part_penalites >= 0 AND part_interets >= 0 AND part_capital >= 0),
    CONSTRAINT fk_paiement_pret     FOREIGN KEY (pret_id)
        REFERENCES pret (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_paiement_pret_date ON paiement (pret_id, date_paiement);   -- F9 (tri par date)
