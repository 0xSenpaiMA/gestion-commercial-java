package com.gestioncommerciale.service;

import java.util.List;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.CompanyInfo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * Service class for company information management
 */
public class CompanyInfoService {
    
    /**
     * Get all companies
     */
    public List<CompanyInfo> getAllCompanies() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<CompanyInfo> query = em.createQuery("SELECT c FROM CompanyInfo c ORDER BY c.raisonSociale", CompanyInfo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Find company by ID
     */
    public CompanyInfo findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.find(CompanyInfo.class, id);
        } finally {
            em.close();
        }
    }
    
    /**
     * Find company by raison sociale
     */
    public CompanyInfo findByRaisonSociale(String raisonSociale) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<CompanyInfo> query = em.createQuery("SELECT c FROM CompanyInfo c WHERE c.raisonSociale = :raisonSociale", CompanyInfo.class);
            query.setParameter("raisonSociale", raisonSociale);
            List<CompanyInfo> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    /**
     * Save or update a company
     */
    public CompanyInfo save(CompanyInfo companyInfo) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            CompanyInfo savedCompany;
            if (companyInfo.getId() == null) {
                // New company
                em.persist(companyInfo);
                savedCompany = companyInfo;
            } else {
                // Update existing company
                savedCompany = em.merge(companyInfo);
            }
            
            transaction.commit();
            return savedCompany;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde des informations de l'entreprise", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Delete a company by ID
     */
    public void delete(Long companyId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            CompanyInfo companyInfo = em.find(CompanyInfo.class, companyId);
            if (companyInfo != null) {
                em.remove(companyInfo);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression des informations de l'entreprise", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get the first company (main company)
     */
    public CompanyInfo getMainCompany() {
        List<CompanyInfo> companies = getAllCompanies();
        return companies.isEmpty() ? null : companies.get(0);
    }
    
    /**
     * Get count of companies
     */
    public long getCompanyCount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(c) FROM CompanyInfo c", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}