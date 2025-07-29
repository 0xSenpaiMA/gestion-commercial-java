package com.gestioncommerciale.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
 * LigneDevis/Quote Line entity
 */
@Entity
@Table(name = "lignes_devis")
public class LigneDevis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
    
    @Column(nullable = false, columnDefinition = "REAL")
    private BigDecimal quantite;
    
    @Column(name = "prix_unitaire", nullable = false, columnDefinition = "REAL")
    private BigDecimal prixUnitaire;
    
    @Column(name = "remise", columnDefinition = "REAL")
    private BigDecimal remise;
    
    @Column(columnDefinition = "REAL")
    private BigDecimal total;
    
    @Column
    private String description;
    
    // Constructors
    public LigneDevis() {
        this.quantite = BigDecimal.ONE;
        this.remise = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }
    
    public LigneDevis(Article article, BigDecimal quantite) {
        this();
        this.article = article;
        this.quantite = quantite;
        if (article != null && article.getPrixVente() != null) {
            this.prixUnitaire = article.getPrixVente();
            calculateTotal();
        }
    }
    
    public LigneDevis(Article article, BigDecimal quantite, BigDecimal prixUnitaire) {
        this();
        this.article = article;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        calculateTotal();
    }
    
    // Business methods
    public void calculateTotal() {
        if (quantite != null && prixUnitaire != null) {
            BigDecimal sousTotal = quantite.multiply(prixUnitaire);
            if (remise != null && remise.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal montantRemise = sousTotal.multiply(remise).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                this.total = sousTotal.subtract(montantRemise);
            } else {
                this.total = sousTotal;
            }
            this.total = this.total.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.total = BigDecimal.ZERO;
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Devis getDevis() {
        return devis;
    }
    
    public void setDevis(Devis devis) {
        this.devis = devis;
    }
    
    public Article getArticle() {
        return article;
    }
    
    public void setArticle(Article article) {
        this.article = article;
        if (article != null && article.getPrixVente() != null && this.prixUnitaire == null) {
            this.prixUnitaire = article.getPrixVente();
            calculateTotal();
        }
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
    
    @Override
    public String toString() {
        return (article != null ? article.getDesignation() : "N/A") + " - Qté: " + quantite + " - Total: " + total + "€";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LigneDevis ligne = (LigneDevis) obj;
        return id != null ? id.equals(ligne.id) : ligne.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
