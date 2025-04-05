package com.bookstore.gui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.bookstore.db.DatabaseConnection;

public class BuyerPanel extends JPanel {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> genreComboBox;
    private String username;
    private RoleSwitcher roleSwitcher;

    public BuyerPanel(String username, RoleSwitcher roleSwitcher) {
        this.username = username;
        this.roleSwitcher = roleSwitcher;

        setLayout(new BorderLayout());

        // Top panel
        JPanel topPanel = new JPanel();
        searchField = new JTextField(10);
        genreComboBox = new JComboBox<>();

        JButton filterBtn = new JButton("Search");
        JButton switchRoleBtn = new JButton("Switch to Seller");
        switchRoleBtn.addActionListener(e -> roleSwitcher.switchRole());

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(new JLabel("Genre:"));
        topPanel.add(genreComboBox);
        topPanel.add(filterBtn);
        topPanel.add(switchRoleBtn);

        filterBtn.addActionListener(e -> loadBooks(searchField.getText(), (String) genreComboBox.getSelectedItem()));

        // Table
        tableModel = new DefaultTableModel(new String[]{"Title", "Author", "Genre", "Price", "Image"}, 0);
        bookTable = new JTable(tableModel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JScrollPane scrollPane = new JScrollPane(bookTable);

        // Load genres and books
        loadGenres();
        loadBooks(null, "All");

        // Image preview
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && bookTable.getSelectedRow() != -1) {
                String imagePath = (String) tableModel.getValueAt(bookTable.getSelectedRow(), 4);
                if (imagePath != null && !imagePath.isEmpty()) {
                    ImageIcon icon = new ImageIcon(imagePath);
                    Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    JOptionPane.showMessageDialog(this, new JLabel(new ImageIcon(scaled)), "Book Image", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadGenres() {
        genreComboBox.removeAllItems();
        genreComboBox.addItem("All");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT genre FROM books")) {
            while (rs.next()) {
                genreComboBox.addItem(rs.getString("genre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBooks(String search, String genre) {
        tableModel.setRowCount(0);
        String query = "SELECT title, author, genre, price, image_path FROM books WHERE title LIKE ?";
        if (!"All".equalsIgnoreCase(genre)) {
            query += " AND genre = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, "%" + (search == null ? "" : search) + "%");
            if (!"All".equalsIgnoreCase(genre)) {
                ps.setString(2, genre);
            }

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
}
