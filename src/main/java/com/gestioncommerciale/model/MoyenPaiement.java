package com.gestioncommerciale.model;

/**
 * Enumeration for payment methods
 */
public enum MoyenPaiement {
    ESPECES("Espèces"),
    CHEQUE("Chèque"),
    VIREMENT("Virement bancaire"),
    CARTE_BANCAIRE("Carte bancaire"),
    PRELEVEMENT("Prélèvement automatique"),
    TRAITE("Traite"),
    AUTRE("Autre");
    
    private final String libelle;
    
    MoyenPaiement(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    @Override
    public String toString() {
        return libelle;
    }
}
