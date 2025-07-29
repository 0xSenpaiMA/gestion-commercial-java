package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.CompanyInfo;
import com.gestioncommerciale.service.CompanyInfoService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Company information management form
 */
public class CompanyInfoManagementFrame extends JFrame {
    private JTable companyTable;
    private CompanyTableModel tableModel;
    private final CompanyInfoService companyInfoService;
    private JButton addButton, editButton, deleteButton, refreshButton;
    
    public CompanyInfoManagementFrame() {
        this.companyInfoService = new CompanyInfoService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadCompanyInfo();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Create table
        tableModel = new CompanyTableModel();
        companyTable = new JTable(tableModel);
        companyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        companyTable.setRowHeight(25);
        
        // Configure table columns
        companyTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        companyTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Nom
        companyTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Téléphone
        companyTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Email
        companyTable.getColumnModel().getColumn(4).setPreferredWidth(300); // Adresse
        
        // Create buttons
        addButton = UIUtils.createStyledButton("Ajouter", UIUtils.SUCCESS_COLOR);
        editButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        refreshButton = UIUtils.createStyledButton("Actualiser", UIUtils.SECONDARY_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Gestion des Informations de l'Entreprise");
        titleLabel.setFont(UIUtils.TITLE_FONT);
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(companyTable);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(e -> showAddCompanyDialog());
        editButton.addActionListener(e -> showEditCompanyDialog());
        deleteButton.addActionListener(e -> deleteSelectedCompany());
        refreshButton.addActionListener(e -> loadCompanyInfo());
        
        // Enable/disable buttons based on selection
        companyTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = companyTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
        
        // Initially disable edit/delete buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void configureWindow() {
        setTitle("Gestion des Informations de l'Entreprise - Gestion Commerciale");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 500);
        UIUtils.centerWindow(this);
    }
    
    private void loadCompanyInfo() {
        try {
            List<CompanyInfo> companies = companyInfoService.getAllCompanies();
            tableModel.setCompanies(companies);
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors du chargement des informations: " + e.getMessage());
        }
    }
    
