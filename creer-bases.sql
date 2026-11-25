-- Création (ou remise à zéro) des bases de données des cinq projets.
-- Les tables ne sont pas créées ici : Hibernate les génère à partir des entités au démarrage de chaque projet
-- (spring.jpa.hibernate.ddl-auto=update). Lancer ensuite le script donnees/remplir-base.ps1 de chaque projet.
-- Exécution : mysql -u root -p < creer-bases.sql   (ou depuis MySQL Workbench)

DROP DATABASE IF EXISTS place_de_marche;
CREATE DATABASE place_de_marche CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP DATABASE IF EXISTS microfinance;
CREATE DATABASE microfinance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP DATABASE IF EXISTS atelier_de_production;
CREATE DATABASE atelier_de_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP DATABASE IF EXISTS transport_urbain;
CREATE DATABASE transport_urbain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP DATABASE IF EXISTS la_paie;
CREATE DATABASE la_paie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
