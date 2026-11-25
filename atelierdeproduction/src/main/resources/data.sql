-- =====================================================================
--  Données de test — à placer dans : src/main/resources/data.sql
--  Exemple : un atelier qui fabrique des vélos
-- =====================================================================

-- ARTICLES
INSERT INTO article (reference, libelle, unite, type, duree_unitaire_minutes, quantite_stock, quantite_reservee) VALUES
('VEL-001', 'Vélo de ville',       'PIECE', 'FABRIQUE', 45,     0.000,   0.000),   -- id 1
('ROU-001', 'Roue 28 pouces',      'PIECE', 'FABRIQUE', 20,    20.000,   0.000),   -- id 2
('CAD-001', 'Cadre aluminium',     'PIECE', 'ACHETE',   NULL,  25.000,  10.000),   -- id 3
('SEL-001', 'Selle confort',       'PIECE', 'ACHETE',   NULL,  15.000,  10.000),   -- id 4 : 5 disponibles -> F14
('JAN-001', 'Jante 28 pouces',     'PIECE', 'ACHETE',   NULL,  30.000,  20.000),   -- id 5
('RAY-001', 'Rayon inox',          'PIECE', 'ACHETE',   NULL, 1000.000, 640.000),  -- id 6
('PNE-001', 'Pneu 28 pouces',      'PIECE', 'ACHETE',   NULL,  30.000,  20.000),   -- id 7
('VIS-001', 'Vis M5',              'PIECE', 'ACHETE',   NULL, 500.000,   0.000);   -- id 8 : utilisé nulle part (F7)

-- NOMENCLATURES
-- Version 1 (en vigueur depuis le 01/01/2026)
--   Vélo = 1 cadre + 2 roues + 1 selle
--   Roue = 1 jante + 32 rayons + 1 pneu
INSERT INTO ligne_nomenclature (article_compose_id, composant_reference, quantite, version, date_application) VALUES
(1, 'CAD-001',  1.000, 1, '2026-01-01'),
(1, 'ROU-001',  2.000, 1, '2026-01-01'),
(1, 'SEL-001',  1.000, 1, '2026-01-01'),
(2, 'JAN-001',  1.000, 1, '2026-01-01'),
(2, 'RAY-001', 32.000, 1, '2026-01-01'),
(2, 'PNE-001',  1.000, 1, '2026-01-01');

-- Version 2 de la roue, en préparation (36 rayons) -> F15 : pas de 2e version en préparation possible
INSERT INTO ligne_nomenclature (article_compose_id, composant_reference, quantite, version, date_application) VALUES
(2, 'JAN-001',  1.000, 2, NULL),
(2, 'RAY-001', 36.000, 2, NULL),
(2, 'PNE-001',  1.000, 2, NULL);

-- MACHINES
INSERT INTO machine (code, type, atelier, heures_ouvrables_par_jour, debut_maintenance, fin_maintenance) VALUES
('M-01', 'Banc d''assemblage', 'Atelier A', 8, NULL, NULL),                                    -- id 1
('M-02', 'Monteuse de roues',  'Atelier B', 8, NULL, NULL),                                    -- id 2
('M-03', 'Presse',             'Atelier A', 8, '2026-11-23 08:00:00', '2026-11-24 16:00:00');  -- id 3 : sans ordre, en maintenance (F19)

-- OPERATEURS
INSERT INTO operateur (nom, matricule, equipe) VALUES
('Karim Benali',   'OP-101', 'Matin'),
('Julie Moreau',   'OP-102', 'Matin'),
('Thomas Lefèvre', 'OP-103', 'Après-midi');

-- ORDRES DE FABRICATION
-- date_fin_prevue = date_prevue + quantite × duree_unitaire_minutes
INSERT INTO ordre_fabrication (article_id, quantite, machine_id, operateur_id, date_prevue, date_fin_prevue, statut, version_nomenclature, retard_jours) VALUES
(2, 20, 2, 1, '2026-11-16 08:00:00', '2026-11-16 14:40:00', 'TERMINE',  1, 0),   -- 20 × 20 min = 400 min
(1, 10, 1, 2, '2026-11-20 08:00:00', '2026-11-20 15:30:00', 'LANCE',    1, 5),   -- 10 × 45 min ; date passée, non terminé -> en retard (F16)
(1, 12, 1, 2, '2026-11-30 08:00:00', '2026-11-30 17:00:00', 'PLANIFIE', NULL, 0),-- 12 selles nécessaires, 5 disponibles -> lancement refusé (F14)
(2, 10, 2, 3, '2026-11-27 08:00:00', '2026-11-27 11:20:00', 'ANNULE',   NULL, 0);-- annulé : ne bloque pas la machine (F12)
