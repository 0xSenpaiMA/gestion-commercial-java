package com.gestioncommerciale.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing an accounting period (Exercice)
 */
@Entity
@Table(name = "exercices")
public class Exercice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "libelle", nullable = false)
    private String libelle;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;
    
    @Column(name = "date_cloture")
    private LocalDate dateCloture;
    
    @Column(name = "actif", nullable = false)
    private boolean actif = true;
    
    @Column(name = "cloture", nullable = false)
    private boolean cloture = false;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "observations")
    private String observations;
    
    // Constructors
    public Exercice() {}
    
    public Exercice(String libelle, LocalDate dateDebut, LocalDate dateFin) {
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.actif = true;
        this.cloture = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public LocalDate getDateFin() {
        return dateFin;
    }
    
    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
    
    public LocalDate getDateCloture() {
        return dateCloture;
    }
    
    public void setDateCloture(LocalDate dateCloture) {
        this.dateCloture = dateCloture;
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    public boolean isCloture() {
        return cloture;
    }
    
    public void setCloture(boolean cloture) {
        this.cloture = cloture;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    // Business methods
    public long getDureeEnJours() {
        if (dateDebut != null && dateFin != null) {
            return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        }
        return 0;
    }
    
    public boolean isEnCours() {
        LocalDate today = LocalDate.now();
        return actif && !cloture && 
               dateDebut != null && dateFin != null &&
               !today.isBefore(dateDebut) && !today.isAfter(dateFin);
    }
    
    public boolean isExpire() {
        LocalDate today = LocalDate.now();
        return dateFin != null && today.isAfter(dateFin);
    }
    
    public boolean peutEtreCloture() {
        return actif && !cloture && isExpire();
    }
    
    public void cloturer() {
        if (peutEtreCloture()) {
            this.cloture = true;
            this.dateCloture = LocalDate.now();
            this.actif = false;
        } else {
            throw new IllegalStateException("L'exercice ne peut pas être clôturé dans son état actuel");
        }
    }
    
    public String getStatut() {
        if (cloture) {
            return "Clôturé";
        } else if (isEnCours()) {
            return "En cours";
        } else if (isExpire()) {
            return "Échu";
        } else {
            return "À venir";
        }
    }
    
    @Override
    public String toString() {
        return libelle != null ? libelle : "Exercice " + id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Exercice exercice = (Exercice) obj;
        return id != null && id.equals(exercice.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
