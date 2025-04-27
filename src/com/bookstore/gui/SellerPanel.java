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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.bookstore.db.DatabaseConnection;

public class SellerPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(SellerPanel.class.getName());
    private final JTextField titleField, authorField, priceField, customGenreField;
    private final JLabel imageLabel;
    private final List<String> imagePaths;
    private final JTable bookTable;
    private final DefaultTableModel tableModel;
    private final String username;
    private final JCheckBox fictionCB, nonFictionCB, mysteryCB, scifiCB, romanceCB, fantasyCB, historyCB, horrorCB, bioCB;
    private final JPanel genrePanel;
    private float gradientAngle = 0f;
    private final List<Point> particles = new ArrayList<>();
    private final List<Float> particleAlphas = new ArrayList<>(); // For pulsing effect

    public SellerPanel(String username) {
        System.out.println("SellerPanel initialized with Celestial Light theme on April 25, 2025");
        this.username = username;
        this.imagePaths = new ArrayList<>();

        // Generate random particles for the background
        Random rand = new Random();
        for (int i = 0; i < 100; i++) { // Increased particle count
            particles.add(new Point(rand.nextInt(1000), rand.nextInt(600)));
            particleAlphas.add(rand.nextFloat() * 255); // Random initial alpha for pulsing
        }

        // Use JSplitPane to balance upper and lower halves
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 245)); // Slightly darker Alice Blue

        titleField = createStyledTextField(20);
        authorField = createStyledTextField(20);
        priceField = createStyledTextField(20);
        customGenreField = createStyledTextField(15);
        imageLabel = new JLabel("No images selected");
        imageLabel.setForeground(new Color(255, 255, 255)); // White text for readability
        bookTable = new JTable();
        tableModel = new DefaultTableModel();
        genrePanel = new JPanel(new GridLayout(0, 3, 10, 10)); // Reduced spacing between checkboxes
        genrePanel.setBackground(new Color(160, 200, 215, 200)); // Slightly darker Light Blue with transparency
        genrePanel.setOpaque(false);

        fictionCB = createStyledCheckbox("Fiction");
        nonFictionCB = createStyledCheckbox("Non-Fiction");
        mysteryCB = createStyledCheckbox("Mystery");
        scifiCB = createStyledCheckbox("Sci-Fi");
        romanceCB = createStyledCheckbox("Romance");
        fantasyCB = createStyledCheckbox("Fantasy");
        historyCB = createStyledCheckbox("History");
        horrorCB = createStyledCheckbox("Horror");
        bioCB = createStyledCheckbox("Biography");

        ((AbstractDocument) priceField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string.matches("[0-9.]*")) super.insertString(fb, offset, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text.matches("[0-9.]*")) super.replace(fb, offset, length, text, attrs);
            }
        });

        // Create the upper half (form components)
        JPanel topPanel = createFormComponents();
        topPanel.setMinimumSize(new Dimension(800, 400)); // Ensure enough height for all content

        // Create the lower half (table + buttons)
        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setOpaque(false);
        JScrollPane tableScroll = createEnhancedBookTable();
        JPanel buttonsPanel = createButtonsPanel();
        lowerPanel.add(tableScroll, BorderLayout.CENTER);
        lowerPanel.add(buttonsPanel, BorderLayout.SOUTH);

        // Use JSplitPane to split the upper and lower halves
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, lowerPanel);
        splitPane.setDividerLocation(0.6); // 60% for upper half, 40% for lower half
        splitPane.setResizeWeight(0.6); // Maintain the 60/40 ratio when resizing
        splitPane.setOpaque(false);
        splitPane.setBackground(new Color(230, 240, 245)); // Slightly darker Alice Blue

        // Add the split pane to the background panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Layered celestial gradient
                GradientPaint celestialGradient = new GradientPaint(
                        (float) (w * Math.cos(Math.toRadians(gradientAngle))),
                        (float) (h * Math.sin(Math.toRadians(gradientAngle))),
                        new Color(230, 240, 245), // Slightly darker Alice Blue
                        w, h, new Color(160, 200, 215) // Slightly darker Light Blue
                );
                g2.setPaint(celestialGradient);
                g2.fillRect(0, 0, w, h);

                // Add a secondary gradient for depth
                g2.setPaint(new RadialGradientPaint((float) w / 2, (float) h / 2, (float) w,
                        new float[]{0f, 0.5f, 1f},
                        new Color[]{new Color(135, 206, 235, 80), new Color(255, 215, 0, 50), new Color(255, 255, 255, 0)})); // Sky Blue to Gold
                g2.fillRect(0, 0, w, h);

                // Draw twinkling particles
                for (int i = 0; i < particles.size(); i++) {
                    Point particle = particles.get(i);
                    float alpha = particleAlphas.get(i);
                    g2.setColor(new Color(255, 215, 0, (int) alpha)); // Gold with pulsing alpha
                    int size = (int) (Math.sin(Math.toRadians(gradientAngle + particle.x)) * 3 + 3);
                    g2.fillOval(particle.x, particle.y, size, size);
                }
            }
        };
        backgroundPanel.setOpaque(false);
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(splitPane, BorderLayout.CENTER);

        add(backgroundPanel, BorderLayout.CENTER);

        loadBooks();

        // Start animation timer for gradient and particles
        Timer animationTimer = new Timer(50, (e) -> { // Added parameter to match ActionListener
            gradientAngle += 0.3f;
            if (gradientAngle > 360) gradientAngle -= 360;
            for (int i = 0; i < particleAlphas.size(); i++) {
                float alpha = particleAlphas.get(i);
                alpha += (float) (Math.sin(Math.toRadians(gradientAngle + i * 10)) * 5); // Explicit cast to float
                if (alpha > 255) alpha = 255;
                if (alpha < 50) alpha = 50;
                particleAlphas.set(i, alpha);
            }
            repaint();
        });
        animationTimer.start();
    }

    private JPanel createFormComponents() {
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 15, 15)); // Reduced spacing
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Reduced padding

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        leftPanel.setBackground(new Color(160, 200, 215, 200)); // Slightly darker Light Blue with transparency
        leftPanel.setBorder(createTitledBorder("Book Information", new Color(135, 206, 235), 16)); // Reduced font size

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Reduced insets
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormField(leftPanel, gbc, "Title:", titleField, 0);
        addFormField(leftPanel, gbc, "Author:", authorField, 1);
        addFormField(leftPanel, gbc, "Price (Rs):", priceField, 2);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBackground(new Color(160, 200, 215, 200)); // Slightly darker Light Blue with transparency
        rightPanel.setBorder(createTitledBorder("Book Details", new Color(135, 206, 235), 16));

        genrePanel.setBorder(createTitledBorder("Select Genres", new Color(135, 206, 235), 14));
        for (JCheckBox cb : new JCheckBox[]{fictionCB, nonFictionCB, mysteryCB, scifiCB,
                romanceCB, fantasyCB, historyCB, horrorCB, bioCB}) {
            genrePanel.add(cb);
        }

        JPanel customGenrePanel = new JPanel(new BorderLayout(5, 0)); // Reduced spacing
        customGenrePanel.setOpaque(false);
        JButton addGenreBtn = createStyledButton("+ Add", new Color(135, 206, 235), new Color(255, 255, 255));
        addGenreBtn.setPreferredSize(new Dimension(70, 25)); // Reduced button size
        addGenreBtn.addActionListener((e) -> { // Added parameter
            String customGenre = customGenreField.getText().trim();
            if (customGenre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a genre name", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (Component comp : genrePanel.getComponents()) {
                if (comp instanceof JCheckBox && ((JCheckBox) comp).getText().equalsIgnoreCase(customGenre)) {
                    JOptionPane.showMessageDialog(this, "Genre already exists", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO genres (name) VALUES (?) ON DUPLICATE KEY UPDATE name=name";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, customGenre);
                ps.executeUpdate();
            } catch (SQLException ex) {
                LOGGER.severe("Error adding custom genre: " + ex.getMessage());
            }
            JCheckBox newGenreCB = createStyledCheckbox(customGenre);
            genrePanel.add(newGenreCB);
            genrePanel.revalidate();
            genrePanel.repaint();
            customGenreField.setText("");
            JOptionPane.showMessageDialog(this, "Genre '" + customGenre + "' added!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        customGenrePanel.add(customGenreField, BorderLayout.CENTER);
        customGenrePanel.add(addGenreBtn, BorderLayout.EAST);

        addFormField(rightPanel, gbc, "Custom Genre:", customGenrePanel, 0);
        gbc.gridy = 1;
        rightPanel.add(genrePanel, gbc);

        JButton browseBtn = createStyledButton("Browse Images 📷", new Color(255, 255, 255), new Color(135, 206, 235));
        browseBtn.setPreferredSize(new Dimension(130, 25)); // Reduced button size
        browseBtn.addActionListener(this::browseImages); // Using method reference

        gbc.gridy = 2;
        rightPanel.add(browseBtn, gbc);
        gbc.gridy = 3;
        rightPanel.add(imageLabel, gbc);

        formPanel.add(leftPanel);
        formPanel.add(rightPanel);

        JButton addBookBtn = createStyledButton("Add Book 📚", new Color(135, 206, 235), new Color(255, 255, 255));
        addBookBtn.setPreferredSize(new Dimension(180, 35)); // Reduced button size
        addBookBtn.addActionListener((e) -> addBook()); // Added parameter

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Reduced padding
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(addBookBtn);
        topPanel.add(buttonWrapper, BorderLayout.SOUTH);

        return topPanel;
    }

    private JScrollPane createEnhancedBookTable() {
        tableModel.setDataVector(
                new Object[][]{},
                new Object[]{"Title", "Author", "Genre", "Price", "Images", "book_id"}
        );
        tableModel.setColumnCount(6);

        bookTable.setModel(tableModel);
        bookTable.removeColumn(bookTable.getColumnModel().getColumn(5));

        bookTable.setFont(new Font("Orbitron", Font.PLAIN, 18)); // Increased font size
        bookTable.setRowHeight(60); // Increased row height
        bookTable.setBackground(new Color(160, 200, 215)); // Slightly darker Light Blue
        bookTable.setForeground(new Color(0, 0, 0)); // Black for readability
        bookTable.setSelectionBackground(new Color(135, 206, 235)); // Sky Blue
        bookTable.setSelectionForeground(new Color(0, 0, 0));
        bookTable.setGridColor(new Color(255, 215, 0)); // Gold
        bookTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        bookTable.setIntercellSpacing(new Dimension(5, 5));
        bookTable.setShowGrid(true);

        bookTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(160, 200, 215) : new Color(180, 220, 235));
                }
                c.setForeground(new Color(0, 0, 0));
                if (c instanceof JComponent && hasFocus) {
                    ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(135, 206, 235), 2));
                }
                return c;
            }
        });

        bookTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = bookTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    bookTable.repaint(); // Trigger repaint to update hover effect
                }
            }
        });

        JTableHeader header = bookTable.getTableHeader();
        header.setFont(new Font("Orbitron", Font.BOLD, 18)); // Increased font size
        header.setBackground(new Color(255, 215, 0)); // Gold
        header.setForeground(new Color(0, 0, 0));
        header.setPreferredSize(new Dimension(header.getWidth(), 50)); // Slightly increased header height
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JScrollPane tableScroll = new JScrollPane(bookTable);
        tableScroll.setPreferredSize(new Dimension(800, 500)); // Increased size of the table area
        tableScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tableScroll.setBackground(new Color(230, 240, 245));
        tableScroll.getViewport().setBackground(new Color(230, 240, 245));
        return tableScroll;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15)); // Reduced padding
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(new Color(160, 200, 215, 200)); // Slightly darker Light Blue with transparency
        buttonPanel.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(255, 215, 0)));

        JButton deleteBtn = createStyledButton("Delete Book(s) 🗑", new Color(255, 255, 255), new Color(135, 206, 235));
        deleteBtn.setPreferredSize(new Dimension(140, 40)); // Reduced button size
        deleteBtn.addActionListener((e) -> deleteBook()); // Added parameter

        JButton notificationsBtn = createStyledButton("View Requests 🔔", new Color(135, 206, 235), new Color(255, 255, 255));
        notificationsBtn.setPreferredSize(new Dimension(140, 40)); // Reduced button size
        notificationsBtn.addActionListener((e) -> showNotifications()); // Added parameter

        JButton logoutBtn = createStyledButton("Logout 🚪", new Color(255, 255, 255), new Color(135, 206, 235));
        logoutBtn.setPreferredSize(new Dimension(120, 40)); // Reduced button size
        logoutBtn.addActionListener((e) -> {
            System.out.println("Logging out from SellerPanel for user: " + username);
            SwingUtilities.getWindowAncestor(this).dispose();
            new LoginSignup();
        }); // Added parameter

        buttonPanel.add(deleteBtn);
        buttonPanel.add(notificationsBtn);
        buttonPanel.add(logoutBtn);

        return buttonPanel;
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
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
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
            previewDialog.setSize(700, 500);
            previewDialog.setLocationRelativeTo(this);
            previewDialog.setModal(true);
            previewDialog.getContentPane().setBackground(new Color(160, 200, 215)); // Slightly darker Light Blue

            JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
            previewPanel.setBackground(new Color(160, 200, 215));
            previewPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            JScrollPane scrollPane = new JScrollPane(previewPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBackground(new Color(160, 200, 215));
            scrollPane.getViewport().setBackground(new Color(160, 200, 215));

            imagePaths.clear();

            for (File file : files) {
                try {
                    ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                    Image scaled = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(scaled));
                    imageLabel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                    previewPanel.add(imageLabel);
                    imagePaths.add(file.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading image: " + file.getName(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.severe("Error loading image: " + ex.getMessage());
                }
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
            buttonPanel.setBackground(new Color(160, 200, 215));

            JButton confirmBtn = createStyledButton("Confirm ✅", new Color(135, 206, 235), new Color(255, 255, 255));
            confirmBtn.addActionListener((ev) -> { // Added parameter
                previewDialog.dispose();
                this.imageLabel.setText(imagePaths.size() + " image(s) selected");
            });

            JButton cancelBtn = createStyledButton("Cancel ❌", new Color(255, 255, 255), new Color(135, 206, 235));
            cancelBtn.addActionListener((ev) -> { // Added parameter
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

        if (priceText.startsWith(".") || priceText.endsWith(".")) { // Simplified condition
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
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price format", "Error", JOptionPane.WARNING_MESSAGE);
            LOGGER.severe("Invalid price format: " + ex.getMessage());
            return;
        }

        List<String> genres = new ArrayList<>();
        for (Component comp : genrePanel.getComponents()) {
            if (comp instanceof JCheckBox && ((JCheckBox) comp).isSelected()) {
                genres.add(((JCheckBox) comp).getText());
            }
        }

        if (genres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one genre", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String genreString = String.join(", ", genres);
        String imagePath = String.join(";", imagePaths);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Insert genres into genres table
            for (String genre : genres) {
                String genreInsertQuery = "INSERT INTO genres (name) VALUES (?) ON DUPLICATE KEY UPDATE name = name";
                PreparedStatement genreStmt = conn.prepareStatement(genreInsertQuery);
                genreStmt.setString(1, genre);
                genreStmt.executeUpdate();
            }

            int sellerId = getSellerId(username);
            if (sellerId == -1) {
                JOptionPane.showMessageDialog(this, "Error: Seller not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String query = "INSERT INTO books (title, author, genre, price, image_path, seller_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, genreString);
            ps.setDouble(4, price);
            ps.setString(5, imagePath);
            ps.setInt(6, sellerId);

            ps.executeUpdate();

            titleField.setText("");
            authorField.setText("");
            priceField.setText("");
            customGenreField.setText("");
            imagePaths.clear();
            imageLabel.setText("No images selected");
            for (Component comp : genrePanel.getComponents()) {
                if (comp instanceof JCheckBox) ((JCheckBox) comp).setSelected(false);
            }

            loadBooks();
            JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.severe("Error adding book: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBook() {
        int[] selectedRows = bookTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one book to delete", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = selectedRows.length == 1 ? "Delete the selected book?" : "Delete all " + selectedRows.length + " selected books?";
        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

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
                    String deleteRequestsQuery = "DELETE FROM book_requests WHERE book_id=?";
                    try (PreparedStatement psRequests = conn.prepareStatement(deleteRequestsQuery)) {
                        psRequests.setInt(1, bookId);
                        psRequests.executeUpdate();
                    }

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
                JOptionPane.showMessageDialog(this, deletedCount + " book(s) deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No books were deleted", "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            LOGGER.severe("Error deleting books: " + ex.getMessage());
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
            String query = "SELECT br.request_id, b.title, b.author, b.price, br.buyer_username, br.buyer_email " +
                    "FROM book_requests br JOIN books b ON br.book_id = b.book_id WHERE b.seller_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, sellerId);
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                JOptionPane.showMessageDialog(this, "No pending book requests", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JPanel notificationPanel = new JPanel();
            notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
            notificationPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            notificationPanel.setBackground(new Color(160, 200, 215)); // Slightly darker Light Blue

            while (rs.next()) {
                int requestId = rs.getInt("request_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                double price = rs.getDouble("price");
                String buyer = rs.getString("buyer_username");
                String buyerEmail = rs.getString("buyer_email");

                JPanel requestPanel = new JPanel(new BorderLayout(10, 10));
                requestPanel.setBorder(createTitledBorder("Request #" + requestId, new Color(135, 206, 235), 16));
                requestPanel.setBackground(new Color(180, 220, 235));
                requestPanel.setBorder(BorderFactory.createCompoundBorder(
                        requestPanel.getBorder(),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                JTextArea infoArea = new JTextArea(
                        "Book: " + title + "\nAuthor: " + author + "\nPrice: Rs" + String.format("%.2f", price) + "\nRequested by: " + buyer + "\nEmail: " + (buyerEmail != null ? buyerEmail : "Not provided")
                );
                infoArea.setEditable(false);
                infoArea.setBackground(new Color(180, 220, 235));
                infoArea.setForeground(new Color(0, 0, 0));
                infoArea.setFont(new Font("Orbitron", Font.PLAIN, 14));
                infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
                buttonPanel.setBackground(new Color(180, 220, 235));

                JButton acceptBtn = createStyledButton("Accept ✅", new Color(135, 206, 235), new Color(255, 255, 255));
                acceptBtn.addActionListener((e) -> processRequest(requestId, true)); // Added parameter

                JButton declineBtn = createStyledButton("Decline ❌", new Color(255, 255, 255), new Color(135, 206, 235));
                declineBtn.addActionListener((e) -> processRequest(requestId, false)); // Added parameter

                buttonPanel.add(acceptBtn);
                buttonPanel.add(declineBtn);

                requestPanel.add(infoArea, BorderLayout.CENTER);
                requestPanel.add(buttonPanel, BorderLayout.SOUTH);

                notificationPanel.add(requestPanel);
                notificationPanel.add(Box.createVerticalStrut(20));
            }

            JScrollPane scrollPane = new JScrollPane(notificationPanel);
            scrollPane.setPreferredSize(new Dimension(550, 450));
            scrollPane.setBackground(new Color(160, 200, 215));
            scrollPane.getViewport().setBackground(new Color(160, 200, 215));
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JOptionPane.showMessageDialog(this, scrollPane, "Book Requests", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.severe("Error loading requests: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading requests: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processRequest(int requestId, boolean accept) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (accept) {
                String bookQuery = "SELECT book_id FROM book_requests WHERE request_id=?";
                PreparedStatement bookStmt = conn.prepareStatement(bookQuery);
                bookStmt.setInt(1, requestId);
                ResultSet rs = bookStmt.executeQuery();

                if (rs.next()) {
                    int bookId = rs.getInt("book_id");

                    String deleteRequestQuery = "DELETE FROM book_requests WHERE request_id=?";
                    try (PreparedStatement deleteReqStmt = conn.prepareStatement(deleteRequestQuery)) {
                        deleteReqStmt.setInt(1, requestId);
                        deleteReqStmt.executeUpdate();
                    }

                    String deleteBookQuery = "DELETE FROM books WHERE book_id=?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteBookQuery)) {
                        deleteStmt.setInt(1, bookId);
                        deleteStmt.executeUpdate();
                    }
                }
            } else {
                String deleteRequestQuery = "DELETE FROM book_requests WHERE request_id=?";
                try (PreparedStatement deleteReqStmt = conn.prepareStatement(deleteRequestQuery)) {
                    deleteReqStmt.setInt(1, requestId);
                    deleteReqStmt.executeUpdate();
                }
            }

            conn.commit();

            loadBooks();
            JOptionPane.showMessageDialog(this, "Request " + (accept ? "accepted" : "declined") + " successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            LOGGER.severe("Error processing request: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error processing request: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        int sellerId = getSellerId(username);
        if (sellerId == -1) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT book_id, title, author, genre, price, image_path FROM books WHERE seller_id=?";
            PreparedStatement ps = conn.prepareStatement(query);
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
            LOGGER.severe("Error loading books: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSellerId(String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id FROM users WHERE username=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException ex) {
            LOGGER.severe("Error getting seller ID: " + ex.getMessage());
        }
        return -1;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int row) {
        gbc.gridy = row * 2;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Orbitron", Font.PLAIN, 14)); // Reduced font size
        label.setForeground(new Color(0, 0, 0)); // Black text
        panel.add(label, gbc);

        gbc.gridy = row * 2 + 1;
        panel.add(field, gbc);
    }

    private TitledBorder createTitledBorder(String title, Color borderColor, int fontSize) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 2),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10) // Reduced padding
                ),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Orbitron", Font.BOLD, fontSize),
                new Color(255, 255, 255) // White text for titles
        );
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Orbitron", Font.PLAIN, 16)); // Reduced font size
        field.setBackground(new Color(160, 200, 215, 200)); // Slightly darker Light Blue with transparency
        field.setForeground(new Color(255, 255, 255)); // White text
        field.setCaretColor(new Color(135, 206, 235));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10) // Reduced padding
        ));
        field.setOpaque(false);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(135, 206, 235), 3),
                        BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
        return field;
    }

    private JCheckBox createStyledCheckbox(String text) {
        JCheckBox cb = new JCheckBox(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(135, 206, 235, 80)); // Sky Blue glow
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                super.paintComponent(g);
            }
        };
        cb.setFont(new Font("Orbitron", Font.PLAIN, 14)); // Reduced font size
        cb.setBackground(new Color(160, 200, 215, 200)); // Slightly darker Light Blue with transparency
        cb.setForeground(new Color(255, 255, 255)); // White text
        cb.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3)); // Reduced padding
        cb.setFocusPainted(false);
        cb.setOpaque(false);
        cb.addChangeListener(e -> cb.repaint());
        return cb;
    }

    private JButton createStyledButton(String text, Color startColor, Color endColor) {
        class CelestialButton extends JButton {
            private float pulseAlpha = 0f;
            private boolean isHovered = false;

            public CelestialButton(String text) {
                super(text);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gradient = new GradientPaint(0, 0, startColor, w, h, endColor);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                if (isHovered) {
                    g2.setPaint(new RadialGradientPaint((float) w / 2, (float) h / 2, (float) w,
                            new float[]{0f, 1f},
                            new Color[]{new Color(255, 215, 0, (int) (pulseAlpha * 150)), new Color(255, 215, 0, 0)})); // Gold glow
                    g2.fillRoundRect(5, 5, w - 10, h - 10, 15, 15);
                }
                super.paintComponent(g);
            }
        }

        CelestialButton button = new CelestialButton(text);
        button.setFont(new Font("Orbitron", Font.BOLD, 14)); // Reduced font size
        button.setForeground(new Color(0, 0, 0)); // Retain black text for buttons
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Reduced padding
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        // Add hover animation
        Timer pulseTimer = new Timer(50, (e) -> { // Added parameter
            button.pulseAlpha += 0.05f;
            if (button.pulseAlpha > 1f) button.pulseAlpha = 0f;
            if (button.isHovered) button.repaint();
        });
        pulseTimer.start();

        button.addMouseListener(new MouseAdapter() {
            private float scale = 1.0f;

            @Override
            public void mouseEntered(MouseEvent e) {
                scale = 1.05f;
                button.isHovered = true;
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                scale = 1.0f;
                button.isHovered = false;
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                scale = 0.95f;
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                scale = 1.05f;
                button.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                scale = 1.0f;
                button.repaint();
            }

            {
                button.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        button.setPreferredSize(new Dimension(
                                (int) (button.getPreferredSize().width * scale),
                                (int) (button.getPreferredSize().height * scale)
                        ));
                    }
                });
            }
        });
        return button;
    }
}