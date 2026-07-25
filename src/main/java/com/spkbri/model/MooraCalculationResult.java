package com.spkbri.model;

import java.util.List;
import java.util.Map;

public class MooraCalculationResult {
    private List<Karyawan> karyawanList;
    private List<Kriteria> kriteriaList;
    private Map<Integer, Map<Integer, Double>> matriksKeputusan;
    private Map<Integer, Map<Integer, Double>> matriksNormalisasi;
    private Map<Integer, Map<Integer, Double>> matriksNormalisasiTerbobot;
    private List<RankingResult> rankingResults;

    public MooraCalculationResult(List<Karyawan> karyawanList,
                                   List<Kriteria> kriteriaList,
                                   Map<Integer, Map<Integer, Double>> matriksKeputusan,
                                   Map<Integer, Map<Integer, Double>> matriksNormalisasi,
                                   Map<Integer, Map<Integer, Double>> matriksNormalisasiTerbobot,
                                   List<RankingResult> rankingResults) {
        this.karyawanList = karyawanList;
        this.kriteriaList = kriteriaList;
        this.matriksKeputusan = matriksKeputusan;
        this.matriksNormalisasi = matriksNormalisasi;
        this.matriksNormalisasiTerbobot = matriksNormalisasiTerbobot;
        this.rankingResults = rankingResults;
    }

    public List<Karyawan> getKaryawanList() {
        return karyawanList;
    }

    public List<Kriteria> getKriteriaList() {
        return kriteriaList;
    }

    public Map<Integer, Map<Integer, Double>> getMatriksKeputusan() {
        return matriksKeputusan;
    }

    public Map<Integer, Map<Integer, Double>> getMatriksNormalisasi() {
        return matriksNormalisasi;
    }

    public Map<Integer, Map<Integer, Double>> getMatriksNormalisasiTerbobot() {
        return matriksNormalisasiTerbobot;
    }

    public List<RankingResult> getRankingResults() {
        return rankingResults;
    }
}
