package com.spkbri.model;

public class Kriteria {
    private int idKriteria;
    private String kodeKriteria;
    private String namaKriteria;
    private String sifat;
    private double bobot;
    private String divisi;

    public Kriteria() {}

    public Kriteria(int idKriteria, String kodeKriteria, String namaKriteria, String sifat, double bobot, String divisi) {
        this.idKriteria = idKriteria;
        this.kodeKriteria = kodeKriteria;
        this.namaKriteria = namaKriteria;
        this.sifat = sifat;
        this.bobot = bobot;
        this.divisi = divisi;
    }

    public int getIdKriteria() { return idKriteria; }
    public void setIdKriteria(int idKriteria) { this.idKriteria = idKriteria; }

    public String getKodeKriteria() { return kodeKriteria; }
    public void setKodeKriteria(String kodeKriteria) { this.kodeKriteria = kodeKriteria; }

    public String getNamaKriteria() { return namaKriteria; }
    public void setNamaKriteria(String namaKriteria) { this.namaKriteria = namaKriteria; }

    public String getSifat() { return sifat; }
    public void setSifat(String sifat) { this.sifat = sifat; }

    public double getBobot() { return bobot; }
    public void setBobot(double bobot) { this.bobot = bobot; }

    public String getDivisi() { return divisi; }
    public void setDivisi(String divisi) { this.divisi = divisi; }

    @Override
    public String toString() {
        return kodeKriteria + " - " + namaKriteria;
    }
}
