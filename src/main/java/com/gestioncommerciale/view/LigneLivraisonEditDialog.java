package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.model.LigneLivraison;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for adding/editing delivery lines
 */
public class LigneLivraisonEditDialog extends JDialog {
    private final LigneLivraison ligne;
    private boolean dataSaved = false;
    
    // Form fields
    private JComboBox<Article> articleCombo;
    private JSpinner quantiteDemandeeSpinner;
    private JSpinner quantiteLivreeSpinner;
    private JTextField emplacementField;
    private JTextField numeroSerieField;
    private JTextArea descriptionArea;
    
    // Action buttons
    private JButton saveButton, cancelButton;
    
    public LigneLivraisonEditDialog(JDialog parent, LigneLivraison ligne, List<Article> articles) {
        super(parent, ligne == null ? "Ajouter Article" : "Modifier Article", true);
        this.ligne = ligne != null ? ligne : new LigneLivraison();
        
        initializeComponents(articles);
        setupLayout();
        setupEventHandlers();
        populateForm();
        configureDialog();
    }
    
    private void initializeComponents(List<Article> articles) {
        articleCombo = new JComboBox<>();
        for (Article article : articles) {
            articleCombo.addItem(article);
        }
        
        quantiteDemandeeSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 999999.0, 0.1));
        quantiteLivreeSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 999999.0, 0.1));
        emplacementField = new JTextField(20);
        numeroSerieField = new JTextField(20);
        
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
        cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.SECONDARY_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Article
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Article:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(articleCombo, gbc);
        
        // Quantité demandée
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Quantité Demandée:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(quantiteDemandeeSpinner, gbc);
        
        // Quantité livrée
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Quantité Livrée:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(quantiteLivreeSpinner, gbc);
        
        // Emplacement
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Emplacement:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(emplacementField, gbc);
        
        // Numéro de série
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("N° Série:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(numeroSerieField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTHEAST;
        mainPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(descriptionArea, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveLigne());
        cancelButton.addActionListener(e -> dispose());
        
        // Auto-update delivered quantity when requested quantity changes
        quantiteDemandeeSpinner.addChangeListener(e -> {
            Double demanded = (Double) quantiteDemandeeSpinner.getValue();
            Double delivered = (Double) quantiteLivreeSpinner.getValue();
            
            // If delivered is 0 or equal to previous demanded, update it
            if (delivered == 0.0 || delivered.equals(ligne.getQuantiteDemandee().doubleValue())) {
                quantiteLivreeSpinner.setValue(demanded);
            }
        });
    }
    
    private void populateForm() {
        if (ligne.getArticle() != null) {
            articleCombo.setSelectedItem(ligne.getArticle());
        }
        
        if (ligne.getQuantiteDemandee() != null) {
            quantiteDemandeeSpinner.setValue(ligne.getQuantiteDemandee().doubleValue());
        }
        
        if (ligne.getQuantiteLivree() != null) {
            quantiteLivreeSpinner.setValue(ligne.getQuantiteLivree().doubleValue());
        }
        
        if (ligne.getEmplacement() != null) {
            emplacementField.setText(ligne.getEmplacement());
        }
        
        if (ligne.getNumeroSerie() != null) {
            numeroSerieField.setText(ligne.getNumeroSerie());
        }
        
        if (ligne.getDescription() != null) {
            descriptionArea.setText(ligne.getDescription());
        }
    }
    
    private void saveLigne() {
        try {
            // Validate fields
            if (articleCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un article.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Double demandedQty = (Double) quantiteDemandeeSpinner.getValue();
            Double deliveredQty = (Double) quantiteLivreeSpinner.getValue();
            
            if (demandedQty <= 0) {
                JOptionPane.showMessageDialog(this, "La quantité demandée doit être positive.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (deliveredQty < 0) {
                JOptionPane.showMessageDialog(this, "La quantité livrée ne peut pas être négative.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (deliveredQty > demandedQty) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "La quantité livrée est supérieure à la quantité demandée.\nContinuer ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Update ligne with form data
            ligne.setArticle((Article) articleCombo.getSelectedItem());
            ligne.setQuantiteDemandee(BigDecimal.valueOf(demandedQty));
            ligne.setQuantiteLivree(BigDecimal.valueOf(deliveredQty));
            ligne.setEmplacement(emplacementField.getText().trim());
            ligne.setNumeroSerie(numeroSerieField.getText().trim());
            ligne.setDescription(descriptionArea.getText().trim());
            
            dataSaved = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la sauvegarde: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureDialog() {
        setSize(500, 450);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
    
    public LigneLivraison getLigne() {
        return ligne;
    }
}
