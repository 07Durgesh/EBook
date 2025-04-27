package com.bookstore.gui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.bookstore.db.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoginSignup extends JFrame {
    private final CardLayout cardLayout;
    private JPanel mainPanel;
    private BackgroundPanel backgroundPanel;
    private Point initialClick;

    public LoginSignup() {
        System.out.println("LoginSignup initialized with Celestial Light theme on April 25, 2025");
        setTitle("📚 Bookstore");
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createSignupPanel(), "signup");

        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());

        JPanel titleBar = createTitleBar();
        backgroundPanel.add(titleBar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBackground(new Color(160, 200, 215, 200)); // Updated to match SellerPanel

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(50, 50, 50, 50);
        centerPanel.add(mainPanel, gbc);

        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        add(backgroundPanel);
        setVisible(true);
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(20, 25, 30));
        titleBar.setPreferredSize(new Dimension(0, 50));

        JLabel titleLabel = new JLabel("📚 Bookstore");
        titleLabel.setForeground(new Color(255, 150, 200));
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlPanel.setOpaque(false);

        JButton minimizeButton = createTitleBarButton("−", new Color(100, 150, 200));
        minimizeButton.addActionListener(e -> setState(Frame.ICONIFIED));

        JButton maximizeButton = createTitleBarButton("□", new Color(100, 150, 200));
        maximizeButton.addActionListener(e -> {
            if (getExtendedState() == MAXIMIZED_BOTH) setExtendedState(NORMAL);
            else setExtendedState(MAXIMIZED_BOTH);
        });

        JButton closeButton = createTitleBarButton("✕", new Color(250, 100, 100));
        closeButton.addActionListener(e -> System.exit(0));

        controlPanel.add(minimizeButton);
        controlPanel.add(maximizeButton);
        controlPanel.add(closeButton);
        titleBar.add(controlPanel, BorderLayout.EAST);

        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() != MAXIMIZED_BOTH) {
                    int x = getLocation().x + e.getX() - initialClick.x;
                    int y = getLocation().y + e.getY() - initialClick.y;
                    setLocation(x, y);
                }
            }
        });

        return titleBar;
    }

    private JButton createTitleBarButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Montserrat", Font.PLAIN, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(40, 30));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(text.equals("✕") ? new Color(250, 100, 100) : bgColor);
            }
        });
        return button;
    }

    class BackgroundPanel extends JPanel {
        Random rand = new Random();
        private final List<Point> particles = new ArrayList<>();

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

            // Celestial gradient with rotation
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
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");
        usernameLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        passwordLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(0, 0, 0)); // Changed to black
        passwordLabel.setForeground(new Color(0, 0, 0)); // Changed to black

        PlaceholderTextField usernameField = new PlaceholderTextField("Enter username");
        PlaceholderPasswordField passwordField = new PlaceholderPasswordField("Enter password");

        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridy = 1;
        panel.add(usernameField, gbc);
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);
        gbc.gridy = 3;
        panel.add(passwordField, gbc);

        JButton loginBtn = createStyledButton("Sign In", new Color(100, 200, 150), new Color(150, 250, 200));
        gbc.insets = new Insets(30, 15, 15, 15);
        gbc.gridy = 4;
        panel.add(loginBtn, gbc);

        JButton goToSignupBtn = createLinkButton("New to Bookstore? Sign Up", new Color(0, 0, 0)); // Changed to black
        gbc.insets = new Insets(10, 15, 30, 15);
        gbc.gridy = 5;
        panel.add(goToSignupBtn, gbc);

        goToSignupBtn.addActionListener(e -> cardLayout.show(mainPanel, "signup"));

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.equals("Enter username") || password.equals("Enter password") || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password must not be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    System.out.println("Login successful for user: " + username + ", role: " + role);
                    openPanelByRole(username, role);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 150, 200));
        gbc.insets = new Insets(30, 15, 40, 15);
        panel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username");
        JLabel emailLabel = new JLabel("Email");
        JLabel passwordLabel = new JLabel("Password");
        JLabel roleLabel = new JLabel("Role");
        usernameLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        emailLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        passwordLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        roleLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(0, 0, 0)); // Changed to black
        emailLabel.setForeground(new Color(0, 0, 0)); // Changed to black
        passwordLabel.setForeground(new Color(0, 0, 0)); // Changed to black
        roleLabel.setForeground(new Color(0, 0, 0)); // Changed to black

        PlaceholderTextField usernameField = new PlaceholderTextField("Enter username");
        PlaceholderTextField emailField = new PlaceholderTextField("Enter email");
        PlaceholderPasswordField passwordField = new PlaceholderPasswordField("Enter password");
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"buyer", "seller"});
        styleComboBox(roleBox);

        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);
        gbc.gridy = 2;
        panel.add(usernameField, gbc);
        gbc.gridy = 3;
        panel.add(emailLabel, gbc);
        gbc.gridy = 4;
        panel.add(emailField, gbc);
        gbc.gridy = 5;
        panel.add(passwordLabel, gbc);
        gbc.gridy = 6;
        panel.add(passwordField, gbc);
        gbc.gridy = 7;
        panel.add(roleLabel, gbc);
        gbc.gridy = 8;
        panel.add(roleBox, gbc);

        JButton signupBtn = createStyledButton("Sign Up", new Color(200, 100, 150), new Color(250, 150, 200));
        gbc.insets = new Insets(30, 15, 15, 15);
        gbc.gridy = 9;
        panel.add(signupBtn, gbc);

        JButton backToLoginBtn = createLinkButton("Already have an account? Sign In", new Color(0, 0, 0)); // Changed to black
        gbc.insets = new Insets(10, 15, 30, 15);
        gbc.gridy = 10;
        panel.add(backToLoginBtn, gbc);

        backToLoginBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        signupBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();

            if (username.equals("Enter username") || email.equals("Enter email") || password.equals("Enter password") ||
                    username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, role);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Signup successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "login");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Signup failed. Username may be taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // Custom JTextField with placeholder support
    class PlaceholderTextField extends JTextField {
        private String placeholder;

        public PlaceholderTextField(String placeholder) {
            super(20);
            this.placeholder = placeholder;
            setText(placeholder);
            setForeground(Color.GRAY);
            styleTextField(this, new Color(160, 200, 215, 200));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                    }
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(135, 206, 235, 50), 2),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)
                    ));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(135, 206, 235, 30), 1),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)
                    ));
                    repaint();
                }
            });
        }
    }

    // Custom JPasswordField with placeholder support
    class PlaceholderPasswordField extends JPasswordField {
        private String placeholder;

        public PlaceholderPasswordField(String placeholder) {
            super(20);
            this.placeholder = placeholder;
            setText(placeholder);
            setForeground(Color.GRAY);
            setEchoChar((char) 0); // Show placeholder text
            styleTextField(this, new Color(160, 200, 215, 200));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(getPassword()).equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                        setEchoChar('•'); // Show bullets for password
                    }
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(135, 206, 235, 50), 2),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)
                    ));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (new String(getPassword()).isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        setEchoChar((char) 0); // Show placeholder text
                    }
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(135, 206, 235, 30), 1),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)
                    ));
                    repaint();
                }
            });
        }
    }

    private void styleTextField(JTextComponent field, Color bgColor) {
        field.setFont(new Font("Montserrat", Font.PLAIN, 14));
        field.setBackground(bgColor);
        field.setForeground(Color.WHITE); // Default foreground (will be overridden by placeholder logic)
        field.setCaretColor(new Color(135, 206, 235));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235, 30), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setOpaque(true);
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Montserrat", Font.PLAIN, 14));
        comboBox.setBackground(new Color(160, 200, 215, 200));
        comboBox.setForeground(Color.BLACK);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 235, 30), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        comboBox.setOpaque(true);
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
                            new Color[]{new Color(255, 215, 0, (int) (pulseAlpha * 150)), new Color(255, 215, 0, 0)}));
                    g2.fillRoundRect(5, 5, w - 10, h - 10, 15, 15);
                }
                super.paintComponent(g);
            }
        }

        CelestialButton button = new CelestialButton(text);
        button.setFont(new Font("Montserrat", Font.BOLD, 14));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        Timer pulseTimer = new Timer(50, (e) -> {
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

    private JButton createLinkButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Montserrat", Font.PLAIN, 14));
        button.setForeground(color);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(color.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(color);
            }
        });
        return button;
    }

    private void openPanelByRole(String username, String role) {
        System.out.println("openPanelByRole called for user: " + username + ", role: " + role);
        SwingUtilities.invokeLater(() -> {
            JFrame roleFrame = new JFrame("Bookstore - " + role.toUpperCase() + " | User: " + username);
            roleFrame.setUndecorated(true);
            roleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            roleFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(new Color(40, 50, 60));

            JPanel titleBar = createCustomTitleBar("Bookstore - " + role.toUpperCase() + " | User: " + username, roleFrame);
            contentPanel.add(titleBar, BorderLayout.NORTH);

            JPanel rolePanel;
            if ("buyer".equalsIgnoreCase(role)) {
                System.out.println("Opening BuyerPanel for user: " + username);
                rolePanel = new BuyerPanel(username);
            } else {
                System.out.println("Opening SellerPanel for user: " + username);
                rolePanel = new SellerPanel(username);
            }

            contentPanel.add(rolePanel, BorderLayout.CENTER);
            roleFrame.setContentPane(contentPanel);
            roleFrame.setVisible(true);
            dispose();
        });
    }

    private JPanel createCustomTitleBar(String title, JFrame frame) {
        System.out.println("Creating custom title bar for: " + title);
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(20, 25, 30));
        titleBar.setPreferredSize(new Dimension(0, 50));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(255, 150, 200));
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlPanel.setOpaque(false);

        JButton minimizeButton = createTitleBarButton("−", new Color(100, 150, 200));
        minimizeButton.addActionListener(e -> frame.setState(Frame.ICONIFIED));

        JButton maximizeButton = createTitleBarButton("□", new Color(100, 150, 200));
        maximizeButton.addActionListener(e -> {
            if (frame.getExtendedState() == MAXIMIZED_BOTH) frame.setExtendedState(NORMAL);
            else frame.setExtendedState(MAXIMIZED_BOTH);
        });

        JButton closeButton = createTitleBarButton("✕", new Color(250, 100, 100));
        closeButton.addActionListener(e -> frame.dispose());

        controlPanel.add(minimizeButton);
        controlPanel.add(maximizeButton);
        controlPanel.add(closeButton);
        titleBar.add(controlPanel, BorderLayout.EAST);

        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (frame.getExtendedState() != MAXIMIZED_BOTH) {
                    int x = frame.getLocation().x + e.getX() - initialClick.x;
                    int y = frame.getLocation().y + e.getY() - initialClick.y;
                    frame.setLocation(x, y);
                }
            }
        });

        return titleBar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginSignup::new);
    }

    static {
        UIManager.put("OptionPane.background", new Color(160, 200, 215, 180));
        UIManager.put("Panel.background", new Color(160, 200, 215, 180));
        UIManager.put("OptionPane.messageForeground", Color.BLACK);
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("TextField.background", new Color(160, 200, 215, 200));
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("TextField.caretForeground", new Color(135, 206, 235));
        UIManager.put("PasswordField.background", new Color(160, 200, 215, 200));
        UIManager.put("PasswordField.foreground", Color.BLACK);
        UIManager.put("PasswordField.caretForeground", new Color(135, 206, 235));
        UIManager.put("Button.background", new Color(135, 206, 235));
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.focus", new Color(160, 200, 215, 180));
        UIManager.put("Button.border", BorderFactory.createLineBorder(new Color(255, 215, 0), 1));
        UIManager.put("InternalFrame.activeTitleBackground", new Color(20, 25, 30));
    }
}