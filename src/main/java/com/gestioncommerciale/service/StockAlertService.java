package com.gestioncommerciale.service;

import java.util.List;

import com.gestioncommerciale.model.Article;

/**
 * Service pour gérer les alertes de stock
 */
public class StockAlertService {
    
    private ArticleService articleService;
    
    public StockAlertService() {
        this.articleService = new ArticleService();
    }
    
    /**
     * Obtient tous les articles en alerte de stock
     * @return Liste des articles dont le stock actuel est inférieur ou égal au seuil minimum
     */
    public List<Article> getStockAlerts() {
        return articleService.findLowStockArticles();
    }
    
    /**
     * Vérifie si un article est en alerte de stock
     * @param article L'article à vérifier
     * @return true si l'article est en alerte
     */
    public boolean isStockAlert(Article article) {
        return article != null && article.isStockLow();
    }
    
    /**
     * Vérifie les alertes après une modification de stock
     * Cette méthode doit être appelée après chaque vente ou modification de stock
     * @param articleId L'ID de l'article modifié
     * @return true si l'article est maintenant en alerte
     */
    public boolean checkStockAlertAfterUpdate(Long articleId) {
        Article article = articleService.findById(articleId);
        return isStockAlert(article);
    }
    
    /**
     * Met à jour le stock d'un article et vérifie les alertes
     * @param articleId L'ID de l'article
     * @param newStock Le nouveau stock
     * @return true si l'article est en alerte après la mise à jour
     */
    public boolean updateStockAndCheckAlert(Long articleId, int newStock) {
        return updateStockAndCheckAlert(articleId, newStock, null);
    }
    
    /**
     * Met à jour le stock d'un article avec motif et vérifie les alertes
     * @param articleId L'ID de l'article
     * @param newStock Le nouveau stock
     * @param reason Le motif de la modification (optionnel)
     * @return true si l'article est en alerte après la mise à jour
     */
    public boolean updateStockAndCheckAlert(Long articleId, int newStock, String reason) {
        articleService.updateStock(articleId, newStock);
        // Le motif pourrait être utilisé pour un historique des mouvements de stock
        return checkStockAlertAfterUpdate(articleId);
    }
    
    /**
     * Diminue le stock d'un article (pour une vente) et vérifie les alertes
     * @param articleId L'ID de l'article
     * @param quantitySold La quantité vendue
     * @return true si l'article est en alerte après la vente
     * @throws RuntimeException si le stock est insuffisant
     */
    public boolean sellAndCheckAlert(Long articleId, int quantitySold) {
        Article article = articleService.findById(articleId);
        if (article == null) {
            throw new RuntimeException("Article introuvable");
        }
        
        int currentStock = article.getStockActuel();
        if (currentStock < quantitySold) {
            throw new RuntimeException("Stock insuffisant. Stock actuel: " + currentStock + ", quantité demandée: " + quantitySold);
        }
        
        int newStock = currentStock - quantitySold;
        return updateStockAndCheckAlert(articleId, newStock);
    }
    
    /**
     * Augmente le stock d'un article (pour un réapprovisionnement) et vérifie les alertes
     * @param articleId L'ID de l'article
     * @param quantityAdded La quantité ajoutée
     * @return true si l'article est encore en alerte après l'ajout
     */
    public boolean restockAndCheckAlert(Long articleId, int quantityAdded) {
        Article article = articleService.findById(articleId);
        if (article == null) {
            throw new RuntimeException("Article introuvable");
        }
        
        int newStock = article.getStockActuel() + quantityAdded;
        return updateStockAndCheckAlert(articleId, newStock);
    }
    
    /**
     * Obtient le nombre d'articles en alerte
     * @return Le nombre d'articles en alerte de stock
     */
    public int getStockAlertCount() {
        return getStockAlerts().size();
    }
    
    /**
     * Obtient les articles critiques (stock = 0)
     * @return Liste des articles en rupture de stock
     */
    public List<Article> getCriticalStockArticles() {
        return articleService.getAllArticles().stream()
                .filter(article -> article.isActive() && article.getStockActuel() == 0)
                .toList();
    }
}
