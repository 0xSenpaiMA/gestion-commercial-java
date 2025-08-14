# Système de Gestion des Alertes de Stock

## Vue d'ensemble

Le système d'alertes de stock a été implémenté pour surveiller automatiquement les niveaux de stock et alerter l'utilisateur lorsque des articles atteignent ou dépassent leur seuil d'alerte.

## Composants Implémentés

### 1. Service Principal - `StockAlertService`

Le service principal pour la gestion des alertes de stock avec les méthodes suivantes :

- **`getStockAlerts()`** : Récupère tous les articles en alerte de stock
- **`isStockAlert(Article)`** : Vérifie si un article spécifique est en alerte
- **`updateStockAndCheckAlert()`** : Met à jour le stock et vérifie les alertes
- **`sellAndCheckAlert()`** : Gère les ventes avec vérification d'alertes
- **`restockAndCheckAlert()`** : Gère le réapprovisionnement
- **`getStockAlertCount()`** : Compte le nombre d'articles en alerte
- **`getCriticalStockArticles()`** : Articles avec stock = 0

### 2. Interface Utilisateur - `StockManagementFrame`

Fenêtre dédiée à la gestion du stock avec :

- **Table interactive** avec indicateurs visuels (lignes colorées)
- **Filtrage** : Afficher tous les articles ou seulement les alertes
- **Statut coloré** : 
  - 🟢 Vert = Stock normal
  - 🟠 Orange = Alerte de stock
  - 🔴 Rouge = Rupture de stock
- **Actions** : Modification du stock, consultation des détails
- **Compteur d'alertes** en temps réel

### 3. Dialogues Auxiliaires

#### `StockUpdateDialog`
- Modification du stock avec trois modes :
  - Ajouter au stock existant
  - Retirer du stock
  - Définir un nouveau stock absolu
- Validation des quantités négatives
- Motif optionnel pour traçabilité

#### `ArticleDetailsDialog`
- Affichage complet des informations article
- Calcul automatique des marges
- Statut de stock avec indicateurs visuels

### 4. Service d'Intégration - `StockAlertIntegrationService`

Service pour intégrer les alertes dans les workflows existants :

- **`processSaleWithStockCheck()`** : Traite les ventes avec alertes automatiques
- **`checkStockAvailabilityBeforeSale()`** : Vérification préventive avant vente
- **`processRestockWithAlertCheck()`** : Gestion du réapprovisionnement
- **`getStockAlertSummary()`** : Résumé des alertes pour tableaux de bord

## Comment Utiliser le Système

### 1. Accès à la Gestion de Stock

Dans l'application principale :
- Menu **Gestion** → **Stock**
- Ou directement via `MainFrame.showStockManagement()`

### 2. Surveillance des Alertes

L'interface affiche automatiquement :
- Nombre total d'articles en alerte
- Nombre d'articles en rupture
- Status coloré par ligne dans le tableau

### 3. Actions Possibles

#### Modification du Stock
1. Sélectionner un article dans le tableau
2. Cliquer sur "Modifier Stock" ou double-cliquer
3. Choisir le type d'opération
4. Saisir la quantité ou nouveau stock
5. Ajouter un motif (optionnel)

#### Filtrage
- Cocher "Afficher uniquement les alertes" pour voir seulement les articles problématiques
- Le compteur reste visible même en mode filtré

### 4. Intégration dans les Ventes

Pour intégrer les alertes dans vos processus de vente existants :

```java
// Exemple d'utilisation dans FactureService
StockAlertIntegrationService alertIntegration = new StockAlertIntegrationService();

// Avant la vente - vérification
if (alertIntegration.checkStockAvailabilityBeforeSale(lignesFacture)) {
    // Procéder à la vente
    List<Article> articlesEnAlerte = alertIntegration.processSaleWithStockCheck(lignesFacture);
    
    // Afficher les alertes générées
    alertIntegration.showStockAlertsAfterSale(articlesEnAlerte);
}
```

## Configuration des Seuils

Les seuils d'alerte sont configurés au niveau de chaque article :

- **`stockMin`** : Seuil d'alerte (déclenche l'alerte quand `stockActuel <= stockMin`)
- **`stockMax`** : Seuil maximum recommandé
- **`stockActuel`** : Stock en cours

## Indicateurs Visuels

### Dans les Tableaux
- **Fond vert clair** : Stock normal (> seuil minimum)
- **Fond orange clair** : Alerte de stock (≤ seuil minimum mais > 0)
- **Fond rouge clair** : Rupture de stock (= 0)

### Statut Texte
- **"OK"** en vert : Stock suffisant
- **"ALERTE"** en orange : Stock faible
- **"RUPTURE"** en rouge : Stock épuisé

## Points d'Extension

Le système est conçu pour être extensible :

1. **Historique des mouvements** : Le paramètre `reason` dans `updateStockAndCheckAlert()` peut être utilisé pour un système de logs
2. **Notifications** : Possibilité d'ajouter des notifications email/SMS
3. **Seuils dynamiques** : Algorithmes de calcul automatique des seuils
4. **Prévisions** : Intégration de calculs de réapprovisionnement automatique

## Architecture

Le système respecte l'architecture MVC existante :

- **Model** : `Article` avec méthode `isStockLow()`
- **View** : `StockManagementFrame`, dialogues auxiliaires
- **Controller/Service** : `StockAlertService`, `StockAlertIntegrationService`

## Compatibilité

- Compatible avec l'architecture existante
- N'affecte pas les fonctionnalités existantes
- Utilise les services `ArticleService` existants
- Intégrable facilement dans `FactureService` et `DevisService`
