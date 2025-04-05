package com.bookstore.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.bookstore.db.DatabaseConnection;

public class SellerPanel extends JPanel {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, authorField, genreField, priceField;
    private JLabel imageLabel;
    private File selectedImageFile = null;
    private String username;
    private RoleSwitcher roleSwitcher;

    public SellerPanel(String username, RoleSwitcher roleSwitcher) {
        this.username = username;
        this.roleSwitcher = roleSwitcher;

        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        titleField = new JTextField();
        authorField = new JTextField();
        genreField = new JTextField();
        priceField = new JTextField();
        imageLabel = new JLabel("No image selected");

        JButton browseBtn = new JButton("Browse Image");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = chooser.getSelectedFile();
                imageLabel.setText(selectedImageFile.getName());
            }
        });

        JButton addBtn = new JButton("Add Book");
        addBtn.addActionListener(e -> addBook());

        JButton deleteBtn = new JButton("Delete Book");
        deleteBtn.addActionListener(e -> deleteBook());

        JButton switchRoleBtn = new JButton("Switch to Buyer");
        switchRoleBtn.addActionListener(e -> roleSwitcher.switchRole());

        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author:"));
        inputPanel.add(authorField);
        inputPanel.add(new JLabel("Genre:"));
        inputPanel.add(genreField);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Image:"));
        inputPanel.add(browseBtn);
        inputPanel.add(imageLabel);
        inputPanel.add(addBtn);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Title", "Author", "Genre", "Price", "Image"}, 0);
        bookTable = new JTable(tableModel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JScrollPane scrollPane = new JScrollPane(bookTable);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(deleteBtn);
        bottomPanel.add(switchRoleBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadBooks();
    }

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String genre = genreField.getText().trim();
        double price;

        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price");
            return;
        }

        String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title, author, genre, price, image_path, seller_username) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, genre);
            ps.setDouble(4, price);
            ps.setString(5, imagePath);
            ps.setString(6, username);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book added!");
            clearFields();
            loadBooks();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a book to delete.");
            return;
        }

        String title = (String) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE title = ? AND seller_username = ?")) {
            ps.setString(1, title);
            ps.setString(2, username);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book deleted.");
            loadBooks();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBooks() {
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT title, author, genre, price, image_path FROM books WHERE seller_username = ?")) {
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getDouble("price"),
                    rs.getString("image_path")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        genreField.setText("");
        priceField.setText("");
        imageLabel.setText("No image selected");
        selectedImageFile = null;
    }
}
