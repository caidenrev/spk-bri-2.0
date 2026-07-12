package com.spkbri.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Window utama untuk role Pimpinan.
 * Hak akses: lihat data karyawan, input/update penilaian, lihat hasil ranking.
 * Tidak ada akses ke manajemen karyawan/kriteria maupun fitur ekspor.
 */
public class PimpinanFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    private JButton btnKaryawan;
    private JButton btnPenilaian;
    private JButton btnRanking;
    private JButton btnLogout;

    private PimpinanKaryawanPanel karyawanPanel;
    private PimpinanPenilaianPanel penilaianPanel;
    private PimpinanRankingPanel rankingPanel;

    private int mouseX, mouseY;

    public PimpinanFrame(String namaLengkap) {
        setTitle("SPK MOORA — Portal Pimpinan | Bank BRI KCP Arundina");
        setSize(1024, 680);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(10, 50, 110)); // Biru gelap BRI
        sidebar.setPreferredSize(new Dimension(240, 680));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Logo
        JLabel lblLogo = new JLabel();
        try {
            java.io.File logoFile = new java.io.File("logo.png");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon("logo.png");
                Image scaledImg = icon.getImage().getScaledInstance(160, 50, Image.SCALE_SMOOTH);
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
        lblLogo.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel lblSubLogo = new JLabel("BANK BRI KCP Arundina");
        lblSubLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubLogo.setForeground(new Color(148, 180, 220));
        lblSubLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubLogo.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Badge role
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgePanel.setBackground(null);
        badgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lblBadge = new JLabel("  PORTAL PIMPINAN  ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setBackground(new Color(242, 142, 43));
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(3, 6, 3, 6));
        badgePanel.add(lblBadge);
        badgePanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Nama pimpinan
        JLabel lblRoleTitle = new JLabel("LOGIN SEBAGAI");
        lblRoleTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblRoleTitle.setForeground(new Color(100, 140, 180));
        lblRoleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNama = new JLabel(namaLengkap);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNama.setForeground(Color.WHITE);
        lblNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNama.setBorder(new EmptyBorder(0, 0, 25, 0));

        sidebar.add(lblLogo);
        sidebar.add(lblSubLogo);
        sidebar.add(badgePanel);
        sidebar.add(lblRoleTitle);
        sidebar.add(lblNama);

        // Separator label
        JLabel lblMenuSep = new JLabel("MENU UTAMA");
        lblMenuSep.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenuSep.setForeground(new Color(100, 140, 180));
        lblMenuSep.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenuSep.setBorder(new EmptyBorder(0, 0, 8, 0));
        sidebar.add(lblMenuSep);

        // Tombol navigasi
        btnKaryawan = createSidebarButton("Data Karyawan");
        btnPenilaian = createSidebarButton("Input Penilaian");
        btnRanking = createSidebarButton("Hasil Ranking");
        btnLogout = createSidebarButton("Logout");

        btnKaryawan.addActionListener(e -> switchCard("Karyawan", btnKaryawan));
        btnPenilaian.addActionListener(e -> switchCard("Penilaian", btnPenilaian));
        btnRanking.addActionListener(e -> switchCard("Ranking", btnRanking));
        btnLogout.addActionListener(e -> logout());

        sidebar.add(btnKaryawan);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnPenilaian);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnRanking);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebar.add(btnLogout);

        root.add(sidebar, BorderLayout.WEST);

        // ===== CONTENT AREA =====
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        karyawanPanel = new PimpinanKaryawanPanel();
        penilaianPanel = new PimpinanPenilaianPanel();
        rankingPanel = new PimpinanRankingPanel();

        mainContentPanel.add(karyawanPanel, "Karyawan");
        mainContentPanel.add(penilaianPanel, "Penilaian");
        mainContentPanel.add(rankingPanel, "Ranking");

        // ===== HEADER (drag + window controls) =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setPreferredSize(new Dimension(784, 35));

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

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setBackground(null);
        Dimension btnSize = new Dimension(45, 35);

        JButton btnMin = new JButton("—");
        styleWindowBtn(btnMin, btnSize, false);
        btnMin.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        JButton btnMax = new JButton("▢");
        styleWindowBtn(btnMax, btnSize, false);
        btnMax.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
                btnMax.setText("▢");
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                btnMax.setText("⧉");
            }
        });

        JButton btnClose = new JButton("×");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        styleWindowBtn(btnClose, btnSize, true);
        btnClose.addActionListener(e -> System.exit(0));

        controls.add(btnMin);
        controls.add(btnMax);
        controls.add(btnClose);
        headerPanel.add(controls, BorderLayout.EAST);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(headerPanel, BorderLayout.NORTH);
        rightContainer.add(mainContentPanel, BorderLayout.CENTER);
        root.add(rightContainer, BorderLayout.CENTER);

        // Default aktif: Data Karyawan
        highlightButton(btnKaryawan);
        cardLayout.show(mainContentPanel, "Karyawan");
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(148, 180, 220));
        btn.setBackground(new Color(10, 50, 110));
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
                if (!btn.isOpaque()) btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.isOpaque()) btn.setForeground(new Color(148, 180, 220));
            }
        });
        return btn;
    }

    private void styleWindowBtn(JButton btn, Dimension size, boolean isClose) {
        if (btn.getFont() == null || btn.getText().equals("—") || btn.getText().equals("▢")) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        btn.setPreferredSize(size);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setForeground(Color.BLACK);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(isClose ? new Color(232, 17, 35) : new Color(229, 229, 229));
                btn.setForeground(isClose ? Color.WHITE : Color.BLACK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
                btn.setForeground(Color.BLACK);
            }
        });
    }

    private void switchCard(String name, JButton source) {
        cardLayout.show(mainContentPanel, name);
        highlightButton(source);
    }

    private void highlightButton(JButton active) {
        JButton[] all = {btnKaryawan, btnPenilaian, btnRanking, btnLogout};
        for (JButton b : all) {
            b.setForeground(new Color(148, 180, 220));
            b.setOpaque(false);
            b.setBackground(new Color(10, 50, 110));
        }
        active.setForeground(Color.WHITE);
        active.setOpaque(true);
        active.setBackground(new Color(0, 82, 162));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
