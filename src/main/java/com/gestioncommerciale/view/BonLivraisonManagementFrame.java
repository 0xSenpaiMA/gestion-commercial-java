package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import com.gestioncommerciale.model.BonLivraison;
import com.gestioncommerciale.service.BonLivraisonService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Bon de Livraison management form with CRUD operations
 */
public class BonLivraisonManagementFrame extends JFrame {
    private JTable bonLivraisonTable;
    private BonLivraisonTableModel tableModel;
    private final BonLivraisonService bonLivraisonService;
    private JButton addButton, editButton, deleteButton, refreshButton, printButton, duplicateButton;
    
    public BonLivraisonManagementFrame() {
        this.bonLivraisonService = new BonLivraisonService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadBonsLivraison();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Create table
        tableModel = new BonLivraisonTableModel();
        bonLivraisonTable = new JTable(tableModel);
        bonLivraisonTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bonLivraisonTable.setRowHeight(25);
        
        // Configure table columns
        bonLivraisonTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        bonLivraisonTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Numéro
        bonLivraisonTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Client
        bonLivraisonTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Date création
        bonLivraisonTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Date livraison
        bonLivraisonTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Statut
        bonLivraisonTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Devis
        bonLivraisonTable.getColumnModel().getColumn(7).setPreferredWidth(150); // Transporteur
        
        // Create buttons
        addButton = UIUtils.createStyledButton("Nouveau Bon", UIUtils.SUCCESS_COLOR);
        editButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        refreshButton = UIUtils.createStyledButton("Actualiser", UIUtils.SECONDARY_COLOR);
        printButton = UIUtils.createStyledButton("Imprimer", UIUtils.PRIMARY_COLOR);
        duplicateButton = UIUtils.createStyledButton("Dupliquer", UIUtils.WARNING_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Gestion des Bons de Livraison");
        titleLabel.setFont(UIUtils.TITLE_FONT);
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(bonLivraisonTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(duplicateButton);
        buttonPanel.add(printButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(e -> showAddBonLivraisonDialog());
        editButton.addActionListener(e -> showEditBonLivraisonDialog());
        deleteButton.addActionListener(e -> deleteSelectedBonLivraison());
        refreshButton.addActionListener(e -> loadBonsLivraison());
        printButton.addActionListener(e -> printSelectedBonLivraison());
        duplicateButton.addActionListener(e -> duplicateSelectedBonLivraison());
        
        // Enable/disable buttons based on selection
        bonLivraisonTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = bonLivraisonTable.getSelectedRow() != -1;
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
        setTitle("Gestion des Bons de Livraison - Gestion Commerciale");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void loadBonsLivraison() {
        try {
            List<BonLivraison> bonsLivraison = bonLivraisonService.getAllBonsLivraison();
            tableModel.setBonsLivraisonList(bonsLivraison);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des bons de livraison: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddBonLivraisonDialog() {
        BonLivraisonEditDialog dialog = new BonLivraisonEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadBonsLivraison();
        }
    }
    
    private void showEditBonLivraisonDialog() {
        int selectedRow = bonLivraisonTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un bon de livraison à modifier.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        BonLivraison selectedBon = tableModel.getBonLivraisonAt(selectedRow);
        BonLivraisonEditDialog dialog = new BonLivraisonEditDialog(this, selectedBon);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadBonsLivraison();
        }
    }
    
    private void deleteSelectedBonLivraison() {
        int selectedRow = bonLivraisonTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un bon de livraison à supprimer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        BonLivraison selectedBon = tableModel.getBonLivraisonAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer le bon de livraison " + selectedBon.getNumero() + " ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bonLivraisonService.delete(selectedBon);
                loadBonsLivraison();
                JOptionPane.showMessageDialog(this,
                    "Bon de livraison supprimé avec succès.",
                    "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void duplicateSelectedBonLivraison() {
        int selectedRow = bonLivraisonTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un bon de livraison à dupliquer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        BonLivraison selectedBon = tableModel.getBonLivraisonAt(selectedRow);
        BonLivraison duplicatedBon = bonLivraisonService.duplicateBonLivraison(selectedBon);
        
        BonLivraisonEditDialog dialog = new BonLivraisonEditDialog(this, duplicatedBon);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadBonsLivraison();
        }
    }
    
    private void printSelectedBonLivraison() {
        int selectedRow = bonLivraisonTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un bon de livraison à imprimer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        BonLivraison selectedBon = tableModel.getBonLivraisonAt(selectedRow);
        
        try {
            // Create a simple print report
            BonLivraisonPrintDialog printDialog = new BonLivraisonPrintDialog(this, selectedBon);
            printDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'impression: " + e.getMessage(),
                "Erreur d'impression", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Table Model for BonLivraison
    private static class BonLivraisonTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Numéro", "Client", "Date Création", "Date Livraison", "Statut", "Devis", "Transporteur"
        };
        private List<BonLivraison> bonsLivraisonList = List.of();
        
        public void setBonsLivraisonList(List<BonLivraison> bonsLivraisonList) {
            this.bonsLivraisonList = bonsLivraisonList;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return bonsLivraisonList.size();
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
            BonLivraison bon = bonsLivraisonList.get(rowIndex);
            switch (columnIndex) {
                case 0: return bon.getId();
                case 1: return bon.getNumero();
                case 2: return bon.getClient() != null ? bon.getClient().getNom() + " " + 
                    (bon.getClient().getPrenom() != null ? bon.getClient().getPrenom() : "") : "N/A";
                case 3: return bon.getDateCreation() != null ? 
                    bon.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
                case 4: return bon.getDateLivraison() != null ? 
                    bon.getDateLivraison().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Non programmée";
                case 5: return bon.getStatut() != null ? bon.getStatut().getLibelle() : "N/A";
                case 6: return bon.getDevis() != null ? bon.getDevis().getNumero() : "Aucun";
                case 7: return bon.getTransporteur() != null ? bon.getTransporteur() : "Non défini";
                default: return "";
            }
        }
        
        public BonLivraison getBonLivraisonAt(int rowIndex) {
            return bonsLivraisonList.get(rowIndex);
        }
    }
}
