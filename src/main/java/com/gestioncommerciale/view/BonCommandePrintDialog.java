package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.gestioncommerciale.model.BonCommande;
import com.gestioncommerciale.model.LigneCommande;

/**
 * Dialog for printing purchase orders
 */
public class BonCommandePrintDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private BonCommande bonCommande;
    private JTextArea textArea;
    private JButton printButton;
    private JButton closeButton;
    
    public BonCommandePrintDialog(JFrame parent, BonCommande bonCommande) {
        super(parent, "Aperçu d'impression - Bon de Commande", true);
        this.bonCommande = bonCommande;
        
        initializeUI();
        generatePrintContent();
    }
    
    private void initializeUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Create text area for print preview
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        printButton = new JButton("Imprimer");
        closeButton = new JButton("Fermer");
        
        buttonsPanel.add(printButton);
        buttonsPanel.add(closeButton);
        
        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add listeners
        printButton.addActionListener(e -> print());
        closeButton.addActionListener(e -> dispose());
    }
    
    private void generatePrintContent() {
        StringBuilder content = new StringBuilder();
        
        // Header
        content.append("=".repeat(80)).append("\n");
        content.append("                            BON DE COMMANDE\n");
        content.append("=".repeat(80)).append("\n\n");
        
        // Company info (you can customize this)
        content.append("Entreprise: [Nom de votre entreprise]\n");
        content.append("Adresse: [Adresse de votre entreprise]\n");
        content.append("Téléphone: [Téléphone]\n");
        content.append("Email: [Email]\n\n");
        
        // Bon de commande info
        content.append("-".repeat(80)).append("\n");
        content.append(String.format("Numéro: %-20s", bonCommande.getNumero()));
        content.append(String.format("Date: %s\n", bonCommande.getDateCommande() != null ? 
            bonCommande.getDateCommande().format(DATE_FORMATTER) : ""));
        
        if (bonCommande.getDateLivraisonPrevue() != null) {
            content.append(String.format("Date Livraison Prévue: %s\n", 
                bonCommande.getDateLivraisonPrevue().format(DATE_FORMATTER)));
        }
        
        content.append(String.format("Statut: %s\n", bonCommande.getStatut()));
        
        if (bonCommande.getNumeroDevisFournisseur() != null && !bonCommande.getNumeroDevisFournisseur().trim().isEmpty()) {
            content.append(String.format("N° Devis Fournisseur: %s\n", bonCommande.getNumeroDevisFournisseur()));
        }
        
        content.append("-".repeat(80)).append("\n\n");
        
        // Fournisseur info
        content.append("FOURNISSEUR:\n");
        content.append("-".repeat(15)).append("\n");
        if (bonCommande.getFournisseur() != null) {
            content.append(String.format("Nom: %s\n", bonCommande.getFournisseur().getNom()));
            if (bonCommande.getFournisseur().getPrenom() != null) {
                content.append(String.format("Prénom: %s\n", bonCommande.getFournisseur().getPrenom()));
            }
            if (bonCommande.getFournisseur().getAdresse() != null) {
                content.append(String.format("Adresse: %s\n", bonCommande.getFournisseur().getAdresse()));
            }
            if (bonCommande.getFournisseur().getTelephone() != null) {
                content.append(String.format("Téléphone: %s\n", bonCommande.getFournisseur().getTelephone()));
            }
            if (bonCommande.getFournisseur().getEmail() != null) {
                content.append(String.format("Email: %s\n", bonCommande.getFournisseur().getEmail()));
            }
        }
        content.append("\n");
        
        // Adresse de livraison
        if (bonCommande.getAdresseLivraison() != null && !bonCommande.getAdresseLivraison().trim().isEmpty()) {
            content.append("ADRESSE DE LIVRAISON:\n");
            content.append("-".repeat(22)).append("\n");
            content.append(bonCommande.getAdresseLivraison()).append("\n\n");
        }
        
        // Articles table
        content.append("ARTICLES COMMANDÉS:\n");
        content.append("-".repeat(19)).append("\n");
        content.append(String.format("%-40s %8s %12s %8s %12s\n", 
            "Désignation", "Qté", "Prix Unit.", "Remise", "Total"));
        content.append("-".repeat(80)).append("\n");
        
        for (LigneCommande ligne : bonCommande.getLignes()) {
            String designation = ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "";
            if (designation.length() > 38) {
                designation = designation.substring(0, 35) + "...";
            }
            
            content.append(String.format("%-40s %8s %12.2f %7.1f%% %12.2f\n",
                designation,
                ligne.getQuantite().toString(),
                ligne.getPrixUnitaire(),
                ligne.getRemise(),
                ligne.getTotal()));
            
            // Add description and reference if available
            if (ligne.getDescription() != null && !ligne.getDescription().trim().isEmpty()) {
                content.append(String.format("  Description: %s\n", ligne.getDescription()));
            }
            if (ligne.getReferenceFournisseur() != null && !ligne.getReferenceFournisseur().trim().isEmpty()) {
                content.append(String.format("  Réf. Fournisseur: %s\n", ligne.getReferenceFournisseur()));
            }
            if (ligne.getDateLivraisonSouhaitee() != null) {
                content.append(String.format("  Date livraison souhaitée: %s\n", 
                    ligne.getDateLivraisonSouhaitee().format(DATE_FORMATTER)));
            }
        }
        
        content.append("-".repeat(80)).append("\n");
        
        // Totals
        content.append(String.format("%60s %12.2f €\n", "Total HT:", bonCommande.getTotalHT()));
        content.append(String.format("%60s %12.2f €\n", "TVA (" + bonCommande.getTauxTVA() + "%):", bonCommande.getMontantTVA()));
        content.append(String.format("%60s %12.2f €\n", "Total TTC:", bonCommande.getTotalTTC()));
        content.append("=".repeat(80)).append("\n\n");
        
        // Conditions de paiement
        if (bonCommande.getConditionsPaiement() != null && !bonCommande.getConditionsPaiement().trim().isEmpty()) {
            content.append("CONDITIONS DE PAIEMENT:\n");
            content.append("-".repeat(23)).append("\n");
            content.append(bonCommande.getConditionsPaiement()).append("\n\n");
        }
        
        // Observations
        if (bonCommande.getObservations() != null && !bonCommande.getObservations().trim().isEmpty()) {
            content.append("OBSERVATIONS:\n");
            content.append("-".repeat(13)).append("\n");
            content.append(bonCommande.getObservations()).append("\n\n");
        }
        
        // Footer
        content.append("-".repeat(80)).append("\n");
        content.append("Date d'impression: ").append(java.time.LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        content.append("Document généré par le système de gestion commerciale\n");
        
        textArea.setText(content.toString());
        textArea.setCaretPosition(0);
    }
    
    private void print() {
        try {
            MessageFormat header = new MessageFormat("Bon de Commande - " + bonCommande.getNumero());
            MessageFormat footer = new MessageFormat("Page {0}");
            
            boolean complete = textArea.print(header, footer);
            
            if (complete) {
                JOptionPane.showMessageDialog(this,
                    "Impression terminée avec succès.",
                    "Impression", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Impression annulée par l'utilisateur.",
                    "Impression", JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'impression: " + e.getMessage(),
                "Erreur d'impression", JOptionPane.ERROR_MESSAGE);
        }
    }
}
