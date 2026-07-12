package com.spkbri.model;

public class Karyawan {
    private int idKaryawan;
    private String nik;
    private String nama;
    private String divisi;

    public Karyawan() {}

    public Karyawan(int idKaryawan, String nik, String nama, String divisi) {
        this.idKaryawan = idKaryawan;
        this.nik = nik;
        this.nama = nama;
        this.divisi = divisi;
    }

    public int getIdKaryawan() { return idKaryawan; }
    public void setIdKaryawan(int idKaryawan) { this.idKaryawan = idKaryawan; }

    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getDivisi() { return divisi; }
    public void setDivisi(String divisi) { this.divisi = divisi; }

    @Override
    public String toString() {
        return nama + " (" + nik + ")";
    }
}
