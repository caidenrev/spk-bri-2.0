package com.spkbri.core;

import com.spkbri.database.DatabaseHelper;
import com.spkbri.model.Karyawan;
import com.spkbri.model.Kriteria;
import com.spkbri.model.RankingResult;
import com.spkbri.model.MooraCalculationResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MooraEngine {

    public static MooraCalculationResult calculate(String divisi) {
        List<Karyawan> karyawanList = new ArrayList<>();
        List<Kriteria> kriteriaList = new ArrayList<>();
        // Maps karyawanId -> (kriteriaId -> nilai)
        Map<Integer, Map<Integer, Double>> matriksKeputusan = new HashMap<>();

        try (Connection conn = DatabaseHelper.getConnection()) {
            // 1. Get Karyawan
            String sqlKaryawan = "SELECT * FROM karyawan WHERE divisi = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlKaryawan)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Karyawan k = new Karyawan(
                                rs.getInt("id_karyawan"),
                                rs.getString("kode_karyawan"),
                                rs.getString("nama"),
                                rs.getString("divisi"));
                        karyawanList.add(k);
                        matriksKeputusan.put(k.getIdKaryawan(), new HashMap<>());
                    }
                }
            }

            // 2. Get Kriteria
            String sqlKriteria = "SELECT * FROM kriteria WHERE divisi = ? ORDER BY id_kriteria ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlKriteria)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Kriteria kr = new Kriteria(
                                rs.getInt("id_kriteria"),
                                rs.getString("kode_kriteria"),
                                rs.getString("nama_kriteria"),
                                rs.getString("sifat"),
                                rs.getDouble("bobot"),
                                rs.getString("divisi"));
                        kriteriaList.add(kr);
                    }
                }
            }

            if (karyawanList.isEmpty() || kriteriaList.isEmpty()) {
                return new MooraCalculationResult(karyawanList, kriteriaList, new HashMap<>(), new HashMap<>(),
                        new HashMap<>(), new ArrayList<>());
            }
            // 3. Get Penilaian
            String sqlPenilaian = "SELECT p.* FROM penilaian p " +
                    "JOIN karyawan k ON p.id_karyawan = k.id_karyawan " +
                    "WHERE k.divisi = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlPenilaian)) {
                pstmt.setString(1, divisi);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int idKaryawan = rs.getInt("id_karyawan");
                        int idKriteria = rs.getInt("id_kriteria");
                        double nilai = rs.getDouble("nilai");
                        if (matriksKeputusan.containsKey(idKaryawan)) {
                            matriksKeputusan.get(idKaryawan).put(idKriteria, nilai);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new MooraCalculationResult(karyawanList, kriteriaList, new HashMap<>(), new HashMap<>(),
                    new HashMap<>(), new ArrayList<>());
        }

        // Fill missing values with 0.0
        for (Karyawan k : karyawanList) {
            Map<Integer, Double> nilaiMap = matriksKeputusan.get(k.getIdKaryawan());
            for (Kriteria kr : kriteriaList) {
                nilaiMap.putIfAbsent(kr.getIdKriteria(), 0.0);
            }
        }

        // 4. Calculate Denominator for Normalization for each criterion:
        // sqrt(sum(x_ij^2))
        Map<Integer, Double> pembagiMap = new HashMap<>();
        for (Kriteria kr : kriteriaList) {
            double sumSq = 0.0;
            for (Karyawan k : karyawanList) {
                double val = matriksKeputusan.get(k.getIdKaryawan()).get(kr.getIdKriteria());
                sumSq += val * val;
            }
            double pembagi = Math.sqrt(sumSq);
            if (pembagi == 0.0)
                pembagi = 1.0; // Avoid division by zero
            pembagiMap.put(kr.getIdKriteria(), pembagi);
        }

        // 5. Normalization, Weighted Optimization, and calculating Yi = Sum(Benefit) -
        // Sum(Cost)
        Map<Integer, Map<Integer, Double>> matriksNormalisasi = new HashMap<>();
        Map<Integer, Map<Integer, Double>> matriksNormalisasiTerbobot = new HashMap<>();
        List<RankingResult> results = new ArrayList<>();

        for (Karyawan k : karyawanList) {
            double sumBenefit = 0.0;
            double sumCost = 0.0;
            Map<Integer, Double> nilaiMap = matriksKeputusan.get(k.getIdKaryawan());
            Map<Integer, Double> normRow = new HashMap<>();
            Map<Integer, Double> weightedNormRow = new HashMap<>();

            for (Kriteria kr : kriteriaList) {
                double nilaiMentah = nilaiMap.get(kr.getIdKriteria());
                double pembagi = pembagiMap.get(kr.getIdKriteria());
                double terbiasa = nilaiMentah / pembagi; // Normalization
                double terbobot = terbiasa * kr.getBobot(); // Weighted normalization

                normRow.put(kr.getIdKriteria(), terbiasa);
                weightedNormRow.put(kr.getIdKriteria(), terbobot);

                if ("Benefit".equalsIgnoreCase(kr.getSifat())) {
                    sumBenefit += terbobot;
                } else {
                    sumCost += terbobot;
                }
            }

            matriksNormalisasi.put(k.getIdKaryawan(), normRow);
            matriksNormalisasiTerbobot.put(k.getIdKaryawan(), weightedNormRow);

            double score = sumBenefit - sumCost;
            results.add(new RankingResult(k, score));
        }

        // 6. Sort results by score descending
        Collections.sort(results, new Comparator<RankingResult>() {
            @Override
            public int compare(RankingResult o1, RankingResult o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        });

        // 7. Assign ranks
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }

        return new MooraCalculationResult(karyawanList, kriteriaList, matriksKeputusan, matriksNormalisasi,
                matriksNormalisasiTerbobot, results);
    }
}
