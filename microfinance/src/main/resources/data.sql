-- =====================================================================
--  Données de test — à placer dans : src/main/resources/data.sql
--  Montants en FCFA
-- =====================================================================

-- CLIENTS
INSERT INTO client (nom, telephone, adresse, plafond_emprunt, agent_credit) VALUES
('Awa Diallo',        '+221 77 123 45 67', 'Quartier Médina, Dakar',      1000000.00, 'Moussa Ndiaye'),   -- id 1
('Boutique Kane',     '+221 76 234 56 78', 'Marché Sandaga, Dakar',        500000.00, 'Moussa Ndiaye'),   -- id 2
('Fatou Sow',         '+221 78 345 67 89', 'Rue 10, Thiès',                300000.00, 'Aminata Fall');    -- id 3 : sans prêt ni compte

-- COMPTES
INSERT INTO compte (numero, type, date_ouverture, solde, client_id) VALUES
('CPT-0001', 'COURANT', '2025-06-10',  45000.00, 1),
('CPT-0002', 'EPARGNE', '2026-01-15', 120000.00, 1),
('CPT-0003', 'COURANT', '2025-11-02',  80000.00, 2);

-- PRETS
INSERT INTO pret (montant, duree_mois, taux, date_accord, statut, client_id, date_solde, jours_retard_max) VALUES
(300000.00,  6, 12.00, '2026-08-01', 'EN_COURS', 1, NULL,         24),   -- id 1 : en cours, échéance de novembre en retard
(200000.00, 12, 15.00, '2026-11-20', 'ACCORDE',  1, NULL,          0),   -- id 2 : échéancier pas encore généré (F8, F11, F19)
(100000.00,  2, 12.00, '2026-03-01', 'SOLDE',    2, '2026-05-01',  0);   -- id 3 : entièrement remboursé (F15)

-- ECHEANCES
-- Prêt 1 : 300 000 sur 6 mois à 12 %/an (1 %/mois), capital constant de 50 000
-- Intérêts = 1 % du capital restant dû. Somme des capitaux = 300 000 (F11)
INSERT INTO echeance (pret_id, date_echeance, capital, interets, montant_total, capital_paye, interets_paye) VALUES
(1, '2026-09-01', 50000.00, 3000.00, 53000.00, 50000.00, 3000.00),   -- payée
(1, '2026-10-01', 50000.00, 2500.00, 52500.00, 50000.00, 2500.00),   -- payée
(1, '2026-11-01', 50000.00, 2000.00, 52000.00, 18000.00, 2000.00),   -- partiellement payée -> impayé (F16, F18)
(1, '2026-12-01', 50000.00, 1500.00, 51500.00,     0.00,    0.00),
(1, '2027-01-01', 50000.00, 1000.00, 51000.00,     0.00,    0.00),
(1, '2027-02-01', 50000.00,  500.00, 50500.00,     0.00,    0.00);

-- Prêt 3 : 100 000 sur 2 mois à 12 %/an, entièrement payé
INSERT INTO echeance (pret_id, date_echeance, capital, interets, montant_total, capital_paye, interets_paye) VALUES
(3, '2026-04-01', 50000.00, 1000.00, 51000.00, 50000.00, 1000.00),
(3, '2026-05-01', 50000.00,  500.00, 50500.00, 50000.00,  500.00);

-- PAIEMENTS (part_penalites + part_interets + part_capital = montant)
INSERT INTO paiement (pret_id, date_paiement, montant, mode_reglement, part_penalites, part_interets, part_capital) VALUES
(1, '2026-09-01', 53000.00, 'ESPECES',      0.00, 3000.00, 50000.00),
(1, '2026-10-03', 52500.00, 'MOBILE_MONEY', 0.00, 2500.00, 50000.00),
(1, '2026-11-05', 20000.00, 'ESPECES',      0.00, 2000.00, 18000.00),
(3, '2026-04-01', 51000.00, 'VIREMENT',     0.00, 1000.00, 50000.00),
(3, '2026-05-01', 50500.00, 'VIREMENT',     0.00,  500.00, 50000.00);
