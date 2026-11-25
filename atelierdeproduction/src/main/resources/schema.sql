-- =====================================================================
--  PROJET 8 — L'ATELIER DE PRODUCTION
--  Schéma de la base de données (MySQL 8)
--  À placer dans : src/main/resources/schema.sql
-- =====================================================================

-- On supprime dans l'ordre inverse des dépendances (enfants d'abord)
DROP TABLE IF EXISTS ordre_fabrication;
DROP TABLE IF EXISTS ligne_nomenclature;
DROP TABLE IF EXISTS operateur;
DROP TABLE IF EXISTS machine;
DROP TABLE IF EXISTS article;


-- ---------------------------------------------------------------------
-- 1. ARTICLE
--    "référence, libellé, unité et type" (acheté ou fabriqué)
-- ---------------------------------------------------------------------
CREATE TABLE article (
    id                      BIGINT         NOT NULL AUTO_INCREMENT,
    reference               VARCHAR(30)    NOT NULL,
    libelle                 VARCHAR(150)   NOT NULL,
    unite                   VARCHAR(20)    NOT NULL,              -- ex. PIECE, KG, METRE
    type                    VARCHAR(20)    NOT NULL,              -- @Enumerated(EnumType.STRING) côté Java

    -- F12 : "le créneau calculé à partir de la gamme et de la quantité"
    -- temps machine pour produire UNE unité (NULL pour un article acheté)
    duree_unitaire_minutes  INT            NULL,

    -- F14 : disponibilité des composants achetables
    quantite_stock          DECIMAL(12,3)  NOT NULL DEFAULT 0,
    quantite_reservee       DECIMAL(12,3)  NOT NULL DEFAULT 0,    -- réservée au lancement des ordres
    -- Disponible = quantite_stock - quantite_reservee

    CONSTRAINT pk_article            PRIMARY KEY (id),
    CONSTRAINT uk_article_reference  UNIQUE (reference),
    CONSTRAINT ck_article_type       CHECK (type IN ('ACHETE', 'FABRIQUE')),
    CONSTRAINT ck_article_duree      CHECK (duree_unitaire_minutes IS NULL OR duree_unitaire_minutes > 0),
    CONSTRAINT ck_article_stock      CHECK (quantite_stock >= 0 AND quantite_reservee >= 0
                                        AND quantite_reservee <= quantite_stock)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 2. LIGNE_NOMENCLATURE
--    "l'article composé, le composant et la quantité"
--    Une ligne = "pour fabriquer 1 article_compose, il faut <quantite> du composant <composant_reference>"
--    Une seule clé étrangère vers article (l'article composé).
--    Le composant est désigné par sa référence, en texte simple, SANS clé étrangère
--    -> pas de double lien vers article, donc pas de boucle dans le schéma.
--    Le Service vérifie que la référence existe dans article.
-- ---------------------------------------------------------------------
CREATE TABLE ligne_nomenclature (
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    article_compose_id    BIGINT         NOT NULL,
    composant_reference   VARCHAR(30)    NOT NULL,     -- = article.reference du composant
    quantite              DECIMAL(12,3)  NOT NULL,

    -- F15 : versionnement de la nomenclature
    version               INT            NOT NULL DEFAULT 1,
    date_application      DATE           NULL,     -- NULL = version encore en préparation

    CONSTRAINT pk_ligne_nomenclature   PRIMARY KEY (id),
    CONSTRAINT uk_ligne_nomenclature   UNIQUE (article_compose_id, composant_reference, version),  -- un composant une seule fois par version
    CONSTRAINT ck_ligne_quantite       CHECK (quantite > 0),
    CONSTRAINT ck_ligne_version        CHECK (version > 0),
    CONSTRAINT fk_ligne_compose        FOREIGN KEY (article_compose_id)
        REFERENCES article (id)
        ON DELETE RESTRICT                       -- un article qui a une nomenclature ne peut pas être supprimé
) ENGINE = InnoDB;

CREATE INDEX idx_ligne_composant ON ligne_nomenclature (composant_reference);   -- F7 (où ce composant est-il utilisé ?)


-- ---------------------------------------------------------------------
-- 3. MACHINE
--    "code, type et atelier"
-- ---------------------------------------------------------------------
CREATE TABLE machine (
    id                         BIGINT        NOT NULL AUTO_INCREMENT,
    code                       VARCHAR(30)   NOT NULL,
    type                       VARCHAR(50)   NOT NULL,
    atelier                    VARCHAR(50)   NOT NULL,     -- F16 : une alerte par atelier

    -- F19 : "heures occupées rapportées aux heures ouvrables, hors arrêts de maintenance"
    heures_ouvrables_par_jour  INT           NOT NULL DEFAULT 8,
    debut_maintenance          DATETIME      NULL,          -- arrêt de maintenance prévu (NULL = aucun)
    fin_maintenance            DATETIME      NULL,

    CONSTRAINT pk_machine           PRIMARY KEY (id),
    CONSTRAINT uk_machine_code      UNIQUE (code),
    CONSTRAINT ck_machine_heures    CHECK (heures_ouvrables_par_jour BETWEEN 1 AND 24),
    CONSTRAINT ck_machine_maint     CHECK (fin_maintenance IS NULL OR debut_maintenance < fin_maintenance)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 4. OPERATEUR
--    "nom, matricule et équipe"
-- ---------------------------------------------------------------------
CREATE TABLE operateur (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    nom        VARCHAR(150)  NOT NULL,
    matricule  VARCHAR(20)   NOT NULL,
    equipe     VARCHAR(50)   NOT NULL,

    CONSTRAINT pk_operateur            PRIMARY KEY (id),
    CONSTRAINT uk_operateur_matricule  UNIQUE (matricule)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 5. ORDRE_FABRICATION
--    "l'article, la quantité, la date prévue, la machine et l'opérateur"
-- ---------------------------------------------------------------------
CREATE TABLE ordre_fabrication (
    id                     BIGINT         NOT NULL AUTO_INCREMENT,
    article_id             BIGINT         NOT NULL,
    quantite               DECIMAL(12,3)  NOT NULL,
    machine_id             BIGINT         NOT NULL,
    operateur_id           BIGINT         NOT NULL,

    -- F12 : le créneau occupé sur la machine
    date_prevue            DATETIME       NOT NULL,     -- début du créneau
    date_fin_prevue        DATETIME       NOT NULL,     -- = date_prevue + quantite × duree_unitaire_minutes (calculée par le Service)

    -- F13 : statut stocké en texte -> @Enumerated(EnumType.STRING)
    statut                 VARCHAR(20)    NOT NULL DEFAULT 'PLANIFIE',

    -- F15 : version de nomenclature utilisée par l'ordre (fixée au lancement)
    version_nomenclature   INT            NULL,

    -- F16 : "le retard cumulé est conservé sur l'ordre"
    retard_jours           INT            NOT NULL DEFAULT 0,

    -- Date de la dernière modification (n'importe quel attribut), gérée par MySQL
    date_modification      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_ordre_fabrication  PRIMARY KEY (id),
    CONSTRAINT ck_ordre_quantite     CHECK (quantite > 0),
    CONSTRAINT ck_ordre_creneau      CHECK (date_prevue < date_fin_prevue),
    CONSTRAINT ck_ordre_statut       CHECK (statut IN ('PLANIFIE', 'LANCE', 'TERMINE', 'ANNULE')),
    CONSTRAINT ck_ordre_retard       CHECK (retard_jours >= 0),
    CONSTRAINT fk_ordre_article      FOREIGN KEY (article_id)
        REFERENCES article (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ordre_machine      FOREIGN KEY (machine_id)
        REFERENCES machine (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_ordre_operateur    FOREIGN KEY (operateur_id)
        REFERENCES operateur (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_ordre_machine_date ON ordre_fabrication (machine_id, date_prevue);   -- F9, F12, F19
CREATE INDEX idx_ordre_statut_date  ON ordre_fabrication (statut, date_prevue);       -- F8, F16
