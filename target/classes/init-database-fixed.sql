-- Fixed database initialization script
-- This ensures the admin user is created correctly

-- Create users table with proper structure
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    login VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE', 'VIEWER')),
    active BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    email VARCHAR(255),
    telephone VARCHAR(255)
);

-- Delete any existing admin user to avoid conflicts
DELETE FROM users WHERE login = 'admin';

-- Insert admin user with correct SHA-256 hash for password "admin123"
-- Hash: JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=
INSERT INTO users (nom, prenom, login, password, role, email, telephone, active, created_at) 
VALUES (
    'Administrateur', 
    'Système', 
    'admin', 
    'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 
    'ADMIN', 
    'admin@gestioncommerciale.com', 
    '0612345678', 
    1, 
    CURRENT_TIMESTAMP
);

-- Verify admin user creation
SELECT 'Admin user verification:' as info;
SELECT id, login, role, active, created_at FROM users WHERE login = 'admin';

-- Company information table
CREATE TABLE IF NOT EXISTS company_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    raison_sociale VARCHAR(255) NOT NULL,
    adresse TEXT,
    ville VARCHAR(255),
    pays VARCHAR(255),
    telephone VARCHAR(255),
    fax VARCHAR(255),
    email VARCHAR(255),
    cnss VARCHAR(255),
    rc VARCHAR(255),
    if_number VARCHAR(255),
    ice VARCHAR(255),
    logo VARCHAR(255),
    code_postal VARCHAR(255),
    site_web VARCHAR(255),
    forme_juridique VARCHAR(255),
    capital VARCHAR(255)
);

-- Clients table
CREATE TABLE IF NOT EXISTS clients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255),
    type VARCHAR(255) NOT NULL CHECK (type IN ('CLIENT', 'FOURNISSEUR', 'CLIENT_FOURNISSEUR')),
    adresse TEXT,
    ville VARCHAR(255),
    pays VARCHAR(255),
    telephone VARCHAR(255),
    fax VARCHAR(255),
    email VARCHAR(255),
    ice VARCHAR(255),
    rc VARCHAR(255),
    code_postal VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    credit_limit DECIMAL(10,2),
    active BOOLEAN NOT NULL DEFAULT 1,
    notes TEXT
);

-- Articles table
CREATE TABLE IF NOT EXISTS articles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(255) UNIQUE NOT NULL,
    designation VARCHAR(255) NOT NULL,
    description TEXT,
    prix_achat DECIMAL(10,2),
    prix_vente DECIMAL(10,2) NOT NULL,
    stock_actuel INTEGER DEFAULT 0,
    stock_min INTEGER DEFAULT 0,
    stock_max INTEGER DEFAULT 1000,
    unite VARCHAR(255) DEFAULT 'Unité',
    categorie VARCHAR(255),
    fournisseur VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tva_rate DECIMAL(5,2) DEFAULT 20.0,
    code_barres VARCHAR(255),
    image VARCHAR(255)
);

-- Devis table
CREATE TABLE IF NOT EXISTS devis (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero VARCHAR(255) UNIQUE NOT NULL,
    client_id INTEGER NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_validite TIMESTAMP,
    observations TEXT,
    total_ht DECIMAL(10,2) DEFAULT 0.00,
    taux_tva DECIMAL(5,2) DEFAULT 20.0,
    montant_tva DECIMAL(10,2) DEFAULT 0.00,
    total_ttc DECIMAL(10,2) DEFAULT 0.00,
    statut VARCHAR(255) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'ENVOYE', 'ACCEPTE', 'REFUSE', 'EXPIRE')),
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

