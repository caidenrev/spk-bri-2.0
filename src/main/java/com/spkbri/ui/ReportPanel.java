package com.spkbri.ui;

import com.spkbri.core.MooraEngine;
import com.spkbri.model.RankingResult;
import com.spkbri.util.ExportHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ReportPanel extends JPanel {

    private ReportDivisiPanel panelBisnis;
    private ReportDivisiPanel panelOps;

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel("Laporan Ranking MOORA");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Hasil perankingan optimasi multi-objektif (Yi) karyawan terbaik Bank BRI KCP Arundina");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelBisnis = new ReportDivisiPanel("Bisnis");
        panelOps = new ReportDivisiPanel("Operasional");

        tabbedPane.addTab("Hasil Divisi Bisnis", panelBisnis);
        tabbedPane.addTab("Hasil Divisi Operasional", panelOps);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        panelBisnis.loadData();
        panelOps.loadData();
    }

    private static class ReportDivisiPanel extends JPanel {
        private String divisi;
        private JTable tblRanking;
        private DefaultTableModel tableModel;
        private JTable tblKeputusan;
        private DefaultTableModel modelKeputusan;
        private JTable tblNormalisasi;
        private DefaultTableModel modelNormalisasi;
        private JTable tblNormalisasiTerbobot;
        private DefaultTableModel modelNormalisasiTerbobot;
        private List<RankingResult> rankingResults = new ArrayList<>();
        private JPanel conclusionPanel;
        private JLabel lblConclusion;
        private Timer animationTimer;

        public ReportDivisiPanel(String divisi) {
            this.divisi = divisi;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            // Toolbar
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            toolbar.setBackground(null);
            toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));

            JButton btnRefresh = new JButton("Hitung Ulang");
            btnRefresh.addActionListener(e -> loadData());

            JButton btnPDF = new JButton("Cetak PDF");
            btnPDF.setBackground(new Color(220, 53, 69));
            btnPDF.setForeground(Color.WHITE);
            btnPDF.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPDF.addActionListener(e -> exportPDF());

            JButton btnExcel = new JButton("Cetak Excel");
            btnExcel.setBackground(new Color(40, 167, 69));
            btnExcel.setForeground(Color.WHITE);
            btnExcel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnExcel.addActionListener(e -> exportExcel());

            toolbar.add(btnRefresh);
            toolbar.add(btnPDF);
            toolbar.add(btnExcel);
            add(toolbar, BorderLayout.NORTH);

            // JTabbedPane internal untuk menampilkan langkah perhitungan
            JTabbedPane calculationTabs = new JTabbedPane();
            calculationTabs.setFont(new Font("Segoe UI", Font.BOLD, 11));

            // Tab 1: Matriks Keputusan
            modelKeputusan = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            tblKeputusan = new JTable(modelKeputusan);
            tblKeputusan.setRowHeight(25);
            tblKeputusan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            calculationTabs.addTab("1. Matriks Keputusan", new JScrollPane(tblKeputusan));

            // Tab 2: Matriks Normalisasi
            modelNormalisasi = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            tblNormalisasi = new JTable(modelNormalisasi);
            tblNormalisasi.setRowHeight(25);
            tblNormalisasi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            calculationTabs.addTab("2. Matriks Normalisasi", new JScrollPane(tblNormalisasi));

            // Tab 3: Matriks Normalisasi Terbobot
            modelNormalisasiTerbobot = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };
            tblNormalisasiTerbobot = new JTable(modelNormalisasiTerbobot);
            tblNormalisasiTerbobot.setRowHeight(25);
            tblNormalisasiTerbobot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            calculationTabs.addTab("3. Normalisasi Terbobot", new JScrollPane(tblNormalisasiTerbobot));

            // Tab 4: Hasil Akhir & Ranking
            tableModel = new DefaultTableModel(new Object[]{"Rank", "Kode Karyawan", "Nama Karyawan", "Divisi", "Score (Yi)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblRanking = new JTable(tableModel);
            tblRanking.setRowHeight(28);
            tblRanking.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblRanking.getColumnModel().getColumn(0).setMaxWidth(60);
            tblRanking.getColumnModel().getColumn(1).setMaxWidth(120);
            tblRanking.getColumnModel().getColumn(3).setMaxWidth(120);
            calculationTabs.addTab("4. Hasil Akhir & Ranking", new JScrollPane(tblRanking));

            add(calculationTabs, BorderLayout.CENTER);

            // Conclusion Panel
            conclusionPanel = new JPanel(new BorderLayout());
            conclusionPanel.setBackground(new Color(245, 247, 250));
            conclusionPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                    new EmptyBorder(15, 20, 15, 20)
            ));
            
            lblConclusion = new JLabel("<html>Belum ada data untuk kalkulasi peringkat.</html>");
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
            modelKeputusan.setRowCount(0);
            modelNormalisasi.setRowCount(0);
            modelNormalisasiTerbobot.setRowCount(0);
            conclusionPanel.setBackground(new Color(245, 247, 250));
            conclusionPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                    new EmptyBorder(15, 20, 15, 20)
            ));

            final int[] step = {0};
            lblConclusion.setText("<html>" +
                    "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                    "  <span style='font-size: 11px; font-weight: bold; color: #0066cc; text-transform: uppercase;'>Engine MOORA Aktif</span><br>" +
                    "  <span style='font-size: 14px; font-weight: bold; color: #111;'>[1/4] Mengambil data penilaian dari database...</span>" +
                    "</div></html>");

            animationTimer = new Timer(400, null);
            animationTimer.addActionListener(e -> {
                step[0]++;
                if (step[0] == 1) {
                    lblConclusion.setText("<html>" +
                            "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                            "  <span style='font-size: 11px; font-weight: bold; color: #0066cc; text-transform: uppercase;'>Engine MOORA Aktif</span><br>" +
                            "  <span style='font-size: 14px; font-weight: bold; color: #111;'>[2/4] Membuat matriks keputusan & melakukan normalisasi...</span>" +
                            "</div></html>");
                } else if (step[0] == 2) {
                    lblConclusion.setText("<html>" +
                            "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                            "  <span style='font-size: 11px; font-weight: bold; color: #0066cc; text-transform: uppercase;'>Engine MOORA Aktif</span><br>" +
                            "  <span style='font-size: 14px; font-weight: bold; color: #111;'>[3/4] Mengalikan bobot kriteria dengan matriks normalisasi...</span>" +
                            "</div></html>");
                } else if (step[0] == 3) {
                    lblConclusion.setText("<html>" +
                            "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                            "  <span style='font-size: 11px; font-weight: bold; color: #0066cc; text-transform: uppercase;'>Engine MOORA Aktif</span><br>" +
                            "  <span style='font-size: 14px; font-weight: bold; color: #111;'>[4/4] Memfinalisasi perankingan optimasi Yi...</span>" +
                            "</div></html>");
                } else if (step[0] >= 4) {
                    animationTimer.stop();

                    com.spkbri.model.MooraCalculationResult calcResult = MooraEngine.calculate(divisi);
                    rankingResults = calcResult.getRankingResults();
                    java.util.List<com.spkbri.model.Karyawan> karyawanList = calcResult.getKaryawanList();
                    java.util.List<com.spkbri.model.Kriteria> kriteriaList = calcResult.getKriteriaList();
                    java.util.Map<Integer, java.util.Map<Integer, Double>> matriksKeputusan = calcResult.getMatriksKeputusan();
                    java.util.Map<Integer, java.util.Map<Integer, Double>> matriksNormalisasi = calcResult.getMatriksNormalisasi();
                    java.util.Map<Integer, java.util.Map<Integer, Double>> matriksNormalisasiTerbobot = calcResult.getMatriksNormalisasiTerbobot();

                    DecimalFormat df = new DecimalFormat("0.0000");

                    // 1. Populate Matriks Keputusan
                    java.util.Vector<String> colKeputusan = new java.util.Vector<>();
                    colKeputusan.add("Nama Karyawan");
                    for (com.spkbri.model.Kriteria kr : kriteriaList) {
                        colKeputusan.add(kr.getKodeKriteria() + " (" + (kr.getSifat().equalsIgnoreCase("Benefit") ? "B" : "C") + ")");
                    }
                    java.util.Vector<java.util.Vector<Object>> dataKeputusan = new java.util.Vector<>();
                    for (com.spkbri.model.Karyawan k : karyawanList) {
                        java.util.Vector<Object> row = new java.util.Vector<>();
                        row.add(k.getNama());
                        java.util.Map<Integer, Double> nilaiMap = matriksKeputusan.get(k.getIdKaryawan());
                        for (com.spkbri.model.Kriteria kr : kriteriaList) {
                            row.add(nilaiMap != null ? nilaiMap.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0);
                        }
                        dataKeputusan.add(row);
                    }
                    modelKeputusan.setDataVector(dataKeputusan, colKeputusan);

                    // 2. Populate Matriks Normalisasi
                    java.util.Vector<String> colNorm = new java.util.Vector<>(colKeputusan);
                    java.util.Vector<java.util.Vector<Object>> dataNorm = new java.util.Vector<>();
                    for (com.spkbri.model.Karyawan k : karyawanList) {
                        java.util.Vector<Object> row = new java.util.Vector<>();
                        row.add(k.getNama());
                        java.util.Map<Integer, Double> nilaiMap = matriksNormalisasi.get(k.getIdKaryawan());
                        for (com.spkbri.model.Kriteria kr : kriteriaList) {
                            double val = nilaiMap != null ? nilaiMap.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0;
                            row.add(df.format(val));
                        }
                        dataNorm.add(row);
                    }
                    modelNormalisasi.setDataVector(dataNorm, colNorm);

                    // 3. Populate Matriks Normalisasi Terbobot
                    java.util.Vector<String> colWeighted = new java.util.Vector<>(colKeputusan);
                    java.util.Vector<java.util.Vector<Object>> dataWeighted = new java.util.Vector<>();
                    for (com.spkbri.model.Karyawan k : karyawanList) {
                        java.util.Vector<Object> row = new java.util.Vector<>();
                        row.add(k.getNama());
                        java.util.Map<Integer, Double> nilaiMap = matriksNormalisasiTerbobot.get(k.getIdKaryawan());
                        for (com.spkbri.model.Kriteria kr : kriteriaList) {
                            double val = nilaiMap != null ? nilaiMap.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0;
                            row.add(df.format(val));
                        }
                        dataWeighted.add(row);
                    }
                    modelNormalisasiTerbobot.setDataVector(dataWeighted, colWeighted);

                    // 4. Populate Hasil Akhir & Ranking
                    for (RankingResult r : rankingResults) {
                        tableModel.addRow(new Object[]{
                                r.getRank(),
                                r.getKaryawan().getKodeKaryawan(),
                                r.getKaryawan().getNama(),
                                r.getKaryawan().getDivisi(),
                                df.format(r.getScore())
                        });
                    }

                    if (!rankingResults.isEmpty()) {
                        RankingResult best = rankingResults.get(0);
                        lblConclusion.setText("<html>" +
                                "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                                "  <span style='font-size: 11px; font-weight: bold; color: #2e7d32; text-transform: uppercase;'>Rekomendasi Karyawan Terbaik</span><br>" +
                                "  <span style='font-size: 18px; font-weight: bold; color: #1e4620;'>" + best.getKaryawan().getNama() + "</span>" +
                                "  <span style='font-size: 12px; color: #555;'> (Kode: " + best.getKaryawan().getKodeKaryawan() + ")</span><br>" +
                                "  <span style='font-size: 13px; color: #333;'>Berdasarkan hasil kalkulasi metode MOORA, karyawan ini menduduki peringkat pertama dengan skor optimasi (Yi) tertinggi sebesar <b>" + df.format(best.getScore()) + "</b>.</span>" +
                                "</div></html>");
                        conclusionPanel.setBackground(new Color(230, 245, 235)); // soft green
                        conclusionPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(195, 230, 203)),
                                new EmptyBorder(15, 20, 15, 20)
                        ));
                    } else {
                        lblConclusion.setText("<html>" +
                                "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                                "  <span style='font-size: 11px; font-weight: bold; color: #721c24; text-transform: uppercase;'>Kalkulasi MOORA</span><br>" +
                                "  <span style='font-size: 14px; font-weight: bold; color: #666;'>Belum ada data untuk kalkulasi peringkat</span><br>" +
                                "  <span style='font-size: 12px; color: #666;'>Silakan masukkan data penilaian karyawan terlebih dahulu di menu <b>Input Penilaian</b>.</span>" +
                                "</div></html>");
                        conclusionPanel.setBackground(new Color(245, 247, 250)); // soft gray
                        conclusionPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                                new EmptyBorder(15, 20, 15, 20)
                        ));
                    }
                }
            });
            animationTimer.start();
        }

        private void exportPDF() {
            if (rankingResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk dicetak!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan PDF");
            fileChooser.setSelectedFile(new File("Laporan_Ranking_" + divisi + ".pdf"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ExportHelper.exportToPDF(rankingResults, divisi, fileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Laporan PDF berhasil disimpan!");
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal mengekspor PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void exportExcel() {
            if (rankingResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data untuk dicetak!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan Excel");
            fileChooser.setSelectedFile(new File("Laporan_Ranking_" + divisi + ".csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ExportHelper.exportToCSV(rankingResults, divisi, fileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Laporan Excel (CSV) berhasil disimpan!");
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal mengekspor Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
