package com.bookstore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.bookstore.db.DatabaseConnection;

public class BuyerPanel extends JPanel {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> genreComboBox;
    private final String username;
    private final RoleSwitcher roleSwitcher;
    private JButton notificationsBtn;
    private JDialog notificationsDialog;
    private DefaultListModel<String> notificationsListModel;

    public BuyerPanel(String username, RoleSwitcher roleSwitcher) {
        this.username = username;
        this.roleSwitcher = roleSwitcher;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        createNotificationsPanel();
        createSearchPanel();
        createBookTable();
        loadGenres();
        loadBooks("", "All");
        startNotificationChecker();
    }

    private void createNotificationsPanel() {
        notificationsListModel = new DefaultListModel<>();

        notificationsBtn = new JButton("Notifications");
        styleButton(notificationsBtn, new Color(255, 165, 0), 14);
        notificationsBtn.addActionListener(e -> showNotifications());

        notificationsDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Notifications", Dialog.ModalityType.MODELESS);
        notificationsDialog.setSize(500, 400);
        notificationsDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JList<String> notificationsList = new JList<>(notificationsListModel);
        notificationsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notificationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(notificationsList);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton clearBtn = new JButton("Clear Notifications");
        styleButton(clearBtn, new Color(220, 20, 60), 12);
        clearBtn.addActionListener(e -> {
            clearNotifications();
            notificationsListModel.clear();
        });

        JButton closeBtn = new JButton("Close");
        styleButton(closeBtn, new Color(100, 149, 237), 12);
        closeBtn.addActionListener(e -> notificationsDialog.setVisible(false));

        buttonPanel.add(clearBtn);
        buttonPanel.add(closeBtn);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        notificationsDialog.add(contentPanel);
    }

