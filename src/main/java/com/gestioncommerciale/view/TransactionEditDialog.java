package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;

import com.gestioncommerciale.model.CompteBancaire;
import com.gestioncommerciale.model.TransactionBancaire;
import com.gestioncommerciale.service.CompteBancaireService;
import com.gestioncommerciale.service.TransactionBancaireService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialogue pour créer ou éditer une transaction bancaire
 */
public class TransactionEditDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private final TransactionBancaireService transactionService;
    private final CompteBancaireService compteService;
    private TransactionBancaire transaction;
    private boolean confirmed = false;
    
    // Composants du formulaire
    private JTextField referenceField;
    private JComboBox<CompteBancaire> compteComboBox;
    private JComboBox<TransactionBancaire.TypeTransaction> typeComboBox;
    private JComboBox<TransactionBancaire.ModePaiement> modeComboBox;
    private JSpinner montantSpinner;
    private JTextField beneficiaireField;
    private JTextField motifField;
    private JSpinner dateTransactionSpinner;
    private JSpinner dateEcheanceSpinner;
    private JTextArea notesArea;
    
    private JButton saveButton;
    private JButton cancelButton;
    
    public TransactionEditDialog(JFrame parent, TransactionBancaire transaction) {
        super(parent, transaction == null ? "Nouvelle Transaction" : "Modifier Transaction", true);
        this.transactionService = new TransactionBancaireService();
        this.compteService = new CompteBancaireService();
        this.transaction = transaction;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadComptes();
        populateFields();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Champs de base
        referenceField = new JTextField(20);
        beneficiaireField = new JTextField(20);
        motifField = new JTextField(20);
        
        // ComboBoxes
        compteComboBox = new JComboBox<>();
        typeComboBox = new JComboBox<>();
        typeComboBox.setModel(new DefaultComboBoxModel<>(TransactionBancaire.TypeTransaction.values()));
        modeComboBox = new JComboBox<>();
        modeComboBox.setModel(new DefaultComboBoxModel<>(TransactionBancaire.ModePaiement.values()));
        
        // Spinner pour le montant
        montantSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999.99, 10.0));
        JSpinner.NumberEditor montantEditor = new JSpinner.NumberEditor(montantSpinner, "#,##0.00");
        montantSpinner.setEditor(montantEditor);
        
        // Date spinners
        dateTransactionSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateTransactionEditor = new JSpinner.DateEditor(dateTransactionSpinner, "dd/MM/yyyy");
        dateTransactionSpinner.setEditor(dateTransactionEditor);
        dateTransactionSpinner.setValue(new Date());
        
        dateEcheanceSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEcheanceEditor = new JSpinner.DateEditor(dateEcheanceSpinner, "dd/MM/yyyy");
        dateEcheanceSpinner.setEditor(dateEcheanceEditor);
        
        // Notes
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
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
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Référence
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Référence *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(referenceField, gbc);
        
        // Compte
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Compte *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(compteComboBox, gbc);
        
        // Type de transaction
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Type *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(typeComboBox, gbc);
        
        // Mode de paiement
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Mode de paiement *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(modeComboBox, gbc);
        
        // Montant
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Montant (DH) *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(montantSpinner, gbc);
        
        // Bénéficiaire
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Bénéficiaire:"), gbc);
        gbc.gridx = 1;
        formPanel.add(beneficiaireField, gbc);
        
        // Motif
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Motif:"), gbc);
        gbc.gridx = 1;
        formPanel.add(motifField, gbc);
        
        // Date de transaction
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Date transaction *:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateTransactionSpinner, gbc);
        
        // Date d'échéance
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Date échéance:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateEcheanceSpinner, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(notesArea, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(e -> saveTransaction());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadComptes() {
        try {
            List<CompteBancaire> comptes = compteService.getAllComptes();
            comptes.removeIf(compte -> !compte.isActive());
            
            DefaultComboBoxModel<CompteBancaire> model = new DefaultComboBoxModel<>();
            for (CompteBancaire compte : comptes) {
                model.addElement(compte);
            }
            compteComboBox.setModel(model);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des comptes: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void populateFields() {
        if (transaction != null) {
            referenceField.setText(transaction.getReference());
            compteComboBox.setSelectedItem(transaction.getCompte());
            typeComboBox.setSelectedItem(transaction.getType());
            modeComboBox.setSelectedItem(transaction.getModePaiement());
            montantSpinner.setValue(transaction.getMontant().doubleValue());
            beneficiaireField.setText(transaction.getBeneficiaire());
            motifField.setText(transaction.getMotif());
            notesArea.setText(transaction.getNotes());
            
            if (transaction.getDateTransaction() != null) {
                dateTransactionSpinner.setValue(Date.from(transaction.getDateTransaction().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            
            if (transaction.getDateEcheance() != null) {
                dateEcheanceSpinner.setValue(Date.from(transaction.getDateEcheance().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            
            // La référence ne peut pas être modifiée
            referenceField.setEnabled(false);
        } else {
            // Générer une référence automatique pour une nouvelle transaction
            referenceField.setText(generateReference());
        }
    }
    
    private String generateReference() {
        return "TXN" + System.currentTimeMillis();
    }
    
    private void saveTransaction() {
        try {
            // Validation
            if (referenceField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "La référence est obligatoire.",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                referenceField.requestFocus();
                return;
            }
            
            if (compteComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un compte.",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                compteComboBox.requestFocus();
                return;
            }
            
            if (dateTransactionSpinner.getValue() == null) {
                JOptionPane.showMessageDialog(this,
                    "La date de transaction est obligatoire.",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                dateTransactionSpinner.requestFocus();
                return;
            }
            
            // Création ou modification de la transaction
            if (transaction == null) {
                transaction = new TransactionBancaire();
            }
            
            transaction.setReference(referenceField.getText().trim());
            transaction.setCompte((CompteBancaire) compteComboBox.getSelectedItem());
            transaction.setType((TransactionBancaire.TypeTransaction) typeComboBox.getSelectedItem());
            transaction.setModePaiement((TransactionBancaire.ModePaiement) modeComboBox.getSelectedItem());
            transaction.setMontant(BigDecimal.valueOf((Double) montantSpinner.getValue()));
            transaction.setBeneficiaire(beneficiaireField.getText().trim());
            transaction.setMotif(motifField.getText().trim());
            transaction.setNotes(notesArea.getText().trim());
            
            // Dates
            Date dateTransaction = (Date) dateTransactionSpinner.getValue();
            transaction.setDateTransaction(dateTransaction.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            
            Date dateEcheance = (Date) dateEcheanceSpinner.getValue();
            if (dateEcheance != null) {
                transaction.setDateEcheance(dateEcheance.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            
            // Sauvegarde
            if (transaction.getId() == null) {
                transactionService.ajouterTransaction(transaction);
                JOptionPane.showMessageDialog(this,
                    "Transaction créée avec succès.",
                    "Création réussie", JOptionPane.INFORMATION_MESSAGE);
            } else {
                transactionService.updateTransaction(transaction);
                JOptionPane.showMessageDialog(this,
                    "Transaction modifiée avec succès.",
                    "Modification réussie", JOptionPane.INFORMATION_MESSAGE);
            }
            
            confirmed = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'enregistrement: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureDialog() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public TransactionBancaire getTransaction() {
        return transaction;
    }
}
