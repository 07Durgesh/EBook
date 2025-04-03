package com.bookstore.gui;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.bookstore.db.DatabaseConnection;

public class MainBookstoreWindow extends JFrame {
    private JTable bookTable;
    private JTextField searchField;
    private JComboBox<String> genreFilter;
    private JButton addToCartButton;
    private DefaultTableModel tableModel;

    public MainBookstoreWindow() {
        setTitle("Bookstore");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search and Filter Panel
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(10);
        filterPanel.add(searchField);
        
        filterPanel.add(new JLabel("Genre:"));
        genreFilter = new JComboBox<>(new String[]{"All", "Fiction", "Non-Fiction", "Science", "Fantasy"});
        filterPanel.add(genreFilter);
        
        JButton searchButton = new JButton("Search");
        filterPanel.add(searchButton);
        
        add(filterPanel, BorderLayout.NORTH);

        // Book Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "Price"}, 0);
        bookTable = new JTable(tableModel);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Add to Cart Button
        addToCartButton = new JButton("Add to Cart");
        add(addToCartButton, BorderLayout.SOUTH);

        // Load Books
        loadBooks(null, "All");

        // Event Listeners
        searchButton.addActionListener(e -> loadBooks(searchField.getText(), (String) genreFilter.getSelectedItem()));
        addToCartButton.addActionListener(e -> addToCart());

        setVisible(true);
    }

    private void loadBooks(String search, String genre) {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM books WHERE title LIKE ?";
        if (!"All".equals(genre)) query += " AND genre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + (search == null ? "" : search) + "%");
            if (!"All".equals(genre)) ps.setString(2, genre);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("book_id"), rs.getString("title"), rs.getString("author"), rs.getString("genre"), rs.getDouble("price")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addToCart() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to add to cart.");
            return;
        }
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
        JOptionPane.showMessageDialog(this, bookTitle + " added to cart!");
    }

    public static void main(String[] args) {
        new MainBookstoreWindow();
    }
}
