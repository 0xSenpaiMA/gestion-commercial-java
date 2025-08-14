package com.gestioncommerciale.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.gestioncommerciale.model.Exercice;
import com.gestioncommerciale.service.ExerciceService;

/**
 * Frame for managing accounting periods (exercices)
 */
public class ExerciceManagementFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private ExerciceService exerciceService;
    private JTable exercicesTable;
    private ExerciceTableModel tableModel;
    private JButton nouvelExerciceButton, cloturerButton, cloturerEtCreerButton, refreshButton;
    private JLabel exerciceActifLabel;
    
    public ExerciceManagementFrame() {
        this.exerciceService = new ExerciceService();
        initializeComponents();
        layoutComponents();
        bindEvents();
        refreshData();
        
        setTitle("Gestion des Exercices Comptables");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Exercise info label
        exerciceActifLabel = new JLabel();
        exerciceActifLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        exerciceActifLabel.setForeground(new Color(0, 100, 0));
        
        // Buttons
        nouvelExerciceButton = new JButton("Nouvel Exercice");
        cloturerButton = new JButton("Clôturer Exercice");
        cloturerEtCreerButton = new JButton("Clôturer et Créer Nouveau");
        refreshButton = new JButton("Actualiser");
        
        // Initially disable buttons
        cloturerButton.setEnabled(false);
        cloturerEtCreerButton.setEnabled(false);
        
        // Table
        tableModel = new ExerciceTableModel();
        exercicesTable = new JTable(tableModel);
        exercicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setupTable();
    }
    
    private void setupTable() {
        // Column widths
        TableColumnModel columnModel = exercicesTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200); // Libellé
        columnModel.getColumn(1).setPreferredWidth(100); // Date début
        columnModel.getColumn(2).setPreferredWidth(100); // Date fin
        columnModel.getColumn(3).setPreferredWidth(100); // Date clôture
        columnModel.getColumn(4).setPreferredWidth(80);  // Durée
        columnModel.getColumn(5).setPreferredWidth(100); // Statut
        
        // Custom renderer for status column
        exercicesTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        
        // Row height
        exercicesTable.setRowHeight(25);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Top panel with current exercise info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Exercice actuel : "));
        infoPanel.add(exerciceActifLabel);
        
        topPanel.add(infoPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(exercicesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Historique des Exercices"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        buttonPanel.add(nouvelExerciceButton);
        buttonPanel.add(cloturerButton);
        buttonPanel.add(cloturerEtCreerButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void bindEvents() {
        // Table selection
        exercicesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Double-click to view details
        exercicesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewExerciceDetails();
                }
            }
        });
        
        // Buttons
        nouvelExerciceButton.addActionListener(e -> creerNouvelExercice());
        cloturerButton.addActionListener(e -> cloturerExercice());
        cloturerEtCreerButton.addActionListener(e -> cloturerEtCreerNouvelExercice());
        refreshButton.addActionListener(e -> refreshData());
    }
    
    private void updateButtonStates() {
        Exercice exerciceActif = exerciceService.getExerciceActif();
        boolean hasActiveExercice = exerciceActif != null;
        boolean canCloseExercice = hasActiveExercice && exerciceActif.peutEtreCloture();
        
        cloturerButton.setEnabled(canCloseExercice);
        cloturerEtCreerButton.setEnabled(canCloseExercice);
        nouvelExerciceButton.setEnabled(exerciceService.peutCreerNouvelExercice());
    }
    
    private void creerNouvelExercice() {
        NouvelExerciceDialog dialog = new NouvelExerciceDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refreshData();
        }
    }
    
    private void cloturerExercice() {
        Exercice exerciceActif = exerciceService.getExerciceActif();
        if (exerciceActif == null) {
            JOptionPane.showMessageDialog(
                this,
                "Aucun exercice actif à clôturer",
                "Information",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir clôturer l'exercice en cours ?\n\n" +
            "Exercice : " + exerciceActif.getLibelle() + "\n" +
            "Période : " + exerciceActif.getDateDebut().format(DATE_FORMATTER) + 
            " au " + exerciceActif.getDateFin().format(DATE_FORMATTER) + "\n\n" +
            "Cette action est irréversible.",
            "Confirmation de clôture",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                exerciceService.cloturerExerciceActif();
                refreshData();
                JOptionPane.showMessageDialog(
                    this,
                    "Exercice clôturé avec succès",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la clôture de l'exercice :\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void cloturerEtCreerNouvelExercice() {
        CloturerEtCreerDialog dialog = new CloturerEtCreerDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refreshData();
        }
    }
    
    private void viewExerciceDetails() {
        int selectedRow = exercicesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Exercice exercice = tableModel.getExerciceAt(selectedRow);
        ExerciceDetailsDialog dialog = new ExerciceDetailsDialog(this, exercice);
        dialog.setVisible(true);
    }
    
    private void refreshData() {
        try {
            List<Exercice> exercices = exerciceService.getAllExercices();
            tableModel.setExercices(exercices);
            
            // Update current exercise info
            Exercice exerciceActif = exerciceService.getExerciceActif();
            if (exerciceActif != null) {
                exerciceActifLabel.setText(exerciceActif.getLibelle() + " (" + exerciceActif.getStatut() + ")");
                exerciceActifLabel.setForeground(exerciceActif.isEnCours() ? 
                    new Color(0, 150, 0) : new Color(200, 100, 0));
            } else {
                exerciceActifLabel.setText("Aucun exercice actif");
                exerciceActifLabel.setForeground(Color.RED);
            }
            
            updateButtonStates();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors du chargement des exercices :\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private static class ExerciceTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = {
            "Libellé", "Date début", "Date fin", "Date clôture", "Durée (j)", "Statut"
        };
        
        private List<Exercice> exercices = List.of();
        
        public void setExercices(List<Exercice> exercices) {
            this.exercices = exercices;
            fireTableDataChanged();
        }
        
        public Exercice getExerciceAt(int row) {
            return exercices.get(row);
        }
        
        @Override
        public int getRowCount() {
            return exercices.size();
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            Exercice exercice = exercices.get(row);
            
            switch (column) {
                case 0:
                    return exercice.getLibelle();
                case 1:
                    return exercice.getDateDebut() != null ? 
                        exercice.getDateDebut().format(DATE_FORMATTER) : "";
                case 2:
                    return exercice.getDateFin() != null ? 
                        exercice.getDateFin().format(DATE_FORMATTER) : "";
                case 3:
                    return exercice.getDateCloture() != null ? 
                        exercice.getDateCloture().format(DATE_FORMATTER) : "";
                case 4:
                    return exercice.getDureeEnJours();
                case 5:
                    return exercice.getStatut();
                default:
                    return "";
            }
        }
    }
    
    private static class StatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String status = (String) value;
                switch (status) {
                    case "En cours":
                        c.setBackground(new Color(230, 255, 230)); // Light green
                        break;
                    case "Clôturé":
                        c.setBackground(new Color(245, 245, 245)); // Light gray
                        break;
                    case "Échu":
                        c.setBackground(new Color(255, 245, 230)); // Light orange
                        break;
                    case "À venir":
                        c.setBackground(new Color(230, 230, 255)); // Light blue
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        break;
                }
            }
            
            return c;
        }
    }
}
