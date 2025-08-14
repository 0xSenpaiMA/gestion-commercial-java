package com.gestioncommerciale.view;

import com.gestioncommerciale.model.User;
import com.gestioncommerciale.service.AuthService;
import com.gestioncommerciale.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login window for user authentication
 */
public class LoginFrame extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    
    public LoginFrame() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureWindow();
        
        // Initialize default user if needed
        AuthService.initializeDefaultUser();
    }
    
    private void initializeComponents() {
        loginField = new JTextField(15);
        passwordField = new JPasswordField(15);
        
        // Créer des boutons avec un style personnalisé et visible
        loginButton = createLoginStyleButton("Se connecter", UIUtils.PRIMARY_COLOR);
        exitButton = createLoginStyleButton("Quitter", UIUtils.ERROR_COLOR);
        
        // Set default values for testing
        loginField.setText("admin");
        passwordField.setText("admin123");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(UIUtils.PRIMARY_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Gestion Commerciale", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Système de Gestion Commerciale Professionnel", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.WHITE);
        
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Login form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Login label and field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel loginLabel = new JLabel("Nom d'utilisateur:");
        loginLabel.setFont(UIUtils.DEFAULT_FONT);
        formPanel.add(loginLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(loginField, gbc);
        
        // Password label and field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(UIUtils.DEFAULT_FONT);
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonPanel, gbc);
        
        // Default credentials info
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel infoLabel = new JLabel("<html><center><b>Identifiants par défaut:</b><br/>" +
                "Utilisateur: admin<br/>Mot de passe: admin123</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(UIUtils.PRIMARY_COLOR);
        infoPanel.add(infoLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // Enter key to login
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        
        loginField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
    }
    
    private void configureWindow() {
        setTitle("Connexion - Gestion Commerciale");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        UIUtils.centerWindow(this);
        
        // Set focus to login field
        SwingUtilities.invokeLater(() -> loginField.requestFocus());
    }
    
    private void performLogin() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (login.isEmpty() || password.isEmpty()) {
            UIUtils.showWarningMessage(this, "Veuillez saisir votre nom d'utilisateur et mot de passe.");
            return;
        }
        
        // Show loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false);
        
        // Perform authentication in background thread
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return AuthService.authenticate(login, password);
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                loginButton.setEnabled(true);
                
                try {
                    User user = get();
                    if (user != null) {
                        // Login successful
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            new MainFrame().setVisible(true);
                        });
                    } else {
                        // Login failed
                        UIUtils.showErrorMessage(LoginFrame.this, 
                            "Nom d'utilisateur ou mot de passe incorrect.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    UIUtils.showErrorMessage(LoginFrame.this, 
                        "Erreur lors de la connexion: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Crée un bouton stylisé spécifiquement pour le LoginFrame avec une meilleure visibilité
     */
    private JButton createLoginStyleButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        
        // Configuration de l'apparence pour assurer la visibilité
        button.setBackground(backgroundColor);
        button.setOpaque(true);
        button.setBorderPainted(true);
        
        // Forcer le texte en noir pour une meilleure visibilité
        button.setForeground(Color.BLACK);
        
        // Style et police
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet de survol pour améliorer l'interaction
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
                button.setForeground(Color.BLACK); // Maintenir le texte noir même en survol
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor, 3),
                    BorderFactory.createEmptyBorder(9, 19, 9, 19)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor);
                button.setForeground(Color.BLACK); // Maintenir le texte noir
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor.darker(), 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
        });
        
        return button;
    }
}
