package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.model.LigneFacture;
import com.gestioncommerciale.service.ArticleService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for editing LigneFacture
 */
public class LigneFactureEditDialog extends JDialog {
    private final ArticleService articleService;
    private boolean dataSaved = false;
    
    private JComboBox<Article> articleComboBox;
    private JTextField quantiteField;
    private JTextField prixUnitaireField;
    private JTextField remiseField;
    private JTextArea descriptionArea;
    private JLabel totalLabel;
    
    private JButton saveButton, cancelButton;
    
    private LigneFacture ligneFacture;
    
    public LigneFactureEditDialog(JFrame parent, LigneFacture ligneFacture) {
        super(parent, "Ligne de Facture", true);
        this.articleService = new ArticleService();
        this.ligneFacture = ligneFacture;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadComboBoxData();
        
        if (ligneFacture != null) {
            populateFields();
            setTitle("Modifier Ligne de Facture");
        } else {
            this.ligneFacture = new LigneFacture();
            setTitle("Nouvelle Ligne de Facture");
        }
        
        updateTotal();
        configureDialog();
    }
    
    private void initializeComponents() {
        articleComboBox = new JComboBox<>();
        quantiteField = new JTextField("1.0", 10);
        prixUnitaireField = new JTextField("0.00", 10);
        remiseField = new JTextField("0.0", 10);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        totalLabel = new JLabel("0.00 €");
        
        saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
        cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.ERROR_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Article
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Article:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(articleComboBox, gbc);
        
        // Quantity and Unit Price
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Quantité:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(quantiteField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Prix Unitaire:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(prixUnitaireField, gbc);
        
        // Discount
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Remise (%):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(remiseField, gbc);
        
        // Total
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Total:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(totalLabel, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        mainPanel.add(descriptionScroll, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveLigne());
        cancelButton.addActionListener(e -> dispose());
        
        // Update total when values change
        quantiteField.addActionListener(e -> updateTotal());
        prixUnitaireField.addActionListener(e -> updateTotal());
        remiseField.addActionListener(e -> updateTotal());
        
        // Update price when article changes
        articleComboBox.addActionListener(e -> {
            Article selectedArticle = (Article) articleComboBox.getSelectedItem();
            if (selectedArticle != null && selectedArticle.getPrixVente() != null) {
                prixUnitaireField.setText(selectedArticle.getPrixVente().toString());
                updateTotal();
            }
        });
    }
    
    private void loadComboBoxData() {
        try {
            List<Article> articles = articleService.getAllArticles();
            DefaultComboBoxModel<Article> articleModel = new DefaultComboBoxModel<>();
            for (Article article : articles) {
                articleModel.addElement(article);
            }
            articleComboBox.setModel(articleModel);
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors du chargement des articles: " + e.getMessage());
        }
    }
    
    private void populateFields() {
        if (ligneFacture == null) return;
        
        if (ligneFacture.getArticle() != null) {
            articleComboBox.setSelectedItem(ligneFacture.getArticle());
        }
        
        quantiteField.setText(ligneFacture.getQuantite().toString());
        prixUnitaireField.setText(ligneFacture.getPrixUnitaire().toString());
        remiseField.setText(ligneFacture.getRemise().toString());
        descriptionArea.setText(ligneFacture.getDescription());
    }
    
    private void updateTotal() {
        try {
            BigDecimal quantite = new BigDecimal(quantiteField.getText());
            BigDecimal prixUnitaire = new BigDecimal(prixUnitaireField.getText());
            BigDecimal remise = new BigDecimal(remiseField.getText());
            
            ligneFacture.setQuantite(quantite);
            ligneFacture.setPrixUnitaire(prixUnitaire);
            ligneFacture.setRemise(remise);
            ligneFacture.calculateTotal();
            
            totalLabel.setText(String.format("%.2f €", ligneFacture.getTotal()));
        } catch (NumberFormatException e) {
            totalLabel.setText("0.00 €");
        }
    }
    
    private void saveLigne() {
        try {
            // Validate required fields
            if (articleComboBox.getSelectedItem() == null) {
                UIUtils.showErrorMessage(this, "Veuillez sélectionner un article.");
                return;
            }
            
            // Update ligne object
            ligneFacture.setArticle((Article) articleComboBox.getSelectedItem());
            
            try {
                ligneFacture.setQuantite(new BigDecimal(quantiteField.getText()));
                ligneFacture.setPrixUnitaire(new BigDecimal(prixUnitaireField.getText()));
                ligneFacture.setRemise(new BigDecimal(remiseField.getText()));
            } catch (NumberFormatException e) {
                UIUtils.showErrorMessage(this, "Format numérique invalide.");
                return;
            }
            
            ligneFacture.setDescription(descriptionArea.getText().trim());
            ligneFacture.calculateTotal();
            
            dataSaved = true;
            dispose();
            
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de la sauvegarde: " + e.getMessage());
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
    
    public LigneFacture getLigneFacture() {
        return ligneFacture;
    }
}
