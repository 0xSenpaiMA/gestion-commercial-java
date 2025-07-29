package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.gestioncommerciale.model.BonCommande;
import com.gestioncommerciale.service.BonCommandeService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Frame for managing purchase orders (bons de commande)
 */
public class BonCommandeManagementFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private JTable bonCommandeTable;
    private DefaultTableModel tableModel;
    private BonCommandeService bonCommandeService;
    
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton duplicateButton;
    private JButton printButton;
    private JButton refreshButton;
    
    public BonCommandeManagementFrame() {
        this.bonCommandeService = new BonCommandeService();
        initializeUI();
        loadBonsCommande();
    }
    
    private void initializeUI() {
        setTitle("Gestion des Bons de Commande");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set application icon if available
        UIUtils.setApplicationIcon(this);
        
        setLayout(new BorderLayout());
        
        // Create table
        createTable();
        
        // Create buttons panel
        createButtonsPanel();
        
        // Add components
        add(new JScrollPane(bonCommandeTable), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);
        
        // Add listeners
        addListeners();
    }
    
    private void createTable() {
        String[] columnNames = {
            "Numéro", "Fournisseur", "Date Commande", "Date Livraison Prévue",
            "Statut", "Total HT", "Total TTC"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bonCommandeTable = new JTable(tableModel);
        bonCommandeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bonCommandeTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        bonCommandeTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Numéro
        bonCommandeTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Fournisseur
        bonCommandeTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Date Commande
        bonCommandeTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Date Livraison
        bonCommandeTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Statut
        bonCommandeTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Total HT
        bonCommandeTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Total TTC
        
        bonCommandeTable.setRowHeight(25);
        bonCommandeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }
    
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        
        addButton = new JButton("Nouveau");
        addButton.setPreferredSize(new Dimension(100, 30));
        
        editButton = new JButton("Modifier");
        editButton.setPreferredSize(new Dimension(100, 30));
        editButton.setEnabled(false);
        
        deleteButton = new JButton("Supprimer");
        deleteButton.setPreferredSize(new Dimension(100, 30));
        deleteButton.setEnabled(false);
        
        duplicateButton = new JButton("Dupliquer");
        duplicateButton.setPreferredSize(new Dimension(100, 30));
        duplicateButton.setEnabled(false);
        
        printButton = new JButton("Imprimer");
        printButton.setPreferredSize(new Dimension(100, 30));
        printButton.setEnabled(false);
        
        refreshButton = new JButton("Actualiser");
        refreshButton.setPreferredSize(new Dimension(100, 30));
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(duplicateButton);
        buttonsPanel.add(printButton);
        buttonsPanel.add(refreshButton);
        
        return buttonsPanel;
    }
    
    private void addListeners() {
        // Table selection listener
        bonCommandeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Double-click listener
        bonCommandeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bonCommandeTable.getSelectedRow() != -1) {
                    editBonCommande();
                }
            }
        });
        
        // Button listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBonCommande();
            }
        });
        
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editBonCommande();
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBonCommande();
            }
        });
        
        duplicateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                duplicateBonCommande();
            }
        });
        
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printBonCommande();
            }
        });
        
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBonsCommande();
            }
        });
    }
    
    private void updateButtonStates() {
        boolean hasSelection = bonCommandeTable.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        duplicateButton.setEnabled(hasSelection);
        printButton.setEnabled(hasSelection);
    }
    
    private void loadBonsCommande() {
        try {
            List<BonCommande> bonsCommande = bonCommandeService.getAllBonsCommande();
            updateTable(bonsCommande);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des bons de commande: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable(List<BonCommande> bonsCommande) {
        tableModel.setRowCount(0);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (BonCommande bc : bonsCommande) {
            Object[] rowData = {
                bc.getNumero(),
                bc.getFournisseur() != null ? bc.getFournisseur().getNom() : "",
                bc.getDateCommande() != null ? bc.getDateCommande().format(dateFormatter) : "",
                bc.getDateLivraisonPrevue() != null ? bc.getDateLivraisonPrevue().format(dateFormatter) : "",
                bc.getStatut() != null ? bc.getStatut().toString() : "",
                String.format("%.2f €", bc.getTotalHT()),
                String.format("%.2f €", bc.getTotalTTC())
            };
            tableModel.addRow(rowData);
        }
    }
    
    private BonCommande getSelectedBonCommande() {
        int selectedRow = bonCommandeTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        
        String numero = (String) tableModel.getValueAt(selectedRow, 0);
        try {
            List<BonCommande> bonsCommande = bonCommandeService.getAllBonsCommande();
            for (BonCommande bc : bonsCommande) {
                if (bc.getNumero().equals(numero)) {
                    return bc;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la récupération du bon de commande: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    private void addBonCommande() {
        BonCommandeEditDialog dialog = new BonCommandeEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadBonsCommande();
        }
    }
    
    private void editBonCommande() {
        BonCommande selectedBonCommande = getSelectedBonCommande();
        if (selectedBonCommande != null) {
            BonCommandeEditDialog dialog = new BonCommandeEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), selectedBonCommande);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                loadBonsCommande();
            }
        }
    }
    
    private void deleteBonCommande() {
        BonCommande selectedBonCommande = getSelectedBonCommande();
        if (selectedBonCommande != null) {
            int result = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer le bon de commande " + selectedBonCommande.getNumero() + " ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    bonCommandeService.delete(selectedBonCommande);
                    loadBonsCommande();
                    JOptionPane.showMessageDialog(this,
                        "Bon de commande supprimé avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la suppression: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void duplicateBonCommande() {
        BonCommande selectedBonCommande = getSelectedBonCommande();
        if (selectedBonCommande != null) {
            try {
                BonCommande duplicate = bonCommandeService.duplicateBonCommande(selectedBonCommande);
                BonCommandeEditDialog dialog = new BonCommandeEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), duplicate);
                dialog.setVisible(true);
                
                if (dialog.isConfirmed()) {
                    loadBonsCommande();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la duplication: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void printBonCommande() {
        BonCommande selectedBonCommande = getSelectedBonCommande();
        if (selectedBonCommande != null) {
            BonCommandePrintDialog printDialog = new BonCommandePrintDialog((JFrame) SwingUtilities.getWindowAncestor(this), selectedBonCommande);
            printDialog.setVisible(true);
        }
    }
}
