package com.bookstore.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.bookstore.db.DatabaseConnection;

public class BuyerPanel extends JPanel {
    private final JTextField searchField;
    private final JComboBox<String> genreComboBox;
    private final JTable bookTable;
    private final DefaultTableModel tableModel;
    private final String username;
    private Random rand = new Random();
    private final List<Point> particles = new ArrayList<>();

    public BuyerPanel(String username) {
        System.out.println("BuyerPanel initialized with Celestial Light theme on April 25, 2025");
        this.username = username;

        setLayout(new BorderLayout());
        setOpaque(false); // Allow background to show through

        searchField = createStyledTextField(20);
        genreComboBox = new JComboBox<>();
        bookTable = new JTable();
        tableModel = new DefaultTableModel();

        createSearchPanel();
        createBookTable();
        createButtonsPanel();
        loadGenres();
        loadBooks("");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Initialize particles only if not already initialized
        if (particles.isEmpty() && w > 0 && h > 0) {
            for (int i = 0; i < 20; i++) {
                particles.add(new Point(rand.nextInt(w), rand.nextInt(h)));
            }
        }

        // Celestial gradient
        GradientPaint celestialGradient = new GradientPaint(
                0, 0,
                new Color(230, 240, 245), // Slightly darker Alice Blue
                w, h, new Color(160, 200, 215) // Slightly darker Light Blue
        );
        g2.setPaint(celestialGradient);
        g2.fillRect(0, 0, w, h);

        // Secondary radial gradient for yellow and white shade
        g2.setPaint(new RadialGradientPaint(
                (float) w / 2, (float) h / 2, (float) w,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(135, 206, 235, 80), new Color(255, 215, 0, 50), new Color(255, 255, 255, 0)}
        ));
        g2.fillRect(0, 0, w, h);

