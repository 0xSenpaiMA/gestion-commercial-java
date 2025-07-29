package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.model.BonCommande;
import com.gestioncommerciale.model.LigneCommande;
import com.gestioncommerciale.service.ArticleService;

/**
 * Dialog for editing purchase order lines
 */
public class LigneCommandeEditDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private LigneCommande ligneCommande;
    private BonCommande bonCommande;
    private ArticleService articleService;
    private boolean confirmed = false;
    
    // Form fields
    private JComboBox<Article> articleComboBox;
    private JTextField quantiteField;
    private JTextField prixUnitaireField;
    private JTextField remiseField;
    private JTextField totalField;
    private JTextArea descriptionArea;
    private JTextField dateLivraisonSouhaiteeField;
    private JTextField referenceFournisseurField;
    
    // Buttons
    private JButton okButton;
    private JButton cancelButton;
    
    public LigneCommandeEditDialog(JDialog parent, LigneCommande ligneCommande, BonCommande bonCommande) {
        super(parent, ligneCommande == null ? "Nouvelle Ligne de Commande" : "Modifier Ligne de Commande", true);
        this.ligneCommande = ligneCommande;
        this.bonCommande = bonCommande;
        this.articleService = new ArticleService();
        
        if (this.ligneCommande == null) {
            this.ligneCommande = new LigneCommande();
            this.ligneCommande.setQuantite(BigDecimal.ONE);
            this.ligneCommande.setPrixUnitaire(BigDecimal.ZERO);
            this.ligneCommande.setRemise(BigDecimal.ZERO);
        }
        
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        
        // Create buttons panel
        JPanel buttonsPanel = createButtonsPanel();
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add listeners
        addListeners();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Détails de la Ligne"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 1: Article
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Article:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.gridwidth = 2;
        articleComboBox = new JComboBox<>();
        articleComboBox.setPreferredSize(new Dimension(300, 25));
        panel.add(articleComboBox, gbc);
        
        // Row 2: Quantité and Prix Unitaire
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Quantité:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        quantiteField = new JTextField();
        quantiteField.setPreferredSize(new Dimension(100, 25));
        panel.add(quantiteField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Prix Unitaire (€):"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        prixUnitaireField = new JTextField();
        prixUnitaireField.setPreferredSize(new Dimension(100, 25));
        panel.add(prixUnitaireField, gbc);
        
        // Row 3: Remise and Total
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Remise (%):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        remiseField = new JTextField();
        remiseField.setPreferredSize(new Dimension(100, 25));
        panel.add(remiseField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Total (€):"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        totalField = new JTextField();
        totalField.setEditable(false);
        totalField.setPreferredSize(new Dimension(100, 25));
        panel.add(totalField, gbc);
        
        // Row 4: Date Livraison Souhaitée
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Date Livraison Souhaitée:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        dateLivraisonSouhaiteeField = new JTextField();
        dateLivraisonSouhaiteeField.setPreferredSize(new Dimension(100, 25));
        panel.add(dateLivraisonSouhaiteeField, gbc);
        
        // Row 5: Référence Fournisseur
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Référence Fournisseur:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.gridwidth = 3;
        referenceFournisseurField = new JTextField();
        referenceFournisseurField.setPreferredSize(new Dimension(200, 25));
        panel.add(referenceFournisseurField, gbc);
        
        // Row 6: Description
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.gridwidth = 3;
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setPreferredSize(new Dimension(300, 100));
        panel.add(descriptionScrollPane, gbc);
        
        return panel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        okButton = new JButton("Sauvegarder");
        cancelButton = new JButton("Annuler");
        
        panel.add(okButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private void addListeners() {
        // Article selection listener
        articleComboBox.addActionListener(e -> {
            Article selectedArticle = (Article) articleComboBox.getSelectedItem();
            if (selectedArticle != null) {
                prixUnitaireField.setText(selectedArticle.getPrixVente().toString());
                calculateTotal();
            }
        });
        
        // Calculation listeners
        quantiteField.addActionListener(e -> calculateTotal());
        prixUnitaireField.addActionListener(e -> calculateTotal());
        remiseField.addActionListener(e -> calculateTotal());
        
        // Focus listeners for calculation on field exit
        quantiteField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                calculateTotal();
            }
        });
        
        prixUnitaireField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                calculateTotal();
            }
        });
        
        remiseField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                calculateTotal();
            }
        });
        
        // Button listeners
        okButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> cancel());
    }
    
    private void loadData() {
        try {
            // Load articles
            List<Article> articles = articleService.getAllArticles();
            DefaultComboBoxModel<Article> articleModel = new DefaultComboBoxModel<>();
            for (Article article : articles) {
                articleModel.addElement(article);
            }
            articleComboBox.setModel(articleModel);
            
            // Populate fields
            populateFields();
            calculateTotal();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateFields() {
        if (ligneCommande.getArticle() != null) {
            articleComboBox.setSelectedItem(ligneCommande.getArticle());
        }
        
        quantiteField.setText(ligneCommande.getQuantite().toString());
        prixUnitaireField.setText(ligneCommande.getPrixUnitaire().toString());
        remiseField.setText(ligneCommande.getRemise().toString());
        
        if (ligneCommande.getDateLivraisonSouhaitee() != null) {
            dateLivraisonSouhaiteeField.setText(ligneCommande.getDateLivraisonSouhaitee().format(DATE_FORMATTER));
        }
        
        referenceFournisseurField.setText(ligneCommande.getReferenceFournisseur() != null ? ligneCommande.getReferenceFournisseur() : "");
        descriptionArea.setText(ligneCommande.getDescription() != null ? ligneCommande.getDescription() : "");
    }
    
    private void calculateTotal() {
        try {
            BigDecimal quantite = new BigDecimal(quantiteField.getText().trim().isEmpty() ? "0" : quantiteField.getText().trim());
            BigDecimal prixUnitaire = new BigDecimal(prixUnitaireField.getText().trim().isEmpty() ? "0" : prixUnitaireField.getText().trim());
            BigDecimal remise = new BigDecimal(remiseField.getText().trim().isEmpty() ? "0" : remiseField.getText().trim());
            
            BigDecimal total = quantite.multiply(prixUnitaire);
            if (remise.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal montantRemise = total.multiply(remise).divide(new BigDecimal("100"));
                total = total.subtract(montantRemise);
            }
            
            totalField.setText(String.format("%.2f", total));
            
        } catch (NumberFormatException e) {
            totalField.setText("0.00");
        }
    }
    
    private boolean validateData() {
        // Validate article
        Article article = (Article) articleComboBox.getSelectedItem();
        if (article == null) {
            JOptionPane.showMessageDialog(this, "L'article est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            articleComboBox.requestFocus();
            return false;
        }
        
        // Validate quantité
        try {
            BigDecimal quantite = new BigDecimal(quantiteField.getText().trim());
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "La quantité doit être supérieure à zéro.", "Erreur", JOptionPane.ERROR_MESSAGE);
                quantiteField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La quantité doit être un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            quantiteField.requestFocus();
            return false;
        }
        
        // Validate prix unitaire
        try {
            BigDecimal prixUnitaire = new BigDecimal(prixUnitaireField.getText().trim());
            if (prixUnitaire.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Le prix unitaire ne peut pas être négatif.", "Erreur", JOptionPane.ERROR_MESSAGE);
                prixUnitaireField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le prix unitaire doit être un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            prixUnitaireField.requestFocus();
            return false;
        }
        
        // Validate remise
        try {
            BigDecimal remise = new BigDecimal(remiseField.getText().trim());
            if (remise.compareTo(BigDecimal.ZERO) < 0 || remise.compareTo(new BigDecimal("100")) > 0) {
                JOptionPane.showMessageDialog(this, "La remise doit être entre 0 et 100.", "Erreur", JOptionPane.ERROR_MESSAGE);
                remiseField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La remise doit être un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            remiseField.requestFocus();
            return false;
        }
        
        // Validate date
        try {
            if (!dateLivraisonSouhaiteeField.getText().trim().isEmpty()) {
                LocalDate.parse(dateLivraisonSouhaiteeField.getText().trim(), DATE_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format de date invalide (dd/MM/yyyy).", "Erreur", JOptionPane.ERROR_MESSAGE);
            dateLivraisonSouhaiteeField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void save() {
        if (!validateData()) {
            return;
        }
        
        try {
            // Update ligneCommande with form data
            ligneCommande.setArticle((Article) articleComboBox.getSelectedItem());
            ligneCommande.setQuantite(new BigDecimal(quantiteField.getText().trim()));
            ligneCommande.setPrixUnitaire(new BigDecimal(prixUnitaireField.getText().trim()));
            ligneCommande.setRemise(new BigDecimal(remiseField.getText().trim()));
            ligneCommande.setDescription(descriptionArea.getText().trim());
            ligneCommande.setReferenceFournisseur(referenceFournisseurField.getText().trim());
            
            if (!dateLivraisonSouhaiteeField.getText().trim().isEmpty()) {
                ligneCommande.setDateLivraisonSouhaitee(LocalDate.parse(dateLivraisonSouhaiteeField.getText().trim(), DATE_FORMATTER));
            }
            
            // Calculate total
            ligneCommande.calculateTotal();
            
            confirmed = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la sauvegarde: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancel() {
        confirmed = false;
        dispose();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public LigneCommande getLigneCommande() {
        return ligneCommande;
    }
}
