-- =====================================================================
--  Données de test — à placer dans : src/main/resources/data.sql
--  Exemple : un réseau à Abidjan
-- =====================================================================

-- LIGNES
INSERT INTO ligne (numero, libelle, terminus_depart, terminus_arrivee, duree_trajet_minutes, temps_retour_minutes) VALUES
('81', 'Riviera 2 - Plateau',   'Riviera 2',         'Plateau Cité Administrative', 40, 10),   -- id 1
('12', 'Yopougon - Plateau',    'Yopougon Siporex',  'Plateau Cité Administrative', 50, 15),   -- id 2
('99', 'Ligne en préparation',  'Abobo Gare',        'Adjamé Liberté',              35, 10);   -- id 3 : sans arrêt (F6)

-- ARRETS
INSERT INTO arret (nom, commune, latitude, longitude) VALUES
('Riviera 2',                   'Cocody',   5.367500, -3.965000),   -- id 1
('Cocody Saint-Jean',           'Cocody',   5.335600, -3.993400),   -- id 2
('Plateau Cité Administrative', 'Plateau',  5.325800, -4.019500),   -- id 3 : desservi par 2 lignes (F9)
('Adjamé Liberté',              'Adjamé',   5.354300, -4.024100),   -- id 4
('Yopougon Siporex',            'Yopougon', 5.339800, -4.077900),   -- id 5
('Abobo Gare',                  'Abobo',    5.418600, -4.020700);   -- id 6 : desservi par aucune ligne

-- DESSERTES (ordre des arrêts par ligne et par sens)
INSERT INTO desserte (ligne_id, arret_id, sens, position) VALUES
(1, 1, 'ALLER',  1),
(1, 2, 'ALLER',  2),
(1, 3, 'ALLER',  3),
(1, 3, 'RETOUR', 1),
(1, 2, 'RETOUR', 2),
(1, 1, 'RETOUR', 3),
(2, 5, 'ALLER',  1),
(2, 4, 'ALLER',  2),
(2, 3, 'ALLER',  3);

-- BUS
INSERT INTO bus (immatriculation, capacite, depot) VALUES
('AB-1234-CI', 90, 'Dépôt Cocody'),     -- id 1
('AB-5678-CI', 90, 'Dépôt Yopougon'),   -- id 2
('AB-9012-CI', 60, 'Dépôt Cocody'),     -- id 3
('AB-3456-CI', 60, 'Dépôt Abobo');      -- id 4 : sans course (F19)

-- CONDUCTEURS (aujourd'hui = 25/11/2026)
INSERT INTO conducteur (nom, matricule, numero_permis, date_validite_permis) VALUES
('Yao Kouassi',     'CD-001', 'P-CI-778899', '2027-01-10'),   -- expire dans moins de 60 jours (F16)
('Adjoua Konan',    'CD-002', 'P-CI-112233', '2029-06-30'),
('Ibrahim Traoré',  'CD-003', 'P-CI-445566', '2026-10-31');   -- permis expiré -> non affectable (F14)

-- COURSES
-- heure_arrivee_prevue = heure_depart + duree_trajet_minutes de la ligne
INSERT INTO course (ligne_id, bus_id, conducteur_id, date_course, heure_depart, heure_arrivee_prevue, heure_arrivee_reelle, statut) VALUES
(1, 1, 1, '2026-11-15', '06:00:00', '06:40:00', '06:43:00', 'TERMINEE'),   -- 3 min de retard : ponctuelle (F18)
(1, 1, 1, '2026-11-15', '07:00:00', '07:40:00', '07:52:00', 'TERMINEE'),   -- 12 min de retard : non ponctuelle
(2, 2, 2, '2026-11-15', '06:30:00', '07:20:00', '07:21:00', 'TERMINEE'),
(2, 2, 2, '2026-11-15', '08:00:00', '08:50:00', NULL,       'ANNULEE'),    -- exclue des calculs (F17, F18, F19)
(1, 1, 1, '2026-11-25', '07:00:00', '07:40:00', NULL,       'EN_COURS'),
(1, 3, 2, '2026-11-26', '08:00:00', '08:40:00', NULL,       'PREVUE');