    private void showAddCompanyDialog() {
        CompanyFormDialog dialog = new CompanyFormDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            CompanyInfo newCompany = dialog.getCompanyInfo();
            try {
                companyInfoService.save(newCompany);
                loadCompanyInfo();
                UIUtils.showSuccessMessage(this, "Informations de l'entreprise ajoutées avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }
    
    private void showEditCompanyDialog() {
        int selectedRow = companyTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        CompanyInfo selectedCompany = tableModel.getCompanyAt(selectedRow);
        CompanyFormDialog dialog = new CompanyFormDialog(this, selectedCompany);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            CompanyInfo updatedCompany = dialog.getCompanyInfo();
            try {
                companyInfoService.save(updatedCompany);
                loadCompanyInfo();
                UIUtils.showSuccessMessage(this, "Informations de l'entreprise modifiées avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }
    
    private void deleteSelectedCompany() {
        int selectedRow = companyTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        CompanyInfo selectedCompany = tableModel.getCompanyAt(selectedRow);
        
        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer l'entreprise '" + selectedCompany.getRaisonSociale() + "' ?",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                companyInfoService.delete(selectedCompany.getId());
                loadCompanyInfo();
                UIUtils.showSuccessMessage(this, "Informations de l'entreprise supprimées avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }
    
    /**
     * Table model for company information
     */
    private static class CompanyTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Raison Sociale", "Téléphone", "Email", "Adresse"
        };
        private List<CompanyInfo> companies;
        
        public void setCompanies(List<CompanyInfo> companies) {
            this.companies = companies;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return companies != null ? companies.size() : 0;
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
            if (companies == null || rowIndex >= companies.size()) {
                return null;
            }
            
            CompanyInfo company = companies.get(rowIndex);
            switch (columnIndex) {
                case 0: return company.getId();
                case 1: return company.getRaisonSociale();
                case 2: return company.getTelephone();
                case 3: return company.getEmail();
                case 4: return company.getAdresse();
                default: return null;
            }
        }
        
        public CompanyInfo getCompanyAt(int rowIndex) {
            return companies.get(rowIndex);
        }
    }
    
    /**
     * Dialog for adding/editing company information
     */
    private static class CompanyFormDialog extends JDialog {
        private JTextField raisonSocialeField, telephoneField, emailField, faxField;
        private JTextField adresseField, villeField, codePostalField, paysField;
        private JTextField cnssField, rcField, ifNumberField, iceField;
        private JTextField capitalField, formeJuridiqueField, siteWebField;
        private boolean confirmed = false;
        private CompanyInfo companyInfo;
        
        public CompanyFormDialog(JFrame parent, CompanyInfo companyInfo) {
            super(parent, companyInfo == null ? "Ajouter une entreprise" : "Modifier l'entreprise", true);
            this.companyInfo = companyInfo;
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            if (companyInfo != null) {
                populateFields();
            }
            pack();
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
        
        private void initializeComponents() {
            raisonSocialeField = new JTextField(20);
            telephoneField = new JTextField(20);
            emailField = new JTextField(20);
            faxField = new JTextField(20);
            adresseField = new JTextField(20);
            villeField = new JTextField(20);
            codePostalField = new JTextField(20);
            paysField = new JTextField(20);
            cnssField = new JTextField(20);
            rcField = new JTextField(20);
            ifNumberField = new JTextField(20);
            iceField = new JTextField(20);
            capitalField = new JTextField(20);
            formeJuridiqueField = new JTextField(20);
            siteWebField = new JTextField(20);
            
            // Set default values
            paysField.setText("Maroc");
            formeJuridiqueField.setText("SARL");
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Informations de base
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
            JLabel sectionLabel1 = new JLabel("Informations de base");
            sectionLabel1.setFont(UIUtils.HEADER_FONT);
            formPanel.add(sectionLabel1, gbc);
            gbc.gridwidth = 1;
            
            // Row 1: Raison sociale et Forme juridique
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Raison sociale *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(raisonSocialeField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Forme juridique:"), gbc);
            gbc.gridx = 3;
            formPanel.add(formeJuridiqueField, gbc);
            
            // Row 2: Capital et Site web
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Capital:"), gbc);
            gbc.gridx = 1;
            formPanel.add(capitalField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Site web:"), gbc);
            gbc.gridx = 3;
            formPanel.add(siteWebField, gbc);
            
            // Informations de contact
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
            JLabel sectionLabel2 = new JLabel("Informations de contact");
            sectionLabel2.setFont(UIUtils.HEADER_FONT);
            formPanel.add(sectionLabel2, gbc);
            gbc.gridwidth = 1;
            
            // Row 4: Téléphone et Fax
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Téléphone:"), gbc);
            gbc.gridx = 1;
            formPanel.add(telephoneField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Fax:"), gbc);
            gbc.gridx = 3;
            formPanel.add(faxField, gbc);
            
            // Row 5: Email
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(emailField, gbc);
            gbc.gridwidth = 1;
            
            // Adresse
            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 4;
            JLabel sectionLabel3 = new JLabel("Adresse");
            sectionLabel3.setFont(UIUtils.HEADER_FONT);
            formPanel.add(sectionLabel3, gbc);
            gbc.gridwidth = 1;
            
            // Row 7: Adresse
            gbc.gridx = 0; gbc.gridy = 7;
            formPanel.add(new JLabel("Adresse:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(adresseField, gbc);
            gbc.gridwidth = 1;
            
            // Row 8: Ville et Code postal
            gbc.gridx = 0; gbc.gridy = 8;
            formPanel.add(new JLabel("Ville:"), gbc);
            gbc.gridx = 1;
            formPanel.add(villeField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Code postal:"), gbc);
            gbc.gridx = 3;
            formPanel.add(codePostalField, gbc);
            
            // Row 9: Pays
            gbc.gridx = 0; gbc.gridy = 9;
            formPanel.add(new JLabel("Pays:"), gbc);
            gbc.gridx = 1;
            formPanel.add(paysField, gbc);
            
            // Informations légales
            gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 4;
            JLabel sectionLabel4 = new JLabel("Informations légales");
            sectionLabel4.setFont(UIUtils.HEADER_FONT);
            formPanel.add(sectionLabel4, gbc);
            gbc.gridwidth = 1;
            
            // Row 11: CNSS et RC
            gbc.gridx = 0; gbc.gridy = 11;
            formPanel.add(new JLabel("CNSS:"), gbc);
            gbc.gridx = 1;
            formPanel.add(cnssField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("RC:"), gbc);
            gbc.gridx = 3;
            formPanel.add(rcField, gbc);
            
            // Row 12: IF et ICE
            gbc.gridx = 0; gbc.gridy = 12;
            formPanel.add(new JLabel("IF:"), gbc);
            gbc.gridx = 1;
            formPanel.add(ifNumberField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("ICE:"), gbc);
            gbc.gridx = 3;
            formPanel.add(iceField, gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
            JButton cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.ERROR_COLOR);
            
            saveButton.addActionListener(e -> saveCompanyInfo());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            // Add input validation if needed
        }
        
        private void populateFields() {
            raisonSocialeField.setText(companyInfo.getRaisonSociale());
            telephoneField.setText(companyInfo.getTelephone());
            emailField.setText(companyInfo.getEmail());
            faxField.setText(companyInfo.getFax());
            adresseField.setText(companyInfo.getAdresse());
            villeField.setText(companyInfo.getVille());
            codePostalField.setText(companyInfo.getCodePostal());
            paysField.setText(companyInfo.getPays());
            cnssField.setText(companyInfo.getCnss());
            rcField.setText(companyInfo.getRc());
            ifNumberField.setText(companyInfo.getIfNumber());
            iceField.setText(companyInfo.getIce());
            capitalField.setText(companyInfo.getCapital());
            formeJuridiqueField.setText(companyInfo.getFormeJuridique());
            siteWebField.setText(companyInfo.getSiteWeb());
        }
        
        private void saveCompanyInfo() {
            // Validate required fields
            if (raisonSocialeField.getText().trim().isEmpty()) {
                UIUtils.showWarningMessage(this, "Veuillez saisir la raison sociale de l'entreprise.");
                return;
            }
            
            // Create or update company info
            if (companyInfo == null) {
                companyInfo = new CompanyInfo();
            }
            
            companyInfo.setRaisonSociale(raisonSocialeField.getText().trim());
            companyInfo.setTelephone(telephoneField.getText().trim());
            companyInfo.setEmail(emailField.getText().trim());
            companyInfo.setFax(faxField.getText().trim());
            companyInfo.setAdresse(adresseField.getText().trim());
            companyInfo.setVille(villeField.getText().trim());
            companyInfo.setCodePostal(codePostalField.getText().trim());
            companyInfo.setPays(paysField.getText().trim());
            companyInfo.setCnss(cnssField.getText().trim());
            companyInfo.setRc(rcField.getText().trim());
            companyInfo.setIfNumber(ifNumberField.getText().trim());
            companyInfo.setIce(iceField.getText().trim());
            companyInfo.setCapital(capitalField.getText().trim());
            companyInfo.setFormeJuridique(formeJuridiqueField.getText().trim());
            companyInfo.setSiteWeb(siteWebField.getText().trim());
            
            confirmed = true;
            dispose();
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public CompanyInfo getCompanyInfo() {
            return companyInfo;
        }
    }
}
