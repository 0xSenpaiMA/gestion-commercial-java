package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
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
 * Dialog for adding/editing devis
 */
public class DevisEditDialog extends JDialog {
    private final DevisService devisService;
    private final Devis devis;
    private boolean dataSaved = false;
    
    // Form fields
    private JTextField numeroField;
    private JComboBox<Client> clientCombo;
    private JTextField dateCreationField;
    private JTextField dateValiditeField;
    private JSpinner tauxTVASpinner;
    private JTextArea observationsArea;
    private JTextField totalHTField;
    private JTextField montantTVAField;
    private JTextField totalTTCField;
    private JComboBox<Devis.StatutDevis> statutCombo;
    
    // Lines table
    private JTable lignesTable;
    private LignesTableModel lignesTableModel;
    private JButton addLigneButton, editLigneButton, deleteLigneButton;
    
    // Action buttons
    private JButton saveButton, cancelButton;
    
    public DevisEditDialog(JFrame parent, Devis devis) {
        super(parent, devis == null ? "Nouveau Devis" : "Modifier Devis", true);
        this.devisService = new DevisService();
        this.devis = devis != null ? devis : new Devis();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateForm();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Header fields
        numeroField = new JTextField(20);
        clientCombo = new JComboBox<>();
        dateCreationField = new JTextField(20);
        dateCreationField.setEditable(false);
        dateValiditeField = new JTextField(20);
        tauxTVASpinner = new JSpinner(new SpinnerNumberModel(20.0, 0.0, 100.0, 0.1));
        observationsArea = new JTextArea(3, 30);
        observationsArea.setLineWrap(true);
        observationsArea.setWrapStyleWord(true);
        
        // Total fields
        totalHTField = new JTextField(15);
        totalHTField.setEditable(false);
        montantTVAField = new JTextField(15);
        montantTVAField.setEditable(false);
        totalTTCField = new JTextField(15);
        totalTTCField.setEditable(false);
        
        statutCombo = new JComboBox<>(Devis.StatutDevis.values());
        
        // Lines table
        lignesTableModel = new LignesTableModel();
        lignesTable = new JTable(lignesTableModel);
        lignesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Line buttons
        addLigneButton = UIUtils.createStyledButton("Ajouter Article", UIUtils.SUCCESS_COLOR);
        editLigneButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteLigneButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        
        // Action buttons
        saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
        cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.SECONDARY_COLOR);
        
        // Load combo data
        loadComboData();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Lines panel
        JPanel lignesPanel = createLignesPanel();
        mainPanel.add(lignesPanel, BorderLayout.CENTER);
        
        // Totals panel
        JPanel totalsPanel = createTotalsPanel();
        mainPanel.add(totalsPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informations du Devis"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Numéro:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(numeroField, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(clientCombo, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Date Création:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(dateCreationField, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Date Validité:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(dateValiditeField, gbc);
        
        // Row 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("TVA (%):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(tauxTVASpinner, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Statut:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(statutCombo, gbc);
        
        // Row 4 - Observations
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(new JLabel("Observations:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(observationsArea), gbc);
        
        return panel;
    }
    
    private JPanel createLignesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Articles du Devis"));
        
        JScrollPane scrollPane = new JScrollPane(lignesTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addLigneButton);
        buttonPanel.add(editLigneButton);
        buttonPanel.add(deleteLigneButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Totaux"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Total HT:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(totalHTField, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Montant TVA:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(montantTVAField, gbc);
        
        gbc.gridx = 4; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Total TTC:"), gbc);
        gbc.gridx = 5; gbc.anchor = GridBagConstraints.WEST;
        panel.add(totalTTCField, gbc);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveDevis());
        cancelButton.addActionListener(e -> dispose());
        
        addLigneButton.addActionListener(e -> addLigne());
        editLigneButton.addActionListener(e -> editLigne());
        deleteLigneButton.addActionListener(e -> deleteLigne());
        
        // TVA change listener
        tauxTVASpinner.addChangeListener(e -> {
            BigDecimal newTaux = BigDecimal.valueOf((Double) tauxTVASpinner.getValue());
            devis.setTauxTVA(newTaux);
            updateTotals();
        });
        
        // Enable/disable ligne buttons
        lignesTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = lignesTable.getSelectedRow() != -1;
            editLigneButton.setEnabled(hasSelection);
            deleteLigneButton.setEnabled(hasSelection);
        });
        
