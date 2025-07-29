package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Facture;
import com.gestioncommerciale.model.LigneFacture;
import com.gestioncommerciale.service.ClientService;
import com.gestioncommerciale.service.FactureService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for adding/editing Facture
 */
public class FactureEditDialog extends JDialog {
    private final FactureService factureService;
    private final ClientService clientService;
    private boolean dataSaved = false;
    
    // Form fields
    private JTextField numeroField;
    private JComboBox<Client> clientComboBox;
    private JTextField dateFacturationField;
    private JTextField dateEcheanceField;
    private JComboBox<Facture.StatutFacture> statutComboBox;
    private JTextField modePaiementField;
    private JTextField tauxTVAField;
    private JTextField montantPayeField;
    private JTextArea observationsArea;
    
    // Lines management
    private JTable lignesTable;
    private LignesTableModel lignesTableModel;
    private JButton addLigneButton, editLigneButton, deleteLigneButton;
    
    // Totals
    private JLabel totalHTLabel, montantTVALabel, totalTTCLabel, montantRestantLabel;
    
    // Action buttons
    private JButton saveButton, cancelButton;
    
    private Facture facture;
    
    public FactureEditDialog(JFrame parent, Facture facture) {
        super(parent, "Facture", true);
        this.factureService = new FactureService();
        this.clientService = new ClientService();
        this.facture = facture;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadComboBoxData();
        
        if (facture != null) {
            populateFields();
            setTitle("Modifier Facture - " + facture.getNumero());
        } else {
            this.facture = new Facture();
            this.facture.setNumero(factureService.generateNumero());
            numeroField.setText(this.facture.getNumero());
            setTitle("Nouvelle Facture");
        }
        
        updateTotals();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Basic fields
        numeroField = new JTextField(20);
        clientComboBox = new JComboBox<>();
        dateFacturationField = new JTextField(20);
        dateEcheanceField = new JTextField(20);
        statutComboBox = new JComboBox<>(Facture.StatutFacture.values());
        modePaiementField = new JTextField(20);
        tauxTVAField = new JTextField("20.0", 10);
        montantPayeField = new JTextField("0.00", 10);
        observationsArea = new JTextArea(3, 20);
        observationsArea.setLineWrap(true);
        observationsArea.setWrapStyleWord(true);
        
        // Set date format hint
        dateFacturationField.setToolTipText("Format: dd/MM/yyyy");
        dateEcheanceField.setToolTipText("Format: dd/MM/yyyy");
        
        // Lines table
        lignesTableModel = new LignesTableModel();
        lignesTable = new JTable(lignesTableModel);
        lignesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lignesTable.setRowHeight(25);
        
        // Lines management buttons
        addLigneButton = UIUtils.createStyledButton("Ajouter Ligne", UIUtils.SUCCESS_COLOR);
        editLigneButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteLigneButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        
        // Totals labels
        totalHTLabel = new JLabel("0.00 €");
        montantTVALabel = new JLabel("0.00 €");
        totalTTCLabel = new JLabel("0.00 €");
        montantRestantLabel = new JLabel("0.00 €");
        
        // Action buttons
        saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
        cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.ERROR_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel with form
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 1: Numero and Client
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Numéro:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(numeroField, gbc);
        
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(clientComboBox, gbc);
        
        // Row 2: Dates
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Date Facturation:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(dateFacturationField, gbc);
        
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Date Échéance:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(dateEcheanceField, gbc);
        
        // Row 3: Status and Payment
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Statut:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(statutComboBox, gbc);
        
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Mode Paiement:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(modePaiementField, gbc);
        
        // Row 4: TVA and Payment amount
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Taux TVA (%):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(tauxTVAField, gbc);
        
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Montant Payé:"), gbc);
        gbc.gridx = 3;
        mainPanel.add(montantPayeField, gbc);
        
        // Row 5: Observations
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Observations:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane observationsScroll = new JScrollPane(observationsArea);
        mainPanel.add(observationsScroll, gbc);
        
        add(mainPanel, BorderLayout.NORTH);
        
        // Lines panel
        JPanel lignesPanel = new JPanel(new BorderLayout());
        lignesPanel.setBorder(BorderFactory.createTitledBorder("Lignes de la Facture"));
        
        JScrollPane lignesScroll = new JScrollPane(lignesTable);
        lignesPanel.add(lignesScroll, BorderLayout.CENTER);
        
        // Lines buttons
        JPanel lignesButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lignesButtonPanel.add(addLigneButton);
        lignesButtonPanel.add(editLigneButton);
        lignesButtonPanel.add(deleteLigneButton);
        lignesPanel.add(lignesButtonPanel, BorderLayout.SOUTH);
        
        add(lignesPanel, BorderLayout.CENTER);
        
        // Totals panel
        JPanel totalsPanel = new JPanel(new GridBagLayout());
        totalsPanel.setBorder(BorderFactory.createTitledBorder("Totaux"));
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        totalsPanel.add(new JLabel("Total HT:"), gbc);
        gbc.gridx = 1;
        totalsPanel.add(totalHTLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        totalsPanel.add(new JLabel("Montant TVA:"), gbc);
        gbc.gridx = 1;
        totalsPanel.add(montantTVALabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        totalsPanel.add(new JLabel("Total TTC:"), gbc);
        gbc.gridx = 1;
        totalsPanel.add(totalTTCLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        totalsPanel.add(new JLabel("Montant Restant:"), gbc);
        gbc.gridx = 1;
        totalsPanel.add(montantRestantLabel, gbc);
        
        // Action buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalsPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveFacture());
        cancelButton.addActionListener(e -> dispose());
        
        addLigneButton.addActionListener(e -> addLigne());
        editLigneButton.addActionListener(e -> editLigne());
        deleteLigneButton.addActionListener(e -> deleteLigne());
        
        // Enable/disable ligne buttons based on selection
        lignesTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = lignesTable.getSelectedRow() != -1;
            editLigneButton.setEnabled(hasSelection);
            deleteLigneButton.setEnabled(hasSelection);
        });
        
        // Update totals when TVA rate or payment amount changes
        tauxTVAField.addActionListener(e -> updateTotals());
        montantPayeField.addActionListener(e -> updateTotals());
        
        // Initially disable ligne edit/delete buttons
        editLigneButton.setEnabled(false);
        deleteLigneButton.setEnabled(false);
    }
    
    private void loadComboBoxData() {
        try {
            // Load clients
            List<Client> clients = clientService.getAllClients();
            DefaultComboBoxModel<Client> clientModel = new DefaultComboBoxModel<>();
            for (Client client : clients) {
                clientModel.addElement(client);
            }
            clientComboBox.setModel(clientModel);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateFields() {
        if (facture == null) return;
        
        numeroField.setText(facture.getNumero());
        
        if (facture.getClient() != null) {
            clientComboBox.setSelectedItem(facture.getClient());
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        if (facture.getDateFacturation() != null) {
            dateFacturationField.setText(facture.getDateFacturation().format(formatter));
        }
        
        if (facture.getDateEcheance() != null) {
            dateEcheanceField.setText(facture.getDateEcheance().format(formatter));
        }
        
        statutComboBox.setSelectedItem(facture.getStatut());
        modePaiementField.setText(facture.getModePaiement());
        tauxTVAField.setText(facture.getTauxTVA().toString());
        montantPayeField.setText(facture.getMontantPaye().toString());
        observationsArea.setText(facture.getObservations());
        
        lignesTableModel.setLignes(facture.getLignes());
    }
    
    private void addLigne() {
        LigneFactureEditDialog dialog = new LigneFactureEditDialog((JFrame) getOwner(), null);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            LigneFacture newLigne = dialog.getLigneFacture();
            facture.addLigne(newLigne);
            lignesTableModel.setLignes(facture.getLignes());
            updateTotals();
        }
    }
    
    private void editLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        LigneFacture selectedLigne = lignesTableModel.getLigneAt(selectedRow);
        LigneFactureEditDialog dialog = new LigneFactureEditDialog((JFrame) getOwner(), selectedLigne);
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            lignesTableModel.setLignes(facture.getLignes());
            updateTotals();
        }
    }
    
    private void deleteLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        LigneFacture selectedLigne = lignesTableModel.getLigneAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer cette ligne?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            facture.removeLigne(selectedLigne);
            lignesTableModel.setLignes(facture.getLignes());
            updateTotals();
        }
    }
    
    private void updateTotals() {
        try {
            // Update TVA rate
            BigDecimal tauxTVA = new BigDecimal(tauxTVAField.getText());
            facture.setTauxTVA(tauxTVA);
            
            // Update payment amount
            BigDecimal montantPaye = new BigDecimal(montantPayeField.getText());
            facture.setMontantPaye(montantPaye);
            
            // Recalculate totals
            facture.calculateTotals();
            
            // Update labels
            totalHTLabel.setText(String.format("%.2f €", facture.getTotalHT()));
            montantTVALabel.setText(String.format("%.2f €", facture.getMontantTVA()));
            totalTTCLabel.setText(String.format("%.2f €", facture.getTotalTTC()));
            montantRestantLabel.setText(String.format("%.2f €", facture.getMontantRestant()));
            
        } catch (NumberFormatException e) {
            // Invalid numbers, keep previous values
        }
    }
    
    private void saveFacture() {
        try {
            // Validate required fields
            if (numeroField.getText().trim().isEmpty()) {
                UIUtils.showErrorMessage(this, "Le numéro est obligatoire.");
                return;
            }
            
            if (clientComboBox.getSelectedItem() == null) {
                UIUtils.showErrorMessage(this, "Veuillez sélectionner un client.");
                return;
            }
            
            // Update facture object
            facture.setNumero(numeroField.getText().trim());
            facture.setClient((Client) clientComboBox.getSelectedItem());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            try {
                String dateFacturationText = dateFacturationField.getText().trim();
                if (!dateFacturationText.isEmpty()) {
                    facture.setDateFacturation(LocalDate.parse(dateFacturationText, formatter));
                }
                
                String dateEcheanceText = dateEcheanceField.getText().trim();
                if (!dateEcheanceText.isEmpty()) {
                    facture.setDateEcheance(LocalDate.parse(dateEcheanceText, formatter));
                }
            } catch (DateTimeParseException e) {
                UIUtils.showErrorMessage(this, "Format de date invalide. Utilisez le format dd/MM/yyyy");
                return;
            }
            
            facture.setStatut((Facture.StatutFacture) statutComboBox.getSelectedItem());
            facture.setModePaiement(modePaiementField.getText().trim());
            facture.setObservations(observationsArea.getText().trim());
            
            try {
                facture.setTauxTVA(new BigDecimal(tauxTVAField.getText()));
                facture.setMontantPaye(new BigDecimal(montantPayeField.getText()));
            } catch (NumberFormatException e) {
                UIUtils.showErrorMessage(this, "Format numérique invalide pour TVA ou montant payé.");
                return;
            }
            
            // Save to database
            factureService.save(facture);
            
            dataSaved = true;
            UIUtils.showSuccessMessage(this, "Facture enregistrée avec succès.");
            dispose();
            
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de la sauvegarde: " + e.getMessage());
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
    
    // Table model for lines
    private class LignesTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Article", "Quantité", "Prix Unit.", "Remise %", "Total"};
        private List<LigneFacture> lignes = List.of();
        
        public void setLignes(List<LigneFacture> lignes) {
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
            LigneFacture ligne = lignes.get(rowIndex);
            switch (columnIndex) {
                case 0: return ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "N/A";
                case 1: return ligne.getQuantite();
                case 2: return String.format("%.2f €", ligne.getPrixUnitaire());
                case 3: return ligne.getRemise() + " %";
                case 4: return String.format("%.2f €", ligne.getTotal());
                default: return "";
            }
        }
        
        public LigneFacture getLigneAt(int rowIndex) {
            return lignes.get(rowIndex);
        }
    }
}
