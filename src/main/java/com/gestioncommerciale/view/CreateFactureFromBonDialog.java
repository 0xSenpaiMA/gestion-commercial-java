package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
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
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.BonLivraison;
import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.Facture;
import com.gestioncommerciale.model.LigneLivraison;
import com.gestioncommerciale.service.ClientService;
import com.gestioncommerciale.service.FactureService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialog for creating a Facture from a BonLivraison
 */
public class CreateFactureFromBonDialog extends JDialog {
    private final FactureService factureService;
    private final ClientService clientService;
    
    private JComboBox<Client> clientComboBox;
    private JComboBox<BonLivraison> bonLivraisonComboBox;
    private JTable previewTable;
    private PreviewTableModel previewTableModel;
    
    private JButton createButton, cancelButton;
    
    private Facture createdFacture;
    
    public CreateFactureFromBonDialog(JFrame parent) {
        super(parent, "Créer Facture depuis Bon de Livraison", true);
        this.factureService = new FactureService();
        this.clientService = new ClientService();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadComboBoxData();
        configureDialog();
    }
    
    private void initializeComponents() {
        clientComboBox = new JComboBox<>();
        bonLivraisonComboBox = new JComboBox<>();
        
        previewTableModel = new PreviewTableModel();
        previewTable = new JTable(previewTableModel);
        previewTable.setRowHeight(25);
        
        createButton = UIUtils.createStyledButton("Créer Facture", UIUtils.SUCCESS_COLOR);
        cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.ERROR_COLOR);
        
        // Initially disable create button
        createButton.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Client selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(clientComboBox, gbc);
        
        // Bon de livraison selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Bon de Livraison:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(bonLivraisonComboBox, gbc);
        
        add(formPanel, BorderLayout.NORTH);
        
        // Preview panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Aperçu des lignes à facturer"));
        
        JScrollPane scrollPane = new JScrollPane(previewTable);
        previewPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(previewPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        clientComboBox.addActionListener(e -> {
            Client selectedClient = (Client) clientComboBox.getSelectedItem();
            updateBonLivraisonComboBox(selectedClient);
        });
        
        bonLivraisonComboBox.addActionListener(e -> {
            BonLivraison selectedBon = (BonLivraison) bonLivraisonComboBox.getSelectedItem();
            updatePreview(selectedBon);
        });
        
        createButton.addActionListener(e -> createFacture());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadComboBoxData() {
        try {
            // Load clients
            List<Client> clients = clientService.getAllClients();
            DefaultComboBoxModel<Client> clientModel = new DefaultComboBoxModel<>();
            clientModel.addElement(null); // Allow empty selection
            for (Client client : clients) {
                clientModel.addElement(client);
            }
            clientComboBox.setModel(clientModel);
            
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors du chargement des données: " + e.getMessage());
        }
    }
    
    private void updateBonLivraisonComboBox(Client selectedClient) {
        DefaultComboBoxModel<BonLivraison> bonModel = new DefaultComboBoxModel<>();
        
        if (selectedClient != null) {
            try {
                List<BonLivraison> bonsLivraison = factureService.getBonsLivraisonByClient(selectedClient);
                bonModel.addElement(null); // Allow empty selection
                for (BonLivraison bon : bonsLivraison) {
                    bonModel.addElement(bon);
                }
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors du chargement des bons de livraison: " + e.getMessage());
            }
        }
        
        bonLivraisonComboBox.setModel(bonModel);
        updatePreview(null);
    }
    
    private void updatePreview(BonLivraison selectedBon) {
        if (selectedBon != null) {
            previewTableModel.setBonLivraison(selectedBon);
            createButton.setEnabled(true);
        } else {
            previewTableModel.setBonLivraison(null);
            createButton.setEnabled(false);
        }
    }
    
    private void createFacture() {
        BonLivraison selectedBon = (BonLivraison) bonLivraisonComboBox.getSelectedItem();
        
        if (selectedBon == null) {
            UIUtils.showErrorMessage(this, "Veuillez sélectionner un bon de livraison.");
            return;
        }
        
        try {
            createdFacture = factureService.createFromBonLivraison(selectedBon);
            
            // Save the facture
            createdFacture = factureService.save(createdFacture);
            
            UIUtils.showSuccessMessage(this, "Facture créée avec succès: " + createdFacture.getNumero());
            dispose();
            
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de la création de la facture: " + e.getMessage());
        }
    }
    
    private void configureDialog() {
        setSize(700, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public Facture getCreatedFacture() {
        return createdFacture;
    }
    
    // Table model for preview
    private static class PreviewTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Article", "Qté Demandée", "Qté Livrée", "Prix Unit.", "Total"};
        private BonLivraison bonLivraison;
        
        public void setBonLivraison(BonLivraison bonLivraison) {
            this.bonLivraison = bonLivraison;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return bonLivraison != null ? bonLivraison.getLignes().size() : 0;
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
            if (bonLivraison == null || rowIndex >= bonLivraison.getLignes().size()) {
                return "";
            }
            
            LigneLivraison ligne = bonLivraison.getLignes().get(rowIndex);
            switch (columnIndex) {
                case 0: return ligne.getArticle() != null ? ligne.getArticle().getDesignation() : "N/A";
                case 1: return ligne.getQuantiteDemandee() != null ? ligne.getQuantiteDemandee().toString() : "0";
                case 2: return ligne.getQuantiteLivree() != null ? ligne.getQuantiteLivree().toString() : "0";
                case 3: {
                    if (ligne.getArticle() != null && ligne.getArticle().getPrixVente() != null) {
                        return String.format("%.2f €", ligne.getArticle().getPrixVente());
                    }
                    return "0.00 €";
                }
                case 4: {
                    if (ligne.getQuantiteLivree() != null && ligne.getArticle() != null && 
                        ligne.getArticle().getPrixVente() != null) {
                        return String.format("%.2f €", 
                            ligne.getQuantiteLivree().multiply(ligne.getArticle().getPrixVente()));
                    }
                    return "0.00 €";
                }
                default: return "";
            }
        }
    }
}
