package com.gestioncommerciale.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.BonCommande;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.LigneCommande;

/**
 * Service class for BonCommande operations
 */
public class BonCommandeService {
    
    public List<BonCommande> getAllBonsCommande() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BonCommande> query = em.createQuery(
                "SELECT bc FROM BonCommande bc " +
                "LEFT JOIN FETCH bc.fournisseur " +
                "LEFT JOIN FETCH bc.lignes l " +
                "LEFT JOIN FETCH l.article " +
                "ORDER BY bc.dateCreation DESC", BonCommande.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public BonCommande getBonCommandeById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            BonCommande bonCommande = em.find(BonCommande.class, id);
            if (bonCommande != null) {
                // Force loading of lazy relationships
                bonCommande.getFournisseur().getNom();
                bonCommande.getLignes().size();
                for (LigneCommande ligne : bonCommande.getLignes()) {
                    ligne.getArticle().getDesignation();
                }
            }
            return bonCommande;
        } finally {
            em.close();
        }
    }
    
    public BonCommande save(BonCommande bonCommande) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Generate numero if not set
            if (bonCommande.getNumero() == null || bonCommande.getNumero().trim().isEmpty()) {
                bonCommande.setNumero(generateNumero());
            }
            
            // Calculate totals before saving
            bonCommande.calculateTotals();
            
            BonCommande savedBonCommande;
            if (bonCommande.getId() == null) {
                em.persist(bonCommande);
                savedBonCommande = bonCommande;
            } else {
                savedBonCommande = em.merge(bonCommande);
            }
            
            em.getTransaction().commit();
            return savedBonCommande;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde du bon de commande: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(BonCommande bonCommande) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            em.getTransaction().begin();
            
            BonCommande managedBonCommande = em.find(BonCommande.class, bonCommande.getId());
            if (managedBonCommande != null) {
                em.remove(managedBonCommande);
            }
            
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la suppression du bon de commande: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public List<BonCommande> getBonsCommandeByFournisseur(Client fournisseur) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BonCommande> query = em.createQuery(
                "SELECT bc FROM BonCommande bc " +
                "LEFT JOIN FETCH bc.fournisseur " +
                "LEFT JOIN FETCH bc.lignes " +
                "WHERE bc.fournisseur = :fournisseur " +
                "ORDER BY bc.dateCreation DESC", BonCommande.class);
            query.setParameter("fournisseur", fournisseur);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<BonCommande> getBonsCommandeByStatut(BonCommande.StatutCommande statut) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BonCommande> query = em.createQuery(
                "SELECT bc FROM BonCommande bc " +
                "LEFT JOIN FETCH bc.fournisseur " +
                "WHERE bc.statut = :statut " +
                "ORDER BY bc.dateCreation DESC", BonCommande.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Client> getFournisseurs() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                "SELECT c FROM Client c " +
                "WHERE c.type IN ('FOURNISSEUR', 'CLIENT_FOURNISSEUR') " +
                "AND c.active = true " +
                "ORDER BY c.nom ASC", Client.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public BonCommande duplicateBonCommande(BonCommande original) {
        if (original == null) {
            return null;
        }
        
        BonCommande duplicate = new BonCommande();
        duplicate.setNumero(generateNumero());
        duplicate.setFournisseur(original.getFournisseur());
        duplicate.setDateCommande(LocalDate.now());
        duplicate.setDateLivraisonPrevue(original.getDateLivraisonPrevue());
        duplicate.setAdresseLivraison(original.getAdresseLivraison());
        duplicate.setConditionsPaiement(original.getConditionsPaiement());
        duplicate.setObservations(original.getObservations());
        duplicate.setTauxTVA(original.getTauxTVA());
        duplicate.setStatut(BonCommande.StatutCommande.BROUILLON);
        duplicate.setNumeroDevisFournisseur(original.getNumeroDevisFournisseur());
        
        // Duplicate lines
        for (LigneCommande originalLigne : original.getLignes()) {
            LigneCommande duplicateLigne = new LigneCommande();
            duplicateLigne.setArticle(originalLigne.getArticle());
            duplicateLigne.setQuantite(originalLigne.getQuantite());
            duplicateLigne.setPrixUnitaire(originalLigne.getPrixUnitaire());
            duplicateLigne.setRemise(originalLigne.getRemise());
            duplicateLigne.setDescription(originalLigne.getDescription());
            duplicateLigne.setDateLivraisonSouhaitee(originalLigne.getDateLivraisonSouhaitee());
            duplicateLigne.setReferenceFournisseur(originalLigne.getReferenceFournisseur());
            
            duplicate.addLigne(duplicateLigne);
        }
        
        return duplicate;
    }
    
    public String generateNumero() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String prefix = "BC" + today;
            
            TypedQuery<String> query = em.createQuery(
                "SELECT bc.numero FROM BonCommande bc " +
                "WHERE bc.numero LIKE :prefix " +
                "ORDER BY bc.numero DESC", String.class);
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
    
    public BigDecimal getTotalBonsCommandeAmount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(bc.totalTTC), 0) FROM BonCommande bc " +
                "WHERE bc.statut != 'ANNULEE'", BigDecimal.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public List<BonCommande> getBonsCommandeEnAttente() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<BonCommande> query = em.createQuery(
                "SELECT bc FROM BonCommande bc " +
                "LEFT JOIN FETCH bc.fournisseur " +
                "WHERE bc.statut IN ('CONFIRMEE', 'ENVOYEE', 'ACCEPTEE', 'EN_COURS') " +
                "ORDER BY bc.dateCommande ASC", BonCommande.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<BonCommande> getBonsCommandeEnRetard() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            TypedQuery<BonCommande> query = em.createQuery(
                "SELECT bc FROM BonCommande bc " +
                "LEFT JOIN FETCH bc.fournisseur " +
                "WHERE bc.dateLivraisonPrevue < :today " +
                "AND bc.statut IN ('CONFIRMEE', 'ENVOYEE', 'ACCEPTEE', 'EN_COURS') " +
                "ORDER BY bc.dateLivraisonPrevue ASC", BonCommande.class);
            query.setParameter("today", today);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
