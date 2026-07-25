package com.spkbri.ui;

import com.spkbri.core.MooraEngine;
import com.spkbri.database.DatabaseHelper;
import com.spkbri.model.Karyawan;
import com.spkbri.model.Kriteria;
import com.spkbri.model.RankingResult;
import com.spkbri.model.MooraCalculationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PenilaianPanel extends JPanel {

    private PenilaianDivisiPanel panelBisnis;
    private PenilaianDivisiPanel panelOps;

    public PenilaianPanel(DashboardPanel dashboardPanel) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(null);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(null);
        JLabel title = new JLabel("PROSES PERHITUNGAN DAN INPUT PENILAIAN MOORA KARYAWAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0, 82, 162));
        JLabel subtitle = new JLabel("Form perbandingan penilaian karyawan, kalkulasi matriks keputusan, normalisasi, dan perankingan");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Color.GRAY);
        textPanel.add(title);
        textPanel.add(subtitle);
        headerPanel.add(textPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelBisnis = new PenilaianDivisiPanel("Bisnis", dashboardPanel);
        panelOps = new PenilaianDivisiPanel("Operasional", dashboardPanel);

        tabbedPane.addTab("Divisi Bisnis", panelBisnis);
        tabbedPane.addTab("Divisi Operasional", panelOps);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshTabs() {
        panelBisnis.loadKriteriaAndSetupMatrix();
        panelOps.loadKriteriaAndSetupMatrix();
    }

    private static class PenilaianDivisiPanel extends JPanel {
        private String divisi;
        private DashboardPanel dashboardPanel;

        // Left Panel Components
        private JTextField txtKodePerhitungan;
        private JTextField[] txtKaryawanKode = new JTextField[5];
        private JTextField[] txtKaryawanNama = new JTextField[5];
        private int[] selectedKaryawanIds = new int[5];

        // Right Matrix Components
        private JPanel pnlKeputusanMatrix;
        private JPanel pnlNormalisasiMatrix;
        private JTextField[][] gridKeputusan; // [karyawan 0-4][kriteria 0-N]
        private JTextField[][] gridNormalisasi; // [karyawan 0-4][kriteria 0-N]

        // Bottom Table Components
        private JTable tblHistory;
        private DefaultTableModel tableModel;

        private List<Kriteria> kriteriaList = new ArrayList<>();
        private List<Karyawan> allKaryawanList = new ArrayList<>();

        public PenilaianDivisiPanel(String divisi, DashboardPanel dashboardPanel) {
            this.divisi = divisi;
            this.dashboardPanel = dashboardPanel;

            for (int i = 0; i < 5; i++) {
                selectedKaryawanIds[i] = -1;
            }

            setLayout(new BorderLayout(15, 15));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            // 1. LEFT SIDE PANEL (Form & Controls)
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new GridBagLayout());
            leftPanel.setBackground(Color.WHITE);
            leftPanel.setPreferredSize(new Dimension(300, 600));
            leftPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(10, 10, 10, 10)
            ));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(4, 2, 4, 2);
            gbc.gridx = 0;
            gbc.gridy = 0;

            leftPanel.add(new JLabel("Kode Perhitungan:"), gbc);
            gbc.gridy++;
            txtKodePerhitungan = new JTextField("CALC_" + divisi.toUpperCase().substring(0, 3) + "_" + (System.currentTimeMillis() % 1000));
            txtKodePerhitungan.setPreferredSize(new Dimension(200, 30));
            leftPanel.add(txtKodePerhitungan, gbc);

            gbc.gridy++;
            JButton btnCariGuru = new JButton("CARI DATA KARYAWAN");
            btnCariGuru.setBackground(new Color(0, 82, 162));
            btnCariGuru.setForeground(Color.WHITE);
            btnCariGuru.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCariGuru.setPreferredSize(new Dimension(200, 35));
            btnCariGuru.addActionListener(e -> showKaryawanSelectionDialog());
            leftPanel.add(btnCariGuru, gbc);

            for (int i = 0; i < 5; i++) {
                gbc.gridy++;
                leftPanel.add(new JLabel("Kode Karyawan " + (i + 1) + ":"), gbc);
                gbc.gridy++;
                txtKaryawanKode[i] = new JTextField();
                txtKaryawanKode[i].setEditable(false);
                txtKaryawanKode[i].setPreferredSize(new Dimension(200, 25));
                leftPanel.add(txtKaryawanKode[i], gbc);

                gbc.gridy++;
                leftPanel.add(new JLabel("Nama Karyawan " + (i + 1) + ":"), gbc);
                gbc.gridy++;
                txtKaryawanNama[i] = new JTextField();
                txtKaryawanNama[i].setEditable(false);
                txtKaryawanNama[i].setPreferredSize(new Dimension(200, 25));
                leftPanel.add(txtKaryawanNama[i], gbc);
            }

            // Buttons panel inside left panel
            gbc.gridy++;
            JPanel pnlButtons = new JPanel(new GridLayout(1, 3, 5, 0));
            pnlButtons.setBackground(Color.WHITE);
            JButton btnKembali = new JButton("KEMBALI");
            btnKembali.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btnKembali.addActionListener(e -> resetInputs());
            JButton btnSimpan = new JButton("SIMPAN");
            btnSimpan.setBackground(new Color(40, 167, 69));
            btnSimpan.setForeground(Color.WHITE);
            btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btnSimpan.addActionListener(e -> savePenilaian());
            JButton btnHapus = new JButton("HAPUS");
            btnHapus.setBackground(new Color(220, 53, 69));
            btnHapus.setForeground(Color.WHITE);
            btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btnHapus.addActionListener(e -> clearSelectedKaryawan());
            pnlButtons.add(btnKembali);
            pnlButtons.add(btnSimpan);
            pnlButtons.add(btnHapus);
            leftPanel.add(pnlButtons, gbc);

            gbc.gridy++;
            JButton btnMulaiHitung = new JButton("MULAI HITUNG");
            btnMulaiHitung.setBackground(new Color(0, 128, 128));
            btnMulaiHitung.setForeground(Color.WHITE);
            btnMulaiHitung.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnMulaiHitung.setPreferredSize(new Dimension(200, 38));
            btnMulaiHitung.addActionListener(e -> calculateMOORAOnGrid());
            leftPanel.add(btnMulaiHitung, gbc);

            add(leftPanel, BorderLayout.WEST);

            // 2. RIGHT SIDE PANEL
            JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
            rightPanel.setBackground(Color.WHITE);

            // Matrices Panel (Top Right)
            JPanel matricesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            matricesPanel.setBackground(Color.WHITE);

            pnlKeputusanMatrix = new JPanel();
            pnlKeputusanMatrix.setBackground(Color.WHITE);
            pnlKeputusanMatrix.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Matriks Keputusan (Skala 1-5)",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    Color.DARK_GRAY
            ));

            pnlNormalisasiMatrix = new JPanel();
            pnlNormalisasiMatrix.setBackground(Color.WHITE);
            pnlNormalisasiMatrix.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Matriks Normalisasi",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    Color.DARK_GRAY
            ));

            matricesPanel.add(pnlKeputusanMatrix);
            matricesPanel.add(pnlNormalisasiMatrix);
            rightPanel.add(matricesPanel, BorderLayout.NORTH);

            // Table Panel (Bottom Right)
            tableModel = new DefaultTableModel();
            tblHistory = new JTable(tableModel);
            tblHistory.setRowHeight(25);
            tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            JScrollPane scrollTable = new JScrollPane(tblHistory);
            scrollTable.setPreferredSize(new Dimension(500, 250));
            scrollTable.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Riwayat Perankingan Evaluasi",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    Color.DARK_GRAY
            ));
            rightPanel.add(scrollTable, BorderLayout.CENTER);

            add(rightPanel, BorderLayout.CENTER);

            loadKriteriaAndSetupMatrix();
            loadAllKaryawan();
            loadHistoryTable();
        }

        private void loadAllKaryawan() {
            allKaryawanList.clear();
            String sql = "SELECT * FROM karyawan WHERE divisi = ? ORDER BY nama ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        allKaryawanList.add(new Karyawan(
                                rs.getInt("id_karyawan"),
                                rs.getString("kode_karyawan"),
                                rs.getString("nama"),
                                rs.getString("divisi")
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void loadKriteriaAndSetupMatrix() {
            kriteriaList.clear();
            String sql = "SELECT * FROM kriteria WHERE divisi = ? ORDER BY kode_kriteria ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        kriteriaList.add(new Kriteria(
                                rs.getInt("id_kriteria"),
                                rs.getString("kode_kriteria"),
                                rs.getString("nama_kriteria"),
                                rs.getString("sifat"),
                                rs.getDouble("bobot"),
                                rs.getString("divisi")
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            setupMatrixGrids();
        }

        private void setupMatrixGrids() {
            pnlKeputusanMatrix.removeAll();
            pnlNormalisasiMatrix.removeAll();

            int rows = 5;
            int cols = kriteriaList.size();

            if (cols == 0) {
                pnlKeputusanMatrix.add(new JLabel("Kriteria kosong"));
                pnlNormalisasiMatrix.add(new JLabel("Kriteria kosong"));
                pnlKeputusanMatrix.revalidate();
                pnlKeputusanMatrix.repaint();
                pnlNormalisasiMatrix.revalidate();
                pnlNormalisasiMatrix.repaint();
                return;
            }

            pnlKeputusanMatrix.setLayout(new GridLayout(rows + 1, cols + 1, 5, 5));
            pnlNormalisasiMatrix.setLayout(new GridLayout(rows + 1, cols + 1, 5, 5));

            gridKeputusan = new JTextField[rows][cols];
            gridNormalisasi = new JTextField[rows][cols];

            // Header Labels
            pnlKeputusanMatrix.add(new JLabel("Karyawan", JLabel.CENTER));
            pnlNormalisasiMatrix.add(new JLabel("Karyawan", JLabel.CENTER));
            for (Kriteria kr : kriteriaList) {
                JLabel lblK1 = new JLabel(kr.getKodeKriteria(), JLabel.CENTER);
                lblK1.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pnlKeputusanMatrix.add(lblK1);

                JLabel lblK2 = new JLabel(kr.getKodeKriteria(), JLabel.CENTER);
                lblK2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pnlNormalisasiMatrix.add(lblK2);
            }

            // Create Grid Rows
            for (int r = 0; r < rows; r++) {
                JLabel lblRowHeaderKep = new JLabel("E" + (r + 1), JLabel.CENTER);
                lblRowHeaderKep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                pnlKeputusanMatrix.add(lblRowHeaderKep);

                JLabel lblRowHeaderNorm = new JLabel("E" + (r + 1), JLabel.CENTER);
                lblRowHeaderNorm.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                pnlNormalisasiMatrix.add(lblRowHeaderNorm);

                for (int c = 0; c < cols; c++) {
                    gridKeputusan[r][c] = new JTextField("0");
                    gridKeputusan[r][c].setHorizontalAlignment(JTextField.CENTER);
                    gridKeputusan[r][c].setEnabled(false); // Enabled only when employee is selected
                    pnlKeputusanMatrix.add(gridKeputusan[r][c]);

                    gridNormalisasi[r][c] = new JTextField("");
                    gridNormalisasi[r][c].setHorizontalAlignment(JTextField.CENTER);
                    gridNormalisasi[r][c].setEditable(false);
                    gridNormalisasi[r][c].setBackground(new Color(245, 247, 250));
                    pnlNormalisasiMatrix.add(gridNormalisasi[r][c]);
                }
            }

            pnlKeputusanMatrix.revalidate();
            pnlKeputusanMatrix.repaint();
            pnlNormalisasiMatrix.revalidate();
            pnlNormalisasiMatrix.repaint();
        }

        private void showKaryawanSelectionDialog() {
            loadAllKaryawan();
            if (allKaryawanList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada data karyawan di database!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pilih Karyawan (Maksimal 5)", true);
            dialog.setSize(400, 450);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            JScrollPane scroll = new JScrollPane(content);
            dialog.add(scroll, BorderLayout.CENTER);

            List<JCheckBox> checkBoxes = new ArrayList<>();
            for (Karyawan k : allKaryawanList) {
                JCheckBox cb = new JCheckBox(k.getKodeKaryawan() + " - " + k.getNama());
                cb.putClientProperty("karyawan", k);
                // Pre-select if already chosen
                for (int id : selectedKaryawanIds) {
                    if (id == k.getIdKaryawan()) {
                        cb.setSelected(true);
                        break;
                    }
                }
                content.add(cb);
                checkBoxes.add(cb);
            }

            JButton btnOk = new JButton("PILIH");
            btnOk.setBackground(new Color(0, 82, 162));
            btnOk.setForeground(Color.WHITE);
            btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnOk.addActionListener(e -> {
                List<Karyawan> selected = new ArrayList<>();
                for (JCheckBox cb : checkBoxes) {
                    if (cb.isSelected()) {
                        selected.add((Karyawan) cb.getClientProperty("karyawan"));
                    }
                }

                if (selected.size() > 5) {
                    JOptionPane.showMessageDialog(dialog, "Maksimal pilih 5 karyawan!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Clear previous
                clearSelectedKaryawan();

                // Populate selected
                for (int i = 0; i < selected.size(); i++) {
                    Karyawan k = selected.get(i);
                    selectedKaryawanIds[i] = k.getIdKaryawan();
                    txtKaryawanKode[i].setText(k.getKodeKaryawan());
                    txtKaryawanNama[i].setText(k.getNama());

                    // Enable this row in the decision matrix
                    for (int c = 0; c < kriteriaList.size(); c++) {
                        gridKeputusan[i][c].setEnabled(true);
                    }

                    // Try to load existing scores
                    loadExistingScores(i, k.getIdKaryawan());
                }

                dialog.dispose();
            });

            dialog.add(btnOk, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }

        private void loadExistingScores(int rowIndex, int karyawanId) {
            String sql = "SELECT id_kriteria, nilai FROM penilaian WHERE id_karyawan = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, karyawanId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int critId = rs.getInt("id_kriteria");
                        double val = rs.getDouble("nilai");

                        // Find column index
                        for (int colIndex = 0; colIndex < kriteriaList.size(); colIndex++) {
                            if (kriteriaList.get(colIndex).getIdKriteria() == critId) {
                                gridKeputusan[rowIndex][colIndex].setText(String.valueOf(val));
                                break;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void clearSelectedKaryawan() {
            for (int i = 0; i < 5; i++) {
                selectedKaryawanIds[i] = -1;
                txtKaryawanKode[i].setText("");
                txtKaryawanNama[i].setText("");
            }
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < kriteriaList.size(); c++) {
                    if (gridKeputusan != null && gridKeputusan[r][c] != null) {
                        gridKeputusan[r][c].setText("0");
                        gridKeputusan[r][c].setEnabled(false);
                    }
                    if (gridNormalisasi != null && gridNormalisasi[r][c] != null) {
                        gridNormalisasi[r][c].setText("");
                    }
                }
            }
        }

        private void resetInputs() {
            clearSelectedKaryawan();
            txtKodePerhitungan.setText("CALC_" + divisi.toUpperCase().substring(0, 3) + "_" + (System.currentTimeMillis() % 1000));
        }

        private void calculateMOORAOnGrid() {
            // Count selected
            int count = 0;
            for (int id : selectedKaryawanIds) {
                if (id != -1) count++;
            }

            if (count == 0) {
                JOptionPane.showMessageDialog(this, "Silakan pilih karyawan terlebih dahulu!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Read decision matrix
            int cols = kriteriaList.size();
            double[][] matrix = new double[count][cols];

            for (int r = 0; r < count; r++) {
                for (int c = 0; c < cols; c++) {
                    String valStr = gridKeputusan[r][c].getText().trim();
                    try {
                        double val = Double.parseDouble(valStr);
                        if (val < 1 || val > 5) {
                            JOptionPane.showMessageDialog(this, "Nilai harus di antara rentang skala 1 sampai 5!", "Validasi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        matrix[r][c] = val;
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Semua input nilai harus berupa angka desimal!", "Validasi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // Normalization denominator: sqrt(sum(x_ij^2)) for each column
            DecimalFormat df = new DecimalFormat("0.0000");
            for (int c = 0; c < cols; c++) {
                double sumSq = 0.0;
                for (int r = 0; r < count; r++) {
                    sumSq += matrix[r][c] * matrix[r][c];
                }
                double denominator = Math.sqrt(sumSq);
                if (denominator == 0.0) denominator = 1.0;

                for (int r = 0; r < count; r++) {
                    double normalizedVal = matrix[r][c] / denominator;
                    gridNormalisasi[r][c].setText(df.format(normalizedVal));
                }
            }
            JOptionPane.showMessageDialog(this, "Kalkulasi matriks normalisasi berhasil!");
        }

        private void savePenilaian() {
            int count = 0;
            for (int id : selectedKaryawanIds) {
                if (id != -1) count++;
            }

            if (count == 0) {
                JOptionPane.showMessageDialog(this, "Pilih karyawan dan masukkan nilai terlebih dahulu!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);
                String sql = "INSERT INTO penilaian (id_karyawan, id_kriteria, nilai) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE nilai = VALUES(nilai)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (int r = 0; r < count; r++) {
                        int karyawanId = selectedKaryawanIds[r];
                        for (int c = 0; c < kriteriaList.size(); c++) {
                            int kriteriaId = kriteriaList.get(c).getIdKriteria();
                            String valStr = gridKeputusan[r][c].getText().trim();
                            double val = Double.parseDouble(valStr);

                            if (val < 1 || val > 5) {
                                throw new NumberFormatException("Nilai diluar range 1-5");
                            }

                            pstmt.setInt(1, karyawanId);
                            pstmt.setInt(2, kriteriaId);
                            pstmt.setDouble(3, val);
                            pstmt.addBatch();
                        }
                    }
                    pstmt.executeBatch();
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Penilaian matriks berhasil disimpan ke database!");
                    dashboardPanel.refreshData();
                    loadHistoryTable();
                } catch (Exception e) {
                    conn.rollback();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan penilaian: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void loadHistoryTable() {
            // Build columns dynamically based on criteria
            Vector<String> columns = new Vector<>();
            columns.add("Rank");
            columns.add("Kode Karyawan");
            columns.add("Nama Karyawan");
            for (Kriteria kr : kriteriaList) {
                columns.add(kr.getKodeKriteria());
            }
            columns.add("Skor (Yi)");

            Vector<Vector<Object>> data = new Vector<>();
            MooraCalculationResult calcResult = MooraEngine.calculate(divisi);
            List<RankingResult> ranking = calcResult.getRankingResults();
            Map<Integer, Map<Integer, Double>> matriksKeputusan = calcResult.getMatriksKeputusan();

            DecimalFormat df = new DecimalFormat("0.0000");
            for (RankingResult r : ranking) {
                Vector<Object> row = new Vector<>();
                row.add(r.getRank());
                row.add(r.getKaryawan().getKodeKaryawan());
                row.add(r.getKaryawan().getNama());

                Map<Integer, Double> nilaiMap = matriksKeputusan.get(r.getKaryawan().getIdKaryawan());
                for (Kriteria kr : kriteriaList) {
                    row.add(nilaiMap != null ? nilaiMap.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0);
                }
                row.add(df.format(r.getScore()));
                data.add(row);
            }

            tableModel.setDataVector(data, columns);
        }
    }
}
