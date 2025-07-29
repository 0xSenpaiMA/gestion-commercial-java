package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.gestioncommerciale.model.BonCommande;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.LigneCommande;
import com.gestioncommerciale.service.BonCommandeService;

/**
 * Dialog for editing purchase orders
 */
public class BonCommandeEditDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private BonCommande bonCommande;
    private final BonCommandeService bonCommandeService;
    private boolean confirmed = false;
    
    // Form fields
    private JTextField numeroField;
    private JComboBox<Client> fournisseurComboBox;
    private JTextField dateCommandeField;
    private JTextField dateLivraisonPrevueField;
    private JTextField adresseLivraisonField;
    private JTextField conditionsPaiementField;
    private JTextArea observationsArea;
    private JTextField tauxTVAField;
    private JComboBox<BonCommande.StatutCommande> statutComboBox;
    private JTextField numeroDevisFournisseurField;
    
    // Totals fields
    private JTextField totalHTField;
    private JTextField totalTVAField;
    private JTextField totalTTCField;
    
    // Lines table
    private JTable lignesTable;
    private DefaultTableModel lignesTableModel;
    
    // Buttons
    private JButton addLigneButton;
    private JButton editLigneButton;
    private JButton deleteLigneButton;
    private JButton okButton;
    private JButton cancelButton;
    
    public BonCommandeEditDialog(JFrame parent, BonCommande bonCommande) {
        super(parent, bonCommande == null ? "Nouveau Bon de Commande" : "Modifier Bon de Commande", true);
        this.bonCommande = bonCommande;
        this.bonCommandeService = new BonCommandeService();
        
        if (this.bonCommande == null) {
            this.bonCommande = new BonCommande();
            this.bonCommande.setNumero(bonCommandeService.generateNumero());
            this.bonCommande.setDateCommande(LocalDate.now());
            this.bonCommande.setDateLivraisonPrevue(LocalDate.now().plusDays(15));
            this.bonCommande.setTauxTVA(new BigDecimal("20.00"));
            this.bonCommande.setStatut(BonCommande.StatutCommande.BROUILLON);
        }
        
        initializeUI();
        loadData();
    }
    
    private void initializeUI() {
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        
        // Create lines panel
        JPanel lignesPanel = createLignesPanel();
        
        // Create totals panel
        JPanel totalsPanel = createTotalsPanel();
        
        // Create buttons panel
        JPanel buttonsPanel = createButtonsPanel();
        
        // Assemble the dialog
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(lignesPanel, BorderLayout.CENTER);
        topPanel.add(totalsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Add listeners
        addListeners();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Informations du Bon de Commande"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 1: Numéro and Fournisseur
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Numéro:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        numeroField = new JTextField();
        numeroField.setPreferredSize(new Dimension(150, 25));
        panel.add(numeroField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Fournisseur:"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fournisseurComboBox = new JComboBox<>();
        fournisseurComboBox.setPreferredSize(new Dimension(200, 25));
        panel.add(fournisseurComboBox, gbc);
        
        // Row 2: Dates
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Date Commande:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        dateCommandeField = new JTextField();
        dateCommandeField.setPreferredSize(new Dimension(150, 25));
        panel.add(dateCommandeField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Date Livraison Prévue:"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        dateLivraisonPrevueField = new JTextField();
        dateLivraisonPrevueField.setPreferredSize(new Dimension(150, 25));
        panel.add(dateLivraisonPrevueField, gbc);
        
        // Row 3: Adresse and Conditions
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Adresse Livraison:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        adresseLivraisonField = new JTextField();
        adresseLivraisonField.setPreferredSize(new Dimension(150, 25));
        panel.add(adresseLivraisonField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Conditions Paiement:"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        conditionsPaiementField = new JTextField();
        conditionsPaiementField.setPreferredSize(new Dimension(150, 25));
        panel.add(conditionsPaiementField, gbc);
        
        // Row 4: TVA, Statut, and Numero Devis
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Taux TVA (%):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tauxTVAField = new JTextField();
        tauxTVAField.setPreferredSize(new Dimension(150, 25));
        panel.add(tauxTVAField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Statut:"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        statutComboBox = new JComboBox<>(BonCommande.StatutCommande.values());
        statutComboBox.setPreferredSize(new Dimension(150, 25));
        panel.add(statutComboBox, gbc);
        
        // Row 5: Numero Devis and Observations
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("N° Devis Fournisseur:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        numeroDevisFournisseurField = new JTextField();
        numeroDevisFournisseurField.setPreferredSize(new Dimension(150, 25));
        panel.add(numeroDevisFournisseurField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Observations:"), gbc);
        
        gbc.gridx = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        observationsArea = new JTextArea(3, 20);
        observationsArea.setLineWrap(true);
        observationsArea.setWrapStyleWord(true);
        JScrollPane observationsScrollPane = new JScrollPane(observationsArea);
        observationsScrollPane.setPreferredSize(new Dimension(200, 60));
        panel.add(observationsScrollPane, gbc);
        
        return panel;
    }
    
    private JPanel createLignesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Lignes de Commande"));
        
        // Create table
        String[] columnNames = {"Article", "Quantité", "Prix Unitaire", "Remise %", "Total"};
        lignesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        lignesTable = new JTable(lignesTableModel);
        lignesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lignesTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(lignesTable);
        scrollPane.setPreferredSize(new Dimension(800, 200));
        
        // Create buttons for lines
        JPanel lignesButtonsPanel = new JPanel(new FlowLayout());
        addLigneButton = new JButton("Ajouter");
        editLigneButton = new JButton("Modifier");
        deleteLigneButton = new JButton("Supprimer");
        
        editLigneButton.setEnabled(false);
        deleteLigneButton.setEnabled(false);
        
        lignesButtonsPanel.add(addLigneButton);
        lignesButtonsPanel.add(editLigneButton);
        lignesButtonsPanel.add(deleteLigneButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(lignesButtonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new TitledBorder("Totaux"));
        
        panel.add(new JLabel("Total HT:"));
        totalHTField = new JTextField(10);
        totalHTField.setEditable(false);
        panel.add(totalHTField);
        
        panel.add(new JLabel("Total TVA:"));
        totalTVAField = new JTextField(10);
        totalTVAField.setEditable(false);
        panel.add(totalTVAField);
        
        panel.add(new JLabel("Total TTC:"));
        totalTTCField = new JTextField(10);
        totalTTCField.setEditable(false);
        panel.add(totalTTCField);
        
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
        // Lines table selection
        lignesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateLignesButtonStates();
            }
        });
        
        // Double-click on lines table
        lignesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lignesTable.getSelectedRow() != -1) {
                    editLigne();
                }
            }
        });
        
        // Lines buttons
        addLigneButton.addActionListener(e -> addLigne());
        editLigneButton.addActionListener(e -> editLigne());
        deleteLigneButton.addActionListener(e -> deleteLigne());
        
        // Main buttons
        okButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> cancel());
        
        // TVA change listener
        tauxTVAField.addActionListener(e -> updateTotals());
    }
    
    private void loadData() {
        try {
            // Load fournisseurs
            List<Client> fournisseurs = bonCommandeService.getFournisseurs();
            DefaultComboBoxModel<Client> fournisseurModel = new DefaultComboBoxModel<>();
            for (Client fournisseur : fournisseurs) {
                fournisseurModel.addElement(fournisseur);
            }
            fournisseurComboBox.setModel(fournisseurModel);
            
            // Populate fields
            populateFields();
            updateLignesTable();
            updateTotals();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateFields() {
        numeroField.setText(bonCommande.getNumero());
        
        if (bonCommande.getFournisseur() != null) {
            fournisseurComboBox.setSelectedItem(bonCommande.getFournisseur());
        }
        
        if (bonCommande.getDateCommande() != null) {
            dateCommandeField.setText(bonCommande.getDateCommande().format(DATE_FORMATTER));
        }
        
        if (bonCommande.getDateLivraisonPrevue() != null) {
            dateLivraisonPrevueField.setText(bonCommande.getDateLivraisonPrevue().format(DATE_FORMATTER));
        }
        
        adresseLivraisonField.setText(bonCommande.getAdresseLivraison() != null ? bonCommande.getAdresseLivraison() : "");
        conditionsPaiementField.setText(bonCommande.getConditionsPaiement() != null ? bonCommande.getConditionsPaiement() : "");
        observationsArea.setText(bonCommande.getObservations() != null ? bonCommande.getObservations() : "");
        tauxTVAField.setText(bonCommande.getTauxTVA() != null ? bonCommande.getTauxTVA().toString() : "20.00");
        statutComboBox.setSelectedItem(bonCommande.getStatut());
        numeroDevisFournisseurField.setText(bonCommande.getNumeroDevisFournisseur() != null ? bonCommande.getNumeroDevisFournisseur() : "");
    }
    
    private void updateLignesTable() {
        lignesTableModel.setRowCount(0);
        
        for (LigneCommande ligne : bonCommande.getLignes()) {
            Object[] rowData = {
                ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "",
                ligne.getQuantite().toString(),
                String.format("%.2f €", ligne.getPrixUnitaire()),
                String.format("%.1f %%", ligne.getRemise()),
                String.format("%.2f €", ligne.getTotal())
            };
            lignesTableModel.addRow(rowData);
        }
    }
    
    private void updateTotals() {
        bonCommande.calculateTotals();
        
        totalHTField.setText(String.format("%.2f €", bonCommande.getTotalHT()));
        totalTVAField.setText(String.format("%.2f €", bonCommande.getMontantTVA()));
        totalTTCField.setText(String.format("%.2f €", bonCommande.getTotalTTC()));
    }
    
    private void updateLignesButtonStates() {
        boolean hasSelection = lignesTable.getSelectedRow() != -1;
        editLigneButton.setEnabled(hasSelection);
        deleteLigneButton.setEnabled(hasSelection);
    }
    
    private void addLigne() {
        LigneCommandeEditDialog dialog = new LigneCommandeEditDialog(this, null, bonCommande);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            LigneCommande newLigne = dialog.getLigneCommande();
            bonCommande.addLigne(newLigne);
            updateLignesTable();
            updateTotals();
        }
    }
    
    private void editLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow < bonCommande.getLignes().size()) {
            LigneCommande ligne = bonCommande.getLignes().get(selectedRow);
            LigneCommandeEditDialog dialog = new LigneCommandeEditDialog(this, ligne, bonCommande);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                updateLignesTable();
                updateTotals();
            }
        }
    }
    
    private void deleteLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow < bonCommande.getLignes().size()) {
            int result = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer cette ligne ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                LigneCommande ligne = bonCommande.getLignes().get(selectedRow);
                bonCommande.removeLigne(ligne);
                updateLignesTable();
                updateTotals();
            }
        }
    }
    
    private boolean validateData() {
        // Validate numero
        String numero = numeroField.getText().trim();
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le numéro est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            numeroField.requestFocus();
            return false;
        }
        
        // Validate fournisseur
        Client fournisseur = (Client) fournisseurComboBox.getSelectedItem();
        if (fournisseur == null) {
            JOptionPane.showMessageDialog(this, "Le fournisseur est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
            fournisseurComboBox.requestFocus();
            return false;
        }
        
        // Validate dates
        try {
            if (!dateCommandeField.getText().trim().isEmpty()) {
                LocalDate.parse(dateCommandeField.getText().trim(), DATE_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format de date invalide (dd/MM/yyyy).", "Erreur", JOptionPane.ERROR_MESSAGE);
            dateCommandeField.requestFocus();
            return false;
        }
        
        try {
            if (!dateLivraisonPrevueField.getText().trim().isEmpty()) {
                LocalDate.parse(dateLivraisonPrevueField.getText().trim(), DATE_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format de date de livraison invalide (dd/MM/yyyy).", "Erreur", JOptionPane.ERROR_MESSAGE);
            dateLivraisonPrevueField.requestFocus();
            return false;
        }
        
        // Validate TVA
        try {
            new BigDecimal(tauxTVAField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le taux de TVA doit être un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            tauxTVAField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void save() {
        if (!validateData()) {
            return;
        }
        
        try {
            // Update bonCommande with form data
            bonCommande.setNumero(numeroField.getText().trim());
            bonCommande.setFournisseur((Client) fournisseurComboBox.getSelectedItem());
            
            if (!dateCommandeField.getText().trim().isEmpty()) {
                bonCommande.setDateCommande(LocalDate.parse(dateCommandeField.getText().trim(), DATE_FORMATTER));
            }
            
            if (!dateLivraisonPrevueField.getText().trim().isEmpty()) {
                bonCommande.setDateLivraisonPrevue(LocalDate.parse(dateLivraisonPrevueField.getText().trim(), DATE_FORMATTER));
            }
            
            bonCommande.setAdresseLivraison(adresseLivraisonField.getText().trim());
            bonCommande.setConditionsPaiement(conditionsPaiementField.getText().trim());
            bonCommande.setObservations(observationsArea.getText().trim());
            bonCommande.setTauxTVA(new BigDecimal(tauxTVAField.getText().trim()));
            bonCommande.setStatut((BonCommande.StatutCommande) statutComboBox.getSelectedItem());
            bonCommande.setNumeroDevisFournisseur(numeroDevisFournisseurField.getText().trim());
            
            // Save
            bonCommandeService.save(bonCommande);
            
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
    
    public BonCommande getBonCommande() {
        return bonCommande;
    }
}
