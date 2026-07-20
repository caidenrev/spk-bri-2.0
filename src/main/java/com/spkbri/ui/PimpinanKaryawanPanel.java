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

/**
 * Panel read-only untuk Pimpinan — hanya menampilkan daftar karyawan,
 * tidak ada aksi tambah/edit/hapus.
 */
public class PimpinanKaryawanPanel extends JPanel {

    private JTable tblKaryawan;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    public PimpinanKaryawanPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel("Data Karyawan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Daftar seluruh karyawan Bank BRI KCP Arundina");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Search Bar
        JPanel searchBarPanel = new JPanel(new BorderLayout(10, 0));
        searchBarPanel.setBackground(null);
        searchBarPanel.setBorder(new EmptyBorder(0, 0, 12, 0));
        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari karyawan berdasarkan nama atau Kode Karyawan...");
        JButton btnSearch = new JButton("Cari");
        btnSearch.setBackground(new Color(0, 82, 162));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.addActionListener(e -> loadTableData(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadTableData(txtSearch.getText().trim()));
        searchBarPanel.add(txtSearch, BorderLayout.CENTER);
        searchBarPanel.add(btnSearch, BorderLayout.EAST);
        tablePanel.add(searchBarPanel, BorderLayout.NORTH);

        // Table — read-only, tidak bisa di-edit
        tableModel = new DefaultTableModel(new Object[]{"No", "Kode Karyawan", "Nama Karyawan", "Divisi"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKaryawan = new JTable(tableModel);
        tblKaryawan.setRowHeight(28);
        tblKaryawan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblKaryawan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblKaryawan.getColumnModel().getColumn(0).setMaxWidth(50);
        tblKaryawan.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblKaryawan.getColumnModel().getColumn(3).setPreferredWidth(120);

        tablePanel.add(new JScrollPane(tblKaryawan), BorderLayout.CENTER);

        // Info label — pemberitahuan read-only
        JLabel lblInfo = new JLabel("  ℹ  Anda hanya dapat melihat data karyawan. Perubahan data dilakukan oleh Administrator.");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(new Color(100, 116, 139));
        lblInfo.setBorder(new EmptyBorder(8, 0, 0, 0));
        tablePanel.add(lblInfo, BorderLayout.SOUTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(null);
        contentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        loadTableData("");
    }

    private void loadTableData(String keyword) {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM karyawan";
        if (!keyword.isEmpty()) {
            sql += " WHERE nama LIKE ? OR kode_karyawan LIKE ?";
        }
        sql += " ORDER BY divisi ASC, nama ASC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (!keyword.isEmpty()) {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            no++,
                            rs.getString("kode_karyawan"),
                            rs.getString("nama"),
                            rs.getString("divisi")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data karyawan: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        loadTableData("");
    }
}
