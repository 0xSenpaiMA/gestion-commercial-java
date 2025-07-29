package com.gestioncommerciale.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Devis/Quote entity
 */
@Entity
@Table(name = "devis")
public class Devis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String numero;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_validite")
    private LocalDateTime dateValidite;
    
    @Column(columnDefinition = "TEXT")
    private String observations;
    
    @Column(name = "total_ht", columnDefinition = "REAL")
    private BigDecimal totalHT;
    
    @Column(name = "taux_tva", columnDefinition = "REAL")
    private BigDecimal tauxTVA;
    
    @Column(name = "montant_tva", columnDefinition = "REAL")
    private BigDecimal montantTVA;
    
    @Column(name = "total_ttc", columnDefinition = "REAL")
    private BigDecimal totalTTC;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDevis statut;
    
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LigneDevis> lignes = new ArrayList<>();
    
    // Constructors
    public Devis() {
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutDevis.BROUILLON;
        this.tauxTVA = BigDecimal.valueOf(20.0); // Default 20% TVA
        this.totalHT = BigDecimal.ZERO;
        this.montantTVA = BigDecimal.ZERO;
        this.totalTTC = BigDecimal.ZERO;
    }
    
    public Devis(String numero, Client client) {
        this();
        this.numero = numero;
        this.client = client;
    }
    
    // Business methods
    public void calculateTotals() {
        this.totalHT = BigDecimal.ZERO;
        
        for (LigneDevis ligne : lignes) {
            if (ligne.getTotal() != null) {
                this.totalHT = this.totalHT.add(ligne.getTotal());
            }
        }
        
        this.montantTVA = this.totalHT.multiply(this.tauxTVA).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.totalTTC = this.totalHT.add(this.montantTVA);
    }
    
    public void addLigne(LigneDevis ligne) {
        ligne.setDevis(this);
        this.lignes.add(ligne);
        calculateTotals();
    }
    
    public void removeLigne(LigneDevis ligne) {
        this.lignes.remove(ligne);
        ligne.setDevis(null);
        calculateTotals();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDateTime getDateValidite() {
        return dateValidite;
    }
    
    public void setDateValidite(LocalDateTime dateValidite) {
        this.dateValidite = dateValidite;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    public BigDecimal getTotalHT() {
        return totalHT;
    }
    
    public void setTotalHT(BigDecimal totalHT) {
        this.totalHT = totalHT;
    }
    
    public BigDecimal getTauxTVA() {
        return tauxTVA;
    }
    
    public void setTauxTVA(BigDecimal tauxTVA) {
        this.tauxTVA = tauxTVA;
        calculateTotals();
    }
    
    public BigDecimal getMontantTVA() {
        return montantTVA;
    }
    
    public void setMontantTVA(BigDecimal montantTVA) {
        this.montantTVA = montantTVA;
    }
    
    public BigDecimal getTotalTTC() {
        return totalTTC;
    }
    
    public void setTotalTTC(BigDecimal totalTTC) {
        this.totalTTC = totalTTC;
    }
    
    public StatutDevis getStatut() {
        return statut;
    }
    
    public void setStatut(StatutDevis statut) {
        this.statut = statut;
    }
    
    public List<LigneDevis> getLignes() {
        return lignes;
    }
    
    public void setLignes(List<LigneDevis> lignes) {
        this.lignes = lignes;
        for (LigneDevis ligne : lignes) {
            ligne.setDevis(this);
        }
        calculateTotals();
    }
    
    @Override
    public String toString() {
        return "Devis #" + numero + " - " + (client != null ? client.getNom() : "N/A");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Devis devis = (Devis) obj;
        return id != null ? id.equals(devis.id) : devis.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    // Enum for Devis Status
    public enum StatutDevis {
        BROUILLON("Brouillon"),
        ENVOYE("Envoyé"),
        ACCEPTE("Accepté"),
        REFUSE("Refusé"),
        EXPIRE("Expiré");
        
        private final String libelle;
        
        StatutDevis(String libelle) {
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
}
