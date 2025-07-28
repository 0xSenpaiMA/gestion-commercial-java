package com.gestioncommerciale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gestioncommerciale.config.DatabaseConfig;
import com.gestioncommerciale.util.UIUtils;
import com.gestioncommerciale.view.LoginFrame;

/**
 * Main application entry point for Gestion Commerciale
 * Professional Java Swing Desktop Application for Commercial Management
 */
public class GestionCommercialeApp {
    private static final Logger logger = LoggerFactory.getLogger(GestionCommercialeApp.class);
    
    public static void main(String[] args) {
        try {
            // Set system look and feel for better UI (simple fallback approach)
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            try {
                // Fallback to Metal look and feel
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (Exception ex) {
                logger.warn("Could not set look and feel, using default", ex);
            }
        }
        
        try {
            // Set application properties
            System.setProperty("app.name", "Gestion Commerciale");
            System.setProperty("app.version", "1.0.0");
            
            // Configure UI defaults
            UIUtils.configureUIDefaults();
            
            // Initialize database
            DatabaseConfig.initialize();
            
            // Ensure admin user exists
            com.gestioncommerciale.service.AuthService.initializeDefaultUser();
            
            // Start application on EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    // Show splash screen
                    showSplashScreen();
                    
                    // Start login window
                    new LoginFrame().setVisible(true);
                    
                } catch (Exception e) {
                    logger.error("Error starting application", e);
                    JOptionPane.showMessageDialog(null, 
                        "Erreur lors du démarrage de l'application: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            });
            
        } catch (Exception e) {
            logger.error("Fatal error during application startup", e);
            JOptionPane.showMessageDialog(null, 
                "Erreur fatale: " + e.getMessage(),
                "Erreur Fatale", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        splash.setSize(400, 300);
        splash.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Gestion Commerciale", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(0, 102, 204));
        
        JLabel subtitle = new JLabel("Système de Gestion Commerciale Professionnel", JLabel.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(Color.GRAY);
        
        JLabel version = new JLabel("Version 1.0.0", JLabel.CENTER);
        version.setFont(new Font("Arial", Font.PLAIN, 10));
        version.setForeground(Color.GRAY);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Chargement...");
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(version, BorderLayout.NORTH);
        southPanel.add(progressBar, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        splash.add(panel);
        splash.setVisible(true);
        
        // Show splash for 2 seconds
        Timer timer = new Timer(2000, e -> splash.dispose());
        timer.setRepeats(false);
        timer.start();
        
        try {
            Thread.sleep(2100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
