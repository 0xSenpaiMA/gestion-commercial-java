package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.gestioncommerciale.model.User;
import com.gestioncommerciale.service.AuthService;
import com.gestioncommerciale.util.UIUtils;

/**
 * Main application window with navigation menu and content area
 */
public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private JLabel statusLabel;
    private JLabel userLabel;
    
    public MainFrame() {
        initializeComponents();
        setupLayout();
        setupMenuBar();
        setupEventHandlers();
        configureWindow();
        showWelcomePanel();
    }
    
    private void initializeComponents() {
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        
        statusLabel = new JLabel("Prêt");
        statusLabel.setFont(UIUtils.DEFAULT_FONT);
        
        User currentUser = AuthService.getCurrentUser();
        userLabel = new JLabel("Utilisateur: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(UIUtils.DEFAULT_FONT);
        userLabel.setForeground(UIUtils.PRIMARY_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(userLabel, BorderLayout.EAST);
        
        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("Fichier");
        
        JMenuItem companyInfoItem = new JMenuItem("Informations Entreprise");
        companyInfoItem.addActionListener(e -> showCompanyInfoForm());
        fileMenu.add(companyInfoItem);
        
        fileMenu.addSeparator();
        
        JMenuItem logoutItem = new JMenuItem("Déconnexion");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);
        
        JMenuItem exitItem = new JMenuItem("Quitter");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        
        // Management Menu
        JMenu managementMenu = new JMenu("Gestion");
        
        JMenuItem usersItem = new JMenuItem("Utilisateurs");
        usersItem.addActionListener(e -> showUserManagement());
        managementMenu.add(usersItem);
        
        JMenuItem clientsItem = new JMenuItem("Clients/Fournisseurs");
        clientsItem.addActionListener(e -> showClientManagement());
        managementMenu.add(clientsItem);
        
        JMenuItem articlesItem = new JMenuItem("Articles");
        articlesItem.addActionListener(e -> showArticleManagement());
        managementMenu.add(articlesItem);
        
        JMenuItem contratsItem = new JMenuItem("Contrats d'Assistance");
        contratsItem.addActionListener(e -> showContratAssistanceManagement());
        managementMenu.add(contratsItem);
        
        // Documents Menu
        JMenu documentsMenu = new JMenu("Documents");
        
        JMenuItem quotationsItem = new JMenuItem("Devis");
        quotationsItem.addActionListener(e -> showQuotationManagement());
        documentsMenu.add(quotationsItem);
        
        JMenuItem deliveryNotesItem = new JMenuItem("Bons de livraison");
        deliveryNotesItem.addActionListener(e -> showDeliveryNoteManagement());
        documentsMenu.add(deliveryNotesItem);
        
        JMenuItem invoicesItem = new JMenuItem("Factures");
        invoicesItem.addActionListener(e -> showInvoiceManagement());
        documentsMenu.add(invoicesItem);
        
        JMenuItem purchaseOrdersItem = new JMenuItem("Bons de commande");
        purchaseOrdersItem.addActionListener(e -> showPurchaseOrderManagement());
        documentsMenu.add(purchaseOrdersItem);
        
        // Statistics Menu
        JMenu statisticsMenu = new JMenu("Statistiques");
        
        JMenuItem dashboardItem = new JMenuItem("Tableau de bord");
        dashboardItem.addActionListener(e -> showDashboard());
        statisticsMenu.add(dashboardItem);
        
        JMenuItem reportsItem = new JMenuItem("Rapports");
        reportsItem.addActionListener(e -> showReports());
        statisticsMenu.add(reportsItem);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Outils");
        
        JMenuItem stockItem = new JMenuItem("Gestion de stock");
        stockItem.addActionListener(e -> showStockManagement());
        toolsMenu.add(stockItem);
        
        JMenuItem bankingItem = new JMenuItem("Comptes bancaires");
        bankingItem.addActionListener(e -> showBankingManagement());
        toolsMenu.add(bankingItem);
        
        JMenuItem exercicesItem = new JMenuItem("Exercices comptables");
        exercicesItem.addActionListener(e -> showExerciceManagement());
        toolsMenu.add(exercicesItem);
        
        // Configuration Menu
        JMenu configMenu = new JMenu("Configuration");
        
        JMenuItem emailTestItem = new JMenuItem("Tester configuration e-mail");
        emailTestItem.addActionListener(e -> showEmailTestDialog());
        configMenu.add(emailTestItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Aide");
        
        JMenuItem aboutItem = new JMenuItem("À propos");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        
        // Only show management menu if user has appropriate permissions
        if (AuthService.hasPermission(User.UserRole.EMPLOYEE)) {
            menuBar.add(managementMenu);
            menuBar.add(documentsMenu);
        }
        
        if (AuthService.hasPermission(User.UserRole.MANAGER)) {
            menuBar.add(statisticsMenu);
            menuBar.add(toolsMenu);
            menuBar.add(configMenu);
        }
        
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void configureWindow() {
        setTitle("Gestion Commerciale - " + AuthService.getCurrentUser().getFullName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        UIUtils.centerWindow(this);
    }
    
    private void showWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        
        JLabel welcomeLabel = new JLabel("Bienvenue dans Gestion Commerciale", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(UIUtils.PRIMARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Système de Gestion Commerciale Professionnel", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        textPanel.add(welcomeLabel);
        textPanel.add(subtitleLabel);
        
        welcomePanel.add(textPanel, BorderLayout.CENTER);
        
        contentPanel.removeAll();
        contentPanel.add(welcomePanel, "welcome");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "welcome");
        contentPanel.revalidate();
        contentPanel.repaint();
        
        updateStatus("Bienvenue " + AuthService.getCurrentUser().getFullName());
    }
    
    private void showCompanyInfoForm() {
        try {
            CompanyInfoManagementFrame companyFrame = new CompanyInfoManagementFrame();
            companyFrame.setVisible(true);
            updateStatus("Informations entreprise");
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de l'ouverture du formulaire d'entreprise: " + e.getMessage());
        }
    }
    
    private void showUserManagement() {
        if (!AuthService.hasPermission(User.UserRole.ADMIN)) {
            UIUtils.showWarningMessage(this, "Accès non autorisé. Droits administrateur requis.");
            return;
        }
        try {
            UserManagementFrame userFrame = new UserManagementFrame();
            userFrame.setVisible(true);
            updateStatus("Gestion des utilisateurs");
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de l'ouverture de la gestion des utilisateurs: " + e.getMessage());
        }
    }
    
    private void showClientManagement() {
        try {
            ClientManagementFrame clientFrame = new ClientManagementFrame();
            clientFrame.setVisible(true);
            updateStatus("Gestion des clients/fournisseurs");
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de l'ouverture de la gestion des clients: " + e.getMessage());
        }
    }
    
    private void showArticleManagement() {
        try {
            ArticleManagementFrame articleFrame = new ArticleManagementFrame();
            articleFrame.setVisible(true);
            updateStatus("Gestion des articles");
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de l'ouverture de la gestion des articles: " + e.getMessage());
        }
    }
    
    private void showContratAssistanceManagement() {
        try {
            ContratAssistanceManagementFrame contratFrame = new ContratAssistanceManagementFrame();
            contratFrame.setVisible(true);
            updateStatus("Gestion des contrats d'assistance");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ouverture de la gestion des contrats: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showExerciceManagement() {
        try {
            ExerciceManagementFrame exerciceFrame = new ExerciceManagementFrame();
            exerciceFrame.setVisible(true);
            updateStatus("Gestion des exercices comptables");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ouverture de la gestion des exercices: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showQuotationManagement() {
        try {
            updateStatus("Ouverture de la gestion des devis...");
            DevisManagementFrame devisFrame = new DevisManagementFrame();
            devisFrame.setVisible(true);
            updateStatus("Gestion des devis ouverte");
        } catch (Exception e) {
            UIUtils.showErrorMessage(this, "Erreur lors de l'ouverture de la gestion des devis: " + e.getMessage());
        }
    }
    
    private void showDeliveryNoteManagement() {
        updateStatus("Gestion des bons de livraison");
        BonLivraisonManagementFrame bonLivraisonFrame = new BonLivraisonManagementFrame();
        bonLivraisonFrame.setVisible(true);
    }
    
    private void showInvoiceManagement() {
        updateStatus("Gestion des factures");
        FactureManagementFrame factureFrame = new FactureManagementFrame();
        factureFrame.setVisible(true);
    }
    
    private void showPurchaseOrderManagement() {
        updateStatus("Gestion des bons de commande");
        BonCommandeManagementFrame frame = new BonCommandeManagementFrame();
        frame.setVisible(true);
    }
    
    private void showDashboard() {
        updateStatus("Tableau de bord");
        StatisticsFrame frame = new StatisticsFrame();
        frame.setVisible(true);
    }
    
    private void showReports() {
        // TODO: Implement reports
        updateStatus("Rapports");
        UIUtils.showSuccessMessage(this, "Rapports à implémenter");
    }
    
    private void showStockManagement() {
        updateStatus("Ouverture de la gestion de stock");
        StockManagementFrame stockFrame = new StockManagementFrame();
        stockFrame.setVisible(true);
    }
    
    private void showBankingManagement() {
        // TODO: Implement banking management
        updateStatus("Gestion des comptes bancaires");
        UIUtils.showSuccessMessage(this, "Gestion des comptes bancaires à implémenter");
    }
    
    private void showAboutDialog() {
        String aboutText = "<html><center>" +
                "<h2>Gestion Commerciale</h2>" +
                "<p>Version 1.0.0</p>" +
                "<p>Système de Gestion Commerciale Professionnel</p>" +
                "<p>Développé avec Java Swing</p>" +
                "<br/>" +
                "<p>© 2025 - Tous droits réservés</p>" +
                "</center></html>";
        
        JOptionPane.showMessageDialog(this, aboutText, "À propos", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showEmailTestDialog() {
        EmailTestDialog dialog = new EmailTestDialog(this);
        dialog.setVisible(true);
    }
    
    private void logout() {
        if (UIUtils.showConfirmDialog(this, "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            AuthService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
    
    private void exitApplication() {
        if (UIUtils.showConfirmDialog(this, "Êtes-vous sûr de vouloir quitter l'application ?")) {
            AuthService.logout();
            System.exit(0);
        }
    }
    
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
}