-- Lignes de devis table
CREATE TABLE IF NOT EXISTS lignes_devis (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    devis_id INTEGER NOT NULL,
    article_id INTEGER NOT NULL,
    quantite DECIMAL(10,3) NOT NULL DEFAULT 1.0,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    remise DECIMAL(5,2) DEFAULT 0.0,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    description TEXT,
    FOREIGN KEY (devis_id) REFERENCES devis(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE RESTRICT
);

-- Sample company information
INSERT OR IGNORE INTO company_info (id, raison_sociale, adresse, ville, pays, telephone, email, ice) 
VALUES (1, 'Entreprise Demo SARL', '123 Rue de la Démonstration', 'Casablanca', 'Maroc', '0522123456', 'contact@demo.ma', '001234567890123');

-- Sample clients
INSERT OR IGNORE INTO clients (nom, prenom, type, adresse, ville, pays, telephone, email, credit_limit) 
VALUES 
('Client Démo', 'Premier', 'CLIENT', '456 Avenue Client', 'Rabat', 'Maroc', '0537123456', 'client1@demo.ma', 10000.00),
('Fournisseur Test', '', 'FOURNISSEUR', '789 Boulevard Fournisseur', 'Fès', 'Maroc', '0535123456', 'fournisseur1@demo.ma', NULL),
('Partenaire Mixte', 'Société', 'CLIENT_FOURNISSEUR', '321 Place Partenaire', 'Marrakech', 'Maroc', '0524123456', 'partenaire@demo.ma', 15000.00);

-- Sample articles
INSERT OR IGNORE INTO articles (code, designation, description, prix_achat, prix_vente, stock_actuel, stock_min, categorie, unite) 
VALUES 
('ART001', 'Article Démonstration 1', 'Premier article pour démonstration', 50.00, 75.00, 100, 10, 'Catégorie A', 'Pièce'),
('ART002', 'Service Consultation', 'Service de consultation professionnelle', 0.00, 500.00, 0, 0, 'Services', 'Heure'),
('ART003', 'Produit Standard', 'Produit standard avec stock', 25.00, 40.00, 250, 20, 'Catégorie B', 'Unité'),
('ART004', 'Matériel Informatique', 'Équipement informatique', 800.00, 1200.00, 15, 5, 'Informatique', 'Pièce'),
('ART005', 'Formation', 'Session de formation', 0.00, 1500.00, 0, 0, 'Services', 'Jour');

-- Sample devis
INSERT OR IGNORE INTO devis (id, numero, client_id, date_creation, date_validite, observations, total_ht, taux_tva, montant_tva, total_ttc, statut) 
VALUES 
(1, 'DEV202501001', 1, '2025-01-15 10:30:00', '2025-02-14', 'Devis pour matériel informatique', 1275.00, 20.0, 255.00, 1530.00, 'ENVOYE'),
(2, 'DEV202501002', 3, '2025-01-20 14:15:00', '2025-02-19', 'Formation et consultation', 2000.00, 20.0, 400.00, 2400.00, 'BROUILLON');

-- Sample lignes de devis
INSERT OR IGNORE INTO lignes_devis (devis_id, article_id, quantite, prix_unitaire, remise, total, description) 
VALUES 
(1, 4, 1.0, 1200.00, 0.0, 1200.00, 'Ordinateur portable professionnel'),
(1, 1, 1.0, 75.00, 0.0, 75.00, 'Accessoires inclus'),
(2, 2, 2.0, 500.00, 0.0, 1000.00, 'Consultation technique - 2 heures'),
(2, 5, 1.0, 1500.00, 25.0, 1125.00, 'Formation équipe - 1 jour (remise 25%)');

-- Bons de livraison table
CREATE TABLE IF NOT EXISTS bons_livraison (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero VARCHAR(255) UNIQUE NOT NULL,
    client_id INTEGER NOT NULL,
    devis_id INTEGER,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_livraison TIMESTAMP,
    adresse_livraison TEXT,
    observations TEXT,
    transporteur VARCHAR(255),
    mode_livraison VARCHAR(255),
    statut VARCHAR(255) NOT NULL DEFAULT 'EN_PREPARATION' CHECK (statut IN ('EN_PREPARATION', 'PRET', 'EXPEDIEE', 'LIVREE', 'PARTIELLEMENT_LIVREE', 'ANNULEE')),
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (devis_id) REFERENCES devis(id) ON DELETE SET NULL
);

-- Lignes de livraison table
CREATE TABLE IF NOT EXISTS lignes_livraison (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bon_livraison_id INTEGER NOT NULL,
    article_id INTEGER NOT NULL,
    quantite_demandee DECIMAL(10,3) NOT NULL DEFAULT 1.0,
    quantite_livree DECIMAL(10,3) DEFAULT 0.0,
    emplacement VARCHAR(255),
    numero_serie VARCHAR(255),
    description TEXT,
    FOREIGN KEY (bon_livraison_id) REFERENCES bons_livraison(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE RESTRICT
);

-- Sample bons de livraison
INSERT OR IGNORE INTO bons_livraison (id, numero, client_id, devis_id, date_creation, date_livraison, adresse_livraison, observations, transporteur, mode_livraison, statut) 
VALUES 
(1, 'BL202501001', 1, 1, '2025-01-16 09:00:00', '2025-01-16', '456 Avenue Client, Rabat', 'Livraison urgente demandée', 'Transport Express', 'Standard', 'LIVREE'),
(2, 'BL202501002', 3, NULL, '2025-01-21 10:30:00', NULL, NULL, 'Commande en préparation', NULL, 'Retrait magasin', 'EN_PREPARATION');

-- Sample lignes de livraison
INSERT OR IGNORE INTO lignes_livraison (bon_livraison_id, article_id, quantite_demandee, quantite_livree, emplacement, numero_serie, description) 
VALUES 
(1, 4, 1.0, 1.0, 'Magasin-A12', 'SN2025001', 'Ordinateur portable professionnel livré'),
(1, 1, 1.0, 1.0, 'Magasin-B5', NULL, 'Accessoires livrés avec l\'ordinateur'),
(2, 2, 2.0, 0.0, NULL, NULL, 'Consultation en attente de planification'),
(2, 5, 1.0, 0.0, 'Salle formation', NULL, 'Formation programmée pour la semaine prochaine');

-- Factures table
CREATE TABLE IF NOT EXISTS factures (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero VARCHAR(255) UNIQUE NOT NULL,
    client_id INTEGER NOT NULL,
    bon_livraison_id INTEGER,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_facturation DATE,
    date_echeance DATE,
    observations TEXT,
    total_ht DECIMAL(10,2) DEFAULT 0.00,
    taux_tva DECIMAL(5,2) DEFAULT 20.0,
    montant_tva DECIMAL(10,2) DEFAULT 0.00,
    total_ttc DECIMAL(10,2) DEFAULT 0.00,
    montant_paye DECIMAL(10,2) DEFAULT 0.00,
    mode_paiement VARCHAR(255),
    statut VARCHAR(255) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'VALIDEE', 'ENVOYEE', 'PAYEE', 'PARTIELLEMENT_PAYEE', 'EN_RETARD', 'ANNULEE')),
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    FOREIGN KEY (bon_livraison_id) REFERENCES bons_livraison(id) ON DELETE SET NULL
);

