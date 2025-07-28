-- Initial database schema and sample data for Gestion Commerciale
-- This script creates the initial SQLite database with sample data

-- Users table with sample data
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

-- Insert sample data

-- Default admin user (password: admin123)
INSERT OR IGNORE INTO users (nom, prenom, login, password, role, email, telephone) VALUES 
('Administrateur', 'Système', 'admin', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'ADMIN', 'admin@gestioncommerciale.com', '0612345678');

-- Additional test users
INSERT OR IGNORE INTO users (nom, prenom, login, password, role, email, telephone) VALUES 
('Dupont', 'Jean', 'manager', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'MANAGER', 'manager@gestioncommerciale.com', '0612345679'),
('Martin', 'Marie', 'employee', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'EMPLOYEE', 'employee@gestioncommerciale.com', '0612345680'),
('Bernard', 'Paul', 'viewer', 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=', 'VIEWER', 'viewer@gestioncommerciale.com', '0612345681');

-- Sample company information
INSERT OR IGNORE INTO company_info (id, raison_sociale, adresse, ville, pays, telephone, email, ice) VALUES 
(1, 'Entreprise Demo SARL', '123 Rue de la Démonstration', 'Casablanca', 'Maroc', '0522123456', 'contact@demo.ma', '001234567890123');

-- Sample clients
INSERT OR IGNORE INTO clients (nom, prenom, type, adresse, ville, pays, telephone, email, credit_limit) VALUES 
('Client Démo', 'Premier', 'CLIENT', '456 Avenue Client', 'Rabat', 'Maroc', '0537123456', 'client1@demo.ma', 10000.00),
('Fournisseur Test', '', 'FOURNISSEUR', '789 Boulevard Fournisseur', 'Fès', 'Maroc', '0535123456', 'fournisseur1@demo.ma', NULL),
('Partenaire Mixte', 'Société', 'CLIENT_FOURNISSEUR', '321 Place Partenaire', 'Marrakech', 'Maroc', '0524123456', 'partenaire@demo.ma', 15000.00);

-- Sample articles
INSERT OR IGNORE INTO articles (code, designation, description, prix_achat, prix_vente, stock_actuel, stock_min, categorie, unite) VALUES 
('ART001', 'Article Démonstration 1', 'Premier article pour démonstration', 50.00, 75.00, 100, 10, 'Catégorie A', 'Pièce'),
('ART002', 'Service Consultation', 'Service de consultation professionnelle', 0.00, 500.00, 0, 0, 'Services', 'Heure'),
('ART003', 'Produit Standard', 'Produit standard avec stock', 25.00, 40.00, 250, 20, 'Catégorie B', 'Unité'),
('ART004', 'Matériel Informatique', 'Équipement informatique', 800.00, 1200.00, 15, 5, 'Informatique', 'Pièce'),
('ART005', 'Formation', 'Session de formation', 0.00, 1500.00, 0, 0, 'Services', 'Jour');

-- Enable foreign keys
PRAGMA foreign_keys = ON;
