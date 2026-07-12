package com.spkbri.model;

public class Penilaian {
    private int idPenilaian;
    private int idKaryawan;
    private int idKriteria;
    private double nilai;

    public Penilaian() {}

    public Penilaian(int idPenilaian, int idKaryawan, int idKriteria, double nilai) {
        this.idPenilaian = idPenilaian;
        this.idKaryawan = idKaryawan;
        this.idKriteria = idKriteria;
        this.nilai = nilai;
    }

    public int getIdPenilaian() { return idPenilaian; }
    public void setIdPenilaian(int idPenilaian) { this.idPenilaian = idPenilaian; }

    public int getIdKaryawan() { return idKaryawan; }
    public void setIdKaryawan(int idKaryawan) { this.idKaryawan = idKaryawan; }

    public int getIdKriteria() { return idKriteria; }
    public void setIdKriteria(int idKriteria) { this.idKriteria = idKriteria; }

    public double getNilai() { return nilai; }
    public void setNilai(double nilai) { this.nilai = nilai; }
}