-- Lignes de facture table
CREATE TABLE IF NOT EXISTS lignes_facture (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    facture_id INTEGER NOT NULL,
    article_id INTEGER NOT NULL,
    quantite DECIMAL(10,3) NOT NULL DEFAULT 1.0,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    remise DECIMAL(5,2) DEFAULT 0.0,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    description TEXT,
    FOREIGN KEY (facture_id) REFERENCES factures(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE RESTRICT
);

-- Sample factures
INSERT OR IGNORE INTO factures (id, numero, client_id, bon_livraison_id, date_creation, date_facturation, date_echeance, observations, total_ht, taux_tva, montant_tva, total_ttc, montant_paye, mode_paiement, statut) 
VALUES 
(1, 'FAC202501001', 1, 1, '2025-01-17 14:00:00', '2025-01-17', '2025-02-16', 'Facture pour matériel informatique livré', 1275.00, 20.0, 255.00, 1530.00, 0.00, 'Virement', 'ENVOYEE'),
(2, 'FAC202501002', 3, NULL, '2025-01-22 09:30:00', '2025-01-22', '2025-02-21', 'Facture pour services de formation', 2000.00, 20.0, 400.00, 2400.00, 1200.00, 'Chèque', 'PARTIELLEMENT_PAYEE');

-- Sample lignes de facture
INSERT OR IGNORE INTO lignes_facture (facture_id, article_id, quantite, prix_unitaire, remise, total, description) 
VALUES 
(1, 4, 1.0, 1200.00, 0.0, 1200.00, 'Ordinateur portable professionnel facturé'),
(1, 1, 1.0, 75.00, 0.0, 75.00, 'Accessoires facturés'),
(2, 2, 2.0, 500.00, 0.0, 1000.00, 'Consultation technique - 2 heures facturées'),
(2, 5, 1.0, 1500.00, 25.0, 1125.00, 'Formation équipe - 1 jour facturée (remise 25%)');

-- Enable foreign keys
PRAGMA foreign_keys = ON;
