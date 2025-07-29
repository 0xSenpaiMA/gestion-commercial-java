package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.Facture;
import com.gestioncommerciale.service.FactureService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Facture management form with CRUD operations
 */
public class FactureManagementFrame extends JFrame {
    private JTable factureTable;
    private FactureTableModel tableModel;
    private final FactureService factureService;
    private JButton addButton, editButton, deleteButton, refreshButton, printButton, duplicateButton;
    private JButton createFromBonButton;
    
    public FactureManagementFrame() {
        this.factureService = new FactureService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadFactures();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Create table
        tableModel = new FactureTableModel();
        factureTable = new JTable(tableModel);
        factureTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        factureTable.setRowHeight(25);
        
        // Configure table columns
        factureTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        factureTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Numéro
        factureTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Client
        factureTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Date facturation
        factureTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Date échéance
        factureTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Montant TTC
        factureTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Montant payé
        factureTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Statut
        factureTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Bon livraison
        
        // Create buttons
        addButton = UIUtils.createStyledButton("Nouvelle Facture", UIUtils.SUCCESS_COLOR);
        editButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        refreshButton = UIUtils.createStyledButton("Actualiser", UIUtils.SECONDARY_COLOR);
        printButton = UIUtils.createStyledButton("Imprimer", UIUtils.PRIMARY_COLOR);
        duplicateButton = UIUtils.createStyledButton("Dupliquer", UIUtils.WARNING_COLOR);
        createFromBonButton = UIUtils.createStyledButton("Créer depuis Bon", UIUtils.SUCCESS_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Gestion des Factures");
        titleLabel.setFont(UIUtils.TITLE_FONT);
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(factureTable);
        scrollPane.setPreferredSize(new Dimension(1100, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(createFromBonButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(duplicateButton);
        buttonPanel.add(printButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(e -> showAddFactureDialog());
        createFromBonButton.addActionListener(e -> showCreateFromBonLivraisonDialog());
        editButton.addActionListener(e -> showEditFactureDialog());
        deleteButton.addActionListener(e -> deleteSelectedFacture());
        refreshButton.addActionListener(e -> loadFactures());
        printButton.addActionListener(e -> printSelectedFacture());
        duplicateButton.addActionListener(e -> duplicateSelectedFacture());
        
        // Enable/disable buttons based on selection
        factureTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = factureTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
            printButton.setEnabled(hasSelection);
            duplicateButton.setEnabled(hasSelection);
        });
        
        // Initially disable buttons that require selection
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        printButton.setEnabled(false);
        duplicateButton.setEnabled(false);
    }
    
    private void configureWindow() {
        setTitle("Gestion des Factures - Gestion Commerciale");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void loadFactures() {
        try {
            List<Facture> factures = factureService.getAllFactures();
            tableModel.setFacturesList(factures);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des factures: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddFactureDialog() {
        FactureEditDialog dialog = new FactureEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadFactures();
        }
    }
    
    private void showCreateFromBonLivraisonDialog() {
        CreateFactureFromBonDialog dialog = new CreateFactureFromBonDialog(this);
        dialog.setVisible(true);
        if (dialog.getCreatedFacture() != null) {
            loadFactures();
        }
    }
    
    private void showEditFactureDialog() {
        int selectedRow = factureTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une facture à modifier.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Facture selectedFacture = tableModel.getFactureAt(selectedRow);
        FactureEditDialog dialog = new FactureEditDialog(this, selectedFacture);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadFactures();
        }
    }
    
    private void deleteSelectedFacture() {
        int selectedRow = factureTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une facture à supprimer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Facture selectedFacture = tableModel.getFactureAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer la facture " + selectedFacture.getNumero() + " ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                factureService.delete(selectedFacture);
                loadFactures();
                JOptionPane.showMessageDialog(this,
                    "Facture supprimée avec succès.",
                    "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void duplicateSelectedFacture() {
        int selectedRow = factureTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une facture à dupliquer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Facture selectedFacture = tableModel.getFactureAt(selectedRow);
        Facture duplicatedFacture = factureService.duplicateFacture(selectedFacture);
        
        FactureEditDialog dialog = new FactureEditDialog(this, duplicatedFacture);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadFactures();
        }
    }
    
    private void printSelectedFacture() {
        int selectedRow = factureTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une facture à imprimer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Facture selectedFacture = tableModel.getFactureAt(selectedRow);
        
        try {
            FacturePrintDialog printDialog = new FacturePrintDialog(this, selectedFacture);
            printDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'impression: " + e.getMessage(),
                "Erreur d'impression", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Table Model for Facture
    private static class FactureTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Numéro", "Client", "Date Facturation", "Date Échéance", 
            "Montant TTC", "Montant Payé", "Statut", "Bon Livraison"
        };
        private List<Facture> facturesList = List.of();
        
        public void setFacturesList(List<Facture> facturesList) {
            this.facturesList = facturesList;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return facturesList.size();
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
            Facture facture = facturesList.get(rowIndex);
            switch (columnIndex) {
                case 0: return facture.getId();
                case 1: return facture.getNumero();
                case 2: return facture.getClient() != null ? facture.getClient().getNom() + " " + 
                    (facture.getClient().getPrenom() != null ? facture.getClient().getPrenom() : "") : "N/A";
                case 3: return facture.getDateFacturation() != null ? 
                    facture.getDateFacturation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                case 4: return facture.getDateEcheance() != null ? 
                    facture.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Non définie";
                case 5: return formatCurrency(facture.getTotalTTC());
                case 6: return formatCurrency(facture.getMontantPaye());
                case 7: return facture.getStatut() != null ? facture.getStatut().getLibelle() : "N/A";
                case 8: return facture.getBonLivraison() != null ? facture.getBonLivraison().getNumero() : "Aucun";
                default: return "";
            }
        }
        
        private String formatCurrency(BigDecimal amount) {
            return amount != null ? String.format("%.2f €", amount) : "0.00 €";
        }
        
        public Facture getFactureAt(int rowIndex) {
            return facturesList.get(rowIndex);
        }
    }
}
