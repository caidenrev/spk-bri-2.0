package com.spkbri.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    
    // Sidebar Buttons
    private JButton btnDashboard;
    private JButton btnKaryawan;
    private JButton btnKriteria;
    private JButton btnPenilaian;
    private JButton btnReport;
    private JButton btnLogout;

    // Panels
    private DashboardPanel dashboardPanel;
    private KaryawanPanel karyawanPanel;
    private KriteriaPanel kriteriaPanel;
    private PenilaianPanel penilaianPanel;
    
    // Submenu buttons
    private JButton subBtnDataBisnis;
    private JButton subBtnDataOps;
    private JButton subBtnHitungBisnis;
    private JButton subBtnHitungOps;
    private JButton subBtnRankingBisnis;
    private JButton subBtnRankingOps;

    private int mouseX, mouseY;

    public MainFrame(String adminName) {
        setTitle("SPK MOORA Karyawan Terbaik - Bank BRI KCP Arundina");
        setSize(1024, 680);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root container with border
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // Left Sidebar Panel (Dark Theme)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(15, 23, 42)); // Premium Slate Blue/Dark
        sidebar.setPreferredSize(new Dimension(240, 680));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Sidebar Header (Logo / Title)
        JLabel lblLogo = new JLabel();
        try {
            java.io.File logoFile = new java.io.File("logo.png");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon("logo.png");
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(160, 50, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(scaledImg));
            } else {
                lblLogo.setText("SPK MOORA");
                lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
                lblLogo.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            lblLogo.setText("SPK MOORA");
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblLogo.setForeground(Color.WHITE);
        }
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblLogo.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblSubLogo = new JLabel("BANK BRI KCP Arundina");
        lblSubLogo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubLogo.setForeground(new Color(148, 163, 184));
        lblSubLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubLogo.setBorder(new EmptyBorder(0, 0, 25, 0));

        sidebar.add(lblLogo);
        sidebar.add(lblSubLogo);

        // Active Admin Info
        JLabel lblUserTitle = new JLabel("ADMINISTRATOR");
        lblUserTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUserTitle.setForeground(new Color(100, 116, 139));
        lblUserTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUserName = new JLabel(adminName);
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserName.setForeground(Color.WHITE);
        lblUserName.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblUserName.setBorder(new EmptyBorder(0, 0, 30, 0));

        sidebar.add(lblUserTitle);
        sidebar.add(lblUserName);

        // Sidebar Menu Buttons
        btnDashboard = createSidebarButton("Dashboard (Beranda)");
        btnKaryawan = createSidebarButton("Data Karyawan");
        btnKriteria = createSidebarButton("Data Kriteria");
        btnPenilaian = createSidebarButton("Input Penilaian");
        btnReport = createSidebarButton("Laporan \u25BC");
        btnLogout = createSidebarButton("Logout");

        // Action Listeners for Sidebar Buttons
        btnDashboard.addActionListener(e -> switchCard("Dashboard", btnDashboard));
        btnKaryawan.addActionListener(e -> switchCard("Karyawan", btnKaryawan));
        btnKriteria.addActionListener(e -> switchCard("Kriteria", btnKriteria));
        btnPenilaian.addActionListener(e -> switchCard("Penilaian", btnPenilaian));
        
        // Report Submenu Panel
        JPanel submenuPanel = new JPanel();
        submenuPanel.setLayout(new BoxLayout(submenuPanel, BoxLayout.Y_AXIS));
        submenuPanel.setBackground(new Color(15, 23, 42));
        submenuPanel.setVisible(false);

        subBtnDataBisnis = createSubMenuButton("- Laporan Data Karyawan (Bisnis)");
        subBtnDataOps = createSubMenuButton("- Laporan Data Karyawan (Ops)");
        subBtnHitungBisnis = createSubMenuButton("- Perhitungan MOORA (Bisnis)");
        subBtnHitungOps = createSubMenuButton("- Perhitungan MOORA (Ops)");
        subBtnRankingBisnis = createSubMenuButton("- Hasil Ranking (Bisnis)");
        subBtnRankingOps = createSubMenuButton("- Hasil Ranking (Ops)");

        submenuPanel.add(subBtnDataBisnis);
        submenuPanel.add(subBtnDataOps);
        submenuPanel.add(subBtnHitungBisnis);
        submenuPanel.add(subBtnHitungOps);
        submenuPanel.add(subBtnRankingBisnis);
        submenuPanel.add(subBtnRankingOps);

        subBtnDataBisnis.addActionListener(e -> switchReport("DATA_KARYAWAN", "Bisnis", subBtnDataBisnis));
        subBtnDataOps.addActionListener(e -> switchReport("DATA_KARYAWAN", "Operasional", subBtnDataOps));
        subBtnHitungBisnis.addActionListener(e -> switchReport("PERHITUNGAN", "Bisnis", subBtnHitungBisnis));
        subBtnHitungOps.addActionListener(e -> switchReport("PERHITUNGAN", "Operasional", subBtnHitungOps));
        subBtnRankingBisnis.addActionListener(e -> switchReport("RANKING", "Bisnis", subBtnRankingBisnis));
        subBtnRankingOps.addActionListener(e -> switchReport("RANKING", "Operasional", subBtnRankingOps));

        btnReport.addActionListener(e -> {
            boolean isVisible = submenuPanel.isVisible();
            submenuPanel.setVisible(!isVisible);
            btnReport.setText(isVisible ? "Laporan \u25BC" : "Laporan \u25B2");
            sidebar.revalidate();
            sidebar.repaint();
        });
        btnLogout.addActionListener(e -> logout());

        sidebar.add(btnDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnKaryawan);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnKriteria);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnPenilaian);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnReport);
        sidebar.add(submenuPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebar.add(btnLogout);

        root.add(sidebar, BorderLayout.WEST);

        // Right Main Content Panel (CardLayout)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Initialize Panels
        dashboardPanel = new DashboardPanel();
        karyawanPanel = new KaryawanPanel(dashboardPanel);
        kriteriaPanel = new KriteriaPanel(dashboardPanel);
        penilaianPanel = new PenilaianPanel(dashboardPanel);

        mainContentPanel.add(dashboardPanel, "Dashboard");
        mainContentPanel.add(karyawanPanel, "Karyawan");
        mainContentPanel.add(kriteriaPanel, "Kriteria");
        mainContentPanel.add(penilaianPanel, "Penilaian");

        // Header Panel (Window Control & Draggable)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setPreferredSize(new Dimension(1024, 35));

        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        headerPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX - 240, e.getYOnScreen() - mouseY);
            }
        });

        // Window Controls (Min, Max, Close)
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
        btnMin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnMin.setOpaque(true);
                btnMin.setBackground(new Color(229, 229, 229)); // Light Gray Hover
                btnMin.setForeground(Color.BLACK);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnMin.setOpaque(false);
                btnMin.setForeground(Color.BLACK);
            }
        });
        btnMin.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        // Maximize Button
        JButton btnMax = new JButton("▢");
        btnMax.setPreferredSize(btnSize);
        btnMax.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnMax.setBorderPainted(false);
        btnMax.setContentAreaFilled(false);
        btnMax.setFocusPainted(false);
        btnMax.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMax.setForeground(Color.BLACK);
        btnMax.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnMax.setOpaque(true);
                btnMax.setBackground(new Color(229, 229, 229)); // Light Gray Hover
                btnMax.setForeground(Color.BLACK);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnMax.setOpaque(false);
                btnMax.setForeground(Color.BLACK);
            }
        });
        btnMax.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
                btnMax.setText("▢");
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                btnMax.setText("⧉"); // Double overlapping squares
            }
        });

        // Close Button
        JButton btnClose = new JButton("×");
        btnClose.setPreferredSize(btnSize);
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setForeground(Color.BLACK);
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnClose.setOpaque(true);
                btnClose.setBackground(new Color(232, 17, 35)); // Red Hover
                btnClose.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnClose.setOpaque(false);
                btnClose.setForeground(Color.BLACK);
            }
        });
        btnClose.addActionListener(e -> System.exit(0));

        controls.add(btnMin);
        controls.add(btnMax);
        controls.add(btnClose);
        headerPanel.add(controls, BorderLayout.EAST);

        // Layout Integration
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(headerPanel, BorderLayout.NORTH);
        rightContainer.add(mainContentPanel, BorderLayout.CENTER);
        
        root.add(rightContainer, BorderLayout.CENTER);

        // Highlight first menu
        highlightButton(btnDashboard);
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(148, 163, 184));
        btn.setBackground(new Color(15, 23, 42));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getForeground() != Color.WHITE) {
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() == new Color(15, 23, 42)) {
                    btn.setForeground(new Color(148, 163, 184));
                }
            }
        });

        return btn;
    }

    private JButton createSubMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(148, 163, 184));
        btn.setBackground(new Color(15, 23, 42));
        btn.setBorder(new EmptyBorder(8, 30, 8, 10)); // indented
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 30));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getForeground() != Color.WHITE) {
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() == new Color(15, 23, 42)) {
                    btn.setForeground(new Color(148, 163, 184));
                }
            }
        });

        return btn;
    }

    private void switchCard(String name, JButton source) {
        cardLayout.show(mainContentPanel, name);
        highlightButton(source);

        // Refresh dynamic panels
        if ("Dashboard".equals(name)) {
            dashboardPanel.refreshData();
        } else if ("Penilaian".equals(name)) {
            penilaianPanel.refreshTabs();
        }
    }
    
    private void switchReport(String type, String divisi, JButton source) {
        highlightButton(source);
        
        // Remove existing dynamic report panel if any
        Component[] comps = mainContentPanel.getComponents();
        for (Component c : comps) {
            if (c.getName() != null && c.getName().equals("ReportDyn")) {
                mainContentPanel.remove(c);
                break;
            }
        }
        
        ReportPanel rp = new ReportPanel(type, divisi);
        rp.setName("ReportDyn");
        mainContentPanel.add(rp, "ReportDyn");
        cardLayout.show(mainContentPanel, "ReportDyn");
    }

    private void highlightButton(JButton activeBtn) {
        // Reset all buttons
        JButton[] buttons = {btnDashboard, btnKaryawan, btnKriteria, btnPenilaian, btnReport, btnLogout,
                             subBtnDataBisnis, subBtnDataOps, subBtnHitungBisnis, subBtnHitungOps, subBtnRankingBisnis, subBtnRankingOps};
        for (JButton b : buttons) {
            if (b == null) continue;
            b.setForeground(new Color(148, 163, 184));
            b.setOpaque(false);
            b.setBackground(new Color(15, 23, 42));
        }

        // Highlight active button
        activeBtn.setForeground(Color.WHITE);
        activeBtn.setOpaque(true);
        activeBtn.setBackground(new Color(30, 41, 59)); // Lighter dark
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
