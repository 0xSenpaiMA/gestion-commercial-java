package com.gestioncommerciale.view;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.*;

import com.gestioncommerciale.model.Exercice;
import com.gestioncommerciale.service.ExerciceService;

/**
 * Dialog for closing current exercise and creating a new one
 */
public class CloturerEtCreerDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private ExerciceService exerciceService;
    private boolean confirmed = false;
    
    // Form fields
    private JLabel exerciceActuelLabel;
    private JTextField nouveauLibelleField;
    private JTextField nouvelleDateDebutField;
    private JTextField nouvelleDateFinField;
    private JTextArea nouvelleDescriptionArea;
    
    private JButton saveButton, cancelButton;
    
    public CloturerEtCreerDialog(Window parent) {
        super(parent, "Clôturer et Créer Nouvel Exercice", ModalityType.APPLICATION_MODAL);
        this.exerciceService = new ExerciceService();
        
        initializeComponents();
        layoutComponents();
        bindEvents();
        loadCurrentExercise();
        setupDefaultValues();
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        exerciceActuelLabel = new JLabel();
        exerciceActuelLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        nouveauLibelleField = new JTextField(30);
        
        nouvelleDateDebutField = new JTextField(15);
        nouvelleDateDebutField.setToolTipText("Format: dd/MM/yyyy");
        
        nouvelleDateFinField = new JTextField(15);
        nouvelleDateFinField.setToolTipText("Format: dd/MM/yyyy");
        
        nouvelleDescriptionArea = new JTextArea(3, 30);
        nouvelleDescriptionArea.setLineWrap(true);
        nouvelleDescriptionArea.setWrapStyleWord(true);
        
        saveButton = new JButton("Clôturer et Créer");
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
        
        // Current exercise section
        JPanel currentSection = new JPanel(new BorderLayout());
        currentSection.setBorder(BorderFactory.createTitledBorder("Exercice à clôturer"));
        currentSection.add(exerciceActuelLabel, BorderLayout.CENTER);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(currentSection, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 15, 5);
        mainPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;
        row++;
        
        // New exercise section
        JLabel newExerciseTitle = new JLabel("Nouvel exercice");
        newExerciseTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(newExerciseTitle, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Libellé
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Libellé: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nouveauLibelleField, gbc);
        row++;
        
        // Dates panel
        JPanel datesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datesPanel.add(new JLabel("Du: "));
        datesPanel.add(nouvelleDateDebutField);
        datesPanel.add(Box.createHorizontalStrut(20));
        datesPanel.add(new JLabel("Au: "));
        datesPanel.add(nouvelleDateFinField);
        
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
        mainPanel.add(new JScrollPane(nouvelleDescriptionArea), gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void bindEvents() {
        saveButton.addActionListener(e -> cloturerEtCreer());
        cancelButton.addActionListener(e -> dispose());
        
        // Auto-generate libelle when dates change
        nouvelleDateDebutField.addActionListener(e -> generateLibelle());
        nouvelleDateFinField.addActionListener(e -> generateLibelle());
    }
    
    private void loadCurrentExercise() {
        Exercice exerciceActif = exerciceService.getExerciceActif();
        if (exerciceActif != null) {
            String info = exerciceActif.getLibelle() + "\n" +
                         "Période : " + exerciceActif.getDateDebut().format(DATE_FORMATTER) + 
                         " au " + exerciceActif.getDateFin().format(DATE_FORMATTER) + "\n" +
                         "Statut : " + exerciceActif.getStatut();
            exerciceActuelLabel.setText("<html>" + info.replace("\n", "<br>") + "</html>");
        } else {
            exerciceActuelLabel.setText("Aucun exercice actif trouvé");
            saveButton.setEnabled(false);
        }
    }
    
    private void setupDefaultValues() {
        Exercice exerciceActif = exerciceService.getExerciceActif();
        if (exerciceActif != null) {
            // Suggest next period after current exercise
            LocalDate nouveauDebut = exerciceActif.getDateFin().plusDays(1);
            LocalDate nouvelleFin = nouveauDebut.plusYears(1).minusDays(1);
            
            nouvelleDateDebutField.setText(nouveauDebut.format(DATE_FORMATTER));
            nouvelleDateFinField.setText(nouvelleFin.format(DATE_FORMATTER));
            
            generateLibelle();
        }
    }
    
    private void generateLibelle() {
        try {
            String dateDebutStr = nouvelleDateDebutField.getText().trim();
            String dateFinStr = nouvelleDateFinField.getText().trim();
            
            if (!dateDebutStr.isEmpty() && !dateFinStr.isEmpty()) {
                LocalDate dateDebut = LocalDate.parse(dateDebutStr, DATE_FORMATTER);
                LocalDate dateFin = LocalDate.parse(dateFinStr, DATE_FORMATTER);
                
                String libelleSuggere = exerciceService.genererLibelleAutomatique(dateDebut, dateFin);
                
                if (nouveauLibelleField.getText().trim().isEmpty() || 
                    nouveauLibelleField.getText().startsWith("Exercice ")) {
                    nouveauLibelleField.setText(libelleSuggere);
                }
            }
        } catch (DateTimeParseException e) {
            // Ignore invalid dates
        }
    }
    
    private void cloturerEtCreer() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir :\n\n" +
            "1. Clôturer l'exercice en cours\n" +
            "2. Créer le nouvel exercice : " + nouveauLibelleField.getText() + "\n\n" +
            "Cette action est irréversible.",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Validate form
            if (!validateForm()) {
                return;
            }
            
            // Parse data
            String libelle = nouveauLibelleField.getText().trim();
            LocalDate dateDebut = LocalDate.parse(nouvelleDateDebutField.getText().trim(), DATE_FORMATTER);
            LocalDate dateFin = LocalDate.parse(nouvelleDateFinField.getText().trim(), DATE_FORMATTER);
            String description = nouvelleDescriptionArea.getText().trim();
            
            // Close and create
            Exercice nouvelExercice = exerciceService.cloturerEtCreerNouvelExercice(dateDebut, dateFin, libelle);
            
            if (!description.isEmpty()) {
                nouvelExercice.setDescription(description);
                exerciceService.save(nouvelExercice);
            }
            
            confirmed = true;
            dispose();
            
            JOptionPane.showMessageDialog(
                getOwner(),
                "Exercice clôturé et nouvel exercice créé avec succès",
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
                "Erreur lors de l'opération :\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private boolean validateForm() {
        // Libellé required
        if (nouveauLibelleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez saisir un libellé pour le nouvel exercice",
                "Champ requis",
                JOptionPane.WARNING_MESSAGE
            );
            nouveauLibelleField.requestFocus();
            return false;
        }
        
        // Validate dates format and logic
        String dateDebutStr = nouvelleDateDebutField.getText().trim();
        String dateFinStr = nouvelleDateFinField.getText().trim();
        
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
            dateFin = LocalDate.parse(dateFinStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                this,
                "Format de date invalide. Utilisez le format dd/MM/yyyy",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
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
            return false;
        }
        
        // Check minimum duration
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
