package com.gestioncommerciale.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Facture;

/**
 * Service pour l'envoi d'e-mails
 */
public class EmailService {
    
    // Configuration SMTP
    private static final String MAIL_HOST = "smtp.gmail.com";
    private static final String MAIL_PORT = "465";
    private static final String MAIL_USERNAME = "abdelazizaitbenihya@gmail.com";
    private static final String MAIL_PASSWORD = "tzjxrfkpevicsrwd";
    private static final String MAIL_FROM_NAME = "From gestion de gestion commercial logiciel by Omni Networks";
    private static final String MAIL_FROM_ADDRESS = "abdelazizaitbenihya@gmail.com";
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final FactureService factureService;
    
    public EmailService() {
        this.factureService = new FactureService();
    }
    
    /**
     * Envoie la situation d'un client par e-mail
     */
    public void envoyerSituationClient(Client client, String emailDestinataire) throws MessagingException {
        if (client == null) {
            throw new IllegalArgumentException("Le client ne peut pas être null");
        }
        
        if (emailDestinataire == null || emailDestinataire.trim().isEmpty()) {
            throw new IllegalArgumentException("L'adresse e-mail de destination est requise");
        }
        
        // Générer le contenu du rapport
        String contenuRapport = genererRapportSituationClient(client);
        String sujet = "Situation client - " + client.getNom() + 
                      (client.getPrenom() != null ? " " + client.getPrenom() : "");
        
        // Envoyer l'e-mail
        envoyerEmail(emailDestinataire, sujet, contenuRapport, true);
    }
    
