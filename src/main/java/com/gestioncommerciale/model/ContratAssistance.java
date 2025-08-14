package com.gestioncommerciale.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity representing a ContratAssistance (Support Contract)
 */
@Entity
@Table(name = "contrats_assistance")
public class ContratAssistance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero", unique = true, nullable = false)
    private String numero;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "conditions")
    private String conditions;
    
    @Column(name = "tarif_horaire", precision = 10, scale = 2)
    private java.math.BigDecimal tarifHoraire;
    
    @Column(name = "heures_incluses")
    private Integer heuresIncluses = 0;
    
    @Column(name = "observations")
    private String observations;
    
    @Column(name = "actif")
    private boolean actif = true;
    
    @OneToMany(mappedBy = "contratAssistance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Facture> factures = new ArrayList<>();
    
    // Constructors
    public ContratAssistance() {
        this.dateDebut = LocalDate.now();
        this.dateFin = LocalDate.now().plusMonths(12);
    }
    
    public ContratAssistance(String numero, Client client, LocalDate dateDebut, LocalDate dateFin) {
        this();
        this.numero = numero;
        this.client = client;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getConditions() {
        return conditions;
    }
    
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
    
    public java.math.BigDecimal getTarifHoraire() {
        return tarifHoraire;
    }
    
    public void setTarifHoraire(java.math.BigDecimal tarifHoraire) {
        this.tarifHoraire = tarifHoraire;
    }
    
    public Integer getHeuresIncluses() {
        return heuresIncluses;
    }
    
    public void setHeuresIncluses(Integer heuresIncluses) {
        this.heuresIncluses = heuresIncluses;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    public List<Facture> getFactures() {
        return factures;
    }
    
    public void setFactures(List<Facture> factures) {
        this.factures.clear();
        if (factures != null) {
            this.factures.addAll(factures);
            // Set the reverse relationship
            for (Facture facture : factures) {
                facture.setContratAssistance(this);
            }
        }
    }
    
    // Business methods
    public void addFacture(Facture facture) {
        if (facture != null) {
            this.factures.add(facture);
            facture.setContratAssistance(this);
        }
    }
    
    public void removeFacture(Facture facture) {
        if (facture != null) {
            this.factures.remove(facture);
            facture.setContratAssistance(null);
        }
    }
    
    public boolean isExpire() {
        return LocalDate.now().isAfter(dateFin);
    }
    
    public boolean isEnCours() {
        LocalDate now = LocalDate.now();
        return (now.isEqual(dateDebut) || now.isAfter(dateDebut)) && 
               (now.isEqual(dateFin) || now.isBefore(dateFin));
    }
    
    public long getDureeEnJours() {
        return java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin);
    }
    
    @Override
    public String toString() {
        return numero + " - " + (client != null ? client.getNom() : "Client inconnu") + 
               " (" + dateDebut + " à " + dateFin + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ContratAssistance that = (ContratAssistance) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
