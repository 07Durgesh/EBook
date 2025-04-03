package com.bookstore.gui;

import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.bookstore.db.DatabaseConnection;

public class LoginSignup {
    public static Connection conn = DatabaseConnection.getConnection();
    
    public static void main(String[] args) {
        new LoginFrame();
    }
}

class LoginFrame extends JFrame {
    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton, signupButton;

    public LoginFrame() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        signupButton = new JButton("Signup");

        add(loginButton);
        add(signupButton);

        loginButton.addActionListener(e -> login());
        signupButton.addActionListener(e -> new SignupFrame());

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            String query = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = LoginSignup.conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login Successful as " + role);
                dispose();
                // Redirect to Admin/User Dashboard
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

class SignupFrame extends JFrame {
    JTextField usernameField, emailField;
    JPasswordField passwordField;
    JButton signupButton;

    public SignupFrame() {
        setTitle("Signup");
        setSize(300, 250);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        signupButton = new JButton("Signup");
        add(signupButton);

        signupButton.addActionListener(e -> signup());
        setVisible(true);
    }

    private void signup() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            String query = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, 'customer')";
            PreparedStatement ps = LoginSignup.conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Signup Successful! Please Login.");
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
