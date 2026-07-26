package com.spkbri.ui;

import com.spkbri.core.MooraEngine;
import com.spkbri.database.DatabaseHelper;
import com.spkbri.model.RankingResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.List;

public class DashboardPanel extends JPanel {

    private JLabel lblTotalKaryawan;
    private JLabel lblTotalKriteriaBisnis;
    private JLabel lblTotalKriteriaOps;
    private JTable tblTopBisnis;
    private JTable tblTopOps;
    private DefaultTableModel modelBisnis;
    private DefaultTableModel modelOps;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(null);
        JLabel title = new JLabel("Beranda Utama");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Selamat Datang di SPK Karyawan Terbaik Bank BRI KCP Arundina");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        add(titlePanel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(null);
        cardsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel card1 = createCard("Total Karyawan", "0", new Color(0, 82, 162));
        lblTotalKaryawan = (JLabel) card1.getClientProperty("valLabel");

        JPanel card2 = createCard("Kriteria Bisnis", "0", new Color(242, 142, 43));
        lblTotalKriteriaBisnis = (JLabel) card2.getClientProperty("valLabel");

        JPanel card3 = createCard("Kriteria Operasional", "0", new Color(40, 167, 69));
        lblTotalKriteriaOps = (JLabel) card3.getClientProperty("valLabel");

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setBackground(null);

        JPanel pnlTopBisnis = new JPanel(new BorderLayout());
        pnlTopBisnis.setBackground(Color.WHITE);
        pnlTopBisnis.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        JLabel lblTitleBisnis = new JLabel("Top 3 Divisi Bisnis (Sementara)");
        lblTitleBisnis.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitleBisnis.setForeground(new Color(33, 37, 41));
        lblTitleBisnis.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlTopBisnis.add(lblTitleBisnis, BorderLayout.NORTH);

        modelBisnis = new DefaultTableModel(new Object[]{"Rank", "Nama", "Score (Yi)"}, 0);
        tblTopBisnis = new JTable(modelBisnis);
        tblTopBisnis.setRowHeight(30);
        tblTopBisnis.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTopBisnis.add(new JScrollPane(tblTopBisnis), BorderLayout.CENTER);

        JPanel pnlTopOps = new JPanel(new BorderLayout());
        pnlTopOps.setBackground(Color.WHITE);
        pnlTopOps.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        JLabel lblTitleOps = new JLabel("Top 3 Divisi Operasional (Sementara)");
        lblTitleOps.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitleOps.setForeground(new Color(33, 37, 41));
        lblTitleOps.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlTopOps.add(lblTitleOps, BorderLayout.NORTH);

        modelOps = new DefaultTableModel(new Object[]{"Rank", "Nama", "Score (Yi)"}, 0);
        tblTopOps = new JTable(modelOps);
        tblTopOps.setRowHeight(30);
        tblTopOps.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTopOps.add(new JScrollPane(tblTopOps), BorderLayout.CENTER);

        tablesPanel.add(pnlTopBisnis);
        tablesPanel.add(pnlTopOps);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(null);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(tablesPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createCard(String title, String initialValue, Color leftBorderColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(leftBorderColor);
                g.fillRect(0, 0, 5, getHeight());
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblVal = new JLabel(initialValue);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblVal.setForeground(new Color(33, 37, 41));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.putClientProperty("valLabel", lblVal);

        return card;
    }

    public void refreshData() {

        int karyawanCount = 0;
        int kriteriaBisnisCount = 0;
        int kriteriaOpsCount = 0;

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM karyawan")) {
                if (rs.next()) karyawanCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM kriteria WHERE divisi = 'Bisnis'")) {
                if (rs.next()) kriteriaBisnisCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM kriteria WHERE divisi = 'Operasional'")) {
                if (rs.next()) kriteriaOpsCount = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        lblTotalKaryawan.setText(String.valueOf(karyawanCount));
        lblTotalKriteriaBisnis.setText(String.valueOf(kriteriaBisnisCount));
        lblTotalKriteriaOps.setText(String.valueOf(kriteriaOpsCount));

        modelBisnis.setRowCount(0);
        modelOps.setRowCount(0);

        List<RankingResult> rankingBisnis = MooraEngine.calculate("Bisnis").getRankingResults();
        List<RankingResult> rankingOps = MooraEngine.calculate("Operasional").getRankingResults();

        DecimalFormat df = new DecimalFormat("0.0000");

        int limitBisnis = Math.min(rankingBisnis.size(), 3);
        for (int i = 0; i < limitBisnis; i++) {
            RankingResult r = rankingBisnis.get(i);
            modelBisnis.addRow(new Object[]{i + 1, r.getKaryawan().getNama(), df.format(r.getScore())});
        }

        int limitOps = Math.min(rankingOps.size(), 3);
        for (int i = 0; i < limitOps; i++) {
            RankingResult r = rankingOps.get(i);
            modelOps.addRow(new Object[]{i + 1, r.getKaryawan().getNama(), df.format(r.getScore())});
        }
    }
}
