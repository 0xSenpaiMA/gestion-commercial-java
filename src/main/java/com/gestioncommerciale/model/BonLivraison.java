package com.gestioncommerciale.model;

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
 * BonLivraison/Delivery Note entity
 */
@Entity
@Table(name = "bons_livraison")
public class BonLivraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String numero;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "devis_id")
    private Devis devis;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_livraison")
    private LocalDateTime dateLivraison;
    
    @Column(name = "adresse_livraison", columnDefinition = "TEXT")
    private String adresseLivraison;
    
    @Column(columnDefinition = "TEXT")
    private String observations;
    
    @Column(name = "transporteur")
    private String transporteur;
    
    @Column(name = "mode_livraison")
    private String modeLivraison;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLivraison statut;
    
    @OneToMany(mappedBy = "bonLivraison", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LigneLivraison> lignes = new ArrayList<>();
    
    // Constructors
    public BonLivraison() {
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutLivraison.EN_PREPARATION;
    }
    
    public BonLivraison(String numero, Client client) {
        this();
        this.numero = numero;
        this.client = client;
    }
    
    // Business methods
    public void addLigne(LigneLivraison ligne) {
        ligne.setBonLivraison(this);
        this.lignes.add(ligne);
    }
    
    public void removeLigne(LigneLivraison ligne) {
        this.lignes.remove(ligne);
        ligne.setBonLivraison(null);
    }
    
    public void populateFromDevis(Devis devis) {
        if (devis == null) return;
        
        this.devis = devis;
        this.client = devis.getClient();
        this.observations = devis.getObservations();
        
        // Clear existing lines
        this.lignes.clear();
        
        // Copy lines from devis
        for (LigneDevis ligneDevis : devis.getLignes()) {
            LigneLivraison ligneLivraison = new LigneLivraison();
            ligneLivraison.setArticle(ligneDevis.getArticle());
            ligneLivraison.setQuantiteDemandee(ligneDevis.getQuantite());
            ligneLivraison.setQuantiteLivree(ligneDevis.getQuantite()); // By default, deliver all
            ligneLivraison.setDescription(ligneDevis.getDescription());
            addLigne(ligneLivraison);
        }
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
    
    public Devis getDevis() {
        return devis;
    }
    
    public void setDevis(Devis devis) {
        this.devis = devis;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDateTime getDateLivraison() {
        return dateLivraison;
    }
    
    public void setDateLivraison(LocalDateTime dateLivraison) {
        this.dateLivraison = dateLivraison;
    }
    
    public String getAdresseLivraison() {
        return adresseLivraison;
    }
    
    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    public String getTransporteur() {
        return transporteur;
    }
    
    public void setTransporteur(String transporteur) {
        this.transporteur = transporteur;
    }
    
    public String getModeLivraison() {
        return modeLivraison;
    }
    
    public void setModeLivraison(String modeLivraison) {
        this.modeLivraison = modeLivraison;
    }
    
    public StatutLivraison getStatut() {
        return statut;
    }
    
    public void setStatut(StatutLivraison statut) {
        this.statut = statut;
    }
    
    public List<LigneLivraison> getLignes() {
        return lignes;
    }
    
    public void setLignes(List<LigneLivraison> lignes) {
        this.lignes = lignes;
        for (LigneLivraison ligne : lignes) {
            ligne.setBonLivraison(this);
        }
    }
    
    @Override
    public String toString() {
        return "Bon de Livraison #" + numero + " - " + (client != null ? client.getNom() : "N/A");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BonLivraison bon = (BonLivraison) obj;
        return id != null ? id.equals(bon.id) : bon.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    // Enum for Delivery Status
    public enum StatutLivraison {
        EN_PREPARATION("En préparation"),
        EXPEDIE("Expédié"),
        LIVRE("Livré"),
        RETOURNE("Retourné"),
        ANNULE("Annulé");
        
        private final String libelle;
        
        StatutLivraison(String libelle) {
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
