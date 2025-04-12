package com.bookstore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.bookstore.db.DatabaseConnection;

public class SellerPanel extends JPanel {
    private final JTextField titleField, authorField, priceField, customGenreField;
    private final JLabel imageLabel;
    private final List<String> imagePaths;
    private final JTable bookTable;
    private final DefaultTableModel tableModel;
    private final String username;
    private final RoleSwitcher roleSwitcher;
    private final JCheckBox fictionCB, nonFictionCB, mysteryCB, scifiCB, romanceCB, fantasyCB, historyCB, horrorCB, bioCB;
    private final JPanel genrePanel;

    public SellerPanel(String username, RoleSwitcher roleSwitcher) {
        this.username = username;
        this.roleSwitcher = roleSwitcher;
        this.imagePaths = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        // Initialize components
        titleField = createStyledTextField(20);
        authorField = createStyledTextField(20);
        priceField = createStyledTextField(20);
        customGenreField = createStyledTextField(15);
        imageLabel = new JLabel("No images selected");
        bookTable = new JTable();
        tableModel = new DefaultTableModel();
        genrePanel = new JPanel(new GridLayout(0, 3, 8, 8));

        fictionCB = createStyledCheckbox("Fiction");
        nonFictionCB = createStyledCheckbox("Non-Fiction");
        mysteryCB = createStyledCheckbox("Mystery");
        scifiCB = createStyledCheckbox("Sci-Fi");
        romanceCB = createStyledCheckbox("Romance");
        fantasyCB = createStyledCheckbox("Fantasy");
        historyCB = createStyledCheckbox("History");
        horrorCB = createStyledCheckbox("Horror");
        bioCB = createStyledCheckbox("Biography");

        // Add document filter to price field to allow only numbers and decimal point
        ((AbstractDocument) priceField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string.matches("[0-9.]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text.matches("[0-9.]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        createFormComponents();
        createBookTable();
        createButtonsPanel();
        loadBooks();
    }

    private void createFormComponents() {
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(new Color(245, 245, 245));

        // Left Panel - Book Information
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(245, 245, 245));
        leftPanel.setBorder(createTitledBorder("Book Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormField(leftPanel, gbc, "Title:", titleField, 0);
        addFormField(leftPanel, gbc, "Author:", authorField, 1);
        addFormField(leftPanel, gbc, "Price (Rs):", priceField, 2);

        // Right Panel - Book Details
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(245, 245, 245));
        rightPanel.setBorder(createTitledBorder("Book Details"));

        // Genre Panel
        genrePanel.setBorder(createTitledBorder("Select Genres", 12));
        genrePanel.setBackground(new Color(245, 245, 245));

        for (JCheckBox cb : new JCheckBox[]{fictionCB, nonFictionCB, mysteryCB, scifiCB,
                romanceCB, fantasyCB, historyCB, horrorCB, bioCB}) {
            genrePanel.add(cb);
        }

        // Custom Genre
        JPanel customGenrePanel = new JPanel(new BorderLayout(8, 0));
        JButton addGenreBtn = new JButton("+ Add");
        styleButton(addGenreBtn, new Color(70, 130, 180), 12);
        addGenreBtn.setPreferredSize(new Dimension(80, 30));
        addGenreBtn.addActionListener(e -> {
            String customGenre = customGenreField.getText().trim();
            if (customGenre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a genre name", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Component comp : genrePanel.getComponents()) {
                if (comp instanceof JCheckBox && ((JCheckBox)comp).getText().equalsIgnoreCase(customGenre)) {
                    JOptionPane.showMessageDialog(this, "This genre already exists", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO genres (name) VALUES (?) ON DUPLICATE KEY UPDATE name=name";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, customGenre);
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            JCheckBox newGenreCB = createStyledCheckbox(customGenre);
            genrePanel.add(newGenreCB);
            genrePanel.revalidate();
            genrePanel.repaint();
            customGenreField.setText("");
            JOptionPane.showMessageDialog(this, "Genre '" + customGenre + "' added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        customGenrePanel.add(customGenreField, BorderLayout.CENTER);
        customGenrePanel.add(addGenreBtn, BorderLayout.EAST);

        addFormField(rightPanel, gbc, "Custom Genre:", customGenrePanel, 0);
        gbc.gridy = 1;
        rightPanel.add(genrePanel, gbc);

        // Image Upload
        JButton browseBtn = new JButton("Browse Images");
        styleButton(browseBtn, new Color(70, 130, 180), 12);
        browseBtn.setPreferredSize(new Dimension(150, 30));
        browseBtn.addActionListener(this::browseImages);

        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        imageLabel.setForeground(new Color(100, 100, 100));

        gbc.gridy = 2;
        rightPanel.add(browseBtn, gbc);
        gbc.gridy = 3;
        rightPanel.add(imageLabel, gbc);

        formPanel.add(leftPanel);
        formPanel.add(rightPanel);

        // Add Book Button
        JButton addBookBtn = new JButton("Add Book");
        styleButton(addBookBtn, new Color(60, 179, 113), 14);
        addBookBtn.setPreferredSize(new Dimension(200, 40));
        addBookBtn.addActionListener(e -> addBook());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setBackground(new Color(245, 245, 245));
        buttonWrapper.add(addBookBtn);
        topPanel.add(buttonWrapper, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
    }

    private void createBookTable() {
        tableModel.setDataVector(
                new Object[][]{},
                new Object[]{"Title", "Author", "Genre", "Price", "Images", "book_id"}
        );
        tableModel.setColumnCount(6);

        bookTable.setModel(tableModel);
        bookTable.removeColumn(bookTable.getColumnModel().getColumn(5)); // Hide book_id

        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookTable.setRowHeight(30);
        bookTable.setSelectionBackground(new Color(220, 240, 255));
        bookTable.setSelectionForeground(Color.BLACK);
        bookTable.setGridColor(new Color(220, 220, 220));
        bookTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JTableHeader header = bookTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        JScrollPane tableScroll = new JScrollPane(bookTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        add(tableScroll, BorderLayout.CENTER);
    }

    private void createButtonsPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton deleteBtn = new JButton("Delete Book(s)");
        styleButton(deleteBtn, new Color(220, 20, 60), 13);
        deleteBtn.setPreferredSize(new Dimension(140, 35));
        deleteBtn.addActionListener(e -> deleteBook());

        JButton notificationsBtn = new JButton("View Requests");
        styleButton(notificationsBtn, new Color(255, 165, 0), 13);
        notificationsBtn.setPreferredSize(new Dimension(140, 35));
        notificationsBtn.addActionListener(e -> showNotifications());

        JButton switchRoleBtn = new JButton("Switch to Buyer");
        styleButton(switchRoleBtn, new Color(100, 149, 237), 13);
        switchRoleBtn.setPreferredSize(new Dimension(140, 35));
        switchRoleBtn.addActionListener(e -> roleSwitcher.switchRole());

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(120, 120, 120), 13);
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            new LoginSignup();
        });

        buttonPanel.add(deleteBtn);
        buttonPanel.add(notificationsBtn);
        buttonPanel.add(switchRoleBtn);
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void browseImages(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Select Book Images");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                        name.endsWith(".png") || name.endsWith(".gif");
            }
            public String getDescription() {
                return "Image Files (*.jpg, *.jpeg, *.png, *.gif)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            if (files.length > 5) {
                JOptionPane.showMessageDialog(this, "Maximum 5 images allowed", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog previewDialog = new JDialog();
            previewDialog.setTitle("Image Preview");
            previewDialog.setLayout(new BorderLayout());
            previewDialog.setSize(600, 400);
            previewDialog.setLocationRelativeTo(this);
            previewDialog.setModal(true);

            JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            previewPanel.setBackground(Color.WHITE);
            JScrollPane scrollPane = new JScrollPane(previewPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            imagePaths.clear();

            for (File file : files) {
                try {
                    ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                    Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                    imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    previewPanel.add(imageLabel);
                    imagePaths.add(file.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading image: " + file.getName(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setBackground(Color.WHITE);

            JButton confirmBtn = new JButton("Confirm Selection");
            styleButton(confirmBtn, new Color(70, 130, 180));
            confirmBtn.addActionListener(ev -> {
                previewDialog.dispose();
                this.imageLabel.setText(imagePaths.size() + " images selected");
            });

            JButton cancelBtn = new JButton("Cancel");
            styleButton(cancelBtn, new Color(220, 20, 60));
            cancelBtn.addActionListener(ev -> {
                imagePaths.clear();
                previewDialog.dispose();
            });

            buttonPanel.add(confirmBtn);
            buttonPanel.add(cancelBtn);

            previewDialog.add(scrollPane, BorderLayout.CENTER);
            previewDialog.add(buttonPanel, BorderLayout.SOUTH);
            previewDialog.setVisible(true);
        }
    }

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String priceText = priceField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Additional validation for price
        if (priceText.equals(".") || priceText.startsWith(".") || priceText.endsWith(".")) {
            JOptionPane.showMessageDialog(this, "Invalid price format", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than 0", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> genres = new ArrayList<>();
        for (Component comp : genrePanel.getComponents()) {
            if (comp instanceof JCheckBox && ((JCheckBox)comp).isSelected()) {
                genres.add(((JCheckBox)comp).getText());
            }
        }

        if (genres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one genre", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String genreString = String.join(", ", genres);
        String imagePath = String.join(";", imagePaths);

        try (Connection conn = DatabaseConnection.getConnection()) {
            int sellerId = getSellerId(username);
            if (sellerId == -1) {
                JOptionPane.showMessageDialog(this, "Error: Seller not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String query = "INSERT INTO books (title, author, genre, price, image_path, seller_id, is_sold) " +
                    "VALUES (?, ?, ?, ?, ?, ?, FALSE)";

            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, genreString);
            ps.setDouble(4, price);
            ps.setString(5, imagePath);
            ps.setInt(6, sellerId);

            ps.executeUpdate();

            // Get the generated book_id
            ResultSet generatedKeys = ps.getGeneratedKeys();
            int bookId = -1;
            if (generatedKeys.next()) {
                bookId = generatedKeys.getInt(1);
            }

            // Insert genres into book_genres table
            for (String genre : genres) {
                int genreId = getGenreId(conn, genre);
                if (genreId != -1) {
                    String genreQuery = "INSERT INTO book_genres (book_id, genre_id) VALUES (?, ?)";
                    try (PreparedStatement genrePs = conn.prepareStatement(genreQuery)) {
                        genrePs.setInt(1, bookId);
                        genrePs.setInt(2, genreId);
                        genrePs.executeUpdate();
                    }
                }
            }

            // Reset form
            titleField.setText("");
            authorField.setText("");
            priceField.setText("");
            customGenreField.setText("");
            imagePaths.clear();
            imageLabel.setText("No images selected");
            for (Component comp : genrePanel.getComponents()) {
                if (comp instanceof JCheckBox) {
                    ((JCheckBox)comp).setSelected(false);
                }
            }

            loadBooks();
            JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getGenreId(Connection conn, String genreName) throws SQLException {
        String query = "SELECT genre_id FROM genres WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, genreName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("genre_id");
            }
        }
        return -1;
    }

    private void deleteBook() {
        int[] selectedRows = bookTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one book to delete", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = selectedRows.length == 1 ?
                "Delete the selected book?" :
                "Delete all " + selectedRows.length + " selected books?";

        int confirm = JOptionPane.showConfirmDialog(this,
                message,
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int sellerId = getSellerId(username);
            if (sellerId == -1) {
                JOptionPane.showMessageDialog(this, "Error: Seller not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int deletedCount = 0;
            for (int viewRow : selectedRows) {
                int modelRow = bookTable.convertRowIndexToModel(viewRow);
                int bookId = (int) tableModel.getValueAt(modelRow, 5);

                try {
                    // Delete related requests first
                    String deleteRequestsQuery = "DELETE FROM book_requests WHERE book_id=?";
                    try (PreparedStatement psRequests = conn.prepareStatement(deleteRequestsQuery)) {
                        psRequests.setInt(1, bookId);
                        psRequests.executeUpdate();
                    }

                    // Then delete the book
                    String deleteBookQuery = "DELETE FROM books WHERE book_id=? AND seller_id=?";
                    try (PreparedStatement psBook = conn.prepareStatement(deleteBookQuery)) {
                        psBook.setInt(1, bookId);
                        psBook.setInt(2, sellerId);
                        deletedCount += psBook.executeUpdate();
                    }
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            }

            conn.commit();

            if (deletedCount > 0) {
                loadBooks();
                JOptionPane.showMessageDialog(this,
                        deletedCount + " book(s) deleted successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No books were deleted",
                        "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showNotifications() {
        int sellerId = getSellerId(username);
        if (sellerId == -1) {
            JOptionPane.showMessageDialog(this, "Error: Seller not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT br.request_id, b.title, b.author, b.price, u.username AS buyer_username " +
                    "FROM book_requests br " +
                    "JOIN books b ON br.book_id = b.book_id " +
                    "JOIN users u ON br.requested_by = u.id " +
                    "WHERE b.seller_id = ? AND br.status = 'pending'";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, sellerId);
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No pending book requests", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JPanel notificationPanel = new JPanel();
            notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
            notificationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            notificationPanel.setBackground(Color.WHITE);

            while (rs.next()) {
                int requestId = rs.getInt("request_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                double price = rs.getDouble("price");
                String buyer = rs.getString("buyer_username");

                JPanel requestPanel = new JPanel(new BorderLayout());
                requestPanel.setBorder(createTitledBorder("Request #" + requestId, 12));
                requestPanel.setBackground(new Color(245, 245, 245));
                requestPanel.setMaximumSize(new Dimension(450, 150));

                JTextArea infoArea = new JTextArea(
                        "Book: " + title + "\n" +
                                "Author: " + author + "\n" +
                                "Price: Rs" + String.format("%.2f", price) + "\n" +
                                "Requested by: " + buyer
                );
                infoArea.setEditable(false);
                infoArea.setBackground(requestPanel.getBackground());
                infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
                buttonPanel.setBackground(requestPanel.getBackground());

                JButton acceptBtn = new JButton("Accept");
                styleButton(acceptBtn, new Color(60, 179, 113), 12);
                acceptBtn.addActionListener(e -> processRequest(requestId, true));

                JButton declineBtn = new JButton("Decline");
                styleButton(declineBtn, new Color(220, 20, 60), 12);
                declineBtn.addActionListener(e -> processRequest(requestId, false));

                buttonPanel.add(acceptBtn);
                buttonPanel.add(declineBtn);

                requestPanel.add(infoArea, BorderLayout.CENTER);
                requestPanel.add(buttonPanel, BorderLayout.SOUTH);

                notificationPanel.add(requestPanel);
                notificationPanel.add(Box.createVerticalStrut(10));
            }

            JScrollPane scrollPane = new JScrollPane(notificationPanel);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane, "Book Requests", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading requests: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processRequest(int requestId, boolean accept) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String selectQuery = "SELECT book_id, requested_by FROM book_requests WHERE request_id = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, requestId);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    int bookId = rs.getInt("book_id");
                    int buyerId = rs.getInt("requested_by");

                    if (accept) {
                        // Update book to sold and link to buyer
                        String updateBookQuery = "UPDATE books SET is_sold = TRUE WHERE book_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateBookQuery)) {
                            updateStmt.setInt(1, bookId);
                            updateStmt.executeUpdate();
                        }

                        // Notify buyer of acceptance (optional)
                        String notifyBuyerQuery = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";
                        try (PreparedStatement notifyStmt = conn.prepareStatement(notifyBuyerQuery)) {
                            notifyStmt.setInt(1, buyerId);
                            notifyStmt.setString(2, "Your request for book ID " + bookId + " has been accepted.");
                            notifyStmt.executeUpdate();
                        }
                    }

                    // Delete the request
                    String deleteRequestQuery = "DELETE FROM book_requests WHERE request_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteRequestQuery)) {
                        deleteStmt.setInt(1, requestId);
                        deleteStmt.executeUpdate();
                    }
                }
            }

            conn.commit();

            loadBooks();
            JOptionPane.showMessageDialog(this,
                    "Request " + (accept ? "accepted" : "declined") + " successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing request: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        int sellerId = getSellerId(username);
        if (sellerId == -1) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT book_id, title, author, genre, price, image_path FROM books WHERE seller_id = ? AND is_sold = FALSE";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, sellerId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getDouble("price"),
                            rs.getString("image_path"),
                            rs.getInt("book_id")
                    });
                }
            } catch (SQLException e) {
                // Fallback if is_sold column is missing
                if (e.getMessage().contains("Unknown column 'is_sold'")) {
                    System.out.println("Falling back to query without is_sold filter.");
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT book_id, title, author, genre, price, image_path FROM books WHERE seller_id = ?")) {
                        ps.setInt(1, sellerId);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            tableModel.addRow(new Object[]{
                                    rs.getString("title"),
                                    rs.getString("author"),
                                    rs.getString("genre"),
                                    rs.getDouble("price"),
                                    rs.getString("image_path"),
                                    rs.getInt("book_id")
                            });
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error loading books (fallback): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    throw e; // Re-throw other SQL exceptions
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSellerId(String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    // Helper methods
    private TitledBorder createTitledBorder(String title) {
        return createTitledBorder(title, 14);
    }

    private TitledBorder createTitledBorder(String title, int fontSize) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, fontSize),
                new Color(70, 70, 70));
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JCheckBox createStyledCheckbox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(new Color(245, 245, 245));
        cb.setFocusPainted(false);
        return cb;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int yPos) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(70, 70, 70));

        gbc.gridx = 0;
        gbc.gridy = yPos;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void styleButton(JButton button, Color bgColor, int fontSize) {
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void styleButton(JButton button, Color bgColor) {
        styleButton(button, bgColor, 12);
    }
}