package com.spkbri.ui;

import com.spkbri.core.MooraEngine;
import com.spkbri.database.DatabaseHelper;
import com.spkbri.model.Karyawan;
import com.spkbri.model.Kriteria;

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
        JLabel subtitle = new JLabel("Menginput nilai aktual (1-100) karyawan berdasarkan kriteria untuk diproses menggunakan metode MOORA");
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
        private Karyawan[] selectedKaryawan = new Karyawan[5];

        // Right Matrix Components
        private JPanel pnlKeputusanMatrix;
        private JPanel pnlNormalisasiMatrix;
        private JTextField[][] gridKeputusan; // [karyawan 5][kriteria N]
        private JTextField[][] gridNormalisasi; // [karyawan 5][kriteria N]

        // Bottom Table Components
        private JTable tblHistory;
        private DefaultTableModel tableModel;

        private List<Kriteria> kriteriaList = new ArrayList<>();
        private List<Karyawan> allKaryawanList = new ArrayList<>();
        private double[][] calculatedNormalization; // Holds normalization results

        public PenilaianDivisiPanel(String divisi, DashboardPanel dashboardPanel) {
            this.divisi = divisi;
            this.dashboardPanel = dashboardPanel;

            for (int i = 0; i < 5; i++) {
                selectedKaryawanIds[i] = -1;
                selectedKaryawan[i] = null;
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
            txtKodePerhitungan = new JTextField("CALC_" + (System.currentTimeMillis() % 1000));
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
            btnMulaiHitung.addActionListener(e -> calculateMOORANormalization());
            leftPanel.add(btnMulaiHitung, gbc);

            add(leftPanel, BorderLayout.WEST);

            // 2. RIGHT SIDE PANEL
            JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
            rightPanel.setBackground(Color.WHITE);

            // Matrices Panel (Top Right)
            JPanel matricesPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // Top: Keputusan, Bottom: Normalisasi
            matricesPanel.setBackground(Color.WHITE);

            pnlKeputusanMatrix = new JPanel();
            pnlKeputusanMatrix.setBackground(Color.WHITE);
            pnlKeputusanMatrix.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Matriks Keputusan (Input Nilai 1-100)",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    Color.DARK_GRAY
            ));

            pnlNormalisasiMatrix = new JPanel();
            pnlNormalisasiMatrix.setBackground(Color.WHITE);
            pnlNormalisasiMatrix.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Matriks Normalisasi MOORA",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12),
                    Color.DARK_GRAY
            ));

            matricesPanel.add(pnlKeputusanMatrix);
            matricesPanel.add(pnlNormalisasiMatrix);
            rightPanel.add(matricesPanel, BorderLayout.NORTH);

            // Table Panel (Bottom Right)
            tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make table read-only
                }
            };
            tblHistory = new JTable(tableModel);
            tblHistory.setRowHeight(25);
            tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tblHistory.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && tblHistory.getSelectedRow() != -1) {
                    int row = tblHistory.getSelectedRow();
                    int idKaryawan = (int) tableModel.getValueAt(row, 0);
                    
                    Karyawan selectedK = null;
                    for (Karyawan k : allKaryawanList) {
                        if (k.getIdKaryawan() == idKaryawan) {
                            selectedK = k;
                            break;
                        }
                    }
                    
                    if (selectedK != null) {
                        clearSelectedKaryawan(); // clears and sets all selectedKaryawanIds to -1
                        selectedKaryawanIds[0] = selectedK.getIdKaryawan();
                        selectedKaryawan[0] = selectedK;
                        txtKaryawanKode[0].setText(selectedK.getKodeKaryawan());
                        txtKaryawanNama[0].setText(selectedK.getNama());
                        updateGridState();
                        loadExistingPenilaian();
                    }
                }
            });
            JScrollPane scrollTable = new JScrollPane(tblHistory);
            scrollTable.setPreferredSize(new Dimension(500, 200));
            scrollTable.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Riwayat Evaluasi Karyawan",
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

            int nCols = kriteriaList.size();
            if (nCols == 0) {
                pnlKeputusanMatrix.add(new JLabel("Kriteria kosong"));
                pnlNormalisasiMatrix.add(new JLabel("Kriteria kosong"));
                pnlKeputusanMatrix.revalidate();
                pnlKeputusanMatrix.repaint();
                pnlNormalisasiMatrix.revalidate();
                pnlNormalisasiMatrix.repaint();
                return;
            }

            // Rows: 1 header row + 5 employee rows = 6 rows
            pnlKeputusanMatrix.setLayout(new GridLayout(6, nCols + 1, 5, 5));
            pnlNormalisasiMatrix.setLayout(new GridLayout(6, nCols + 1, 5, 5));

            gridKeputusan = new JTextField[5][nCols];
            gridNormalisasi = new JTextField[5][nCols];

            // 1. Header Labels
            pnlKeputusanMatrix.add(new JLabel("Alternatif / Kriteria", JLabel.CENTER));
            pnlNormalisasiMatrix.add(new JLabel("Alternatif / Kriteria", JLabel.CENTER));
            for (Kriteria kr : kriteriaList) {
                JLabel lblK1 = new JLabel(kr.getKodeKriteria(), JLabel.CENTER);
                lblK1.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pnlKeputusanMatrix.add(lblK1);

                JLabel lblK2 = new JLabel(kr.getKodeKriteria(), JLabel.CENTER);
                lblK2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pnlNormalisasiMatrix.add(lblK2);
            }

            // 2. Create Grid Rows (5 max)
            for (int r = 0; r < 5; r++) {
                JLabel lblRowHeaderKep = new JLabel("E" + (r + 1), JLabel.CENTER); // Will be updated on select
                lblRowHeaderKep.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblRowHeaderKep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                pnlKeputusanMatrix.add(lblRowHeaderKep);

                JLabel lblRowHeaderNorm = new JLabel("E" + (r + 1), JLabel.CENTER);
                lblRowHeaderNorm.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblRowHeaderNorm.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                pnlNormalisasiMatrix.add(lblRowHeaderNorm);

                for (int c = 0; c < nCols; c++) {
                    gridKeputusan[r][c] = new JTextField("");
                    gridKeputusan[r][c].setHorizontalAlignment(JTextField.CENTER);
                    gridKeputusan[r][c].setEnabled(false); // Enable only if employee is selected
                    pnlKeputusanMatrix.add(gridKeputusan[r][c]);

                    gridNormalisasi[r][c] = new JTextField("");
                    gridNormalisasi[r][c].setHorizontalAlignment(JTextField.CENTER);
                    gridNormalisasi[r][c].setEditable(false);
                    gridNormalisasi[r][c].setFocusable(false); // Prevents cursor from clicking here
                    gridNormalisasi[r][c].setBackground(new Color(245, 247, 250));
                    pnlNormalisasiMatrix.add(gridNormalisasi[r][c]);
                }
            }

            pnlKeputusanMatrix.revalidate();
            pnlKeputusanMatrix.repaint();
            pnlNormalisasiMatrix.revalidate();
            pnlNormalisasiMatrix.repaint();
            updateGridState(); // Check if fields should be enabled
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
                    selectedKaryawan[i] = k;
                    txtKaryawanKode[i].setText(k.getKodeKaryawan());
                    txtKaryawanNama[i].setText(k.getNama());
                }

                updateGridState();
                loadExistingPenilaian();
                dialog.dispose();
            });

            dialog.add(btnOk, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }

        private void clearSelectedKaryawan() {
            for (int i = 0; i < 5; i++) {
                selectedKaryawanIds[i] = -1;
                selectedKaryawan[i] = null;
                txtKaryawanKode[i].setText("");
                txtKaryawanNama[i].setText("");
                for (int c = 0; c < kriteriaList.size(); c++) {
                    gridKeputusan[i][c].setText("");
                    gridNormalisasi[i][c].setText("");
                }
            }
            updateGridState();
        }

        private void resetInputs() {
            clearSelectedKaryawan();
            txtKodePerhitungan.setText("CALC_" + (System.currentTimeMillis() % 1000));
            calculatedNormalization = null;
        }
        
        private void updateGridState() {
            for (int r = 0; r < 5; r++) {
                boolean hasKaryawan = selectedKaryawanIds[r] != -1;
                
                // Update row headers
                Component[] compsKep = pnlKeputusanMatrix.getComponents();
                Component[] compsNorm = pnlNormalisasiMatrix.getComponents();
                int headerIndex = (kriteriaList.size() + 1) + (r * (kriteriaList.size() + 1));
                if(headerIndex < compsKep.length && compsKep[headerIndex] instanceof JLabel) {
                    String labelTxt = hasKaryawan ? selectedKaryawan[r].getKodeKaryawan() : "E" + (r+1);
                    ((JLabel)compsKep[headerIndex]).setText(labelTxt);
                    ((JLabel)compsNorm[headerIndex]).setText(labelTxt);
                }

                for (int c = 0; c < kriteriaList.size(); c++) {
                    gridKeputusan[r][c].setEnabled(hasKaryawan);
                    if (!hasKaryawan) {
                        gridKeputusan[r][c].setText("");
                        gridNormalisasi[r][c].setText("");
                    }
                }
            }
        }
        
        private void loadExistingPenilaian() {
            // Load existing values for selected employees
            String sql = "SELECT id_kriteria, nilai FROM penilaian WHERE id_karyawan = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
                for (int r = 0; r < 5; r++) {
                    if (selectedKaryawanIds[r] != -1) {
                        pstmt.setInt(1, selectedKaryawanIds[r]);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            while (rs.next()) {
                                int idKriteria = rs.getInt("id_kriteria");
                                double nilai = rs.getDouble("nilai");
                                
                                // Find column index
                                for (int c = 0; c < kriteriaList.size(); c++) {
                                    if (kriteriaList.get(c).getIdKriteria() == idKriteria) {
                                        gridKeputusan[r][c].setText(String.valueOf(nilai));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void calculateMOORANormalization() {
            int nCols = kriteriaList.size();
            double[][] matrix = new double[5][nCols];
            boolean hasData = false;

            // 1. Read values from grid
            for (int r = 0; r < 5; r++) {
                if (selectedKaryawanIds[r] == -1) continue;
                hasData = true;
                
                for (int c = 0; c < nCols; c++) {
                    try {
                        String valStr = gridKeputusan[r][c].getText().trim();
                        if (valStr.isEmpty()) {
                            matrix[r][c] = 0.0;
                            gridKeputusan[r][c].setText("0.0");
                        } else {
                            matrix[r][c] = Double.parseDouble(valStr);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Format angka salah pada baris " + (r+1) + " kolom " + (c+1));
                        return;
                    }
                }
            }
            
            if (!hasData) {
                JOptionPane.showMessageDialog(this, "Pilih setidaknya satu karyawan dan masukkan nilainya!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Calculate sum of squares for each column
            double[] colSumSquares = new double[nCols];
            for (int c = 0; c < nCols; c++) {
                double sum = 0.0;
                for (int r = 0; r < 5; r++) {
                    if (selectedKaryawanIds[r] != -1) {
                        sum += Math.pow(matrix[r][c], 2);
                    }
                }
                colSumSquares[c] = Math.sqrt(sum);
            }

            // 3. Normalize matrix (MOORA)
            calculatedNormalization = new double[5][nCols];
            DecimalFormat df = new DecimalFormat("0.0000");

            for (int r = 0; r < 5; r++) {
                if (selectedKaryawanIds[r] == -1) continue;
                
                for (int c = 0; c < nCols; c++) {
                    double denominator = colSumSquares[c];
                    double normVal = 0.0;
                    if (denominator != 0) {
                        normVal = matrix[r][c] / denominator;
                    }
                    calculatedNormalization[r][c] = normVal;
                    gridNormalisasi[r][c].setText(df.format(normVal).replace(",", "."));
                }
            }

            JOptionPane.showMessageDialog(this, "Perhitungan normalisasi MOORA berhasil!");
        }

        private void savePenilaian() {
            boolean hasData = false;
            for (int id : selectedKaryawanIds) {
                if (id != -1) hasData = true;
            }
            
            if (!hasData) {
                JOptionPane.showMessageDialog(this, "Silakan pilih karyawan dan masukkan nilainya!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);
                String sql = "INSERT INTO penilaian (id_karyawan, id_kriteria, nilai) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nilai = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (int r = 0; r < 5; r++) {
                        if (selectedKaryawanIds[r] == -1) continue;
                        
                        for (int c = 0; c < kriteriaList.size(); c++) {
                            String valStr = gridKeputusan[r][c].getText().trim();
                            double val = 0.0;
                            if (!valStr.isEmpty()) {
                                try {
                                    val = Double.parseDouble(valStr);
                                } catch (NumberFormatException ignored) {}
                            }
                            
                            pstmt.setInt(1, selectedKaryawanIds[r]);
                            pstmt.setInt(2, kriteriaList.get(c).getIdKriteria());
                            pstmt.setDouble(3, val);
                            pstmt.setDouble(4, val);
                            pstmt.addBatch();
                        }
                    }
                    pstmt.executeBatch();
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Data penilaian berhasil disimpan ke database!");
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
            Vector<String> columns = new Vector<>();
            columns.add("ID");
            columns.add("Karyawan");
            for (Kriteria kr : kriteriaList) {
                columns.add(kr.getKodeKriteria());
            }

            Vector<Vector<Object>> data = new Vector<>();
            
            String sql = "SELECT k.id_karyawan, k.nama, p.id_kriteria, p.nilai FROM penilaian p " +
                         "JOIN karyawan k ON p.id_karyawan = k.id_karyawan " +
                         "WHERE k.divisi = ? ORDER BY k.nama ASC";
                         
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    int currentIdKaryawan = -1;
                    Vector<Object> currentRow = null;
                    DecimalFormat df = new DecimalFormat("0.00");
                    
                    while (rs.next()) {
                        int idKaryawan = rs.getInt("id_karyawan");
                        String nama = rs.getString("nama");
                        if (idKaryawan != currentIdKaryawan) {
                            if (currentRow != null) {
                                // Fill missing criteria with "-"
                                while(currentRow.size() < columns.size()) {
                                    currentRow.add("-");
                                }
                                data.add(currentRow);
                            }
                            currentIdKaryawan = idKaryawan;
                            currentRow = new Vector<>();
                            currentRow.add(idKaryawan);
                            currentRow.add(nama);
                            // Init with dashes
                            for(int i=0; i<kriteriaList.size(); i++) {
                                currentRow.add("-");
                            }
                        }
                        
                        int idKrit = rs.getInt("id_kriteria");
                        double nilai = rs.getDouble("nilai");
                        
                        // Find column index
                        for (int c = 0; c < kriteriaList.size(); c++) {
                            if (kriteriaList.get(c).getIdKriteria() == idKrit) {
                                currentRow.set(c + 2, df.format(nilai).replace(",", "."));
                                break;
                            }
                        }
                    }
                    if (currentRow != null) {
                        data.add(currentRow);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            tableModel.setDataVector(data, columns);
            
            // Hide the ID column
            if (tblHistory.getColumnModel().getColumnCount() > 0) {
                tblHistory.getColumnModel().getColumn(0).setMinWidth(0);
                tblHistory.getColumnModel().getColumn(0).setMaxWidth(0);
                tblHistory.getColumnModel().getColumn(0).setWidth(0);
                tblHistory.getColumnModel().getColumn(0).setPreferredWidth(0);
            }
        }
    }
}