        editLigneButton.setEnabled(false);
        deleteLigneButton.setEnabled(false);
    }
    
    private void loadComboData() {
        try {
            List<Client> clients = devisService.getAllClients();
            clientCombo.removeAllItems();
            for (Client client : clients) {
                clientCombo.addItem(client);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des clients: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateForm() {
        if (devis.getId() != null) {
            numeroField.setText(devis.getNumero());
            numeroField.setEditable(false);
        }
        
        if (devis.getClient() != null) {
            clientCombo.setSelectedItem(devis.getClient());
        }
        
        if (devis.getDateCreation() != null) {
            dateCreationField.setText(devis.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            dateCreationField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        
        if (devis.getDateValidite() != null) {
            dateValiditeField.setText(devis.getDateValidite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        if (devis.getTauxTVA() != null) {
            tauxTVASpinner.setValue(devis.getTauxTVA().doubleValue());
        }
        
        if (devis.getObservations() != null) {
            observationsArea.setText(devis.getObservations());
        }
        
        if (devis.getStatut() != null) {
            statutCombo.setSelectedItem(devis.getStatut());
        }
        
        lignesTableModel.setLignes(devis.getLignes());
        updateTotals();
    }
    
    private void updateTotals() {
        devis.calculateTotals();
        
        totalHTField.setText(devis.getTotalHT() != null ? devis.getTotalHT().toString() + " €" : "0,00 €");
        montantTVAField.setText(devis.getMontantTVA() != null ? devis.getMontantTVA().toString() + " €" : "0,00 €");
        totalTTCField.setText(devis.getTotalTTC() != null ? devis.getTotalTTC().toString() + " €" : "0,00 €");
    }
    
    private void addLigne() {
        LigneEditDialog dialog = new LigneEditDialog(this, null, devisService.getAllArticles());
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            LigneDevis newLigne = dialog.getLigne();
            devis.addLigne(newLigne);
            lignesTableModel.setLignes(devis.getLignes());
            updateTotals();
        }
    }
    
    private void editLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        LigneDevis selectedLigne = lignesTableModel.getLigneAt(selectedRow);
        LigneEditDialog dialog = new LigneEditDialog(this, selectedLigne, devisService.getAllArticles());
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            lignesTableModel.setLignes(devis.getLignes());
            updateTotals();
        }
    }
    
    private void deleteLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        LigneDevis selectedLigne = lignesTableModel.getLigneAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer cette ligne du devis ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            devis.removeLigne(selectedLigne);
            lignesTableModel.setLignes(devis.getLignes());
            updateTotals();
        }
    }
    
    private void saveDevis() {
        try {
            // Validate fields
            if (clientCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Update devis with form data
            devis.setClient((Client) clientCombo.getSelectedItem());
            devis.setObservations(observationsArea.getText());
            devis.setStatut((Devis.StatutDevis) statutCombo.getSelectedItem());
            
            // Save devis
            devisService.save(devis);
            dataSaved = true;
            
            JOptionPane.showMessageDialog(this, "Devis sauvegardé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la sauvegarde: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureDialog() {
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
    
    // Table Model for Lines
    private static class LignesTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "Article", "Désignation", "Quantité", "Prix Unitaire", "Remise (%)", "Total"
        };
        private List<LigneDevis> lignes = List.of();
        
        public void setLignes(List<LigneDevis> lignes) {
            this.lignes = lignes;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return lignes.size();
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
            LigneDevis ligne = lignes.get(rowIndex);
            switch (columnIndex) {
                case 0: return ligne.getArticle() != null ? ligne.getArticle().getCode() : "N/A";
                case 1: return ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "N/A";
                case 2: return ligne.getQuantite();
                case 3: return ligne.getPrixUnitaire() + " €";
                case 4: return ligne.getRemise() != null ? ligne.getRemise() + " %" : "0 %";
                case 5: return ligne.getTotal() + " €";
                default: return "";
            }
        }
        
        public LigneDevis getLigneAt(int rowIndex) {
            return lignes.get(rowIndex);
        }
    }
}
