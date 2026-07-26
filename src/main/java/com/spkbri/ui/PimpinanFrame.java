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

    private PimpinanKaryawanPanel karyawanPanel;
    private PimpinanPenilaianPanel penilaianPanel;

    private JButton btnKaryawan;
    private JButton btnPenilaian;
    private JButton btnReport;
    private JButton btnLogout;

    private JButton subBtnDataBisnis;
    private JButton subBtnDataOps;
    private JButton subBtnHitungBisnis;
    private JButton subBtnHitungOps;
    private JButton subBtnRankingBisnis;
    private JButton subBtnRankingOps;

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

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(10, 50, 110));
        sidebar.setPreferredSize(new Dimension(240, 680));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

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

        JLabel lblMenuSep = new JLabel("MENU UTAMA");
        lblMenuSep.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenuSep.setForeground(new Color(100, 140, 180));
        lblMenuSep.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenuSep.setBorder(new EmptyBorder(0, 0, 8, 0));
        sidebar.add(lblMenuSep);

        btnKaryawan = createSidebarButton("Data Karyawan");
        btnPenilaian = createSidebarButton("Input Penilaian");
        btnReport = createSidebarButton("Laporan \u25BC");
        btnLogout = createSidebarButton("Logout");

        JPanel submenuPanel = new JPanel();
        submenuPanel.setLayout(new BoxLayout(submenuPanel, BoxLayout.Y_AXIS));
        submenuPanel.setBackground(new Color(10, 50, 110));
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

        btnKaryawan.addActionListener(e -> switchCard("Karyawan", btnKaryawan));
        btnPenilaian.addActionListener(e -> switchCard("Penilaian", btnPenilaian));
        btnLogout.addActionListener(e -> logout());

        sidebar.add(btnKaryawan);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnPenilaian);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnReport);
        sidebar.add(submenuPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));
        sidebar.add(btnLogout);

        root.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        karyawanPanel = new PimpinanKaryawanPanel();
        penilaianPanel = new PimpinanPenilaianPanel();

        mainContentPanel.add(karyawanPanel, "Karyawan");
        mainContentPanel.add(penilaianPanel, "Penilaian");

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

    private JButton createSubMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(148, 180, 220));
        btn.setBackground(new Color(10, 50, 110));
        btn.setBorder(new EmptyBorder(8, 30, 8, 10));
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
                if (btn.getBackground() == new Color(10, 50, 110)) {
                    btn.setForeground(new Color(148, 180, 220));
                }
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

        if ("Penilaian".equals(name)) {
            penilaianPanel.refreshTabs();
        } else if ("Karyawan".equals(name)) {
            karyawanPanel.refreshData();
        }
    }

    private void switchReport(String type, String divisi, JButton source) {
        highlightButton(source);

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

    private void highlightButton(JButton active) {
        JButton[] all = {btnKaryawan, btnPenilaian, btnReport, btnLogout,
                         subBtnDataBisnis, subBtnDataOps, subBtnHitungBisnis, subBtnHitungOps, subBtnRankingBisnis, subBtnRankingOps};
        for (JButton b : all) {
            if (b == null) continue;
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
