package com.gestioncommerciale.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * UI utility methods and configurations
 */
public class UIUtils {
    
    public static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    public static final Color SECONDARY_COLOR = new Color(76, 175, 80);
    public static final Color WARNING_COLOR = new Color(255, 152, 0);
    public static final Color ERROR_COLOR = new Color(244, 67, 54);
    public static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    
    public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    
    public static void configureUIDefaults() {
        try {
            // Set default font for all components
            setUIFont(new FontUIResource(DEFAULT_FONT));
            
            // Configure UI colors
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Button.focus", PRIMARY_COLOR);
            
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("TextField.caretForeground", Color.BLACK);
            
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.foreground", Color.BLACK);
            UIManager.put("Table.selectionBackground", PRIMARY_COLOR);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", Color.LIGHT_GRAY);
            
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("Panel.foreground", Color.BLACK);
            
        } catch (Exception e) {
            // Fallback to default UI
        }
    }
    
    private static void setUIFont(FontUIResource font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
    
    public static JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(DEFAULT_FONT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR);
    }
    
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR);
    }
    
    public static JButton createWarningButton(String text) {
        return createStyledButton(text, WARNING_COLOR);
    }
    
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, ERROR_COLOR);
    }
    
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            title,
            0,
            0,
            HEADER_FONT,
            PRIMARY_COLOR
        ));
        return panel;
    }
    
    public static void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showWarningMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Attention", JOptionPane.WARNING_MESSAGE);
    }
    
    public static boolean showConfirmDialog(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    
    public static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;
        window.setLocation(x, y);
    }
    
    public static ImageIcon createImageIcon(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(UIUtils.class.getResource(path));
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }
}
