package com.bookstore.gui;

import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.bookstore.db.DatabaseConnection;

public class RoleSwitcherImpl implements RoleSwitcher {
    private JFrame currentFrame;
    private String username;

    public RoleSwitcherImpl(JFrame currentFrame, String username) {
        this.currentFrame = currentFrame;
        this.username = username;
    }

    @Override
    public void switchRole() {
        // Create authentication dialog
        JPanel authPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField(username);
        JPasswordField passwordField = new JPasswordField();

        authPanel.add(new JLabel("Username:"));
        authPanel.add(usernameField);
        authPanel.add(new JLabel("Password:"));
        authPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(currentFrame, authPanel,
                "Switch Role - Authentication Required", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT role FROM users WHERE username = ? AND password = ?")) {

                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String currentRole = rs.getString("role");
                    String newRole = "buyer".equals(currentRole) ? "seller" : "buyer";

                    currentFrame.dispose();

                    JFrame newFrame = new JFrame("Bookstore - " + newRole.toUpperCase());
                    newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    newFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                    JPanel panel;
                    if ("buyer".equals(newRole)) {
                        panel = new BuyerPanel(username, new RoleSwitcherImpl(newFrame, username));
                    } else {
                        panel = new SellerPanel(username, new RoleSwitcherImpl(newFrame, username));
                    }

                    newFrame.setContentPane(panel);
                    newFrame.setVisible(true);

                } else {
                    JOptionPane.showMessageDialog(currentFrame, "Authentication failed. Please try again.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(currentFrame, "Error during authentication.");
            }
        }
    }
}