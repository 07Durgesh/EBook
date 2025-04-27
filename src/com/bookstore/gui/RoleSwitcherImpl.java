package com.bookstore.gui;

import javax.swing.*;
import java.sql.*;
import com.bookstore.db.DatabaseConnection;

public class RoleSwitcherImpl implements RoleSwitcher {

    private final JPanel currentPanel;

    public RoleSwitcherImpl(JPanel currentPanel) {
        this.currentPanel = currentPanel;
    }

    @Override
    public JPanel switchRole(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT role FROM users WHERE username = ? AND password = ?")) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String assignedRole = rs.getString("role");

                // Determine the current panel and the requested role
                String currentRole = (currentPanel instanceof BuyerPanel) ? "buyer" : "seller";
                String requestedRole = "buyer".equals(currentRole) ? "seller" : "buyer";

                // Check if the requested role matches the assigned role
                if (!requestedRole.equalsIgnoreCase(assignedRole)) {
                    throw new IllegalStateException(
                            "Access denied. Your assigned role is '" + assignedRole +
                                    "'. You cannot switch to '" + requestedRole + "'."
                    );
                }

                // Return the appropriate panel based on the requested role
                return "buyer".equals(requestedRole) ?
                        new BuyerPanel(username) :
                        new SellerPanel(username);
            } else {
                throw new IllegalArgumentException("Authentication failed. Invalid username or password.");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error during role switching: " + ex.getMessage(), ex);
        }
    }
}