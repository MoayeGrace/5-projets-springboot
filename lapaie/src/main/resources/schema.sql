-- =====================================================================
--  PROJET 10 — LA PAIE
--  Schéma de la base de données (MySQL 8)
--  À placer dans : src/main/resources/schema.sql
-- =====================================================================

-- On supprime dans l'ordre inverse des dépendances (enfants d'abord)
DROP TABLE IF EXISTS ligne_bulletin;
DROP TABLE IF EXISTS bulletin;
DROP TABLE IF EXISTS absence;
DROP TABLE IF EXISTS contrat;
DROP TABLE IF EXISTS rubrique;
DROP TABLE IF EXISTS employe;


-- ---------------------------------------------------------------------
-- 1. EMPLOYE
--    "nom, matricule, date d'embauche et service"
-- ---------------------------------------------------------------------
CREATE TABLE employe (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    nom            VARCHAR(150)  NOT NULL,
    matricule      VARCHAR(20)   NOT NULL,
    date_embauche  DATE          NOT NULL,      -- F18 : point de départ des congés acquis
    service        VARCHAR(50)   NOT NULL,      -- F9, F17 : recherche et regroupement par service

    CONSTRAINT pk_employe            PRIMARY KEY (id),
    CONSTRAINT uk_employe_matricule  UNIQUE (matricule)
) ENGINE = InnoDB;

CREATE INDEX idx_employe_service ON employe (service);


