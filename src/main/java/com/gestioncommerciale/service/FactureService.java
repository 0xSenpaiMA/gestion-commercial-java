package com.gestioncommerciale.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.BonLivraison;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Facture;
import com.gestioncommerciale.model.LigneFacture;

/**
 * Service class for Facture operations
 */
public class FactureService {
    
    public List<Facture> getAllFactures() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Facture> query = em.createQuery(
                "SELECT f FROM Facture f " +
                "LEFT JOIN FETCH f.client " +
                "LEFT JOIN FETCH f.bonLivraison " +
                "LEFT JOIN FETCH f.lignes l " +
                "LEFT JOIN FETCH l.article " +
                "ORDER BY f.dateCreation DESC", Facture.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Facture getFactureById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Facture facture = em.find(Facture.class, id);
            if (facture != null) {
                // Force loading of lazy relationships
                facture.getClient().getNom();
                facture.getLignes().size();
                for (LigneFacture ligne : facture.getLignes()) {
                    ligne.getArticle().getDesignation();
                }
                if (facture.getBonLivraison() != null) {
                    facture.getBonLivraison().getNumero();
                }
            }
            return facture;
        } finally {
            em.close();
        }
    }
    
    public Facture save(Facture facture) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Generate numero if not set
            if (facture.getNumero() == null || facture.getNumero().trim().isEmpty()) {
                facture.setNumero(generateNumero());
            }
            
            // Calculate totals before saving
            facture.calculateTotals();
            
            Facture savedFacture;
            if (facture.getId() == null) {
                em.persist(facture);
                savedFacture = facture;
            } else {
                savedFacture = em.merge(facture);
            }
            
            em.getTransaction().commit();
            return savedFacture;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde de la facture: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(Facture facture) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            Facture managedFacture = em.find(Facture.class, facture.getId());
            if (managedFacture != null) {
                em.remove(managedFacture);
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression de la facture: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public List<Facture> getFacturesByClient(Client client) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Facture> query = em.createQuery(
                "SELECT f FROM Facture f " +
                "LEFT JOIN FETCH f.client " +
                "LEFT JOIN FETCH f.bonLivraison " +
                "WHERE f.client = :client " +
                "ORDER BY f.dateCreation DESC", Facture.class);
            query.setParameter("client", client);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Facture> getFacturesByStatut(Facture.StatutFacture statut) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Facture> query = em.createQuery(
                "SELECT f FROM Facture f " +
                "LEFT JOIN FETCH f.client " +
                "WHERE f.statut = :statut " +
                "ORDER BY f.dateCreation DESC", Facture.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<BonLivraison> getBonsLivraisonByClient(Client client) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BonLivraison> query = em.createQuery(
                "SELECT bl FROM BonLivraison bl " +
                "LEFT JOIN FETCH bl.client " +
                "LEFT JOIN FETCH bl.lignes " +
                "WHERE bl.client = :client " +
                "AND bl.statut IN ('LIVREE', 'PARTIELLEMENT_LIVREE') " +
                "ORDER BY bl.dateCreation DESC", BonLivraison.class);
            query.setParameter("client", client);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<BonLivraison> getAvailableBonsLivraison() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Get bons de livraison that are delivered but not yet invoiced
            TypedQuery<BonLivraison> query = em.createQuery(
                "SELECT bl FROM BonLivraison bl " +
                "LEFT JOIN FETCH bl.client " +
                "LEFT JOIN FETCH bl.lignes " +
                "WHERE bl.statut IN ('LIVREE', 'PARTIELLEMENT_LIVREE') " +
                "AND bl.id NOT IN (" +
                "  SELECT DISTINCT f.bonLivraison.id FROM Facture f " +
                "  WHERE f.bonLivraison IS NOT NULL" +
                ") " +
                "ORDER BY bl.dateCreation DESC", BonLivraison.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Facture createFromBonLivraison(BonLivraison bonLivraison) {
        if (bonLivraison == null) {
            throw new IllegalArgumentException("Le bon de livraison ne peut pas être null");
        }
        
        // Check if bon is already invoiced
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Facture> query = em.createQuery(
                "SELECT f FROM Facture f WHERE f.bonLivraison = :bonLivraison", Facture.class);
            query.setParameter("bonLivraison", bonLivraison);
            List<Facture> existingFactures = query.getResultList();
            
            if (!existingFactures.isEmpty()) {
                throw new IllegalStateException("Ce bon de livraison a déjà été facturé");
            }
        } finally {
            em.close();
        }
        
        Facture facture = new Facture();
        facture.setNumero(generateNumero());
        facture.populateFromBonLivraison(bonLivraison);
        
        return facture;
    }
    
    public Facture duplicateFacture(Facture original) {
        if (original == null) {
            return null;
        }
        
        Facture duplicate = new Facture();
        duplicate.setNumero(generateNumero());
        duplicate.setClient(original.getClient());
        duplicate.setDateFacturation(LocalDate.now());
        duplicate.setDateEcheance(original.getDateEcheance());
        duplicate.setObservations(original.getObservations());
        duplicate.setTauxTVA(original.getTauxTVA());
        duplicate.setModePaiement(original.getModePaiement());
        duplicate.setStatut(Facture.StatutFacture.BROUILLON);
        
        // Duplicate lines
        for (LigneFacture originalLigne : original.getLignes()) {
            LigneFacture duplicateLigne = new LigneFacture();
            duplicateLigne.setArticle(originalLigne.getArticle());
            duplicateLigne.setQuantite(originalLigne.getQuantite());
            duplicateLigne.setPrixUnitaire(originalLigne.getPrixUnitaire());
            duplicateLigne.setRemise(originalLigne.getRemise());
            duplicateLigne.setDescription(originalLigne.getDescription());
            
            duplicate.addLigne(duplicateLigne);
        }
        
        return duplicate;
    }
    
    public String generateNumero() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String prefix = "FAC" + today;
            
            TypedQuery<String> query = em.createQuery(
                "SELECT f.numero FROM Facture f " +
                "WHERE f.numero LIKE :prefix " +
                "ORDER BY f.numero DESC", String.class);
            query.setParameter("prefix", prefix + "%");
            query.setMaxResults(1);
            
            List<String> results = query.getResultList();
            
            int nextNumber = 1;
            if (!results.isEmpty()) {
                String lastNumero = results.get(0);
                String numberPart = lastNumero.substring(prefix.length());
                try {
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            
            return prefix + String.format("%03d", nextNumber);
        } finally {
            em.close();
        }
    }
    
    public BigDecimal getTotalFacturesAmount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(f.totalTTC), 0) FROM Facture f " +
                "WHERE f.statut != 'ANNULEE'", BigDecimal.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public BigDecimal getTotalUnpaidAmount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(f.totalTTC - f.montantPaye), 0) FROM Facture f " +
                "WHERE f.statut IN ('VALIDEE', 'ENVOYEE', 'PARTIELLEMENT_PAYEE', 'EN_RETARD')", BigDecimal.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public List<Facture> getOverdueFactures() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            TypedQuery<Facture> query = em.createQuery(
                "SELECT f FROM Facture f " +
                "LEFT JOIN FETCH f.client " +
                "WHERE f.dateEcheance < :today " +
                "AND f.statut NOT IN ('PAYEE', 'ANNULEE') " +
                "AND (f.totalTTC - f.montantPaye) > 0 " +
                "ORDER BY f.dateEcheance ASC", Facture.class);
            query.setParameter("today", today);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
