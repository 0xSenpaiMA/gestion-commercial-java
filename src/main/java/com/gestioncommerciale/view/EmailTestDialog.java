package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.gestioncommerciale.service.EmailService;

/**
 * Dialogue de test de la configuration e-mail
 */
public class EmailTestDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private final EmailService emailService;
    private JTextField emailField;
    private JButton testButton, closeButton;
    
    public EmailTestDialog(JFrame parent) {
        super(parent, "Test Configuration E-mail", true);
        this.emailService = new EmailService();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        emailField = new JTextField(30);
        testButton = new JButton("Tester");
        closeButton = new JButton("Fermer");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Titre
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Test de la configuration e-mail");
        titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        mainPanel.add(titleLabel, gbc);
        
        // Instructions
        gbc.gridy = 1;
        JLabel instructionLabel = new JLabel("Saisissez une adresse e-mail pour recevoir un e-mail de test :");
        mainPanel.add(instructionLabel, gbc);
        
        // Champ e-mail
        gbc.gridy = 2; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Adresse e-mail :"), gbc);
        
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(testButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        testButton.addActionListener(e -> testerConfiguration());
        closeButton.addActionListener(e -> dispose());
        emailField.addActionListener(e -> testerConfiguration());
    }
    
    private void testerConfiguration() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez saisir une adresse e-mail.",
                "Champ requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validation basique
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                "Veuillez saisir une adresse e-mail valide.",
                "Format invalide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Désactiver le bouton pendant le test
        testButton.setEnabled(false);
        testButton.setText("Test en cours...");
        
        // Test dans un thread séparé
        Thread testThread = new Thread(() -> {
            try {
                String sujet = "Test de configuration - Gestion Commerciale";
                String contenu = "Ceci est un e-mail de test pour vérifier la configuration du service e-mail.\n\n" +
                               "Si vous recevez cet e-mail, la configuration fonctionne correctement.\n\n" +
                               "Cordialement,\n" +
                               "Système de Gestion Commerciale";
                
                emailService.envoyerEmail(email, sujet, contenu, false);
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Tester");
                    JOptionPane.showMessageDialog(this,
                        "E-mail de test envoyé avec succès à : " + email + "\n\n" +
                        "Vérifiez votre boîte de réception (et le dossier spam si nécessaire).",
                        "Test réussi", JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    testButton.setEnabled(true);
                    testButton.setText("Tester");
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'envoi de l'e-mail de test :\n" + e.getMessage() + "\n\n" +
                        "Vérifiez la configuration SMTP dans EmailService.java",
                        "Erreur de test", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
        
        testThread.start();
    }
}
