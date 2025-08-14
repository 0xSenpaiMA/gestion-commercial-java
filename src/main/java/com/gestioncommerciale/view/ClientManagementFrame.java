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
import javax.swing.JCheckBox;
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
import com.gestioncommerciale.service.ClientService;
import com.gestioncommerciale.service.EmailService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Client management form with CRUD operations
 */
public class ClientManagementFrame extends JFrame {
    private JTable clientTable;
    private ClientTableModel tableModel;
    private final ClientService clientService;
    private final EmailService emailService;
    private JButton addButton, editButton, deleteButton, refreshButton, emailButton;
    
    public ClientManagementFrame() {
        this.clientService = new ClientService();
        this.emailService = new EmailService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadClients();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Create table
        tableModel = new ClientTableModel();
        clientTable = new JTable(tableModel);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.setRowHeight(25);
        
        // Configure table columns
        clientTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        clientTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Nom
        clientTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Prénom
        clientTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Type
        clientTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Téléphone
        clientTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Email
        clientTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Ville
        clientTable.getColumnModel().getColumn(7).setPreferredWidth(60);  // Actif
        
        // Create buttons
        addButton = UIUtils.createStyledButton("Ajouter", UIUtils.SUCCESS_COLOR);
        editButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        refreshButton = UIUtils.createStyledButton("Actualiser", UIUtils.SECONDARY_COLOR);
        emailButton = UIUtils.createStyledButton("Envoyer situation par e-mail", UIUtils.WARNING_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Gestion des Clients/Fournisseurs");
        titleLabel.setFont(UIUtils.TITLE_FONT);
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(clientTable);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(emailButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(e -> showAddClientDialog());
        editButton.addActionListener(e -> showEditClientDialog());
        deleteButton.addActionListener(e -> deleteSelectedClient());
        refreshButton.addActionListener(e -> loadClients());
        emailButton.addActionListener(e -> envoyerSituationParEmail());
        
        // Enable/disable buttons based on selection
        clientTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = clientTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
            emailButton.setEnabled(hasSelection);
        });
        
        // Initially disable edit/delete/email buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        emailButton.setEnabled(false);
    }
    
    private void configureWindow() {
        setTitle("Gestion des Clients/Fournisseurs - Gestion Commerciale");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        UIUtils.centerWindow(this);
    }
    
    private void loadClients() {
        try {
            List<Client> clients = clientService.getAllClients();
            tableModel.setClients(clients);
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors du chargement des clients: " + e.getMessage());
        }
    }
    
