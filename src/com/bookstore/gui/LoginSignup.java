package com.bookstore.gui;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

import com.bookstore.db.DatabaseConnection;

public class LoginSignup extends JFrame implements RoleSwitcher {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public LoginSignup() {
        setTitle("Login & Signup");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createSignupPanel(), "signup");

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JButton goToSignupBtn = new JButton("Go to Signup");

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginBtn);
        panel.add(goToSignupBtn);

        goToSignupBtn.addActionListener(e -> cardLayout.show(mainPanel, "signup"));

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    JOptionPane.showMessageDialog(this, "Logged in as " + role);
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
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"buyer", "seller"});
        JButton signupBtn = new JButton("Signup");
        JButton backToLoginBtn = new JButton("Back to Login");

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        panel.add(signupBtn);
        panel.add(backToLoginBtn);

        backToLoginBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        signupBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, role);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Signup successful. You can now login.");
                cardLayout.show(mainPanel, "login");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Signup failed. Username might be taken.");
            }
        });

        return panel;
    }

    private void openPanelByRole(String username, String role) {
        getContentPane().removeAll();

        JPanel rolePanel;
        if ("buyer".equalsIgnoreCase(role)) {
            rolePanel = new BuyerPanel(username, this);
        } else {
            rolePanel = new SellerPanel(username, this);
        }

        getContentPane().add(rolePanel);
        revalidate();
        repaint();
    }

    @Override
    public void switchRole() {
        JPanel authPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        authPanel.add(new JLabel("Username:"));
        authPanel.add(userField);
        authPanel.add(new JLabel("Password:"));
        authPanel.add(passField);

        int option = JOptionPane.showConfirmDialog(this, authPanel, "Switch Role (Re-authenticate)", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    JOptionPane.showMessageDialog(this, "Switching to " + role);
                    openPanelByRole(username, role);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginSignup::new);
    }
}
