package com.spkbri.ui;

import com.spkbri.database.DatabaseHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtNamaLengkap;
    private JComboBox<String> cbRole;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedUserId = -1;

    public UserPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(null);
        JLabel title = new JLabel("Manajemen Akun (User)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 37, 41));
        JLabel subtitle = new JLabel("Kelola data admin dan pimpinan untuk akses ke dalam sistem");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        headerPanel.add(title);
        headerPanel.add(subtitle);

        add(headerPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        JPanel formPanelWrapper = new JPanel(new BorderLayout());
        formPanelWrapper.setBackground(Color.WHITE);
        formPanelWrapper.setBorder(new EmptyBorder(0, 0, 0, 20));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                "Form Akun", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(33, 37, 41)));

        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        txtUsername = createTextField();
        txtPassword = new JPasswordField();
        stylePasswordField(txtPassword);
        txtNamaLengkap = createTextField();
        cbRole = new JComboBox<>(new String[]{"admin", "pimpinan"});
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        addFormField(formPanel, "Username", txtUsername);
        addFormField(formPanel, "Password (Isi jika ingin diubah/dibuat)", txtPassword);
        addFormField(formPanel, "Nama Lengkap", txtNamaLengkap);

        JPanel comboPanel = new JPanel(new BorderLayout());
        comboPanel.setBackground(Color.WHITE);
        comboPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel lblRole = new JLabel("Role");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setBorder(new EmptyBorder(0, 0, 5, 0));
        comboPanel.add(lblRole, BorderLayout.NORTH);
        comboPanel.add(cbRole, BorderLayout.CENTER);
        comboPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        formPanel.add(comboPanel);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 15, 15, 15));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        btnAdd = createButton("Simpan", new Color(40, 167, 69));
        btnUpdate = createButton("Update", new Color(0, 123, 255));
        btnDelete = createButton("Hapus", new Color(220, 53, 69));
        btnClear = createButton("Bersihkan", new Color(108, 117, 125));

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> saveUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formPanel.add(btnPanel);

        formPanelWrapper.add(formPanel, BorderLayout.NORTH);
        splitPane.setLeftComponent(formPanelWrapper);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        tableModel = new DefaultTableModel(new Object[]{"ID", "Username", "Nama Lengkap", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 247, 250));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedUserId = (int) table.getValueAt(row, 0);
                txtUsername.setText((String) table.getValueAt(row, 1));
                txtNamaLengkap.setText((String) table.getValueAt(row, 2));
                cbRole.setSelectedItem(table.getValueAt(row, 3));
                txtPassword.setText("");

                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
            }
        });

        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        splitPane.setRightComponent(tablePanel);

        add(splitPane, BorderLayout.CENTER);

        loadData();
    }

    private void addFormField(JPanel parent, String labelText, JComponent inputComp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(inputComp, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(0, 15, 15, 15));

        parent.add(panel);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return tf;
    }

    private void stylePasswordField(JPasswordField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(85, 30));
        return btn;
    }

    public void loadData() {
        tableModel.setRowCount(0);
        String sql = "SELECT id_user, username, nama_lengkap, role FROM users ORDER BY id_user ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("nama_lengkap"),
                        rs.getString("role")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data akun!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String namaLengkap = txtNamaLengkap.getText().trim();
        String role = cbRole.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty() || namaLengkap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi (termasuk password)!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (username, password, nama_lengkap, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, namaLengkap);
            pstmt.setString(4, role);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Akun berhasil ditambahkan!");
            clearForm();
            loadData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan akun: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        if (selectedUserId == -1) return;

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String namaLengkap = txtNamaLengkap.getText().trim();
        String role = cbRole.getSelectedItem().toString();

        if (username.isEmpty() || namaLengkap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Nama Lengkap harus diisi!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            if (password.isEmpty()) {

                String sql = "UPDATE users SET username=?, nama_lengkap=?, role=? WHERE id_user=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, namaLengkap);
                    pstmt.setString(3, role);
                    pstmt.setInt(4, selectedUserId);
                    pstmt.executeUpdate();
                }
            } else {

                String sql = "UPDATE users SET username=?, password=?, nama_lengkap=?, role=? WHERE id_user=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    pstmt.setString(3, namaLengkap);
                    pstmt.setString(4, role);
                    pstmt.setInt(5, selectedUserId);
                    pstmt.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Akun berhasil diperbarui!");
            clearForm();
            loadData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memperbarui akun: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        if (selectedUserId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus akun ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE id_user=?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedUserId);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Akun berhasil dihapus!");
                clearForm();
                loadData();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menghapus akun: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedUserId = -1;
        table.clearSelection();
        txtUsername.setText("");
        txtPassword.setText("");
        txtNamaLengkap.setText("");
        cbRole.setSelectedIndex(0);

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
}
