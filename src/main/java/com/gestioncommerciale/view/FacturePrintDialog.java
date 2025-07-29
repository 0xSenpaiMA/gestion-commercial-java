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

import com.gestioncommerciale.model.Facture;
import com.gestioncommerciale.model.LigneFacture;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for printing facture
 */
public class FacturePrintDialog extends JDialog implements Printable {
    private final Facture facture;
    private JTextArea contentArea;
    private JButton printButton, previewButton, closeButton;
    
    public FacturePrintDialog(JFrame parent, Facture facture) {
        super(parent, "Aperçu et Impression - Facture " + facture.getNumero(), true);
        this.facture = facture;
        
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("Aperçu de la Facture"));
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
        printButton.addActionListener(e -> printFacture());
        previewButton.addActionListener(e -> generateContent());
        closeButton.addActionListener(e -> dispose());
    }
    
    private void generateContent() {
        StringBuilder content = new StringBuilder();
        
        // Header
        content.append("=====================================\n");
        content.append("             FACTURE                \n");
        content.append("=====================================\n\n");
        
        // Company info (you might want to load this from database)
        content.append("VOTRE ENTREPRISE\n");
        content.append("Adresse de l'entreprise\n");
        content.append("Téléphone: +33 X XX XX XX XX\n");
        content.append("Email: contact@votre-entreprise.com\n\n");
        
        // Invoice info
        content.append("Facture N°: ").append(facture.getNumero()).append("\n");
        content.append("Date de facturation: ").append(facture.getDateFacturation() != null ? 
            facture.getDateFacturation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A").append("\n");
        
        if (facture.getDateEcheance() != null) {
            content.append("Date d'échéance: ").append(
                facture.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }
        
        content.append("Statut: ").append(facture.getStatut().getLibelle()).append("\n");
        
        if (facture.getBonLivraison() != null) {
            content.append("Bon de livraison: ").append(facture.getBonLivraison().getNumero()).append("\n");
        }
        
        if (facture.getModePaiement() != null && !facture.getModePaiement().trim().isEmpty()) {
            content.append("Mode de paiement: ").append(facture.getModePaiement()).append("\n");
        }
        
        content.append("\n");
        
        // Client info
        content.append("FACTURÉ À:\n");
        if (facture.getClient() != null) {
            content.append(facture.getClient().getNom());
            if (facture.getClient().getPrenom() != null) {
                content.append(" ").append(facture.getClient().getPrenom());
            }
            content.append("\n");
            
            if (facture.getClient().getAdresse() != null) {
                content.append(facture.getClient().getAdresse()).append("\n");
            }
            if (facture.getClient().getVille() != null) {
                content.append(facture.getClient().getVille());
                if (facture.getClient().getPays() != null) {
                    content.append(", ").append(facture.getClient().getPays());
                }
                content.append("\n");
            }
            if (facture.getClient().getTelephone() != null) {
                content.append("Tél: ").append(facture.getClient().getTelephone()).append("\n");
            }
            if (facture.getClient().getEmail() != null) {
                content.append("Email: ").append(facture.getClient().getEmail()).append("\n");
            }
        }
        
        content.append("\n");
        
        // Line items
        content.append("DÉTAIL DE LA FACTURATION:\n");
        content.append("------------------------------------------------------------------------\n");
        content.append(String.format("%-30s %8s %12s %8s %12s\n", 
            "Désignation", "Qté", "Prix Unit.", "Remise", "Total"));
        content.append("------------------------------------------------------------------------\n");
        
        for (LigneFacture ligne : facture.getLignes()) {
            String designation = ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "N/A";
            if (designation.length() > 30) {
                designation = designation.substring(0, 27) + "...";
            }
            
            content.append(String.format("%-30s %8s %12s %8s %12s\n",
                designation,
                ligne.getQuantite() != null ? ligne.getQuantite().toString() : "0",
                ligne.getPrixUnitaire() != null ? String.format("%.2f €", ligne.getPrixUnitaire()) : "0.00 €",
                ligne.getRemise() != null ? ligne.getRemise().toString() + "%" : "0%",
                ligne.getTotal() != null ? String.format("%.2f €", ligne.getTotal()) : "0.00 €"));
                
            // Add description if present
            if (ligne.getDescription() != null && !ligne.getDescription().trim().isEmpty()) {
                content.append("    Description: ").append(ligne.getDescription()).append("\n");
            }
        }
        
        content.append("------------------------------------------------------------------------\n");
        
        // Totals
        content.append(String.format("%60s %12s\n", "Total HT:", 
            String.format("%.2f €", facture.getTotalHT())));
        content.append(String.format("%60s %12s\n", "TVA (" + facture.getTauxTVA() + "%):", 
            String.format("%.2f €", facture.getMontantTVA())));
        content.append(String.format("%60s %12s\n", "Total TTC:", 
            String.format("%.2f €", facture.getTotalTTC())));
        
        if (facture.getMontantPaye() != null && facture.getMontantPaye().compareTo(java.math.BigDecimal.ZERO) > 0) {
            content.append(String.format("%60s %12s\n", "Montant payé:", 
                String.format("%.2f €", facture.getMontantPaye())));
            content.append(String.format("%60s %12s\n", "Montant restant:", 
                String.format("%.2f €", facture.getMontantRestant())));
        }
        
        content.append("========================================================================\n");
        
        // Observations
        if (facture.getObservations() != null && !facture.getObservations().trim().isEmpty()) {
            content.append("\nOBSERVATIONS:\n");
            content.append(facture.getObservations()).append("\n");
        }
        
        // Payment terms
        content.append("\nCONDITIONS DE PAIEMENT:\n");
        if (facture.getDateEcheance() != null) {
            content.append("Paiement à effectuer avant le ")
                .append(facture.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .append("\n");
        }
        if (facture.getModePaiement() != null && !facture.getModePaiement().trim().isEmpty()) {
            content.append("Mode de paiement: ").append(facture.getModePaiement()).append("\n");
        }
        
        // Footer
        content.append("\n\nMerci de votre confiance !\n");
        content.append("En cas de retard de paiement, des pénalités pourront être appliquées.\n");
        
        contentArea.setText(content.toString());
        contentArea.setCaretPosition(0);
    }
    
    private void printFacture() {
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
