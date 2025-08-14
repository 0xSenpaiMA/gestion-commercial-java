package com.gestioncommerciale.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.gestioncommerciale.model.Client;
import com.gestioncommerciale.service.StatisticsService;
import com.gestioncommerciale.service.StatisticsService.ClientRevenueData;
import com.gestioncommerciale.service.StatisticsService.RevenueData;
import com.gestioncommerciale.util.UIUtils;

/**
 * Statistics and Dashboard frame for business analytics
 */
public class StatisticsFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private final StatisticsService statisticsService;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Filter components
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<String> clientFilter;
    private JComboBox<String> cityFilter;
    private JComboBox<String> countryFilter;
    private JButton applyFiltersButton;
    private JButton resetFiltersButton;
    
    // Summary panels
    private JLabel totalRevenueLabel;
    private JLabel totalInvoicesLabel;
    private JLabel averageRevenueLabel;
    
    // Tables for different views
    private JTable dailyTable;
    private JTable monthlyTable;
    private JTable yearlyTable;
    private JTable clientTable;
    private JTable cityTable;
    private JTable countryTable;
    
    // Table models
    private DefaultTableModel dailyTableModel;
    private DefaultTableModel monthlyTableModel;
    private DefaultTableModel yearlyTableModel;
    private DefaultTableModel clientTableModel;
    private DefaultTableModel cityTableModel;
    private DefaultTableModel countryTableModel;
    
    // Navigation buttons
    private JButton dailyViewButton;
    private JButton monthlyViewButton;
    private JButton yearlyViewButton;
    private JButton clientViewButton;
    private JButton cityViewButton;
    private JButton countryViewButton;
    
    // Current filter values
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;
    
    public StatisticsFrame() {
        this.statisticsService = new StatisticsService();
        
        // Initialize default date range (last 30 days)
        this.currentEndDate = LocalDate.now();
        this.currentStartDate = currentEndDate.minusDays(30);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadInitialData();
        configureWindow();
    }
    
    private void initializeComponents() {
        // Initialize card layout for different views
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Initialize filter components
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        startDateField = new JTextField(currentStartDate.format(formatter), 10);
        endDateField = new JTextField(currentEndDate.format(formatter), 10);
        
        clientFilter = new JComboBox<>();
        cityFilter = new JComboBox<>();
        countryFilter = new JComboBox<>();
        
        applyFiltersButton = new JButton("Appliquer les filtres");
        resetFiltersButton = new JButton("Réinitialiser");
        
        // Initialize summary labels
        totalRevenueLabel = new JLabel("0,00 €", SwingConstants.CENTER);
        totalRevenueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        totalRevenueLabel.setForeground(new Color(46, 125, 50));
        
        totalInvoicesLabel = new JLabel("0", SwingConstants.CENTER);
        totalInvoicesLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        averageRevenueLabel = new JLabel("0,00 €", SwingConstants.CENTER);
        averageRevenueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        
        // Initialize navigation buttons
        dailyViewButton = new JButton("Vue Journalière");
        monthlyViewButton = new JButton("Vue Mensuelle");
        yearlyViewButton = new JButton("Vue Annuelle");
        clientViewButton = new JButton("Par Client");
        cityViewButton = new JButton("Par Ville");
        countryViewButton = new JButton("Par Pays");
        
        // Initialize tables and models
        initializeTables();
    }
    
    private void initializeTables() {
        // Daily revenue table
        String[] dailyColumns = {"Date", "Chiffre d'Affaires", "Nombre de Factures"};
        dailyTableModel = new DefaultTableModel(dailyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dailyTable = new JTable(dailyTableModel);
        dailyTable.setRowHeight(25);
        
        // Monthly revenue table
        String[] monthlyColumns = {"Mois", "Chiffre d'Affaires", "Nombre de Factures"};
        monthlyTableModel = new DefaultTableModel(monthlyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        monthlyTable = new JTable(monthlyTableModel);
        monthlyTable.setRowHeight(25);
        
        // Yearly revenue table
        String[] yearlyColumns = {"Année", "Chiffre d'Affaires", "Nombre de Factures"};
        yearlyTableModel = new DefaultTableModel(yearlyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        yearlyTable = new JTable(yearlyTableModel);
        yearlyTable.setRowHeight(25);
        
        // Client revenue table
        String[] clientColumns = {"Client", "Chiffre d'Affaires", "Nombre de Factures", "Ville", "Pays"};
        clientTableModel = new DefaultTableModel(clientColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clientTable = new JTable(clientTableModel);
        clientTable.setRowHeight(25);
        
        // City revenue table
        String[] cityColumns = {"Ville", "Chiffre d'Affaires", "Nombre de Factures"};
        cityTableModel = new DefaultTableModel(cityColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cityTable = new JTable(cityTableModel);
        cityTable.setRowHeight(25);
        
        // Country revenue table
        String[] countryColumns = {"Pays", "Chiffre d'Affaires", "Nombre de Factures"};
        countryTableModel = new DefaultTableModel(countryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        countryTable = new JTable(countryTableModel);
        countryTable.setRowHeight(25);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Main content area
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Filters panel
        JPanel filtersPanel = createFiltersPanel();
        mainPanel.add(filtersPanel, BorderLayout.NORTH);
        
        // Summary panel
        JPanel summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel, BorderLayout.CENTER);
        
        // Navigation and content panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel navigationPanel = createNavigationPanel();
        bottomPanel.add(navigationPanel, BorderLayout.NORTH);
        bottomPanel.add(createContentPanel(), BorderLayout.CENTER);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Tableau de Bord - Statistiques");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(new Color(33, 150, 243));
        panel.add(titleLabel);
        
        return panel;
    }
    
    private JPanel createFiltersPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Filtres"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Date filters
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Date début:"), gbc);
        gbc.gridx = 1;
        panel.add(startDateField, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("Date fin:"), gbc);
        gbc.gridx = 3;
        panel.add(endDateField, gbc);
        
        // Client filter
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 1;
        panel.add(clientFilter, gbc);
        
        // City filter
        gbc.gridx = 2;
        panel.add(new JLabel("Ville:"), gbc);
        gbc.gridx = 3;
        panel.add(cityFilter, gbc);
        
        // Country filter
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Pays:"), gbc);
        gbc.gridx = 1;
        panel.add(countryFilter, gbc);
        
        // Buttons
        gbc.gridx = 2; gbc.gridy = 2;
        panel.add(applyFiltersButton, gbc);
        gbc.gridx = 3;
        panel.add(resetFiltersButton, gbc);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Total revenue card
        JPanel revenueCard = createSummaryCard("Chiffre d'Affaires Total", totalRevenueLabel, new Color(76, 175, 80));
        panel.add(revenueCard);
        
        // Total invoices card
        JPanel invoicesCard = createSummaryCard("Nombre de Factures", totalInvoicesLabel, new Color(33, 150, 243));
        panel.add(invoicesCard);
        
        // Average revenue card
        JPanel averageCard = createSummaryCard("Chiffre d'Affaires Moyen", averageRevenueLabel, new Color(255, 152, 0));
        panel.add(averageCard);
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setForeground(Color.GRAY);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        panel.add(dailyViewButton);
        panel.add(monthlyViewButton);
        panel.add(yearlyViewButton);
        panel.add(clientViewButton);
        panel.add(cityViewButton);
        panel.add(countryViewButton);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        // Add all table views to the card panel
        contentPanel.add(createTablePanel(dailyTable, "Vue Journalière"), "daily");
        contentPanel.add(createTablePanel(monthlyTable, "Vue Mensuelle"), "monthly");
        contentPanel.add(createTablePanel(yearlyTable, "Vue Annuelle"), "yearly");
        contentPanel.add(createTablePanel(clientTable, "Revenus par Client"), "client");
        contentPanel.add(createTablePanel(cityTable, "Revenus par Ville"), "city");
        contentPanel.add(createTablePanel(countryTable, "Revenus par Pays"), "country");
        
        return contentPanel;
    }
    
    private JPanel createTablePanel(JTable table, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(title));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Filter buttons
        applyFiltersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        
        resetFiltersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFilters();
            }
        });
        
        // Navigation buttons
        dailyViewButton.addActionListener(e -> showDailyView());
        monthlyViewButton.addActionListener(e -> showMonthlyView());
        yearlyViewButton.addActionListener(e -> showYearlyView());
        clientViewButton.addActionListener(e -> showClientView());
        cityViewButton.addActionListener(e -> showCityView());
        countryViewButton.addActionListener(e -> showCountryView());
    }
    
    private void loadInitialData() {
        try {
            // Load filter options
            loadFilterOptions();
            
            // Load initial statistics
            updateSummaryData();
            showDailyView();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadFilterOptions() {
        // Load clients
        List<Client> clients = statisticsService.getAllClients();
        DefaultComboBoxModel<String> clientModel = new DefaultComboBoxModel<>();
        clientModel.addElement("Tous les clients");
        for (Client client : clients) {
            String clientName = client.getNom() + (client.getPrenom() != null ? " " + client.getPrenom() : "");
            clientModel.addElement(clientName);
        }
        clientFilter.setModel(clientModel);
        
        // Load cities
        List<String> cities = statisticsService.getAllCities();
        DefaultComboBoxModel<String> cityModel = new DefaultComboBoxModel<>();
        cityModel.addElement("Toutes les villes");
        for (String city : cities) {
            cityModel.addElement(city);
        }
        cityFilter.setModel(cityModel);
        
        // Load countries
        List<String> countries = statisticsService.getAllCountries();
        DefaultComboBoxModel<String> countryModel = new DefaultComboBoxModel<>();
        countryModel.addElement("Tous les pays");
        for (String country : countries) {
            countryModel.addElement(country);
        }
        countryFilter.setModel(countryModel);
    }
    
    private void updateSummaryData() {
        try {
            BigDecimal totalRevenue = statisticsService.getTotalRevenue(currentStartDate, currentEndDate);
            long totalInvoices = statisticsService.getTotalInvoiceCount(currentStartDate, currentEndDate);
            
            BigDecimal averageRevenue = BigDecimal.ZERO;
            if (totalInvoices > 0) {
                averageRevenue = totalRevenue.divide(BigDecimal.valueOf(totalInvoices), 2, RoundingMode.HALF_UP);
            }
            
            totalRevenueLabel.setText(String.format("%,.2f €", totalRevenue));
            totalInvoicesLabel.setText(String.valueOf(totalInvoices));
            averageRevenueLabel.setText(String.format("%,.2f €", averageRevenue));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la mise à jour des données: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void applyFilters() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            currentStartDate = LocalDate.parse(startDateField.getText(), formatter);
            currentEndDate = LocalDate.parse(endDateField.getText(), formatter);
            
            updateSummaryData();
            
            // Refresh current view
            String currentCard = getCurrentCard();
            switch (currentCard) {
                case "daily":
                    showDailyView();
                    break;
                case "monthly":
                    showMonthlyView();
                    break;
                case "yearly":
                    showYearlyView();
                    break;
                case "client":
                    showClientView();
                    break;
                case "city":
                    showCityView();
                    break;
                case "country":
                    showCountryView();
                    break;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Format de date invalide. Utilisez le format dd/MM/yyyy",
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetFilters() {
        currentEndDate = LocalDate.now();
        currentStartDate = currentEndDate.minusDays(30);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        startDateField.setText(currentStartDate.format(formatter));
        endDateField.setText(currentEndDate.format(formatter));
        
        clientFilter.setSelectedIndex(0);
        cityFilter.setSelectedIndex(0);
        countryFilter.setSelectedIndex(0);
        
        applyFilters();
    }
    
    private String getCurrentCard() {
        // This is a simplified way to track current card
        return "daily"; // Default fallback
    }
    
    private void showDailyView() {
        try {
            List<RevenueData> dailyData = statisticsService.getDailyRevenue(currentStartDate, currentEndDate);
            updateTableModel(dailyTableModel, dailyData);
            cardLayout.show(contentPanel, "daily");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données journalières: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showMonthlyView() {
        try {
            List<RevenueData> monthlyData = statisticsService.getMonthlyRevenue(currentStartDate, currentEndDate);
            updateTableModel(monthlyTableModel, monthlyData);
            cardLayout.show(contentPanel, "monthly");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données mensuelles: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showYearlyView() {
        try {
            List<RevenueData> yearlyData = statisticsService.getYearlyRevenue();
            updateTableModel(yearlyTableModel, yearlyData);
            cardLayout.show(contentPanel, "yearly");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données annuelles: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showClientView() {
        try {
            List<ClientRevenueData> clientData = statisticsService.getRevenueByClient(currentStartDate, currentEndDate);
            updateClientTableModel(clientTableModel, clientData);
            cardLayout.show(contentPanel, "client");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données par client: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showCityView() {
        try {
            List<RevenueData> cityData = statisticsService.getRevenueByCity(currentStartDate, currentEndDate);
            updateTableModel(cityTableModel, cityData);
            cardLayout.show(contentPanel, "city");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données par ville: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showCountryView() {
        try {
            List<RevenueData> countryData = statisticsService.getRevenueByCountry(currentStartDate, currentEndDate);
            updateTableModel(countryTableModel, countryData);
            cardLayout.show(contentPanel, "country");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des données par pays: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTableModel(DefaultTableModel tableModel, List<RevenueData> data) {
        tableModel.setRowCount(0);
        for (RevenueData item : data) {
            Object[] row = {
                item.getPeriod(),
                String.format("%,.2f €", item.getRevenue()),
                item.getInvoiceCount()
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateClientTableModel(DefaultTableModel tableModel, List<ClientRevenueData> data) {
        tableModel.setRowCount(0);
        for (ClientRevenueData item : data) {
            Object[] row = {
                item.getClientName(),
                String.format("%,.2f €", item.getTotalRevenue()),
                item.getInvoiceCount(),
                item.getCity() != null ? item.getCity() : "N/A",
                item.getCountry() != null ? item.getCountry() : "N/A"
            };
            tableModel.addRow(row);
        }
    }
    
    private void configureWindow() {
        setTitle("Statistiques - Gestion Commerciale");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Set application icon if available
        UIUtils.setApplicationIcon(this);
    }
}
