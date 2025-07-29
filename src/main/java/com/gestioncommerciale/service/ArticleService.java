package com.gestioncommerciale.service;

import java.util.List;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.model.Article;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

/**
 * Service class for article management
 */
public class ArticleService {
    
    /**
     * Get all articles
     */
    public List<Article> getAllArticles() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Article> query = em.createQuery("SELECT a FROM Article a ORDER BY a.designation", Article.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get active articles only
     */
    public List<Article> getActiveArticles() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Article> query = em.createQuery("SELECT a FROM Article a WHERE a.active = true ORDER BY a.designation", Article.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Find article by ID
     */
    public Article findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.find(Article.class, id);
        } finally {
            em.close();
        }
    }
    
    /**
     * Find article by code
     */
    public Article findByCode(String code) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Article> query = em.createQuery("SELECT a FROM Article a WHERE a.code = :code", Article.class);
            query.setParameter("code", code);
            List<Article> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
    
    /**
     * Find articles by category
     */
    public List<Article> findByCategory(String category) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Article> query = em.createQuery("SELECT a FROM Article a WHERE a.categorie = :category ORDER BY a.designation", Article.class);
            query.setParameter("category", category);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Find articles with low stock (stock <= minimum stock)
     */
    public List<Article> findLowStockArticles() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Article> query = em.createQuery(
                "SELECT a FROM Article a WHERE a.stockActuel <= a.stockMin AND a.active = true ORDER BY a.designation", 
                Article.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Search articles by designation or code
     */
    public List<Article> searchArticles(String searchTerm) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Article> query = em.createQuery(
                "SELECT a FROM Article a WHERE LOWER(a.designation) LIKE :searchTerm OR LOWER(a.code) LIKE :searchTerm ORDER BY a.designation", 
                Article.class);
            query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Save or update an article
     */
    public Article save(Article article) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            
            Article savedArticle;
            if (article.getId() == null) {
                // New article - ensure all required fields are set
                if (article.getCreatedAt() == null) {
                    article.setCreatedAt(java.time.LocalDateTime.now().toString());
                }
                em.persist(article);
                savedArticle = article;
            } else {
                // Update existing article
                savedArticle = em.merge(article);
            }
            
            em.flush(); // Force immediate write to database
            transaction.commit();
            return savedArticle;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    // Log but don't throw rollback exception
                }
            }
            throw new RuntimeException("Erreur lors de la sauvegarde de l'article: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Delete an article by ID
     */
    public void delete(Long articleId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            Article article = em.find(Article.class, articleId);
            if (article != null) {
                em.remove(article);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression de l'article", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Update stock quantity
     */
    public void updateStock(Long articleId, int newStock) {
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        
        try {
            transaction.begin();
            
            Article article = em.find(Article.class, articleId);
            if (article != null) {
                article.setStockActuel(newStock);
                em.merge(article);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la mise à jour du stock", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get count of active articles
     */
    public long getActiveArticleCount() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(a) FROM Article a WHERE a.active = true", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get count of articles by category
     */
    public long getArticleCountByCategory(String category) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(a) FROM Article a WHERE a.categorie = :category", Long.class);
            query.setParameter("category", category);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
    
    /**
     * Get all distinct categories
     */
    public List<String> getAllCategories() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery("SELECT DISTINCT a.categorie FROM Article a WHERE a.categorie IS NOT NULL ORDER BY a.categorie", String.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}