-- ---------------------------------------------------------------------
-- 2. CONTRAT
--    "type, dates, poste et salaire de base, rattaché à un employé"
-- ---------------------------------------------------------------------
CREATE TABLE contrat (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    employe_id    BIGINT         NOT NULL,
    type          VARCHAR(20)    NOT NULL,     -- @Enumerated(EnumType.STRING)
    date_debut    DATE           NOT NULL,
    date_fin      DATE           NULL,         -- NULL = contrat sans date de fin (F11 : bloque toute période postérieure)
    poste         VARCHAR(100)   NOT NULL,
    salaire_base  DECIMAL(12,2)  NOT NULL,     -- F13 : salaire de base du bulletin

    CONSTRAINT pk_contrat          PRIMARY KEY (id),
    CONSTRAINT ck_contrat_type     CHECK (type IN ('CDI', 'CDD', 'STAGE', 'INTERIM')),
    CONSTRAINT ck_contrat_dates    CHECK (date_fin IS NULL OR date_debut <= date_fin),
    CONSTRAINT ck_contrat_salaire  CHECK (salaire_base > 0),
    CONSTRAINT fk_contrat_employe  FOREIGN KEY (employe_id)
        REFERENCES employe (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_contrat_employe_debut ON contrat (employe_id, date_debut);   -- F6, F11


-- ---------------------------------------------------------------------
-- 3. RUBRIQUE  (le référentiel : salaire de base, primes, cotisations)
--    "code, libellé, type et taux"
-- ---------------------------------------------------------------------
CREATE TABLE rubrique (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    code          VARCHAR(20)   NOT NULL,
    libelle       VARCHAR(100)  NOT NULL,
    type          VARCHAR(20)   NOT NULL,
    taux          DECIMAL(7,4)  NOT NULL,      -- en %, ex. 6.3000

    -- F13 : "les rubriques sont appliquées dans leur ordre de calcul"
    ordre_calcul  INT           NOT NULL,

    CONSTRAINT pk_rubrique         PRIMARY KEY (id),
    CONSTRAINT uk_rubrique_code    UNIQUE (code),
    CONSTRAINT uk_rubrique_ordre   UNIQUE (ordre_calcul),
    CONSTRAINT ck_rubrique_type    CHECK (type IN ('BASE', 'PRIME', 'COTISATION')),
    CONSTRAINT ck_rubrique_taux    CHECK (taux >= 0)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 4. BULLETIN
--    "l'employé, le mois, le brut et le net"
-- ---------------------------------------------------------------------
CREATE TABLE bulletin (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    employe_id         BIGINT         NOT NULL,
    mois               CHAR(7)        NOT NULL,     -- format 'AAAA-MM', ex. '2026-11'
    brut               DECIMAL(12,2)  NOT NULL DEFAULT 0,
    retenues           DECIMAL(12,2)  NOT NULL DEFAULT 0,
    net                DECIMAL(12,2)  NOT NULL DEFAULT 0,

    -- F14 / F16 : BROUILLON (modifiable) -> EDITE (figé) -> CLOTURE (mois arrêté)
    etat               VARCHAR(20)    NOT NULL DEFAULT 'BROUILLON',

    -- Date de la dernière modification (n'importe quel attribut), gérée par MySQL
    date_modification  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_bulletin           PRIMARY KEY (id),
    CONSTRAINT uk_bulletin_mois      UNIQUE (employe_id, mois),      -- F13 : un seul bulletin par employé et par mois
    CONSTRAINT ck_bulletin_mois      CHECK (mois REGEXP '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    CONSTRAINT ck_bulletin_montants  CHECK (brut >= 0 AND retenues >= 0),
    CONSTRAINT ck_bulletin_etat      CHECK (etat IN ('BROUILLON', 'EDITE', 'CLOTURE')),
    CONSTRAINT fk_bulletin_employe   FOREIGN KEY (employe_id)
        REFERENCES employe (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_bulletin_mois ON bulletin (mois, etat);   -- F10, F16, F17


-- ---------------------------------------------------------------------
-- 5. LIGNE_BULLETIN
--    F13 : "le brut, les retenues et le net sont calculés et conservés ligne par ligne"
--    Table associative entre bulletin et rubrique (relation N:N) :
--      un bulletin contient plusieurs rubriques, une rubrique sert dans plusieurs bulletins
--    F14 : "les taux utilisés sont figés avec lui" -> le libellé et le taux
--          de la rubrique sont aussi COPIÉS dans la ligne au moment du calcul
--    F15 : une ligne de régularisation porte le mois du bulletin corrigé et le motif
--    F19 : détail par rubrique
-- ---------------------------------------------------------------------
CREATE TABLE ligne_bulletin (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    bulletin_id     BIGINT         NOT NULL,
    ordre           INT            NOT NULL,          -- ordre d'affichage / de calcul
    type            VARCHAR(20)    NOT NULL,
    rubrique_id     BIGINT         NULL,              -- NULL pour une ligne d'absence ou de régularisation
    libelle         VARCHAR(150)   NOT NULL,
    base            DECIMAL(12,2)  NULL,              -- montant sur lequel le taux s'applique
    taux            DECIMAL(7,4)   NULL,              -- copie de rubrique.taux au moment du calcul
    montant         DECIMAL(12,2)  NOT NULL,

    -- F15 : uniquement pour type = 'REGULARISATION'
    -- le bulletin corrigé = même employé + ce mois -> pas besoin de clé étrangère
    mois_corrige    CHAR(7)        NULL,
    motif           VARCHAR(300)   NULL,

    CONSTRAINT pk_ligne_bulletin    PRIMARY KEY (id),
    CONSTRAINT uk_ligne_ordre       UNIQUE (bulletin_id, ordre),
    CONSTRAINT ck_ligne_type        CHECK (type IN ('BASE', 'PRIME', 'COTISATION', 'ABSENCE', 'REGULARISATION')),
    CONSTRAINT ck_ligne_regul       CHECK ((type = 'REGULARISATION' AND mois_corrige IS NOT NULL AND motif IS NOT NULL)
                                        OR (type <> 'REGULARISATION' AND mois_corrige IS NULL)),
    CONSTRAINT fk_ligne_bulletin    FOREIGN KEY (bulletin_id)
        REFERENCES bulletin (id)
        ON DELETE CASCADE,               -- supprimer un bulletin (non édité) supprime ses lignes
    CONSTRAINT fk_ligne_rubrique    FOREIGN KEY (rubrique_id)
        REFERENCES rubrique (id)
        ON DELETE RESTRICT               -- une rubrique utilisée dans un bulletin ne peut pas être supprimée
) ENGINE = InnoDB;

CREATE INDEX idx_ligne_rubrique ON ligne_bulletin (rubrique_id);   -- F19 (détail par rubrique)


-- ---------------------------------------------------------------------
-- 6. ABSENCE
--    "type, dates et motif, rattachée à un employé"
-- ---------------------------------------------------------------------
CREATE TABLE absence (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    employe_id  BIGINT        NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    date_debut  DATE          NOT NULL,
    date_fin    DATE          NOT NULL,      -- durée en jours = date_fin - date_debut + 1
    motif       VARCHAR(300)  NULL,

    -- F13 : "les absences non rémunérées réduisent le brut au prorata des jours"
    remuneree   BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_absence          PRIMARY KEY (id),
    CONSTRAINT ck_absence_type     CHECK (type IN ('CONGE_PAYE', 'MALADIE', 'SANS_SOLDE', 'AUTRE')),
    CONSTRAINT ck_absence_dates    CHECK (date_debut <= date_fin),
    CONSTRAINT fk_absence_employe  FOREIGN KEY (employe_id)
        REFERENCES employe (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_absence_employe_dates ON absence (employe_id, date_debut, date_fin);   -- F8, F12, F18
