package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.gestioncommerciale.model.CompteBancaire;
import com.gestioncommerciale.model.TransactionBancaire;
import com.gestioncommerciale.service.CompteBancaireService;
import com.gestioncommerciale.service.TransactionBancaireService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Interface de gestion des comptes bancaires
 */
public class BankingManagementFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final CompteBancaireService compteBancaireService;
    private final TransactionBancaireService transactionBancaireService;
    
    // Composants pour les comptes
    private JTable comptesTable;
    private ComptesTableModel comptesTableModel;
    private JButton addCompteButton, editCompteButton, deleteCompteButton;
    private JButton viewTransactionsButton, refreshComptesButton;
    
    // Composants pour les transactions
    private JTable transactionsTable;
    private TransactionsTableModel transactionsTableModel;
    private JButton addTransactionButton, editTransactionButton, deleteTransactionButton;
    private JButton compenserButton, rejeterButton, refreshTransactionsButton;
    
    // Panneau de résumé
    private JLabel totalComptesLabel, soldeTotalLabel, comptesActifsLabel, comptesEnDecouvertLabel;
    private JLabel transactionsEchuesLabel, prochainementEchuesLabel;
    
    public BankingManagementFrame() {
        this.compteBancaireService = new CompteBancaireService();
        this.transactionBancaireService = new TransactionBancaireService();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Table des comptes
        comptesTableModel = new ComptesTableModel();
        comptesTable = new JTable(comptesTableModel);
        comptesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        comptesTable.setRowHeight(25);
        setupComptesTableColumns();
        
        // Boutons pour les comptes
        addCompteButton = UIUtils.createStyledButton("Nouveau Compte", UIUtils.SUCCESS_COLOR);
        editCompteButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteCompteButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        viewTransactionsButton = UIUtils.createStyledButton("Voir Transactions", UIUtils.WARNING_COLOR);
        refreshComptesButton = UIUtils.createStyledButton("Actualiser", UIUtils.SECONDARY_COLOR);
        
        // Table des transactions
        transactionsTableModel = new TransactionsTableModel();
        transactionsTable = new JTable(transactionsTableModel);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionsTable.setRowHeight(25);
        setupTransactionsTableColumns();
        
        // Boutons pour les transactions
        addTransactionButton = UIUtils.createStyledButton("Nouvelle Transaction", UIUtils.SUCCESS_COLOR);
        editTransactionButton = UIUtils.createStyledButton("Modifier", UIUtils.PRIMARY_COLOR);
        deleteTransactionButton = UIUtils.createStyledButton("Supprimer", UIUtils.ERROR_COLOR);
        compenserButton = UIUtils.createStyledButton("Compenser", UIUtils.WARNING_COLOR);
        rejeterButton = UIUtils.createStyledButton("Rejeter", UIUtils.ERROR_COLOR);
        refreshTransactionsButton = UIUtils.createStyledButton("Actualiser", UIUtils.SECONDARY_COLOR);
        
        // Labels pour le résumé
        totalComptesLabel = new JLabel("0");
        soldeTotalLabel = new JLabel("0.00 DH");
        comptesActifsLabel = new JLabel("0");
        comptesEnDecouvertLabel = new JLabel("0");
        transactionsEchuesLabel = new JLabel("0");
        prochainementEchuesLabel = new JLabel("0");
        
        // Initialiser l'état des boutons
        editCompteButton.setEnabled(false);
        deleteCompteButton.setEnabled(false);
        viewTransactionsButton.setEnabled(false);
        editTransactionButton.setEnabled(false);
        deleteTransactionButton.setEnabled(false);
        compenserButton.setEnabled(false);
        rejeterButton.setEnabled(false);
    }
    
    private void setupComptesTableColumns() {
        comptesTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        comptesTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Numéro
        comptesTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Banque
        comptesTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Titulaire
        comptesTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Type
        comptesTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Solde
        comptesTable.getColumnModel().getColumn(6).setPreferredWidth(60);   // Actif
        
        // Renderer pour le solde avec couleurs
        comptesTable.getColumnModel().getColumn(5).setCellRenderer(new SoldeCellRenderer());
    }
    
    private void setupTransactionsTableColumns() {
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Référence
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Date
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // Type
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Mode
        transactionsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Montant
        transactionsTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Bénéficiaire
        transactionsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Statut
        transactionsTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Échéance
        
        // Renderer pour le montant avec couleurs
        transactionsTable.getColumnModel().getColumn(4).setCellRenderer(new MontantCellRenderer());
        transactionsTable.getColumnModel().getColumn(6).setCellRenderer(new StatutCellRenderer());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panneau principal avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Onglet Comptes
        JPanel comptesPanel = createComptesPanel();
        tabbedPane.addTab("Comptes Bancaires", comptesPanel);
        
        // Onglet Transactions
        JPanel transactionsPanel = createTransactionsPanel();
        tabbedPane.addTab("Transactions", transactionsPanel);
        
        // Onglet Tableau de bord
        JPanel dashboardPanel = createDashboardPanel();
        tabbedPane.addTab("Tableau de Bord", dashboardPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createComptesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Titre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel titleLabel = new JLabel("Gestion des Comptes Bancaires");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Table
        JScrollPane scrollPane = new JScrollPane(comptesTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addCompteButton);
        buttonPanel.add(editCompteButton);
        buttonPanel.add(deleteCompteButton);
        buttonPanel.add(viewTransactionsButton);
        buttonPanel.add(refreshComptesButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Titre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        JLabel titleLabel = new JLabel("Gestion des Transactions");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(UIUtils.PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Table
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(addTransactionButton);
        buttonPanel.add(editTransactionButton);
        buttonPanel.add(deleteTransactionButton);
        buttonPanel.add(compenserButton);
        buttonPanel.add(rejeterButton);
        buttonPanel.add(refreshTransactionsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Statistiques des comptes
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        panel.add(createStatsPanel("Statistiques des Comptes", 
                                 "Total comptes:", totalComptesLabel,
                                 "Comptes actifs:", comptesActifsLabel,
                                 "Solde total:", soldeTotalLabel,
                                 "Comptes en découvert:", comptesEnDecouvertLabel), gbc);
        
        // Alertes d'échéances
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(createStatsPanel("Alertes d'Échéances",
                                 "Transactions échues:", transactionsEchuesLabel,
                                 "Prochainement échues:", prochainementEchuesLabel), gbc);
        
        return panel;
    }
    
    private JPanel createStatsPanel(String title, Object... items) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        for (int i = 0; i < items.length; i += 2) {
            gbc.gridx = 0; gbc.gridy = i / 2;
            JLabel keyLabel = new JLabel((String) items[i]);
            keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
            panel.add(keyLabel, gbc);
            
            gbc.gridx = 1;
            JLabel valueLabel = (JLabel) items[i + 1];
            valueLabel.setFont(valueLabel.getFont().deriveFont(14f));
            panel.add(valueLabel, gbc);
        }
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Événements des comptes
        addCompteButton.addActionListener(e -> showAddCompteDialog());
        editCompteButton.addActionListener(e -> showEditCompteDialog());
        deleteCompteButton.addActionListener(e -> deleteSelectedCompte());
        viewTransactionsButton.addActionListener(e -> showTransactionsForCompte());
        refreshComptesButton.addActionListener(e -> loadComptes());
        
        comptesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = comptesTable.getSelectedRow() != -1;
                editCompteButton.setEnabled(hasSelection);
                deleteCompteButton.setEnabled(hasSelection);
                viewTransactionsButton.setEnabled(hasSelection);
            }
        });
        
        // Événements des transactions
        addTransactionButton.addActionListener(e -> showAddTransactionDialog());
        editTransactionButton.addActionListener(e -> showEditTransactionDialog());
        deleteTransactionButton.addActionListener(e -> deleteSelectedTransaction());
        compenserButton.addActionListener(e -> compenserTransaction());
        rejeterButton.addActionListener(e -> rejeterTransaction());
        refreshTransactionsButton.addActionListener(e -> loadTransactions());
        
        transactionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = transactionsTable.getSelectedRow() != -1;
                editTransactionButton.setEnabled(hasSelection);
                deleteTransactionButton.setEnabled(hasSelection);
                
                // Activer compenser/rejeter seulement pour les transactions en attente
                if (hasSelection) {
                    TransactionBancaire selectedTransaction = transactionsTableModel.getTransactionAt(transactionsTable.getSelectedRow());
                    boolean canProcess = selectedTransaction.getStatut() == TransactionBancaire.StatutTransaction.EN_ATTENTE;
                    compenserButton.setEnabled(canProcess);
                    rejeterButton.setEnabled(canProcess);
                } else {
                    compenserButton.setEnabled(false);
                    rejeterButton.setEnabled(false);
                }
            }
        });
    }
    
    private void configureWindow() {
        setTitle("Gestion des Comptes Bancaires - Gestion Commerciale");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void loadData() {
        loadComptes();
        loadTransactions();
        updateDashboard();
    }
    
    private void loadComptes() {
        try {
            List<CompteBancaire> comptes = compteBancaireService.getAllComptes();
            comptesTableModel.setComptes(comptes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des comptes: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTransactions() {
        try {
            List<TransactionBancaire> transactions = transactionBancaireService.getAllTransactions();
            transactionsTableModel.setTransactions(transactions);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des transactions: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateDashboard() {
        try {
            CompteBancaireService.CompteStats stats = compteBancaireService.getStatistiques();
            
            totalComptesLabel.setText(stats.getTotalComptes().toString());
            comptesActifsLabel.setText(stats.getComptesActifs().toString());
            soldeTotalLabel.setText(CURRENCY_FORMAT.format(stats.getSoldeTotal()) + " DH");
            comptesEnDecouvertLabel.setText(stats.getComptesEnDecouvert().toString());
            
            // Couleur pour le solde total
            if (stats.getSoldeTotal().compareTo(BigDecimal.ZERO) < 0) {
                soldeTotalLabel.setForeground(Color.RED);
            } else {
                soldeTotalLabel.setForeground(new Color(0, 150, 0));
            }
            
            // Alertes d'échéances
            List<TransactionBancaire> echues = transactionBancaireService.getTransactionsEchues();
            List<TransactionBancaire> prochainement = transactionBancaireService.getTransactionsProchainementEchues(7);
            
            transactionsEchuesLabel.setText(String.valueOf(echues.size()));
            prochainementEchuesLabel.setText(String.valueOf(prochainement.size()));
            
            // Couleurs pour les alertes
            transactionsEchuesLabel.setForeground(echues.isEmpty() ? Color.BLACK : Color.RED);
            prochainementEchuesLabel.setForeground(prochainement.isEmpty() ? Color.BLACK : new Color(255, 140, 0));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la mise à jour du tableau de bord: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Actions pour les comptes
    private void showAddCompteDialog() {
        CompteEditDialog dialog = new CompteEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadComptes();
            updateDashboard();
        }
    }
    
    private void showEditCompteDialog() {
        int selectedRow = comptesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        CompteBancaire selectedCompte = comptesTableModel.getCompteAt(selectedRow);
        CompteEditDialog dialog = new CompteEditDialog(this, selectedCompte);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadComptes();
            updateDashboard();
        }
    }
    
    private void deleteSelectedCompte() {
        int selectedRow = comptesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        CompteBancaire selectedCompte = comptesTableModel.getCompteAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer le compte " + selectedCompte.getNumeroCompte() + " ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                compteBancaireService.delete(selectedCompte);
                loadComptes();
                updateDashboard();
                JOptionPane.showMessageDialog(this,
                    "Compte supprimé avec succès.",
                    "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showTransactionsForCompte() {
        int selectedRow = comptesTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        CompteBancaire selectedCompte = comptesTableModel.getCompteAt(selectedRow);
        TransactionsByCompteDialog dialog = new TransactionsByCompteDialog(this, selectedCompte);
        dialog.setVisible(true);
    }
    
    // Actions pour les transactions
    private void showAddTransactionDialog() {
        TransactionEditDialog dialog = new TransactionEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadTransactions();
            loadComptes(); // Mettre à jour les soldes
            updateDashboard();
        }
    }
    
    private void showEditTransactionDialog() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        TransactionBancaire selectedTransaction = transactionsTableModel.getTransactionAt(selectedRow);
        TransactionEditDialog dialog = new TransactionEditDialog(this, selectedTransaction);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadTransactions();
            loadComptes();
            updateDashboard();
        }
    }
    
    private void deleteSelectedTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        TransactionBancaire selectedTransaction = transactionsTableModel.getTransactionAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer la transaction " + selectedTransaction.getReference() + " ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                transactionBancaireService.deleteTransaction(selectedTransaction);
                loadTransactions();
                loadComptes();
                updateDashboard();
                JOptionPane.showMessageDialog(this,
                    "Transaction supprimée avec succès.",
                    "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void compenserTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        TransactionBancaire selectedTransaction = transactionsTableModel.getTransactionAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Marquer la transaction " + selectedTransaction.getReference() + " comme compensée ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                transactionBancaireService.marquerCompensee(selectedTransaction.getId());
                loadTransactions();
                JOptionPane.showMessageDialog(this,
                    "Transaction marquée comme compensée.",
                    "Opération réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void rejeterTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        TransactionBancaire selectedTransaction = transactionsTableModel.getTransactionAt(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Marquer la transaction " + selectedTransaction.getReference() + " comme rejetée ?\n" +
            "Le montant sera automatiquement annulé du compte.",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                transactionBancaireService.marquerRejetee(selectedTransaction.getId());
                loadTransactions();
                loadComptes();
                updateDashboard();
                JOptionPane.showMessageDialog(this,
                    "Transaction marquée comme rejetée et montant annulé.",
                    "Opération réussie", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Model pour la table des comptes
    private static class ComptesTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = {
            "ID", "Numéro Compte", "Banque", "Titulaire", "Type", "Solde (DH)", "Actif"
        };
        
        private List<CompteBancaire> comptes = List.of();
        
        public void setComptes(List<CompteBancaire> comptes) {
            this.comptes = comptes;
            fireTableDataChanged();
        }
        
        public CompteBancaire getCompteAt(int row) {
            return comptes.get(row);
        }
        
        @Override
        public int getRowCount() {
            return comptes.size();
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
            CompteBancaire compte = comptes.get(row);
            switch (column) {
                case 0: return compte.getId();
                case 1: return compte.getNumeroCompte();
                case 2: return compte.getNomBanque();
                case 3: return compte.getTitulaire();
                case 4: return compte.getTypeCompte().getLibelle();
                case 5: return compte.getSoldeActuel();
                case 6: return compte.isActive() ? "Oui" : "Non";
                default: return "";
            }
        }
    }
    
    // Model pour la table des transactions
    private static class TransactionsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = {
            "Référence", "Date", "Type", "Mode", "Montant (DH)", "Bénéficiaire", "Statut", "Échéance"
        };
        
        private List<TransactionBancaire> transactions = List.of();
        
        public void setTransactions(List<TransactionBancaire> transactions) {
            this.transactions = transactions;
            fireTableDataChanged();
        }
        
        public TransactionBancaire getTransactionAt(int row) {
            return transactions.get(row);
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
                case 0: return transaction.getReference();
                case 1: return transaction.getDateTransaction() != null ? 
                            transaction.getDateTransaction().format(DATE_FORMAT) : "";
                case 2: return transaction.getType().getLibelle();
                case 3: return transaction.getModePaiement().getLibelle();
                case 4: return transaction.getMontant();
                case 5: return transaction.getBeneficiaire();
                case 6: return transaction.getStatut().getLibelle();
                case 7: return transaction.getDateEcheance() != null ? 
                            transaction.getDateEcheance().format(DATE_FORMAT) : "";
                default: return "";
            }
        }
    }
    
    // Renderer pour les soldes avec couleurs
    private static class SoldeCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        public SoldeCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
        
        @Override
        protected void setValue(Object value) {
            if (value instanceof BigDecimal) {
                BigDecimal montant = (BigDecimal) value;
                setText(CURRENCY_FORMAT.format(montant) + " DH");
                
                if (montant.compareTo(BigDecimal.ZERO) < 0) {
                    setForeground(Color.RED);
                } else {
                    setForeground(new Color(0, 150, 0));
                }
            } else {
                super.setValue(value);
                setForeground(Color.BLACK);
            }
        }
    }
    
    // Renderer pour les montants des transactions
    private static class MontantCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        public MontantCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
        
        @Override
        protected void setValue(Object value) {
            if (value instanceof BigDecimal) {
                BigDecimal montant = (BigDecimal) value;
                setText(CURRENCY_FORMAT.format(montant) + " DH");
                setForeground(new Color(0, 100, 0));
            } else {
                super.setValue(value);
                setForeground(Color.BLACK);
            }
        }
    }
    
    // Renderer pour les statuts
    private static class StatutCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        
        @Override
        protected void setValue(Object value) {
            super.setValue(value);
            if (value != null) {
                String statut = value.toString();
                switch (statut) {
                    case "En attente":
                        setForeground(new Color(255, 140, 0));
                        break;
                    case "Compensée":
                        setForeground(new Color(0, 150, 0));
                        break;
                    case "Rejetée":
                    case "Annulée":
                        setForeground(Color.RED);
                        break;
                    default:
                        setForeground(Color.BLACK);
                        break;
                }
            }
        }
    }
}
