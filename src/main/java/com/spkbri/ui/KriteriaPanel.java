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

public class KriteriaPanel extends JPanel {

    public KriteriaPanel(DashboardPanel dashboardPanel) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel("Kelola Data Kriteria");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Atur kriteria penilaian, sifat (Benefit/Cost), dan bobot untuk setiap divisi");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        KriteriaDivisiPanel panelBisnis = new KriteriaDivisiPanel("Bisnis", dashboardPanel);

        KriteriaDivisiPanel panelOps = new KriteriaDivisiPanel("Operasional", dashboardPanel);

        tabbedPane.addTab("Divisi Bisnis", panelBisnis);
        tabbedPane.addTab("Divisi Operasional", panelOps);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private static class KriteriaDivisiPanel extends JPanel {
        private String divisi;
        private DashboardPanel dashboardPanel;

        private JTextField txtKode;
        private JTextField txtNama;
        private JComboBox<String> cbSifat;
        private JTextField txtBobot;
        private JTable tblKriteria;
        private DefaultTableModel tableModel;
        private JButton btnSimpan;
        private JButton btnEdit;
        private JButton btnHapus;
        private JButton btnBatal;

        private int selectedId = -1;

        public KriteriaDivisiPanel(String divisi, DashboardPanel dashboardPanel) {
            this.divisi = divisi;
            this.dashboardPanel = dashboardPanel;

            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(15, 15, 15, 15));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(10, 10, 10, 10);

            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(Color.WHITE);
            formPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(15, 15, 15, 15)
            ));

            JLabel lblKode = new JLabel("Kode Kriteria (e.g. C1, C2)");
            lblKode.setFont(new Font("Segoe UI", Font.BOLD, 12));
            txtKode = new JTextField();
            txtKode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            JLabel lblNama = new JLabel("Nama Kriteria");
            lblNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
            txtNama = new JTextField();
            txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            JLabel lblSifat = new JLabel("Sifat");
            lblSifat.setFont(new Font("Segoe UI", Font.BOLD, 12));
            cbSifat = new JComboBox<>(new String[]{"Benefit", "Cost"});
            cbSifat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            JLabel lblBobot = new JLabel("Bobot (e.g. 0.25)");
            lblBobot.setFont(new Font("Segoe UI", Font.BOLD, 12));
            txtBobot = new JTextField();
            txtBobot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

            formPanel.add(lblKode);
            formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            formPanel.add(txtKode);
            formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            formPanel.add(lblNama);
            formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            formPanel.add(txtNama);
            formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            formPanel.add(lblSifat);
            formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            formPanel.add(cbSifat);
            formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            formPanel.add(lblBobot);
            formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            formPanel.add(txtBobot);
            formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            btnPanel.setBackground(null);
            btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            btnSimpan = new JButton("Simpan");
            btnSimpan.setBackground(new Color(0, 82, 162));
            btnSimpan.setForeground(Color.WHITE);
            btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnSimpan.addActionListener(e -> saveKriteria());

            btnEdit = new JButton("Update");
            btnEdit.setBackground(new Color(242, 142, 43));
            btnEdit.setForeground(Color.WHITE);
            btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnEdit.setEnabled(false);
            btnEdit.addActionListener(e -> updateKriteria());

            btnHapus = new JButton("Hapus");
            btnHapus.setBackground(new Color(220, 53, 69));
            btnHapus.setForeground(Color.WHITE);
            btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnHapus.setEnabled(false);
            btnHapus.addActionListener(e -> deleteKriteria());

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
                    new EmptyBorder(10, 10, 10, 10)
            ));

            tableModel = new DefaultTableModel(new Object[]{"ID", "Kode", "Nama Kriteria", "Sifat", "Bobot"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblKriteria = new JTable(tableModel);
            tblKriteria.setRowHeight(25);
            tblKriteria.getColumnModel().getColumn(0).setMaxWidth(50);
            tblKriteria.getColumnModel().getColumn(1).setMaxWidth(80);
            tblKriteria.getSelectionModel().addListSelectionListener(e -> {
                int r = tblKriteria.getSelectedRow();
                if (r != -1) {
                    selectedId = (int) tblKriteria.getValueAt(r, 0);
                    txtKode.setText((String) tblKriteria.getValueAt(r, 1));
                    txtNama.setText((String) tblKriteria.getValueAt(r, 2));
                    cbSifat.setSelectedItem(tblKriteria.getValueAt(r, 3));
                    txtBobot.setText(String.valueOf(tblKriteria.getValueAt(r, 4)));

                    btnSimpan.setEnabled(false);
                    btnEdit.setEnabled(true);
                    btnHapus.setEnabled(true);
                }
            });

            tablePanel.add(new JScrollPane(tblKriteria), BorderLayout.CENTER);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.35;
            gbc.weighty = 1.0;
            add(formPanel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.65;
            add(tablePanel, gbc);

            loadTableData();
        }

        private void loadTableData() {
            tableModel.setRowCount(0);
            String sql = "SELECT * FROM kriteria WHERE divisi = ? ORDER BY kode_kriteria ASC";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, divisi);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getInt("id_kriteria"),
                                rs.getString("kode_kriteria"),
                                rs.getString("nama_kriteria"),
                                rs.getString("sifat"),
                                rs.getDouble("bobot")
                        });
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void saveKriteria() {
            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            String sifat = (String) cbSifat.getSelectedItem();
            String bobotStr = txtBobot.getText().trim();

            if (kode.isEmpty() || nama.isEmpty() || bobotStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double bobot;
            try {
                bobot = Double.parseDouble(bobotStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Bobot harus berupa angka desimal!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO kriteria (kode_kriteria, nama_kriteria, sifat, bobot, divisi) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, kode);
                pstmt.setString(2, nama);
                pstmt.setString(3, sifat);
                pstmt.setDouble(4, bobot);
                pstmt.setString(5, divisi);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Kriteria berhasil disimpan!");
                clearForm();
                loadTableData();
                dashboardPanel.refreshData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan kriteria: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updateKriteria() {
            if (selectedId == -1) return;

            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            String sifat = (String) cbSifat.getSelectedItem();
            String bobotStr = txtBobot.getText().trim();

            if (kode.isEmpty() || nama.isEmpty() || bobotStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double bobot;
            try {
                bobot = Double.parseDouble(bobotStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Bobot harus berupa angka desimal!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "UPDATE kriteria SET kode_kriteria = ?, nama_kriteria = ?, sifat = ?, bobot = ? WHERE id_kriteria = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, kode);
                pstmt.setString(2, nama);
                pstmt.setString(3, sifat);
                pstmt.setDouble(4, bobot);
                pstmt.setInt(5, selectedId);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Kriteria berhasil diupdate!");
                clearForm();
                loadTableData();
                dashboardPanel.refreshData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mengupdate kriteria: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteKriteria() {
            if (selectedId == -1) return;

            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus kriteria ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            String sql = "DELETE FROM kriteria WHERE id_kriteria = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedId);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Kriteria berhasil dihapus!");
                clearForm();
                loadTableData();
                dashboardPanel.refreshData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menghapus kriteria: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void clearForm() {
            txtKode.setText("");
            txtNama.setText("");
            cbSifat.setSelectedIndex(0);
            txtBobot.setText("");
            selectedId = -1;
            tblKriteria.clearSelection();

            btnSimpan.setEnabled(true);
            btnEdit.setEnabled(false);
            btnHapus.setEnabled(false);
        }
    }
}
