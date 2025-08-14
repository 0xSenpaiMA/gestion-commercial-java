package com.gestioncommerciale.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.service.ArticleService;
import com.gestioncommerciale.service.StockAlertService;

/**
 * Fenêtre de gestion du stock avec alertes de seuil
 */
public class StockManagementFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private ArticleService articleService;
    private StockAlertService stockAlertService;
    
    // Components
    private JTable stockTable;
    private StockTableModel tableModel;
    private JLabel alertCountLabel;
    private JCheckBox showAlertsOnlyCheckBox;
    private JButton refreshButton, updateStockButton, viewDetailsButton;
    private JPanel alertPanel;
    
    public StockManagementFrame() {
        this.articleService = new ArticleService();
        this.stockAlertService = new StockAlertService();
        
        initializeComponents();
        layoutComponents();
        bindEvents();
        refreshData();
        
        setTitle("Gestion du Stock et Alertes");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Alert info
        alertCountLabel = new JLabel();
        alertCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        showAlertsOnlyCheckBox = new JCheckBox("Afficher uniquement les alertes");
        
        // Table
        tableModel = new StockTableModel();
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setupTable();
        
        // Buttons
        refreshButton = new JButton("Actualiser");
        updateStockButton = new JButton("Modifier Stock");
        viewDetailsButton = new JButton("Voir Détails");
        
        // Initially disable buttons that require selection
        updateStockButton.setEnabled(false);
        viewDetailsButton.setEnabled(false);
        
        // Alert panel
        alertPanel = new JPanel();
        alertPanel.setBackground(new Color(255, 240, 240));
        alertPanel.setBorder(BorderFactory.createTitledBorder("Alertes de Stock"));
    }
    
    private void setupTable() {
        // Column widths
        TableColumnModel columnModel = stockTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);   // Code
        columnModel.getColumn(1).setPreferredWidth(200);  // Désignation
        columnModel.getColumn(2).setPreferredWidth(100);  // Stock actuel
        columnModel.getColumn(3).setPreferredWidth(80);   // Stock min
        columnModel.getColumn(4).setPreferredWidth(80);   // Stock max
        columnModel.getColumn(5).setPreferredWidth(80);   // Unité
        columnModel.getColumn(6).setPreferredWidth(120);  // Catégorie
        columnModel.getColumn(7).setPreferredWidth(80);   // Statut
        
        // Custom renderer for stock columns to highlight alerts
        stockTable.getColumnModel().getColumn(2).setCellRenderer(new StockCellRenderer()); // Stock actuel
        stockTable.getColumnModel().getColumn(7).setCellRenderer(new AlertStatusRenderer()); // Statut
        
        // Row height
        stockTable.setRowHeight(25);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Top panel with alert info and controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        // Alert info panel
        JPanel alertInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        alertInfoPanel.add(alertCountLabel);
        alertInfoPanel.add(Box.createHorizontalStrut(20));
        alertInfoPanel.add(showAlertsOnlyCheckBox);
        
        topPanel.add(alertInfoPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Stock des Articles"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(updateStockButton);
        buttonPanel.add(viewDetailsButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void bindEvents() {
        // Table selection
        stockTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = stockTable.getSelectedRow() != -1;
                updateStockButton.setEnabled(hasSelection);
                viewDetailsButton.setEnabled(hasSelection);
            }
        });
        
        // Double-click to update stock
        stockTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    updateStock();
                }
            }
        });
        
        // Checkbox to filter alerts
        showAlertsOnlyCheckBox.addActionListener(e -> refreshData());
        
        // Buttons
        refreshButton.addActionListener(e -> refreshData());
        updateStockButton.addActionListener(e -> updateStock());
        viewDetailsButton.addActionListener(e -> viewArticleDetails());
    }
    
    private void updateStock() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Article article = tableModel.getArticleAt(selectedRow);
        StockUpdateDialog dialog = new StockUpdateDialog(this, article, stockAlertService);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refreshData();
            
            // Show alert if article is now in low stock
            if (stockAlertService.isStockAlert(article)) {
                JOptionPane.showMessageDialog(
                    this,
                    "ATTENTION: L'article \"" + article.getDesignation() + "\" est maintenant en alerte de stock!\n" +
                    "Stock actuel: " + article.getStockActuel() + " (seuil: " + article.getStockMin() + ")",
                    "Alerte de Stock",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }
    
    private void viewArticleDetails() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Article article = tableModel.getArticleAt(selectedRow);
        ArticleDetailsDialog dialog = new ArticleDetailsDialog(this, article);
        dialog.setVisible(true);
    }
    
    private void refreshData() {
        try {
            List<Article> articles;
            
            if (showAlertsOnlyCheckBox.isSelected()) {
                articles = stockAlertService.getStockAlerts();
            } else {
                articles = articleService.getActiveArticles();
            }
            
            tableModel.setArticles(articles);
            
            // Update alert count
            int alertCount = stockAlertService.getStockAlertCount();
            int criticalCount = stockAlertService.getCriticalStockArticles().size();
            
            String alertText = String.format("Alertes: %d article(s) | Ruptures: %d article(s)", alertCount, criticalCount);
            alertCountLabel.setText(alertText);
            
            if (alertCount > 0) {
                alertCountLabel.setForeground(Color.RED);
            } else {
                alertCountLabel.setForeground(new Color(0, 150, 0));
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors du chargement des données de stock:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    // Table Model
    private static class StockTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = {
            "Code", "Désignation", "Stock Actuel", "Stock Min", "Stock Max", "Unité", "Catégorie", "Statut"
        };
        
        private List<Article> articles = List.of();
        
        public void setArticles(List<Article> articles) {
            this.articles = articles;
            fireTableDataChanged();
        }
        
        public Article getArticleAt(int row) {
            return articles.get(row);
        }
        
        @Override
        public int getRowCount() {
            return articles.size();
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            Article article = articles.get(row);
            
            switch (column) {
                case 0: return article.getCode();
                case 1: return article.getDesignation();
                case 2: return article.getStockActuel();
                case 3: return article.getStockMin();
                case 4: return article.getStockMax();
                case 5: return article.getUnite();
                case 6: return article.getCategorie();
                case 7: 
                    if (article.getStockActuel() == 0) {
                        return "RUPTURE";
                    } else if (article.isStockLow()) {
                        return "ALERTE";
                    } else {
                        return "OK";
                    }
                default: return "";
            }
        }
    }
    
    // Custom cell renderer for stock values
    private static class StockCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                StockTableModel model = (StockTableModel) table.getModel();
                Article article = model.getArticleAt(row);
                
                if (article.getStockActuel() == 0) {
                    c.setBackground(new Color(255, 200, 200)); // Light red for zero stock
                    c.setForeground(Color.RED);
                } else if (article.isStockLow()) {
                    c.setBackground(new Color(255, 245, 200)); // Light orange for low stock
                    c.setForeground(new Color(200, 100, 0));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            }
            
            setHorizontalAlignment(JLabel.CENTER);
            return c;
        }
    }
    
    // Custom cell renderer for alert status
    private static class AlertStatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String status = (String) value;
                switch (status) {
                    case "RUPTURE":
                        c.setBackground(new Color(255, 200, 200)); // Light red
                        c.setForeground(Color.RED);
                        setFont(getFont().deriveFont(Font.BOLD));
                        break;
                    case "ALERTE":
                        c.setBackground(new Color(255, 245, 200)); // Light orange
                        c.setForeground(new Color(200, 100, 0));
                        setFont(getFont().deriveFont(Font.BOLD));
                        break;
                    case "OK":
                        c.setBackground(new Color(230, 255, 230)); // Light green
                        c.setForeground(new Color(0, 150, 0));
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                        break;
                }
            }
            
            setHorizontalAlignment(JLabel.CENTER);
            return c;
        }
    }
}
