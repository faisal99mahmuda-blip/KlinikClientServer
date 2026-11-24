package klinik.server;

import klinik.model.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/klinik_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static String cekLogin(String u, String p) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT role FROM users WHERE username=? AND password=?")) {
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Pasien> getPasien() {
        List<Pasien> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM pasien ORDER BY id")) {
            while (rs.next()) {
                Pasien p = new Pasien();
                p.id = rs.getInt("id");
                p.nama = rs.getString("nama");
                p.nik = rs.getString("nik");
                p.tglLahir = String.valueOf(rs.getDate("tgl_lahir"));
                p.gender = rs.getString("gender");
                p.telepon = rs.getString("telepon");
                p.alamat = rs.getString("alamat");
                p.darah = rs.getString("darah");
                p.alergi = rs.getString("alergi");
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean tambahPasien(Pasien p) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO pasien(nama,nik,tgl_lahir,gender,telepon,alamat,darah,alergi) VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setString(1, p.nama);
            ps.setString(2, p.nik);
            ps.setDate(3, java.sql.Date.valueOf(p.tglLahir));
            ps.setString(4, p.gender);
            ps.setString(5, p.telepon);
            ps.setString(6, p.alamat);
            ps.setString(7, p.darah);
            ps.setString(8, p.alergi);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hapusPasien(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM pasien WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<Dokter> getDokter() {
        List<Dokter> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM dokter")) {
            while (rs.next()) {
                Dokter d = new Dokter();
                d.id = rs.getInt("id");
                d.nama = rs.getString("nama");
                d.spesialis = rs.getString("spesialis");
                d.jadwal = rs.getString("jadwal");
                d.status = rs.getString("status");
                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Antrian> getAntrian() {
        List<Antrian> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM antrian ORDER BY id")) {
            while (rs.next()) {
                Antrian a = new Antrian();
                a.id = rs.getInt("id");
                a.noAntrian = rs.getString("no_antrian");
                a.namaPasien = rs.getString("nama_pasien");
                a.dokter = rs.getString("dokter");
                a.tanggal = String.valueOf(rs.getDate("tanggal"));
                a.jam = rs.getString("jam");
                a.keluhan = rs.getString("keluhan");
                a.status = rs.getString("status");
                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean tambahAntrian(Antrian a) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO antrian(no_antrian,nama_pasien,dokter,tanggal,jam,keluhan,status) VALUES(?,?,?,?,?,?,?)")) {
            ps.setString(1, a.noAntrian);
            ps.setString(2, a.namaPasien);
            ps.setString(3, a.dokter);
            ps.setDate(4, java.sql.Date.valueOf(a.tanggal));
            ps.setString(5, a.jam);
            ps.setString(6, a.keluhan);
            ps.setString(7, a.status);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // === TAHAP 6: 4 fungsi yang kemarin hilang ===
    public static List<Obat> getObat() {
        List<Obat> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM obat")) {
            while (rs.next()) {
                Obat o = new Obat();
                o.id = rs.getInt("id");
                o.nama = rs.getString("nama");
                o.stok = rs.getInt("stok");
                o.harga = rs.getInt("harga");
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<RekamMedis> getRekamMedis() {
        List<RekamMedis> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM rekam_medis ORDER BY id")) {
            while (rs.next()) {
                RekamMedis r = new RekamMedis();
                r.id = rs.getInt("id");
                r.namaPasien = rs.getString("nama_pasien");
                r.diagnosa = rs.getString("diagnosa");
                r.tindakan = rs.getString("tindakan");
                r.resep = rs.getString("resep");
                r.tanggal = String.valueOf(rs.getDate("tanggal"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean tambahRekamMedis(RekamMedis r) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO rekam_medis(nama_pasien,diagnosa,tindakan,resep,tanggal) VALUES(?,?,?,?,?)")) {
            ps.setString(1, r.namaPasien);
            ps.setString(2, r.diagnosa);
            ps.setString(3, r.tindakan);
            ps.setString(4, r.resep);
            ps.setDate(5, java.sql.Date.valueOf(r.tanggal));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hapusAntrian(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM antrian WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hapusRekam(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM rekam_medis WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean tambahObat(Obat o) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO obat(nama,stok,harga) VALUES(?,?,?)")) {
            ps.setString(1, o.nama);
            ps.setInt(2, o.stok);
            ps.setInt(3, o.harga);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean tambahDokter(Dokter d) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO dokter(nama,spesialis,jadwal,status) VALUES(?,?,?,?)")) {
            ps.setString(1, d.nama);
            ps.setString(2, d.spesialis);
            ps.setString(3, d.jadwal);
            ps.setString(4, d.status);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hapusObat(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM obat WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hapusDokter(int id) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dokter WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean resetPassword(String username, String newPass) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE users SET password=? WHERE username=?")) {
            ps.setString(1, newPass);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateStatusAntrian(int id, String status) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE antrian SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
