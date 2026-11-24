package klinik.client.ui;

import klinik.client.KlinikClient;
import klinik.common.*;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    JTextField txtUser = new JTextField(20);
    JPasswordField txtPass = new JPasswordField(20);

    public LoginFrame() {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception e) {
        }

        // DESAIN: Pakai Nimbus biar lebih modern
        setTitle("Login - Klinik Sehat Nusantara");
        setSize(650, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        // KIRI: Panel Branding
        JPanel kiri = new JPanel(new GridBagLayout());
        kiri.setBackground(new Color(0, 150, 136));
        kiri.setPreferredSize(new Dimension(280, 380));
        JLabel judul = new JLabel("<html><center><font size=6><b>KLINIK</b></font><br><font size=5>Sehat Nusantara</font><br><br><font size=3>Pematangsiantar<br>Client - Server System</font></center></html>");
        judul.setForeground(Color.WHITE);
        kiri.add(judul);
        // KANAN: Form Login
        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lJudul = new JLabel("Selamat Datang");
        lJudul.setFont(new Font("SansSerif", Font.BOLD, 20));
        lJudul.setForeground(new Color(0, 150, 136));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        kanan.add(lJudul, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        kanan.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        kanan.add(txtUser, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        kanan.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        kanan.add(txtPass, gbc);
        JButton btn = new JButton("LOGIN");
        btn.setBackground(new Color(0, 150, 136));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        kanan.add(btn, gbc);
        // PASSWORD DISEMBUNYIKAN, ada tombol Lihat
        JCheckBox chkLihat = new JCheckBox("Lihat Password");
        chkLihat.setBackground(Color.WHITE);
        chkLihat.addActionListener(e -> {
            if (chkLihat.isSelected()) {
                txtPass.setEchoChar((char) 0); // tampilkan huruf
            } else {
                txtPass.setEchoChar('\u2022'); // sembunyikan jadi bulat-bulat
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        kanan.add(chkLihat, gbc);
        // geser  ke bawah
        txtPass.setEchoChar('\u2022'); // default: password disembunyikan
        // LUPA PASSWORD
        JButton btnLupa = new JButton("Lupa Password?");
        btnLupa.setBorderPainted(false);
        btnLupa.setContentAreaFilled(false);
        btnLupa.setForeground(new java.awt.Color(0, 150, 136));
        btnLupa.addActionListener(e -> {
            String user = JOptionPane.showInputDialog(this, "Masukkan Username yang lupa passwordnya:");
            if (user == null || user.trim().isEmpty()) {
                return;
            }
            JPasswordField pf1 = new JPasswordField(), pf2 = new JPasswordField();
            Object[] isi = {"Password Baru:", pf1, "Ulangi Password Baru:", pf2};
            int ok = JOptionPane.showConfirmDialog(this, isi, "Reset Password - " + user, JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }
            String p1 = new String(pf1.getPassword()), p2 = new String(pf2.getPassword());
            if (p1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password baru tidak boleh kosong!");
                return;
            }
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Password baru tidak sama! Coba lagi.");
                return;
            }
            if (!KlinikClient.getInstance().connect()) {
                JOptionPane.showMessageDialog(this, "Server belum jalan!");
                return;
            }
            Response r = KlinikClient.getInstance().kirim(new Request("RESET_PASSWORD", user, p1));
            JOptionPane.showMessageDialog(this, r.message);
            if (r.success) {
                JOptionPane.showMessageDialog(this, "Silakan login dengan password baru!");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        kanan.add(btnLupa, gbc);
        btn.addActionListener(e -> login());
        txtPass.addActionListener(e -> login());
        txtUser.addActionListener(e -> login());
        getRootPane().setDefaultButton(btn);
        add(kiri, BorderLayout.WEST);
        add(kanan, BorderLayout.CENTER);
    }

    void login() {
        if (!KlinikClient.getInstance().connect()) {
            JOptionPane.showMessageDialog(this, "Server belum jalan! Jalankan KlinikServer dulu.");
            return;
        }
        Response r = KlinikClient.getInstance().kirim(new Request("LOGIN", txtUser.getText(), new String(txtPass.getPassword())));
        if (r.success) {
            new MainFrame(r.role).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, r.message);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
