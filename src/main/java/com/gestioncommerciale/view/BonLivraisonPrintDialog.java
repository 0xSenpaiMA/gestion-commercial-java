package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.gestioncommerciale.model.BonLivraison;
import com.gestioncommerciale.model.LigneLivraison;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for printing bon de livraison
 */
public class BonLivraisonPrintDialog extends JDialog implements Printable {
    private final BonLivraison bonLivraison;
    private JTextArea contentArea;
    private JButton printButton, previewButton, closeButton;
    
    public BonLivraisonPrintDialog(JFrame parent, BonLivraison bonLivraison) {
        super(parent, "Aperçu et Impression - Bon de Livraison " + bonLivraison.getNumero(), true);
        this.bonLivraison = bonLivraison;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        generateContent();
        configureDialog();
    }
    
    private void initializeComponents() {
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        printButton = UIUtils.createStyledButton("Imprimer", UIUtils.PRIMARY_COLOR);
        previewButton = UIUtils.createStyledButton("Aperçu", UIUtils.SECONDARY_COLOR);
        closeButton = UIUtils.createStyledButton("Fermer", UIUtils.ERROR_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Content panel
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Aperçu du Bon de Livraison"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(previewButton);
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        printButton.addActionListener(e -> printBonLivraison());
        previewButton.addActionListener(e -> generateContent());
        closeButton.addActionListener(e -> dispose());
    }
    
    private void generateContent() {
        StringBuilder content = new StringBuilder();
        
        // Header
        content.append("=====================================\n");
        content.append("        BON DE LIVRAISON            \n");
        content.append("=====================================\n\n");
        
        // Company info (you might want to load this from database)
        content.append("VOTRE ENTREPRISE\n");
        content.append("Adresse de l'entreprise\n");
        content.append("Téléphone: +33 X XX XX XX XX\n");
        content.append("Email: contact@votre-entreprise.com\n\n");
        
        // Bon de livraison info
        content.append("Bon de Livraison N°: ").append(bonLivraison.getNumero()).append("\n");
        content.append("Date de création: ").append(bonLivraison.getDateCreation() != null ? 
            bonLivraison.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A").append("\n");
        
        if (bonLivraison.getDateLivraison() != null) {
            content.append("Date de livraison: ").append(
                bonLivraison.getDateLivraison().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }
        
        content.append("Statut: ").append(bonLivraison.getStatut().getLibelle()).append("\n");
        
        if (bonLivraison.getDevis() != null) {
            content.append("Devis de référence: ").append(bonLivraison.getDevis().getNumero()).append("\n");
        }
        
        content.append("\n");
        
        // Client info
        content.append("CLIENT À LIVRER:\n");
        if (bonLivraison.getClient() != null) {
            content.append(bonLivraison.getClient().getNom());
            if (bonLivraison.getClient().getPrenom() != null) {
                content.append(" ").append(bonLivraison.getClient().getPrenom());
            }
            content.append("\n");
            
            if (bonLivraison.getClient().getAdresse() != null) {
                content.append(bonLivraison.getClient().getAdresse()).append("\n");
            }
            if (bonLivraison.getClient().getVille() != null) {
                content.append(bonLivraison.getClient().getVille());
                if (bonLivraison.getClient().getPays() != null) {
                    content.append(", ").append(bonLivraison.getClient().getPays());
                }
                content.append("\n");
            }
            if (bonLivraison.getClient().getTelephone() != null) {
                content.append("Tél: ").append(bonLivraison.getClient().getTelephone()).append("\n");
            }
        }
        
        // Delivery address if different
        if (bonLivraison.getAdresseLivraison() != null && !bonLivraison.getAdresseLivraison().trim().isEmpty()) {
            content.append("\nADRESSE DE LIVRAISON:\n");
            content.append(bonLivraison.getAdresseLivraison()).append("\n");
        }
        
        content.append("\n");
        
        // Transport info
        if (bonLivraison.getTransporteur() != null || bonLivraison.getModeLivraison() != null) {
            content.append("INFORMATIONS TRANSPORT:\n");
            if (bonLivraison.getTransporteur() != null) {
                content.append("Transporteur: ").append(bonLivraison.getTransporteur()).append("\n");
            }
            if (bonLivraison.getModeLivraison() != null) {
                content.append("Mode de livraison: ").append(bonLivraison.getModeLivraison()).append("\n");
            }
            content.append("\n");
        }
        
        // Line items
        content.append("DÉTAIL DES ARTICLES À LIVRER:\n");
        content.append("------------------------------------------------------------------------\n");
        content.append(String.format("%-25s %12s %12s %15s %10s\n", 
            "Article", "Qté Demandée", "Qté Livrée", "Emplacement", "N° Série"));
        content.append("------------------------------------------------------------------------\n");
        
        for (LigneLivraison ligne : bonLivraison.getLignes()) {
            String designation = ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "N/A";
            if (designation.length() > 25) {
                designation = designation.substring(0, 22) + "...";
            }
            
            String emplacement = ligne.getEmplacement() != null ? ligne.getEmplacement() : "";
            if (emplacement.length() > 15) {
                emplacement = emplacement.substring(0, 12) + "...";
            }
            
            String numeroSerie = ligne.getNumeroSerie() != null ? ligne.getNumeroSerie() : "";
            if (numeroSerie.length() > 10) {
                numeroSerie = numeroSerie.substring(0, 7) + "...";
            }
            
            content.append(String.format("%-25s %12s %12s %15s %10s\n",
                designation,
                ligne.getQuantiteDemandee() != null ? ligne.getQuantiteDemandee().toString() : "0",
                ligne.getQuantiteLivree() != null ? ligne.getQuantiteLivree().toString() : "0",
                emplacement,
                numeroSerie));
                
            // Add description if present
            if (ligne.getDescription() != null && !ligne.getDescription().trim().isEmpty()) {
                content.append("    Description: ").append(ligne.getDescription()).append("\n");
            }
        }
        
        content.append("------------------------------------------------------------------------\n");
        
        // Observations
        if (bonLivraison.getObservations() != null && !bonLivraison.getObservations().trim().isEmpty()) {
            content.append("\nOBSERVATIONS:\n");
            content.append(bonLivraison.getObservations()).append("\n");
        }
        
        // Footer
        content.append("\n\n");
        content.append("SIGNATURES:\n\n");
        content.append("Livreur: ____________________    Date: ___________\n\n");
        content.append("Client: _____________________    Date: ___________\n\n");
        content.append("Observations du client:\n");
        content.append("_________________________________________________\n");
        content.append("_________________________________________________\n");
        content.append("_________________________________________________\n\n");
        content.append("Merci de votre confiance !\n");
        
        contentArea.setText(content.toString());
        contentArea.setCaretPosition(0);
    }
    
    private void printBonLivraison() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(this);
            
            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this,
                    "Impression lancée avec succès.",
                    "Impression", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'impression: " + e.getMessage(),
                "Erreur d'impression", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // Set font for printing
        Font font = new Font("Courier New", Font.PLAIN, 10);
        g2d.setFont(font);
        
        // Print the content
        String[] lines = contentArea.getText().split("\n");
        int y = 20;
        int lineHeight = 12;
        
        for (String line : lines) {
            if (y > pageFormat.getImageableHeight() - 20) {
                break; // Avoid printing outside page bounds
            }
            g2d.drawString(line, 10, y);
            y += lineHeight;
        }
        
        return PAGE_EXISTS;
    }
    
    private void configureDialog() {
        setSize(750, 650);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
}
