package com.gestioncommerciale.view;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.*;

import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.ContratAssistance;
import com.gestioncommerciale.service.ContratAssistanceService;

/**
 * Dialog for editing assistance contracts
 */
public class ContratAssistanceEditDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private ContratAssistanceService contratService;
    private ContratAssistance contrat;
    private boolean confirmed = false;
    
    // Form fields
    private JTextField numeroField;
    private JComboBox<Client> clientComboBox;
    private JTextField dateDebutField;
    private JTextField dateFinField;
    private JTextArea descriptionArea;
    private JTextArea conditionsArea;
    private JTextField tarifHoraireField;
    private JSpinner heuresInclusesSpinner;
    private JTextArea observationsArea;
    private JCheckBox actifCheckBox;
    
    private JButton saveButton, cancelButton;
    
    public ContratAssistanceEditDialog(Window parent, ContratAssistance contrat) {
        super(parent, "Contrat d'Assistance", ModalityType.APPLICATION_MODAL);
        this.contratService = new ContratAssistanceService();
        this.contrat = contrat;
        
        initializeComponents();
        layoutComponents();
        bindEvents();
        loadData();
        
        setSize(600, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        numeroField = new JTextField(20);
        numeroField.setToolTipText("Laissez vide pour génération automatique");
        
        clientComboBox = new JComboBox<>();
        clientComboBox.setRenderer(new ClientRenderer());
        
        dateDebutField = new JTextField(15);
        dateDebutField.setToolTipText("Format: dd/MM/yyyy");
        
        dateFinField = new JTextField(15);
        dateFinField.setToolTipText("Format: dd/MM/yyyy");
        
        descriptionArea = new JTextArea(3, 40);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        conditionsArea = new JTextArea(4, 40);
        conditionsArea.setLineWrap(true);
        conditionsArea.setWrapStyleWord(true);
        
        tarifHoraireField = new JTextField(10);
        
        heuresInclusesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        
        observationsArea = new JTextArea(3, 40);
        observationsArea.setLineWrap(true);
        observationsArea.setWrapStyleWord(true);
        
        actifCheckBox = new JCheckBox("Contrat actif");
        actifCheckBox.setSelected(true);
        
        saveButton = new JButton("Enregistrer");
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
        
        // Numéro
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("Numéro:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(numeroField, gbc);
        row++;
        
        // Client
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Client: *"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(clientComboBox, gbc);
        row++;
        
        // Dates panel
        JPanel datesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datesPanel.add(new JLabel("Début: "));
        datesPanel.add(dateDebutField);
        datesPanel.add(Box.createHorizontalStrut(20));
        datesPanel.add(new JLabel("Fin: "));
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
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(descriptionArea), gbc);
        row++;
        
        // Conditions
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Conditions:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(conditionsArea), gbc);
        row++;
        
        // Tarif et heures panel
        JPanel tarifPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tarifPanel.add(new JLabel("Tarif/h (€): "));
        tarifPanel.add(tarifHoraireField);
        tarifPanel.add(Box.createHorizontalStrut(20));
        tarifPanel.add(new JLabel("Heures incluses: "));
        tarifPanel.add(heuresInclusesSpinner);
        
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(tarifPanel, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Observations
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Observations:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(observationsArea), gbc);
        row++;
        
        // Actif checkbox
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(actifCheckBox, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void bindEvents() {
        saveButton.addActionListener(e -> saveContrat());
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void loadData() {
        // Load clients
        try {
            List<Client> clients = contratService.getAllClients();
            clientComboBox.removeAllItems();
            for (Client client : clients) {
                clientComboBox.addItem(client);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors du chargement des clients:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
        
        // Fill form if editing existing contract
        if (contrat != null) {
            numeroField.setText(contrat.getNumero());
            
            if (contrat.getClient() != null) {
                clientComboBox.setSelectedItem(contrat.getClient());
            }
            
            if (contrat.getDateDebut() != null) {
                dateDebutField.setText(contrat.getDateDebut().format(DATE_FORMATTER));
            }
            
            if (contrat.getDateFin() != null) {
                dateFinField.setText(contrat.getDateFin().format(DATE_FORMATTER));
            }
            
            descriptionArea.setText(contrat.getDescription());
            conditionsArea.setText(contrat.getConditions());
            
            if (contrat.getTarifHoraire() != null) {
                tarifHoraireField.setText(contrat.getTarifHoraire().toString());
            }
            
            heuresInclusesSpinner.setValue(contrat.getHeuresIncluses());
            observationsArea.setText(contrat.getObservations());
            actifCheckBox.setSelected(contrat.isActif());
        }
    }
    
    private void saveContrat() {
        try {
            // Validate form
            if (!validateForm()) {
                return;
            }
            
            // Create or update contract
            if (contrat == null) {
                contrat = new ContratAssistance();
            }
            
            // Set values
            String numero = numeroField.getText().trim();
            if (!numero.isEmpty()) {
                contrat.setNumero(numero);
            }
            
            contrat.setClient((Client) clientComboBox.getSelectedItem());
            
            String dateDebutStr = dateDebutField.getText().trim();
            if (!dateDebutStr.isEmpty()) {
                contrat.setDateDebut(LocalDate.parse(dateDebutStr, DATE_FORMATTER));
            }
            
            String dateFinStr = dateFinField.getText().trim();
            if (!dateFinStr.isEmpty()) {
                contrat.setDateFin(LocalDate.parse(dateFinStr, DATE_FORMATTER));
            }
            
            contrat.setDescription(descriptionArea.getText().trim());
            contrat.setConditions(conditionsArea.getText().trim());
            
            String tarifStr = tarifHoraireField.getText().trim();
            if (!tarifStr.isEmpty()) {
                contrat.setTarifHoraire(new BigDecimal(tarifStr));
            }
            
            contrat.setHeuresIncluses((Integer) heuresInclusesSpinner.getValue());
            contrat.setObservations(observationsArea.getText().trim());
            contrat.setActif(actifCheckBox.isSelected());
            
            // Save
            contratService.save(contrat);
            
            confirmed = true;
            dispose();
            
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                this,
                "Format de date invalide. Utilisez le format dd/MM/yyyy",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Le tarif horaire doit être un nombre valide",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors de la sauvegarde:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private boolean validateForm() {
        // Client required
        if (clientComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez sélectionner un client",
                "Champ requis",
                JOptionPane.WARNING_MESSAGE
            );
            clientComboBox.requestFocus();
            return false;
        }
        
        // Validate dates format
        String dateDebutStr = dateDebutField.getText().trim();
        String dateFinStr = dateFinField.getText().trim();
        
        LocalDate dateDebut = null;
        LocalDate dateFin = null;
        
        if (!dateDebutStr.isEmpty()) {
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
        }
        
        if (!dateFinStr.isEmpty()) {
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
        }
        
        // Check date order
        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            JOptionPane.showMessageDialog(
                this,
                "La date de début ne peut pas être postérieure à la date de fin",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE
            );
            dateDebutField.requestFocus();
            return false;
        }
        
        // Validate tarif horaire
        String tarifStr = tarifHoraireField.getText().trim();
        if (!tarifStr.isEmpty()) {
            try {
                BigDecimal tarif = new BigDecimal(tarifStr);
                if (tarif.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Le tarif horaire ne peut pas être négatif",
                        "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE
                    );
                    tarifHoraireField.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Le tarif horaire doit être un nombre valide",
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE
                );
                tarifHoraireField.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    private static class ClientRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Client) {
                Client client = (Client) value;
                setText(client.getNom() + " (" + client.getEmail() + ")");
            }
            
            return this;
        }
    }
}
