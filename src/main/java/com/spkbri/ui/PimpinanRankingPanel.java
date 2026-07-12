package com.spkbri.ui;

import com.spkbri.core.MooraEngine;
import com.spkbri.model.RankingResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel hasil ranking MOORA untuk Pimpinan — read-only, tanpa fitur ekspor.
 */
public class PimpinanRankingPanel extends JPanel {

    public PimpinanRankingPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel("Hasil Ranking Karyawan Terbaik");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Hasil kalkulasi metode MOORA — perankingan optimasi nilai kinerja karyawan");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabbedPane.addTab("Ranking Divisi Bisnis", new RankingDivisiPanel("Bisnis"));
        tabbedPane.addTab("Ranking Divisi Operasional", new RankingDivisiPanel("Operasional"));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private static class RankingDivisiPanel extends JPanel {

        private final String divisi;
        private DefaultTableModel tableModel;
        private JLabel lblConclusion;
        private JPanel conclusionPanel;
        private Timer animationTimer;
        private List<RankingResult> rankingResults = new ArrayList<>();

        public RankingDivisiPanel(String divisi) {
            this.divisi = divisi;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            // Toolbar — hanya tombol refresh, tanpa ekspor
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            toolbar.setBackground(null);
            toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));

            JButton btnRefresh = new JButton("Perbarui Ranking");
            btnRefresh.setBackground(new Color(0, 82, 162));
            btnRefresh.setForeground(Color.WHITE);
            btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRefresh.addActionListener(e -> loadData());
            toolbar.add(btnRefresh);
            add(toolbar, BorderLayout.NORTH);

            // Tabel ranking
            tableModel = new DefaultTableModel(
                    new Object[]{"Rank", "NIK", "Nama Karyawan", "Divisi", "Score (Yi)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable tblRanking = new JTable(tableModel);
            tblRanking.setRowHeight(30);
            tblRanking.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblRanking.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tblRanking.getTableHeader().setBackground(new Color(0, 82, 162));
            tblRanking.getTableHeader().setForeground(Color.WHITE);
            tblRanking.getColumnModel().getColumn(0).setMaxWidth(60);
            tblRanking.getColumnModel().getColumn(1).setMaxWidth(130);
            tblRanking.getColumnModel().getColumn(3).setMaxWidth(130);

            // Highlight baris pertama dengan warna emas
            tblRanking.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
                    if (!isSelected) {
                        if (row == 0) {
                            c.setBackground(new Color(255, 248, 220)); // Warna emas muda untuk rank 1
                            c.setForeground(new Color(133, 77, 14));
                        } else if (row == 1) {
                            c.setBackground(new Color(245, 245, 245));
                            c.setForeground(Color.DARK_GRAY);
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.DARK_GRAY);
                        }
                    }
                    return c;
                }
            });

            add(new JScrollPane(tblRanking), BorderLayout.CENTER);

            // Panel kesimpulan
            conclusionPanel = new JPanel(new BorderLayout());
            conclusionPanel.setBackground(new Color(245, 247, 250));
            conclusionPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                    new EmptyBorder(15, 20, 15, 20)
            ));
            lblConclusion = new JLabel("<html>Klik <b>Perbarui Ranking</b> untuk melihat hasil kalkulasi terbaru.</html>");
            lblConclusion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            conclusionPanel.add(lblConclusion, BorderLayout.CENTER);
            add(conclusionPanel, BorderLayout.SOUTH);

            loadData();
        }

        public void loadData() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }

            tableModel.setRowCount(0);
            conclusionPanel.setBackground(new Color(245, 247, 250));

            final int[] step = {0};
            lblConclusion.setText("<html><span style='color:#0066cc; font-weight:bold;'>MOORA</span> &nbsp;[1/4] Mengambil data dari database...</html>");

            animationTimer = new Timer(350, null);
            animationTimer.addActionListener(e -> {
                step[0]++;
                if (step[0] == 1) {
                    lblConclusion.setText("<html><span style='color:#0066cc; font-weight:bold;'>MOORA</span> &nbsp;[2/4] Membentuk matriks keputusan & normalisasi...</html>");
                } else if (step[0] == 2) {
                    lblConclusion.setText("<html><span style='color:#0066cc; font-weight:bold;'>MOORA</span> &nbsp;[3/4] Menghitung pembobotan kriteria...</html>");
                } else if (step[0] == 3) {
                    lblConclusion.setText("<html><span style='color:#0066cc; font-weight:bold;'>MOORA</span> &nbsp;[4/4] Menyusun perankingan akhir Yi...</html>");
                } else if (step[0] >= 4) {
                    animationTimer.stop();

                    rankingResults = MooraEngine.calculate(divisi);
                    DecimalFormat df = new DecimalFormat("0.0000");

                    for (RankingResult r : rankingResults) {
                        tableModel.addRow(new Object[]{
                                r.getRank(),
                                r.getKaryawan().getNik(),
                                r.getKaryawan().getNama(),
                                r.getKaryawan().getDivisi(),
                                df.format(r.getScore())
                        });
                    }

                    if (!rankingResults.isEmpty()) {
                        RankingResult best = rankingResults.get(0);
                        lblConclusion.setText("<html>" +
                                "<div style='font-family:\"Segoe UI\",sans-serif;'>" +
                                "<span style='font-size:11px;font-weight:bold;color:#2e7d32;'>KARYAWAN TERBAIK — DIVISI " + divisi.toUpperCase() + "</span><br>" +
                                "<span style='font-size:17px;font-weight:bold;color:#1a5e20;'>🏆 " + best.getKaryawan().getNama() + "</span>" +
                                "<span style='font-size:12px;color:#555;'>&nbsp;&nbsp;NIK: " + best.getKaryawan().getNik() + "</span><br>" +
                                "<span style='font-size:12px;color:#333;'>Skor Optimasi (Yi): <b>" + df.format(best.getScore()) + "</b> &nbsp;|&nbsp; " +
                                "Total karyawan dievaluasi: <b>" + rankingResults.size() + "</b></span>" +
                                "</div></html>");
                        conclusionPanel.setBackground(new Color(230, 245, 235));
                        conclusionPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(195, 230, 203)),
                                new EmptyBorder(15, 20, 15, 20)
                        ));
                    } else {
                        lblConclusion.setText("<html>" +
                                "<span style='color:#666;font-size:13px;'>Belum ada data penilaian yang cukup untuk kalkulasi ranking.</span>" +
                                "</html>");
                        conclusionPanel.setBackground(new Color(245, 247, 250));
                    }
                }
            });
            animationTimer.start();
        }
    }
}
