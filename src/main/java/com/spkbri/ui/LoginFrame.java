package com.spkbri.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.spkbri.database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;
    private int mouseX, mouseY;

    public LoginFrame() {
        setTitle("Login - SPK MOORA BRI");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 480);
        setLocationRelativeTo(null);
        
        // Root Panel
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(new Color(218, 224, 233), 1));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // --- LEFT PANE (Corporate Branding / Welcome) ---
        JPanel leftPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Beautiful blue gradient matching BRI branding
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 82, 162), 0, getHeight(), new Color(10, 50, 110));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Optional geometric overlay for premium look
                g2d.setColor(new Color(255, 255, 255, 15));
                g2d.fillOval(-100, -100, 350, 350);
                g2d.fillOval(getWidth() - 150, getHeight() - 150, 300, 300);
            }
        };
        leftPane.setPreferredSize(new Dimension(320, 480));
        leftPane.setLayout(new GridBagLayout());
        leftPane.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.weightx = 1.0;

        JLabel lblLeftLogo = new JLabel();
        try {
            java.io.File logoFile = new java.io.File("logo.png");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon("logo.png");
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(180, 60, Image.SCALE_SMOOTH);
                lblLeftLogo.setIcon(new ImageIcon(scaledImg));
            } else {
                lblLeftLogo.setText("SPK MOORA");
                lblLeftLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
                lblLeftLogo.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            lblLeftLogo.setText("SPK MOORA");
            lblLeftLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblLeftLogo.setForeground(Color.WHITE);
        }
        leftPane.add(lblLeftLogo, gbcLeft);

        gbcLeft.gridy++;
        gbcLeft.insets = new Insets(5, 0, 25, 0);
        JLabel lblLeftSub = new JLabel("KCP ARUNDINA");
        lblLeftSub.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLeftSub.setForeground(new Color(242, 142, 43)); // Accent Orange
        leftPane.add(lblLeftSub, gbcLeft);

        gbcLeft.gridy++;
        gbcLeft.insets = new Insets(0, 0, 10, 0);
        JLabel lblLeftDesc1 = new JLabel("Sistem Penunjang Keputusan");
        lblLeftDesc1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLeftDesc1.setForeground(new Color(220, 230, 245));
        leftPane.add(lblLeftDesc1, gbcLeft);

        gbcLeft.gridy++;
        gbcLeft.insets = new Insets(0, 0, 0, 0);
        JLabel lblLeftDesc2 = new JLabel("Pemilihan Karyawan Terbaik");
        lblLeftDesc2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLeftDesc2.setForeground(new Color(220, 230, 245));
        leftPane.add(lblLeftDesc2, gbcLeft);

        root.add(leftPane, BorderLayout.WEST);

        // --- RIGHT PANE (Form Fields) ---
        JPanel rightPane = new JPanel(new BorderLayout());
        rightPane.setBackground(Color.WHITE);

        // Header Panel (Window control & dragging area)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(430, 35));
        
        // Window drag listeners
        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX - 320, e.getYOnScreen() - mouseY);
            }
        };
        headerPanel.addMouseListener(dragAdapter);
        headerPanel.addMouseMotionListener(dragAdapter);

        // Window Controls (Min, Close)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setBackground(null);

        Dimension btnSize = new Dimension(45, 35);

        // Minimize Button
        JButton btnMin = new JButton("—");
        btnMin.setPreferredSize(btnSize);
        btnMin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMin.setBorderPainted(false);
        btnMin.setContentAreaFilled(false);
        btnMin.setFocusPainted(false);
        btnMin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMin.setForeground(Color.BLACK);
        btnMin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnMin.setOpaque(true);
                btnMin.setBackground(new Color(229, 229, 229)); // Light Gray Hover
                btnMin.setForeground(Color.BLACK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnMin.setOpaque(false);
                btnMin.setForeground(Color.BLACK);
            }
        });
        btnMin.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        // Close Button
        JButton btnClose = new JButton("×");
        btnClose.setPreferredSize(btnSize);
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setForeground(Color.BLACK);
        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setOpaque(true);
                btnClose.setBackground(new Color(232, 17, 35)); // Red Hover
                btnClose.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setOpaque(false);
                btnClose.setForeground(Color.BLACK);
            }
        });
        btnClose.addActionListener(e -> System.exit(0));

        controls.add(btnMin);
        controls.add(btnClose);
        headerPanel.add(controls, BorderLayout.EAST);
        rightPane.add(headerPanel, BorderLayout.NORTH);

        // Form Center Container
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(new EmptyBorder(10, 45, 45, 45));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel lblFormTitle = new JLabel("Selamat Datang");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFormTitle.setForeground(new Color(33, 37, 41));
        formContainer.add(lblFormTitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 30, 0);
        JLabel lblFormSub = new JLabel("Silakan masuk menggunakan akun admin Anda");
        lblFormSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFormSub.setForeground(Color.GRAY);
        formContainer.add(lblFormSub, gbc);

        // Username Field
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(70, 80, 95));
        formContainer.add(lblUser, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(320, 38));
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan username...");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new ImageIcon()); // Spacer/Icon placeholder if needed
        txtUsername.putClientProperty("JComponent.roundRect", true);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formContainer.add(txtUsername, gbc);

        // Password Field
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(70, 80, 95));
        formContainer.add(lblPass, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(320, 38));
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Masukkan password...");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        txtPassword.putClientProperty("JComponent.roundRect", true);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formContainer.add(txtPassword, gbc);

        // Error message label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(220, 53, 69));
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formContainer.add(lblError, gbc);

        // Login Button
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnLogin = new JButton("Masuk");
        btnLogin.setPreferredSize(new Dimension(320, 42));
        btnLogin.setBackground(new Color(0, 82, 162));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");
        btnLogin.addActionListener(e -> attemptLogin());
        formContainer.add(btnLogin, gbc);

        rightPane.add(formContainer, BorderLayout.CENTER);
        root.add(rightPane, BorderLayout.CENTER);
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password tidak boleh kosong!");
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String namaLengkap = rs.getString("nama_lengkap");
                    String role = rs.getString("role");
                    lblError.setText(" ");
                    this.dispose();

                    if ("pimpinan".equalsIgnoreCase(role)) {
                        SwingUtilities.invokeLater(() -> {
                            PimpinanFrame pimpinanFrame = new PimpinanFrame(namaLengkap);
                            pimpinanFrame.setVisible(true);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            MainFrame mainFrame = new MainFrame(namaLengkap);
                            mainFrame.setVisible(true);
                        });
                    }
                } else {
                    lblError.setText("Username atau password salah!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Error database: " + e.getMessage());
        }
    }
}
