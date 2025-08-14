package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.gestioncommerciale.model.CompteBancaire;
import com.gestioncommerciale.service.CompteBancaireService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialogue pour créer ou éditer un compte bancaire
 */
public class CompteEditDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private final CompteBancaireService service;
    private CompteBancaire compte;
    private boolean confirmed = false;
    
    // Composants du formulaire avec des tailles appropriées
    private JTextField numeroCompteField;
    private JTextField nomBanqueField;
    private JTextField nomAgenceField;
    private JTextField titulaireField;
    private JComboBox<CompteBancaire.TypeCompte> typeComboBox;
    private JSpinner soldeSpinner;
    private JTextField ibanField;
    private JTextArea notesArea;
    private JCheckBox actifCheckBox;
    
    private JButton saveButton;
    private JButton cancelButton;
    
    public CompteEditDialog(JFrame parent, CompteBancaire compte) {
        super(parent, compte == null ? "Nouveau Compte Bancaire" : "Modifier Compte Bancaire", true);
        this.service = new CompteBancaireService();
        this.compte = compte;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateFields();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Champs de texte avec des colonnes plus larges
        numeroCompteField = new JTextField(30);
        numeroCompteField.setEditable(true);
        numeroCompteField.setEnabled(true);
        
        nomBanqueField = new JTextField(30);
        nomBanqueField.setEditable(true);
        nomBanqueField.setEnabled(true);
        
        nomAgenceField = new JTextField(30);
        nomAgenceField.setEditable(true);
        nomAgenceField.setEnabled(true);
        
        titulaireField = new JTextField(30);
        titulaireField.setEditable(true);
        titulaireField.setEnabled(true);
        
        ibanField = new JTextField(30);
        ibanField.setEditable(true);
        ibanField.setEnabled(true);
        
        // Zone de texte pour les notes
        notesArea = new JTextArea(3, 30);
        notesArea.setEditable(true);
        notesArea.setEnabled(true);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        // ComboBox pour le type de compte
        typeComboBox = new JComboBox<>();
        typeComboBox.setModel(new DefaultComboBoxModel<>(CompteBancaire.TypeCompte.values()));
        typeComboBox.setEnabled(true);
        
        // Spinner pour le solde avec format numérique
        soldeSpinner = new JSpinner(new SpinnerNumberModel(0.0, -999999999.99, 999999999.99, 100.0));
        JSpinner.NumberEditor soldeEditor = new JSpinner.NumberEditor(soldeSpinner, "#,##0.00");
        soldeSpinner.setEditor(soldeEditor);
        soldeSpinner.setEnabled(true);
        
        // Checkbox
        actifCheckBox = new JCheckBox("Compte actif", true);
        actifCheckBox.setEnabled(true);
        
        // Boutons
        saveButton = UIUtils.createStyledButton("Enregistrer", UIUtils.SUCCESS_COLOR);
        cancelButton = UIUtils.createStyledButton("Annuler", UIUtils.SECONDARY_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panneau principal avec le formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Numéro de compte
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Numéro de compte *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(numeroCompteField, gbc);
        
        // Nom de la banque
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Nom de la banque *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(nomBanqueField, gbc);
        
        // Nom de l'agence
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Nom de l'agence:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(nomAgenceField, gbc);
        
        // Titulaire
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Titulaire *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(titulaireField, gbc);
        
        // Type de compte
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Type de compte *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(typeComboBox, gbc);
        
        // Solde actuel
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Solde actuel (DH):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(soldeSpinner, gbc);
        
        // IBAN
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("IBAN:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(ibanField, gbc);
        
        // Notes
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        formPanel.add(notesScrollPane, gbc);
        
        // Actif
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(actifCheckBox, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCompte();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void populateFields() {
        if (compte != null) {
            // Mode édition - remplir les champs avec les données existantes
            numeroCompteField.setText(compte.getNumeroCompte() != null ? compte.getNumeroCompte() : "");
            nomBanqueField.setText(compte.getNomBanque() != null ? compte.getNomBanque() : "");
            nomAgenceField.setText(compte.getNomAgence() != null ? compte.getNomAgence() : "");
            titulaireField.setText(compte.getTitulaire() != null ? compte.getTitulaire() : "");
            typeComboBox.setSelectedItem(compte.getTypeCompte());
            soldeSpinner.setValue(compte.getSoldeActuel().doubleValue());
            ibanField.setText(compte.getIban() != null ? compte.getIban() : "");
            notesArea.setText(compte.getNotes() != null ? compte.getNotes() : "");
            actifCheckBox.setSelected(compte.isActive());
            
            // Le numéro de compte ne peut pas être modifié en mode édition
            numeroCompteField.setEnabled(false);
        } else {
            // Mode création - tous les champs sont vides et éditables
            numeroCompteField.setText("");
            nomBanqueField.setText("");
            nomAgenceField.setText("");
            titulaireField.setText("");
            typeComboBox.setSelectedIndex(0);
            soldeSpinner.setValue(0.0);
            ibanField.setText("");
            notesArea.setText("");
            actifCheckBox.setSelected(true);
            
            // Tous les champs sont éditables en mode création
            numeroCompteField.setEnabled(true);
        }
        
        // S'assurer que tous les champs sont bien éditables
        nomBanqueField.setEnabled(true);
        nomAgenceField.setEnabled(true);
        titulaireField.setEnabled(true);
        typeComboBox.setEnabled(true);
        soldeSpinner.setEnabled(true);
        ibanField.setEnabled(true);
        notesArea.setEnabled(true);
        actifCheckBox.setEnabled(true);
    }
    
    private void saveCompte() {
        try {
            // Validation des champs obligatoires
            String numeroCompte = numeroCompteField.getText().trim();
            if (numeroCompte.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Le numéro de compte est obligatoire.",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                numeroCompteField.requestFocus();
                return;
            }
            
            String nomBanque = nomBanqueField.getText().trim();
            if (nomBanque.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Le nom de la banque est obligatoire.",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                nomBanqueField.requestFocus();
                return;
            }
            
            String titulaire = titulaireField.getText().trim();
            if (titulaire.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Le titulaire est obligatoire.",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                titulaireField.requestFocus();
                return;
            }
            
            // Création ou modification du compte
            if (compte == null) {
                compte = new CompteBancaire();
            }
            
            // Affectation des valeurs du formulaire au modèle
            compte.setNumeroCompte(numeroCompte);
            compte.setNomBanque(nomBanque);
            compte.setNomAgence(nomAgenceField.getText().trim());
            compte.setTitulaire(titulaire);
            compte.setTypeCompte((CompteBancaire.TypeCompte) typeComboBox.getSelectedItem());
            compte.setSoldeActuel(BigDecimal.valueOf((Double) soldeSpinner.getValue()));
            compte.setIban(ibanField.getText().trim());
            compte.setNotes(notesArea.getText().trim());
            compte.setActif(actifCheckBox.isSelected());
            
            // Sauvegarde dans la base de données
            if (compte.getId() == null) {
                // Nouveau compte
                service.save(compte);
                JOptionPane.showMessageDialog(this,
                    "Compte créé avec succès.",
                    "Création réussie", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Modification d'un compte existant
                compte = service.save(compte);
                JOptionPane.showMessageDialog(this,
                    "Compte modifié avec succès.",
                    "Modification réussie", JOptionPane.INFORMATION_MESSAGE);
            }
            
            confirmed = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'enregistrement: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Pour le debug
        }
    }
    
    private void configureDialog() {
        setSize(600, 550);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        
        // S'assurer que la fenêtre est focusable et modale
        setFocusable(true);
        setModal(true);
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public CompteBancaire getCompte() {
        return compte;
    }
}
