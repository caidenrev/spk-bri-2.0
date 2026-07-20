package com.spkbri.model;

public class Karyawan {
    private int idKaryawan;
    private String kodeKaryawan;
    private String nama;
    private String divisi;

    public Karyawan() {}

    public Karyawan(int idKaryawan, String kodeKaryawan, String nama, String divisi) {
        this.idKaryawan = idKaryawan;
        this.kodeKaryawan = kodeKaryawan;
        this.nama = nama;
        this.divisi = divisi;
    }

    public int getIdKaryawan() { return idKaryawan; }
    public void setIdKaryawan(int idKaryawan) { this.idKaryawan = idKaryawan; }

    public String getKodeKaryawan() { return kodeKaryawan; }
    public void setKodeKaryawan(String kodeKaryawan) { this.kodeKaryawan = kodeKaryawan; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getDivisi() { return divisi; }
    public void setDivisi(String divisi) { this.divisi = divisi; }

    @Override
    public String toString() {
        return nama + " (" + kodeKaryawan + ")";
    }
}
