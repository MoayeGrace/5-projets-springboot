-- =====================================================================
--  PROJET 6 — LA PLACE DE MARCHÉ
--  Schéma de la base de données (MySQL 8)
--  À placer dans : src/main/resources/schema.sql
-- =====================================================================

-- On supprime dans l'ordre inverse des dépendances (enfants d'abord)
DROP TABLE IF EXISTS commande;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS produit;
DROP TABLE IF EXISTS categorie;
DROP TABLE IF EXISTS vendeur;


-- ---------------------------------------------------------------------
-- 1. VENDEUR
--    "raison sociale, email et ville"
-- ---------------------------------------------------------------------
CREATE TABLE vendeur (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    raison_sociale  VARCHAR(150)  NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    ville           VARCHAR(100)  NOT NULL,

    CONSTRAINT pk_vendeur        PRIMARY KEY (id),
    CONSTRAINT uk_vendeur_email  UNIQUE (email)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 2. CATEGORIE
--    "libellé et description"
-- ---------------------------------------------------------------------
CREATE TABLE categorie (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    libelle      VARCHAR(100)  NOT NULL,
    description  VARCHAR(500),

    CONSTRAINT pk_categorie          PRIMARY KEY (id),
    CONSTRAINT uk_categorie_libelle  UNIQUE (libelle)   -- deux catégories ne peuvent pas avoir le même libellé
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 3. PRODUIT
--    "nom, description, prix et stock, rattaché à un vendeur et à une catégorie"
-- ---------------------------------------------------------------------
CREATE TABLE produit (
    id                     BIGINT         NOT NULL AUTO_INCREMENT,
    nom                    VARCHAR(150)   NOT NULL,
    description            VARCHAR(1000),
    prix                   DECIMAL(12,2)  NOT NULL,
    stock                  INT            NOT NULL DEFAULT 0,
    vendeur_id             BIGINT         NOT NULL,
    categorie_id           BIGINT         NOT NULL,

    -- F16 : "un produit déjà signalé n'est pas signalé une seconde fois avant réapprovisionnement"
    alerte_stock_envoyee   BOOLEAN        NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_produit            PRIMARY KEY (id),
    CONSTRAINT ck_produit_prix       CHECK (prix >= 0),
    CONSTRAINT ck_produit_stock      CHECK (stock >= 0),      -- le stock ne peut jamais devenir négatif
    CONSTRAINT fk_produit_vendeur    FOREIGN KEY (vendeur_id)
        REFERENCES vendeur (id)
        ON DELETE RESTRICT,                                    -- un vendeur qui a des produits ne peut pas être supprimé directement
    CONSTRAINT fk_produit_categorie  FOREIGN KEY (categorie_id)
        REFERENCES categorie (id)
        ON DELETE RESTRICT                                     -- "supprime une catégorie VIDE"
) ENGINE = InnoDB;

-- Index utiles aux recherches
CREATE INDEX idx_produit_nom             ON produit (nom);                 -- F6 (tri par nom), F9 (recherche par nom)
CREATE INDEX idx_produit_categorie_prix  ON produit (categorie_id, prix);  -- F7 (catégorie triée par prix)
CREATE INDEX idx_produit_prix            ON produit (prix);                -- F9, F10 (tri / filtre par prix)


-- ---------------------------------------------------------------------
-- 4. CLIENT
--    "nom, email et adresse de livraison"
-- ---------------------------------------------------------------------
CREATE TABLE client (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    nom                VARCHAR(150)  NOT NULL,
    email              VARCHAR(150)  NOT NULL,
    adresse_livraison  VARCHAR(300)  NOT NULL,

    CONSTRAINT pk_client        PRIMARY KEY (id),
    CONSTRAINT uk_client_email  UNIQUE (email)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 5. COMMANDE
--    "le client, le produit, la quantité et le statut"
-- ---------------------------------------------------------------------
CREATE TABLE commande (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    client_id       BIGINT         NOT NULL,
    produit_id      BIGINT         NOT NULL,
    quantite        INT            NOT NULL,

    -- F15 : le prix est COPIÉ depuis le produit au moment de la commande, puis ne bouge plus
    prix_unitaire   DECIMAL(12,2)  NOT NULL,

    -- F13 : statut stocké en texte -> correspond à @Enumerated(EnumType.STRING) côté Java
    statut          VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE',

    -- Date de la dernière modification de la commande (n'importe quel attribut).
    -- Remplie à la création, puis mise à jour automatiquement par MySQL à chaque UPDATE.
    date_modification  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_commande           PRIMARY KEY (id),
    CONSTRAINT ck_commande_quantite  CHECK (quantite > 0),
    CONSTRAINT ck_commande_prix      CHECK (prix_unitaire >= 0),
    CONSTRAINT ck_commande_statut    CHECK (statut IN ('EN_ATTENTE', 'PAYEE', 'EXPEDIEE', 'LIVREE', 'ANNULEE')),
    CONSTRAINT fk_commande_client    FOREIGN KEY (client_id)
        REFERENCES client (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_commande_produit   FOREIGN KEY (produit_id)
        REFERENCES produit (id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_commande_client_date  ON commande (client_id, date_modification);  -- F8
CREATE INDEX idx_commande_statut       ON commande (statut);                    -- F16, F19
CREATE INDEX idx_commande_produit      ON commande (produit_id);                -- F17, F18