    private void clearNotifications() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "DELETE FROM notifications WHERE user_id = (SELECT id FROM users WHERE username = ?)";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, username);
                        ps.executeUpdate();
                    }
                }
                return null;
            }
        };
        worker.execute();
    }

    private void showNotifications() {
        loadNotifications();
        notificationsDialog.setVisible(true);
    }

    private void loadNotifications() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "SELECT n.message, n.created_at, u.username as seller_username " +
                            "FROM notifications n " +
                            "JOIN users u ON n.seller_id = u.id " +
                            "WHERE n.user_id = (SELECT id FROM users WHERE username = ?) " +
                            "ORDER BY n.created_at DESC";

                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, username);
                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            String message = rs.getString("message");
                            String seller = rs.getString("seller_username");
                            String timestamp = rs.getTimestamp("created_at").toString();
                            publish(String.format("[%s] %s (Seller: %s)", timestamp, message, seller));
                        }
                    }

                    String updateQuery = "UPDATE notifications SET is_read = TRUE " +
                            "WHERE user_id = (SELECT id FROM users WHERE username = ?)";
                    try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                        ps.setString(1, username);
                        ps.executeUpdate();
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String notification : chunks) {
                    if (!notificationsListModel.contains(notification)) {
                        notificationsListModel.addElement(notification);
                    }
                }
            }
        };
        worker.execute();
    }

    private void startNotificationChecker() {
        Timer timer = new Timer(30000, e -> {
            if (notificationsDialog == null || !notificationsDialog.isVisible()) {
                checkNewNotifications();
            }
        });
        timer.start();
    }

    private void checkNewNotifications() {
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "SELECT COUNT(*) FROM notifications " +
                            "WHERE user_id = (SELECT id FROM users WHERE username = ?) " +
                            "AND is_read = FALSE";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, username);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
                return 0;
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    if (count > 0) {
                        notificationsBtn.setText("Notifications (" + count + ")");
                        notificationsBtn.setBackground(new Color(255, 140, 0));
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        notificationsBtn.setText("Notifications");
                        notificationsBtn.setBackground(new Color(255, 165, 0));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(new Color(230, 230, 250));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField(20);
        styleTextField(searchField);

        genreComboBox = new JComboBox<>();
        styleComboBox(genreComboBox);

        JButton searchBtn = new JButton("Search");
        styleButton(searchBtn, new Color(70, 130, 180), 14);
        searchBtn.addActionListener(e -> loadBooks(searchField.getText(), (String) genreComboBox.getSelectedItem()));

        JButton switchRoleBtn = new JButton("Switch to Seller");
        styleButton(switchRoleBtn, new Color(60, 179, 113), 14);
        switchRoleBtn.addActionListener(e -> roleSwitcher.switchRole());

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, new Color(120, 120, 120), 14);
        logoutBtn.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            new LoginSignup();
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Genre:"));
        searchPanel.add(genreComboBox);
        searchPanel.add(searchBtn);
        searchPanel.add(notificationsBtn);
        searchPanel.add(switchRoleBtn);
        searchPanel.add(logoutBtn);

        add(searchPanel, BorderLayout.NORTH);
    }

    private void createBookTable() {
        tableModel = new DefaultTableModel(new Object[]{"Title", "Author", "Genres", "Price", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 4 ? JButton.class : Object.class;
            }
        };

        bookTable = new JTable(tableModel);
        styleTable();

        // Add hidden column for book_id only
        tableModel.addColumn("book_id");

        TableColumn buttonColumn = bookTable.getColumnModel().getColumn(4);
        buttonColumn.setCellRenderer(new ButtonRenderer());
        buttonColumn.setCellEditor(new ButtonEditor(new JCheckBox(), this));

        add(new JScrollPane(bookTable), BorderLayout.CENTER);
    }

    private void loadGenres() {
        genreComboBox.removeAllItems();
        genreComboBox.addItem("All");

        SwingWorker<Void, String> genreWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT name FROM genres ORDER BY name")) {
                    while (rs.next()) {
                        publish(rs.getString("name"));
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> genres) {
                for (String genre : genres) {
                    genreComboBox.addItem(genre);
                }
            }
        };
        genreWorker.execute();
    }

    private void loadBooks(String search, String genre) {
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                tableModel.setRowCount(0);
                Connection conn = null;

                try {
                    conn = DatabaseConnection.getConnection();
                    StringBuilder query = new StringBuilder(
                            "SELECT b.book_id, b.title, b.author, b.price, " +
                                    "GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') AS genres " +
                                    "FROM books b " +
                                    "LEFT JOIN book_genres bg ON b.book_id = bg.book_id " +
                                    "LEFT JOIN genres g ON bg.genre_id = g.genre_id " +
                                    "WHERE b.is_sold = FALSE AND (b.title LIKE ? OR b.author LIKE ?) "
                    );

                    if (!"All".equals(genre)) {
                        query.append("AND g.name = ? ");
                    }
                    query.append("GROUP BY b.book_id");

                    try (PreparedStatement ps = conn.prepareStatement(query.toString())) {
                        ps.setString(1, "%" + search + "%");
                        ps.setString(2, "%" + search + "%");

                        if (!"All".equals(genre)) {
                            ps.setString(3, genre);
                        }

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            String genres = rs.getString("genres");
                            if (genres == null) genres = ""; // Handle NULL genres
                            System.out.println("Book: " + rs.getString("title") + ", Genres: " + genres); // Debug output
                            publish(new Object[]{
                                    rs.getString("title"),
                                    rs.getString("author"),
                                    genres,
                                    rs.getDouble("price"),
                                    "View Details",
                                    rs.getInt("book_id")
                            });
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Fallback query if is_sold column is missing
                    if (e.getMessage().contains("Unknown column 'b.is_sold'")) {
                        System.out.println("Falling back to query without is_sold filter.");
                        if (conn != null) {
                            String fallbackQuery = "SELECT b.book_id, b.title, b.author, b.price, " +
                                    "GROUP_CONCAT(DISTINCT g.name SEPARATOR ', ') AS genres " +
                                    "FROM books b " +
                                    "LEFT JOIN book_genres bg ON b.book_id = bg.book_id " +
                                    "LEFT JOIN genres g ON bg.genre_id = g.genre_id " +
                                    "WHERE (b.title LIKE ? OR b.author LIKE ?) " +
                                    ( !"All".equals(genre) ? "AND g.name = ? " : "") +
                                    "GROUP BY b.book_id";
                            try (PreparedStatement ps = conn.prepareStatement(fallbackQuery)) {
                                ps.setString(1, "%" + search + "%");
                                ps.setString(2, "%" + search + "%");
                                if (!"All".equals(genre)) {
                                    ps.setString(3, genre);
                                }
                                ResultSet rs = ps.executeQuery();
                                while (rs.next()) {
                                    String genres = rs.getString("genres");
                                    if (genres == null) genres = "";
                                    System.out.println("Fallback Book: " + rs.getString("title") + ", Genres: " + genres);
                                    publish(new Object[]{
                                            rs.getString("title"),
                                            rs.getString("author"),
                                            genres,
                                            rs.getDouble("price"),
                                            "View Details",
                                            rs.getInt("book_id")
                                    });
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                SwingUtilities.invokeLater(() ->
                                        JOptionPane.showMessageDialog(BuyerPanel.this,
                                                "Error loading books (fallback): " + ex.getMessage(),
                                                "Database Error", JOptionPane.ERROR_MESSAGE));
                            }
                        }
                    } else {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(BuyerPanel.this,
                                        "Error loading books: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE));
                    }
                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> rows) {
                for (Object[] row : rows) {
                    tableModel.addRow(new Object[]{
                            row[0], // title
                            row[1], // author
                            row[2], // genres
                            row[3], // price
                            row[4], // action
                            row[5]  // book_id (hidden)
                    });
                }
            }
        };
        worker.execute();
    }

    private void showBookDetailsDialog(String title, String author, String genres, double price, int bookId) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Book Details: " + title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.add(createDetailLabel("Title: " + title, Font.BOLD, 16));
        infoPanel.add(createDetailLabel("Author: " + author, Font.PLAIN, 14));
        infoPanel.add(createDetailLabel("Genres: " + genres, Font.PLAIN, 14));
        infoPanel.add(createDetailLabel(String.format("Price: ₹%.2f", price), Font.PLAIN, 14));
        contentPanel.add(infoPanel, BorderLayout.CENTER);

        JButton requestBtn = new JButton("Request This Book");
        styleButton(requestBtn, new Color(70, 130, 180), 14);
        requestBtn.addActionListener(e -> {
            requestBook(bookId, title, author, price);
            dialog.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(requestBtn);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void requestBook(int bookId, String title, String author, double price) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);

                    // Get buyer ID
                    int buyerId = -1;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT id FROM users WHERE username = ?")) {
                        ps.setString(1, username);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            buyerId = rs.getInt("id");
                        } else {
                            return false;
                        }
                    }

                    // Check book availability and get seller ID
                    int sellerId = -1;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT seller_id FROM books WHERE book_id = ? AND is_sold = FALSE FOR UPDATE")) {
                        ps.setInt(1, bookId);
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(BuyerPanel.this,
                                            "This book is no longer available",
                                            "Error", JOptionPane.ERROR_MESSAGE));
                            return false;
                        }
                        sellerId = rs.getInt("seller_id");
                    }

                    // Check for existing request
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COUNT(*) FROM book_requests " +
                                    "WHERE book_id = ? AND buyer_id = ? AND status = 'pending'")) {
                        ps.setInt(1, bookId);
                        ps.setInt(2, buyerId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(BuyerPanel.this,
                                            "You already have a pending request for this book",
                                            "Info", JOptionPane.INFORMATION_MESSAGE));
                            return false;
                        }
                    }

                    // Create request
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO book_requests (book_id, buyer_id, status) " +
                                    "VALUES (?, ?, 'pending')")) {
                        ps.setInt(1, bookId);
                        ps.setInt(2, buyerId);
                        ps.executeUpdate();
                    }

                    // Create notification
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO notifications (user_id, seller_id, message) " +
                                    "VALUES (?, ?, ?)")) {
                        ps.setInt(1, sellerId);
                        ps.setInt(2, buyerId); // Notify the seller
                        ps.setString(3, "New request for book: " + title);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    return true;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(BuyerPanel.this,
                                "Book request sent successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadBooks(searchField.getText(), (String) genreComboBox.getSelectedItem());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BuyerPanel.this,
                            "Error requesting book: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Helper methods
    private JLabel createDetailLabel(String text, int style, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
    }

    private void styleTable() {
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookTable.setRowHeight(40);
        bookTable.setSelectionBackground(new Color(220, 240, 255));
        bookTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookTable.getTableHeader().setBackground(new Color(70, 130, 180));
        bookTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void styleButton(JButton button, Color bgColor, int fontSize) {
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBackground(new Color(70, 130, 180));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View Details");
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final BuyerPanel panel;

        public ButtonEditor(JCheckBox checkBox, BuyerPanel panel) {
            super(checkBox);
            this.panel = panel;
            button = new JButton("View Details");
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.addActionListener(evt -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            int row = bookTable.getSelectedRow();
            if (row != -1) {
                String title = (String) tableModel.getValueAt(row, 0);
                String author = (String) tableModel.getValueAt(row, 1);
                String genres = (String) tableModel.getValueAt(row, 2);
                double price = (Double) tableModel.getValueAt(row, 3);
                int bookId = (Integer) tableModel.getValueAt(row, 5);

                panel.showBookDetailsDialog(title, author, genres, price, bookId);
            }
            return "View Details";
        }
    }
}