package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.BonLivraison;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Devis;
import com.gestioncommerciale.model.LigneLivraison;
import com.gestioncommerciale.service.BonLivraisonService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for adding/editing bon de livraison
 */
public class BonLivraisonEditDialog extends JDialog {
    private final BonLivraisonService bonLivraisonService;
    private final BonLivraison bonLivraison;
    private boolean dataSaved = false;
    
    // Form fields
    private JTextField numeroField;
    private JComboBox<Client> clientCombo;
    private JComboBox<Devis> devisCombo;
    private JTextField dateCreationField;
    private JTextField dateLivraisonField;
    private JTextField adresseLivraisonField;
    private JTextField transporteurField;
    private JTextField modeLivraisonField;
    private JTextArea observationsArea;
    private JComboBox<BonLivraison.StatutLivraison> statutCombo;
    
    // Lines table
    private JTable lignesTable;
    private LignesTableModel lignesTableModel;
    private JButton addLigneButton, editLigneButton, deleteLigneButton, loadFromDevisButton;
    
    // Action buttons
    private JButton saveButton, cancelButton;
    
    public BonLivraisonEditDialog(JFrame parent, BonLivraison bonLivraison) {
        super(parent, bonLivraison == null ? "Nouveau Bon de Livraison" : "Modifier Bon de Livraison", true);
        this.bonLivraisonService = new BonLivraisonService();
        this.bonLivraison = bonLivraison != null ? bonLivraison : new BonLivraison();
        
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
        devisCombo = new JComboBox<>();
        dateCreationField = new JTextField(20);
        dateCreationField.setEditable(false);
        dateLivraisonField = new JTextField(20);
        adresseLivraisonField = new JTextField(30);
        transporteurField = new JTextField(20);
        modeLivraisonField = new JTextField(20);
        observationsArea = new JTextArea(3, 30);
        observationsArea.setLineWrap(true);
        observationsArea.setWrapStyleWord(true);
        
        statutCombo = new JComboBox<>(BonLivraison.StatutLivraison.values());
        
        // Lines table
        lignesTableModel = new LignesTableModel();
        lignesTable = new JTable(lignesTableModel);
        lignesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Line buttons
        addLigneButton = UIUtils.createStyledButton("Ajouter Article", UIUtils.SUCCESS_COLOR);
        editLigneButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteLigneButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        loadFromDevisButton = UIUtils.createStyledButton("Charger depuis Devis", UIUtils.WARNING_COLOR);
        
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
        panel.setBorder(BorderFactory.createTitledBorder("Informations du Bon de Livraison"));
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
        panel.add(new JLabel("Devis:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(devisCombo, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Date Création:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(dateCreationField, gbc);
        
        // Row 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Date Livraison:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(dateLivraisonField, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Statut:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(statutCombo, gbc);
        
        // Row 4
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Adresse Livraison:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(adresseLivraisonField, gbc);
        
        // Row 5
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Transporteur:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(transporteurField, gbc);
        
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Mode Livraison:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        panel.add(modeLivraisonField, gbc);
        
        // Row 6 - Observations
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(new JLabel("Observations:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(observationsArea), gbc);
        
        return panel;
    }
    
    private JPanel createLignesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Articles à Livrer"));
        
        JScrollPane scrollPane = new JScrollPane(lignesTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addLigneButton);
        buttonPanel.add(editLigneButton);
        buttonPanel.add(deleteLigneButton);
        buttonPanel.add(loadFromDevisButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveBonLivraison());
        cancelButton.addActionListener(e -> dispose());
        
        addLigneButton.addActionListener(e -> addLigne());
        editLigneButton.addActionListener(e -> editLigne());
        deleteLigneButton.addActionListener(e -> deleteLigne());
        loadFromDevisButton.addActionListener(e -> loadFromDevis());
        
        // Client change listener to update devis combo
        clientCombo.addActionListener(e -> updateDevisCombo());
        
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
            // Load clients
            List<Client> clients = bonLivraisonService.getAllClients();
            clientCombo.removeAllItems();
            clientCombo.addItem(null); // Allow no client selection initially
            for (Client client : clients) {
                clientCombo.addItem(client);
            }
            
            updateDevisCombo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateDevisCombo() {
        devisCombo.removeAllItems();
        devisCombo.addItem(null); // Allow no devis selection
        
        Client selectedClient = (Client) clientCombo.getSelectedItem();
        if (selectedClient != null) {
            try {
                List<Devis> devisList = bonLivraisonService.getDevisByClient(selectedClient);
                for (Devis devis : devisList) {
                    devisCombo.addItem(devis);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des devis: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void populateForm() {
        if (bonLivraison.getId() != null) {
            numeroField.setText(bonLivraison.getNumero());
            numeroField.setEditable(false);
        }
        
        if (bonLivraison.getClient() != null) {
            clientCombo.setSelectedItem(bonLivraison.getClient());
        }
        
        if (bonLivraison.getDevis() != null) {
            devisCombo.setSelectedItem(bonLivraison.getDevis());
        }
        
        if (bonLivraison.getDateCreation() != null) {
            dateCreationField.setText(bonLivraison.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } else {
            dateCreationField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        
        if (bonLivraison.getDateLivraison() != null) {
            dateLivraisonField.setText(bonLivraison.getDateLivraison().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        if (bonLivraison.getAdresseLivraison() != null) {
            adresseLivraisonField.setText(bonLivraison.getAdresseLivraison());
        }
        
        if (bonLivraison.getTransporteur() != null) {
            transporteurField.setText(bonLivraison.getTransporteur());
        }
        
        if (bonLivraison.getModeLivraison() != null) {
            modeLivraisonField.setText(bonLivraison.getModeLivraison());
        }
        
        if (bonLivraison.getObservations() != null) {
            observationsArea.setText(bonLivraison.getObservations());
        }
        
        if (bonLivraison.getStatut() != null) {
            statutCombo.setSelectedItem(bonLivraison.getStatut());
        }
        
        lignesTableModel.setLignes(bonLivraison.getLignes());
    }
    
    private void addLigne() {
        LigneLivraisonEditDialog dialog = new LigneLivraisonEditDialog(this, null, bonLivraisonService.getAllArticles());
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            LigneLivraison newLigne = dialog.getLigne();
            bonLivraison.addLigne(newLigne);
            lignesTableModel.setLignes(bonLivraison.getLignes());
        }
    }
    
    private void editLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        LigneLivraison selectedLigne = lignesTableModel.getLigneAt(selectedRow);
        LigneLivraisonEditDialog dialog = new LigneLivraisonEditDialog(this, selectedLigne, bonLivraisonService.getAllArticles());
        dialog.setVisible(true);
        if (dialog.isDataSaved()) {
            lignesTableModel.setLignes(bonLivraison.getLignes());
        }
    }
    
    private void deleteLigne() {
        int selectedRow = lignesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        LigneLivraison selectedLigne = lignesTableModel.getLigneAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer cette ligne du bon de livraison ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            bonLivraison.removeLigne(selectedLigne);
            lignesTableModel.setLignes(bonLivraison.getLignes());
        }
    }
    
    private void loadFromDevis() {
        Devis selectedDevis = (Devis) devisCombo.getSelectedItem();
        if (selectedDevis == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un devis.",
                "Aucun devis sélectionné", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Charger les articles du devis " + selectedDevis.getNumero() + " ?\n" +
            "Cela remplacera les articles actuels.",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            bonLivraison.populateFromDevis(selectedDevis);
            populateForm(); // Refresh the form
            lignesTableModel.setLignes(bonLivraison.getLignes());
        }
    }
    
    private void saveBonLivraison() {
        try {
            // Validate fields
            if (clientCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un client.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Update bon with form data
            bonLivraison.setClient((Client) clientCombo.getSelectedItem());
            bonLivraison.setDevis((Devis) devisCombo.getSelectedItem());
            bonLivraison.setAdresseLivraison(adresseLivraisonField.getText());
            bonLivraison.setTransporteur(transporteurField.getText());
            bonLivraison.setModeLivraison(modeLivraisonField.getText());
            bonLivraison.setObservations(observationsArea.getText());
            bonLivraison.setStatut((BonLivraison.StatutLivraison) statutCombo.getSelectedItem());
            
            // Save bon de livraison
            bonLivraisonService.save(bonLivraison);
            dataSaved = true;
            
            JOptionPane.showMessageDialog(this, "Bon de livraison sauvegardé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la sauvegarde: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureDialog() {
        setSize(950, 750);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public boolean isDataSaved() {
        return dataSaved;
    }
    
    // Table Model for Lines
    private static class LignesTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "Article", "Désignation", "Qté Demandée", "Qté Livrée", "Emplacement", "N° Série"
        };
        private List<LigneLivraison> lignes = List.of();
        
        public void setLignes(List<LigneLivraison> lignes) {
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
            LigneLivraison ligne = lignes.get(rowIndex);
            switch (columnIndex) {
                case 0: return ligne.getArticle() != null ? ligne.getArticle().getCode() : "N/A";
                case 1: return ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "N/A";
                case 2: return ligne.getQuantiteDemandee();
                case 3: return ligne.getQuantiteLivree();
                case 4: return ligne.getEmplacement() != null ? ligne.getEmplacement() : "";
                case 5: return ligne.getNumeroSerie() != null ? ligne.getNumeroSerie() : "";
                default: return "";
            }
        }
        
        public LigneLivraison getLigneAt(int rowIndex) {
            return lignes.get(rowIndex);
        }
    }
}
