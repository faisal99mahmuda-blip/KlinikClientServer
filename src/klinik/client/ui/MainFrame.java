package klinik.client.ui;

import klinik.client.KlinikClient;
import klinik.common.*;
import klinik.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    DefaultTableModel mPasien = new DefaultTableModel(new String[]{"ID", "Nama", "NIK", "Tgl Lahir", "Gender", "Telepon", "Darah", "Alergi"}, 0);
    DefaultTableModel mAntrian = new DefaultTableModel(new String[]{"ID", "No", "Pasien", "Dokter", "Tanggal", "Jam", "Keluhan", "Status"}, 0);
    DefaultTableModel mRekam = new DefaultTableModel(new String[]{"ID", "Pasien", "Diagnosa", "Tindakan", "Resep", "Tanggal"}, 0);
    DefaultTableModel mDokter = new DefaultTableModel(new String[]{"ID", "Nama", "Spesialis", "Jadwal", "Status"}, 0);
    DefaultTableModel mObat = new DefaultTableModel(new String[]{"ID", "Nama Obat", "Stok", "Harga"}, 0);
    JTable tPasien = new JTable(mPasien), tAntrian = new JTable(mAntrian), tRekam = new JTable(mRekam), tObat = new JTable(mObat), tDokter = new JTable(mDokter);
    JLabel lblTotalTab = new JLabel("Total Pasien: 0");
    boolean sudahPeringatanStok = false;
    JLabel lblJagaCard = new JLabel("Dokter Jaga: -");
    JLabel tglDash = new JLabel("");
    JLabel lblStokWarning = new JLabel("");
    JLabel lblPendapatan = new JLabel("Pendapatan Hari Ini: Rp 0");
    JTabbedPane tab;
    JLabel lblNextAntrian = new JLabel("Antrian Berikutnya: -");
    JLabel lblHariIni = new JLabel("Hari ini: -");
    JLabel lblTotalPasien = new JLabel("0", SwingConstants.LEFT), lblAntrian = new JLabel("0", SwingConstants.LEFT), lblObat = new JLabel("0", SwingConstants.LEFT), lblDokter = new JLabel("dr. Faisal Mahmuda", SwingConstants.CENTER), lblInfo = new JLabel("", SwingConstants.LEFT), lblRekam = new JLabel("0", SwingConstants.LEFT);

    public MainFrame(String role) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception e) {
        }

        setTitle("Klinik - Dashboard (" + role + ")");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        tab = new JTabbedPane();
        // === DASHBOARD PRO ===
        JPanel pDash = new JPanel(new BorderLayout());
        pDash.setBackground(new Color(245, 247, 250));
        // HEADER: Sapaan + tanggal
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(new Color(0, 150, 136));
        head.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel sapaan = new JLabel("Selamat Datang, " + role + " 👋");
        sapaan.setFont(new Font("SansSerif", Font.BOLD, 22));
        sapaan.setForeground(Color.WHITE);
        tglDash = new JLabel("Klinik Sehat Nusantara  •  Pematangsiantar  •  " + java.time.LocalDate.now().toString() + "  •  Dokter Jaga: " + dokterJaga());
        tglDash.setForeground(new Color(220, 255, 250));
        tglDash.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel headKiri = new JPanel();
        headKiri.setLayout(new BoxLayout(headKiri, BoxLayout.Y_AXIS));
        headKiri.setOpaque(false);
        headKiri.add(sapaan);
        headKiri.add(tglDash);
        head.add(headKiri, BorderLayout.WEST);
        JButton bRefDash = new JButton("↻ Refresh");
        bRefDash.setBackground(Color.WHITE);
        bRefDash.setForeground(new Color(0, 150, 136));
        bRefDash.setFont(new Font("SansSerif", Font.BOLD, 12));
        JButton bBackup = new JButton("💾 Backup Semua");
        bBackup.setBackground(new java.awt.Color(63, 81, 181));
        bBackup.setForeground(java.awt.Color.WHITE);
        bBackup.addActionListener(e -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("=== PASIEN ===\n");
                for (int i = 0; i < mPasien.getRowCount(); i++) {
                    for (int j = 0; j < mPasien.getColumnCount(); j++) {
                        sb.append(mPasien.getValueAt(i, j)).append(",");
                    }
                    sb.append("\n");
                }
                sb.append("=== ANTRIAN ===\n");
                for (int i = 0; i < mAntrian.getRowCount(); i++) {
                    for (int j = 0; j < mAntrian.getColumnCount(); j++) {
                        sb.append(mAntrian.getValueAt(i, j)).append(",");
                    }
                    sb.append("\n");
                }
                sb.append("=== REKAM MEDIS ===\n");
                for (int i = 0; i < mRekam.getRowCount(); i++) {
                    for (int j = 0; j < mRekam.getColumnCount(); j++) {
                        sb.append(mRekam.getValueAt(i, j)).append(",");
                    }
                    sb.append("\n");
                }
                sb.append("=== OBAT ===\n");
                for (int i = 0; i < mObat.getRowCount(); i++) {
                    for (int j = 0; j < mObat.getColumnCount(); j++) {
                        sb.append(mObat.getValueAt(i, j)).append(",");
                    }
                    sb.append("\n");
                }
                java.nio.file.Files.write(java.nio.file.Paths.get("Backup-Klinik-" + java.time.LocalDate.now() + ".csv"), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(this, "Backup berhasil! File: Backup-Klinik-" + java.time.LocalDate.now() + ".csv");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal backup: " + ex.getMessage());
            }
        });
        bRefDash.addActionListener(e -> {
            loadPasien();
            loadAntrian();
            loadRekam();
            loadObat();
            loadDokter();
            updateDashboard();
            // OBAT STOK MERAH: kalau stok < 60, baris jadi merah muda
            tObat.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
                public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                    java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                    try {
                        int stok = (int) mObat.getValueAt(row, 2);
                        if (stok < 60 && !isSelected) {
                            c.setBackground(new java.awt.Color(255, 235, 235));
                            c.setForeground(new java.awt.Color(183, 28, 28));
                        } else if (!isSelected) {
                            c.setBackground(java.awt.Color.WHITE);
                            c.setForeground(java.awt.Color.BLACK);
                        }
                    } catch (Exception e) {
                    }
                    return c;
                }
            });
        });
        // V6: Warna Status Antrian - Menunggu kuning, Dipanggil biru, Selesai hijau
        tAntrian.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                try {
                    int modelRow = table.convertRowIndexToModel(row);
                    String st = String.valueOf(mAntrian.getValueAt(modelRow, 7));
                    if (!isSelected) {
                        if (st.equals("Menunggu")) {
                            c.setBackground(new java.awt.Color(255, 243, 205));
                            c.setForeground(new java.awt.Color(133, 100, 4));
                        } else if (st.equals("Dipanggil")) {
                            c.setBackground(new java.awt.Color(207, 232, 255));
                            c.setForeground(new java.awt.Color(13, 71, 161));
                        } else if (st.equals("Selesai")) {
                            c.setBackground(new java.awt.Color(200, 230, 201));
                            c.setForeground(new java.awt.Color(27, 94, 32));
                        } else {
                            c.setBackground(java.awt.Color.WHITE);
                            c.setForeground(java.awt.Color.BLACK);
                        }
                    }
                } catch (Exception e) {
                }
                return c;
            }
        });
        JPanel headKanan = new JPanel(new FlowLayout());
        headKanan.setOpaque(false);
        headKanan.add(bBackup);
        headKanan.add(bRefDash);
        head.add(headKanan, BorderLayout.EAST);
        pDash.add(head, BorderLayout.NORTH);
        // KARTU: 4 kartu modern
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 16));
        cards.setBackground(new Color(245, 247, 250));
        cards.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        cards.add(buatKartuPro("👥", "Total Pasien", lblTotalPasien, "Pasien terdaftar • Klik untuk lihat →", new Color(0, 150, 136), 1));
        cards.add(buatKartuPro("⏳", "Antrian Menunggu", lblAntrian, "Menunggu dipanggil • Klik untuk lihat →", new Color(255, 152, 0), 2));
        cards.add(buatKartuPro("💊", "Obat Stok Menipis", lblObat, "Stok < 60, perlu restock • Klik untuk lihat →", new Color(229, 57, 53), 4));
        cards.add(buatKartuPro("📋", "Total Rekam Medis", lblRekam, "Riwayat pemeriksaan • Klik untuk lihat →", new Color(63, 81, 181), 3));
        JPanel grafikKunjungan = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isGelap = javax.swing.UIManager.getLookAndFeel().getName().toLowerCase().contains("dark");
                g2.setColor(isGelap ? new Color(30, 30, 30) : new Color(245, 247, 250));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(isGelap ? Color.WHITE : new Color(60, 60, 60));
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString("📊  Grafik Kunjungan 7 Hari Terakhir", 16, 22);
                int[] vals = new int[7];
                String[] labels = new String[7];
                for (int d = 6; d >= 0; d--) {
                    java.time.LocalDate dt = java.time.LocalDate.now().minusDays(d);
                    labels[6 - d] = dt.toString().substring(5);
                    int c = 0;
                    for (int i = 0; i < mAntrian.getRowCount(); i++) {
                        if (String.valueOf(mAntrian.getValueAt(i, 4)).equals(dt.toString())) {
                            c++;
                        }
                    }
                    vals[6 - d] = c;
                }
                int max = 1;
                for (int v : vals) {
                    max = Math.max(max, v);
                }
                int bw = (getWidth() - 40) / 7;
                for (int i = 0; i < 7; i++) {
                    int bh = (int) ((getHeight() - 60) * vals[i] / (double) max * 0.8);
                    int x = 20 + i * bw + 10;
                    int y = getHeight() - 30 - bh;
                    g2.setColor(new Color(0, 150, 136));
                    g2.fillRoundRect(x, y, bw - 20, bh, 8, 8);
                    g2.setColor(isGelap ? Color.WHITE : new Color(60, 60, 60));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2.drawString(String.valueOf(vals[i]), x + (bw - 20) / 2 - 4, y - 6);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    g2.drawString(labels[i], x, getHeight() - 12);
                }
            }
        };
        grafikKunjungan.setPreferredSize(new Dimension(0, 140));
        grafikKunjungan.setBackground(Color.WHITE);
        JPanel dashTengah = new JPanel(new BorderLayout());
        dashTengah.add(cards, BorderLayout.NORTH);
        dashTengah.add(grafikKunjungan, BorderLayout.CENTER);
        pDash.add(dashTengah, BorderLayout.CENTER);
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickPanel.setBackground(new java.awt.Color(245, 247, 250));
        JButton bJumpPasien = new JButton("👥 Lihat Pasien");
        JButton bJumpAnt = new JButton("🎫 Lihat Antrian");
        JButton bJumpRek = new JButton("📋 Lihat Rekam Medis");
        JButton bJumpObat = new JButton("💊 Lihat Obat");
        bJumpPasien.addActionListener(evJ -> {
            tab.setSelectedIndex(1);
        });
        bJumpAnt.addActionListener(evJ2 -> {
            tab.setSelectedIndex(2);
        });
        bJumpRek.addActionListener(evJ3 -> {
            tab.setSelectedIndex(3);
        });
        bJumpObat.addActionListener(evJ4 -> {
            tab.setSelectedIndex(4);
        });
        quickPanel.add(new JLabel("Aksi Cepat:"));
        quickPanel.add(bJumpPasien);
        quickPanel.add(bJumpAnt);
        quickPanel.add(bJumpRek);
        quickPanel.add(bJumpObat);
        pDash.add(quickPanel, BorderLayout.SOUTH);
        quickPanel.add(new JLabel("   |   "));
        lblNextAntrian.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        quickPanel.add(lblNextAntrian);
        quickPanel.add(lblHariIni);
        lblJagaCard.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        lblJagaCard.setForeground(new java.awt.Color(0, 150, 136));
        quickPanel.add(new JLabel("   |   "));
        quickPanel.add(lblJagaCard);
        lblPendapatan.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        lblPendapatan.setForeground(new java.awt.Color(46, 125, 50));
        quickPanel.add(new JLabel("   |   "));
        quickPanel.add(lblPendapatan);
        lblStokWarning.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        lblStokWarning.setForeground(new java.awt.Color(183, 28, 28));
        quickPanel.add(new JLabel("   |   "));
        quickPanel.add(lblStokWarning);
        // BAWAH: Info restock + tips
        JPanel bawahDash = new JPanel(new BorderLayout());
        bawahDash.setBackground(Color.WHITE);
        bawahDash.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(60, 60, 60));
        JLabel judulInfo = new JLabel("⚠️  Perlu Perhatian Hari Ini");
        judulInfo.setFont(new Font("SansSerif", Font.BOLD, 13));
        JPanel infoBox = new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setBackground(Color.WHITE);
        infoBox.add(judulInfo);
        infoBox.add(lblInfo);
        bawahDash.add(infoBox, BorderLayout.CENTER);
        JLabel tips = new JLabel("<html><i>Tips: Tambah pasien baru, lalu kembali ke Dashboard dan klik Refresh untuk melihat angka berubah.</i></html>");
        tips.setForeground(Color.GRAY);
        tips.setFont(new Font("SansSerif", Font.ITALIC, 11));
        bawahDash.add(tips, BorderLayout.SOUTH);
        pDash.add(bawahDash, BorderLayout.SOUTH);
        tab.add("0. Dashboard", pDash);
        JPanel pPasien = new JPanel(new BorderLayout());
        JPanel f1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nNama = new JTextField(12), nNik = new JTextField(12), nTgl = new JTextField(10), nTelp = new JTextField(10), nAlergi = new JTextField(8);
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
        JComboBox<String> cbDarah = new JComboBox<>(new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"});
        f1.add(new JLabel("Nama:"));
        f1.add(nNama);
        f1.add(new JLabel("NIK:"));
        f1.add(nNik);
        f1.add(new JLabel("Tgl yyyy-mm-dd:"));
        f1.add(nTgl);
        f1.add(new JLabel("Telp:"));
        f1.add(nTelp);
        f1.add(new JLabel("Gender:"));
        f1.add(cbGender);
        f1.add(new JLabel("Darah:"));
        f1.add(cbDarah);
        f1.add(new JLabel("Alergi:"));
        f1.add(nAlergi);
        JButton bTambah = new JButton("Tambah Pasien"), bRef = new JButton("Refresh"), bHapus = new JButton("Hapus"), bEdit = new JButton("Edit Pasien");
        bEdit.setBackground(new java.awt.Color(255, 152, 0));
        bEdit.setForeground(java.awt.Color.WHITE);
        JButton bBersih = new JButton("Bersihkan Form");
        JTextField txtCari = new JTextField(12);
        JButton bCari = new JButton("Cari"), bRiwayat = new JButton("Riwayat Pasien"), bExport = new JButton("Export Excel");
        JButton bDetail = new JButton("Detail Pasien");
        bDetail.setBackground(new java.awt.Color(0, 150, 136));
        bDetail.setForeground(java.awt.Color.WHITE);
        bCari.setBackground(new java.awt.Color(0, 150, 136));
        bCari.setForeground(java.awt.Color.WHITE);
        bRiwayat.setBackground(new java.awt.Color(63, 81, 181));
        bRiwayat.setForeground(java.awt.Color.WHITE);
        bExport.setBackground(new java.awt.Color(46, 125, 50));
        bExport.setForeground(java.awt.Color.WHITE);
        // CARI: filter tabel pasien berdasarkan nama / NIK
        bCari.addActionListener(e -> {
            String key = txtCari.getText().trim().toLowerCase();
            if (key.isEmpty()) {
                loadPasien();
                return;
            }
            // simpan dulu semua, lalu filter
            loadPasien();
            for (int i = mPasien.getRowCount() - 1; i >= 0; i--) {
                String nama = String.valueOf(mPasien.getValueAt(i, 1)).toLowerCase();
                String nik = String.valueOf(mPasien.getValueAt(i, 2)).toLowerCase();
                if (!nama.contains(key) && !nik.contains(key)) {
                    mPasien.removeRow(i);
                }
            }
            if (mPasien.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Pasien '" + txtCari.getText() + "' tidak ditemukan!");
            }
        });
        // ENTER di kotak Cari = klik tombol Cari
        txtCari.addActionListener(e -> bCari.doClick());
        // V21 FIX: Cari Cepat di Dashboard - dipindah ke sini, SETELAH txtCari & bCari dibuat, jadi tidak error lagi
        JTextField txtCariDash = new JTextField(14);
        JButton bCariDash = new JButton("🔍 Cari Pasien");
        bCariDash.addActionListener(evCDash -> {
            txtCari.setText(txtCariDash.getText());
            tab.setSelectedIndex(1);
            bCari.doClick();
        });
        txtCariDash.addActionListener(evCDash2 -> bCariDash.doClick());
        headKanan.add(txtCariDash, 0);
        headKanan.add(bCariDash, 1);
        headKanan.revalidate();
        // RIWAYAT: tampilkan semua riwayat pasien yang dipilih
        bRiwayat.addActionListener(e -> {
            int row = tPasien.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih dulu pasiennya!");
                return;
            }
            String nama = String.valueOf(mPasien.getValueAt(row, 1));
            StringBuilder sb = new StringBuilder();
            sb.append("RIWAYAT PASIEN: ").append(nama).append("\n\n");
            sb.append("=== REKAM MEDIS ===\n");
            int hit = 0;
            for (int i = 0; i < mRekam.getRowCount(); i++) {
                if (String.valueOf(mRekam.getValueAt(i, 1)).equals(nama)) {
                    sb.append("- ").append(mRekam.getValueAt(i, 5)).append(" | Diagnosa: ").append(mRekam.getValueAt(i, 2)).append(" | Tindakan: ").append(mRekam.getValueAt(i, 3)).append(" | Resep: ").append(mRekam.getValueAt(i, 4)).append("\n");
                    hit++;
                }
            }
            if (hit == 0) {
                sb.append("Belum ada rekam medis.\n");
            }
            sb.append("\n=== ANTRIAN ===\n");
            hit = 0;
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 2)).equals(nama)) {
                    sb.append("- ").append(mAntrian.getValueAt(i, 4)).append(" ").append(mAntrian.getValueAt(i, 5)).append(" | Dokter: ").append(mAntrian.getValueAt(i, 3)).append(" | Keluhan: ").append(mAntrian.getValueAt(i, 6)).append(" | Status: ").append(mAntrian.getValueAt(i, 7)).append("\n");
                    hit++;
                }
            }
            if (hit == 0) {
                sb.append("Belum ada antrian.\n");
            }
            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Riwayat - " + nama, JOptionPane.INFORMATION_MESSAGE);
        });
        // EXPORT EXCEL: simpan jadi CSV yang bisa dibuka di Excel
        bExport.addActionListener(e -> {

            try {
                String fileNama = "Data-Pasien-Klinik.csv";
                StringBuilder sb = new StringBuilder();
                sb.append("ID,Nama,NIK,Tgl Lahir,Gender,Telepon,Darah,Alergi\n");
                for (int i = 0; i < mPasien.getRowCount(); i++) {
                    for (int j = 0; j < mPasien.getColumnCount(); j++) {
                        sb.append("\"").append(mPasien.getValueAt(i, j)).append("\",");
                    }
                    sb.append("\n");
                }
                java.nio.file.Files.write(java.nio.file.Paths.get(fileNama), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(this, "Berhasil export!\nFile: " + fileNama + "\nBuka di Excel, lalu Save As .xlsx kalau mau.");
                java.awt.Desktop.getDesktop().open(new java.io.File(fileNama));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal export: " + ex.getMessage());
            }
        });
        bDetail.addActionListener(ev -> {
            int row = tPasien.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih pasien dulu!");
                return;
            }
            String nama = String.valueOf(mPasien.getValueAt(row, 1));
            int jmlRekam = 0;
            for (int i = 0; i < mRekam.getRowCount(); i++) {
                if (String.valueOf(mRekam.getValueAt(i, 1)).equals(nama)) {
                    jmlRekam++;
                }
            }
            int jmlAnt = 0;
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 2)).equals(nama)) {
                    jmlAnt++;
                }
            }
            String msg = "DETAIL PASIEN\n\nNama: " + nama + "\nNIK: " + mPasien.getValueAt(row, 2) + "\nTgl Lahir: " + mPasien.getValueAt(row, 3) + "\nGender: " + mPasien.getValueAt(row, 4) + "\nTelepon: " + mPasien.getValueAt(row, 5) + "\nGol. Darah: " + mPasien.getValueAt(row, 6) + "\nAlergi: " + mPasien.getValueAt(row, 7) + "\n\nTotal Kunjungan (Antrian): " + jmlAnt + "\nTotal Rekam Medis: " + jmlRekam;
            JOptionPane.showMessageDialog(this, msg);
        });
        f1.add(bTambah);
        f1.add(bEdit);
        f1.add(bRef);
        f1.add(bHapus);
        f1.add(bBersih);
        // BARIS 2: Cari / Riwayat / Export dipindah ke baris sendiri biar tidak ketutupan
        JPanel f1b = new JPanel(new FlowLayout(FlowLayout.LEFT));
        f1b.setBackground(new java.awt.Color(240, 247, 247));
        f1b.add(new JLabel("Cari Nama / NIK:"));
        f1b.add(txtCari);
        f1b.add(bCari);
        f1b.add(bRiwayat);
        f1b.add(bExport);
        f1b.add(lblTotalTab);
        f1b.add(bDetail);
        JButton bUrut = new JButton("Urutkan A-Z");
        bUrut.addActionListener(evU -> {
            javax.swing.table.TableRowSorter<DefaultTableModel> s = new javax.swing.table.TableRowSorter<>(mPasien);
            s.setSortKeys(java.util.Collections.singletonList(new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING)));
            tPasien.setRowSorter(s);
        });
        f1b.add(bUrut);
        JButton bPasienSetia = new JButton("⭐ Pasien Setia (3x+)");
        bPasienSetia.setBackground(new Color(255, 193, 7));
        bPasienSetia.addActionListener(evPS -> {
            java.util.Map<String, Integer> hitung = new java.util.HashMap<>();
            for (int i = 0; i < mRekam.getRowCount(); i++) {
                String nm = String.valueOf(mRekam.getValueAt(i, 1));
                hitung.put(nm, hitung.getOrDefault(nm, 0) + 1);
            }
            StringBuilder sb = new StringBuilder("⭐ PASIEN SETIA (Sudah Berobat 3x atau Lebih)\n\n");
            boolean ada = false;
            for (java.util.Map.Entry<String, Integer> e : hitung.entrySet()) {
                if (e.getValue() >= 3) {
                    sb.append(e.getKey()).append(" - ").append(e.getValue()).append("x berobat\n");
                    ada = true;
                }
            }
            if (!ada) {
                sb.append("Belum ada pasien yang berobat 3x+.\nPasien terbanyak saat ini: ").append(hitung.isEmpty() ? "-" : java.util.Collections.max(hitung.entrySet(), java.util.Map.Entry.comparingByValue()).getKey());
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        });
        f1b.add(bPasienSetia);
        JButton bKartuPasien = new JButton("💳 Cetak Kartu Pasien");
        bKartuPasien.setBackground(new Color(63, 81, 181));
        bKartuPasien.setForeground(Color.WHITE);
        bKartuPasien.addActionListener(evKP -> {
            int r = tPasien.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Pilih pasien dulu!");
                return;
            }
            String nama = String.valueOf(mPasien.getValueAt(r, 1));
            JTextArea aKartu = new JTextArea();
            aKartu.setFont(new Font("Monospaced", Font.BOLD, 13));
            aKartu.setText("KARTU PASIEN - KLINIK SEHAT NUSANTARA\nPematangsiantar\n\nNama    : " + nama + "\nNIK     : " + mPasien.getValueAt(r, 2) + "\nTgl Lahir: " + mPasien.getValueAt(r, 3) + "\nGender  : " + mPasien.getValueAt(r, 4) + "\nTelepon : " + mPasien.getValueAt(r, 5) + "\nGol Darah: " + mPasien.getValueAt(r, 6) + "\nAlergi  : " + mPasien.getValueAt(r, 7) + "\n\nBerlaku seumur hidup. Bawa kartu ini\nsaat berobat. Terima kasih!");
            int pil = JOptionPane.showConfirmDialog(this, new JScrollPane(aKartu), "Kartu Pasien - Klik Yes untuk Cetak", JOptionPane.YES_NO_OPTION);
            if (pil == JOptionPane.YES_OPTION) {
                try {
                    aKartu.print();
                } catch (Exception ex) {
                }
            }
        });
        f1b.add(bKartuPasien);
        JButton bPasienBaruLama = new JButton("🆕 Pasien Baru vs Lama");
        bPasienBaruLama.setBackground(new Color(0, 150, 136));
        bPasienBaruLama.setForeground(Color.WHITE);
        bPasienBaruLama.addActionListener(evBL -> {
            java.util.Map<String, Integer> hitung = new java.util.HashMap<>();
            for (int i = 0; i < mRekam.getRowCount(); i++) {
                String nm = String.valueOf(mRekam.getValueAt(i, 1));
                hitung.put(nm, hitung.getOrDefault(nm, 0) + 1);
            }
            StringBuilder baru = new StringBuilder();
            StringBuilder lama = new StringBuilder();
            int jBaru = 0, jLama = 0;
            for (int i = 0; i < mPasien.getRowCount(); i++) {
                String nm = String.valueOf(mPasien.getValueAt(i, 1));
                int c = hitung.getOrDefault(nm, 0);
                if (c <= 1) {
                    baru.append(nm).append(" (").append(c).append("x)\n");
                    jBaru++;
                } else {
                    lama.append(nm).append(" (").append(c).append("x)\n");
                    jLama++;
                }
            }
            String msg = "🆕 PASIEN BARU vs PASIEN LAMA\n\nPasien Baru (0-1x berobat): " + jBaru + " orang\n" + baru.toString() + "\nPasien Lama (2x+ berobat): " + jLama + " orang\n" + lama.toString();
            JOptionPane.showMessageDialog(this, msg);
        });
        f1b.add(bPasienBaruLama);
        JComboBox<String> cbFilterDarah = new JComboBox<>(new String[]{"Semua Gol Darah", "O", "A", "B", "AB"});
        cbFilterDarah.addActionListener(evFD -> {
            String s = (String) cbFilterDarah.getSelectedItem();
            loadPasien();
            if (!s.equals("Semua Gol Darah")) {
                for (int i = mPasien.getRowCount() - 1; i >= 0; i--) {
                    if (!String.valueOf(mPasien.getValueAt(i, 6)).equals(s)) {
                        mPasien.removeRow(i);
                    }
                }
            }
        });
        f1b.add(new JLabel("Filter Darah:"));
        f1b.add(cbFilterDarah);
        JPanel topPasien = new JPanel(new GridLayout(2, 1));
        topPasien.add(f1);
        topPasien.add(f1b);
        pPasien.add(topPasien, BorderLayout.NORTH);
        pPasien.add(new JScrollPane(tPasien), BorderLayout.CENTER);
        bTambah.addActionListener(e -> {
            // VALIDASI - biar data tidak asal-asalan
            if (nNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong!");
                return;
            }
            if (nNik.getText().trim().length() != 16) {
                JOptionPane.showMessageDialog(this, "NIK harus 16 angka! Sekarang: " + nNik.getText().trim().length() + " angka.");
                return;
            }
            if (!nTgl.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Format tanggal harus yyyy-mm-dd, contoh: 1996-05-15");
                return;
            }
            if (nTelp.getText().trim().length() < 10) {
                JOptionPane.showMessageDialog(this, "Nomor telepon minimal 10 angka!");
                return;
            }
            String alergi = nAlergi.getText().trim().isEmpty() ? "Tidak ada" : nAlergi.getText().trim();
            for (int i = 0; i < mPasien.getRowCount(); i++) {
                if (String.valueOf(mPasien.getValueAt(i, 2)).equals(nNik.getText().trim())) {
                    JOptionPane.showMessageDialog(this, "NIK sudah terdaftar!\nPasien: " + mPasien.getValueAt(i, 1) + "\nNIK: " + mPasien.getValueAt(i, 2) + "\n\nTidak boleh 1 NIK untuk 2 pasien. Cek lagi NIK-nya!");
                    return;
                }
            }
            Response r = KlinikClient.getInstance().kirim(new Request("ADD_PASIEN", new Pasien(nNama.getText().trim(), nNik.getText().trim(), nTgl.getText().trim(), (String) cbGender.getSelectedItem(), nTelp.getText().trim(), "", (String) cbDarah.getSelectedItem(), alergi)));
            JOptionPane.showMessageDialog(this, r.message);
            loadPasien();
            // V4: setelah tambah, form otomatis kosong (tidak perlu klik Bersihkan manual)
            bBersih.doClick();
        });
        // V4: Enter di kolom Telp = langsung Tambah Pasien (kayak Cari kemarin)
        nTelp.addActionListener(e -> bTambah.doClick());
        // EDIT PASIEN: klik 2x di tabel -> isi form, lalu klik Edit untuk simpan
        bEdit.addActionListener(e -> {
            int row = tPasien.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih pasien dulu, lalu ubah di form atas, baru klik Edit!");
                return;
            }
            // Untuk versi sederhana, Edit = Hapus lama + Tambah baru dengan data form (karena server belum ada UPDATE_PASIEN)
            // Validasi sama seperti Tambah
            if (nNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong!");
                return;
            }
            KlinikClient.getInstance().kirim(new Request("DELETE_PASIEN", (int) mPasien.getValueAt(row, 0)));
            String alergi = nAlergi.getText().trim().isEmpty() ? "Tidak ada" : nAlergi.getText().trim();
            for (int i = 0; i < mPasien.getRowCount(); i++) {
                if (String.valueOf(mPasien.getValueAt(i, 2)).equals(nNik.getText().trim())) {
                    JOptionPane.showMessageDialog(this, "NIK sudah terdaftar!\nPasien: " + mPasien.getValueAt(i, 1) + "\nNIK: " + mPasien.getValueAt(i, 2) + "\n\nTidak boleh 1 NIK untuk 2 pasien. Cek lagi NIK-nya!");
                    return;
                }
            }
            Response r = KlinikClient.getInstance().kirim(new Request("ADD_PASIEN", new Pasien(nNama.getText().trim(), nNik.getText().trim(), nTgl.getText().trim(), (String) cbGender.getSelectedItem(), nTelp.getText().trim(), "", (String) cbDarah.getSelectedItem(), alergi)));
            JOptionPane.showMessageDialog(this, "Pasien berhasil diedit!");
            loadPasien();
        });
        // Klik 1x di tabel -> isi form otomatis (biar gampang Edit)
        tPasien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tPasien.getSelectedRow() >= 0) {
                int row = tPasien.getSelectedRow();
                nNama.setText(String.valueOf(mPasien.getValueAt(row, 1)));
                nNik.setText(String.valueOf(mPasien.getValueAt(row, 2)));
                nTgl.setText(String.valueOf(mPasien.getValueAt(row, 3)));
                cbGender.setSelectedItem(String.valueOf(mPasien.getValueAt(row, 4)));
                nTelp.setText(String.valueOf(mPasien.getValueAt(row, 5)));
                cbDarah.setSelectedItem(String.valueOf(mPasien.getValueAt(row, 6)));
                String al = String.valueOf(mPasien.getValueAt(row, 7));
                nAlergi.setText(al.equals("Tidak ada") ? "" : al);
            }
        });
        // Hapus Antrian + Hapus Rekam: tambahkan tombol hapus di panel Antrian/Rekam via action sudah ada? Tambah di sini untuk Antrian

        bRef.addActionListener(e -> loadPasien());
        bHapus.addActionListener(e -> {
            int row = tPasien.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih pasien dulu!");
                return;
            }
            String namaDel = String.valueOf(mPasien.getValueAt(row, 1));
            int ok = JOptionPane.showConfirmDialog(this, "Yakin hapus pasien " + namaDel + "?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            KlinikClient.getInstance().kirim(new Request("DELETE_PASIEN", (int) mPasien.getValueAt(row, 0)));
            loadPasien();
            updateDashboard();
        });
        bBersih.addActionListener(e -> {
            nNama.setText("");
            nNik.setText("");
            nTgl.setText("");
            nTelp.setText("");
            nAlergi.setText("");
            cbGender.setSelectedIndex(0);
            cbDarah.setSelectedIndex(0);
            tPasien.clearSelection();
        });
        tab.add("1. Data Pasien", pPasien);
        JPanel pAnt = new JPanel(new BorderLayout());
        JPanel f2 = new JPanel(new FlowLayout());
        JTextField aNo = new JTextField(5), aPasien = new JTextField(12), aDok = new JTextField(12), aTgl = new JTextField(10), aJam = new JTextField(5), aKel = new JTextField(12);
        f2.add(new JLabel("No:"));
        f2.add(aNo);
        f2.add(new JLabel("Pasien:"));
        f2.add(aPasien);
        f2.add(new JLabel("Dokter:"));
        f2.add(aDok);
        f2.add(new JLabel("Tgl:"));
        f2.add(aTgl);
        f2.add(new JLabel("Jam:"));
        f2.add(aJam);
        f2.add(new JLabel("Keluhan:"));
        JCheckBox cbPrioritas = new JCheckBox("⚡ Prioritas (Lansia / Anak / Darurat)");
        cbPrioritas.setBackground(new Color(255, 235, 238));
        f2.add(aKel);
        f2.add(cbPrioritas);
        JButton bAnt = new JButton("Tambah Antrian"), bPanggil = new JButton("Panggil / Dilayani"), bSelesai = new JButton("Selesai"), bRefAnt = new JButton("Refresh Antrian"), bStruk = new JButton("Cetak Struk Antrian");
        JButton bHapusAnt = new JButton("Hapus Antrian");
        bHapusAnt.setBackground(new java.awt.Color(229, 57, 53));
        bHapusAnt.setForeground(java.awt.Color.WHITE);
        JComboBox<String> cbFilterAnt = new JComboBox<>(new String[]{"Semua", "Menunggu", "Dipanggil", "Selesai"});
        JButton bHariIni = new JButton("Hari Ini");
        JButton bBookingBesok = new JButton("Booking Besok");
        bBookingBesok.setBackground(new java.awt.Color(63, 81, 181));
        bBookingBesok.setForeground(java.awt.Color.WHITE);
        bStruk.setBackground(new java.awt.Color(0, 150, 136));
        bStruk.setForeground(java.awt.Color.WHITE);
        bStruk.addActionListener(e -> {
            int row = tAntrian.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih dulu antrian yang mau dicetak struknya!");
                return;
            }
            String no = String.valueOf(mAntrian.getValueAt(row, 1)), namaA = String.valueOf(mAntrian.getValueAt(row, 2)), dokA = String.valueOf(mAntrian.getValueAt(row, 3)), tglA = String.valueOf(mAntrian.getValueAt(row, 4)), jamA = String.valueOf(mAntrian.getValueAt(row, 5)), kelA = String.valueOf(mAntrian.getValueAt(row, 6));
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>body{font-family:monospace;text-align:center;padding:20px;} .box{border:2px dashed #333;display:inline-block;padding:20px;} h2{margin:0;color:#009688;} .no{font-size:42px;font-weight:bold;} .btn{margin-top:20px;} @media print{.btn{display:none;}}</style></head><body><div class='box'><h2>KLINIK SEHAT NUSANTARA</h2><p>Jl. Merdeka No.23, Pematangsiantar<br>Telp: (0622) 123-456</p><hr><p>NOMOR ANTRIAN ANDA</p><div class='no'>" + no + "</div><hr><table style='text-align:left;margin:auto'><tr><td>Pasien</td><td>: " + namaA + "</td></tr><tr><td>Dokter</td><td>: " + dokA + "</td></tr><tr><td>Tanggal</td><td>: " + tglA + "</td></tr><tr><td>Jam</td><td>: " + jamA + "</td></tr><tr><td>Keluhan</td><td>: " + kelA + "</td></tr></table><hr><p>Silakan menunggu, nomor Anda akan dipanggil.<br>Terima kasih atas kunjungan Anda.</p><p style='font-size:11px;color:#888'>Dicetak: " + java.time.LocalDate.now() + " | dr. Faisal Mahmuda</p></div><div class='btn'><button onclick='window.print()' style='background:#009688;color:#fff;border:none;padding:10px 20px;border-radius:6px'>Cetak Struk</button></div></body></html>";
            try {
                String f = "Struk-" + no + "-" + namaA.replaceAll(" ", "_") + ".html";
                java.nio.file.Files.write(java.nio.file.Paths.get(f), html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                java.awt.Desktop.getDesktop().browse(new java.io.File(f).toURI());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal: " + ex.getMessage());
            }
        });
        f2.add(bAnt);
        f2.add(bPanggil);
        f2.add(bSelesai);
        f2.add(bRefAnt);
        f2.add(bHapusAnt);
        f2.add(new JLabel("Filter:"));
        f2.add(cbFilterAnt);
        f2.add(bHariIni);
        f2.add(bStruk);
        f2.add(bBookingBesok);
        JButton bEstimasi = new JButton("⏱️ Estimasi Tunggu");
        bEstimasi.setBackground(new Color(255, 152, 0));
        bEstimasi.setForeground(Color.WHITE);
        bEstimasi.addActionListener(evEst -> {
            int r = tAntrian.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Pilih antrian dulu!");
                return;
            }
            int mr = tAntrian.convertRowIndexToModel(r);
            String nama = String.valueOf(mAntrian.getValueAt(mr, 2));
            String no = String.valueOf(mAntrian.getValueAt(mr, 1));
            int posisi = 0;
            for (int i = 0; i <= mr; i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 7)).equals("Menunggu")) {
                    posisi++;
                }
            }
            int menit = posisi * 15;
            JOptionPane.showMessageDialog(this, "⏱️ ESTIMASI WAKTU TUNGGU\n\nPasien: " + nama + "\nNo Antrian: " + no + "\nPosisi: Ke-" + posisi + " yang menunggu\nEstimasi: " + menit + " menit lagi (" + (menit / 60) + " jam " + (menit % 60) + " menit)\nDokter: " + mAntrian.getValueAt(mr, 3) + "\n\nMohon tunggu dengan sabar ya!");
        });
        f2.add(bEstimasi);
        JButton bPanggilBerikutnya = new JButton("⏭️ Panggil Berikutnya");
        bPanggilBerikutnya.setBackground(new Color(0, 150, 136));
        bPanggilBerikutnya.setForeground(Color.WHITE);
        bPanggilBerikutnya.addActionListener(evPB -> {
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 7)).equals("Menunggu")) {
                    tAntrian.setRowSelectionInterval(i, i);
                    updateAntrian("Dipanggil");
                    JOptionPane.showMessageDialog(this, "Memanggil: " + mAntrian.getValueAt(i, 1) + " - " + mAntrian.getValueAt(i, 2) + "\nSilakan masuk ke ruang dokter!");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Tidak ada antrian Menunggu! Semua sudah Dipanggil / Selesai.");
        });
        f2.add(bPanggilBerikutnya);
        JButton bSelesaikanSemua = new JButton("✅ Selesaikan Semua (Tutup Klinik)");
        bSelesaikanSemua.setBackground(new Color(46, 125, 50));
        bSelesaikanSemua.setForeground(Color.WHITE);
        bSelesaikanSemua.addActionListener(evSS -> {
            String today = java.time.LocalDate.now().toString();
            int hitung = 0;
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 4)).equals(today) && !String.valueOf(mAntrian.getValueAt(i, 7)).equals("Selesai")) {
                    hitung++;
                }
            }
            if (hitung == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada antrian hari ini yang belum Selesai!");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this, "Selesaikan semua " + hitung + " antrian hari ini (" + today + ")?\nSemua yang Menunggu / Dipanggil jadi Selesai.", "Tutup Klinik", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 4)).equals(today) && !String.valueOf(mAntrian.getValueAt(i, 7)).equals("Selesai")) {
                    int idAnt = (int) mAntrian.getValueAt(i, 0);
                    KlinikClient.getInstance().kirim(new Request("UPDATE_ANTRIAN", new String[]{String.valueOf(idAnt), "Selesai"}));
                }
            }
            loadAntrian();
            updateDashboard();
            JOptionPane.showMessageDialog(this, "Tutup klinik berhasil! " + hitung + " antrian diselesaikan.");
        });
        f2.add(bSelesaikanSemua);
        JButton bDaftarAntrianHariIni = new JButton("🖨️ Cetak Daftar Hari Ini");
        bDaftarAntrianHariIni.setBackground(new Color(63, 81, 181));
        bDaftarAntrianHariIni.setForeground(Color.WHITE);
        bDaftarAntrianHariIni.addActionListener(evDA -> {
            String today = java.time.LocalDate.now().toString();
            StringBuilder sb = new StringBuilder("DAFTAR ANTRIAN HARI INI - KLINIK SEHAT NUSANTARA\nTanggal: " + today + "\nDokter Jaga: " + dokterJaga() + "\n\n");
            int no = 1;
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 4)).equals(today)) {
                    sb.append(no++).append(". ").append(mAntrian.getValueAt(i, 1)).append(" - ").append(mAntrian.getValueAt(i, 2)).append(" - ").append(mAntrian.getValueAt(i, 3)).append(" - ").append(mAntrian.getValueAt(i, 7)).append("\n");
                }
            }
            if (no == 1) {
                sb.append("Tidak ada antrian hari ini.");
            }
            JTextArea aDaftar = new JTextArea(sb.toString());
            aDaftar.setFont(new Font("Monospaced", Font.PLAIN, 13));
            int pil = JOptionPane.showConfirmDialog(this, new JScrollPane(aDaftar), "Daftar Antrian Hari Ini - Klik Yes untuk Cetak", JOptionPane.YES_NO_OPTION);
            if (pil == JOptionPane.YES_OPTION) {
                try {
                    aDaftar.print();
                } catch (Exception ex) {
                }
            }
        });
        f2.add(bDaftarAntrianHariIni);
        JButton bUrutPrioritas = new JButton("⚡ Prioritas Duluan");
        bUrutPrioritas.setBackground(new Color(229, 57, 53));
        bUrutPrioritas.setForeground(Color.WHITE);
        bUrutPrioritas.addActionListener(evUP -> {
            javax.swing.table.TableRowSorter<DefaultTableModel> s = new javax.swing.table.TableRowSorter<>(mAntrian);
            s.setSortKeys(java.util.Collections.singletonList(new javax.swing.RowSorter.SortKey(6, javax.swing.SortOrder.DESCENDING)));
            tAntrian.setRowSorter(s);
            JOptionPane.showMessageDialog(this, "Antrian diurutkan: [PRIORITAS] duluan di atas!");
        });
        f2.add(bUrutPrioritas);
        pAnt.add(f2, BorderLayout.NORTH);
        pAnt.add(new JScrollPane(tAntrian), BorderLayout.CENTER);
        bBookingBesok.addActionListener(evBook -> {
            aTgl.setText(java.time.LocalDate.now().plusDays(1).toString());
            JOptionPane.showMessageDialog(this, "Tanggal antrian diubah ke besok: " + aTgl.getText() + "\nTinggal isi Nama + Keluhan, lalu Tambah Antrian!");
        });
        bAnt.addActionListener(e -> {
            Response r = KlinikClient.getInstance().kirim(new Request("ADD_ANTRIAN", new Antrian(aNo.getText(), aPasien.getText(), aDok.getText(), aTgl.getText(), aJam.getText(), (cbPrioritas.isSelected() ? "[PRIORITAS] " : "") + aKel.getText(), "Menunggu")));
            JOptionPane.showMessageDialog(this, r.message);
            loadAntrian();
        });
        bPanggil.addActionListener(e -> updateAntrian("Dipanggil"));
        bSelesai.addActionListener(e -> updateAntrian("Selesai"));
        bRefAnt.addActionListener(e -> loadAntrian());
        javax.swing.table.TableRowSorter<DefaultTableModel> sorterAnt = new javax.swing.table.TableRowSorter<>(mAntrian);
        tAntrian.setRowSorter(sorterAnt);
        cbFilterAnt.addActionListener(e -> {
            String s = (String) cbFilterAnt.getSelectedItem();
            if (s.equals("Semua")) {
                sorterAnt.setRowFilter(null);
            } else {
                sorterAnt.setRowFilter(javax.swing.RowFilter.regexFilter("^" + s + "$", 7));
            }
        });
        bHariIni.addActionListener(e -> {
            aTgl.setText(java.time.LocalDate.now().toString());
            aJam.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        });
        bHapusAnt.addActionListener(e -> {
            int row = tAntrian.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih antrian dulu!");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this, "Yakin hapus antrian " + mAntrian.getValueAt(row, 1) + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            KlinikClient.getInstance().kirim(new Request("DELETE_ANTRIAN", (int) mAntrian.getValueAt(row, 0)));
            loadAntrian();
            updateDashboard();
        });
        tab.add("2. Antrian", pAnt);
        JPanel pRek = new JPanel(new BorderLayout());
        JPanel f3 = new JPanel(new FlowLayout());
        JTextField rPas = new JTextField(12), rDiag = new JTextField(12), rTind = new JTextField(10), rResep = new JTextField(10), rTgl = new JTextField(10);
        f3.add(new JLabel("Pasien:"));
        f3.add(rPas);
        f3.add(new JLabel("Diagnosa:"));
        f3.add(rDiag);
        f3.add(new JLabel("Tindakan:"));
        f3.add(rTind);
        f3.add(new JLabel("Resep:"));
        f3.add(rResep);
        f3.add(new JLabel("Tgl:"));
        f3.add(rTgl);
        JButton bRek = new JButton("Simpan Rekam Medis"), bRefRek = new JButton("Refresh"), bCetak = new JButton("Cetak / PDF Rekam Medis");
        JButton bHapusRekam = new JButton("Hapus Rekam");
        bHapusRekam.setBackground(new java.awt.Color(229, 57, 53));
        bHapusRekam.setForeground(java.awt.Color.WHITE);
        bCetak.setBackground(new java.awt.Color(0, 150, 136));
        bCetak.setForeground(java.awt.Color.WHITE);
        bCetak.addActionListener(e -> {
            int row = tRekam.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih dulu rekam medis pasien yang mau dicetak!");
                return;
            }
            String nama = String.valueOf(mRekam.getValueAt(row, 1));
            String diag = String.valueOf(mRekam.getValueAt(row, 2));
            String tind = String.valueOf(mRekam.getValueAt(row, 3));
            String resep = String.valueOf(mRekam.getValueAt(row, 4));
            String tgl = String.valueOf(mRekam.getValueAt(row, 5));
            // Cari keluhan dari Antrian kalau ada
            String keluhan = "-";
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 2)).equals(nama)) {
                    keluhan = String.valueOf(mAntrian.getValueAt(i, 6));
                    break;
                }
            }
            // Ambil data lengkap pasien dari Tab Pasien (NIK, gender, dll)
            String nik = "-", gender = "-", tglLahir = "-", telp = "-", alamat = "-", darah = "-", alergi = "-";
            for (int i = 0; i < mPasien.getRowCount(); i++) {
                if (String.valueOf(mPasien.getValueAt(i, 1)).equals(nama)) {
                    nik = String.valueOf(mPasien.getValueAt(i, 2));
                    tglLahir = String.valueOf(mPasien.getValueAt(i, 3));
                    gender = String.valueOf(mPasien.getValueAt(i, 4));
                    telp = String.valueOf(mPasien.getValueAt(i, 5));
                    darah = String.valueOf(mPasien.getValueAt(i, 6));
                    alergi = String.valueOf(mPasien.getValueAt(i, 7));
                    break;
                }
            }
            String noRM = "RM-2026-" + String.format("%04d", (int) mRekam.getValueAt(row, 0));
            String dokterNama = dokterJaga();
            // Dokter pemeriksa selalu Faisal Mahmuda, sesuai permintaan user
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>"
                    + "body{font-family:'Segoe UI',Arial,sans-serif;margin:0;padding:40px;color:#222;}"
                    + ".kop{display:flex;justify-content:space-between;border-bottom:3px solid #009688;padding-bottom:12px;}"
                    + ".kop h1{color:#009688;margin:0;font-size:26px;}.kop p{margin:2px 0;color:#555;font-size:12px;}"
                    + ".badge{background:#009688;color:#fff;padding:8px 14px;border-radius:20px;font-weight:bold;}"
                    + ".judul{text-align:center;margin:20px 0;}.judul h2{margin:0;letter-spacing:1px;}.judul p{color:#777;margin:4px;}"
                    + ".box{background:#f0f7f7;border:1px solid #b2dfdb;padding:14px;border-radius:8px;}"
                    + "table{width:100%;border-collapse:collapse;margin-top:14px;} th{background:#009688;color:#fff;text-align:left;padding:10px;} td{padding:10px;border-bottom:1px solid #ddd;}"
                    + ".warn{background:#fff3e0;border-left:4px solid #ff9800;padding:10px;margin-top:14px;}"
                    + ".ttd{display:flex;justify-content:space-between;margin-top:40px;}"
                    + ".stempel{border:2px solid #009688;color:#009688;border-radius:50%;width:110px;height:110px;display:flex;align-items:center;justify-content:center;text-align:center;font-weight:bold;transform:rotate(-8deg);}"
                    + ".footer{margin-top:30px;border-top:1px solid #ddd;padding-top:10px;font-size:11px;color:#888;text-align:center;}"
                    + ".btn{margin:20px 0;text-align:center;} @media print{.btn{display:none;}}"
                    + "</style></head><body>"
                    + "<div class='kop'><div><h1>🏥 KLINIK SEHAT NUSANTARA</h1><p>Jl. Merdeka No.23, Pematangsiantar, Sumatera Utara 21111</p><p>Telp: (0622) 123-456 | Email: klinik@sehatnusantara.id | Izin: 445/123/KES/2026</p></div><div><span class='badge'>REKAM MEDIS</span></div></div>"
                    + "<div class='judul'><h2>SURAT KETERANGAN REKAM MEDIS</h2><p>Nomor: " + noRM + " / KSN / XI / 2026 &nbsp;|&nbsp; Tanggal Cetak: " + tgl + "</p></div>"
                    + "<div class='box'><b>DATA PASIEN</b><table><tr><td width='25%'>Nama Lengkap</td><td><b>" + nama + "</b></td><td width='25%'>NIK</td><td>" + nik + "</td></tr>"
                    + "<tr><td>Tanggal Lahir</td><td>" + tglLahir + "</td><td>Jenis Kelamin</td><td>" + gender + "</td></tr>"
                    + "<tr><td>Telepon</td><td>" + telp + "</td><td>Golongan Darah</td><td><b>" + darah + "</b></td></tr>"
                    + "<tr><td>Alergi</td><td colspan='3'><b style='color:#d32f2f'>" + alergi + "</b></td></tr></table></div>"
                    + "<h3>HASIL PEMERIKSAAN</h3><table><tr><th style='width:30%'>Item</th><th>Detail</th></tr>"
                    + "<tr><td><b>Keluhan Utama</b></td><td>" + keluhan + "</td></tr>"
                    + "<tr><td><b>Diagnosa / Penyakit</b></td><td><b>" + diag + "</b></td></tr>"
                    + "<tr><td><b>Tindakan Medis</b></td><td>" + tind + "</td></tr>"
                    + "<tr><td><b>Resep / Obat</b></td><td>" + resep + "</td></tr>"
                    + "<tr><td><b>Dokter Pemeriksa</b></td><td>" + dokterNama + "</td></tr>"
                    + "<tr><td><b>Tanggal Periksa</b></td><td>" + tgl + "</td></tr></table>"
                    + "<div class='warn'><b>⚠️ Catatan Dokter:</b> Pasien dianjurkan istirahat cukup, minum obat sesuai resep, kontrol ulang jika keluhan tidak membaik dalam 3 hari, dan segera ke IGD jika demam tinggi / sesak napas.</div>"
                    + "<div class='ttd'><div><p>Pematangsiantar, " + tgl + "</p><p>Pasien / Keluarga</p><br><br><p><b>( " + nama + " )</b></p></div>"
                    + "<div style='text-align:center'><p>Dokter Penanggung Jawab</p><div class='stempel'>KLINIK<br>SEHAT<br>NUSANTARA</div><p><b>( " + dokterNama + " )</b><br>SIP: 4451234567</p></div></div>"
                    + "<div class='footer'>Dokumen ini dicetak dari Aplikasi Klinik Client-Server (Java NetBeans + MySQL) | Data bersifat rahasia, hanya untuk keperluan medis | Halaman 1 dari 1</div>"
                    + "<div class='btn'><button onclick='window.print()' style='background:#009688;color:#fff;border:none;padding:12px 24px;border-radius:6px;font-size:14px;cursor:pointer'>🖨️ Cetak / Save as PDF</button></div>"
                    + "</body></html>";
            try {
                String fileNama = "RekamMedis-" + nama.replaceAll(" ", "_") + ".html";
                java.nio.file.Files.write(java.nio.file.Paths.get(fileNama), html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                java.awt.Desktop.getDesktop().browse(new java.io.File(fileNama).toURI());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal cetak: " + ex.getMessage());
            }
        });
        JButton bSuratSakit = new JButton("📄 Surat Sakit");
        bSuratSakit.setBackground(new Color(255, 152, 0));
        bSuratSakit.setForeground(Color.WHITE);
        bSuratSakit.addActionListener(evSS -> {
            int r = tRekam.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Pilih rekam medis dulu!");
                return;
            }
            int mr = tRekam.convertRowIndexToModel(r);
            String nama = String.valueOf(mRekam.getValueAt(mr, 1));
            String diag = String.valueOf(mRekam.getValueAt(mr, 2));
            JTextArea aSurat = new JTextArea();
            aSurat.setFont(new Font("Monospaced", Font.PLAIN, 13));
            aSurat.setText("SURAT KETERANGAN SAKIT\nKLINIK SEHAT NUSANTARA - Pematangsiantar\n\nYang bertanda tangan di bawah ini, " + dokterJaga() + ", menerangkan bahwa:\n\nNama Pasien : " + nama + "\nDiagnosa    : " + diag + "\nTanggal     : " + java.time.LocalDate.now() + "\n\nPasien tersebut perlu istirahat selama 2 (dua) hari\nterhitung sejak tanggal surat ini dikeluarkan.\n\nPematangsiantar, " + java.time.LocalDate.now() + "\n\n\n" + dokterJaga() + "\nDokter Penanggung Jawab");
            int pil = JOptionPane.showConfirmDialog(this, new JScrollPane(aSurat), "Surat Sakit - Klik Yes untuk Cetak", JOptionPane.YES_NO_OPTION);
            if (pil == JOptionPane.YES_OPTION) {
                try {
                    aSurat.print();
                } catch (Exception ex) {
                }
            }
        });
        JButton bSuratRujukan = new JButton("📄 Surat Rujukan");
        bSuratRujukan.setBackground(new Color(63, 81, 181));
        bSuratRujukan.setForeground(Color.WHITE);
        bSuratRujukan.addActionListener(evSR -> {
            int r = tRekam.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Pilih rekam medis dulu!");
                return;
            }
            int mr = tRekam.convertRowIndexToModel(r);
            String nama = String.valueOf(mRekam.getValueAt(mr, 1));
            String diag = String.valueOf(mRekam.getValueAt(mr, 2));
            JTextArea aRujuk = new JTextArea();
            aRujuk.setFont(new Font("Monospaced", Font.PLAIN, 13));
            aRujuk.setText("SURAT RUJUKAN\nKLINIK SEHAT NUSANTARA - Pematangsiantar\n\nKepada Yth. RSUD Djasamen Saragih, Pematangsiantar\n\nMohon pemeriksaan lanjutan untuk:\nNama Pasien : " + nama + "\nDiagnosa    : " + diag + "\nTanggal     : " + java.time.LocalDate.now() + "\nDokter      : " + dokterJaga() + "\n\nAtas perhatian dan kerjasamanya, kami ucapkan terima kasih.");
            int pil = JOptionPane.showConfirmDialog(this, new JScrollPane(aRujuk), "Surat Rujukan - Klik Yes untuk Cetak", JOptionPane.YES_NO_OPTION);
            if (pil == JOptionPane.YES_OPTION) {
                try {
                    aRujuk.print();
                } catch (Exception ex) {
                }
            }
        });
        f3.add(bRek);
        f3.add(bRefRek);
        f3.add(bHapusRekam);
        f3.add(bCetak);
        f3.add(bSuratSakit);
        f3.add(bSuratRujukan);
        JButton bKontrol = new JButton("📅 Jadwal Kontrol");
        bKontrol.setBackground(new Color(0, 150, 136));
        bKontrol.setForeground(Color.WHITE);
        bKontrol.addActionListener(evK -> {
            int r = tRekam.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Pilih rekam medis dulu!");
                return;
            }
            int mr = tRekam.convertRowIndexToModel(r);
            String nama = String.valueOf(mRekam.getValueAt(mr, 1));
            String tglKontrol = java.time.LocalDate.now().plusDays(7).toString();
            JOptionPane.showMessageDialog(this, "📅 JADWAL KONTROL\n\nPasien: " + nama + "\nKontrol Berikutnya: " + tglKontrol + " (7 hari lagi)\nDokter: " + dokterJaga() + "\n\nJangan lupa datang lagi ya!");
        });
        f3.add(bKontrol);
        JButton bExportRekam = new JButton("📤 Export Rekam CSV");
        bExportRekam.setBackground(new Color(63, 81, 181));
        bExportRekam.setForeground(Color.WHITE);
        bExportRekam.addActionListener(evER -> {
            try {
                String f = "RekamMedis-" + java.time.LocalDate.now() + ".csv";
                java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(f));
                pw.println("ID,Nama,Diagnosa,Tindakan,Resep,Tanggal");
                for (int i = 0; i < mRekam.getRowCount(); i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < mRekam.getColumnCount(); j++) {
                        sb.append(String.valueOf(mRekam.getValueAt(i, j)).replace(",", " ")).append(",");
                    }
                    pw.println(sb.toString());
                }
                pw.close();
                JOptionPane.showMessageDialog(this, "Export Rekam berhasil! File: " + f + "\nTotal: " + mRekam.getRowCount() + " rekam");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal export: " + ex.getMessage());
            }
        });
        f3.add(bExportRekam);
        pRek.add(f3, BorderLayout.NORTH);
        pRek.add(new JScrollPane(tRekam), BorderLayout.CENTER);
        JTextField txtCariRekam = new JTextField(12);
        JButton bCariRekam = new JButton("Cari Rekam");
        bCariRekam.addActionListener(evCR -> {
            String key = txtCariRekam.getText().trim().toLowerCase();
            if (key.isEmpty()) {
                loadRekam();
                return;
            }
            loadRekam();
            for (int i = mRekam.getRowCount() - 1; i >= 0; i--) {
                if (!String.valueOf(mRekam.getValueAt(i, 1)).toLowerCase().contains(key) && !String.valueOf(mRekam.getValueAt(i, 2)).toLowerCase().contains(key)) {
                    mRekam.removeRow(i);
                }
            }
        });
        txtCariRekam.addActionListener(evCR2 -> bCariRekam.doClick());
        f3.add(new JLabel("Cari Pasien / Diagnosa:"));
        f3.add(txtCariRekam);
        f3.add(bCariRekam);
        bRek.addActionListener(e -> {
            Response r = KlinikClient.getInstance().kirim(new Request("ADD_REKAM", new RekamMedis(rPas.getText(), rDiag.getText(), rTind.getText(), rResep.getText(), rTgl.getText())));
            JOptionPane.showMessageDialog(this, r.message);
            loadRekam();
        });
        bRefRek.addActionListener(e -> loadRekam());
        bHapusRekam.addActionListener(e -> {
            int row = tRekam.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih rekam medis dulu!");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this, "Yakin hapus rekam medis " + mRekam.getValueAt(row, 1) + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            KlinikClient.getInstance().kirim(new Request("DELETE_REKAM", (int) mRekam.getValueAt(row, 0)));
            loadRekam();
            updateDashboard();
        });
        tab.add("3. Rekam Medis", pRek);
        JPanel pObat = new JPanel(new BorderLayout());
        JButton bRefObat = new JButton("Refresh Obat");
        JTextField txtCariObat = new JTextField(14);
        JButton bCariObat = new JButton("Cari Obat");
        bCariObat.setBackground(new java.awt.Color(0, 150, 136));
        bCariObat.setForeground(java.awt.Color.WHITE);
        JPanel topObat = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topObat.add(bRefObat);
        topObat.add(new JLabel("Cari Obat:"));
        topObat.add(txtCariObat);
        topObat.add(bCariObat);
        JButton bCekObat = new JButton("⚠️ Cek Stok Kritis");
        bCekObat.setBackground(new Color(229, 57, 53));
        bCekObat.setForeground(Color.WHITE);
        bCekObat.addActionListener(evCO -> {
            StringBuilder sb = new StringBuilder("⚠️ OBAT STOK KRITIS (Stok < 60)\n\n");
            boolean ada = false;
            for (int i = 0; i < mObat.getRowCount(); i++) {
                int stok = (int) mObat.getValueAt(i, 2);
                if (stok < 60) {
                    sb.append(mObat.getValueAt(i, 1)).append(" - Stok: ").append(stok).append(" - Harga: Rp ").append(mObat.getValueAt(i, 3)).append("\n");
                    ada = true;
                }
            }
            if (!ada) {
                sb.append("Semua stok aman! Tidak ada yang < 60.\n");
            }
            sb.append("\nTotal Obat: ").append(mObat.getRowCount());
            JOptionPane.showMessageDialog(this, sb.toString());
        });
        topObat.add(bCekObat);
        JButton bRestock = new JButton("📦 Restock +50");
        bRestock.setBackground(new Color(46, 125, 50));
        bRestock.setForeground(Color.WHITE);
        bRestock.addActionListener(evRS -> {
            int r = tObat.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Pilih obat dulu yang mau di-restock!");
                return;
            }
            int mr = tObat.convertRowIndexToModel(r);
            int idObat = (int) mObat.getValueAt(mr, 0);
            String namaObat = String.valueOf(mObat.getValueAt(mr, 1));
            int stokLama = (int) mObat.getValueAt(mr, 2);
            int hargaObat = (int) mObat.getValueAt(mr, 3);
            String inp = JOptionPane.showInputDialog(this, "Restock " + namaObat + "\nStok sekarang: " + stokLama + "\nMau tambah berapa? (contoh: 50)", "50");
            if (inp == null) {
                return;
            }
            try {
                int tambah = Integer.parseInt(inp.trim());
                if (tambah <= 0) {
                    JOptionPane.showMessageDialog(this, "Jumlah restock harus > 0!");
                    return;
                }
                int stokBaru = stokLama + tambah;
                int ok = JOptionPane.showConfirmDialog(this, "Restock " + namaObat + "\n" + stokLama + " → " + stokBaru + " ( +" + tambah + " )\nLanjutkan?", "Konfirmasi Restock", JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) {
                    return;
                }
                KlinikClient.getInstance().kirim(new Request("DELETE_OBAT", idObat));
                KlinikClient.getInstance().kirim(new Request("ADD_OBAT", new Obat(namaObat, stokBaru, hargaObat)));
                JOptionPane.showMessageDialog(this, "Restock berhasil! " + namaObat + " : " + stokLama + " → " + stokBaru);
                loadObat();
                updateDashboard();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Jumlah harus angka! Contoh: 50");
            }
        });
        topObat.add(bRestock);
        JButton bNilaiStok = new JButton("💰 Nilai Total Stok");
        bNilaiStok.setBackground(new Color(103, 58, 183));
        bNilaiStok.setForeground(Color.WHITE);
        bNilaiStok.addActionListener(evNS -> {
            long total = 0;
            for (int i = 0; i < mObat.getRowCount(); i++) {
                int stok = (int) mObat.getValueAt(i, 2);
                int harga = (int) mObat.getValueAt(i, 3);
                total += (long) stok * harga;
            }
            JOptionPane.showMessageDialog(this, "💰 NILAI TOTAL STOK OBAT\n\nTotal Jenis Obat: " + mObat.getRowCount() + "\nTotal Nilai Stok: Rp " + total + "\n\nIni total nilai obat di gudang kalau\nsemua dijual (stok x harga).");
        });
        topObat.add(bNilaiStok);
        pObat.add(topObat, BorderLayout.NORTH);
        pObat.add(new JScrollPane(tObat), BorderLayout.CENTER);
        JTextField txtObatNama = new JTextField(12), txtObatStok = new JTextField(5), txtObatHarga = new JTextField(7);
        JButton bTambahObat = new JButton("Tambah Obat");
        bTambahObat.setBackground(new java.awt.Color(46, 125, 50));
        bTambahObat.setForeground(java.awt.Color.WHITE);
        topObat.add(new JLabel("Nama Obat:"));
        topObat.add(txtObatNama);
        topObat.add(new JLabel("Stok:"));
        topObat.add(txtObatStok);
        topObat.add(new JLabel("Harga:"));
        topObat.add(txtObatHarga);
        topObat.add(bTambahObat);
        JButton bHapusObat = new JButton("Hapus Obat");
        bHapusObat.setBackground(new java.awt.Color(229, 57, 53));
        bHapusObat.setForeground(java.awt.Color.WHITE);
        topObat.add(bHapusObat);
        bCariObat.addActionListener(e -> {
            String key = txtCariObat.getText().trim().toLowerCase();
            if (key.isEmpty()) {
                loadObat();
                return;
            }
            loadObat();
            for (int i = mObat.getRowCount() - 1; i >= 0; i--) {
                if (!String.valueOf(mObat.getValueAt(i, 1)).toLowerCase().contains(key)) {
                    mObat.removeRow(i);
                }
            }
        });
        bTambahObat.addActionListener(evObat -> {
            try {
                if (txtObatNama.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nama obat tidak boleh kosong!");
                    return;
                }
                int stok = Integer.parseInt(txtObatStok.getText().trim());
                int harga = Integer.parseInt(txtObatHarga.getText().trim());
                if (stok < 0 || harga < 0) {
                    JOptionPane.showMessageDialog(this, "Stok dan Harga tidak boleh negatif!");
                    return;
                }
                Response r = KlinikClient.getInstance().kirim(new Request("ADD_OBAT", new Obat(txtObatNama.getText().trim(), stok, harga)));
                JOptionPane.showMessageDialog(this, r.message);
                loadObat();
                updateDashboard();
                txtObatNama.setText("");
                txtObatStok.setText("");
                txtObatHarga.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Stok dan Harga harus angka! Contoh: 100, 5000");
            }
        });
        bHapusObat.addActionListener(evHO -> {
            int row = tObat.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih obat dulu!");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this, "Yakin hapus obat " + mObat.getValueAt(row, 1) + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            KlinikClient.getInstance().kirim(new Request("DELETE_OBAT", (int) mObat.getValueAt(row, 0)));
            loadObat();
            updateDashboard();
        });
        txtCariObat.addActionListener(e -> bCariObat.doClick());
        bRefObat.addActionListener(e -> loadObat());
        tab.add("4. Data Obat", pObat);
        // TAHAP 7: HAK AKSES
        if (role.equals("Dokter")) {
            bHapus.setEnabled(false);
            bAnt.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Login sebagai Dokter: Hapus Pasien & Tambah Antrian dinonaktifkan");
        }
        if (role.equals("Resepsionis")) {
            bRek.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Login sebagai Resepsionis: Rekam Medis hanya bisa dilihat");
        }
        // TAHAP 7: LAPORAN

        // FITUR LOGOUT
        setLayout(new java.awt.BorderLayout());
        JPanel topBar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JLabel lblJam = new JLabel();
        lblJam.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 13));
        new javax.swing.Timer(1000, evJam -> {
            lblJam.setText("🕐 " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "  |  " + java.time.LocalDate.now().toString() + "  ");
        }).start();
        topBar.add(lblJam);
        topBar.add(new JLabel("Login sebagai: " + role + "  "));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            int pilih = JOptionPane.showConfirmDialog(this, "Yakin mau logout dan ganti user?", "Logout", JOptionPane.YES_NO_OPTION);
            if (pilih == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        JButton btnModeMalam = new JButton("🌙 Mode Malam");
        btnModeMalam.addActionListener(evMode -> {
            try {
                boolean gelap = btnModeMalam.getText().contains("Malam");
                if (gelap) {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                    btnModeMalam.setText("☀️ Mode Terang");
                } else {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                    btnModeMalam.setText("🌙 Mode Malam");
                }
                javax.swing.SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
            }
        });
        topBar.add(btnModeMalam);
        JButton btnPanduan = new JButton("❓ Panduan");
        btnPanduan.addActionListener(evPan -> {
            String panduan = "❓ PANDUAN APLIKASI - KLINIK SEHAT NUSANTARA\n\n1. Dashboard: Lihat Total Pasien, Antrian, Obat, Grafik 7 Hari. Klik kartu untuk lompat.\n2. Data Pasien: Tambah pasien (NIK 16 angka, tidak boleh duplikat), Edit, Hapus, Detail, Kartu Pasien.\n3. Antrian: Tambah antrian, Centang Prioritas untuk Lansia/Darurat, Panggil Berikutnya, Estimasi Tunggu.\n4. Rekam Medis: Simpan diagnosa, Surat Sakit, Surat Rujukan, Jadwal Kontrol, Export CSV.\n5. Data Obat: Cek Stok Kritis, Restock, Nilai Total Stok.\n6. Laporan: Laporan Bulanan, Pendapatan Hari Ini, Top 3 Penyakit.\n7. Data Dokter: Jadwal Mingguan, Pasien per Dokter, Filter Aktif.\n8. Kasir: Pilih pasien dari Antrian, Layanan, Diskon 10%, Metode Tunai/QRIS/Transfer, Enter = Hitung.\n\nRole: admin (semua), faisal (dokter), clarensia (resepsionis), armina (perawat), evi (kasir).\nSesi auto-logout setelah 15 menit tidak dipakai.";
            JTextArea aPan = new JTextArea(panduan);
            aPan.setEditable(false);
            aPan.setFont(new Font("SansSerif", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(aPan), "Panduan", JOptionPane.INFORMATION_MESSAGE);
        });
        topBar.add(btnPanduan);
        /* V44: Reset Demo DIHAPUS - diganti Arsip, biar data tidak hilang */ topBar.add(btnLogout);
        // A: Session Timeout 15 menit - kalau tidak ada aktivitas mouse/keyboard, auto logout
        final long[] lastActivity = new long[]{System.currentTimeMillis()};
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(evIdle -> {
            if (evIdle instanceof java.awt.event.MouseEvent || evIdle instanceof java.awt.event.KeyEvent) {
                lastActivity[0] = System.currentTimeMillis();
            }
        }, java.awt.AWTEvent.KEY_EVENT_MASK | java.awt.AWTEvent.MOUSE_EVENT_MASK | java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK);
        javax.swing.Timer timerSesi = new javax.swing.Timer(60000, evSesi -> {
            if (System.currentTimeMillis() - lastActivity[0] > 15 * 60 * 1000) {
                ((javax.swing.Timer) evSesi.getSource()).stop();
                JOptionPane.showMessageDialog(this, "Sesi habis! 15 menit tidak ada aktivitas.\nSilakan login lagi untuk keamanan.");
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        timerSesi.start();
        add(topBar, java.awt.BorderLayout.NORTH);
        // ===== V7: TAB LAPORAN =====
        JPanel pLap = new JPanel(new BorderLayout());
        JTextArea areaLap = new JTextArea();
        areaLap.setEditable(false);
        areaLap.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
        JButton bLap = new JButton("Tampilkan Laporan");
        bLap.setBackground(new java.awt.Color(63, 81, 181));
        bLap.setForeground(java.awt.Color.WHITE);
        bLap.addActionListener(evLap -> {
            StringBuilder sb = new StringBuilder();
            sb.append("LAPORAN KLINIK SEHAT NUSANTARA\n");
            sb.append("Tanggal: ").append(java.time.LocalDate.now()).append("\n");
            sb.append("Dokter Jaga: ").append(dokterJaga()).append(" (Rotasi Harian, SIP: 4451234567)\n\n");
            int laki = 0, per = 0;
            for (int i = 0; i < mPasien.getRowCount(); i++) {
                if (String.valueOf(mPasien.getValueAt(i, 4)).equals("Laki-laki")) {
                    laki++;
                } else {
                    per++;
                }
            }
            sb.append("Total Pasien: ").append(mPasien.getRowCount()).append(" (Laki-laki: ").append(laki).append(", Perempuan: ").append(per).append(")\n");
            int men = 0, dip = 0, sel = 0;
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                String s = String.valueOf(mAntrian.getValueAt(i, 7));
                if (s.equals("Menunggu")) {
                    men++;
                } else if (s.equals("Dipanggil")) {
                    dip++;
                } else {
                    sel++;
                }
            }
            sb.append("Total Antrian: ").append(mAntrian.getRowCount()).append(" (Menunggu: ").append(men).append(", Dipanggil: ").append(dip).append(", Selesai: ").append(sel).append(")\n");
            sb.append("Total Rekam Medis: ").append(mRekam.getRowCount()).append("\n");
            sb.append("Total Dokter: ").append(mDokter.getRowCount()).append("\n");
            long totalNilai = 0;
            StringBuilder restock = new StringBuilder();
            for (int i = 0; i < mObat.getRowCount(); i++) {
                int stok = (int) mObat.getValueAt(i, 2);
                int harga = 0;
                try {
                    harga = Integer.parseInt(String.valueOf(mObat.getValueAt(i, 3)));
                } catch (Exception ex) {
                }
                totalNilai += (long) stok * harga;
                if (stok < 60) {
                    restock.append(mObat.getValueAt(i, 1)).append(" (").append(stok).append("), ");
                }
            }
            sb.append("Total Nilai Stok Obat: Rp ").append(totalNilai).append("\n");
            sb.append("Obat Perlu Restock: ").append(restock.length() > 0 ? restock.toString() : "Tidak ada - stok aman").append("\n");
            areaLap.setText(sb.toString());
        });
        JPanel topLap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topLap.add(bLap);
        JButton bCetakLaporan = new JButton("🖨️ Cetak Laporan");
        bCetakLaporan.setBackground(new java.awt.Color(0, 150, 136));
        bCetakLaporan.setForeground(java.awt.Color.WHITE);
        bCetakLaporan.addActionListener(evCetak -> {
            try {
                if (areaLap.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Klik Tampilkan Laporan dulu!");
                    return;
                }
                boolean ok = areaLap.print();
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Laporan terkirim ke printer / PDF!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal cetak: " + ex.getMessage());
            }
        });
        topLap.add(bCetakLaporan);
        JButton bExportLap = new JButton("Export Laporan ke File");
        bExportLap.setBackground(new java.awt.Color(46, 125, 50));
        bExportLap.setForeground(java.awt.Color.WHITE);
        bExportLap.addActionListener(evExp -> {
            try {
                if (areaLap.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Klik Tampilkan Laporan dulu!");
                    return;
                }
                String fn = "Laporan-Klinik-" + java.time.LocalDate.now() + ".txt";
                java.nio.file.Files.write(java.nio.file.Paths.get(fn), areaLap.getText().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(this, "Laporan disimpan: " + fn);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal: " + ex.getMessage());
            }
        });
        topLap.add(bExportLap);
        pLap.add(topLap, BorderLayout.NORTH);
        pLap.add(new JScrollPane(areaLap), BorderLayout.CENTER);
        JButton bExportExcel = new JButton("📊 Export ke Excel (CSV)");
        bExportExcel.setBackground(new java.awt.Color(46, 125, 50));
        bExportExcel.setForeground(java.awt.Color.WHITE);
        bExportExcel.addActionListener(evExcel -> {
            try {
                if (areaLap.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Klik Tampilkan Laporan dulu!");
                    return;
                }
                String fn = "Laporan-Klinik-" + java.time.LocalDate.now() + ".csv";
                java.nio.file.Files.write(java.nio.file.Paths.get(fn), areaLap.getText().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(this, "Laporan disimpan: " + fn + "\nBuka di Excel, lalu Save As .xlsx kalau mau.");
                java.awt.Desktop.getDesktop().open(new java.io.File(fn));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal: " + ex.getMessage());
            }
        });
        topLap.add(bExportExcel);
        JButton bLaporanBulanan = new JButton("📅 Laporan Bulanan");
        bLaporanBulanan.setBackground(new Color(255, 152, 0));
        bLaporanBulanan.setForeground(Color.WHITE);
        bLaporanBulanan.addActionListener(evLB -> {
            String bln = java.time.LocalDate.now().toString().substring(0, 7);
            int totAnt = 0, totSelesai = 0;
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 4)).startsWith(bln)) {
                    totAnt++;
                    if (String.valueOf(mAntrian.getValueAt(i, 7)).equals("Selesai")) {
                        totSelesai++;
                    }
                }
            }
            int totRekamBulan = 0;
            for (int i = 0; i < mRekam.getRowCount(); i++) {
                if (String.valueOf(mRekam.getValueAt(i, 5)).startsWith(bln)) {
                    totRekamBulan++;
                }
            }
            areaLap.setText("LAPORAN BULANAN - KLINIK SEHAT NUSANTARA\nBulan: " + bln + "\nTanggal Laporan: " + java.time.LocalDate.now() + "\nDokter Jaga Hari Ini: " + dokterJaga() + "\n\nTotal Pasien Terdaftar: " + mPasien.getRowCount() + "\nTotal Antrian Bulan Ini: " + totAnt + "\nTotal Selesai Bulan Ini: " + totSelesai + "\nTotal Rekam Medis Bulan Ini: " + totRekamBulan + "\nPerkiraan Pendapatan Bulan Ini: Rp " + (totSelesai * 50000) + " (" + totSelesai + " pasien Selesai x Rp 50.000)\n\nDokter Jaga Rotasi: " + dokterJaga());
            JOptionPane.showMessageDialog(this, "Laporan Bulanan " + bln + " sudah tampil di bawah!");
        });
        topLap.add(bLaporanBulanan);
        JButton bTopPenyakit = new JButton("🏆 Top 3 Penyakit");
        bTopPenyakit.setBackground(new Color(103, 58, 183));
        bTopPenyakit.setForeground(Color.WHITE);
        bTopPenyakit.addActionListener(evTP -> {
            java.util.Map<String, Integer> hitung = new java.util.HashMap<>();
            for (int i = 0; i < mRekam.getRowCount(); i++) {
                String d = String.valueOf(mRekam.getValueAt(i, 2));
                hitung.put(d, hitung.getOrDefault(d, 0) + 1);
            }
            java.util.List<java.util.Map.Entry<String, Integer>> list = new java.util.ArrayList<>(hitung.entrySet());
            list.sort((a, b) -> b.getValue() - a.getValue());
            StringBuilder sb = new StringBuilder("🏆 TOP 3 PENYAKIT TERBANYAK\n\n");
            if (list.isEmpty()) {
                sb.append("Belum ada data rekam medis.");
            } else {
                for (int i = 0; i < Math.min(3, list.size()); i++) {
                    sb.append((i + 1) + ". " + list.get(i).getKey() + " - " + list.get(i).getValue() + " pasien\n");
                }
                sb.append("\nTotal Diagnosa Berbeda: ").append(list.size());
            }
            areaLap.setText(sb.toString());
        });
        topLap.add(bTopPenyakit);
        JButton bPendapatanHariIni = new JButton("💰 Pendapatan Hari Ini");
        bPendapatanHariIni.setBackground(new Color(46, 125, 50));
        bPendapatanHariIni.setForeground(Color.WHITE);
        bPendapatanHariIni.addActionListener(evPH -> {
            String today = java.time.LocalDate.now().toString();
            int selesaiHariIni = 0;
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 4)).equals(today) && String.valueOf(mAntrian.getValueAt(i, 7)).equals("Selesai")) {
                    selesaiHariIni++;
                }
            }
            int pendapatan = selesaiHariIni * 50000;
            areaLap.setText("PENDAPATAN HARI INI - KLINIK SEHAT NUSANTARA\nTanggal: " + today + "\nDokter Jaga: " + dokterJaga() + "\n\nPasien Selesai Hari Ini: " + selesaiHariIni + "\nTarif per Pasien: Rp 50.000\nTotal Pendapatan Hari Ini: Rp " + pendapatan + "\n\nLaporan untuk Kasir (Evi) - Tutup Klinik");
            JOptionPane.showMessageDialog(this, "Pendapatan Hari Ini: Rp " + pendapatan + " (" + selesaiHariIni + " pasien)");
        });
        topLap.add(bPendapatanHariIni);
        // SUPER DUPER: Grafik Batang - Antrian + Gender
        JPanel grafikPanel = new JPanel() {
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int men = 0, dip = 0, sel = 0;
                for (int i = 0; i < mAntrian.getRowCount(); i++) {
                    String s = String.valueOf(mAntrian.getValueAt(i, 7));
                    if (s.equals("Menunggu")) {
                        men++;
                    } else if (s.equals("Dipanggil")) {
                        dip++;
                    } else {
                        sel++;
                    }
                }
                int laki = 0, per = 0;
                for (int i = 0; i < mPasien.getRowCount(); i++) {
                    if (String.valueOf(mPasien.getValueAt(i, 4)).equals("Laki-laki")) {
                        laki++;
                    } else {
                        per++;
                    }
                }
                int max = Math.max(1, Math.max(Math.max(men, dip), Math.max(sel, Math.max(laki, per))));
                String[] labels = {"Menunggu", "Dipanggil", "Selesai", "Laki-laki", "Perempuan"};
                int[] vals = {men, dip, sel, laki, per};
                java.awt.Color[] cols = {new java.awt.Color(255, 152, 0), new java.awt.Color(33, 150, 243), new java.awt.Color(76, 175, 80), new java.awt.Color(63, 81, 181), new java.awt.Color(233, 30, 99)};
                g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                g2.setColor(java.awt.Color.DARK_GRAY);
                g2.drawString("GRAFIK KLINIK - Antrian & Pasien", 10, 20);
                for (int i = 0; i < vals.length; i++) {
                    int h = (int) (vals[i] * 80.0 / max);
                    int x = 20 + i * 110;
                    g2.setColor(cols[i]);
                    g2.fillRoundRect(x, 110 - h, 60, h, 8, 8);
                    g2.setColor(java.awt.Color.BLACK);
                    g2.drawString(labels[i], x, 130);
                    g2.drawString(String.valueOf(vals[i]), x + 20, 105 - h);
                }
            }
        };
        grafikPanel.setPreferredSize(new java.awt.Dimension(600, 150));
        grafikPanel.setBackground(java.awt.Color.WHITE);
        pLap.add(grafikPanel, BorderLayout.SOUTH);
        tab.add("5. Laporan", pLap);
        // ===== V7: HAK AKSES PER ROLE =====
        if (role.equalsIgnoreCase("dokter")) {
            bHapus.setEnabled(false);
            bHapus.setToolTipText("Dokter tidak boleh hapus pasien");
        }
        if (role.equalsIgnoreCase("resepsionis")) {
            bHapusRekam.setEnabled(false);
            bHapusRekam.setToolTipText("Resepsionis tidak boleh hapus rekam medis");
        }
        if (role.equalsIgnoreCase("dokter") || role.equalsIgnoreCase("resepsionis")) {
            bHapusAnt.setEnabled(false);
        }
        // ===== V7: AUTO-REFRESH 30 DETIK =====
        new javax.swing.Timer(30000, evTimer -> {
            loadPasien();
            loadAntrian();
            loadRekam();
            loadObat();
            loadDokter();
            updateDashboard();
        }).start();
        // V10: TAB DOKTER - Kelola Dokter
        JPanel pDokter = new JPanel(new BorderLayout());
        JPanel fDok = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtDokNama = new JTextField(12), txtDokSpes = new JTextField(10), txtDokJadwal = new JTextField(10);
        JComboBox<String> cbDokStatus = new JComboBox<>(new String[]{"Aktif", "Tidak Aktif"});
        JButton bTambahDok = new JButton("Tambah Dokter");
        bTambahDok.setBackground(new java.awt.Color(0, 150, 136));
        bTambahDok.setForeground(java.awt.Color.WHITE);
        fDok.add(new JLabel("Nama:"));
        fDok.add(txtDokNama);
        fDok.add(new JLabel("Spesialis:"));
        fDok.add(txtDokSpes);
        fDok.add(new JLabel("Jadwal:"));
        fDok.add(txtDokJadwal);
        fDok.add(new JLabel("Status:"));
        fDok.add(cbDokStatus);
        fDok.add(bTambahDok);
        JButton bRefDok = new JButton("Refresh Dokter");
        fDok.add(bRefDok);
        pDokter.add(fDok, BorderLayout.NORTH);
        pDokter.add(new JScrollPane(tDokter), BorderLayout.CENTER);
        tab.add("6. Data Dokter", pDokter);
        JButton bHapusDok = new JButton("Hapus Dokter");
        bHapusDok.setBackground(new java.awt.Color(229, 57, 53));
        bHapusDok.setForeground(java.awt.Color.WHITE);
        fDok.add(bHapusDok);
        JTextField txtCariDok = new JTextField(10);
        JButton bCariDok = new JButton("Cari Dokter");
        fDok.add(new JLabel("Cari:"));
        fDok.add(txtCariDok);
        fDok.add(bCariDok);
        JButton bDokHariIni = new JButton("Dokter Hari Ini");
        bDokHariIni.setBackground(new java.awt.Color(255, 152, 0));
        bDokHariIni.setForeground(java.awt.Color.WHITE);
        fDok.add(bDokHariIni);
        bTambahDok.addActionListener(evDok -> {
            if (txtDokNama.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama dokter tidak boleh kosong!");
                return;
            }
            Response rDok = KlinikClient.getInstance().kirim(new Request("ADD_DOKTER", new Dokter(txtDokNama.getText().trim(), txtDokSpes.getText().trim(), txtDokJadwal.getText().trim(), (String) cbDokStatus.getSelectedItem())));
            JOptionPane.showMessageDialog(this, rDok.message);
            loadDokter();
            txtDokNama.setText("");
            txtDokSpes.setText("");
            txtDokJadwal.setText("");
        });
        bCariDok.addActionListener(evCD -> {
            String key = txtCariDok.getText().trim().toLowerCase();
            if (key.isEmpty()) {
                loadDokter();
                return;
            }
            loadDokter();
            for (int i = mDokter.getRowCount() - 1; i >= 0; i--) {
                if (!String.valueOf(mDokter.getValueAt(i, 1)).toLowerCase().contains(key)) {
                    mDokter.removeRow(i);
                }
            }
        });
        txtCariDok.addActionListener(evCD2 -> bCariDok.doClick());
        bDokHariIni.addActionListener(evDH -> {
            String hari = hariIniNama();
            if (hari.equals("Minggu")) {
                JOptionPane.showMessageDialog(this, "Hari ini Minggu - Klinik Tutup, hanya piket!");
                return;
            }
            loadDokter();
            for (int i = mDokter.getRowCount() - 1; i >= 0; i--) {
                if (!String.valueOf(mDokter.getValueAt(i, 3)).contains(hari)) {
                    mDokter.removeRow(i);
                }
            }
            if (mDokter.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada dokter jadwal " + hari + "!");
            }
        });
        JButton bJadwalMingguan = new JButton("📅 Jadwal Mingguan");
        bJadwalMingguan.setBackground(new Color(103, 58, 183));
        bJadwalMingguan.setForeground(Color.WHITE);
        fDok.add(bJadwalMingguan);
        JButton bDokAktif = new JButton("✅ Hanya Dokter Aktif");
        bDokAktif.setBackground(new Color(46, 125, 50));
        bDokAktif.setForeground(Color.WHITE);
        bDokAktif.addActionListener(evDA -> {
            loadDokter();
            for (int i = mDokter.getRowCount() - 1; i >= 0; i--) {
                if (!String.valueOf(mDokter.getValueAt(i, 4)).equals("Aktif")) {
                    mDokter.removeRow(i);
                }
            }
            if (mDokter.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tidak ada dokter Aktif!");
            }
        });
        fDok.add(bDokAktif);
        JButton bDokSemua = new JButton("👥 Semua Dokter");
        bDokSemua.addActionListener(evDS -> {
            loadDokter();
        });
        fDok.add(bDokSemua);
        JButton bPasienPerDokter = new JButton("📊 Pasien per Dokter");
        bPasienPerDokter.setBackground(new Color(103, 58, 183));
        bPasienPerDokter.setForeground(Color.WHITE);
        bPasienPerDokter.addActionListener(evPD -> {
            java.util.Map<String, Integer> hitung = new java.util.HashMap<>();
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                String d = String.valueOf(mAntrian.getValueAt(i, 3));
                hitung.put(d, hitung.getOrDefault(d, 0) + 1);
            }
            StringBuilder sb = new StringBuilder("📊 PASIEN PER DOKTER\n\n");
            if (hitung.isEmpty()) {
                sb.append("Belum ada data antrian.");
            } else {
                java.util.List<java.util.Map.Entry<String, Integer>> list = new java.util.ArrayList<>(hitung.entrySet());
                list.sort((a, b) -> b.getValue() - a.getValue());
                for (java.util.Map.Entry<String, Integer> e : list) {
                    sb.append(e.getKey()).append(" : ").append(e.getValue()).append(" pasien\n");
                }
                sb.append("\nDokter Tersibuk: ").append(list.get(0).getKey());
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        });
        fDok.add(bPasienPerDokter);
        bJadwalMingguan.addActionListener(evJM -> {
            String[] hari = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
            StringBuilder sb = new StringBuilder("📅 JADWAL DOKTER MINGGUAN - KLINIK SEHAT NUSANTARA\n\n");
            for (String h : hari) {
                sb.append(h).append(" : ");
                java.util.List<String> list = new java.util.ArrayList<>();
                for (int i = 0; i < mDokter.getRowCount(); i++) {
                    if (String.valueOf(mDokter.getValueAt(i, 3)).contains(h)) {
                        list.add(String.valueOf(mDokter.getValueAt(i, 1)) + " (" + mDokter.getValueAt(i, 2) + ")");
                    }
                }
                sb.append(list.isEmpty() ? "- Libur / Tidak ada dokter -" : String.join(", ", list)).append("\n");
            }
            sb.append("\nDokter Jaga Hari Ini (").append(hariIniNama()).append(") : ").append(dokterJaga());
            JOptionPane.showMessageDialog(this, sb.toString());
        });
        bHapusDok.addActionListener(evHD -> {
            int row = tDokter.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Pilih dokter dulu!");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this, "Yakin hapus dokter " + mDokter.getValueAt(row, 1) + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            KlinikClient.getInstance().kirim(new Request("DELETE_DOKTER", (int) mDokter.getValueAt(row, 0)));
            loadDokter();
        });
        bRefDok.addActionListener(evDok2 -> loadDokter());
        // V15: TAB KASIR
        JPanel pKasir = new JPanel(new BorderLayout());
        JPanel fKas = new JPanel(new java.awt.GridBagLayout());
        fKas.setBackground(Color.WHITE);
        fKas.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 6, 6, 6);
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        JTextField txtBiaya = new JTextField(16), txtBayar = new JTextField(16), txtKembalian = new JTextField(16);
        txtKembalian.setEditable(false);
        txtKembalian.setBackground(new Color(232, 245, 233));
        txtKembalian.setFont(new Font("SansSerif", Font.BOLD, 14));
        JComboBox<String> cbKasPasien = new JComboBox<>();
        cbKasPasien.setPreferredSize(new Dimension(260, 32));
        JComboBox<String> cbLayanan = new JComboBox<>(new String[]{"Periksa Umum - Rp 50.000", "Periksa Gigi - Rp 100.000", "Periksa + Obat - Rp 150.000", "Konsultasi - Rp 30.000"});
        JComboBox<String> cbMetode = new JComboBox<>(new String[]{"💵 Tunai", "📱 QRIS", "🏦 Transfer Bank"});
        cbMetode.setPreferredSize(new Dimension(260, 32));
        cbLayanan.setPreferredSize(new Dimension(260, 32));
        JButton bHitung = new JButton("💰  Hitung Kembalian");
        bHitung.setBackground(new java.awt.Color(46, 125, 50));
        bHitung.setForeground(java.awt.Color.WHITE);
        bHitung.setFont(new Font("SansSerif", Font.BOLD, 14));
        bHitung.setPreferredSize(new Dimension(200, 38));
        JButton bCetakStrukKasir = new JButton("🖨️  Cetak Struk Kasir");
        bCetakStrukKasir.setPreferredSize(new Dimension(200, 38));
        JButton bRefreshKasPasien = new JButton("↻ Refresh");
        bRefreshKasPasien.addActionListener(evRK -> {
            cbKasPasien.removeAllItems();
            java.util.Set<String> sudah = new java.util.LinkedHashSet<>();
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                String item = String.valueOf(mAntrian.getValueAt(i, 1)) + " - " + mAntrian.getValueAt(i, 2) + " (" + mAntrian.getValueAt(i, 7) + ")";
                if (sudah.add(item)) {
                    cbKasPasien.addItem(item);
                }
            }
            if (cbKasPasien.getItemCount() == 0) {
                for (int i = 0; i < mPasien.getRowCount(); i++) {
                    cbKasPasien.addItem(String.valueOf(mPasien.getValueAt(i, 1)));
                }
            }
        });
        java.util.function.BiConsumer<String, java.awt.Component> tambahBarisKasir = (label, comp) -> {
            gbc.gridx = 0;
            gbc.gridy = fKas.getComponentCount() / 2;
            gbc.weightx = 0;
            fKas.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            fKas.add(comp, gbc);
        };
        gbc.gridx = 0;
        gbc.gridy = 0;
        fKas.add(new JLabel("Pasien (dari Antrian)"), gbc);
        gbc.gridx = 1;
        fKas.add(cbKasPasien, gbc);
        gbc.gridx = 2;
        fKas.add(bRefreshKasPasien, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        fKas.add(new JLabel("Layanan"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        fKas.add(cbLayanan, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        fKas.add(new JLabel("Biaya (Rp)"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        fKas.add(txtBiaya, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        fKas.add(new JLabel("Bayar (Rp)"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        fKas.add(txtBayar, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        fKas.add(new JLabel("Metode Pembayaran"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        fKas.add(cbMetode, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        fKas.add(new JLabel("Diskon"), gbc);
        JCheckBox cbDiskonSetia = new JCheckBox("⭐ Diskon Pasien Setia 10% (3x+ berobat)");
        cbDiskonSetia.addActionListener(evDS -> {
            String sBiaya = txtBiaya.getText().trim();
            if (sBiaya.isEmpty()) {
                return;
            }
            try {
                int b = Integer.parseInt(sBiaya);
                if (cbDiskonSetia.isSelected()) {
                    int diskon = b / 10;
                    txtBiaya.setText(String.valueOf(b - diskon));
                    JOptionPane.showMessageDialog(this, "Diskon Pasien Setia: -Rp " + diskon + "\nBiaya jadi: Rp " + (b - diskon));
                } else {
                    String sL = (String) cbLayanan.getSelectedItem();
                    if (sL.contains("150.000")) {
                        txtBiaya.setText("150000");
                    } else if (sL.contains("100.000")) {
                        txtBiaya.setText("100000");
                    } else if (sL.contains("50.000")) {
                        txtBiaya.setText("50000");
                    } else {
                        txtBiaya.setText("30000");
                    }
                }
            } catch (Exception ex) {
            }
        });
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        fKas.add(cbDiskonSetia, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 6;
        fKas.add(new JLabel("Kembalian (Rp)"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        fKas.add(txtKembalian, gbc);
        gbc.gridwidth = 1;
        JPanel btnKasir = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnKasir.setOpaque(false);
        btnKasir.add(bHitung);
        btnKasir.add(Box.createHorizontalStrut(10));
        btnKasir.add(bCetakStrukKasir);
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        fKas.add(btnKasir, gbc);
        gbc.gridwidth = 1;
        JTextArea areaKasir = new JTextArea();
        areaKasir.setEditable(false);
        areaKasir.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
        areaKasir.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        areaKasir.setBackground(new Color(250, 250, 250));
        JPanel kasirBawah = new JPanel(new BorderLayout());
        kasirBawah.setBorder(BorderFactory.createTitledBorder("Struk Kasir"));
        kasirBawah.add(new JScrollPane(areaKasir), BorderLayout.CENTER);
        DefaultListModel<String> modelRiwayatKasir = new DefaultListModel<>();
        JList<String> listRiwayatKasir = new JList<>(modelRiwayatKasir);
        JPanel panelRiwayat = new JPanel(new BorderLayout());
        panelRiwayat.setBorder(BorderFactory.createTitledBorder("Riwayat Transaksi Hari Ini"));
        panelRiwayat.add(new JScrollPane(listRiwayatKasir), BorderLayout.CENTER);
        JLabel lblTotalKasir = new JLabel("Total Hari Ini: Rp 0");
        lblTotalKasir.setFont(new Font("SansSerif", Font.BOLD, 13));
        panelRiwayat.add(lblTotalKasir, BorderLayout.SOUTH);
        kasirBawah.add(panelRiwayat, BorderLayout.EAST);
        JPanel pKasirAtas = new JPanel(new BorderLayout());
        pKasirAtas.add(fKas, BorderLayout.CENTER);
        pKasir.add(pKasirAtas, BorderLayout.NORTH);
        pKasir.add(kasirBawah, BorderLayout.CENTER);
        tab.add("7. Kasir", pKasir);
        cbMetode.addActionListener(evMet -> {
            if (!String.valueOf(cbMetode.getSelectedItem()).contains("Tunai")) {
                txtBayar.setText(txtBiaya.getText());
                txtKembalian.setText("0");
            }
        });
        cbLayanan.addActionListener(evLay -> {
            String s = (String) cbLayanan.getSelectedItem();
            if (s.contains("150.000")) {
                txtBiaya.setText("150000");
            } else if (s.contains("100.000")) {
                txtBiaya.setText("100000");
            } else if (s.contains("50.000")) {
                txtBiaya.setText("50000");
            } else {
                txtBiaya.setText("30000");
            }
        });
        bHitung.addActionListener(evHit -> {
            try {
                int biaya = Integer.parseInt(txtBiaya.getText().trim());
                int bayar = Integer.parseInt(txtBayar.getText().trim());
                if (bayar < biaya) {
                    JOptionPane.showMessageDialog(this, "Uang bayar kurang! Kurang Rp " + (biaya - bayar));
                    return;
                }
                int kembali = bayar - biaya;
                txtKembalian.setText(String.valueOf(kembali));
                areaKasir.setText("STRUK KASIR - KLINIK SEHAT NUSANTARA\n\nPasien: " + cbKasPasien.getSelectedItem() + "\nLayanan: " + cbLayanan.getSelectedItem() + "\nBiaya: Rp " + biaya + "\nBayar: Rp " + bayar + "\nKembalian: Rp " + kembali + "\nMetode Pembayaran: " + cbMetode.getSelectedItem() + "\nDokter Jaga: " + dokterJaga() + "\nTanggal: " + java.time.LocalDate.now() + "\n\nTerima kasih, semoga lekas sembuh!");
                modelRiwayatKasir.addElement(cbKasPasien.getSelectedItem() + " | " + cbLayanan.getSelectedItem() + " | Rp " + biaya + " | " + cbMetode.getSelectedItem() + " | " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                int totKasir = 0;
                for (int i = 0; i < modelRiwayatKasir.size(); i++) {
                    String sRi = modelRiwayatKasir.get(i);
                    int idx = sRi.indexOf("Rp ");
                    if (idx >= 0) {
                        try {
                            String num = sRi.substring(idx + 3).split(" ")[0];
                            totKasir += Integer.parseInt(num);
                        } catch (Exception ex) {
                        }
                    }
                }
                lblTotalKasir.setText("Total Hari Ini: Rp " + totKasir + " (" + modelRiwayatKasir.size() + " transaksi)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Biaya dan Bayar harus angka!");
            }
        });
        txtBayar.addActionListener(evEnterKasir -> bHitung.doClick());
        bCetakStrukKasir.addActionListener(evCS -> {
            try {
                if (areaKasir.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Hitung dulu kembalian-nya!");
                    return;
                }
                areaKasir.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal cetak: " + ex.getMessage());
            }
        });
        // ===== V44: ARSIP - Pasien Selesai / Sudah Bayar, tidak hilang, cuma pindah ke sini =====
        JPanel pArsip = new JPanel(new BorderLayout());
        DefaultTableModel mArsip = new DefaultTableModel(new String[]{"Nama", "Dokter", "Tanggal", "Jam", "Keluhan", "Status"}, 0);
        JTable tArsip = new JTable(mArsip);
        JLabel lblArsip = new JLabel("Arsip: Pasien yang sudah Selesai / Sudah Bayar. Data tidak hilang, cuma pindah ke sini.");
        lblArsip.setFont(new Font("SansSerif", Font.BOLD, 13));
        JPanel topArsip = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bRefreshArsip = new JButton("↻ Refresh Arsip");
        bRefreshArsip.setBackground(new Color(0, 150, 136));
        bRefreshArsip.setForeground(Color.WHITE);
        JButton bCekDiskonArsip = new JButton("⭐ Cek Diskon dari Arsip (3x+)");
        bCekDiskonArsip.setBackground(new Color(255, 193, 7));
        Runnable isiArsip = () -> {
            mArsip.setRowCount(0);
            for (int i = 0; i < mAntrian.getRowCount(); i++) {
                if (String.valueOf(mAntrian.getValueAt(i, 7)).equals("Selesai")) {
                    mArsip.addRow(new Object[]{mAntrian.getValueAt(i, 2), mAntrian.getValueAt(i, 3), mAntrian.getValueAt(i, 4), mAntrian.getValueAt(i, 5), mAntrian.getValueAt(i, 6), "Selesai - Sudah Bayar"});
                }
            }
            lblArsip.setText("Arsip: " + mArsip.getRowCount() + " pasien sudah Selesai / Sudah Bayar. Data tidak hilang, cuma pindah ke sini.");
        };
        bRefreshArsip.addActionListener(evRA -> {
            loadAntrian();
            isiArsip.run();
            JOptionPane.showMessageDialog(this, "Arsip di-refresh! Total: " + mArsip.getRowCount() + " pasien Selesai.");
        });
        bCekDiskonArsip.addActionListener(evCD -> {
            isiArsip.run();
            java.util.Map<String, Integer> hitung = new java.util.HashMap<>();
            for (int i = 0; i < mArsip.getRowCount(); i++) {
                String nm = String.valueOf(mArsip.getValueAt(i, 0));
                hitung.put(nm, hitung.getOrDefault(nm, 0) + 1);
            }
            StringBuilder sb = new StringBuilder("⭐ CEK DISKON DARI ARSIP (Data Arsip, Bukan Tebakan)\n\n");
            boolean ada = false;
            for (java.util.Map.Entry<String, Integer> e : hitung.entrySet()) {
                if (e.getValue() >= 3) {
                    sb.append(e.getKey()).append(" - ").append(e.getValue()).append("x Selesai → DAPAT Diskon 10% di Kasir!\n");
                    ada = true;
                }
            }
            if (!ada) {
                sb.append("Belum ada pasien yang 3x Selesai di Arsip.\nPasien terbanyak di Arsip: ").append(hitung.isEmpty() ? "-" : java.util.Collections.max(hitung.entrySet(), java.util.Map.Entry.comparingByValue()).getKey() + " (" + java.util.Collections.max(hitung.entrySet(), java.util.Map.Entry.comparingByValue()).getValue() + "x)");
            }
            sb.append("\n\nCara pakai: Di Kasir, centang Diskon Pasien Setia 10% kalau namanya ada di list ini.");
            JOptionPane.showMessageDialog(this, sb.toString());
        });
        topArsip.add(bRefreshArsip);
        topArsip.add(bCekDiskonArsip);
        topArsip.add(lblArsip);
        pArsip.add(topArsip, BorderLayout.NORTH);
        pArsip.add(new JScrollPane(tArsip), BorderLayout.CENTER);
        tab.add("8. Arsip", pArsip);
        isiArsip.run();
        // ===== V16: BURGER MENU + SIDEBAR MODERN =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(13, 71, 101));
        sidebar.setPreferredSize(new Dimension(248, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        JLabel logoSidebar = new JLabel("🏥  Klinik Sehat");
        logoSidebar.setFont(new Font("SansSerif", Font.BOLD, 22));
        logoSidebar.setForeground(Color.WHITE);
        logoSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subSidebar = new JLabel("Nusantara  •  Siantar");
        subSidebar.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subSidebar.setForeground(new Color(180, 220, 230));
        subSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);
        // logoSidebar + subSidebar sudah di dalam logoBox (headSidebar)
        String[] menuNama = {"🏠  Dashboard", "👥  Data Pasien", "🎫  Antrian", "📋  Rekam Medis", "💊  Data Obat", "📊  Laporan", "👨‍⚕️  Data Dokter", "💰  Kasir", "🗄️  Arsip"};
        java.util.List<JButton> menuBtns = new java.util.ArrayList<>();
        for (int mi = 0; mi < menuNama.length; mi++) {
            JButton mb = new JButton(menuNama[mi]);
            mb.setAlignmentX(Component.LEFT_ALIGNMENT);
            mb.setMaximumSize(new Dimension(216, 56));
            mb.setPreferredSize(new Dimension(216, 56));
            mb.setFont(new Font("SansSerif", Font.BOLD, 15));
            mb.setForeground(Color.WHITE);
            mb.setBackground(mi == 0 ? new Color(0, 150, 136) : new Color(13, 71, 101));
            mb.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
            mb.setHorizontalAlignment(SwingConstants.LEFT);
            mb.setFocusPainted(false);
            final int idxFinal = mi;
            mb.setCursor(new Cursor(Cursor.HAND_CURSOR));
            mb.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!mb.getBackground().equals(new Color(0, 150, 136))) {
                        mb.setBackground(new Color(20, 95, 130));
                    }
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!mb.getBackground().equals(new Color(20, 95, 130)) || tab.getSelectedIndex() != idxFinal) {
                        /* keep active */ }
                    if (tab.getSelectedIndex() != idxFinal) {
                        mb.setBackground(new Color(13, 71, 101));
                    }
                }
            });
            mb.addActionListener(evMenu -> {
                tab.setSelectedIndex(idxFinal);
                for (JButton b : menuBtns) {
                    b.setBackground(new Color(13, 71, 101));
                }
                mb.setBackground(new Color(0, 150, 136));
            });
            menuBtns.add(mb);
            sidebar.add(mb);
            sidebar.add(Box.createVerticalStrut(10));
        }
        sidebar.add(Box.createVerticalGlue());
        JLabel footSidebar = new JLabel("<html><div style='color:#B0D8E0;font-size:11px;'>v16 Modern UI<br/>dr. Faisal Mahmuda<br/>SIP: 4451234567</div></html>");
        footSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footSidebar);
        add(sidebar, java.awt.BorderLayout.WEST);
        // V18: Panah di SIDEBAR (bukan di kanan atas) - klik = kecilkan / besarkan sidebar
        JPanel headSidebar = new JPanel(new BorderLayout(6, 0));
        headSidebar.setOpaque(false);
        headSidebar.setMaximumSize(new Dimension(216, 44));
        headSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel logoBox = new JPanel();
        logoBox.setOpaque(false);
        logoBox.setLayout(new BoxLayout(logoBox, BoxLayout.Y_AXIS));
        logoBox.add(logoSidebar);
        logoBox.add(subSidebar);
        JButton btnPanah = new JButton("◀");
        btnPanah.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnPanah.setForeground(Color.WHITE);
        btnPanah.setBackground(new Color(0, 150, 136));
        btnPanah.setFocusPainted(false);
        btnPanah.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        btnPanah.setPreferredSize(new Dimension(36, 32));
        btnPanah.setMaximumSize(new Dimension(36, 32));
        btnPanah.setMinimumSize(new Dimension(36, 32));
        btnPanah.setToolTipText("Klik: Kecilkan / Besarkan Menu");
        btnPanah.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headSidebar.add(logoBox, BorderLayout.CENTER);
        headSidebar.add(btnPanah, BorderLayout.EAST);
        sidebar.add(headSidebar, 0);
        sidebar.add(Box.createVerticalStrut(18), 1);
        // Simpan teks lengkap menu untuk kembalikan saat dibuka lagi
        String[] menuLengkap = menuNama.clone();
        String[] menuIkon = {"🏠", "👥", "🎫", "📋", "💊", "📊", "👨‍⚕️", "💰", "🗄️"};
        final boolean[] sidebarKecil = {false};
        btnPanah.addActionListener(evP -> {
            sidebarKecil[0] = !sidebarKecil[0];
            if (sidebarKecil[0]) {
                sidebar.setPreferredSize(new Dimension(84, 0));
                headSidebar.setMaximumSize(new Dimension(52, 44));
                btnPanah.setText("▶");
                logoBox.setVisible(false);
                for (int k = 0; k < menuBtns.size(); k++) {
                    JButton bk = menuBtns.get(k);
                    bk.setText(menuIkon[k]);
                    bk.setMaximumSize(new Dimension(56, 56));
                    bk.setPreferredSize(new Dimension(56, 56));
                    bk.setMinimumSize(new Dimension(56, 56));
                    bk.setFont(new Font("SansSerif", Font.PLAIN, 22));
                    bk.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    bk.setMargin(new java.awt.Insets(0, 0, 0, 0));
                    bk.setIconTextGap(0);
                    bk.setHorizontalAlignment(SwingConstants.CENTER);
                    bk.setHorizontalTextPosition(SwingConstants.CENTER);
                }
                footSidebar.setVisible(false);
            } else {
                sidebar.setPreferredSize(new Dimension(248, 0));
                headSidebar.setMaximumSize(new Dimension(216, 44));
                btnPanah.setText("◀");
                logoBox.setVisible(true);
                for (int k = 0; k < menuBtns.size(); k++) {
                    JButton bk = menuBtns.get(k);
                    bk.setText(menuLengkap[k]);
                    bk.setMaximumSize(new Dimension(216, 56));
                    bk.setPreferredSize(new Dimension(216, 56));
                    bk.setFont(new Font("SansSerif", Font.BOLD, 15));
                    bk.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
                    bk.setMargin(new java.awt.Insets(2, 14, 2, 14));
                    bk.setHorizontalAlignment(SwingConstants.LEFT);
                }
                footSidebar.setVisible(true);
            }
            sidebar.revalidate();
            sidebar.repaint();
            revalidate();
            repaint();
        });
        // V17: HILANGKAN TOTAL tab kiri / tab atas bawaan Java - navigasi hanya dari Sidebar
        tab.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0;
            }

            @Override
            protected void paintTabArea(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
            }

            @Override
            protected void paintTab(java.awt.Graphics g, int tabPlacement, java.awt.Rectangle[] rects, int tabIndex, java.awt.Rectangle iconRect, java.awt.Rectangle textRect) {
            }
        });
        tab.setBorder(BorderFactory.createEmptyBorder());
        // ===== V24: HAK AKSES PER USER =====
        // 0 Dashboard, 1 Pasien, 2 Antrian, 3 Rekam, 4 Obat, 5 Laporan, 6 Dokter, 7 Kasir
        java.util.function.Consumer<Integer> kunciTab = (idxTab) -> {
            try {
                tab.setEnabledAt(idxTab, false);
                JButton b = menuBtns.get(idxTab);
                b.setEnabled(false);
                b.setText(b.getText() + "  🔒");
                b.setToolTipText("Menu ini dikunci untuk role: " + role);
            } catch (Exception ex) {
            }
        };
        if (role.equalsIgnoreCase("dokter")) {
            kunciTab.accept(7);
            kunciTab.accept(4);
        }
        if (role.equalsIgnoreCase("kasir")) {
            kunciTab.accept(3);
            kunciTab.accept(4);
            kunciTab.accept(6);
            kunciTab.accept(5);
        }
        if (role.equalsIgnoreCase("resepsionis")) {
            kunciTab.accept(7);
            kunciTab.accept(5);
        }
        if (role.equalsIgnoreCase("perawat") || role.equalsIgnoreCase("apoteker")) {
            kunciTab.accept(7);
        }
        if (role.equalsIgnoreCase("admin")) {
            JButton bKelolaUser = new JButton("👤  Kelola User");
            bKelolaUser.setAlignmentX(Component.LEFT_ALIGNMENT);
            bKelolaUser.setMaximumSize(new Dimension(216, 56));
            bKelolaUser.setPreferredSize(new Dimension(216, 56));
            bKelolaUser.setFont(new Font("SansSerif", Font.BOLD, 15));
            bKelolaUser.setForeground(Color.WHITE);
            bKelolaUser.setBackground(new Color(103, 58, 183));
            bKelolaUser.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            bKelolaUser.setHorizontalAlignment(SwingConstants.LEFT);
            bKelolaUser.setFocusPainted(false);
            bKelolaUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
            bKelolaUser.addActionListener(evUser -> {
                String msg = "KELOLA USER - KELOMPOK KLINIK SEHAT NUSANTARA (5 ORANG)\n\n1. Faisal Mahmuda (faisal)  →  Ketua + Admin + Dokter Penanggung Jawab\n   Login: faisal / faisal123  →  Semua Akses + Kelola User\n\n2. Anggun (anggun)  →  Dokter Umum\n   Login: anggun / anggun123  →  Pasien, Antrian, Rekam, Laporan, Dokter\n   Dikunci: Kasir, Obat\n\n3. Evi (evi)  →  Kasir\n   Login: evi / evi123  →  Dashboard, Pasien, Antrian, Kasir\n   Dikunci: Rekam, Obat, Dokter, Laporan\n\n4. Clarensia (clarensia)  →  Resepsionis\n   Login: clarensia / clarensia123  →  Dashboard, Pasien, Antrian, Obat, Dokter\n   Dikunci: Kasir, Laporan\n\n5. Armina (armina)  →  Perawat / Apoteker\n   Login: armina / armina123  →  Dashboard, Pasien, Antrian, Rekam, Obat, Dokter, Laporan\n   Dikunci: Kasir saja\n\nCerita: Faisal ketua + yang coding + dokter penanggung jawab. Anggun dokter, Evi kasir, Clarensia resepsionis, Armina perawat/apoteker.";
                JOptionPane.showMessageDialog(this, msg);
            });
            sidebar.add(bKelolaUser);
            sidebar.add(Box.createVerticalStrut(6));
        }
        if (!role.equalsIgnoreCase("admin")) {
            bHapus.setEnabled(false);
            bHapus.setToolTipText("Hanya Admin yang boleh hapus pasien");
        }
        tab.addChangeListener(evTab -> {
            if (tab.getSelectedIndex() == 7) {
                cbKasPasien.removeAllItems();
                java.util.Set<String> sudah2 = new java.util.LinkedHashSet<>();
                for (int i = 0; i < mAntrian.getRowCount(); i++) {
                    String item = String.valueOf(mAntrian.getValueAt(i, 1)) + " - " + mAntrian.getValueAt(i, 2) + " (" + mAntrian.getValueAt(i, 7) + ")";
                    if (sudah2.add(item)) {
                        cbKasPasien.addItem(item);
                    }
                }
                if (cbKasPasien.getItemCount() == 0) {
                    for (int i = 0; i < mPasien.getRowCount(); i++) {
                        cbKasPasien.addItem(String.valueOf(mPasien.getValueAt(i, 1)));
                    }
                }
                String sKas = (String) cbLayanan.getSelectedItem();
                if (sKas != null) {
                    if (sKas.contains("150.000")) {
                        txtBiaya.setText("150000");
                    } else if (sKas.contains("100.000")) {
                        txtBiaya.setText("100000");
                    } else if (sKas.contains("50.000")) {
                        txtBiaya.setText("50000");
                    } else {
                        txtBiaya.setText("30000");
                    }
                }
            }
        });
        add(tab, java.awt.BorderLayout.CENTER);
        loadPasien();
        loadAntrian();
        loadRekam();
        loadObat();
        loadDokter();
        updateDashboard();
        JLabel statusBar = new JLabel("  Klinik Sehat Nusantara  |  dr. Faisal Mahmuda  |  SIP: 4451234567  |  Data tersimpan di Server + MySQL");
        statusBar.setForeground(java.awt.Color.GRAY);
        add(statusBar, java.awt.BorderLayout.SOUTH);
        // OBAT STOK MERAH: kalau stok < 60, baris jadi merah muda
        tObat.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                try {
                    int stok = (int) mObat.getValueAt(row, 2);
                    if (stok < 60 && !isSelected) {
                        c.setBackground(new java.awt.Color(255, 235, 235));
                        c.setForeground(new java.awt.Color(183, 28, 28));
                    } else if (!isSelected) {
                        c.setBackground(java.awt.Color.WHITE);
                        c.setForeground(java.awt.Color.BLACK);
                    }
                } catch (Exception e) {
                }
                return c;
            }
        });
    }

    JPanel buatKartuPro(String ikon, String judul, JLabel nilai, String sub, Color warna, int tujuanTab) {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 22, 22);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 22, 22);
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        atas.setOpaque(false);
        JLabel lIkon = new JLabel(ikon);
        lIkon.setFont(new Font("SansSerif", Font.PLAIN, 22));
        lIkon.setForeground(warna);
        JPanel bulat = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 30));
                g2.fillOval(0, 0, 48, 48);
                super.paintComponent(g);
            }
        };
        bulat.setOpaque(false);
        bulat.setPreferredSize(new Dimension(48, 48));
        bulat.setLayout(new GridBagLayout());
        bulat.add(lIkon);
        atas.add(bulat);
        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("SansSerif", Font.BOLD, 13));
        lJudul.setForeground(new Color(60, 60, 60));
        nilai.setFont(new Font("SansSerif", Font.BOLD, 34));
        nilai.setForeground(warna);
        JLabel lSub = new JLabel(sub);
        lSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lSub.setForeground(new Color(130, 130, 130));
        JPanel tengah = new JPanel();
        tengah.setOpaque(false);
        tengah.setLayout(new BoxLayout(tengah, BoxLayout.Y_AXIS));
        tengah.add(Box.createVerticalStrut(10));
        tengah.add(lJudul);
        tengah.add(nilai);
        tengah.add(Box.createVerticalStrut(4));
        tengah.add(lSub);
        JPanel garis = new JPanel();
        garis.setBackground(warna);
        garis.setPreferredSize(new Dimension(56, 4));
        garis.setMaximumSize(new Dimension(56, 4));
        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bawah.setOpaque(false);
        bawah.add(garis);
        p.add(atas, BorderLayout.NORTH);
        p.add(tengah, BorderLayout.CENTER);
        p.add(bawah, BorderLayout.SOUTH);
        // Hover: kartu terangkat
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setToolTipText("Klik untuk buka " + judul);
        p.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                p.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
            }

            public void mouseClicked(java.awt.event.MouseEvent e) {
                tab.setSelectedIndex(tujuanTab);
            }
        });
        return p;
    }

    String hariIniNama() {
        String[] hari = {"Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
        return hari[java.time.LocalDate.now().getDayOfWeek().getValue() % 7];
    }

    String dokterJaga() {
        if (hariIniNama().equals("Minggu")) {
            return "Klinik Tutup - Hanya Piket";
        }
        if (mDokter.getRowCount() == 0) {
            return "dr. Faisal Mahmuda";
        }
        String hari = hariIniNama();
        java.util.List<String> cocok = new java.util.ArrayList<>();
        for (int i = 0; i < mDokter.getRowCount(); i++) {
            String jadwal = String.valueOf(mDokter.getValueAt(i, 3));
            if (jadwal.contains(hari)) {
                cocok.add(String.valueOf(mDokter.getValueAt(i, 1)) + " - " + mDokter.getValueAt(i, 2));
            }
        }
        if (!cocok.isEmpty()) {
            return cocok.get(0);
        }
        int idx = java.time.LocalDate.now().getDayOfYear() % mDokter.getRowCount();
        return String.valueOf(mDokter.getValueAt(idx, 1)) + " - " + mDokter.getValueAt(idx, 2);
    }

    void updateDashboard() {
        lblJagaCard.setText("Dokter Jaga Hari Ini (" + hariIniNama() + "): " + dokterJaga());
        tglDash.setText("Klinik Sehat Nusantara  •  Pematangsiantar  •  " + java.time.LocalDate.now().toString() + "  •  Dokter Jaga: " + dokterJaga() + " (Rotasi)");
        lblTotalPasien.setText(String.valueOf(mPasien.getRowCount()));
        lblTotalTab.setText("Total Pasien: " + mPasien.getRowCount() + "  |  Total Rekam: " + mRekam.getRowCount() + "  |  Antrian Menunggu: " + mAntrian.getRowCount());
        int menunggu = 0;
        for (int i = 0; i < mAntrian.getRowCount(); i++) {
            if (String.valueOf(mAntrian.getValueAt(i, 7)).equals("Menunggu")) {
                menunggu++;
            }
        }
        lblAntrian.setText(String.valueOf(menunggu));
        int habis = 0;
        StringBuilder namaHabis = new StringBuilder();
        for (int i = 0; i < mObat.getRowCount(); i++) {
            int stok = (int) mObat.getValueAt(i, 2);
            if (stok < 60) {
                habis++;
                namaHabis.append(mObat.getValueAt(i, 1)).append(" (").append(stok).append("), ");
            }
        }
        lblObat.setText(String.valueOf(habis));
        lblRekam.setText(String.valueOf(mRekam.getRowCount()));
        lblInfo.setText("Obat perlu restock: " + (namaHabis.length() > 0 ? namaHabis.toString() : "Tidak ada - stok aman"));
        String next = "-";
        for (int i = 0; i < mAntrian.getRowCount(); i++) {
            if (String.valueOf(mAntrian.getValueAt(i, 7)).equals("Menunggu")) {
                next = mAntrian.getValueAt(i, 1) + " - " + mAntrian.getValueAt(i, 2) + " - " + mAntrian.getValueAt(i, 6);
                break;
            }
        }
        lblNextAntrian.setText("Antrian Berikutnya: " + next);
        int hariIni = 0, hariSelesai = 0;
        String today = java.time.LocalDate.now().toString();
        for (int i = 0; i < mAntrian.getRowCount(); i++) {
            if (String.valueOf(mAntrian.getValueAt(i, 4)).equals(today)) {
                hariIni++;
                if (String.valueOf(mAntrian.getValueAt(i, 7)).equals("Selesai")) {
                    hariSelesai++;
                }
            }
        }
        lblHariIni.setText("Hari ini: " + hariIni + " Antrian, " + hariSelesai + " Selesai");
        lblPendapatan.setText("Pendapatan Hari Ini: Rp " + (hariSelesai * 50000) + " (" + hariSelesai + " pasien Selesai x Rp 50.000)");
        lblStokWarning.setText(habis > 0 ? "⚠️ " + habis + " Obat Stok Menipis! Cek Tab Obat" : "✅ Stok Obat Aman");
        if (habis > 0 && !sudahPeringatanStok) {
            sudahPeringatanStok = true;
            JOptionPane.showMessageDialog(this, "⚠️ Ada " + habis + " obat stok menipis (< 60)!\n" + namaHabis.toString() + "\nSegera restock ya!");
        }
    }

    void updateAntrian(String st) {
        int viewRow = tAntrian.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih antrian dulu!");
            return;
        }
        int row = tAntrian.convertRowIndexToModel(viewRow);
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih antrian dulu!");
            return;
        }
        KlinikClient.getInstance().kirim(new Request("UPDATE_ANTRIAN", new String[]{String.valueOf(mAntrian.getValueAt(row, 0)), st}));
        loadAntrian();
    }

    void loadDokter() {
        try {
            Response r = KlinikClient.getInstance().kirim(new Request("GET_DOKTER", null));
            mDokter.setRowCount(0);
            List<Dokter> l = (List<Dokter>) r.data;
            if (l != null) {
                for (Dokter d : l) {
                    mDokter.addRow(new Object[]{d.id, d.nama, d.spesialis, d.jadwal, d.status});
                }
            }
        } catch (Exception e) {
        }
    }

    void loadPasien() {
        Response r = KlinikClient.getInstance().kirim(new Request("GET_PASIEN", null));
        mPasien.setRowCount(0);
        List<Pasien> l = (List<Pasien>) r.data;
        if (l != null) {
            for (Pasien p : l) {
                mPasien.addRow(new Object[]{p.id, p.nama, p.nik, p.tglLahir, p.gender, p.telepon, p.darah, p.alergi});
            }
        }
    }

    void loadAntrian() {
        Response r = KlinikClient.getInstance().kirim(new Request("GET_ANTRIAN", null));
        mAntrian.setRowCount(0);
        List<Antrian> l = (List<Antrian>) r.data;
        if (l != null) {
            for (Antrian a : l) {
                mAntrian.addRow(new Object[]{a.id, a.noAntrian, a.namaPasien, a.dokter, a.tanggal, a.jam, a.keluhan, a.status});
            }
        }
    }

    void loadRekam() {
        Response r = KlinikClient.getInstance().kirim(new Request("GET_REKAM", null));
        mRekam.setRowCount(0);
        List<RekamMedis> l = (List<RekamMedis>) r.data;
        if (l != null) {
            for (RekamMedis x : l) {
                mRekam.addRow(new Object[]{x.id, x.namaPasien, x.diagnosa, x.tindakan, x.resep, x.tanggal});
            }
        }
    }

    void loadObat() {
        Response r = KlinikClient.getInstance().kirim(new Request("GET_OBAT", null));
        mObat.setRowCount(0);
        List<Obat> l = (List<Obat>) r.data;
        if (l != null) {
            for (Obat o : l) {
                mObat.addRow(new Object[]{o.id, o.nama, o.stok, o.harga});
            }
        }
    }
}
