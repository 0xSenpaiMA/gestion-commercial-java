package com.gestioncommerciale.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Entity representing a Bon de Commande (Purchase Order)
 */
@Entity
@Table(name = "bons_commande")
public class BonCommande {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero", unique = true, nullable = false)
    private String numero;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Client fournisseur; // Using Client entity for suppliers (type FOURNISSEUR)
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_commande")
    private LocalDate dateCommande;
    
    @Column(name = "date_livraison_prevue")
    private LocalDate dateLivraisonPrevue;
    
    @Column(name = "adresse_livraison")
    private String adresseLivraison;
    
    @Column(name = "conditions_paiement")
    private String conditionsPaiement;
    
    @Column(name = "observations")
    private String observations;
    
    @Column(name = "total_ht", precision = 10, scale = 2)
    private BigDecimal totalHT = BigDecimal.ZERO;
    
    @Column(name = "taux_tva", precision = 5, scale = 2)
    private BigDecimal tauxTVA = new BigDecimal("20.0");
    
    @Column(name = "montant_tva", precision = 10, scale = 2)
    private BigDecimal montantTVA = BigDecimal.ZERO;
    
    @Column(name = "total_ttc", precision = 10, scale = 2)
    private BigDecimal totalTTC = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCommande statut = StatutCommande.BROUILLON;
    
    @Column(name = "numero_devis_fournisseur")
    private String numeroDevisFournisseur;
    
    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();
    
    // Constructors
    public BonCommande() {
        this.dateCreation = LocalDateTime.now();
        this.dateCommande = LocalDate.now();
    }
    
    public BonCommande(String numero, Client fournisseur) {
        this();
        this.numero = numero;
        this.fournisseur = fournisseur;
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
    
    public Client getFournisseur() {
        return fournisseur;
    }
    
    public void setFournisseur(Client fournisseur) {
        this.fournisseur = fournisseur;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDate getDateCommande() {
        return dateCommande;
    }
    
    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }
    
    public LocalDate getDateLivraisonPrevue() {
        return dateLivraisonPrevue;
    }
    
    public void setDateLivraisonPrevue(LocalDate dateLivraisonPrevue) {
        this.dateLivraisonPrevue = dateLivraisonPrevue;
    }
    
    public String getAdresseLivraison() {
        return adresseLivraison;
    }
    
    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }
    
    public String getConditionsPaiement() {
        return conditionsPaiement;
    }
    
    public void setConditionsPaiement(String conditionsPaiement) {
        this.conditionsPaiement = conditionsPaiement;
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
    
    public StatutCommande getStatut() {
        return statut;
    }
    
    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }
    
    public String getNumeroDevisFournisseur() {
        return numeroDevisFournisseur;
    }
    
    public void setNumeroDevisFournisseur(String numeroDevisFournisseur) {
        this.numeroDevisFournisseur = numeroDevisFournisseur;
    }
    
    public List<LigneCommande> getLignes() {
        return lignes;
    }
    
    public void setLignes(List<LigneCommande> lignes) {
        this.lignes.clear();
        if (lignes != null) {
            for (LigneCommande ligne : lignes) {
                addLigne(ligne);
            }
        }
    }
    
    // Business methods
    public void addLigne(LigneCommande ligne) {
        lignes.add(ligne);
        ligne.setBonCommande(this);
        calculateTotals();
    }
    
    public void removeLigne(LigneCommande ligne) {
        lignes.remove(ligne);
        ligne.setBonCommande(null);
        calculateTotals();
    }
    
    public void calculateTotals() {
        BigDecimal totalHT = BigDecimal.ZERO;
        
        for (LigneCommande ligne : lignes) {
            totalHT = totalHT.add(ligne.getTotal());
        }
        
        this.totalHT = totalHT;
        this.montantTVA = totalHT.multiply(tauxTVA).divide(new BigDecimal("100"));
        this.totalTTC = totalHT.add(montantTVA);
    }
    
    // Enum for purchase order status
    public enum StatutCommande {
        BROUILLON("Brouillon"),
        CONFIRMEE("Confirmée"),
        ENVOYEE("Envoyée"),
        ACCEPTEE("Acceptée"),
        EN_COURS("En cours"),
        LIVREE("Livrée"),
        PARTIELLEMENT_LIVREE("Partiellement livrée"),
        ANNULEE("Annulée");
        
        private final String libelle;
        
        StatutCommande(String libelle) {
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
    
    @Override
    public String toString() {
        return "BonCommande{" +
                "numero='" + numero + '\'' +
                ", fournisseur=" + (fournisseur != null ? fournisseur.getNom() : "null") +
                ", totalTTC=" + totalTTC +
                ", statut=" + statut +
                '}';
    }
}
