package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.Article;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Devis;
import com.gestioncommerciale.model.LigneDevis;
import com.gestioncommerciale.service.DevisService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Devis management form with CRUD operations
 */
public class DevisManagementFrame extends JFrame {
    private JTable devisTable;
    private DevisTableModel tableModel;
    private final DevisService devisService;
    private JButton addButton, editButton, deleteButton, refreshButton, printButton, duplicateButton;
    
    public DevisManagementFrame() {
        this.devisService = new DevisService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDevis();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Create table
        tableModel = new DevisTableModel();
        devisTable = new JTable(tableModel);
        devisTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        devisTable.setRowHeight(25);
        
        // Configure table columns
        devisTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        devisTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Numéro
        devisTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Client
        devisTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Date création
        devisTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Date validité
        devisTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Total HT
        devisTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Total TTC
        devisTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Statut
        
        // Create buttons
        addButton = UIUtils.createStyledButton("Nouveau Devis", UIUtils.SUCCESS_COLOR);
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
        JLabel titleLabel = new JLabel("Gestion des Devis");
        titleLabel.setFont(UIUtils.TITLE_FONT);
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(devisTable);
        scrollPane.setPreferredSize(new Dimension(900, 400));
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
        addButton.addActionListener(e -> showAddDevisDialog());
        editButton.addActionListener(e -> showEditDevisDialog());
        deleteButton.addActionListener(e -> deleteSelectedDevis());
        refreshButton.addActionListener(e -> loadDevis());
        printButton.addActionListener(e -> printSelectedDevis());
        duplicateButton.addActionListener(e -> duplicateSelectedDevis());
        
        // Enable/disable buttons based on selection
        devisTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = devisTable.getSelectedRow() != -1;
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
        setTitle("Gestion des Devis - Gestion Commerciale");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void loadDevis() {
        try {
            List<Devis> devisList = devisService.getAllDevis();
            tableModel.setDevisList(devisList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des devis: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddDevisDialog() {
        DevisEditDialog dialog = new DevisEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadDevis();
        }
    }
    
    private void showEditDevisDialog() {
        int selectedRow = devisTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un devis à modifier.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Devis selectedDevis = tableModel.getDevisAt(selectedRow);
        DevisEditDialog dialog = new DevisEditDialog(this, selectedDevis);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadDevis();
        }
    }
    
    private void deleteSelectedDevis() {
        int selectedRow = devisTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un devis à supprimer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Devis selectedDevis = tableModel.getDevisAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer le devis " + selectedDevis.getNumero() + " ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                devisService.delete(selectedDevis);
                loadDevis();
                JOptionPane.showMessageDialog(this,
                    "Devis supprimé avec succès.",
                    "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void duplicateSelectedDevis() {
        int selectedRow = devisTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un devis à dupliquer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Devis selectedDevis = tableModel.getDevisAt(selectedRow);
        Devis duplicatedDevis = devisService.duplicateDevis(selectedDevis);
        
        DevisEditDialog dialog = new DevisEditDialog(this, duplicatedDevis);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            loadDevis();
        }
    }
    
    private void printSelectedDevis() {
        int selectedRow = devisTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un devis à imprimer.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Devis selectedDevis = tableModel.getDevisAt(selectedRow);
        
        try {
            // Create a simple print report
            DevisPrintDialog printDialog = new DevisPrintDialog(this, selectedDevis);
            printDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'impression: " + e.getMessage(),
                "Erreur d'impression", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Table Model for Devis
    private static class DevisTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Numéro", "Client", "Date Création", "Date Validité", "Total HT", "Total TTC", "Statut"
        };
        private List<Devis> devisList = List.of();
        
        public void setDevisList(List<Devis> devisList) {
            this.devisList = devisList;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return devisList.size();
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
            Devis devis = devisList.get(rowIndex);
            switch (columnIndex) {
                case 0: return devis.getId();
                case 1: return devis.getNumero();
                case 2: return devis.getClient() != null ? devis.getClient().getNom() + " " + 
                    (devis.getClient().getPrenom() != null ? devis.getClient().getPrenom() : "") : "N/A";
                case 3: return devis.getDateCreation() != null ? 
                    devis.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
                case 4: return devis.getDateValidite() != null ? 
                    devis.getDateValidite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                case 5: return devis.getTotalHT() != null ? devis.getTotalHT() + " €" : "0,00 €";
                case 6: return devis.getTotalTTC() != null ? devis.getTotalTTC() + " €" : "0,00 €";
                case 7: return devis.getStatut() != null ? devis.getStatut().getLibelle() : "N/A";
                default: return "";
            }
        }
        
        public Devis getDevisAt(int rowIndex) {
            return devisList.get(rowIndex);
        }
    }
}
