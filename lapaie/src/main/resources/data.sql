-- =====================================================================
--  Données de test — à placer dans : src/main/resources/data.sql
-- =====================================================================

-- EMPLOYES
INSERT INTO employe (nom, matricule, date_embauche, service) VALUES
('Sarah Lambert',  'EMP001', '2024-01-01', 'TECHNIQUE'),    -- id 1
('Hugo Mercier',   'EMP002', '2025-09-01', 'COMPTABILITE'), -- id 2
('Nina Rousseau',  'EMP003', '2026-11-20', 'TECHNIQUE');    -- id 3 : sans contrat (F6)

-- CONTRATS
INSERT INTO contrat (employe_id, type, date_debut, date_fin, poste, salaire_base) VALUES
(1, 'CDD', '2024-01-01', '2024-12-31', 'Développeuse junior', 2000.00),   -- terminé
(1, 'CDI', '2025-01-01', NULL,         'Développeuse',        2500.00),   -- actif (avenant : CDD terminé puis CDI créé, F11)
(2, 'CDI', '2025-09-01', NULL,         'Comptable',           2200.00);   -- actif

-- RUBRIQUES (appliquées dans l'ordre de calcul) : id 1 à 5
INSERT INTO rubrique (code, libelle, type, taux, ordre_calcul) VALUES
('SAL_BASE',  'Salaire de base',            'BASE',       100.0000, 1),   -- id 1
('PRIME_ANC', 'Prime d''ancienneté',        'PRIME',        5.0000, 2),   -- id 2 : 5 % du salaire de base
('COT_RET',   'Cotisation retraite',        'COTISATION',   6.3000, 3),   -- id 3 : 6,3 % du brut
('COT_SAN',   'Cotisation santé',           'COTISATION',   2.0000, 4),   -- id 4 : 2 % du brut
('PRIME_EXC', 'Prime exceptionnelle 2024',  'PRIME',       10.0000, 5);   -- id 5 : obsolète, utilisée nulle part

-- BULLETINS
-- Septembre : mois clôturé ; octobre : édités ; novembre : brouillon pour EMP001 seulement
-- (EMP002 n'a pas de bulletin de novembre -> la clôture de novembre est refusée, F16)
INSERT INTO bulletin (employe_id, mois, brut, retenues, net, etat) VALUES
(1, '2026-09', 2625.00, 217.88, 2407.12, 'CLOTURE'),     -- id 1
(2, '2026-09', 2310.00, 191.73, 2118.27, 'CLOTURE'),     -- id 2
(1, '2026-10', 2625.00, 217.88, 2407.12, 'EDITE'),       -- id 3
(2, '2026-10', 2310.00, 191.73, 2118.27, 'EDITE'),       -- id 4
(1, '2026-11', 2450.00, 203.35, 2296.65, 'BROUILLON');   -- id 5 : absence sans solde + régularisation d'octobre

-- LIGNES DE BULLETIN
-- EMP001 : base 2500 + prime 5 % (125) = brut 2625 ; retraite 6,3 % = 165,38 ; santé 2 % = 52,50
INSERT INTO ligne_bulletin (bulletin_id, ordre, type, rubrique_id, libelle, base, taux, montant) VALUES
(1, 1, 'BASE',       1,           'Salaire de base',      2500.00, 100.0000, 2500.00),
(1, 2, 'PRIME',      2,           'Prime d''ancienneté',  2500.00,   5.0000,  125.00),
(1, 3, 'COTISATION', 3,           'Cotisation retraite',  2625.00,   6.3000,  165.38),
(1, 4, 'COTISATION', 4,           'Cotisation santé',     2625.00,   2.0000,   52.50),
(3, 1, 'BASE',       1,           'Salaire de base',      2500.00, 100.0000, 2500.00),
(3, 2, 'PRIME',      2,           'Prime d''ancienneté',  2500.00,   5.0000,  125.00),
(3, 3, 'COTISATION', 3,           'Cotisation retraite',  2625.00,   6.3000,  165.38),
(3, 4, 'COTISATION', 4,           'Cotisation santé',     2625.00,   2.0000,   52.50);

-- EMP002 : base 2200 + prime 5 % (110) = brut 2310 ; retraite 145,53 ; santé 46,20
INSERT INTO ligne_bulletin (bulletin_id, ordre, type, rubrique_id, libelle, base, taux, montant) VALUES
(2, 1, 'BASE',       1,           'Salaire de base',      2200.00, 100.0000, 2200.00),
(2, 2, 'PRIME',      2,           'Prime d''ancienneté',  2200.00,   5.0000,  110.00),
(2, 3, 'COTISATION', 3,           'Cotisation retraite',  2310.00,   6.3000,  145.53),
(2, 4, 'COTISATION', 4,           'Cotisation santé',     2310.00,   2.0000,   46.20),
(4, 1, 'BASE',       1,           'Salaire de base',      2200.00, 100.0000, 2200.00),
(4, 2, 'PRIME',      2,           'Prime d''ancienneté',  2200.00,   5.0000,  110.00),
(4, 3, 'COTISATION', 3,           'Cotisation retraite',  2310.00,   6.3000,  145.53),
(4, 4, 'COTISATION', 4,           'Cotisation santé',     2310.00,   2.0000,   46.20);

-- EMP001, novembre : 2 jours sans solde -> 2625 × 2/30 = 175 retirés du brut (brut 2450)
-- retraite 6,3 % de 2450 = 154,35 ; santé 2 % = 49,00 ; + régularisation de 50 sur octobre (F15)
-- net = brut - retenues + régularisation = 2450 - 203,35 + 50 = 2296,65
INSERT INTO ligne_bulletin (bulletin_id, ordre, type, rubrique_id, libelle, base, taux, montant, mois_corrige, motif) VALUES
(5, 1, 'BASE',           1,           'Salaire de base',                2500.00, 100.0000, 2500.00, NULL, NULL),
(5, 2, 'PRIME',          2,           'Prime d''ancienneté',            2500.00,   5.0000,  125.00, NULL, NULL),
(5, 3, 'ABSENCE',        NULL,        'Absence non rémunérée (2 jours)', 2625.00, NULL,    -175.00, NULL, NULL),
(5, 4, 'COTISATION',     3,           'Cotisation retraite',            2450.00,   6.3000,  154.35, NULL, NULL),
(5, 5, 'COTISATION',     4,           'Cotisation santé',               2450.00,   2.0000,   49.00, NULL, NULL),
(5, 6, 'REGULARISATION', NULL,        'Régularisation octobre 2026',    NULL,    NULL,      50.00, '2026-10', 'Heures supplémentaires d''octobre oubliées');

-- ABSENCES
INSERT INTO absence (employe_id, type, date_debut, date_fin, motif, remuneree) VALUES
(1, 'CONGE_PAYE', '2026-08-03', '2026-08-14', 'Congés d''été',       TRUE),    -- F18 : jours consommés
(1, 'SANS_SOLDE', '2026-11-12', '2026-11-13', 'Raisons personnelles', FALSE),  -- F13 : réduit le brut de novembre
(1, 'CONGE_PAYE', '2026-11-14', '2026-11-16', 'Pont',                TRUE),    -- contiguë à la précédente : acceptée (F12)
(2, 'MALADIE',    '2026-11-02', '2026-11-04', 'Arrêt maladie',       TRUE);
