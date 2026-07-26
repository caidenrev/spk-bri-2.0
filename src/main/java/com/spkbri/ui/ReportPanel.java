package com.spkbri.ui;

import com.spkbri.core.MooraEngine;
import com.spkbri.model.Karyawan;
import com.spkbri.model.Kriteria;
import com.spkbri.model.MooraCalculationResult;
import com.spkbri.model.RankingResult;
import com.spkbri.util.ExportHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ReportPanel extends JPanel {

    private String reportType;
    private String divisi;

    private JTable mainTable;
    private DefaultTableModel mainTableModel;

    private DefaultTableModel modelKeputusan;
    private DefaultTableModel modelNormalisasi;
    private DefaultTableModel modelNormalisasiTerbobot;

    private JPanel conclusionPanel;
    private JLabel lblConclusion;

    private MooraCalculationResult calcResult;

    public ReportPanel(String reportType, String divisi) {
        this.reportType = reportType;
        this.divisi = divisi;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel(getTitleByReportType());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel(getSubtitleByReportType());
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(null);
        topPanel.add(headerPanel, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setBackground(null);
        toolbar.setBorder(new EmptyBorder(10, 0, 10, 0));

        JButton btnRefresh = new JButton("Refresh / Hitung Ulang");
        btnRefresh.addActionListener(e -> loadData());

        JButton btnPDF = new JButton("Cetak PDF");
        btnPDF.setBackground(new Color(220, 53, 69));
        btnPDF.setForeground(Color.WHITE);
        btnPDF.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPDF.addActionListener(e -> exportPDF());

        toolbar.add(btnRefresh);
        toolbar.add(btnPDF);
        topPanel.add(toolbar, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        if (reportType.equals("DATA_KARYAWAN")) {
            mainTableModel = new DefaultTableModel(new Object[]{"No", "Kode Karyawan", "Nama Karyawan", "Divisi"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            mainTable = new JTable(mainTableModel);
            setupTable(mainTable);
            contentPanel.add(new JScrollPane(mainTable), BorderLayout.CENTER);

        } else if (reportType.equals("PERHITUNGAN")) {
            JTabbedPane calcTabs = new JTabbedPane();
            calcTabs.setFont(new Font("Segoe UI", Font.BOLD, 11));

            modelKeputusan = new DefaultTableModel() { @Override public boolean isCellEditable(int r, int c) { return false; } };
            JTable tblKep = new JTable(modelKeputusan);
            setupTable(tblKep);
            calcTabs.addTab("1. Matriks Keputusan", new JScrollPane(tblKep));

            modelNormalisasi = new DefaultTableModel() { @Override public boolean isCellEditable(int r, int c) { return false; } };
            JTable tblNorm = new JTable(modelNormalisasi);
            setupTable(tblNorm);
            calcTabs.addTab("2. Matriks Normalisasi", new JScrollPane(tblNorm));

            modelNormalisasiTerbobot = new DefaultTableModel() { @Override public boolean isCellEditable(int r, int c) { return false; } };
            JTable tblNormTerbobot = new JTable(modelNormalisasiTerbobot);
            setupTable(tblNormTerbobot);
            calcTabs.addTab("3. Normalisasi Terbobot", new JScrollPane(tblNormTerbobot));

            contentPanel.add(calcTabs, BorderLayout.CENTER);

        } else if (reportType.equals("RANKING")) {
            mainTableModel = new DefaultTableModel(new Object[]{"Rank", "Kode Karyawan", "Nama Karyawan", "Divisi", "Score (Yi)"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            mainTable = new JTable(mainTableModel);
            setupTable(mainTable);
            mainTable.getColumnModel().getColumn(0).setMaxWidth(60);
            contentPanel.add(new JScrollPane(mainTable), BorderLayout.CENTER);

            conclusionPanel = new JPanel(new BorderLayout());
            conclusionPanel.setBackground(new Color(245, 247, 250));
            conclusionPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                    new EmptyBorder(15, 20, 15, 20)
            ));

            lblConclusion = new JLabel("<html>Belum ada data.</html>");
            lblConclusion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            conclusionPanel.add(lblConclusion, BorderLayout.CENTER);
            contentPanel.add(conclusionPanel, BorderLayout.SOUTH);
        }

        add(contentPanel, BorderLayout.CENTER);
        loadData();
    }

    private void setupTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private String getTitleByReportType() {
        if (reportType.equals("DATA_KARYAWAN")) return "Laporan Data Karyawan - Divisi " + divisi;
        if (reportType.equals("PERHITUNGAN")) return "Laporan Perhitungan MOORA - Divisi " + divisi;
        return "Laporan Hasil Ranking - Divisi " + divisi;
    }

    private String getSubtitleByReportType() {
        if (reportType.equals("DATA_KARYAWAN")) return "Menampilkan daftar seluruh karyawan yang terdaftar di Divisi " + divisi;
        if (reportType.equals("PERHITUNGAN")) return "Menampilkan proses perhitungan matriks keputusan hingga normalisasi terbobot";
        return "Menampilkan hasil perankingan akhir dengan skor optimasi (Yi)";
    }

    public void loadData() {
        calcResult = MooraEngine.calculate(divisi);
        DecimalFormat df = new DecimalFormat("0.0000");

        if (reportType.equals("DATA_KARYAWAN")) {
            mainTableModel.setRowCount(0);
            int no = 1;
            for (Karyawan k : calcResult.getKaryawanList()) {
                mainTableModel.addRow(new Object[]{no++, k.getKodeKaryawan(), k.getNama(), k.getDivisi()});
            }
        } else if (reportType.equals("PERHITUNGAN")) {
            modelKeputusan.setRowCount(0);
            modelNormalisasi.setRowCount(0);
            modelNormalisasiTerbobot.setRowCount(0);

            List<Karyawan> kList = calcResult.getKaryawanList();
            List<Kriteria> kritList = calcResult.getKriteriaList();

            Vector<String> colNames = new Vector<>();
            colNames.add("Karyawan");
            for (Kriteria kr : kritList) {
                colNames.add(kr.getKodeKriteria() + " (" + (kr.getSifat().equalsIgnoreCase("Benefit") ? "B" : "C") + ")");
            }

            Vector<Vector<Object>> dataKep = new Vector<>();
            for (Karyawan k : kList) {
                Vector<Object> row = new Vector<>();
                row.add(k.getNama());
                Map<Integer, Double> vals = calcResult.getMatriksKeputusan().get(k.getIdKaryawan());
                for (Kriteria kr : kritList) {
                    row.add(vals != null ? vals.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0);
                }
                dataKep.add(row);
            }
            modelKeputusan.setDataVector(dataKep, colNames);

            Vector<Vector<Object>> dataNorm = new Vector<>();
            for (Karyawan k : kList) {
                Vector<Object> row = new Vector<>();
                row.add(k.getNama());
                Map<Integer, Double> vals = calcResult.getMatriksNormalisasi().get(k.getIdKaryawan());
                for (Kriteria kr : kritList) {
                    double val = vals != null ? vals.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0;
                    row.add(df.format(val));
                }
                dataNorm.add(row);
            }
            modelNormalisasi.setDataVector(dataNorm, colNames);

            Vector<Vector<Object>> dataWeighted = new Vector<>();
            for (Karyawan k : kList) {
                Vector<Object> row = new Vector<>();
                row.add(k.getNama());
                Map<Integer, Double> vals = calcResult.getMatriksNormalisasiTerbobot().get(k.getIdKaryawan());
                for (Kriteria kr : kritList) {
                    double val = vals != null ? vals.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0;
                    row.add(df.format(val));
                }
                dataWeighted.add(row);
            }
            modelNormalisasiTerbobot.setDataVector(dataWeighted, colNames);

        } else if (reportType.equals("RANKING")) {
            mainTableModel.setRowCount(0);
            List<RankingResult> rankingResults = calcResult.getRankingResults();
            for (RankingResult r : rankingResults) {
                mainTableModel.addRow(new Object[]{
                        r.getRank(),
                        r.getKaryawan().getKodeKaryawan(),
                        r.getKaryawan().getNama(),
                        r.getKaryawan().getDivisi(),
                        df.format(r.getScore())
                });
            }

            if (!rankingResults.isEmpty()) {
                boolean hasValidScore = false;
                for (RankingResult r : rankingResults) {
                    if (r.getScore() != 0.0) {
                        hasValidScore = true;
                        break;
                    }
                }

                if (hasValidScore) {
                    RankingResult best = rankingResults.get(0);
                    lblConclusion.setText("<html>" +
                            "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                            "  <span style='font-size: 11px; font-weight: bold; color: #2e7d32; text-transform: uppercase;'>Rekomendasi Karyawan Terbaik</span><br>" +
                            "  <span style='font-size: 18px; font-weight: bold; color: #1e4620;'>" + best.getKaryawan().getNama() + "</span>" +
                            "  <span style='font-size: 12px; color: #555;'> (Kode: " + best.getKaryawan().getKodeKaryawan() + ")</span><br>" +
                            "  <span style='font-size: 13px; color: #333;'>Berdasarkan hasil kalkulasi metode MOORA, karyawan ini menduduki peringkat pertama dengan skor optimasi (Yi) tertinggi sebesar <b>" + df.format(best.getScore()) + "</b>.</span>" +
                            "</div></html>");
                    conclusionPanel.setBackground(new Color(230, 245, 235));
                } else {
                    lblConclusion.setText("<html>" +
                            "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                            "  <span style='font-size: 11px; font-weight: bold; color: #856404; text-transform: uppercase;'>Belum Ada Penilaian</span><br>" +
                            "  <span style='font-size: 13px; color: #333;'>Nilai kalkulasi masih 0.0000. Silakan isi data penilaian kinerja pada menu Penilaian terlebih dahulu.</span><br>" +
                            "</div></html>");
                    conclusionPanel.setBackground(new Color(255, 243, 205));
                }
            } else {
                lblConclusion.setText("<html>" +
                        "<div style='font-family: \"Segoe UI\", sans-serif;'>" +
                        "  <span style='font-size: 11px; font-weight: bold; color: #721c24; text-transform: uppercase;'>Data Kosong</span><br>" +
                        "  <span style='font-size: 13px; color: #333;'>Belum ada data karyawan untuk kalkulasi peringkat.</span><br>" +
                        "</div></html>");
                conclusionPanel.setBackground(new Color(245, 247, 250));
            }
        }
    }

    private void exportPDF() {
        if (calcResult == null) {
            JOptionPane.showMessageDialog(this, "Data belum dimuat!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan PDF");

        String filename = "Laporan_";
        if (reportType.equals("DATA_KARYAWAN")) filename += "DataKaryawan_";
        if (reportType.equals("PERHITUNGAN")) filename += "Perhitungan_";
        if (reportType.equals("RANKING")) filename += "Ranking_";
        filename += divisi + ".pdf";

        fileChooser.setSelectedFile(new File(filename));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                if (reportType.equals("DATA_KARYAWAN")) {
                    ExportHelper.exportDataKaryawanPDF(calcResult.getKaryawanList(), divisi, fileChooser.getSelectedFile());
                } else if (reportType.equals("PERHITUNGAN")) {
                    ExportHelper.exportPerhitunganPDF(calcResult, divisi, fileChooser.getSelectedFile());
                } else if (reportType.equals("RANKING")) {
                    ExportHelper.exportRankingPDF(calcResult.getRankingResults(), divisi, fileChooser.getSelectedFile());
                }
                JOptionPane.showMessageDialog(this, "Laporan PDF berhasil disimpan!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mengekspor PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
