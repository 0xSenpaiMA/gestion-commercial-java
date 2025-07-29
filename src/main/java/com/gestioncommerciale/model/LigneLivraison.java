package com.gestioncommerciale.model;

import java.math.BigDecimal;

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
 * LigneLivraison/Delivery Line entity
 */
@Entity
@Table(name = "lignes_livraison")
public class LigneLivraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_livraison_id", nullable = false)
    private BonLivraison bonLivraison;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
    
    @Column(name = "quantite_demandee", nullable = false, columnDefinition = "REAL")
    private BigDecimal quantiteDemandee;
    
    @Column(name = "quantite_livree", nullable = false, columnDefinition = "REAL")
    private BigDecimal quantiteLivree;
    
    @Column
    private String description;
    
    @Column
    private String emplacement;
    
    @Column(name = "numero_serie")
    private String numeroSerie;
    
    // Constructors
    public LigneLivraison() {
        this.quantiteDemandee = BigDecimal.ZERO;
        this.quantiteLivree = BigDecimal.ZERO;
    }
    
    public LigneLivraison(Article article, BigDecimal quantiteDemandee) {
        this();
        this.article = article;
        this.quantiteDemandee = quantiteDemandee;
        this.quantiteLivree = quantiteDemandee; // By default, deliver all requested
    }
    
    public LigneLivraison(Article article, BigDecimal quantiteDemandee, BigDecimal quantiteLivree) {
        this();
        this.article = article;
        this.quantiteDemandee = quantiteDemandee;
        this.quantiteLivree = quantiteLivree;
    }
    
    // Business methods
    public boolean isPartiallyDelivered() {
        return quantiteLivree.compareTo(quantiteDemandee) < 0 && quantiteLivree.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isFullyDelivered() {
        return quantiteLivree.compareTo(quantiteDemandee) >= 0;
    }
    
    public BigDecimal getQuantiteRestante() {
        return quantiteDemandee.subtract(quantiteLivree);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BonLivraison getBonLivraison() {
        return bonLivraison;
    }
    
    public void setBonLivraison(BonLivraison bonLivraison) {
        this.bonLivraison = bonLivraison;
    }
    
    public Article getArticle() {
        return article;
    }
    
    public void setArticle(Article article) {
        this.article = article;
    }
    
    public BigDecimal getQuantiteDemandee() {
        return quantiteDemandee;
    }
    
    public void setQuantiteDemandee(BigDecimal quantiteDemandee) {
        this.quantiteDemandee = quantiteDemandee;
    }
    
    public BigDecimal getQuantiteLivree() {
        return quantiteLivree;
    }
    
    public void setQuantiteLivree(BigDecimal quantiteLivree) {
        this.quantiteLivree = quantiteLivree;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEmplacement() {
        return emplacement;
    }
    
    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
    }
    
    public String getNumeroSerie() {
        return numeroSerie;
    }
    
    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
    
    @Override
    public String toString() {
        return (article != null ? article.getDesignation() : "N/A") + 
               " - Demandé: " + quantiteDemandee + 
               " - Livré: " + quantiteLivree;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LigneLivraison ligne = (LigneLivraison) obj;
        return id != null ? id.equals(ligne.id) : ligne.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
