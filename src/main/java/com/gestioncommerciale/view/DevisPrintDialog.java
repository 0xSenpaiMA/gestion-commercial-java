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

import com.gestioncommerciale.model.Devis;
import com.gestioncommerciale.model.LigneDevis;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for printing devis
 */
public class DevisPrintDialog extends JDialog implements Printable {
    private final Devis devis;
    private JTextArea contentArea;
    private JButton printButton, previewButton, closeButton;
    
    public DevisPrintDialog(JFrame parent, Devis devis) {
        super(parent, "Aperçu et Impression - Devis " + devis.getNumero(), true);
        this.devis = devis;
        
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("Aperçu du Devis"));
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
        printButton.addActionListener(e -> printDevis());
        previewButton.addActionListener(e -> generateContent());
        closeButton.addActionListener(e -> dispose());
    }
    
    private void generateContent() {
        StringBuilder content = new StringBuilder();
        
        // Header
        content.append("=====================================\n");
        content.append("           DEVIS / QUOTE            \n");
        content.append("=====================================\n\n");
        
        // Company info (you might want to load this from database)
        content.append("VOTRE ENTREPRISE\n");
        content.append("Adresse de l'entreprise\n");
        content.append("Téléphone: +33 X XX XX XX XX\n");
        content.append("Email: contact@votre-entreprise.com\n\n");
        
        // Devis info
        content.append("Devis N°: ").append(devis.getNumero()).append("\n");
        content.append("Date: ").append(devis.getDateCreation() != null ? 
            devis.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A").append("\n");
        
        if (devis.getDateValidite() != null) {
            content.append("Valide jusqu'au: ").append(
                devis.getDateValidite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }
        
        content.append("Statut: ").append(devis.getStatut().getLibelle()).append("\n\n");
        
        // Client info
        content.append("CLIENT:\n");
        if (devis.getClient() != null) {
            content.append(devis.getClient().getNom());
            if (devis.getClient().getPrenom() != null) {
                content.append(" ").append(devis.getClient().getPrenom());
            }
            content.append("\n");
            
            if (devis.getClient().getAdresse() != null) {
                content.append(devis.getClient().getAdresse()).append("\n");
            }
            if (devis.getClient().getVille() != null) {
                content.append(devis.getClient().getVille());
                if (devis.getClient().getPays() != null) {
                    content.append(", ").append(devis.getClient().getPays());
                }
                content.append("\n");
            }
            if (devis.getClient().getTelephone() != null) {
                content.append("Tél: ").append(devis.getClient().getTelephone()).append("\n");
            }
            if (devis.getClient().getEmail() != null) {
                content.append("Email: ").append(devis.getClient().getEmail()).append("\n");
            }
        }
        content.append("\n");
        
        // Line items
        content.append("DÉTAIL DES ARTICLES:\n");
        content.append("-------------------------------------\n");
        content.append(String.format("%-25s %8s %12s %8s %12s\n", 
            "Article", "Qté", "Prix Unit.", "Remise", "Total"));
        content.append("-------------------------------------\n");
        
        for (LigneDevis ligne : devis.getLignes()) {
            String designation = ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "N/A";
            if (designation.length() > 25) {
                designation = designation.substring(0, 22) + "...";
            }
            
            content.append(String.format("%-25s %8s %12s %8s %12s\n",
                designation,
                ligne.getQuantite() != null ? ligne.getQuantite().toString() : "0",
                ligne.getPrixUnitaire() != null ? ligne.getPrixUnitaire() + "€" : "0€",
                ligne.getRemise() != null ? ligne.getRemise() + "%" : "0%",
                ligne.getTotal() != null ? ligne.getTotal() + "€" : "0€"));
        }
        
        content.append("-------------------------------------\n");
        
        // Totals
        content.append(String.format("%45s %12s\n", "Total HT:", 
            devis.getTotalHT() != null ? devis.getTotalHT() + "€" : "0,00€"));
        content.append(String.format("%45s %12s\n", "TVA (" + 
            (devis.getTauxTVA() != null ? devis.getTauxTVA() : "0") + "%):", 
            devis.getMontantTVA() != null ? devis.getMontantTVA() + "€" : "0,00€"));
        content.append("-------------------------------------\n");
        content.append(String.format("%45s %12s\n", "Total TTC:", 
            devis.getTotalTTC() != null ? devis.getTotalTTC() + "€" : "0,00€"));
        
        // Observations
        if (devis.getObservations() != null && !devis.getObservations().trim().isEmpty()) {
            content.append("\n\nOBSERVATIONS:\n");
            content.append(devis.getObservations()).append("\n");
        }
        
        // Footer
        content.append("\n\n");
        content.append("Conditions de paiement: 30 jours net\n");
        content.append("Devis valable 30 jours\n");
        content.append("\nMerci de votre confiance !\n");
        
        contentArea.setText(content.toString());
        contentArea.setCaretPosition(0);
    }
    
    private void printDevis() {
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
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
}
