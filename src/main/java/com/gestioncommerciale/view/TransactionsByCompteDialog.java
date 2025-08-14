package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.gestioncommerciale.model.CompteBancaire;
import com.gestioncommerciale.model.TransactionBancaire;
import com.gestioncommerciale.service.TransactionBancaireService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Dialogue pour afficher les transactions d'un compte bancaire
 */
public class TransactionsByCompteDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final TransactionBancaireService service;
    private final CompteBancaire compte;
    
    private JTable table;
    private TransactionsTableModel tableModel;
    private JButton closeButton;
    private JLabel totalTransactionsLabel;
    private JLabel soldeLabel;
    
    public TransactionsByCompteDialog(JFrame parent, CompteBancaire compte) {
        super(parent, "Transactions - " + compte.getNumeroCompte(), true);
        this.service = new TransactionBancaireService();
        this.compte = compte;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
        configureDialog();
    }
    
    private void initializeComponents() {
        // Table des transactions
        tableModel = new TransactionsTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        setupTableColumns();
        
        // Labels d'information
        totalTransactionsLabel = new JLabel("0 transactions");
        soldeLabel = new JLabel(CURRENCY_FORMAT.format(compte.getSoldeActuel()) + " DH");
        
        // Bouton
        closeButton = UIUtils.createStyledButton("Fermer", UIUtils.SECONDARY_COLOR);
    }
    
    private void setupTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Référence
        table.getColumnModel().getColumn(2).setPreferredWidth(60);  // Type
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Mode
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Montant
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Bénéficiaire
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Statut
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panneau d'en-tête avec informations du compte
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Table des transactions
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Informations du compte
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel compteLabel = new JLabel("Compte: " + compte.getNumeroCompte() + 
                                      " (" + compte.getNomBanque() + ")");
        compteLabel.setFont(compteLabel.getFont().deriveFont(Font.BOLD, 14f));
        compteLabel.setForeground(UIUtils.PRIMARY_COLOR);
        infoPanel.add(compteLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Statistiques
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.add(new JLabel("Solde actuel: "));
        soldeLabel.setFont(soldeLabel.getFont().deriveFont(Font.BOLD));
        if (compte.getSoldeActuel().compareTo(BigDecimal.ZERO) < 0) {
            soldeLabel.setForeground(UIUtils.ERROR_COLOR);
        } else {
            soldeLabel.setForeground(UIUtils.SUCCESS_COLOR);
        }
        statsPanel.add(soldeLabel);
        
        statsPanel.add(new JLabel("   |   "));
        statsPanel.add(totalTransactionsLabel);
        
        panel.add(statsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        closeButton.addActionListener(e -> dispose());
    }
    
    private void loadData() {
        try {
            List<TransactionBancaire> transactions = service.getTransactionsByCompte(compte.getId());
            tableModel.setTransactions(transactions);
            
            // Mettre à jour les statistiques
            totalTransactionsLabel.setText(transactions.size() + " transaction" + (transactions.size() > 1 ? "s" : ""));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des transactions: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void configureDialog() {
        setSize(800, 500);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    // Model pour la table des transactions
    private static class TransactionsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = {
            "Date", "Référence", "Type", "Mode", "Montant (DH)", "Bénéficiaire", "Statut"
        };
        
        private List<TransactionBancaire> transactions = List.of();
        
        public void setTransactions(List<TransactionBancaire> transactions) {
            this.transactions = transactions;
            fireTableDataChanged();
        }
        
        @Override
        public int getRowCount() {
            return transactions.size();
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
            TransactionBancaire transaction = transactions.get(row);
            switch (column) {
                case 0: return transaction.getDateTransaction() != null ? 
                            transaction.getDateTransaction().format(DATE_FORMAT) : "";
                case 1: return transaction.getReference();
                case 2: return transaction.getType().getLibelle();
                case 3: return transaction.getModePaiement().getLibelle();
                case 4: return CURRENCY_FORMAT.format(transaction.getMontant());
                case 5: return transaction.getBeneficiaire();
                case 6: return transaction.getStatut().getLibelle();
                default: return "";
            }
        }
    }
}
