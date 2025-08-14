package com.gestioncommerciale.service;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.model.LigneFacture;

/**
 * Service pour intégrer les alertes de stock dans les processus de vente
 * Ce service montre comment utiliser StockAlertService dans les workflows existants
 */
public class StockAlertIntegrationService {
    
    private final StockAlertService stockAlertService;
    private final ArticleService articleService;
    
    public StockAlertIntegrationService() {
        this.stockAlertService = new StockAlertService();
        this.articleService = new ArticleService();
    }
    
    /**
     * Traite une vente en vérifiant les alertes de stock après chaque article vendu
     * @param lignesVente Les lignes de vente à traiter
     * @return Liste des articles maintenant en alerte
     */
    public List<Article> processSaleWithStockCheck(List<LigneFacture> lignesVente) {
        List<Article> articlesInAlert = new ArrayList<>();
        
        for (LigneFacture ligne : lignesVente) {
            Article article = ligne.getArticle();
            int quantiteVendue = ligne.getQuantite().intValue();
            
            try {
                // Effectuer la vente et vérifier les alertes
                boolean isNowInAlert = stockAlertService.sellAndCheckAlert(
                    article.getId(), 
                    quantiteVendue
                );
                
                if (isNowInAlert) {
                    // Recharger l'article pour avoir le stock à jour
                    Article updatedArticle = articleService.findById(article.getId());
                    articlesInAlert.add(updatedArticle);
                }
                
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du traitement de la vente pour l'article " + 
                    article.getDesignation() + ": " + e.getMessage(), e);
            }
        }
        
        return articlesInAlert;
    }
    
    /**
     * Affiche les alertes de stock générées par une vente
     * @param articlesInAlert Articles maintenant en alerte
     */
    public void showStockAlertsAfterSale(List<Article> articlesInAlert) {
        if (articlesInAlert.isEmpty()) {
            return;
        }
        
        StringBuilder message = new StringBuilder();
        message.append("⚠ ALERTES DE STOCK GÉNÉRÉES PAR CETTE VENTE ⚠\n\n");
        
        for (Article article : articlesInAlert) {
            message.append("• ").append(article.getDesignation()).append("\n");
            message.append("  Code: ").append(article.getCode()).append("\n");
            message.append("  Stock actuel: ").append(article.getStockActuel());
            message.append(" (Seuil: ").append(article.getStockMin()).append(")\n");
            
            if (article.getStockActuel() == 0) {
                message.append("  ⚠ RUPTURE DE STOCK\n");
            }
            message.append("\n");
        }
        
        message.append("Recommandation: Vérifiez votre stock et passez commande si nécessaire.");
        
        JOptionPane.showMessageDialog(
            null,
            message.toString(),
            "Alertes de Stock - " + articlesInAlert.size() + " article(s)",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Vérifie le stock avant une vente pour éviter les ruptures
     * @param lignesVente Les lignes de vente prévues
     * @return true si toutes les quantités sont disponibles
     */
    public boolean checkStockAvailabilityBeforeSale(List<LigneFacture> lignesVente) {
        List<String> stockIssues = new ArrayList<>();
        
        for (LigneFacture ligne : lignesVente) {
            Article article = ligne.getArticle();
            int quantiteDemandee = ligne.getQuantite().intValue();
            int stockActuel = article.getStockActuel();
            
            if (stockActuel < quantiteDemandee) {
                String issue = String.format(
                    "• %s: Stock insuffisant (Disponible: %d, Demandé: %d)",
                    article.getDesignation(), stockActuel, quantiteDemandee
                );
                stockIssues.add(issue);
            } else if (stockActuel - quantiteDemandee <= article.getStockMin()) {
                String warning = String.format(
                    "• %s: La vente générera une alerte de stock (Nouveau stock: %d, Seuil: %d)",
                    article.getDesignation(), stockActuel - quantiteDemandee, article.getStockMin()
                );
                stockIssues.add(warning);
            }
        }
        
        if (!stockIssues.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append("VÉRIFICATION DU STOCK:\n\n");
            
            for (String issue : stockIssues) {
                message.append(issue).append("\n");
            }
            
            message.append("\nVoulez-vous continuer malgré ces alertes?");
            
            int choice = JOptionPane.showConfirmDialog(
                null,
                message.toString(),
                "Vérification Stock",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            return choice == JOptionPane.YES_OPTION;
        }
        
        return true; // Aucun problème de stock
    }
    
    /**
     * Traite un réapprovisionnement avec vérification des alertes
     * @param articleId L'ID de l'article
     * @param quantiteAjoutee La quantité ajoutée au stock
     * @return true si l'article n'est plus en alerte après le réapprovisionnement
     */
    public boolean processRestockWithAlertCheck(Long articleId, int quantiteAjoutee) {
        try {
            boolean wasInAlert = stockAlertService.isStockAlert(
                articleService.findById(articleId)
            );
            
            boolean stillInAlert = stockAlertService.restockAndCheckAlert(
                articleId, 
                quantiteAjoutee
            );
            
            if (wasInAlert && !stillInAlert) {
                Article article = articleService.findById(articleId);
                JOptionPane.showMessageDialog(
                    null,
                    "✓ L'article \"" + article.getDesignation() + 
                    "\" n'est plus en alerte de stock.\n" +
                    "Nouveau stock: " + article.getStockActuel(),
                    "Alerte Résolue",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return true;
            }
            
            return !stillInAlert;
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du réapprovisionnement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtient un résumé des alertes de stock actuel
     * @return Message formaté avec le résumé des alertes
     */
    public String getStockAlertSummary() {
        try {
            List<Article> alertArticles = stockAlertService.getStockAlerts();
            List<Article> criticalArticles = stockAlertService.getCriticalStockArticles();
            
            if (alertArticles.isEmpty()) {
                return "✓ Aucune alerte de stock";
            }
            
            StringBuilder summary = new StringBuilder();
            summary.append(String.format("⚠ %d article(s) en alerte de stock", alertArticles.size()));
            
            if (!criticalArticles.isEmpty()) {
                summary.append(String.format(" (%d en rupture)", criticalArticles.size()));
            }
            
            return summary.toString();
        } catch (Exception e) {
            return "Erreur lors de la vérification des alertes";
        }
    }
}
