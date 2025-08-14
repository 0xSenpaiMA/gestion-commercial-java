package com.gestioncommerciale.view;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.*;

import com.gestioncommerciale.model.Exercice;
import com.gestioncommerciale.service.ExerciceService;

/**
 * Dialog for creating a new accounting period
 */
public class NouvelExerciceDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private ExerciceService exerciceService;
    private boolean confirmed = false;
    
    // Form fields
    private JTextField libelleField;
    private JTextField dateDebutField;
    private JTextField dateFinField;
    private JTextArea descriptionArea;
    
    private JButton saveButton, cancelButton;
    
    public NouvelExerciceDialog(Window parent) {
        super(parent, "Nouvel Exercice Comptable", ModalityType.APPLICATION_MODAL);
        this.exerciceService = new ExerciceService();
        
        initializeComponents();
        layoutComponents();
        bindEvents();
        setupDefaultValues();
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        libelleField = new JTextField(30);
        
        dateDebutField = new JTextField(15);
        dateDebutField.setToolTipText("Format: dd/MM/yyyy");
        
        dateFinField = new JTextField(15);
        dateFinField.setToolTipText("Format: dd/MM/yyyy");
        
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        saveButton = new JButton("Créer");
        cancelButton = new JButton("Annuler");
        
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main panel with form
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Libellé
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Libellé: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(libelleField, gbc);
        row++;
        
        // Dates panel
        JPanel datesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datesPanel.add(new JLabel("Du: "));
        datesPanel.add(dateDebutField);
        datesPanel.add(Box.createHorizontalStrut(20));
        datesPanel.add(new JLabel("Au: "));
        datesPanel.add(dateFinField);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(datesPanel, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Description
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(descriptionArea), gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void bindEvents() {
        saveButton.addActionListener(e -> creerExercice());
        cancelButton.addActionListener(e -> dispose());
        
        // Auto-generate libelle when dates change
        dateDebutField.addActionListener(e -> generateLibelle());
        dateFinField.addActionListener(e -> generateLibelle());
    }
    
    private void setupDefaultValues() {
        // Suggest next year as default
        LocalDate today = LocalDate.now();
        LocalDate debutSuggere = LocalDate.of(today.getYear() + 1, 1, 1);
        LocalDate finSuggeree = LocalDate.of(today.getYear() + 1, 12, 31);
        
        dateDebutField.setText(debutSuggere.format(DATE_FORMATTER));
        dateFinField.setText(finSuggeree.format(DATE_FORMATTER));
        
        generateLibelle();
    }
    
    private void generateLibelle() {
        try {
            String dateDebutStr = dateDebutField.getText().trim();
            String dateFinStr = dateFinField.getText().trim();
            
            if (!dateDebutStr.isEmpty() && !dateFinStr.isEmpty()) {
                LocalDate dateDebut = LocalDate.parse(dateDebutStr, DATE_FORMATTER);
                LocalDate dateFin = LocalDate.parse(dateFinStr, DATE_FORMATTER);
                
                String libelleSuggere = exerciceService.genererLibelleAutomatique(dateDebut, dateFin);
                
                if (libelleField.getText().trim().isEmpty() || 
                    libelleField.getText().startsWith("Exercice ")) {
                    libelleField.setText(libelleSuggere);
                }
            }
        } catch (DateTimeParseException e) {
            // Ignore invalid dates
        }
    }
    
    private void creerExercice() {
        try {
            // Validate form
            if (!validateForm()) {
                return;
            }
            
            // Parse data
            String libelle = libelleField.getText().trim();
            LocalDate dateDebut = LocalDate.parse(dateDebutField.getText().trim(), DATE_FORMATTER);
            LocalDate dateFin = LocalDate.parse(dateFinField.getText().trim(), DATE_FORMATTER);
            String description = descriptionArea.getText().trim();
            
            // Create exercise
            Exercice nouvelExercice = exerciceService.creerNouvelExercice(dateDebut, dateFin, libelle);
            
            if (!description.isEmpty()) {
                nouvelExercice.setDescription(description);
                exerciceService.save(nouvelExercice);
            }
            
            confirmed = true;
            dispose();
            
            JOptionPane.showMessageDialog(
                getOwner(),
                "Nouvel exercice créé avec succès",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE
            );
            
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                this,
                "Format de date invalide. Utilisez le format dd/MM/yyyy",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors de la création de l'exercice :\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private boolean validateForm() {
        // Libellé required
        if (libelleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez saisir un libellé pour l'exercice",
                "Champ requis",
                JOptionPane.WARNING_MESSAGE
            );
            libelleField.requestFocus();
            return false;
        }
        
        // Validate dates format
        String dateDebutStr = dateDebutField.getText().trim();
        String dateFinStr = dateFinField.getText().trim();
        
        if (dateDebutStr.isEmpty() || dateFinStr.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez saisir les dates de début et de fin",
                "Champs requis",
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        
        LocalDate dateDebut;
        LocalDate dateFin;
        
        try {
            dateDebut = LocalDate.parse(dateDebutStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                this,
                "Format de date de début invalide. Utilisez le format dd/MM/yyyy",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
            dateDebutField.requestFocus();
            return false;
        }
        
        try {
            dateFin = LocalDate.parse(dateFinStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                this,
                "Format de date de fin invalide. Utilisez le format dd/MM/yyyy",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
            dateFinField.requestFocus();
            return false;
        }
        
        // Check date order
        if (dateDebut.isAfter(dateFin)) {
            JOptionPane.showMessageDialog(
                this,
                "La date de début ne peut pas être postérieure à la date de fin",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
            dateDebutField.requestFocus();
            return false;
        }
        
        // Check minimum duration (at least 1 month)
        if (dateDebut.plusDays(30).isAfter(dateFin)) {
            JOptionPane.showMessageDialog(
                this,
                "La durée de l'exercice doit être d'au moins 30 jours",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        return true;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
