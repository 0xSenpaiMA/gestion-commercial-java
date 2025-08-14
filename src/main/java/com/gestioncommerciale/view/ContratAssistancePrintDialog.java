package com.gestioncommerciale.view;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import com.gestioncommerciale.model.ContratAssistance;

/**
 * Dialog for printing assistance contracts
 */
public class ContratAssistancePrintDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private ContratAssistance contrat;
    private JTextArea contentArea;
    
    public ContratAssistancePrintDialog(Window parent, ContratAssistance contrat) {
        super(parent, "Aperçu du Contrat d'Assistance", ModalityType.APPLICATION_MODAL);
        this.contrat = contrat;
        
        initializeComponents();
        layoutComponents();
        bindEvents();
        generateContent();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        contentArea.setMargin(new Insets(20, 20, 20, 20));
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Content area
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Aperçu du contrat"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton printButton = new JButton("Imprimer");
        JButton closeButton = new JButton("Fermer");
        
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Bind events inline
        printButton.addActionListener(e -> printContrat());
        closeButton.addActionListener(e -> dispose());
    }
    
    private void bindEvents() {
        // Events already bound in layoutComponents
    }
    
    private void generateContent() {
        StringBuilder content = new StringBuilder();
        
        content.append("================================================================================\n");
        content.append("                           CONTRAT D'ASSISTANCE\n");
        content.append("================================================================================\n\n");
        
        // Contract details
        content.append("NUMÉRO DU CONTRAT : ").append(contrat.getNumero()).append("\n\n");
        
        // Client information
        content.append("CLIENT :\n");
        content.append("--------\n");
        content.append("Nom     : ").append(contrat.getClient().getNom()).append("\n");
        content.append("Email   : ").append(contrat.getClient().getEmail()).append("\n");
        if (contrat.getClient().getTelephone() != null) {
            content.append("Tél     : ").append(contrat.getClient().getTelephone()).append("\n");
        }
        if (contrat.getClient().getAdresse() != null) {
            content.append("Adresse : ").append(contrat.getClient().getAdresse()).append("\n");
        }
        content.append("\n");
        
        // Contract period
        content.append("PÉRIODE DU CONTRAT :\n");
        content.append("--------------------\n");
        if (contrat.getDateDebut() != null) {
            content.append("Date de début : ").append(contrat.getDateDebut().format(DATE_FORMATTER)).append("\n");
        }
        if (contrat.getDateFin() != null) {
            content.append("Date de fin   : ").append(contrat.getDateFin().format(DATE_FORMATTER)).append("\n");
            content.append("Durée         : ").append(contrat.getDureeEnJours()).append(" jours\n");
        }
        
        String statut;
        if (!contrat.isActif()) {
            statut = "Inactif";
        } else if (contrat.isExpire()) {
            statut = "Expiré";
        } else if (contrat.isEnCours()) {
            statut = "En cours";
        } else {
            statut = "À venir";
        }
        content.append("Statut        : ").append(statut).append("\n\n");
        
        // Description
        if (contrat.getDescription() != null && !contrat.getDescription().trim().isEmpty()) {
            content.append("DESCRIPTION :\n");
            content.append("-------------\n");
            content.append(contrat.getDescription()).append("\n\n");
        }
        
        // Financial details
        content.append("DÉTAILS FINANCIERS :\n");
        content.append("--------------------\n");
        if (contrat.getTarifHoraire() != null) {
            content.append("Tarif horaire    : ").append(String.format("%.2f €", contrat.getTarifHoraire())).append("\n");
        }
        content.append("Heures incluses  : ").append(contrat.getHeuresIncluses()).append(" h\n");
        if (contrat.getTarifHoraire() != null) {
            double montantInclus = contrat.getTarifHoraire().doubleValue() * contrat.getHeuresIncluses();
            content.append("Montant inclus   : ").append(String.format("%.2f €", montantInclus)).append("\n");
        }
        content.append("\n");
        
        // Conditions
        if (contrat.getConditions() != null && !contrat.getConditions().trim().isEmpty()) {
            content.append("CONDITIONS :\n");
            content.append("------------\n");
            content.append(contrat.getConditions()).append("\n\n");
        }
        
        // Observations
        if (contrat.getObservations() != null && !contrat.getObservations().trim().isEmpty()) {
            content.append("OBSERVATIONS :\n");
            content.append("--------------\n");
            content.append(contrat.getObservations()).append("\n\n");
        }
        
        // Associated invoices
        if (contrat.getFactures() != null && !contrat.getFactures().isEmpty()) {
            content.append("FACTURES ASSOCIÉES :\n");
            content.append("--------------------\n");
            contrat.getFactures().forEach(facture -> {
                content.append("- Facture ").append(facture.getNumero());
                if (facture.getDateFacturation() != null) {
                    content.append(" du ").append(facture.getDateFacturation().format(DATE_FORMATTER));
                }
                if (facture.getTotalTTC() != null) {
                    content.append(" - ").append(String.format("%.2f €", facture.getTotalTTC()));
                }
                content.append("\n");
            });
            content.append("\n");
        }
        
        // Footer
        content.append("================================================================================\n");
        content.append("Document généré le ").append(java.time.LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append("\n");
        content.append("================================================================================");
        
        contentArea.setText(content.toString());
        contentArea.setCaretPosition(0);
    }
    
    private void printContrat() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(new ContratPrintable());
            
            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(
                    this,
                    "Contrat envoyé à l'imprimante",
                    "Impression",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors de l'impression:\n" + e.getMessage(),
                "Erreur d'impression",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private class ContratPrintable implements Printable {
        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }
            
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            // Set font
            Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
            g2d.setFont(font);
            
            FontMetrics metrics = g2d.getFontMetrics();
            int lineHeight = metrics.getHeight();
            
            String[] lines = contentArea.getText().split("\n");
            int y = lineHeight;
            
            for (String line : lines) {
                if (y + lineHeight > pageFormat.getImageableHeight()) {
                    break; // Page full
                }
                g2d.drawString(line, 0, y);
                y += lineHeight;
            }
            
            return PAGE_EXISTS;
        }
    }
}
