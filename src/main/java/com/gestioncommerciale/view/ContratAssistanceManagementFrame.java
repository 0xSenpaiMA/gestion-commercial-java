package com.gestioncommerciale.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.model.ContratAssistance;
import com.gestioncommerciale.service.ContratAssistanceService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Frame for managing assistance contracts
 */
public class ContratAssistanceManagementFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private ContratAssistanceService contratService;
    private JTable contratsTable;
    private ContratTableModel tableModel;
    private JTextField rechercheField;
    private JComboBox<String> statutFilter;
    private JButton addButton, editButton, deleteButton, printButton;
    
    public ContratAssistanceManagementFrame() {
        this.contratService = new ContratAssistanceService();
        initializeComponents();
        layoutComponents();
        bindEvents();
        refreshData();
        
        setTitle("Gestion des Contrats d'Assistance");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Search and filter components
        rechercheField = new JTextField(20);
        rechercheField.setToolTipText("Rechercher par numéro, client ou description");
        
        statutFilter = new JComboBox<>(new String[]{
            "Tous", "Actifs", "Expirés", "En cours", "Expiration proche (30j)"
        });
        
        // Buttons
        addButton = new JButton("Nouveau");
        editButton = new JButton("Modifier");
        deleteButton = new JButton("Supprimer");
        printButton = new JButton("Imprimer");
        
        // Initially disable edit and delete buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        printButton.setEnabled(false);
        
        // Table
        tableModel = new ContratTableModel();
        contratsTable = new JTable(tableModel);
        contratsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setupTable();
    }
    
    private void setupTable() {
        // Column widths
        TableColumnModel columnModel = contratsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Numéro
        columnModel.getColumn(1).setPreferredWidth(200); // Client
        columnModel.getColumn(2).setPreferredWidth(100); // Date début
        columnModel.getColumn(3).setPreferredWidth(100); // Date fin
        columnModel.getColumn(4).setPreferredWidth(300); // Description
        columnModel.getColumn(5).setPreferredWidth(80);  // Tarif horaire
        columnModel.getColumn(6).setPreferredWidth(80);  // Statut
        
        // Custom renderer for status column
        contratsTable.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        
        // Row height
        contratsTable.setRowHeight(25);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Top panel with search and filters
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        topPanel.add(new JLabel("Recherche:"));
        topPanel.add(rechercheField);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("Statut:"));
        topPanel.add(statutFilter);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(contratsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Liste des Contrats"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(printButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void bindEvents() {
        // Table selection
        contratsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = contratsTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
                printButton.setEnabled(hasSelection);
            }
        });
        
        // Double-click to edit
        contratsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editContrat();
                }
            }
        });
        
        // Buttons
        addButton.addActionListener(e -> addContrat());
        editButton.addActionListener(e -> editContrat());
        deleteButton.addActionListener(e -> deleteContrat());
        printButton.addActionListener(e -> printContrat());
        
        // Search and filter
        rechercheField.addActionListener(e -> refreshData());
        statutFilter.addActionListener(e -> refreshData());
    }
    
    private void addContrat() {
        ContratAssistanceEditDialog dialog = new ContratAssistanceEditDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refreshData();
        }
    }
    
    private void editContrat() {
        int selectedRow = contratsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        ContratAssistance contrat = tableModel.getContratAt(selectedRow);
        ContratAssistanceEditDialog dialog = new ContratAssistanceEditDialog(this, contrat);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refreshData();
        }
    }
    
    private void deleteContrat() {
        int selectedRow = contratsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        ContratAssistance contrat = tableModel.getContratAt(selectedRow);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir supprimer ce contrat d'assistance ?\n\n" +
            "Numéro: " + contrat.getNumero() + "\n" +
            "Client: " + contrat.getClient().getNom(),
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                contratService.delete(contrat);
                refreshData();
                JOptionPane.showMessageDialog(
                    this,
                    "Contrat supprimé avec succès",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la suppression du contrat:\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    private void printContrat() {
        int selectedRow = contratsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        ContratAssistance contrat = tableModel.getContratAt(selectedRow);
        ContratAssistancePrintDialog printDialog = new ContratAssistancePrintDialog(this, contrat);
        printDialog.setVisible(true);
    }
    
    private void refreshData() {
        String recherche = rechercheField.getText().trim();
        String statut = (String) statutFilter.getSelectedItem();
        
        List<ContratAssistance> contrats;
        
        try {
            switch (statut) {
                case "Actifs":
                    contrats = contratService.getContratsActifs();
                    break;
                case "Expirés":
                    contrats = contratService.getContratsExpires();
                    break;
                case "En cours":
                    contrats = contratService.getContratsActifs();
                    break;
                case "Expiration proche (30j)":
                    contrats = contratService.getContratsExpirantBientot(30);
                    break;
                default:
                    contrats = contratService.getAllContrats();
                    break;
            }
            
            // Apply search filter
            if (!recherche.isEmpty()) {
                contrats = contrats.stream()
                    .filter(c -> 
                        c.getNumero().toLowerCase().contains(recherche.toLowerCase()) ||
                        c.getClient().getNom().toLowerCase().contains(recherche.toLowerCase()) ||
                        (c.getDescription() != null && c.getDescription().toLowerCase().contains(recherche.toLowerCase()))
                    )
                    .toList();
            }
            
            tableModel.setContrats(contrats);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors du chargement des contrats:\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private static class ContratTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = {
            "Numéro", "Client", "Date début", "Date fin", "Description", "Tarif/h", "Statut"
        };
        
        private List<ContratAssistance> contrats = List.of();
        
        public void setContrats(List<ContratAssistance> contrats) {
            this.contrats = contrats;
            fireTableDataChanged();
        }
        
        public ContratAssistance getContratAt(int row) {
            return contrats.get(row);
        }
        
        @Override
        public int getRowCount() {
            return contrats.size();
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
            ContratAssistance contrat = contrats.get(row);
            
            switch (column) {
                case 0:
                    return contrat.getNumero();
                case 1:
                    return contrat.getClient().getNom();
                case 2:
                    return contrat.getDateDebut() != null ? 
                        contrat.getDateDebut().format(DATE_FORMATTER) : "";
                case 3:
                    return contrat.getDateFin() != null ? 
                        contrat.getDateFin().format(DATE_FORMATTER) : "";
                case 4:
                    return contrat.getDescription();
                case 5:
                    return String.format("%.2f €", contrat.getTarifHoraire());
                case 6:
                    if (!contrat.isActif()) {
                        return "Inactif";
                    } else if (contrat.isExpire()) {
                        return "Expiré";
                    } else if (contrat.isEnCours()) {
                        return "En cours";
                    } else {
                        return "À venir";
                    }
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
                    case "Expiré":
                        c.setBackground(new Color(255, 230, 230)); // Light red
                        break;
                    case "À venir":
                        c.setBackground(new Color(230, 230, 255)); // Light blue
                        break;
                    case "Inactif":
                        c.setBackground(new Color(245, 245, 245)); // Light gray
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
