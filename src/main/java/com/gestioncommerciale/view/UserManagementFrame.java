package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.User;
import com.gestioncommerciale.service.AuthService;
import com.gestioncommerciale.service.UserService;
import com.gestioncommerciale.util.UIUtils;

/**
 * User management form with CRUD operations
 */
public class UserManagementFrame extends JFrame {
    private JTable userTable;
    private UserTableModel tableModel;
    private final UserService userService;
    private JButton addButton, editButton, deleteButton, refreshButton;
    
    public UserManagementFrame() {
        this.userService = new UserService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadUsers();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Create table
        tableModel = new UserTableModel();
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        
        // Configure table columns
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Nom
        userTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Prénom
        userTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Login
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Rôle
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Email
        userTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // Actif
        userTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Dernière connexion
        
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
        JLabel titleLabel = new JLabel("Gestion des Utilisateurs");
        titleLabel.setFont(UIUtils.TITLE_FONT);
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
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
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> showEditUserDialog());
        deleteButton.addActionListener(e -> deleteSelectedUser());
        refreshButton.addActionListener(e -> loadUsers());
        
        // Enable/disable buttons based on selection
        userTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = userTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
        
        // Initially disable edit/delete buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void configureWindow() {
        setTitle("Gestion des Utilisateurs - Gestion Commerciale");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        UIUtils.centerWindow(this);
    }
    
    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            tableModel.setUsers(users);
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }
    }
    
    private void showAddUserDialog() {
        UserFormDialog dialog = new UserFormDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            User newUser = dialog.getUser();
            try {
                userService.save(newUser);
                loadUsers();
                UIUtils.showSuccessMessage(this, "Utilisateur ajouté avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            }
        }
    }
    
    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        User selectedUser = tableModel.getUserAt(selectedRow);
        UserFormDialog dialog = new UserFormDialog(this, selectedUser);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            User updatedUser = dialog.getUser();
            try {
                userService.save(updatedUser);
                loadUsers();
                UIUtils.showSuccessMessage(this, "Utilisateur modifié avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la modification de l'utilisateur: " + e.getMessage());
            }
        }
    }
    
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        User selectedUser = tableModel.getUserAt(selectedRow);
        
        // Prevent deletion of current user
        if (selectedUser.getId().equals(AuthService.getCurrentUser().getId())) {
            UIUtils.showWarningMessage(this, "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer l'utilisateur '" + selectedUser.getFullName() + "' ?",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                userService.delete(selectedUser.getId());
                loadUsers();
                UIUtils.showSuccessMessage(this, "Utilisateur supprimé avec succès.");
            } catch (Exception e) {
                UIUtils.showErrorMessage(this, "Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            }
        }
    }
    
    /**
     * Table model for users
     */
    private static class UserTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "ID", "Nom", "Prénom", "Login", "Rôle", "Email", "Actif", "Dernière connexion"
        };
        private List<User> users;
        
        public void setUsers(List<User> users) {
            this.users = users;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return users != null ? users.size() : 0;
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
            if (users == null || rowIndex >= users.size()) {
                return null;
            }
            
            User user = users.get(rowIndex);
            switch (columnIndex) {
                case 0: return user.getId();
                case 1: return user.getNom();
                case 2: return user.getPrenom();
                case 3: return user.getLogin();
                case 4: return user.getRole();
                case 5: return user.getEmail();
                case 6: return user.isActive() ? "Oui" : "Non";
                case 7: 
                    if (user.getLastLogin() != null && !user.getLastLogin().isEmpty()) {
                        try {
                            LocalDateTime lastLogin = LocalDateTime.parse(user.getLastLogin());
                            return lastLogin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        } catch (Exception e) {
                            return user.getLastLogin(); // Return as-is if parsing fails
                        }
                    } else {
                        return "Jamais";
                    }
                default: return null;
            }
        }
        
        public User getUserAt(int rowIndex) {
            return users.get(rowIndex);
        }
    }
    
    /**
     * Dialog for adding/editing users
     */
    private static class UserFormDialog extends JDialog {
        private JTextField nomField, prenomField, loginField, emailField, telephoneField;
        private JPasswordField passwordField, confirmPasswordField;
        private JComboBox<User.UserRole> roleComboBox;
        private JCheckBox activeCheckBox;
        private boolean confirmed = false;
        private User user;
        private boolean isEditMode;
        
        public UserFormDialog(JFrame parent, User user) {
            super(parent, user == null ? "Ajouter un utilisateur" : "Modifier l'utilisateur", true);
            this.user = user;
            this.isEditMode = user != null;
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            if (user != null) {
                populateFields();
            }
            pack();
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            nomField = new JTextField(20);
            prenomField = new JTextField(20);
            loginField = new JTextField(20);
            emailField = new JTextField(20);
            telephoneField = new JTextField(20);
            passwordField = new JPasswordField(20);
            confirmPasswordField = new JPasswordField(20);
            roleComboBox = new JComboBox<>(User.UserRole.values());
            activeCheckBox = new JCheckBox("Actif", true);
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
            formPanel.add(new JLabel("Prénom *:"), gbc);
            gbc.gridx = 3;
            formPanel.add(prenomField, gbc);
            
            // Row 1: Login
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Login *:"), gbc);
            gbc.gridx = 1;
            formPanel.add(loginField, gbc);
            
            // Row 2: Rôle
            gbc.gridx = 2; gbc.gridy = 1;
            formPanel.add(new JLabel("Rôle *:"), gbc);
            gbc.gridx = 3;
            formPanel.add(roleComboBox, gbc);
            
            // Row 3: Email
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(emailField, gbc);
            gbc.gridwidth = 1;
            
            // Row 4: Téléphone
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Téléphone:"), gbc);
            gbc.gridx = 1;
            formPanel.add(telephoneField, gbc);
            
            // Row 5: Actif
            gbc.gridx = 2; gbc.gridy = 3;
            gbc.gridwidth = 2;
            formPanel.add(activeCheckBox, gbc);
            gbc.gridwidth = 1;
            
            // Password fields (only for new users or if admin wants to change password)
            if (!isEditMode) {
                // Row 6: Mot de passe
                gbc.gridx = 0; gbc.gridy = 4;
                formPanel.add(new JLabel("Mot de passe *:"), gbc);
                gbc.gridx = 1;
                formPanel.add(passwordField, gbc);
                
                // Row 7: Confirmer mot de passe
                gbc.gridx = 2; gbc.gridy = 4;
                formPanel.add(new JLabel("Confirmer *:"), gbc);
                gbc.gridx = 3;
                formPanel.add(confirmPasswordField, gbc);
            }
            
            add(formPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
            JButton cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.ERROR_COLOR);
            
            saveButton.addActionListener(e -> saveUser());
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            // Add input validation if needed
        }
        
        private void populateFields() {
            nomField.setText(user.getNom());
            prenomField.setText(user.getPrenom());
            loginField.setText(user.getLogin());
            emailField.setText(user.getEmail());
            telephoneField.setText(user.getTelephone());
            roleComboBox.setSelectedItem(user.getRole());
            activeCheckBox.setSelected(user.isActive());
        }
        
        private void saveUser() {
            // Validate required fields
            if (nomField.getText().trim().isEmpty() || 
                prenomField.getText().trim().isEmpty() ||
                loginField.getText().trim().isEmpty()) {
                UIUtils.showWarningMessage(this, "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            
            // Validate password for new users
            if (!isEditMode) {
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                
                if (password.isEmpty()) {
                    UIUtils.showWarningMessage(this, "Veuillez saisir un mot de passe.");
                    return;
                }
                
                if (!password.equals(confirmPassword)) {
                    UIUtils.showWarningMessage(this, "Les mots de passe ne correspondent pas.");
                    return;
                }
            }
            
            // Create or update user
            if (user == null) {
                user = new User();
            }
            
            user.setNom(nomField.getText().trim());
            user.setPrenom(prenomField.getText().trim());
            user.setLogin(loginField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setTelephone(telephoneField.getText().trim());
            user.setRole((User.UserRole) roleComboBox.getSelectedItem());
            user.setActive(activeCheckBox.isSelected());
            
            // Set password for new users
            if (!isEditMode) {
                String password = new String(passwordField.getPassword());
                user.setPassword(AuthService.hashPassword(password));
            }
            
            confirmed = true;
            dispose();
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public User getUser() {
            return user;
        }
    }
}