    /**
     * Génère le rapport de situation d'un client au format HTML
     */
    private String genererRapportSituationClient(Client client) {
        StringBuilder html = new StringBuilder();
        
        try {
            // Récupérer les factures du client
            List<Facture> factures = factureService.getFacturesByClient(client);
            
            // Calculer les totaux
            BigDecimal totalFacture = BigDecimal.ZERO;
            BigDecimal totalPaye = BigDecimal.ZERO;
            BigDecimal totalDu = BigDecimal.ZERO;
            int nombreFactures = factures.size();
            int facturesPayees = 0;
            int facturesEnCours = 0;
            
            for (Facture facture : factures) {
                if (facture.getTotalTTC() != null) {
                    totalFacture = totalFacture.add(facture.getTotalTTC());
                }
                
                BigDecimal montantPaye = facture.getMontantPaye() != null ? facture.getMontantPaye() : BigDecimal.ZERO;
                totalPaye = totalPaye.add(montantPaye);
                
                BigDecimal resteDu = (facture.getTotalTTC() != null ? facture.getTotalTTC() : BigDecimal.ZERO)
                    .subtract(montantPaye);
                totalDu = totalDu.add(resteDu);
                
                if (resteDu.compareTo(BigDecimal.ZERO) <= 0) {
                    facturesPayees++;
                } else {
                    facturesEnCours++;
                }
            }
            
            // Début du HTML
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("<meta charset='UTF-8'>\n");
            html.append("<title>Situation Client</title>\n");
            html.append("<style>\n");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            html.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n");
            html.append("h2 { color: #34495e; margin-top: 30px; }\n");
            html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
            html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n");
            html.append("th { background-color: #f2f2f2; font-weight: bold; }\n");
            html.append("tr:nth-child(even) { background-color: #f9f9f9; }\n");
            html.append(".info-box { background-color: #e8f4fd; padding: 15px; border-radius: 5px; margin: 20px 0; }\n");
            html.append(".summary-box { background-color: #f0f8f0; padding: 15px; border-radius: 5px; margin: 20px 0; }\n");
            html.append(".amount { text-align: right; font-weight: bold; }\n");
            html.append(".status-paye { color: #27ae60; font-weight: bold; }\n");
            html.append(".status-partiel { color: #f39c12; font-weight: bold; }\n");
            html.append(".status-impaye { color: #e74c3c; font-weight: bold; }\n");
            html.append("</style>\n");
            html.append("</head>\n<body>\n");
            
            // En-tête
            html.append("<h1>Situation Client</h1>\n");
            html.append("<div class='info-box'>\n");
            html.append("<h2>Informations Client</h2>\n");
            html.append("<p><strong>Nom :</strong> ").append(client.getNom());
            if (client.getPrenom() != null && !client.getPrenom().trim().isEmpty()) {
                html.append(" ").append(client.getPrenom());
            }
            html.append("</p>\n");
            if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
                html.append("<p><strong>E-mail :</strong> ").append(client.getEmail()).append("</p>\n");
            }
            if (client.getTelephone() != null && !client.getTelephone().trim().isEmpty()) {
                html.append("<p><strong>Téléphone :</strong> ").append(client.getTelephone()).append("</p>\n");
            }
            if (client.getAdresse() != null && !client.getAdresse().trim().isEmpty()) {
                html.append("<p><strong>Adresse :</strong> ").append(client.getAdresse()).append("</p>\n");
            }
            html.append("<p><strong>Date de création :</strong> Non spécifiée</p>\n");
            html.append("</div>\n");
            
            // Résumé financier
            html.append("<div class='summary-box'>\n");
            html.append("<h2>Résumé Financier</h2>\n");
            html.append("<p><strong>Nombre total de factures :</strong> ").append(nombreFactures).append("</p>\n");
            html.append("<p><strong>Factures payées :</strong> ").append(facturesPayees).append("</p>\n");
            html.append("<p><strong>Factures en cours :</strong> ").append(facturesEnCours).append("</p>\n");
            html.append("<p><strong>Total facturé :</strong> ").append(CURRENCY_FORMAT.format(totalFacture)).append(" DH</p>\n");
            html.append("<p><strong>Total payé :</strong> ").append(CURRENCY_FORMAT.format(totalPaye)).append(" DH</p>\n");
            html.append("<p><strong>Reste dû :</strong> <span style='color: ");
            html.append(totalDu.compareTo(BigDecimal.ZERO) > 0 ? "#e74c3c" : "#27ae60");
            html.append("; font-size: 1.2em;'>").append(CURRENCY_FORMAT.format(totalDu)).append(" DH</span></p>\n");
            html.append("</div>\n");
            
            // Détail des factures
            if (!factures.isEmpty()) {
                html.append("<h2>Détail des Factures</h2>\n");
                html.append("<table>\n");
                html.append("<tr>\n");
                html.append("<th>Numéro</th>\n");
                html.append("<th>Date</th>\n");
                html.append("<th>Montant TTC</th>\n");
                html.append("<th>Montant Payé</th>\n");
                html.append("<th>Reste Dû</th>\n");
                html.append("<th>Statut</th>\n");
                html.append("</tr>\n");
                
                for (Facture facture : factures) {
                    BigDecimal montantTTC = facture.getTotalTTC() != null ? facture.getTotalTTC() : BigDecimal.ZERO;
                    BigDecimal montantPaye = facture.getMontantPaye() != null ? facture.getMontantPaye() : BigDecimal.ZERO;
                    BigDecimal resteDu = montantTTC.subtract(montantPaye);
                    
                    String statut;
                    String classeStatut;
                    if (resteDu.compareTo(BigDecimal.ZERO) <= 0) {
                        statut = "Payée";
                        classeStatut = "status-paye";
                    } else if (montantPaye.compareTo(BigDecimal.ZERO) > 0) {
                        statut = "Partiel";
                        classeStatut = "status-partiel";
                    } else {
                        statut = "Impayée";
                        classeStatut = "status-impaye";
                    }
                    
                    html.append("<tr>\n");
                    html.append("<td>").append(facture.getNumero() != null ? facture.getNumero() : "N/A").append("</td>\n");
                    html.append("<td>");
                    if (facture.getDateCreation() != null) {
                        html.append(facture.getDateCreation().format(DATE_FORMAT));
                    } else {
                        html.append("N/A");
                    }
                    html.append("</td>\n");
                    html.append("<td class='amount'>").append(CURRENCY_FORMAT.format(montantTTC)).append(" DH</td>\n");
                    html.append("<td class='amount'>").append(CURRENCY_FORMAT.format(montantPaye)).append(" DH</td>\n");
                    html.append("<td class='amount'>").append(CURRENCY_FORMAT.format(resteDu)).append(" DH</td>\n");
                    html.append("<td class='").append(classeStatut).append("'>").append(statut).append("</td>\n");
                    html.append("</tr>\n");
                }
                
                html.append("</table>\n");
            } else {
                html.append("<p><em>Aucune facture trouvée pour ce client.</em></p>\n");
            }
            
            // Pied de page
            html.append("<div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; text-align: center; color: #7f8c8d;'>\n");
            html.append("<p>Rapport généré le ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append("</p>\n");
            html.append("<p>").append(MAIL_FROM_NAME).append("</p>\n");
            html.append("</div>\n");
            
            html.append("</body>\n</html>");
            
        } catch (Exception e) {
            // En cas d'erreur, générer un rapport simplifié
            html.append("<!DOCTYPE html>\n<html>\n<head><meta charset='UTF-8'><title>Situation Client</title></head>\n<body>\n");
            html.append("<h1>Situation Client</h1>\n");
            html.append("<p><strong>Client :</strong> ").append(client.getNom());
            if (client.getPrenom() != null) {
                html.append(" ").append(client.getPrenom());
            }
            html.append("</p>\n");
            html.append("<p><em>Erreur lors de la génération du rapport détaillé : ").append(e.getMessage()).append("</em></p>\n");
            html.append("</body>\n</html>");
        }
        
        return html.toString();
    }
    
    /**
     * Envoie un e-mail
     */
    public void envoyerEmail(String destinataire, String sujet, String contenu, boolean isHTML) throws MessagingException {
        // Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", MAIL_HOST);
        props.put("mail.smtp.port", MAIL_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        // Authentification
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_USERNAME, MAIL_PASSWORD);
            }
        };
        
        // Création de la session
        Session session = Session.getInstance(props, auth);
        
        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MAIL_FROM_ADDRESS, MAIL_FROM_NAME, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            
            if (isHTML) {
                message.setContent(contenu, "text/html; charset=UTF-8");
            } else {
                message.setText(contenu);
            }
            
            // Envoi du message
            Transport.send(message);
            
        } catch (Exception e) {
            throw new MessagingException("Erreur lors de l'envoi de l'e-mail: " + e.getMessage(), e);
        }
    }
    
    /**
     * Teste la configuration e-mail
     */
    public void testerConfiguration() throws MessagingException {
        envoyerEmail(MAIL_FROM_ADDRESS, "Test de configuration", 
                    "Ceci est un test de configuration du service e-mail.", false);
    }
}
