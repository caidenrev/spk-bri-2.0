-- MySQL DDL & Seed Data for SPK MOORA Bank BRI KCP Arundina
-- Database: spk_moora

CREATE DATABASE IF NOT EXISTS spk_moora;
USE spk_moora;

-- ============================================================
-- 1. Tabel users
--    Menyimpan akun login untuk dua role: admin & pimpinan
-- ============================================================
CREATE TABLE IF NOT EXISTS `users` (
  `id_user`      INT          AUTO_INCREMENT PRIMARY KEY,
  `username`     VARCHAR(100) UNIQUE NOT NULL,
  `password`     VARCHAR(255) NOT NULL,
  `nama_lengkap` VARCHAR(255) NOT NULL,
  `role`         VARCHAR(20)  NOT NULL DEFAULT 'admin'
                 COMMENT 'Nilai: admin | pimpinan'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Migrasi: tambah kolom role jika tabel sudah ada sebelumnya
ALTER TABLE `users`
  ADD COLUMN IF NOT EXISTS `role` VARCHAR(20) NOT NULL DEFAULT 'admin'
  COMMENT 'Nilai: admin | pimpinan';

-- ============================================================
-- 2. Tabel karyawan
-- ============================================================
CREATE TABLE IF NOT EXISTS `karyawan` (
  `id_karyawan` INT          AUTO_INCREMENT PRIMARY KEY,
  `nik`         VARCHAR(100) UNIQUE NOT NULL,
  `nama`        VARCHAR(255) NOT NULL,
  `divisi`      VARCHAR(50)  NOT NULL COMMENT 'Nilai: Bisnis | Operasional'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. Tabel kriteria
-- ============================================================
CREATE TABLE IF NOT EXISTS `kriteria` (
  `id_kriteria`   INT          AUTO_INCREMENT PRIMARY KEY,
  `kode_kriteria` VARCHAR(50)  NOT NULL,
  `nama_kriteria` VARCHAR(255) NOT NULL,
  `sifat`         VARCHAR(50)  NOT NULL COMMENT 'Nilai: Benefit | Cost',
  `bobot`         DOUBLE       NOT NULL,
  `divisi`        VARCHAR(50)  NOT NULL COMMENT 'Nilai: Bisnis | Operasional'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. Tabel penilaian
--    Menyimpan nilai kinerja karyawan per kriteria
--    UNIQUE KEY mencegah duplikasi penilaian per karyawan-kriteria
--    ON DELETE CASCADE membersihkan data saat karyawan/kriteria dihapus
-- ============================================================
CREATE TABLE IF NOT EXISTS `penilaian` (
  `id_penilaian` INT    AUTO_INCREMENT PRIMARY KEY,
  `id_karyawan`  INT    NOT NULL,
  `id_kriteria`  INT    NOT NULL,
  `nilai`        DOUBLE NOT NULL,
  CONSTRAINT `fk_penilaian_karyawan`
    FOREIGN KEY (`id_karyawan`) REFERENCES `karyawan`(`id_karyawan`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_penilaian_kriteria`
    FOREIGN KEY (`id_kriteria`) REFERENCES `kriteria`(`id_kriteria`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `unique_penilaian`
    UNIQUE KEY (`id_karyawan`, `id_kriteria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SEED DATA
-- ============================================================

-- Akun Admin (role: admin)
INSERT INTO `users` (`username`, `password`, `nama_lengkap`, `role`)
VALUES ('admin', 'admin123', 'Administrator SPK', 'admin')
ON DUPLICATE KEY UPDATE `role` = 'admin';

-- Akun Pimpinan Divisi Bisnis (role: pimpinan)
INSERT INTO `users` (`username`, `password`, `nama_lengkap`, `role`)
VALUES ('pimpinan_bisnis', 'pimpinan123', 'Pimpinan Divisi Bisnis', 'pimpinan')
ON DUPLICATE KEY UPDATE `role` = 'pimpinan';

-- Akun Pimpinan Divisi Operasional (role: pimpinan)
INSERT INTO `users` (`username`, `password`, `nama_lengkap`, `role`)
VALUES ('pimpinan_ops', 'pimpinan123', 'Pimpinan Divisi Operasional', 'pimpinan')
ON DUPLICATE KEY UPDATE `role` = 'pimpinan';

-- Kriteria default Divisi Bisnis & Operasional
INSERT INTO `kriteria` (`kode_kriteria`, `nama_kriteria`, `sifat`, `bobot`, `divisi`) VALUES
('C1', 'Target Simpanan',        'Benefit', 0.40, 'Bisnis'),
('C2', 'Target Kredit',          'Benefit', 0.30, 'Bisnis'),
('C3', 'Akuisisi Rekening Baru', 'Benefit', 0.20, 'Bisnis'),
('C1', 'Kualitas Layanan / CS',  'Benefit', 0.30, 'Operasional'),
('C2', 'Ketelitian Transaksi',   'Benefit', 0.30, 'Operasional'),
('C3', 'Kehadiran / Kedisiplinan','Benefit',0.20, 'Operasional'),
('C4', 'Bebas Komplain Nasabah', 'Benefit', 0.20, 'Operasional')
ON DUPLICATE KEY UPDATE `kode_kriteria` = VALUES(`kode_kriteria`);

