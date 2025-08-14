# Fonctionnalité d'Envoi de Situation Clients par E-mail

## Vue d'ensemble

Cette fonctionnalité permet d'envoyer par e-mail la situation financière complète d'un client, incluant toutes ses factures, les montants dus, les paiements effectués, et un résumé de son état de compte.

## Composants Implémentés

### 1. Service E-mail - `EmailService`

Service principal pour l'envoi d'e-mails avec configuration SMTP :

#### Configuration SMTP
- **Serveur SMTP** : smtp.gmail.com
- **Port** : 465 (SSL)
- **Authentification** : abdelazizaitbenihya@gmail.com
- **Chiffrement** : SSL/TLS
- **Expéditeur** : "From gestion de gestion commercial logiciel by Omni Networks"

#### Méthodes principales
- **`envoyerSituationClient(Client, String)`** : Envoie la situation complète d'un client
- **`genererRapportSituationClient(Client)`** : Génère le rapport HTML formaté
- **`envoyerEmail(String, String, String, boolean)`** : Méthode générique d'envoi d'e-mail
- **`testerConfiguration()`** : Teste la configuration SMTP

### 2. Interface Utilisateur

#### ClientManagementFrame - Bouton d'envoi
- Bouton **"Envoyer situation par e-mail"** ajouté à la barre d'outils
- Actif uniquement quand un client est sélectionné
- Lancement du dialogue de saisie d'adresse e-mail

#### EmailInputDialog - Saisie de l'adresse
- Pré-remplissage avec l'e-mail du client (si disponible)
- Validation basique du format e-mail
- Confirmation avant envoi

#### EmailTestDialog - Test de configuration
- Accessible via Menu **Configuration > Tester configuration e-mail**
- Envoi d'un e-mail de test à une adresse spécifiée
- Vérification du bon fonctionnement de la configuration SMTP

### 3. Rapport Généré

Le rapport de situation client contient :

#### Informations Client
- Nom et prénom
- Coordonnées (e-mail, téléphone, adresse)
- Statut du compte

#### Résumé Financier
- Nombre total de factures
- Nombre de factures payées/en cours
- Total facturé (TTC)
- Total payé
- **Reste dû** (mise en évidence)

#### Détail des Factures
Tableau complet avec :
- Numéro de facture
- Date
- Montant TTC
- Montant payé
- Reste dû
- Statut (Payée/Partiel/Impayée avec codes couleur)

#### Formatage HTML
- Design professionnel avec CSS intégré
- Codes couleur pour les statuts
- Tableaux lisibles avec alternance de lignes
- En-tête et pied de page personnalisés

## Utilisation

### 1. Envoyer la Situation d'un Client

1. Ouvrir **Gestion > Clients/Fournisseurs**
2. Sélectionner un client dans la liste
3. Cliquer sur **"Envoyer situation par e-mail"**
4. Saisir l'adresse e-mail de destination
5. Confirmer l'envoi

### 2. Tester la Configuration E-mail

1. Menu **Configuration > Tester configuration e-mail** (accessible aux managers)
2. Saisir une adresse e-mail de test
3. Cliquer sur **"Tester"**
4. Vérifier la réception de l'e-mail de test

### 3. Traitement Asynchrone

- L'envoi s'effectue dans un thread séparé pour ne pas bloquer l'interface
- Dialogue de progression pendant l'envoi
- Messages de succès ou d'erreur à la fin

## Configuration Technique

### Dépendances Maven

```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.1</version>
</dependency>
```

### Configuration SMTP

Les paramètres sont définis dans `EmailService.java` :

```java
private static final String MAIL_HOST = "smtp.gmail.com";
private static final String MAIL_PORT = "465";
private static final String MAIL_USERNAME = "abdelazizaitbenihya@gmail.com";
private static final String MAIL_PASSWORD = "tzjxrfkpevicsrwd";
// ...
```

### Sécurité

- Utilisation d'un mot de passe d'application Gmail
- Connexion SSL sécurisée
- Validation des adresses e-mail avant envoi

## Architecture

### Respect du Pattern MVC

- **Model** : Utilise `Client` et `Facture` existants
- **View** : `ClientManagementFrame`, `EmailInputDialog`, `EmailTestDialog`  
- **Controller/Service** : `EmailService` pour la logique métier

### Intégration

- Aucune modification des classes existantes
- Utilise `ClientService` et `FactureService` existants
- Ajout transparent à l'interface de gestion des clients

## Gestion d'Erreurs

### Types d'erreurs gérées
- **Connexion SMTP** : Problème de réseau ou configuration
- **Authentification** : Identifiants incorrects
- **Format e-mail** : Validation côté client
- **Données manquantes** : Client sans factures

### Messages d'erreur
- Messages explicites pour l'utilisateur
- Suggestions de résolution
- Logs détaillés pour le débogage

## Exemples d'utilisation

### Envoi de situation standard
```java
EmailService emailService = new EmailService();
Client client = clientService.findById(clientId);
emailService.envoyerSituationClient(client, "client@exemple.com");
```

### Test de configuration
```java
EmailService emailService = new EmailService();
emailService.testerConfiguration(); // Envoie à l'expéditeur configuré
```

## Extensions Possibles

1. **Modèles d'e-mail personnalisables**
2. **Pièces jointes PDF** (factures détaillées)
3. **Programmation d'envois automatiques**
4. **Historique des e-mails envoyés**
5. **Configuration SMTP via interface**
6. **Support de multiples comptes e-mail**
7. **Templates personnalisés par client**

## Dépannage

### Problèmes courants

1. **"Authentification échouée"**
   - Vérifier le mot de passe d'application Gmail
   - S'assurer que l'authentification à 2 facteurs est activée

2. **"Connexion refusée"**
   - Vérifier la connexion Internet
   - Contrôler les paramètres de pare-feu

3. **"Format d'e-mail invalide"**
   - Valider l'adresse e-mail de destination
   - Vérifier les caractères spéciaux

4. **"Aucune facture trouvée"**
   - Vérifier que le client a des factures
   - Contrôler la relation client-factures dans la base

### Logs et Debugging

Le service utilise le système de logging configuré dans l'application pour tracer les erreurs et les succès d'envoi.
