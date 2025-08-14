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
 * Entity representing a Facture (Invoice)
 */
@Entity
@Table(name = "factures")
public class Facture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero", unique = true, nullable = false)
    private String numero;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_livraison_id")
    private BonLivraison bonLivraison;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_echeance")
    private LocalDate dateEcheance;
    
    @Column(name = "date_facturation")
    private LocalDate dateFacturation;
    
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
    
    @Column(name = "montant_paye", precision = 10, scale = 2)
    private BigDecimal montantPaye = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "moyen_paiement")
    private MoyenPaiement moyenPaiement;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutFacture statut = StatutFacture.BROUILLON;
    
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();
    
    // Constructors
    public Facture() {
        this.dateCreation = LocalDateTime.now();
        this.dateFacturation = LocalDate.now();
    }
    
    public Facture(String numero, Client client) {
        this();
        this.numero = numero;
        this.client = client;
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
    
    public BonLivraison getBonLivraison() {
        return bonLivraison;
    }
    
    public void setBonLivraison(BonLivraison bonLivraison) {
        this.bonLivraison = bonLivraison;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDate getDateEcheance() {
        return dateEcheance;
    }
    
    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
    
    public LocalDate getDateFacturation() {
        return dateFacturation;
    }
    
    public void setDateFacturation(LocalDate dateFacturation) {
        this.dateFacturation = dateFacturation;
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
    
    public BigDecimal getMontantPaye() {
        return montantPaye;
    }
    
    public void setMontantPaye(BigDecimal montantPaye) {
        this.montantPaye = montantPaye;
    }
    
    public MoyenPaiement getMoyenPaiement() {
        return moyenPaiement;
    }
    
    public void setMoyenPaiement(MoyenPaiement moyenPaiement) {
        this.moyenPaiement = moyenPaiement;
    }
    
    // Méthodes de compatibilité pour le code existant
    public String getModePaiement() {
        return moyenPaiement != null ? moyenPaiement.getLibelle() : null;
    }
    
    public void setModePaiement(String modePaiement) {
        if (modePaiement == null || modePaiement.trim().isEmpty()) {
            this.moyenPaiement = null;
            return;
        }
        
        // Recherche par libellé
        for (MoyenPaiement mp : MoyenPaiement.values()) {
            if (mp.getLibelle().equalsIgnoreCase(modePaiement.trim())) {
                this.moyenPaiement = mp;
                return;
            }
        }
        
        // Si aucune correspondance trouvée, utilise AUTRE
        this.moyenPaiement = MoyenPaiement.AUTRE;
    }
    
    public StatutFacture getStatut() {
        return statut;
    }
    
    public void setStatut(StatutFacture statut) {
        this.statut = statut;
    }
    
    public List<LigneFacture> getLignes() {
        return lignes;
    }
    
    public void setLignes(List<LigneFacture> lignes) {
        this.lignes.clear();
        if (lignes != null) {
            for (LigneFacture ligne : lignes) {
                addLigne(ligne);
            }
        }
    }
    
    // Business methods
    public void addLigne(LigneFacture ligne) {
        lignes.add(ligne);
        ligne.setFacture(this);
        calculateTotals();
    }
    
    public void removeLigne(LigneFacture ligne) {
        lignes.remove(ligne);
        ligne.setFacture(null);
        calculateTotals();
    }
    
    public void calculateTotals() {
        BigDecimal totalHT = BigDecimal.ZERO;
        
        for (LigneFacture ligne : lignes) {
            totalHT = totalHT.add(ligne.getTotal());
        }
        
        this.totalHT = totalHT;
        this.montantTVA = totalHT.multiply(tauxTVA).divide(new BigDecimal("100"));
        this.totalTTC = totalHT.add(montantTVA);
    }
    
    public BigDecimal getMontantRestant() {
        return totalTTC.subtract(montantPaye);
    }
    
    public boolean isPaid() {
        return getMontantRestant().compareTo(BigDecimal.ZERO) <= 0;
    }
    
    /**
     * Create a facture from a bon de livraison
     */
    public void populateFromBonLivraison(BonLivraison bonLivraison) {
        if (bonLivraison == null) return;
        
        this.bonLivraison = bonLivraison;
        this.client = bonLivraison.getClient();
        
        // Clear existing lines
        this.lignes.clear();
        
        // Convert delivery lines to invoice lines
        for (LigneLivraison ligneLivraison : bonLivraison.getLignes()) {
            if (ligneLivraison.getQuantiteLivree() != null && 
                ligneLivraison.getQuantiteLivree().compareTo(BigDecimal.ZERO) > 0) {
                
                LigneFacture ligneFacture = new LigneFacture();
                ligneFacture.setArticle(ligneLivraison.getArticle());
                ligneFacture.setQuantite(ligneLivraison.getQuantiteLivree());
                
                // Get price from article or use default
                BigDecimal prixUnitaire = ligneLivraison.getArticle() != null && 
                    ligneLivraison.getArticle().getPrixVente() != null ? 
                    ligneLivraison.getArticle().getPrixVente() : BigDecimal.ZERO;
                ligneFacture.setPrixUnitaire(prixUnitaire);
                
                ligneFacture.setDescription(ligneLivraison.getDescription());
                
                addLigne(ligneFacture);
            }
        }
        
        calculateTotals();
    }
    
    // Enum for invoice status
    public enum StatutFacture {
        BROUILLON("Brouillon"),
        VALIDEE("Validée"),
        ENVOYEE("Envoyée"),
        PAYEE("Payée"),
        PARTIELLEMENT_PAYEE("Partiellement payée"),
        EN_RETARD("En retard"),
        ANNULEE("Annulée");
        
        private final String libelle;
        
        StatutFacture(String libelle) {
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
        return "Facture{" +
                "numero='" + numero + '\'' +
                ", client=" + (client != null ? client.getNom() : "null") +
                ", totalTTC=" + totalTTC +
                ", statut=" + statut +
                '}';
    }
}
