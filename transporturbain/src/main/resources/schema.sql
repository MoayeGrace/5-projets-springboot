-- =====================================================================
--  PROJET 9 — LE RÉSEAU DE TRANSPORT URBAIN
--  Schéma de la base de données (MySQL 8)
--  À placer dans : src/main/resources/schema.sql
-- =====================================================================

-- On supprime dans l'ordre inverse des dépendances (enfants d'abord)
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS desserte;
DROP TABLE IF EXISTS conducteur;
DROP TABLE IF EXISTS bus;
DROP TABLE IF EXISTS arret;
DROP TABLE IF EXISTS ligne;


-- ---------------------------------------------------------------------
-- 1. LIGNE
--    "numéro, libellé, terminus de départ et terminus d'arrivée"
--    Les terminus sont des textes simples (pas de clé étrangère vers arret)
--    -> sinon ligne serait reliée deux fois à arret : boucle.
-- ---------------------------------------------------------------------
CREATE TABLE ligne (
    id                        BIGINT        NOT NULL AUTO_INCREMENT,
    numero                    VARCHAR(10)   NOT NULL,
    libelle                   VARCHAR(150)  NOT NULL,
    terminus_depart           VARCHAR(100)  NOT NULL,
    terminus_arrivee          VARCHAR(100)  NOT NULL,

    -- F12 : calcul du créneau d'une course
    duree_trajet_minutes      INT           NOT NULL,   -- départ -> arrivée au terminus
    temps_retour_minutes      INT           NOT NULL DEFAULT 0,   -- "temps de retour au terminus inclus"

    CONSTRAINT pk_ligne          PRIMARY KEY (id),
    CONSTRAINT uk_ligne_numero   UNIQUE (numero),
    CONSTRAINT ck_ligne_durees   CHECK (duree_trajet_minutes > 0 AND temps_retour_minutes >= 0)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 2. ARRET
--    "nom, commune et coordonnées"
-- ---------------------------------------------------------------------
CREATE TABLE arret (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    nom        VARCHAR(150)   NOT NULL,
    commune    VARCHAR(100)   NOT NULL,
    latitude   DECIMAL(9,6)   NOT NULL,     -- ex. 5.359951
    longitude  DECIMAL(9,6)   NOT NULL,     -- ex. -4.008256

    CONSTRAINT pk_arret              PRIMARY KEY (id),
    CONSTRAINT uk_arret_nom_commune  UNIQUE (nom, commune),
    CONSTRAINT ck_arret_coordonnees  CHECK (latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180)
) ENGINE = InnoDB;

CREATE INDEX idx_arret_commune ON arret (commune);   -- F9 (recherche par commune)


-- ---------------------------------------------------------------------
-- 3. DESSERTE  (table de liaison ligne <-> arret)
--    Une ligne passe par plusieurs arrêts, un arrêt est desservi par plusieurs lignes.
--    Une ligne = "sur la ligne X, dans le sens S, l'arrêt A est en position P"
--    F6 : ordre de desserte / F9 : lignes qui desservent un arrêt / F11 : positions
-- ---------------------------------------------------------------------
CREATE TABLE desserte (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    ligne_id   BIGINT       NOT NULL,
    arret_id   BIGINT       NOT NULL,
    sens       VARCHAR(10)  NOT NULL,
    position   INT          NOT NULL,      -- 1 = premier arrêt

    CONSTRAINT pk_desserte            PRIMARY KEY (id),
    CONSTRAINT uk_desserte_position   UNIQUE (ligne_id, sens, position),   -- F11 : pas deux arrêts à la même position
    CONSTRAINT uk_desserte_arret      UNIQUE (ligne_id, sens, arret_id),   -- F11 : un arrêt une seule fois par ligne et par sens
    CONSTRAINT ck_desserte_sens       CHECK (sens IN ('ALLER', 'RETOUR')),
    CONSTRAINT ck_desserte_position   CHECK (position > 0),
    CONSTRAINT fk_desserte_ligne      FOREIGN KEY (ligne_id)
        REFERENCES ligne (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_desserte_arret      FOREIGN KEY (arret_id)
        REFERENCES arret (id)
        ON DELETE RESTRICT                     -- un arrêt encore desservi ne peut pas être supprimé
) ENGINE = InnoDB;

CREATE INDEX idx_desserte_arret ON desserte (arret_id);   -- F9


-- ---------------------------------------------------------------------
-- 4. BUS
--    "immatriculation, capacité et dépôt"
-- ---------------------------------------------------------------------
CREATE TABLE bus (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    immatriculation  VARCHAR(20)   NOT NULL,
    capacite         INT           NOT NULL,
    depot            VARCHAR(100)  NOT NULL,

    CONSTRAINT pk_bus                  PRIMARY KEY (id),
    CONSTRAINT uk_bus_immatriculation  UNIQUE (immatriculation),
    CONSTRAINT ck_bus_capacite         CHECK (capacite > 0)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 5. CONDUCTEUR
--    "nom, matricule et numéro de permis"
-- ---------------------------------------------------------------------
CREATE TABLE conducteur (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    nom                    VARCHAR(150)  NOT NULL,
    matricule              VARCHAR(20)   NOT NULL,
    numero_permis          VARCHAR(30)   NOT NULL,

    -- F14 : "la date de validité du permis doit être postérieure à la date de la course"
    date_validite_permis   DATE          NOT NULL,

    -- F16 : "un conducteur déjà signalé n'est pas signalé deux fois"
    alerte_permis_envoyee  BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_conducteur          PRIMARY KEY (id),
    CONSTRAINT uk_conducteur_matricule UNIQUE (matricule),
    CONSTRAINT uk_conducteur_permis   UNIQUE (numero_permis)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 6. COURSE
--    "la ligne, le bus, le conducteur, la date et l'heure de départ"
-- ---------------------------------------------------------------------
CREATE TABLE course (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    ligne_id              BIGINT       NOT NULL,
    bus_id                BIGINT       NOT NULL,
    conducteur_id         BIGINT       NOT NULL,
    date_course           DATE         NOT NULL,
    heure_depart          TIME         NOT NULL,

    -- F12, F18 : heure_depart + ligne.duree_trajet_minutes (calculée par le Service)
    heure_arrivee_prevue  TIME         NOT NULL,

    -- F15, F18 : enregistrée au passage à TERMINEE (NULL avant)
    heure_arrivee_reelle  TIME         NULL,

    -- F15 : statut stocké en texte -> @Enumerated(EnumType.STRING)
    statut                VARCHAR(20)  NOT NULL DEFAULT 'PREVUE',

    -- Date de la dernière modification (n'importe quel attribut), gérée par MySQL
    date_modification     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_course            PRIMARY KEY (id),
    CONSTRAINT ck_course_horaire    CHECK (heure_depart < heure_arrivee_prevue),
    CONSTRAINT ck_course_statut     CHECK (statut IN ('PREVUE', 'EN_COURS', 'TERMINEE', 'ANNULEE')),
    CONSTRAINT fk_course_ligne      FOREIGN KEY (ligne_id)
        REFERENCES ligne (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_course_bus        FOREIGN KEY (bus_id)
        REFERENCES bus (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_course_conducteur FOREIGN KEY (conducteur_id)
        REFERENCES conducteur (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_course_ligne_date      ON course (ligne_id, date_course, heure_depart);       -- F7, F17, F18
CREATE INDEX idx_course_bus_date        ON course (bus_id, date_course, heure_depart);         -- F12, F19
CREATE INDEX idx_course_conducteur_date ON course (conducteur_id, date_course, heure_depart);  -- F8, F13
