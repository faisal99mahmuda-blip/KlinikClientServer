package klinik.model;

import java.io.Serializable;

public class Obat implements Serializable {

    public int id, stok, harga;
    public String nama;

    public Obat() {
    }

    public Obat(String n, int s, int h) {
        nama = n;
        stok = s;
        harga = h;
    }

    @Override
    public String toString() {
        return nama + " - stok:" + stok + " - Rp" + harga;
    }
}
