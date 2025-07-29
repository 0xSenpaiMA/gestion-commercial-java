package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.service.ArticleService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Article management form with CRUD operations
 */
public class ArticleManagementFrame extends JFrame {
    private JTable articleTable;
    private ArticleTableModel tableModel;
    private final ArticleService articleService;
    private JButton addButton, editButton, deleteButton, refreshButton;
    
    public ArticleManagementFrame() {
        this.articleService = new ArticleService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadArticles();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Create table
        tableModel = new ArticleTableModel();
        articleTable = new JTable(tableModel);
        articleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articleTable.setRowHeight(25);
        
        // Configure table columns
        articleTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        articleTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Code
        articleTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Désignation
        articleTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Prix Achat
        articleTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Prix Vente
        articleTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Stock
        articleTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // Unité
        articleTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Catégorie
        articleTable.getColumnModel().getColumn(8).setPreferredWidth(50);  // Actif
        
        // Create buttons
        addButton = UIUtils.createStyledButton("Ajouter", UIUtils.SUCCESS_COLOR);
        editButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        refreshButton = UIUtils.createStyledButton("Actualiser", UIUtils.SECONDARY_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Gestion des Articles");
        titleLabel.setFont(UIUtils.TITLE_FONT);
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(articleTable);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(e -> showAddArticleDialog());
        editButton.addActionListener(e -> showEditArticleDialog());
        deleteButton.addActionListener(e -> deleteSelectedArticle());
        refreshButton.addActionListener(e -> loadArticles());
        
        // Enable/disable buttons based on selection
        articleTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = articleTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
        
        // Initially disable edit/delete buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void configureWindow() {
        setTitle("Gestion des Articles - Gestion Commerciale");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        UIUtils.centerWindow(this);
    }
    
    private void loadArticles() {
        try {
            List<Article> articles = articleService.getAllArticles();
            tableModel.setArticles(articles);
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors du chargement des articles: " + e.getMessage());
        }
    }
    
    private void showAddArticleDialog() {
        ArticleFormDialog dialog = new ArticleFormDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Article newArticle = dialog.getArticle();
            try {
                articleService.save(newArticle);
                loadArticles();
                UIUtils.showSuccessMessage(this, "Article ajouté avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de l'ajout de l'article: " + e.getMessage());
            }
        }
    }
    
