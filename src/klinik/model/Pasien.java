package klinik.model;

import java.io.Serializable;

public class Pasien implements Serializable {

    public int id;
    public String nama, nik, tglLahir, gender, telepon, alamat, darah, alergi;

    public Pasien() {
    }

    public Pasien(String nama, String nik, String tgl, String gender, String telp, String alamat, String darah, String alergi) {
        this.nama = nama;
        this.nik = nik;
        this.tglLahir = tgl;
        this.gender = gender;
        this.telepon = telp;
        this.alamat = alamat;
        this.darah = darah;
        this.alergi = alergi;
    }

    @Override
    public String toString() {
        return nama + " - " + nik;
    }
}
