-- =====================================================================
--  Données de test — à placer dans : src/main/resources/data.sql
-- =====================================================================

-- VENDEURS
INSERT INTO vendeur (raison_sociale, email, ville) VALUES
('Audio Pro SARL',     'contact@audiopro.fr',    'Lyon'),
('Maison & Déco',      'bonjour@maisondeco.fr',  'Nantes'),
('Vendeur Sans Stock', 'vide@exemple.fr',        'Lille');   -- F6 : vendeur sans produit -> liste vide

-- CATEGORIES
INSERT INTO categorie (libelle, description) VALUES
('Audio',        'Casques, enceintes, micros'),                  -- id 1
('Informatique', 'Ordinateurs et accessoires'),                  -- id 2
('Cuisine',      'Ustensiles et électroménager'),                -- id 3
('Jardin',       'Catégorie vide pour tester la suppression');   -- id 4

-- PRODUITS
INSERT INTO produit (nom, description, prix, stock, vendeur_id, categorie_id) VALUES
('Casque Bluetooth X200', 'Réduction de bruit active', 129.90, 12, 1, 1),
('Casque filaire Basic',  'Léger et pliable',           19.90,  3, 1, 1),   -- stock faible (F16)
('Enceinte Nomade',       'Étanche, 10h d''autonomie',  59.00,  0, 1, 1),   -- rupture (F7, F11)
('Bouilloire Inox',       '1,7 L',                      34.50, 25, 2, 3),
('Lot de 3 poêles',       'Revêtement céramique',       79.00,  5, 2, 3);   -- stock = 5 -> signalé (F16)

-- CLIENTS
INSERT INTO client (nom, email, adresse_livraison) VALUES
('Alice Martin', 'alice@mail.fr', '12 rue des Lilas, 69003 Lyon'),
('Bruno Petit',  'bruno@mail.fr', '4 avenue Foch, 44000 Nantes'),
('Chloé Durand', 'chloe@mail.fr', '8 place du Marché, 59000 Lille');  -- F8 : client sans commande

-- COMMANDES (prix_unitaire = prix du produit au moment de la commande)
INSERT INTO commande (client_id, produit_id, quantite, prix_unitaire, statut, date_modification) VALUES
(1, 1, 1, 129.90, 'LIVREE',     '2026-11-05 17:30:00'),
(1, 4, 2,  34.50, 'PAYEE',      '2026-11-15 14:05:00'),
(2, 2, 1,  19.90, 'EN_ATTENTE', '2026-11-20 09:30:00'),
(2, 5, 1,  79.00, 'ANNULEE',    '2026-11-11 09:00:00');