    private void showEditArticleDialog() {
        int selectedRow = articleTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Article selectedArticle = tableModel.getArticleAt(selectedRow);
        ArticleFormDialog dialog = new ArticleFormDialog(this, selectedArticle);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Article updatedArticle = dialog.getArticle();
            try {
                articleService.save(updatedArticle);
                loadArticles();
                UIUtils.showSuccessMessage(this, "Article modifié avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la modification de l'article: " + e.getMessage());
            }
        }
    }
    
    private void deleteSelectedArticle() {
        int selectedRow = articleTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Article selectedArticle = tableModel.getArticleAt(selectedRow);
        
        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer l'article '" + selectedArticle.getDesignation() + "' ?",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                articleService.delete(selectedArticle.getId());
                loadArticles();
                UIUtils.showSuccessMessage(this, "Article supprimé avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la suppression de l'article: " + e.getMessage());
            }
        }
    }
    
    /**
     * Table model for articles
     */
    private static class ArticleTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Code", "Désignation", "Prix Achat", "Prix Vente", "Stock", "Unité", "Catégorie", "Actif"
        };
        private List<Article> articles;
        
        public void setArticles(List<Article> articles) {
            this.articles = articles;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return articles != null ? articles.size() : 0;
        }
        
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (articles == null || rowIndex >= articles.size()) {
                return null;
            }
            
            Article article = articles.get(rowIndex);
            switch (columnIndex) {
                case 0: return article.getId();
                case 1: return article.getCode();
                case 2: return article.getDesignation();
                case 3: return article.getPrixAchat() != null ? article.getPrixAchat().toString() + " DH" : "-";
                case 4: return article.getPrixVente() != null ? article.getPrixVente().toString() + " DH" : "-";
                case 5: return article.getStockActuel();
                case 6: return article.getUnite();
                case 7: return article.getCategorie();
                case 8: return article.isActive() ? "Oui" : "Non";
                default: return null;
            }
        }
        
        public Article getArticleAt(int rowIndex) {
            return articles.get(rowIndex);
        }
    }
    
    /**
     * Dialog for adding/editing articles
     */
    private static class ArticleFormDialog extends JDialog {
        private JTextField codeField, designationField, prixAchatField, prixVenteField;
        private JTextField stockActuelField, stockMinField, stockMaxField, uniteField;
        private JTextField categorieField, fournisseurField, codeBarresField, tvaRateField;
        private JTextArea descriptionArea;
        private JCheckBox activeCheckBox;
        private boolean confirmed = false;
        private Article article;
        
        public ArticleFormDialog(JFrame parent, Article article) {
            super(parent, article == null ? "Ajouter un article" : "Modifier l'article", true);
            this.article = article;
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            if (article != null) {
                populateFields();
            }
            pack();
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            codeField = new JTextField(20);
            designationField = new JTextField(20);
            prixAchatField = new JTextField(20);
            prixVenteField = new JTextField(20);
            stockActuelField = new JTextField(20);
            stockMinField = new JTextField(20);
            stockMaxField = new JTextField(20);
            uniteField = new JTextField(20);
            categorieField = new JTextField(20);
            fournisseurField = new JTextField(20);
            codeBarresField = new JTextField(20);
            tvaRateField = new JTextField(20);
            descriptionArea = new JTextArea(3, 20);
            activeCheckBox = new JCheckBox("Actif", true);
            
            // Set default values
            uniteField.setText("Unité");
            tvaRateField.setText("20.0");
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Row 0: Code et Désignation
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Code *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(codeField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Désignation *:"), gbc);
            gbc.gridx = 3;
            formPanel.add(designationField, gbc);
            
            // Row 1: Description
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(new JScrollPane(descriptionArea), gbc);
            gbc.gridwidth = 1;
            
            // Row 2: Prix d'achat et Prix de vente
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Prix d'achat:"), gbc);
            gbc.gridx = 1;
            formPanel.add(prixAchatField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Prix de vente *:"), gbc);
            gbc.gridx = 3;
            formPanel.add(prixVenteField, gbc);
            
            // Row 3: Stock
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Stock actuel:"), gbc);
            gbc.gridx = 1;
            formPanel.add(stockActuelField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Stock min:"), gbc);
            gbc.gridx = 3;
            formPanel.add(stockMinField, gbc);
            
            // Row 4: Stock max et Unité
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Stock max:"), gbc);
            gbc.gridx = 1;
            formPanel.add(stockMaxField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Unité:"), gbc);
            gbc.gridx = 3;
            formPanel.add(uniteField, gbc);
            
            // Row 5: Catégorie et Fournisseur
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Catégorie:"), gbc);
            gbc.gridx = 1;
            formPanel.add(categorieField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Fournisseur:"), gbc);
            gbc.gridx = 3;
            formPanel.add(fournisseurField, gbc);
            
            // Row 6: Code barres et TVA
            gbc.gridx = 0; gbc.gridy = 6;
            formPanel.add(new JLabel("Code barres:"), gbc);
            gbc.gridx = 1;
            formPanel.add(codeBarresField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Taux TVA (%):"), gbc);
            gbc.gridx = 3;
            formPanel.add(tvaRateField, gbc);
            
            // Row 7: Actif
            gbc.gridx = 0; gbc.gridy = 7;
            gbc.gridwidth = 2;
            formPanel.add(activeCheckBox, gbc);
            gbc.gridwidth = 1;
            
            add(formPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
            JButton cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.ERROR_COLOR);
            
            saveButton.addActionListener(e -> saveArticle());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            // Add input validation if needed
        }
        
        private void populateFields() {
            codeField.setText(article.getCode());
            designationField.setText(article.getDesignation());
            descriptionArea.setText(article.getDescription());
            if (article.getPrixAchat() != null) {
                prixAchatField.setText(article.getPrixAchat().toString());
            }
            if (article.getPrixVente() != null) {
                prixVenteField.setText(article.getPrixVente().toString());
            }
            stockActuelField.setText(String.valueOf(article.getStockActuel()));
            stockMinField.setText(String.valueOf(article.getStockMin()));
            stockMaxField.setText(String.valueOf(article.getStockMax()));
            uniteField.setText(article.getUnite());
            categorieField.setText(article.getCategorie());
            fournisseurField.setText(article.getFournisseur());
            codeBarresField.setText(article.getCodeBarres());
            if (article.getTvaRate() != null) {
                tvaRateField.setText(article.getTvaRate().toString());
            }
            activeCheckBox.setSelected(article.isActive());
        }
        
        private void saveArticle() {
            // Validate required fields
            if (codeField.getText().trim().isEmpty() || 
                designationField.getText().trim().isEmpty() ||
                prixVenteField.getText().trim().isEmpty()) {
                UIUtils.showWarningMessage(this, "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            
            // Create or update article
            if (article == null) {
                article = new Article();
            }
            
            article.setCode(codeField.getText().trim());
            article.setDesignation(designationField.getText().trim());
            article.setDescription(descriptionArea.getText().trim());
            article.setUnite(uniteField.getText().trim());
            article.setCategorie(categorieField.getText().trim());
            article.setFournisseur(fournisseurField.getText().trim());
            article.setCodeBarres(codeBarresField.getText().trim());
            article.setActive(activeCheckBox.isSelected());
            
            // Parse numeric fields
            try {
                if (!prixAchatField.getText().trim().isEmpty()) {
                    article.setPrixAchat(new BigDecimal(prixAchatField.getText().trim()));
                }
                article.setPrixVente(new BigDecimal(prixVenteField.getText().trim()));
                
                if (!stockActuelField.getText().trim().isEmpty()) {
                    article.setStockActuel(Integer.parseInt(stockActuelField.getText().trim()));
                }
                if (!stockMinField.getText().trim().isEmpty()) {
                    article.setStockMin(Integer.parseInt(stockMinField.getText().trim()));
                }
                if (!stockMaxField.getText().trim().isEmpty()) {
                    article.setStockMax(Integer.parseInt(stockMaxField.getText().trim()));
                }
                if (!tvaRateField.getText().trim().isEmpty()) {
                    article.setTvaRate(new BigDecimal(tvaRateField.getText().trim()));
                }
            } catch (NumberFormatException e) {
                UIUtils.showWarningMessage(this, "Format numérique invalide dans un des champs.");
                return;
            }
            
            confirmed = true;
            dispose();
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public Article getArticle() {
            return article;
        }
    }
}
