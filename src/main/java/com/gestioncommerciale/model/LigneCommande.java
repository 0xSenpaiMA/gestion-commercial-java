package com.gestioncommerciale.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a line item in a Bon de Commande (Purchase Order Line)
 */
@Entity
@Table(name = "lignes_commande")
public class LigneCommande {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_commande_id", nullable = false)
    private BonCommande bonCommande;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
    
    @Column(name = "quantite", precision = 10, scale = 3, nullable = false)
    private BigDecimal quantite = BigDecimal.ONE;
    
    @Column(name = "prix_unitaire", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire = BigDecimal.ZERO;
    
    @Column(name = "remise", precision = 5, scale = 2)
    private BigDecimal remise = BigDecimal.ZERO;
    
    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "date_livraison_souhaitee")
    private LocalDate dateLivraisonSouhaitee;
    
    @Column(name = "reference_fournisseur")
    private String referenceFournisseur;
    
    // Constructors
    public LigneCommande() {
    }
    
    public LigneCommande(BonCommande bonCommande, Article article, BigDecimal quantite, BigDecimal prixUnitaire) {
        this.bonCommande = bonCommande;
        this.article = article;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        calculateTotal();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BonCommande getBonCommande() {
        return bonCommande;
    }
    
    public void setBonCommande(BonCommande bonCommande) {
        this.bonCommande = bonCommande;
    }
    
    public Article getArticle() {
        return article;
    }
    
    public void setArticle(Article article) {
        this.article = article;
    }
    
    public BigDecimal getQuantite() {
        return quantite;
    }
    
    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
        calculateTotal();
    }
    
    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }
    
    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        calculateTotal();
    }
    
    public BigDecimal getRemise() {
        return remise;
    }
    
    public void setRemise(BigDecimal remise) {
        this.remise = remise;
        calculateTotal();
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDateLivraisonSouhaitee() {
        return dateLivraisonSouhaitee;
    }
    
    public void setDateLivraisonSouhaitee(LocalDate dateLivraisonSouhaitee) {
        this.dateLivraisonSouhaitee = dateLivraisonSouhaitee;
    }
    
    public String getReferenceFournisseur() {
        return referenceFournisseur;
    }
    
    public void setReferenceFournisseur(String referenceFournisseur) {
        this.referenceFournisseur = referenceFournisseur;
    }
    
    // Business methods
    public void calculateTotal() {
        if (quantite != null && prixUnitaire != null) {
            BigDecimal subtotal = quantite.multiply(prixUnitaire);
            
            if (remise != null && remise.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remiseAmount = subtotal.multiply(remise).divide(new BigDecimal("100"));
                this.total = subtotal.subtract(remiseAmount);
            } else {
                this.total = subtotal;
            }
        } else {
            this.total = BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getMontantRemise() {
        if (remise != null && remise.compareTo(BigDecimal.ZERO) > 0 && 
            quantite != null && prixUnitaire != null) {
            BigDecimal subtotal = quantite.multiply(prixUnitaire);
            return subtotal.multiply(remise).divide(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return "LigneCommande{" +
                "article=" + (article != null ? article.getDesignation() : "null") +
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", total=" + total +
                '}';
    }
}