        // Draw yellowish bubbles with improved visibility
        for (Point particle : particles) {
            g2.setColor(new Color(255, 255, 0, 50)); // Brighter, more yellowish
            int size = 50 + rand.nextInt(100);
            // Update particle position if panel size changes
            if (particle.x > w || particle.y > h || particle.x < 0 || particle.y < 0) {
                particle.setLocation(rand.nextInt(w), rand.nextInt(h));
            }
            g2.fillOval(particle.x, particle.y, size, size);
        }
    }

    private void createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        searchPanel.setBackground(new Color(160, 200, 215, 200));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235), 2),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
        searchLabel.setForeground(new Color(0, 0, 0));
        searchPanel.add(searchLabel);

        searchField.setPreferredSize(new Dimension(250, 40));
        searchField.setForeground(Color.BLACK);
        searchPanel.add(searchField);

        JLabel genreLabel = new JLabel("Genre:");
        genreLabel.setFont(new Font("Montserrat", Font.BOLD, 14));
        genreLabel.setForeground(new Color(0, 0, 0));
        searchPanel.add(genreLabel);

        styleComboBox(genreComboBox, new Color(160, 200, 215, 200), Color.BLACK);
        genreComboBox.setPreferredSize(new Dimension(200, 40));
        searchPanel.add(genreComboBox);

        JButton searchBtn = createStyledButton("Search", new Color(100, 200, 150), new Color(150, 250, 200));
        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            loadBooks(searchText);
        });
        searchPanel.add(searchBtn);

        add(searchPanel, BorderLayout.NORTH);
    }

    private void createBookTable() {
        tableModel.setDataVector(
                new Object[][]{},
                new Object[]{"Title", "Author", "Genre", "Price", "Action", "book_id"}
        );
        tableModel.setColumnCount(6);

        bookTable.setModel(tableModel);
        bookTable.removeColumn(bookTable.getColumnModel().getColumn(5));

        bookTable.setFont(new Font("Montserrat", Font.PLAIN, 12));
        bookTable.setRowHeight(40);
        bookTable.setBackground(new Color(230, 240, 245));
        bookTable.setForeground(Color.BLACK);
        bookTable.setSelectionBackground(new Color(135, 206, 235));
        bookTable.setSelectionForeground(Color.WHITE);
        bookTable.setGridColor(new Color(135, 206, 235));
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setDefaultEditor(Object.class, null); // Disable editing for all cells
        bookTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(230, 240, 245) : new Color(200, 220, 235));
                }
                return c;
            }
        });

        // Disable text selection and edit pointer on double-click
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click detected
                    int column = bookTable.columnAtPoint(e.getPoint());
                    if (column != 4) { // Exclude "Action" column to preserve button functionality
                        e.consume(); // Prevent default text selection behavior
                    } else {
                        // Handle "View Details" button click
                        int row = bookTable.rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            int modelRow = bookTable.convertRowIndexToModel(row);
                            int bookId = (int) tableModel.getValueAt(modelRow, 5);
                            viewBookDetails(bookId);
                        }
                    }
                }
            }
        });

        JTableHeader header = bookTable.getTableHeader();
        header.setFont(new Font("Montserrat", Font.BOLD, 12));
        header.setBackground(new Color(135, 206, 235));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        TableColumn actionColumn = bookTable.getColumnModel().getColumn(4);
        actionColumn.setCellRenderer(new ButtonRenderer(new Color(100, 200, 150), new Color(150, 250, 200)));
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox(), new Color(100, 200, 150), new Color(150, 250, 200)));

        JScrollPane tableScroll = new JScrollPane(bookTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(135, 206, 235), 2));
        tableScroll.setBackground(new Color(160, 200, 215));
        tableScroll.getViewport().setBackground(new Color(160, 200, 215));
        add(tableScroll, BorderLayout.CENTER);
    }

    private void createButtonsPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        buttonPanel.setBackground(new Color(160, 200, 215, 200));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(135, 206, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JButton logoutBtn = createStyledButton("Logout", new Color(200, 100, 150), new Color(250, 150, 200));
        logoutBtn.setPreferredSize(new Dimension(120, 45));
        logoutBtn.addActionListener(e -> {
            System.out.println("Logging out from BuyerPanel for user: " + username);
            SwingUtilities.getWindowAncestor(this).dispose();
            new LoginSignup();
        });
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadGenres() {
        genreComboBox.removeAllItems(); // Clear existing items
        genreComboBox.addItem("ALL");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Connected to database: " + conn.getCatalog());
            String query = "SELECT DISTINCT name FROM genres";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No genres found in the database.");
                genreComboBox.setEnabled(false); // Disable dropdown if no genres
                return;
            }

            genreComboBox.setEnabled(true); // Enable dropdown if genres exist
            int genreCount = 0;
            while (rs.next()) {
                String genreName = rs.getString("name");
                System.out.println("Adding genre to dropdown: " + genreName);
                genreComboBox.addItem(genreName);
                genreCount++;
            }
            System.out.println("Total genres loaded: " + genreCount);

            // Force UI update
            genreComboBox.revalidate();
            genreComboBox.repaint();
            this.revalidate();
            this.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Error loading genres: " + ex.getMessage());
            if (isVisible()) {
                JOptionPane.showMessageDialog(this, "Error loading genres: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadBooks(String searchText) {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT book_id, title, author, genre, price FROM books WHERE (title LIKE ? OR author LIKE ?)";
            if (!"ALL".equals(genreComboBox.getSelectedItem())) {
                query += " AND genre LIKE ?";
            }

            PreparedStatement ps = conn.prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            if (!"ALL".equals(genreComboBox.getSelectedItem())) {
                ps.setString(3, "%" + genreComboBox.getSelectedItem() + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getDouble("price"),
                        "View Details",
                        rs.getInt("book_id")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewBookDetails(int bookId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT b.title, b.author, b.genre, b.price, b.image_path, u.username " +
                    "FROM books b JOIN users u ON b.seller_id = u.id WHERE b.book_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                double price = rs.getDouble("price");
                String imagePath = rs.getString("image_path");
                String seller = rs.getString("username");

                JPanel detailPanel = new JPanel(new BorderLayout());
                detailPanel.setBackground(new Color(160, 200, 215, 200));
                detailPanel.setBorder(BorderFactory.createLineBorder(new Color(135, 206, 235), 2));

                JTextArea infoArea = new JTextArea(
                        "Title: " + title + "\n" +
                                "Author: " + author + "\n" +
                                "Genre: " + genre + "\n" +
                                "Price: Rs" + String.format("%.2f", price) + "\n" +
                                "Seller: " + seller
                );
                infoArea.setEditable(false);
                infoArea.setBackground(new Color(230, 240, 245));
                infoArea.setForeground(Color.BLACK);
                infoArea.setFont(new Font("Montserrat", Font.PLAIN, 14));
                infoArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
                imagePanel.setBackground(new Color(160, 200, 215, 200));

                final double[] zoomFactor = {1.0};
                final int[] baseSize = {150};

                if (imagePath != null && !imagePath.isEmpty()) {
                    String[] paths = imagePath.split(";");
                    imagePanel.removeAll();
                    for (String path : paths) {
                        try {
                            ImageIcon icon = new ImageIcon(path);
                            Image scaled = icon.getImage().getScaledInstance(
                                    (int) (baseSize[0] * zoomFactor[0]),
                                    (int) (baseSize[0] * zoomFactor[0]),
                                    Image.SCALE_SMOOTH
                            );
                            JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                            imageLabel.setBorder(BorderFactory.createLineBorder(new Color(135, 206, 235)));
                            imagePanel.add(imageLabel);
                        } catch (Exception ex) {
                            System.err.println("Error loading image: " + path);
                        }
                    }
                }

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
                buttonPanel.setBackground(new Color(160, 200, 215, 200));

                JButton zoomInBtn = createStyledButton("Zoom In", new Color(100, 200, 150), new Color(150, 250, 200));
                zoomInBtn.addActionListener(e -> {
                    zoomFactor[0] += 0.2;
                    if (zoomFactor[0] > 3.0) zoomFactor[0] = 3.0;
                    updateImages(imagePanel, imagePath, baseSize[0], zoomFactor[0]);
                });

                JButton zoomOutBtn = createStyledButton("Zoom Out", new Color(100, 200, 150), new Color(150, 250, 200));
                zoomOutBtn.addActionListener(e -> {
                    zoomFactor[0] -= 0.2;
                    if (zoomFactor[0] < 0.5) zoomFactor[0] = 0.5;
                    updateImages(imagePanel, imagePath, baseSize[0], zoomFactor[0]);
                });

                JButton requestBtn = createStyledButton("Request Book", new Color(100, 200, 150), new Color(150, 250, 200));
                requestBtn.addActionListener(e -> requestBook(bookId));

                buttonPanel.add(zoomInBtn);
                buttonPanel.add(zoomOutBtn);
                buttonPanel.add(requestBtn);

                detailPanel.add(infoArea, BorderLayout.NORTH);
                detailPanel.add(new JScrollPane(imagePanel), BorderLayout.CENTER);
                detailPanel.add(buttonPanel, BorderLayout.SOUTH);

                JOptionPane.showMessageDialog(this, detailPanel, "Book Details", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading book details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateImages(JPanel imagePanel, String imagePath, int baseSize, double zoomFactor) {
        imagePanel.removeAll();
        if (imagePath != null && !imagePath.isEmpty()) {
            String[] paths = imagePath.split(";");
            for (String path : paths) {
                try {
                    ImageIcon icon = new ImageIcon(path);
                    Image scaled = icon.getImage().getScaledInstance(
                            (int) (baseSize * zoomFactor),
                            (int) (baseSize * zoomFactor),
                            Image.SCALE_SMOOTH
                    );
                    JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                    imageLabel.setBorder(BorderFactory.createLineBorder(new Color(135, 206, 235)));
                    imagePanel.add(imageLabel);
                } catch (Exception ex) {
                    System.err.println("Error loading image: " + path);
                }
            }
        }
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    private void requestBook(int bookId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String userQuery = "SELECT id, email FROM users WHERE username = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();
            if (!userRs.next()) {
                JOptionPane.showMessageDialog(this, "Error: User not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int buyerId = userRs.getInt("id");
            String buyerEmail = userRs.getString("email");
            if (buyerEmail == null) {
                buyerEmail = "email_not_provided@example.com";
            }

            String insertQuery = "INSERT INTO book_requests (book_id, requested_by, buyer_username, buyer_email) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, bookId);
            insertStmt.setInt(2, buyerId);
            insertStmt.setString(3, username);
            insertStmt.setString(4, buyerEmail);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book request sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending request: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Montserrat", Font.PLAIN, 14));
        field.setBackground(new Color(160, 200, 215, 200));
        field.setForeground(Color.BLACK);
        field.setCaretColor(new Color(135, 206, 235));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        field.setOpaque(true);
        return field;
    }

    private void styleComboBox(JComboBox<String> comboBox, Color bgColor, Color fgColor) {
        comboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        comboBox.setBackground(bgColor);
        comboBox.setForeground(fgColor);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        comboBox.setOpaque(true);
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 100));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Montserrat", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        private final Color bgColor;
        private final Color hoverColor;

        public ButtonRenderer(Color bgColor, Color hoverColor) {
            this.bgColor = bgColor;
            this.hoverColor = hoverColor;
            setOpaque(false);
            setFont(new Font("Montserrat", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            if (isSelected) {
                setBackground(new Color(135, 206, 235));
                setForeground(Color.WHITE);
            } else {
                setBackground(bgColor);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 100));
            g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 12, 12);
            super.paintComponent(g);
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private final Color bgColor;
        private final Color hoverColor;

        public ButtonEditor(JCheckBox checkBox, Color bgColor, Color hoverColor) {
            super(checkBox);
            this.bgColor = bgColor;
            this.hoverColor = hoverColor;
            button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 100));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 12, 12);
                    super.paintComponent(g);
                }
            };
            button.setOpaque(false);
            button.setFont(new Font("Montserrat", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            if (isSelected) {
                button.setBackground(new Color(135, 206, 235));
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(bgColor);
            }
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int modelRow = bookTable.convertRowIndexToModel(bookTable.getEditingRow());
                int bookId = (int) tableModel.getValueAt(modelRow, 5);
                viewBookDetails(bookId);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}