package com.spkbri.ui;

import com.spkbri.database.DatabaseHelper;
import com.spkbri.model.Karyawan;
import com.spkbri.model.Kriteria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PimpinanPenilaianPanel extends JPanel {

    private PimpinanPenilaianDivisiPanel panelBisnis;
    private PimpinanPenilaianDivisiPanel panelOps;

    public PimpinanPenilaianPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(null);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(null);
        JLabel title = new JLabel("PROSES PERHITUNGAN BOBOT KRITERIA AHP (PIMPINAN)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0, 82, 162));
        JLabel subtitle = new JLabel("Menentukan bobot kriteria secara objektif melalui matriks perbandingan berpasangan (pairwise comparison)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Color.GRAY);
        textPanel.add(title);
        textPanel.add(subtitle);
        headerPanel.add(textPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelBisnis = new PimpinanPenilaianDivisiPanel("Bisnis");
        panelOps = new PimpinanPenilaianDivisiPanel("Operasional");

        tabbedPane.addTab("Divisi Bisnis", panelBisnis);
        tabbedPane.addTab("Divisi Operasional", panelOps);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void refreshTabs() {
        panelBisnis.loadKriteriaAndSetupMatrix();
        panelOps.loadKriteriaAndSetupMatrix();
    }

    private static class PimpinanPenilaianDivisiPanel extends JPanel {
        private final String divisi;

        // Left Panel Components
        private JTextField txtKodePerhitungan;
        private JTextField[] txtKaryawanKode = new JTextField[5];
        private JTextField[] txtKaryawanNama = new JTextField[5];
        private int[] selectedKaryawanIds = new int[5];

        // Right Matrix Components
        private JPanel pnlKeputusanMatrix;
        private JPanel pnlNormalisasiMatrix;
        private JTextField[][] gridComparison; // [kriteria N][kriteria N]
        private JTextField[][] gridNormalisasi; // [kriteria N][kriteria N + 1 (Prioritas)]

        // Bottom Table Components
        private JTable tblHistory;
        private DefaultTableModel tableModel;

        private List<Kriteria> kriteriaList = new ArrayList<>();
        private List<Karyawan> allKaryawanList = new ArrayList<>();
        private double[] calculatedPriorities;

        public PimpinanPenilaianDivisiPanel(String divisi) {
            this.divisi = divisi;

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
            txtKodePerhitungan = new JTextField("GURU_" + (System.currentTimeMillis() % 100));
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
            btnSimpan.addActionListener(e -> saveAHPWeights());
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
            btnMulaiHitung.addActionListener(e -> calculateAHPPriorities());
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
                    "Matriks Perbandingan Kriteria",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    Color.DARK_GRAY
            ));

            pnlNormalisasiMatrix = new JPanel();
            pnlNormalisasiMatrix.setBackground(Color.WHITE);
            pnlNormalisasiMatrix.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Matriks Normalisasi Kriteria",
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
                    "Riwayat Perhitungan Bobot Kriteria",
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

            int n = kriteriaList.size();
            if (n == 0) {
                pnlKeputusanMatrix.add(new JLabel("Kriteria kosong"));
                pnlNormalisasiMatrix.add(new JLabel("Kriteria kosong"));
                pnlKeputusanMatrix.revalidate();
                pnlKeputusanMatrix.repaint();
                pnlNormalisasiMatrix.revalidate();
                pnlNormalisasiMatrix.repaint();
                return;
            }

            pnlKeputusanMatrix.setLayout(new GridLayout(n + 1, n + 1, 5, 5));
            pnlNormalisasiMatrix.setLayout(new GridLayout(n + 1, n + 2, 5, 5));

            gridComparison = new JTextField[n][n];
            gridNormalisasi = new JTextField[n][n + 1];

            pnlKeputusanMatrix.add(new JLabel("", JLabel.CENTER));
            pnlNormalisasiMatrix.add(new JLabel("", JLabel.CENTER));
            for (Kriteria kr : kriteriaList) {
                JLabel lblK1 = new JLabel(kr.getKodeKriteria(), JLabel.CENTER);
                lblK1.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pnlKeputusanMatrix.add(lblK1);

                JLabel lblK2 = new JLabel(kr.getKodeKriteria(), JLabel.CENTER);
                lblK2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pnlNormalisasiMatrix.add(lblK2);
            }
            JLabel lblPrioritasHeader = new JLabel("Prioritas", JLabel.CENTER);
            lblPrioritasHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
            pnlNormalisasiMatrix.add(lblPrioritasHeader);

            DecimalFormat df = new DecimalFormat("0.00");

            for (int r = 0; r < n; r++) {
                JLabel lblRowHeaderKep = new JLabel(kriteriaList.get(r).getKodeKriteria(), JLabel.CENTER);
                lblRowHeaderKep.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblRowHeaderKep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                pnlKeputusanMatrix.add(lblRowHeaderKep);

                JLabel lblRowHeaderNorm = new JLabel(kriteriaList.get(r).getKodeKriteria(), JLabel.CENTER);
                lblRowHeaderNorm.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblRowHeaderNorm.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                pnlNormalisasiMatrix.add(lblRowHeaderNorm);

                for (int c = 0; c < n; c++) {
                    gridComparison[r][c] = new JTextField();
                    gridComparison[r][c].setHorizontalAlignment(JTextField.CENTER);
                    
                    if (r == c) {
                        gridComparison[r][c].setText("1");
                        gridComparison[r][c].setEditable(false);
                        gridComparison[r][c].setBackground(new Color(245, 247, 250));
                    } else {
                        gridComparison[r][c].setText("1");
                        final int row = r;
                        final int col = c;
                        gridComparison[r][c].addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                try {
                                    String valStr = gridComparison[row][col].getText().trim();
                                    double val = Double.parseDouble(valStr);
                                    if (val <= 0) throw new NumberFormatException();
                                    
                                    double recip = 1.0 / val;
                                    gridComparison[col][row].setText(df.format(recip).replace(",", "."));
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(PimpinanPenilaianDivisiPanel.this, "Masukkan nilai numerik positif!");
                                    gridComparison[row][col].setText("1");
                                    gridComparison[col][row].setText("1");
                                }
                            }
                        });
                    }
                    pnlKeputusanMatrix.add(gridComparison[r][c]);

                    gridNormalisasi[r][c] = new JTextField("");
                    gridNormalisasi[r][c].setHorizontalAlignment(JTextField.CENTER);
                    gridNormalisasi[r][c].setEditable(false);
                    gridNormalisasi[r][c].setBackground(new Color(245, 247, 250));
                    pnlNormalisasiMatrix.add(gridNormalisasi[r][c]);
                }

                gridNormalisasi[r][n] = new JTextField("");
                gridNormalisasi[r][n].setHorizontalAlignment(JTextField.CENTER);
                gridNormalisasi[r][n].setEditable(false);
                gridNormalisasi[r][n].setBackground(new Color(230, 245, 230));
                gridNormalisasi[r][n].setFont(new Font("Segoe UI", Font.BOLD, 12));
                pnlNormalisasiMatrix.add(gridNormalisasi[r][n]);
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

                clearSelectedKaryawan();

                for (int i = 0; i < selected.size(); i++) {
                    Karyawan k = selected.get(i);
                    selectedKaryawanIds[i] = k.getIdKaryawan();
                    txtKaryawanKode[i].setText(k.getKodeKaryawan());
                    txtKaryawanNama[i].setText(k.getNama());
                }

                dialog.dispose();
            });

            dialog.add(btnOk, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }

        private void clearSelectedKaryawan() {
            for (int i = 0; i < 5; i++) {
                selectedKaryawanIds[i] = -1;
                txtKaryawanKode[i].setText("");
                txtKaryawanNama[i].setText("");
            }
        }

        private void resetInputs() {
            clearSelectedKaryawan();
            txtKodePerhitungan.setText("GURU_" + (System.currentTimeMillis() % 100));
            int n = kriteriaList.size();
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    gridComparison[r][c].setText("1");
                    gridNormalisasi[r][c].setText("");
                }
                gridNormalisasi[r][n].setText("");
            }
        }

        private void calculateAHPPriorities() {
            int n = kriteriaList.size();
            double[][] matrix = new double[n][n];

            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    try {
                        matrix[r][c] = Double.parseDouble(gridComparison[r][c].getText().trim());
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Format angka salah pada baris " + (r+1) + " kolom " + (c+1));
                        return;
                    }
                }
            }

            double[] colSums = new double[n];
            for (int c = 0; c < n; c++) {
                double sum = 0.0;
                for (int r = 0; r < n; r++) {
                    sum += matrix[r][c];
                }
                colSums[c] = sum;
            }

            calculatedPriorities = new double[n];
            DecimalFormat df = new DecimalFormat("0.0000");

            for (int r = 0; r < n; r++) {
                double rowSumNorm = 0.0;
                for (int c = 0; c < n; c++) {
                    double normVal = matrix[r][c] / colSums[c];
                    gridNormalisasi[r][c].setText(df.format(normVal).replace(",", "."));
                    rowSumNorm += normVal;
                }
                calculatedPriorities[r] = rowSumNorm / n;
                gridNormalisasi[r][n].setText(df.format(calculatedPriorities[r]).replace(",", "."));
            }

            JOptionPane.showMessageDialog(this, "Perhitungan AHP prioritas kriteria berhasil!");
        }

        private void saveAHPWeights() {
            if (calculatedPriorities == null) {
                JOptionPane.showMessageDialog(this, "Silakan klik MULAI HITUNG terlebih dahulu!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);
                String sql = "UPDATE kriteria SET bobot = ? WHERE id_kriteria = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < kriteriaList.size(); i++) {
                        pstmt.setDouble(1, calculatedPriorities[i]);
                        pstmt.setInt(2, kriteriaList.get(i).getIdKriteria());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Bobot kriteria baru berhasil disimpan ke database!");
                    loadHistoryTable();
                } catch (Exception e) {
                    conn.rollback();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan bobot: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void loadHistoryTable() {
            Vector<String> columns = new Vector<>();
            columns.add("Kode Perhitungan");
            for (Kriteria kr : kriteriaList) {
                columns.add(kr.getKodeKriteria() + " (Weight)");
            }

            Vector<Vector<Object>> data = new Vector<>();
            
            Vector<Object> row = new Vector<>();
            row.add(txtKodePerhitungan.getText());
            DecimalFormat df = new DecimalFormat("0.0000");
            
            String sql = "SELECT bobot FROM kriteria WHERE divisi = ? ORDER BY kode_kriteria ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        row.add(df.format(rs.getDouble("bobot")).replace(",", "."));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            data.add(row);

            tableModel.setDataVector(data, columns);
        }
    }
}
