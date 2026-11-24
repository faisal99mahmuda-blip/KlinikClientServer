package klinik.model;

import java.io.Serializable;

public class RekamMedis implements Serializable {

    public int id;
    public String namaPasien, diagnosa, tindakan, resep, tanggal;

    public RekamMedis() {
    }

    public RekamMedis(String p, String d, String t, String r, String tg) {
        namaPasien = p;
        diagnosa = d;
        tindakan = t;
        resep = r;
        tanggal = tg;
    }
}
