package klinik.model;

import java.io.Serializable;

public class Dokter implements Serializable {

    public int id;
    public String nama, spesialis, jadwal, status;

    public Dokter() {
    }

    public Dokter(String n, String s, String j, String st) {
        nama = n;
        spesialis = s;
        jadwal = j;
        status = st;
    }

    @Override
    public String toString() {
        return nama + " (" + spesialis + ")";
    }
}
