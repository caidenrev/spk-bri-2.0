package com.spkbri.ui;

import com.spkbri.database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KaryawanPanel extends JPanel {

    private JTextField txtKodeKaryawan;
    private JTextField txtNama;
    private JComboBox<String> cbDivisi;
    private JTable tblKaryawan;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterDivisi;

    private JButton btnSimpan;
    private JButton btnEdit;
    private JButton btnHapus;
    private JButton btnBatal;

    private int selectedId = -1;
    private DashboardPanel dashboardPanel;

    public KaryawanPanel(DashboardPanel dashboardPanel) {
        this.dashboardPanel = dashboardPanel;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel("Kelola Data Karyawan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Tambah, Edit, dan Hapus data karyawan Bank BRI KCP Arundina");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(null);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(15, 0, 0, 0);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblNik = new JLabel("Kode Karyawan");
        lblNik.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtKodeKaryawan = new JTextField();
        txtKodeKaryawan.setPreferredSize(new Dimension(200, 35));
        txtKodeKaryawan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel lblNama = new JLabel("Nama Lengkap");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtNama = new JTextField();
        txtNama.setPreferredSize(new Dimension(200, 35));
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel lblDivisi = new JLabel("Divisi");
        lblDivisi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cbDivisi = new JComboBox<>(new String[]{"Bisnis", "Operasional"});
        cbDivisi.setPreferredSize(new Dimension(200, 35));
        cbDivisi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        formPanel.add(lblNik);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtKodeKaryawan);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(lblNama);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtNama);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(lblDivisi);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(cbDivisi);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setBackground(null);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        btnSimpan = new JButton("Simpan");
        btnSimpan.setBackground(new Color(0, 82, 162));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimpan.addActionListener(e -> saveKaryawan());

        btnEdit = new JButton("Update");
        btnEdit.setBackground(new Color(242, 142, 43));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(e -> updateKaryawan());

        btnHapus = new JButton("Hapus");
        btnHapus.setBackground(new Color(220, 53, 69));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(e -> deleteKaryawan());

        btnBatal = new JButton("Batal");
        btnBatal.addActionListener(e -> clearForm());

        btnPanel.add(btnSimpan);
        btnPanel.add(btnEdit);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBatal);

        formPanel.add(btnPanel);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel searchBarPanel = new JPanel(new BorderLayout(10, 0));
        searchBarPanel.setBackground(null);
        searchBarPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 30));
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari karyawan...");
        txtSearch.addActionListener(e -> loadTableData(txtSearch.getText().trim()));
        JButton btnSearch = new JButton("Cari");
        btnSearch.addActionListener(e -> loadTableData(txtSearch.getText().trim()));

        cbFilterDivisi = new JComboBox<>(new String[]{"Semua Divisi", "Bisnis", "Operasional"});
        cbFilterDivisi.setPreferredSize(new Dimension(150, 30));
        cbFilterDivisi.addActionListener(e -> loadTableData(txtSearch.getText().trim()));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        filterPanel.setBackground(null);
        filterPanel.add(cbFilterDivisi);
        filterPanel.add(btnSearch);

        searchBarPanel.add(txtSearch, BorderLayout.CENTER);
        searchBarPanel.add(filterPanel, BorderLayout.EAST);

        tablePanel.add(searchBarPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Kode Karyawan", "Nama Karyawan", "Divisi"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKaryawan = new JTable(tableModel);
        tblKaryawan.setRowHeight(25);
        tblKaryawan.getColumnModel().getColumn(0).setMaxWidth(50);
        tblKaryawan.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tblKaryawan.getSelectedRow();
            if (selectedRow != -1) {
                selectedId = (int) tblKaryawan.getValueAt(selectedRow, 0);
                txtKodeKaryawan.setText((String) tblKaryawan.getValueAt(selectedRow, 1));
                txtNama.setText((String) tblKaryawan.getValueAt(selectedRow, 2));
                cbDivisi.setSelectedItem(tblKaryawan.getValueAt(selectedRow, 3));

                btnSimpan.setEnabled(false);
                btnEdit.setEnabled(true);
                btnHapus.setEnabled(true);
            }
        });

        tablePanel.add(new JScrollPane(tblKaryawan), BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        contentPanel.add(formPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.insets = new Insets(15, 20, 0, 0);
        contentPanel.add(tablePanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        loadTableData("");
    }

    private void loadTableData(String searchKeyword) {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM karyawan WHERE 1=1";

        String filterDivisi = cbFilterDivisi != null ? (String) cbFilterDivisi.getSelectedItem() : "Semua Divisi";
        if (filterDivisi != null && !filterDivisi.equals("Semua Divisi")) {
            sql += " AND divisi = ?";
        }

        if (!searchKeyword.isEmpty()) {
            sql += " AND (nama LIKE ? OR kode_karyawan LIKE ?)";
        }
        sql += " ORDER BY id_karyawan ASC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (filterDivisi != null && !filterDivisi.equals("Semua Divisi")) {
                pstmt.setString(paramIndex++, filterDivisi);
            }

            if (!searchKeyword.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchKeyword + "%");
                pstmt.setString(paramIndex++, "%" + searchKeyword + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id_karyawan"),
                            rs.getString("kode_karyawan"),
                            rs.getString("nama"),
                            rs.getString("divisi")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data karyawan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveKaryawan() {
        String kodeKaryawan = txtKodeKaryawan.getText().trim();
        String nama = txtNama.getText().trim();
        String divisi = (String) cbDivisi.getSelectedItem();

        if (kodeKaryawan.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO karyawan (kode_karyawan, nama, divisi) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kodeKaryawan);
            pstmt.setString(2, nama);
            pstmt.setString(3, divisi);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data karyawan berhasil disimpan!");
            clearForm();
            loadTableData("");
            dashboardPanel.refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data (Kode Karyawan mungkin sudah terdaftar): " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateKaryawan() {
        if (selectedId == -1) return;

        String kodeKaryawan = txtKodeKaryawan.getText().trim();
        String nama = txtNama.getText().trim();
        String divisi = (String) cbDivisi.getSelectedItem();

        if (kodeKaryawan.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE karyawan SET kode_karyawan = ?, nama = ?, divisi = ? WHERE id_karyawan = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kodeKaryawan);
            pstmt.setString(2, nama);
            pstmt.setString(3, divisi);
            pstmt.setInt(4, selectedId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data karyawan berhasil diupdate!");
            clearForm();
            loadTableData("");
            dashboardPanel.refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteKaryawan() {
        if (selectedId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data karyawan ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM karyawan WHERE id_karyawan = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data karyawan berhasil dihapus!");
            clearForm();
            loadTableData("");
            dashboardPanel.refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtKodeKaryawan.setText("");
        txtNama.setText("");
        cbDivisi.setSelectedIndex(0);
        selectedId = -1;
        tblKaryawan.clearSelection();

        btnSimpan.setEnabled(true);
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
    }
}
