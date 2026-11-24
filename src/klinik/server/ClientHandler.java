package klinik.server;

import klinik.common.*;
import klinik.model.*;
import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket s) {
        this.socket = s;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Request req = (Request) in.readObject();
                System.out.println("[REQUEST] " + req.action + " dari " + socket.getInetAddress());
                Response res = proses(req);
                out.writeObject(res);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client terputus: " + socket.getInetAddress());
        }
    }

    private Response proses(Request req) {
        // Pakai if-else biar tidak ada masalah kurung di case
        if (req.action.equals("LOGIN")) {
            String role = Database.cekLogin(req.username, req.password);
            if (role != null) {
                return new Response(true, "Login berhasil", null, role);
            }
            return new Response(false, "Username / password salah", null);
        }
        if (req.action.equals("GET_PASIEN")) {
            return new Response(true, "OK", Database.getPasien());
        }
        if (req.action.equals("ADD_PASIEN")) {
            return new Response(Database.tambahPasien((Pasien) req.data), "Pasien disimpan", "OK");
        }
        if (req.action.equals("DELETE_ANTRIAN")) {
            return new Response(Database.hapusAntrian((Integer) req.data), "Antrian dihapus", "OK");
        }
        if (req.action.equals("DELETE_REKAM")) {
            return new Response(Database.hapusRekam((Integer) req.data), "Rekam medis dihapus", "OK");
        }
        if (req.action.equals("DELETE_PASIEN")) {
            return new Response(Database.hapusPasien((Integer) req.data), "Pasien dihapus", "OK");
        }
        if (req.action.equals("ADD_DOKTER")) {
            return new Response(Database.tambahDokter((Dokter) req.data), "Dokter berhasil ditambahkan", "OK");
        }
        if (req.action.equals("GET_DOKTER")) {
            return new Response(true, "OK", Database.getDokter());
        }
        if (req.action.equals("GET_ANTRIAN")) {
            return new Response(true, "OK", Database.getAntrian());
        }
        if (req.action.equals("ADD_ANTRIAN")) {
            return new Response(Database.tambahAntrian((Antrian) req.data), "Antrian disimpan", "OK");
        }
        if (req.action.equals("ADD_OBAT")) {
            return new Response(Database.tambahObat((Obat) req.data), "Obat berhasil ditambahkan", "OK");
        }
        if (req.action.equals("DELETE_OBAT")) {
            return new Response(Database.hapusObat((Integer) req.data), "Obat dihapus", "OK");
        }
        if (req.action.equals("DELETE_DOKTER")) {
            return new Response(Database.hapusDokter((Integer) req.data), "Dokter dihapus", "OK");
        }
        if (req.action.equals("GET_OBAT")) {
            return new Response(true, "OK", Database.getObat());
        }
        if (req.action.equals("GET_REKAM")) {
            return new Response(true, "OK", Database.getRekamMedis());
        }
        if (req.action.equals("ADD_REKAM")) {
            return new Response(Database.tambahRekamMedis((RekamMedis) req.data), "Rekam medis disimpan", "OK");
        }
        if (req.action.equals("UPDATE_ANTRIAN")) {
            String[] d = (String[]) req.data;
            boolean ok = Database.updateStatusAntrian(Integer.parseInt(d[0]), d[1]);
            return new Response(ok, "Status antrian diubah", "OK");
        }
        if (req.action.equals("RESET_PASSWORD")) {
            boolean ok = Database.resetPassword(req.username, req.password);
            return new Response(ok, ok ? "Password berhasil direset! Silakan login dengan password baru." : "Username tidak ditemukan!", null);
        }
        return new Response(false, "Aksi tidak dikenal: " + req.action, null);
    }
}