    private void showAddClientDialog() {
        ClientFormDialog dialog = new ClientFormDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Client newClient = dialog.getClient();
            try {
                clientService.save(newClient);
                loadClients();
                UIUtils.showSuccessMessage(this, "Client ajouté avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de l'ajout du client: " + e.getMessage());
            }
        }
    }
    
    private void showEditClientDialog() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Client selectedClient = tableModel.getClientAt(selectedRow);
        ClientFormDialog dialog = new ClientFormDialog(this, selectedClient);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Client updatedClient = dialog.getClient();
            try {
                clientService.save(updatedClient);
                loadClients();
                UIUtils.showSuccessMessage(this, "Client modifié avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la modification du client: " + e.getMessage());
            }
        }
    }
    
    private void deleteSelectedClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Client selectedClient = tableModel.getClientAt(selectedRow);
        
        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer le client '" + selectedClient.getFullName() + "' ?",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                clientService.delete(selectedClient.getId());
                loadClients();
                UIUtils.showSuccessMessage(this, "Client supprimé avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la suppression du client: " + e.getMessage());
            }
        }
    }
    
    private void envoyerSituationParEmail() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un client pour envoyer sa situation par e-mail.",
                "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Client selectedClient = tableModel.getClientAt(selectedRow);
        
        // Dialogue de saisie de l'adresse e-mail
        EmailInputDialog emailDialog = new EmailInputDialog(this, selectedClient);
        emailDialog.setVisible(true);
        
        if (emailDialog.isConfirmed()) {
            String emailDestinataire = emailDialog.getEmailAddress();
            
            // Afficher une boîte de dialogue de progression
            JDialog progressDialog = new JDialog(this, "Envoi en cours...", true);
            JLabel progressLabel = new JLabel("Envoi de l'e-mail en cours...");
            progressLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressDialog.add(progressLabel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(this);
            
            // Envoi dans un thread séparé pour ne pas bloquer l'interface
            Thread emailThread = new Thread(() -> {
                try {
                    emailService.envoyerSituationClient(selectedClient, emailDestinataire);
                    
                    // Fermer le dialogue de progression et afficher le succès
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(this,
                            "Situation du client envoyée avec succès à : " + emailDestinataire,
                            "Envoi réussi", JOptionPane.INFORMATION_MESSAGE);
                    });
                    
                } catch (Exception e) {
                    // Fermer le dialogue de progression et afficher l'erreur
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(this,
                            "Erreur lors de l'envoi de l'e-mail:\n" + e.getMessage(),
                            "Erreur d'envoi", JOptionPane.ERROR_MESSAGE);
                    });
                }
            });
            
            emailThread.start();
            progressDialog.setVisible(true);
        }
    }
    
    // Dialogue pour la saisie de l'adresse e-mail
    private static class EmailInputDialog extends JDialog {
        private static final long serialVersionUID = 1L;
        
        private final Client client;
        private JTextField emailField;
        private boolean confirmed = false;
        
        public EmailInputDialog(JFrame parent, Client client) {
            super(parent, "Envoyer situation par e-mail", true);
            this.client = client;
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            emailField = new JTextField(30);
            // Pré-remplir avec l'e-mail du client s'il existe
            if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
                emailField.setText(client.getEmail());
            }
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Titre
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            JLabel titleLabel = new JLabel("Envoyer la situation du client :");
            titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
            mainPanel.add(titleLabel, gbc);
            
            // Nom du client
            gbc.gridy = 1;
            String clientName = client.getNom();
            if (client.getPrenom() != null && !client.getPrenom().trim().isEmpty()) {
                clientName += " " + client.getPrenom();
            }
            JLabel clientLabel = new JLabel(clientName);
            clientLabel.setFont(clientLabel.getFont().deriveFont(java.awt.Font.BOLD));
            mainPanel.add(clientLabel, gbc);
            
            // Adresse e-mail
            gbc.gridy = 2; gbc.gridwidth = 1;
            mainPanel.add(new JLabel("Adresse e-mail :"), gbc);
            
            gbc.gridx = 1;
            mainPanel.add(emailField, gbc);
            
            add(mainPanel, BorderLayout.CENTER);
            
            // Boutons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton sendButton = new JButton("Envoyer");
            JButton cancelButton = new JButton("Annuler");
            
            sendButton.addActionListener(e -> confirmer());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(sendButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            emailField.addActionListener(e -> confirmer());
        }
        
        private void confirmer() {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Veuillez saisir une adresse e-mail.",
                    "Champ requis", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validation basique de l'e-mail
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this,
                    "Veuillez saisir une adresse e-mail valide.",
                    "Format invalide", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            confirmed = true;
            dispose();
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public String getEmailAddress() {
            return emailField.getText().trim();
        }
    }

    /**
     * Table model for clients
     */
    private static class ClientTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Nom", "Prénom", "Type", "Téléphone", "Email", "Ville", "Actif"
        };
        private List<Client> clients;
        
        public void setClients(List<Client> clients) {
            this.clients = clients;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return clients != null ? clients.size() : 0;
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
            if (clients == null || rowIndex >= clients.size()) {
                return null;
            }
            
            Client client = clients.get(rowIndex);
            switch (columnIndex) {
                case 0: return client.getId();
                case 1: return client.getNom();
                case 2: return client.getPrenom();
                case 3: return client.getType();
                case 4: return client.getTelephone();
                case 5: return client.getEmail();
                case 6: return client.getVille();
                case 7: return client.isActive() ? "Oui" : "Non";
                default: return null;
            }
        }
        
        public Client getClientAt(int rowIndex) {
            return clients.get(rowIndex);
        }
    }
    
    /**
     * Dialog for adding/editing clients
     */
    private static class ClientFormDialog extends JDialog {
        private JTextField nomField, prenomField, adresseField, villeField, paysField;
        private JTextField telephoneField, faxField, emailField, iceField, rcField;
        private JTextField codePostalField, creditLimitField;
        private JComboBox<Client.ClientType> typeComboBox;
        private JCheckBox activeCheckBox;
        private JTextArea notesArea;
        private boolean confirmed = false;
        private Client client;
        
        public ClientFormDialog(JFrame parent, Client client) {
            super(parent, client == null ? "Ajouter un client" : "Modifier le client", true);
            this.client = client;
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            if (client != null) {
                populateFields();
            }
            pack();
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            nomField = new JTextField(20);
            prenomField = new JTextField(20);
            adresseField = new JTextField(20);
            villeField = new JTextField(20);
            paysField = new JTextField(20);
            telephoneField = new JTextField(20);
            faxField = new JTextField(20);
            emailField = new JTextField(20);
            iceField = new JTextField(20);
            rcField = new JTextField(20);
            codePostalField = new JTextField(20);
            creditLimitField = new JTextField(20);
            typeComboBox = new JComboBox<>(Client.ClientType.values());
            activeCheckBox = new JCheckBox("Actif", true);
            notesArea = new JTextArea(3, 20);
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Row 0: Nom et Prénom
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Nom *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(nomField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Prénom:"), gbc);
            gbc.gridx = 3;
            formPanel.add(prenomField, gbc);
            
            // Row 1: Type
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Type *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(typeComboBox, gbc);
            
            // Row 2: Adresse
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Adresse:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(adresseField, gbc);
            gbc.gridwidth = 1;
            
            // Row 3: Ville et Code postal
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Ville:"), gbc);
            gbc.gridx = 1;
            formPanel.add(villeField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Code postal:"), gbc);
            gbc.gridx = 3;
            formPanel.add(codePostalField, gbc);
            
            // Row 4: Pays
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Pays:"), gbc);
            gbc.gridx = 1;
            formPanel.add(paysField, gbc);
            
            // Row 5: Téléphone et Fax
            gbc.gridx = 0; gbc.gridy = 5;
            formPanel.add(new JLabel("Téléphone:"), gbc);
            gbc.gridx = 1;
            formPanel.add(telephoneField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("Fax:"), gbc);
            gbc.gridx = 3;
            formPanel.add(faxField, gbc);
            
            // Row 6: Email
            gbc.gridx = 0; gbc.gridy = 6;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(emailField, gbc);
            gbc.gridwidth = 1;
            
            // Row 7: ICE et RC
            gbc.gridx = 0; gbc.gridy = 7;
            formPanel.add(new JLabel("ICE:"), gbc);
            gbc.gridx = 1;
            formPanel.add(iceField, gbc);
            gbc.gridx = 2;
            formPanel.add(new JLabel("RC:"), gbc);
            gbc.gridx = 3;
            formPanel.add(rcField, gbc);
            
            // Row 8: Limite de crédit
            gbc.gridx = 0; gbc.gridy = 8;
            formPanel.add(new JLabel("Limite de crédit:"), gbc);
            gbc.gridx = 1;
            formPanel.add(creditLimitField, gbc);
            
            // Row 9: Actif
            gbc.gridx = 0; gbc.gridy = 9;
            gbc.gridwidth = 2;
            formPanel.add(activeCheckBox, gbc);
            gbc.gridwidth = 1;
            
            // Row 10: Notes
            gbc.gridx = 0; gbc.gridy = 10;
            formPanel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(new JScrollPane(notesArea), gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
            JButton cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.ERROR_COLOR);
            
            saveButton.addActionListener(e -> saveClient());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            // Add input validation if needed
        }
        
        private void populateFields() {
            nomField.setText(client.getNom());
            prenomField.setText(client.getPrenom());
            adresseField.setText(client.getAdresse());
            villeField.setText(client.getVille());
            paysField.setText(client.getPays());
            telephoneField.setText(client.getTelephone());
            faxField.setText(client.getFax());
            emailField.setText(client.getEmail());
            iceField.setText(client.getIce());
            rcField.setText(client.getRc());
            codePostalField.setText(client.getCodePostal());
            if (client.getCreditLimit() != null) {
                creditLimitField.setText(client.getCreditLimit().toString());
            }
            typeComboBox.setSelectedItem(client.getType());
            activeCheckBox.setSelected(client.isActive());
            notesArea.setText(client.getNotes());
        }
        
        private void saveClient() {
            // Validate required fields
            if (nomField.getText().trim().isEmpty()) {
                UIUtils.showWarningMessage(this, "Veuillez remplir le champ Nom.");
                return;
            }
            
            // Create or update client
            if (client == null) {
                client = new Client();
            }
            
            client.setNom(nomField.getText().trim());
            client.setPrenom(prenomField.getText().trim());
            client.setType((Client.ClientType) typeComboBox.getSelectedItem());
            client.setAdresse(adresseField.getText().trim());
            client.setVille(villeField.getText().trim());
            client.setPays(paysField.getText().trim());
            client.setTelephone(telephoneField.getText().trim());
            client.setFax(faxField.getText().trim());
            client.setEmail(emailField.getText().trim());
            client.setIce(iceField.getText().trim());
            client.setRc(rcField.getText().trim());
            client.setCodePostal(codePostalField.getText().trim());
            client.setActive(activeCheckBox.isSelected());
            client.setNotes(notesArea.getText().trim());
            
            // Parse credit limit
            if (!creditLimitField.getText().trim().isEmpty()) {
                try {
                    client.setCreditLimit(Double.parseDouble(creditLimitField.getText().trim()));
                } catch (NumberFormatException e) {
                    UIUtils.showWarningMessage(this, "Format de limite de crédit invalide.");
                    return;
                }
            }
            
            confirmed = true;
            dispose();
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public Client getClient() {
            return client;
        }
    }
}
