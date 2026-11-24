package klinik.model;

import java.io.Serializable;

public class Antrian implements Serializable {

    public int id;
    public String noAntrian, namaPasien, dokter, tanggal, jam, keluhan, status;

    public Antrian() {
    }

    public Antrian(String no, String pasien, String dok, String tgl, String jam, String kel, String st) {
        noAntrian = no;
        namaPasien = pasien;
        dokter = dok;
        tanggal = tgl;
        this.jam = jam;
        keluhan = kel;
        status = st;
    }
}
