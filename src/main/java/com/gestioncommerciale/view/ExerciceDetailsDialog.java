package com.gestioncommerciale.view;

import java.awt.*;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import com.gestioncommerciale.model.Exercice;

/**
 * Dialog for viewing exercise details
 */
public class ExerciceDetailsDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private Exercice exercice;
    
    public ExerciceDetailsDialog(Window parent, Exercice exercice) {
        super(parent, "Détails de l'Exercice", ModalityType.APPLICATION_MODAL);
        this.exercice = exercice;
        
        initializeComponents();
        layoutComponents();
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // No specific components needed - all will be created in layout
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main panel with details
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Title
        JLabel titleLabel = new JLabel(exercice.getLibelle());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Status with color
        JLabel statusLabel = new JLabel(exercice.getStatut());
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        switch (exercice.getStatut()) {
            case "En cours":
                statusLabel.setForeground(new Color(0, 150, 0));
                break;
            case "Clôturé":
                statusLabel.setForeground(new Color(100, 100, 100));
                break;
            case "Échu":
                statusLabel.setForeground(new Color(200, 100, 0));
                break;
            case "À venir":
                statusLabel.setForeground(new Color(0, 0, 200));
                break;
        }
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(statusLabel, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 15, 5);
        mainPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        row++;
        
        // Date de début
        addDetailRow(mainPanel, gbc, row++, "Date de début:", 
            exercice.getDateDebut() != null ? exercice.getDateDebut().format(DATE_FORMATTER) : "Non définie");
        
        // Date de fin
        addDetailRow(mainPanel, gbc, row++, "Date de fin:", 
            exercice.getDateFin() != null ? exercice.getDateFin().format(DATE_FORMATTER) : "Non définie");
        
        // Durée
        addDetailRow(mainPanel, gbc, row++, "Durée:", 
            exercice.getDureeEnJours() + " jours");
        
        // Date de clôture
        if (exercice.isCloture()) {
            addDetailRow(mainPanel, gbc, row++, "Date de clôture:", 
                exercice.getDateCloture() != null ? exercice.getDateCloture().format(DATE_FORMATTER) : "Non définie");
        }
        
        // Active
        addDetailRow(mainPanel, gbc, row++, "Actif:", 
            exercice.isActif() ? "Oui" : "Non");
        
        // Description
        if (exercice.getDescription() != null && !exercice.getDescription().trim().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = row;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            mainPanel.add(new JLabel("Description:"), gbc);
            
            JTextArea descriptionArea = new JTextArea(exercice.getDescription());
            descriptionArea.setEditable(false);
            descriptionArea.setBackground(getBackground());
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setBorder(BorderFactory.createEmptyBorder());
            
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            JScrollPane scrollPane = new JScrollPane(descriptionArea);
            scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
            mainPanel.add(scrollPane, gbc);
            row++;
        }
        
        // Observations
        if (exercice.getObservations() != null && !exercice.getObservations().trim().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = row;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            mainPanel.add(new JLabel("Observations:"), gbc);
            
            JTextArea observationsArea = new JTextArea(exercice.getObservations());
            observationsArea.setEditable(false);
            observationsArea.setBackground(getBackground());
            observationsArea.setLineWrap(true);
            observationsArea.setWrapStyleWord(true);
            observationsArea.setBorder(BorderFactory.createEmptyBorder());
            
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            JScrollPane scrollPane = new JScrollPane(observationsArea);
            scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
            mainPanel.add(scrollPane, gbc);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        // Label
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(labelComponent, gbc);
        
        // Value
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel valueComponent = new JLabel(value);
        panel.add(valueComponent, gbc);
    }
}
