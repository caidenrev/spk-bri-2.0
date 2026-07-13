package com.spkbri.ui;

import com.spkbri.database.DatabaseHelper;
import com.spkbri.model.Kriteria;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel input penilaian untuk Pimpinan.
 * Pimpinan dapat menginput dan mengupdate nilai, namun tidak dapat menghapus data.
 */
public class PimpinanPenilaianPanel extends JPanel {

    private PimpinanPenilaianDivisiPanel panelBisnis;
    private PimpinanPenilaianDivisiPanel panelOps;

    public PimpinanPenilaianPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel("Input Penilaian Karyawan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Masukkan nilai kinerja (skala 0 - 100) karyawan untuk masing-masing kriteria");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);
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
        panelBisnis.loadKaryawan();
        panelBisnis.loadKriteria();
        panelOps.loadKaryawan();
        panelOps.loadKriteria();
    }

    private static class PimpinanPenilaianDivisiPanel extends JPanel {

        private final String divisi;
        private JTable tblKaryawan;
        private DefaultTableModel tableModel;
        private JPanel dynamicFormPanel;
        private JButton btnSimpan;

        private int selectedKaryawanId = -1;
        private List<Kriteria> kriteriaList = new ArrayList<>();
        private Map<Integer, JTextField> fieldsMap = new HashMap<>();

        public PimpinanPenilaianDivisiPanel(String divisi) {
            this.divisi = divisi;

            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 10, 10, 10);

            // Kiri: Daftar Karyawan
            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setBackground(Color.WHITE);
            listPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(10, 10, 10, 10)
            ));

            tableModel = new DefaultTableModel(new Object[]{"ID", "NIK", "Nama Karyawan"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblKaryawan = new JTable(tableModel);
            tblKaryawan.setRowHeight(25);
            tblKaryawan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblKaryawan.getColumnModel().getColumn(0).setMaxWidth(50);
            tblKaryawan.getSelectionModel().addListSelectionListener(e -> {
                int r = tblKaryawan.getSelectedRow();
                if (r != -1) {
                    selectedKaryawanId = (int) tblKaryawan.getValueAt(r, 0);
                    loadDynamicForm();
                }
            });

            JLabel lblPilih = new JLabel("Pilih Karyawan:");
            lblPilih.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPilih.setBorder(new EmptyBorder(0, 0, 8, 0));
            listPanel.add(lblPilih, BorderLayout.NORTH);
            listPanel.add(new JScrollPane(tblKaryawan), BorderLayout.CENTER);

            // Kanan: Form Nilai Dinamis
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBackground(Color.WHITE);
            rightPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(15, 15, 15, 15)
            ));

            JLabel lblFormTitle = new JLabel("Form Input Nilai");
            lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblFormTitle.setForeground(new Color(33, 37, 41));
            lblFormTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
            rightPanel.add(lblFormTitle, BorderLayout.NORTH);

            dynamicFormPanel = new JPanel(new GridBagLayout());
            dynamicFormPanel.setBackground(Color.WHITE);
            JScrollPane scrollPane = new JScrollPane(dynamicFormPanel);
            scrollPane.setBorder(null);
            rightPanel.add(scrollPane, BorderLayout.CENTER);

            // Tombol simpan saja — tidak ada tombol hapus
            JPanel bottomPanel = new JPanel(new BorderLayout(0, 5));
            bottomPanel.setBackground(Color.WHITE);
            bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

            JLabel lblNote = new JLabel("  ℹ  Nilai yang sudah ada akan diperbarui otomatis.");
            lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblNote.setForeground(new Color(100, 116, 139));

            btnSimpan = new JButton("Simpan Nilai");
            btnSimpan.setBackground(new Color(0, 82, 162));
            btnSimpan.setForeground(Color.WHITE);
            btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnSimpan.setPreferredSize(new Dimension(0, 40));
            btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSimpan.setEnabled(false);
            btnSimpan.addActionListener(e -> savePenilaian());

            bottomPanel.add(lblNote, BorderLayout.NORTH);
            bottomPanel.add(btnSimpan, BorderLayout.SOUTH);
            rightPanel.add(bottomPanel, BorderLayout.SOUTH);

            // Layout
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.42;
            gbc.weighty = 1.0;
            add(listPanel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.58;
            add(rightPanel, gbc);

            loadKaryawan();
            loadKriteria();
        }

        public void loadKaryawan() {
            tableModel.setRowCount(0);
            String sql = "SELECT * FROM karyawan WHERE divisi = ? ORDER BY nama ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getInt("id_karyawan"),
                                rs.getString("nik"),
                                rs.getString("nama")
                        });
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            selectedKaryawanId = -1;
            loadDynamicForm();
        }

        public void loadKriteria() {
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
            loadDynamicForm();
        }

        private void loadDynamicForm() {
            dynamicFormPanel.removeAll();
            fieldsMap.clear();

            if (tableModel.getRowCount() == 0) {
                showPlaceholder("Data karyawan divisi ini belum tersedia.");
                btnSimpan.setEnabled(false);
                return;
            }

            if (selectedKaryawanId == -1) {
                showPlaceholder("Pilih karyawan pada daftar di sebelah kiri.");
                btnSimpan.setEnabled(false);
                return;
            }

            if (kriteriaList.isEmpty()) {
                showPlaceholder("Data kriteria untuk divisi ini belum tersedia.");
                btnSimpan.setEnabled(false);
                return;
            }

            dynamicFormPanel.setLayout(new GridBagLayout());

            // Ambil nilai yang sudah ada
            Map<Integer, Double> existingValues = new HashMap<>();
            String sql = "SELECT id_kriteria, nilai FROM penilaian WHERE id_karyawan = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedKaryawanId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        existingValues.put(rs.getInt("id_kriteria"), rs.getDouble("nilai"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(5, 5, 5, 5);

            for (Kriteria k : kriteriaList) {
                JPanel rowPanel = new JPanel(new BorderLayout(10, 3));
                rowPanel.setBackground(new Color(250, 251, 252));
                rowPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230)),
                        new EmptyBorder(8, 10, 8, 10)
                ));

                JLabel lblKriteria = new JLabel(k.getKodeKriteria() + "  —  " + k.getNamaKriteria());
                lblKriteria.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblKriteria.setForeground(new Color(33, 37, 41));

                JLabel lblSifat = new JLabel("Sifat: " + k.getSifat() + "  |  Bobot: " + k.getBobot());
                lblSifat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblSifat.setForeground(Color.GRAY);

                JPanel labelPanel = new JPanel(new GridLayout(2, 1));
                labelPanel.setBackground(null);
                labelPanel.add(lblKriteria);
                labelPanel.add(lblSifat);

                JTextField txtNilai = new JTextField();
                txtNilai.setPreferredSize(new Dimension(90, 32));
                txtNilai.setHorizontalAlignment(JTextField.CENTER);
                txtNilai.setFont(new Font("Segoe UI", Font.BOLD, 13));

                if (existingValues.containsKey(k.getIdKriteria())) {
                    txtNilai.setText(String.valueOf(existingValues.get(k.getIdKriteria())));
                    txtNilai.setForeground(new Color(0, 82, 162));
                } else {
                    txtNilai.setText("0");
                }

                rowPanel.add(labelPanel, BorderLayout.CENTER);
                rowPanel.add(txtNilai, BorderLayout.EAST);
                fieldsMap.put(k.getIdKriteria(), txtNilai);

                dynamicFormPanel.add(rowPanel, gbc);
                gbc.gridy++;
            }

            JPanel spacer = new JPanel();
            spacer.setBackground(Color.WHITE);
            gbc.weighty = 1.0;
            dynamicFormPanel.add(spacer, gbc);

            btnSimpan.setEnabled(true);
            dynamicFormPanel.revalidate();
            dynamicFormPanel.repaint();
        }

        private void showPlaceholder(String message) {
            dynamicFormPanel.removeAll();
            dynamicFormPanel.setLayout(new BorderLayout());
            JLabel lbl = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(Color.GRAY);
            dynamicFormPanel.add(lbl, BorderLayout.CENTER);
            dynamicFormPanel.revalidate();
            dynamicFormPanel.repaint();
        }

        private void savePenilaian() {
            if (selectedKaryawanId == -1) return;

            try (Connection conn = DatabaseHelper.getConnection()) {
                conn.setAutoCommit(false);
                String sql = "INSERT INTO penilaian (id_karyawan, id_kriteria, nilai) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE nilai = VALUES(nilai)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (Map.Entry<Integer, JTextField> entry : fieldsMap.entrySet()) {
                        int kriteriaId = entry.getKey();
                        String nilaiStr = entry.getValue().getText().trim();

                        double nilai;
                        try {
                            nilai = Double.parseDouble(nilaiStr);
                            if (nilai < 0 || nilai > 100) throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this,
                                    "Nilai harus berupa angka antara 0 sampai 100!",
                                    "Error Validasi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        pstmt.setInt(1, selectedKaryawanId);
                        pstmt.setInt(2, kriteriaId);
                        pstmt.setDouble(3, nilai);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Nilai berhasil disimpan!");
                    // Reload form supaya warna field update (biru = sudah ada nilai)
                    loadDynamicForm();
                } catch (SQLException e) {
                    conn.rollback();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Gagal menyimpan nilai: " + e.getMessage(),
                            "Error Database", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
