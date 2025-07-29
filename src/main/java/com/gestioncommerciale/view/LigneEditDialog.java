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
import com.gestioncommerciale.model.LigneDevis;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for adding/editing devis lines
 */
public class LigneEditDialog extends JDialog {
    private final LigneDevis ligne;
    private boolean dataSaved = false;
    
    // Form fields
    private JComboBox<Article> articleCombo;
    private JSpinner quantiteSpinner;
    private JTextField prixUnitaireField;
    private JSpinner remiseSpinner;
    private JTextField totalField;
    private JTextArea descriptionArea;
    
    // Action buttons
    private JButton saveButton, cancelButton;
    
    public LigneEditDialog(JDialog parent, LigneDevis ligne, List<Article> articles) {
        super(parent, ligne == null ? "Ajouter Article" : "Modifier Article", true);
        this.ligne = ligne != null ? ligne : new LigneDevis();
        
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
        
        quantiteSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 999999.0, 0.1));
        prixUnitaireField = new JTextField(15);
        remiseSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));
        totalField = new JTextField(15);
        totalField.setEditable(false);
        
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
        
        // Quantité
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(quantiteSpinner, gbc);
        
        // Prix unitaire
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Prix Unitaire (€):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(prixUnitaireField, gbc);
        
        // Remise
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Remise (%):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(remiseSpinner, gbc);
        
        // Total
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Total (€):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(totalField, gbc);
        
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
        
        // Auto-fill price when article is selected
        articleCombo.addActionListener(e -> {
            Article selectedArticle = (Article) articleCombo.getSelectedItem();
            if (selectedArticle != null && selectedArticle.getPrixVente() != null) {
                prixUnitaireField.setText(selectedArticle.getPrixVente().toString());
                calculateTotal();
            }
        });
        
        // Recalculate total when values change
        quantiteSpinner.addChangeListener(e -> calculateTotal());
        remiseSpinner.addChangeListener(e -> calculateTotal());
        
        prixUnitaireField.addActionListener(e -> calculateTotal());
        prixUnitaireField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                calculateTotal();
            }
        });
    }
    
    private void populateForm() {
        if (ligne.getArticle() != null) {
            articleCombo.setSelectedItem(ligne.getArticle());
        }
        
        if (ligne.getQuantite() != null) {
            quantiteSpinner.setValue(ligne.getQuantite().doubleValue());
        }
        
        if (ligne.getPrixUnitaire() != null) {
            prixUnitaireField.setText(ligne.getPrixUnitaire().toString());
        }
        
        if (ligne.getRemise() != null) {
            remiseSpinner.setValue(ligne.getRemise().doubleValue());
        }
        
        if (ligne.getDescription() != null) {
            descriptionArea.setText(ligne.getDescription());
        }
        
        calculateTotal();
    }
    
    private void calculateTotal() {
        try {
            BigDecimal quantite = BigDecimal.valueOf((Double) quantiteSpinner.getValue());
            String prixText = prixUnitaireField.getText().trim();
            
            if (prixText.isEmpty()) {
                totalField.setText("0,00");
                return;
            }
            
            BigDecimal prixUnitaire = new BigDecimal(prixText);
            BigDecimal remise = BigDecimal.valueOf((Double) remiseSpinner.getValue());
            
            // Update the ligne object to calculate total
            ligne.setQuantite(quantite);
            ligne.setPrixUnitaire(prixUnitaire);
            ligne.setRemise(remise);
            ligne.calculateTotal();
            
            totalField.setText(ligne.getTotal().toString());
            
        } catch (NumberFormatException e) {
            totalField.setText("Erreur");
        }
    }
    
    private void saveLigne() {
        try {
            // Validate fields
            if (articleCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un article.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String prixText = prixUnitaireField.getText().trim();
            if (prixText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir un prix unitaire.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Update ligne with form data
            ligne.setArticle((Article) articleCombo.getSelectedItem());
            ligne.setQuantite(BigDecimal.valueOf((Double) quantiteSpinner.getValue()));
            ligne.setPrixUnitaire(new BigDecimal(prixText));
            ligne.setRemise(BigDecimal.valueOf((Double) remiseSpinner.getValue()));
            ligne.setDescription(descriptionArea.getText());
            ligne.calculateTotal();
            
            dataSaved = true;
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Le prix unitaire doit être un nombre valide.",
                "Erreur de format", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la sauvegarde: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureDialog() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
    
    public LigneDevis getLigne() {
        return ligne;
    }
}
