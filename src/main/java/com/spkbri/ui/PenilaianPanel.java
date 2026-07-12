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

public class PenilaianPanel extends JPanel {

    private PenilaianDivisiPanel panelBisnis;
    private PenilaianDivisiPanel panelOps;

    public PenilaianPanel(DashboardPanel dashboardPanel) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(null);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(null);
        JLabel title = new JLabel("Input Penilaian Karyawan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Masukkan nilai kinerja (skala 0 - 100) karyawan untuk masing-masing kriteria");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        panelBisnis.loadKaryawan();
        panelBisnis.loadKriteria();
        panelOps.loadKaryawan();
        panelOps.loadKriteria();
    }

    private static class PenilaianDivisiPanel extends JPanel {
        private String divisi;
        private DashboardPanel dashboardPanel;

        private JTable tblKaryawan;
        private DefaultTableModel tableModel;
        private JPanel dynamicFormPanel;
        private JButton btnSimpan;

        private int selectedKaryawanId = -1;
        private List<Kriteria> kriteriaList = new ArrayList<>();
        private Map<Integer, JTextField> fieldsMap = new HashMap<>();

        public PenilaianDivisiPanel(String divisi, DashboardPanel dashboardPanel) {
            this.divisi = divisi;
            this.dashboardPanel = dashboardPanel;

            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 10, 10, 10);

            // Left Side: Karyawan List
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
            tblKaryawan.getColumnModel().getColumn(0).setMaxWidth(50);
            tblKaryawan.getSelectionModel().addListSelectionListener(e -> {
                int r = tblKaryawan.getSelectedRow();
                if (r != -1) {
                    selectedKaryawanId = (int) tblKaryawan.getValueAt(r, 0);
                    loadDynamicForm();
                }
            });

            listPanel.add(new JLabel("Pilih Karyawan:", JLabel.LEFT), BorderLayout.NORTH);
            listPanel.add(new JScrollPane(tblKaryawan), BorderLayout.CENTER);

            // Right Side: Dynamic Form
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBackground(Color.WHITE);
            rightPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(15, 15, 15, 15)
            ));

            JLabel lblFormTitle = new JLabel("Form Input Nilai");
            lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblFormTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
            rightPanel.add(lblFormTitle, BorderLayout.NORTH);

            dynamicFormPanel = new JPanel();
            dynamicFormPanel.setLayout(new GridBagLayout());
            dynamicFormPanel.setBackground(Color.WHITE);
            
            JScrollPane scrollPane = new JScrollPane(dynamicFormPanel);
            scrollPane.setBorder(null);
            rightPanel.add(scrollPane, BorderLayout.CENTER);

            btnSimpan = new JButton("Simpan Nilai");
            btnSimpan.setBackground(new Color(0, 82, 162));
            btnSimpan.setForeground(Color.WHITE);
            btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnSimpan.setEnabled(false);
            btnSimpan.addActionListener(e -> savePenilaian());
            rightPanel.add(btnSimpan, BorderLayout.SOUTH);

            // Add Form & Table to GridBag
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.45;
            gbc.weighty = 1.0;
            add(listPanel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.55;
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
                btnSimpan.setEnabled(false);
                JLabel lblMsg = new JLabel("<html><center>Data karyawan kosong.<br>Silakan tambahkan data karyawan terlebih dahulu di menu <b>Data Karyawan</b>.</center></html>", SwingConstants.CENTER);
                lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblMsg.setForeground(Color.GRAY);
                dynamicFormPanel.setLayout(new BorderLayout());
                dynamicFormPanel.add(lblMsg, BorderLayout.CENTER);
                dynamicFormPanel.revalidate();
                dynamicFormPanel.repaint();
                return;
            }

            if (selectedKaryawanId == -1) {
                btnSimpan.setEnabled(false);
                JLabel lblMsg = new JLabel("Silakan pilih karyawan pada tabel di sebelah kiri.", SwingConstants.CENTER);
                lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblMsg.setForeground(Color.GRAY);
                dynamicFormPanel.setLayout(new BorderLayout());
                dynamicFormPanel.add(lblMsg, BorderLayout.CENTER);
                dynamicFormPanel.revalidate();
                dynamicFormPanel.repaint();
                return;
            }

            if (kriteriaList.isEmpty()) {
                btnSimpan.setEnabled(false);
                JLabel lblMsg = new JLabel("Data kriteria kosong untuk divisi ini.", SwingConstants.CENTER);
                lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblMsg.setForeground(Color.GRAY);
                dynamicFormPanel.setLayout(new BorderLayout());
                dynamicFormPanel.add(lblMsg, BorderLayout.CENTER);
                dynamicFormPanel.revalidate();
                dynamicFormPanel.repaint();
                return;
            }

            dynamicFormPanel.setLayout(new GridBagLayout());

            // Get existing values
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

            // Populate Form
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(5, 5, 5, 5);

            for (Kriteria k : kriteriaList) {
                JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
                rowPanel.setBackground(Color.WHITE);

                JLabel lblKriteria = new JLabel(k.getKodeKriteria() + " - " + k.getNamaKriteria() + " (" + k.getSifat() + ")");
                lblKriteria.setFont(new Font("Segoe UI", Font.BOLD, 12));
                rowPanel.add(lblKriteria, BorderLayout.NORTH);

                JTextField txtNilai = new JTextField();
                txtNilai.setPreferredSize(new Dimension(200, 35));
                
                // Set default if exists
                if (existingValues.containsKey(k.getIdKriteria())) {
                    txtNilai.setText(String.valueOf(existingValues.get(k.getIdKriteria())));
                } else {
                    txtNilai.setText("0");
                }

                rowPanel.add(txtNilai, BorderLayout.CENTER);
                fieldsMap.put(k.getIdKriteria(), txtNilai);

                dynamicFormPanel.add(rowPanel, gbc);
                gbc.gridy++;
            }

            // Spacer
            JPanel spacer = new JPanel();
            spacer.setBackground(Color.WHITE);
            gbc.weighty = 1.0;
            dynamicFormPanel.add(spacer, gbc);

            btnSimpan.setEnabled(true);
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
                            if (nilai < 0 || nilai > 100) {
                                throw new NumberFormatException("Nilai harus 0-100");
                            }
                        } catch (NumberFormatException e) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, "Nilai kriteria harus berupa angka desimal antara 0 - 100!", "Error Validasi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        pstmt.setInt(1, selectedKaryawanId);
                        pstmt.setInt(2, kriteriaId);
                        pstmt.setDouble(3, nilai);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Semua nilai kriteria berhasil disimpan!");
                    dashboardPanel.refreshData();
                } catch (SQLException e) {
                    conn.rollback();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan nilai kriteria: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
