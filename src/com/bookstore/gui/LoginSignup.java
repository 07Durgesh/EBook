package com.bookstore.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.bookstore.db.DatabaseConnection;

public class LoginSignup extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private BackgroundPanel backgroundPanel;
    private boolean isDarkMode = false;

    public LoginSignup() {
        setTitle("📚 Bookstore Login / Signup");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createSignupPanel(), "signup");

        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        JButton themeSwitch = createStyledButton("🌗 Switch Theme");
        themeSwitch.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            refreshUI();
        });
        topPanel.add(themeSwitch);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        // Add padding around the main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        centerPanel.add(mainPanel, gbc);

        backgroundPanel.add(topPanel, BorderLayout.NORTH);
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        add(backgroundPanel);
        setVisible(true);
    }

    private void refreshUI() {
        mainPanel.removeAll();
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createSignupPanel(), "signup");
        SwingUtilities.updateComponentTreeUI(this);
        backgroundPanel.repaint();
    }

    class BackgroundPanel extends JPanel {
        Random rand = new Random();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (isDarkMode) {
                g2.setColor(new Color(30, 30, 30));
                g2.fillRect(0, 0, w, h);
                for (int i = 0; i < 25; i++) {
                    g2.setColor(new Color(rand.nextInt(155) + 100, rand.nextInt(155) + 100, rand.nextInt(155) + 100, 50));
                    int x = rand.nextInt(w);
                    int y = rand.nextInt(h);
                    int width = 80 + rand.nextInt(100);
                    int height = 80 + rand.nextInt(100);
                    g2.fillRoundRect(x, y, width, height, 30, 30);
                }
            } else {
                g2.setColor(new Color(245, 245, 255));
                g2.fillRect(0, 0, w, h);
                for (int i = 0; i < 25; i++) {
                    g2.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 40));
                    int x = rand.nextInt(w);
                    int y = rand.nextInt(h);
                    int width = 80 + rand.nextInt(100);
                    int height = 80 + rand.nextInt(100);
                    g2.fillRoundRect(x, y, width, height, 30, 30);
                }
            }
        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = createFormPanel("Login");

        JTextField usernameField = createRoundedTextField();
        JPasswordField passwordField = createRoundedPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton goToSignupBtn = createLinkButton("New here? Create an account");

        // Use GridBagLayout for better control
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Add components with proper spacing
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridy = 3;
        panel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(loginBtn, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(goToSignupBtn, gbc);

        goToSignupBtn.addActionListener(e -> cardLayout.show(mainPanel, "signup"));

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password must not be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (password.length() < 8) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    openPanelByRole(username, role);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = createFormPanel("Signup");

        JTextField usernameField = createRoundedTextField();
        JTextField emailField = createRoundedTextField();
        JPasswordField passwordField = createRoundedPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"buyer", "seller"});
        JButton signupBtn = createStyledButton("Create Account");
        JButton backToLoginBtn = createLinkButton("Back to Login");

        // Use GridBagLayout for better control
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Add components with proper spacing
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridy = 1;
        panel.add(usernameField, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridy = 3;
        panel.add(emailField, gbc);

        gbc.gridy = 4;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridy = 5;
        panel.add(passwordField, gbc);

        gbc.gridy = 6;
        panel.add(new JLabel("Role:"), gbc);

        gbc.gridy = 7;
        panel.add(roleBox, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(signupBtn, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(backToLoginBtn, gbc);

        backToLoginBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        signupBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (password.length() < 8) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, role);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Signup successful! You can now login.");
                cardLayout.show(mainPanel, "login");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Signup failed. Username may be taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createFormPanel(String title) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(400, 500)); // Increased height to accommodate all components
        panel.setOpaque(true);
        panel.setBackground(isDarkMode ? new Color(40, 40, 40, 200) : new Color(245, 245, 255, 200));
        panel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 100, 200), 2),
                        title,
                        0,
                        0,
                        new Font("Arial", Font.BOLD, 20),
                        isDarkMode ? Color.WHITE : Color.BLACK
                ),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));
        return panel;
    }

    private JTextField createRoundedTextField() {
        JTextField field = new JTextField(20); // Set preferred columns
        styleTextField(field);
        return field;
    }

    private JPasswordField createRoundedPasswordField() {
        JPasswordField field = new JPasswordField(20); // Set preferred columns
        styleTextField(field);
        return field;
    }

    private void styleTextField(JTextComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(isDarkMode ? new Color(60, 60, 60) : Color.WHITE);
        field.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        field.setCaretColor(isDarkMode ? Color.WHITE : Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(isDarkMode ? new Color(100, 149, 237).darker() : new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(isDarkMode ? Color.CYAN : Color.BLUE.darker());
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void openPanelByRole(String username, String role) {
        SwingUtilities.invokeLater(() -> {
            JFrame roleFrame = new JFrame("Bookstore - " + role.toUpperCase());
            roleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            roleFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            JPanel rolePanel;
            if ("buyer".equalsIgnoreCase(role)) {
                rolePanel = new BuyerPanel(username, new RoleSwitcherImpl(roleFrame, username));
            } else {
                rolePanel = new SellerPanel(username, new RoleSwitcherImpl(roleFrame, username));
            }

            roleFrame.setContentPane(rolePanel);
            roleFrame.setVisible(true);
            dispose();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginSignup::new);
    }
}