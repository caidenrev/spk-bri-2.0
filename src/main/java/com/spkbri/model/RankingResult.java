package com.spkbri.model;

public class RankingResult {
    private Karyawan karyawan;
    private double score;
    private int rank;

    public RankingResult(Karyawan karyawan, double score) {
        this.karyawan = karyawan;
        this.score = score;
    }

    public Karyawan getKaryawan() { return karyawan; }
    public void setKaryawan(Karyawan karyawan) { this.karyawan = karyawan; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
