package com.gestioncommerciale.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Client/Supplier entity
 */
@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    
    @Column
    private String prenom;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientType type;
    
    @Column
    private String adresse;
    
    @Column
    private String ville;
    
    @Column
    private String pays;
    
    @Column
    private String telephone;
    
    @Column
    private String fax;
    
    @Column
    private String email;
    
    @Column
    private String ice;
    
    @Column
    private String rc;
    
    @Column(name = "code_postal")
    private String codePostal;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "credit_limit")
    private Double creditLimit;
    
    @Column
    private boolean active = true;
    
    @Column
    private String notes;
    
    public enum ClientType {
        CLIENT, FOURNISSEUR, CLIENT_FOURNISSEUR
    }
    
    // Constructors
    public Client() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Client(String nom, ClientType type) {
        this();
        this.nom = nom;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public ClientType getType() { return type; }
    public void setType(ClientType type) { this.type = type; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }
    
    public String getRc() { return rc; }
    public void setRc(String rc) { this.rc = rc; }
    
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double creditLimit) { this.creditLimit = creditLimit; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getFullName() {
        if (prenom != null && !prenom.trim().isEmpty()) {
            return prenom + " " + nom;
        }
        return nom;
    }
    
    @Override
    public String toString() {
        return getFullName() + " (" + type + ")";
    }
}
