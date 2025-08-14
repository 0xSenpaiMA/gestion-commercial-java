package com.gestioncommerciale.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.service.StockAlertService;

/**
 * Dialogue pour mettre à jour le stock d'un article
 */
public class StockUpdateDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private Article article;
    private final StockAlertService stockAlertService;
    private boolean confirmed = false;
    
    // Components
    private JLabel currentStockLabel;
    private JRadioButton addStockRadio, removeStockRadio, setStockRadio;
    private JTextField quantityField, newStockField;
    private JTextField reasonField;
    private JButton confirmButton, cancelButton;
    
    public StockUpdateDialog(Window parent, Article article, StockAlertService stockAlertService) {
        super(parent, "Modifier le Stock", ModalityType.APPLICATION_MODAL);
        
        this.article = article;
        this.stockAlertService = stockAlertService;
        
        initializeComponents();
        layoutComponents();
        bindEvents();
        
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Article info
        currentStockLabel = new JLabel(
            String.format("Stock actuel: %d %s (Min: %d, Max: %d)", 
                article.getStockActuel(), 
                article.getUnite() != null ? article.getUnite() : "",
                article.getStockMin(), 
                article.getStockMax())
        );
        
        // Operation type
        addStockRadio = new JRadioButton("Ajouter au stock", true);
        removeStockRadio = new JRadioButton("Retirer du stock");
        setStockRadio = new JRadioButton("Définir nouveau stock");
        
        ButtonGroup operationGroup = new ButtonGroup();
        operationGroup.add(addStockRadio);
        operationGroup.add(removeStockRadio);
        operationGroup.add(setStockRadio);
        
        // Quantity fields
        quantityField = new JTextField(10);
        newStockField = new JTextField(10);
        newStockField.setEnabled(false);
        
        // Reason
        reasonField = new JTextField(20);
        
        // Buttons
        confirmButton = new JButton("Confirmer");
        cancelButton = new JButton("Annuler");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Article info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Article: " + article.getDesignation()));
        infoPanel.add(currentStockLabel);
        
        if (article.isStockLow()) {
            JLabel alertLabel = new JLabel("⚠ ALERTE DE STOCK");
            alertLabel.setForeground(Color.RED);
            alertLabel.setFont(alertLabel.getFont().deriveFont(Font.BOLD));
            infoPanel.add(Box.createHorizontalStrut(20));
            infoPanel.add(alertLabel);
        }
        
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Operation panel
        JPanel operationPanel = new JPanel();
        operationPanel.setBorder(BorderFactory.createTitledBorder("Opération"));
        operationPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        operationPanel.add(addStockRadio, gbc);
        
        gbc.gridy = 1;
        operationPanel.add(removeStockRadio, gbc);
        
        gbc.gridy = 2;
        operationPanel.add(setStockRadio, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        operationPanel.add(new JLabel("Quantité:"), gbc);
        
        gbc.gridx = 2;
        operationPanel.add(quantityField, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        operationPanel.add(new JLabel("Nouveau stock:"), gbc);
        
        gbc.gridx = 2;
        operationPanel.add(newStockField, gbc);
        
        mainPanel.add(operationPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Reason panel
        JPanel reasonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reasonPanel.setBorder(BorderFactory.createTitledBorder("Motif (optionnel)"));
        reasonPanel.add(reasonField);
        
        mainPanel.add(reasonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void bindEvents() {
        // Radio button events
        ActionListener radioListener = e -> updateFieldStates();
        addStockRadio.addActionListener(radioListener);
        removeStockRadio.addActionListener(radioListener);
        setStockRadio.addActionListener(radioListener);
        
        // Button events
        confirmButton.addActionListener(this::confirmAction);
        cancelButton.addActionListener(e -> dispose());
        
        // Enter key on quantity fields
        quantityField.addActionListener(this::confirmAction);
        newStockField.addActionListener(this::confirmAction);
    }
    
    private void updateFieldStates() {
        if (setStockRadio.isSelected()) {
            quantityField.setEnabled(false);
            newStockField.setEnabled(true);
            quantityField.setText("");
        } else {
            quantityField.setEnabled(true);
            newStockField.setEnabled(false);
            newStockField.setText("");
        }
    }
    
    private void confirmAction(ActionEvent e) {
        try {
            int newStock;
            String operation;
            
            if (setStockRadio.isSelected()) {
                String newStockText = newStockField.getText().trim();
                if (newStockText.isEmpty()) {
                    showError("Veuillez saisir le nouveau stock.");
                    return;
                }
                newStock = Integer.parseInt(newStockText);
                operation = "Définition du stock à " + newStock;
                
                if (newStock < 0) {
                    showError("Le stock ne peut pas être négatif.");
                    return;
                }
                
            } else {
                String quantityText = quantityField.getText().trim();
                if (quantityText.isEmpty()) {
                    showError("Veuillez saisir une quantité.");
                    return;
                }
                
                int quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    showError("La quantité doit être positive.");
                    return;
                }
                
                int currentStock = article.getStockActuel();
                
                if (addStockRadio.isSelected()) {
                    newStock = currentStock + quantity;
                    operation = "Ajout de " + quantity + " unités";
                } else { // removeStockRadio
                    newStock = currentStock - quantity;
                    operation = "Retrait de " + quantity + " unités";
                    
                    if (newStock < 0) {
                        int result = JOptionPane.showConfirmDialog(
                            this,
                            "Cette opération donnera un stock négatif (" + newStock + ").\n" +
                            "Voulez-vous continuer?",
                            "Stock négatif",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (result != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                }
            }
            
            // Update stock
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                reason = operation;
            } else {
                reason = operation + " - " + reason;
            }
            
            stockAlertService.updateStockAndCheckAlert(article.getId(), newStock, reason);
            
            confirmed = true;
            dispose();
            
        } catch (NumberFormatException ex) {
            showError("Veuillez saisir un nombre valide.");
        } catch (Exception ex) {
            showError("Erreur lors de la mise à jour du stock:\n" + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Erreur",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
