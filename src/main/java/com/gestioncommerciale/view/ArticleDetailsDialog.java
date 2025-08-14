package com.gestioncommerciale.view;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.*;

import com.gestioncommerciale.model.Article;

/**
 * Dialogue pour afficher les détails d'un article
 */
public class ArticleDetailsDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private final Article article;
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.00");
    
    public ArticleDetailsDialog(Window parent, Article article) {
        super(parent, "Détails de l'Article", ModalityType.APPLICATION_MODAL);
        
        this.article = article;
        
        initializeComponents();
        
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel(article.getDesignation());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 15);
        
        // Basic info
        addDetailRow(detailsPanel, gbc, 0, "Code:", article.getCode());
        addDetailRow(detailsPanel, gbc, 1, "Catégorie:", article.getCategorie());
        addDetailRow(detailsPanel, gbc, 2, "Unité:", article.getUnite() != null ? article.getUnite() : "N/A");
        
        // Separator
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        detailsPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        
        // Prices
        addDetailRow(detailsPanel, gbc, 4, "Prix d'achat:", formatPrice(article.getPrixAchat()) + " DH");
        addDetailRow(detailsPanel, gbc, 5, "Prix de vente:", formatPrice(article.getPrixVente()) + " DH");
        
        double margin = 0;
        if (article.getPrixAchat() != null && article.getPrixAchat().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marginCalc = article.getPrixVente()
                .subtract(article.getPrixAchat())
                .divide(article.getPrixAchat(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            margin = marginCalc.doubleValue();
        }
        String marginText = PRICE_FORMAT.format(margin) + "%";
        if (margin > 0) {
            marginText = "+" + marginText;
        }
        addDetailRow(detailsPanel, gbc, 6, "Marge:", marginText);
        
        // Separator
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        detailsPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        
        // Stock info
        addDetailRow(detailsPanel, gbc, 8, "Stock actuel:", String.valueOf(article.getStockActuel()));
        addDetailRow(detailsPanel, gbc, 9, "Stock minimum:", String.valueOf(article.getStockMin()));
        addDetailRow(detailsPanel, gbc, 10, "Stock maximum:", String.valueOf(article.getStockMax()));
        
        // Stock status
        String stockStatus;
        Color statusColor;
        if (article.getStockActuel() == 0) {
            stockStatus = "RUPTURE DE STOCK";
            statusColor = Color.RED;
        } else if (article.isStockLow()) {
            stockStatus = "ALERTE DE STOCK";
            statusColor = new Color(200, 100, 0); // Orange
        } else {
            stockStatus = "STOCK NORMAL";
            statusColor = new Color(0, 150, 0); // Green
        }
        
        JLabel statusLabel = new JLabel(stockStatus);
        statusLabel.setForeground(statusColor);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        
        gbc.gridx = 1; gbc.gridy = 11;
        detailsPanel.add(statusLabel, gbc);
        
        gbc.gridx = 0;
        JLabel statusTitleLabel = new JLabel("Statut:");
        statusTitleLabel.setFont(statusTitleLabel.getFont().deriveFont(Font.BOLD));
        detailsPanel.add(statusTitleLabel, gbc);
        
        mainPanel.add(detailsPanel);
        
        // Description if available
        if (article.getDescription() != null && !article.getDescription().trim().isEmpty()) {
            mainPanel.add(Box.createVerticalStrut(15));
            
            JPanel descPanel = new JPanel(new BorderLayout());
            descPanel.setBorder(BorderFactory.createTitledBorder("Description"));
            
            JTextArea descArea = new JTextArea(article.getDescription());
            descArea.setEditable(false);
            descArea.setWrapStyleWord(true);
            descArea.setLineWrap(true);
            descArea.setRows(3);
            descArea.setBackground(getBackground());
            
            JScrollPane scrollPane = new JScrollPane(descArea);
            scrollPane.setPreferredSize(new Dimension(300, 80));
            
            descPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(descPanel);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
        panel.add(labelComponent, gbc);
        
        gbc.gridx = 1;
        JLabel valueComponent = new JLabel(value != null ? value : "N/A");
        panel.add(valueComponent, gbc);
    }
    
    private String formatPrice(BigDecimal price) {
        return price != null ? PRICE_FORMAT.format(price.doubleValue()) : "0.00";
    }
}
