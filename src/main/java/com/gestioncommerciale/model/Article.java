package com.gestioncommerciale.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Article/Product entity
 */
@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false)
    private String designation;
    
    @Column
    private String description;
    
    @Column(name = "prix_achat", columnDefinition = "REAL")
    private BigDecimal prixAchat;
    
    @Column(name = "prix_vente", nullable = false, columnDefinition = "REAL")
    private BigDecimal prixVente;
    
    @Column(name = "stock_actuel")
    private Integer stockActuel = 0;
    
    @Column(name = "stock_min")
    private Integer stockMin = 0;
    
    @Column(name = "stock_max")
    private Integer stockMax = 1000;
    
    @Column
    private String unite = "Unité";
    
    @Column
    private String categorie;
    
    @Column
    private String fournisseur;
    
    @Column
    private boolean active = true;
    
    @Column(name = "created_at")
    private String createdAt;
    
    @Column(name = "tva_rate", columnDefinition = "REAL")
    private BigDecimal tvaRate;
    
    @Column
    private String codeBarres;
    
    @Column
    private String image;
    
    // Constructors
    public Article() {
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.tvaRate = java.math.BigDecimal.valueOf(20.0); // 20% par défaut
    }
    
    public Article(String code, String designation, BigDecimal prixVente) {
        this();
        this.code = code;
        this.designation = designation;
        this.prixVente = prixVente;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }
    
    public BigDecimal getPrixVente() { return prixVente; }
    public void setPrixVente(BigDecimal prixVente) { this.prixVente = prixVente; }
    
    public Integer getStockActuel() { return stockActuel; }
    public void setStockActuel(Integer stockActuel) { this.stockActuel = stockActuel; }
    
    public Integer getStockMin() { return stockMin; }
    public void setStockMin(Integer stockMin) { this.stockMin = stockMin; }
    
    public Integer getStockMax() { return stockMax; }
    public void setStockMax(Integer stockMax) { this.stockMax = stockMax; }
    
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    
    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public BigDecimal getTvaRate() { return tvaRate; }
    public void setTvaRate(BigDecimal tvaRate) { this.tvaRate = tvaRate; }
    
    public String getCodeBarres() { return codeBarres; }
    public void setCodeBarres(String codeBarres) { this.codeBarres = codeBarres; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    // Business methods
    public boolean isStockLow() {
        return stockActuel <= stockMin;
    }
    
    public BigDecimal getPrixVenteWithTva() {
        return prixVente.multiply(BigDecimal.ONE.add(tvaRate.divide(BigDecimal.valueOf(100))));
    }
    
    public BigDecimal getMarge() {
        if (prixAchat != null) {
            return prixVente.subtract(prixAchat);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getMargePercent() {
        if (prixAchat != null && prixAchat.compareTo(BigDecimal.ZERO) > 0) {
            return getMarge().divide(prixAchat, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return code + " - " + designation;
    }
}
