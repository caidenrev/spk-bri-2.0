-- MySQL DDL & Seed Data (DATA ASLI) untuk SPK MOORA Bank BRI KCP Arundina
-- Database: spk_moora
-- File ini menggantikan data dummy dengan data karyawan & kriteria yang sebenarnya.

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

ALTER TABLE `users`
  ADD COLUMN IF NOT EXISTS `role` VARCHAR(20) NOT NULL DEFAULT 'admin'
  COMMENT 'Nilai: admin | pimpinan';

-- ============================================================
-- 2. Tabel karyawan
-- ============================================================
CREATE TABLE IF NOT EXISTS `karyawan` (
  `id_karyawan` INT          AUTO_INCREMENT PRIMARY KEY,
  `kode_karyawan` VARCHAR(100) UNIQUE NOT NULL,
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
-- BERSIHKAN DATA DUMMY LAMA (kriteria & karyawan)
-- `penilaian` ikut terhapus otomatis karena ON DELETE CASCADE
-- ============================================================
-- Catatan: TRUNCATE tidak dipakai karena MySQL akan menolaknya selama
-- tabel `kriteria`/`karyawan` masih direferensikan oleh foreign key di
-- tabel `penilaian` (error #1701). DELETE FROM aman dipakai asalkan
-- tabel anak (`penilaian`) dikosongkan lebih dulu.
DELETE FROM `penilaian`;
DELETE FROM `kriteria`;
DELETE FROM `karyawan`;

-- Reset AUTO_INCREMENT supaya id mulai dari 1 lagi (opsional, boleh dihapus)
ALTER TABLE `penilaian` AUTO_INCREMENT = 1;
ALTER TABLE `kriteria` AUTO_INCREMENT = 1;
ALTER TABLE `karyawan` AUTO_INCREMENT = 1;

-- ============================================================
-- SEED DATA: users (akun login)
-- ============================================================
INSERT INTO `users` (`username`, `password`, `nama_lengkap`, `role`)
VALUES ('admin', 'admin123', 'Administrator SPK', 'admin')
ON DUPLICATE KEY UPDATE `role` = 'admin';

INSERT INTO `users` (`username`, `password`, `nama_lengkap`, `role`)
VALUES ('pimpinan_bisnis', 'pimpinan123', 'Pimpinan Divisi Bisnis', 'pimpinan')
ON DUPLICATE KEY UPDATE `role` = 'pimpinan';

INSERT INTO `users` (`username`, `password`, `nama_lengkap`, `role`)
VALUES ('pimpinan_ops', 'pimpinan123', 'Pimpinan Divisi Operasional', 'pimpinan')
ON DUPLICATE KEY UPDATE `role` = 'pimpinan';

-- ============================================================
-- SEED DATA: kriteria (DATA ASLI - Divisi Bisnis & Operasional)
-- ============================================================
INSERT INTO `kriteria` (`kode_kriteria`, `nama_kriteria`, `sifat`, `bobot`, `divisi`) VALUES
('B-1', 'Pencapaian Target Kredit', 'Benefit', 0.25, 'Bisnis'),
('B-2', 'Pencapaian Target Simpanan', 'Benefit', 0.20, 'Bisnis'),
('B-3', 'Akuisisi Nasabah Baru', 'Benefit', 0.15, 'Bisnis'),
('B-4', 'Kualitas Kredit Keuangan', 'Cost', 0.10, 'Bisnis'),
('B-5', 'Aktivitas Penjualan Silang', 'Benefit', 0.10, 'Bisnis'),
('B-6', 'Tingkat Kehadiran & Presensi', 'Cost', 0.10, 'Bisnis'),
('B-7', 'Integritas & Etika Kerja', 'Benefit', 0.05, 'Bisnis'),
('B-8', 'Kerja Sama Tim', 'Benefit', 0.05, 'Bisnis'),
('O-1', 'Kualitas Layanan', 'Benefit', 0.25, 'Operasional'),
('O-2', 'Akurasi Transaksi', 'Benefit', 0.20, 'Operasional'),
('O-3', 'Kepatuhan Terhadap SOP', 'Benefit', 0.15, 'Operasional'),
('O-4', 'Kerapihan Administrasi', 'Benefit', 0.10, 'Operasional'),
('O-5', 'Tingkat Kehadiran & Presensi', 'Cost', 0.10, 'Operasional'),
('O-6', 'Pelanggaran Keamanan Data', 'Cost', 0.10, 'Operasional'),
('O-7', 'Produk Knowledge', 'Benefit', 0.05, 'Operasional'),
('O-8', 'Efisiensi & Solusi Kerja', 'Benefit', 0.05, 'Operasional');

-- ============================================================
-- SEED DATA: karyawan (DATA ASLI - 100 Bisnis + 100 Operasional)
-- ============================================================
INSERT INTO `karyawan` (`kode_karyawan`, `nama`, `divisi`) VALUES
('B-001', 'Ambar Elisan', 'Bisnis'),
('B-002', 'Syamira Septiani', 'Bisnis'),
('B-003', 'Julian Rusdi Hakim', 'Bisnis'),
('B-004', 'Farhan Hunta', 'Bisnis'),
('B-005', 'Abdul Aditya', 'Bisnis'),
('B-006', 'Feri Rakhman', 'Bisnis'),
('B-007', 'Achmad Wibowo', 'Bisnis'),
('B-008', 'Panji Fernanda', 'Bisnis'),
('B-009', 'Aga Purwantie', 'Bisnis'),
('B-010', 'Peppy Ramdani', 'Bisnis'),
('B-011', 'Arip Prasetyo', 'Bisnis'),
('B-012', 'Ari Swastiko', 'Bisnis'),
('B-013', 'Yoga Purnomo', 'Bisnis'),
('B-014', 'Indri Rumopa', 'Bisnis'),
('B-015', 'Dita Amores Sabar', 'Bisnis'),
('B-016', 'Anry Anggawiyata', 'Bisnis'),
('B-017', 'Dewanda Christanto', 'Bisnis'),
('B-018', 'Muhammad Yusuf Priansyah', 'Bisnis'),
('B-019', 'Yusuf Suwandi', 'Bisnis'),
('B-020', 'Ahmad Maulana', 'Bisnis'),
('B-021', 'Angga Kurniawan', 'Bisnis'),
('B-022', 'Muhammad Iqbal Kamil', 'Bisnis'),
('B-023', 'Muhammad Burhan Irfan', 'Bisnis'),
('B-024', 'Ulfah Cahyani', 'Bisnis'),
('B-025', 'Zefany Klarita Br Sitorus', 'Bisnis'),
('B-026', 'Fitri Handayani', 'Bisnis'),
('B-027', 'Guntur Wibowo', 'Bisnis'),
('B-028', 'Hendra Wijaya', 'Bisnis'),
('B-029', 'Herry Kustiyanto', 'Bisnis'),
('B-030', 'Joko Susilo', 'Bisnis'),
('B-031', 'Muhammad Noor Dimentri', 'Bisnis'),
('B-032', 'Hedi Fabian', 'Bisnis'),
('B-033', 'Mega Utami', 'Bisnis'),
('B-034', 'Nugroho Adi', 'Bisnis'),
('B-035', 'Onny Syahrial', 'Bisnis'),
('B-036', 'Pratiwi Kusuma', 'Bisnis'),
('B-037', 'Qori Sandioriva', 'Bisnis'),
('B-038', 'Rizky Ramadhan', 'Bisnis'),
('B-039', 'Setyawan Budhi', 'Bisnis'),
('B-040', 'Taufan Nugraha', 'Bisnis'),
('B-041', 'Ahmad Subarjo', 'Bisnis'),
('B-042', 'Daniel Dwi Yunanto', 'Bisnis'),
('B-043', 'Muhammad Novi Azim', 'Bisnis'),
('B-044', 'Dedi Kurniawan', 'Bisnis'),
('B-045', 'Eko Prasetyo', 'Bisnis'),
('B-046', 'Muhammad Taufik', 'Bisnis'),
('B-047', 'M. Abduh Akbar', 'Bisnis'),
('B-048', 'Budi Hutomo', 'Bisnis'),
('B-049', 'Tangguh Sutanto Perwira', 'Bisnis'),
('B-050', 'Alqarana Triputri Anggraeni', 'Bisnis'),
('B-051', 'Naufal Ulfi Abdillah', 'Bisnis'),
('B-052', 'Todo Sitepu', 'Bisnis'),
('B-053', 'Puji Alamanda', 'Bisnis'),
('B-054', 'Dian Siswanto', 'Bisnis'),
('B-055', 'Prastyo Zainuri Linangkung', 'Bisnis'),
('B-056', 'Ahmad Mendung Hasyim', 'Bisnis'),
('B-057', 'Andini Pitria Yusuf', 'Bisnis'),
('B-058', 'Sola Nilam Tambunan', 'Bisnis'),
('B-059', 'Monang Ferdian', 'Bisnis'),
('B-060', 'Singgih Rachmansyah Kertiyoso', 'Bisnis'),
('B-061', 'Bella Handayani', 'Bisnis'),
('B-062', 'Meila Nadia Nopiyana', 'Bisnis'),
('B-063', 'Fajar Wicaksono', 'Bisnis'),
('B-064', 'Rizqi Safani', 'Bisnis'),
('B-065', 'Pugia Amalia', 'Bisnis'),
('B-066', 'Bernita Gishabiel', 'Bisnis'),
('B-067', 'Vikky Gracia', 'Bisnis'),
('B-068', 'Ningrum Adissa', 'Bisnis'),
('B-069', 'Mayang Safira', 'Bisnis'),
('B-070', 'Ully Marshellie', 'Bisnis'),
('B-071', 'Taufik Hidayat', 'Bisnis'),
('B-072', 'Hermansyah', 'Bisnis'),
('B-073', 'Muhammad Naseh', 'Bisnis'),
('B-074', 'Respati Luhur Susilo', 'Bisnis'),
('B-075', 'Abdul Aziz', 'Bisnis'),
('B-076', 'Ari Budi Santoso', 'Bisnis'),
('B-077', 'Harun Al Rasyid', 'Bisnis'),
('B-078', 'Ade Irawan', 'Bisnis'),
('B-079', 'Eko Trusilo Bandoko', 'Bisnis'),
('B-080', 'Suratin', 'Bisnis'),
('B-081', 'Nur Alif Pratama Putra', 'Bisnis'),
('B-082', 'Fahmi', 'Bisnis'),
('B-083', 'Guntur Ginanjar', 'Bisnis'),
('B-084', 'Yudi Maryanto', 'Bisnis'),
('B-085', 'Alfian Nur Cahyono', 'Bisnis'),
('B-086', 'Muhammad Okta Eka Fadillah', 'Bisnis'),
('B-087', 'Rizal Firdaus', 'Bisnis'),
('B-088', 'Fairuz Khansa Azzahra', 'Bisnis'),
('B-089', 'Citra Putri Aulia', 'Bisnis'),
('B-090', 'Dinda Nurmila', 'Bisnis'),
('B-091', 'Novitasari', 'Bisnis'),
('B-092', 'Aldi Mulya Rojali', 'Bisnis'),
('B-093', 'Indah Permata', 'Bisnis'),
('B-094', 'Brillian Setya Rahardika', 'Bisnis'),
('B-095', 'Dinda Nur Fadila', 'Bisnis'),
('B-096', 'Lulu Widiati', 'Bisnis'),
('B-097', 'Zahra Azzalfa', 'Bisnis'),
('B-098', 'Putri Maya Lestari', 'Bisnis'),
('B-099', 'Septi Manda', 'Bisnis'),
('B-100', 'Mira Nur Falla', 'Bisnis'),
('O-001', 'Harum Puspitarini', 'Operasional'),
('O-002', 'Windy Harum Handini', 'Operasional'),
('O-003', 'Adam Ramadhani', 'Operasional'),
('O-004', 'Okthania Pasha Pratama', 'Operasional'),
('O-005', 'Marjuki', 'Operasional'),
('O-006', 'Indah Puspasari', 'Operasional'),
('O-007', 'Rizqita Cahyamaharani', 'Operasional'),
('O-008', 'Diah Chiara Maharani', 'Operasional'),
('O-009', 'Gracianno Fernanda', 'Operasional'),
('O-010', 'Aditya Rahman', 'Operasional'),
('O-011', 'Lulu Dhia Komalasari', 'Operasional'),
('O-012', 'Nur Alya Falah', 'Operasional'),
('O-013', 'Alfa Dipathya', 'Operasional'),
('O-014', 'Dhiska Raytama Roosmaya', 'Operasional'),
('O-015', 'Bernard Dzulqarnain', 'Operasional'),
('O-016', 'Alif Widyantara', 'Operasional'),
('O-017', 'Yulia Fauziah', 'Operasional'),
('O-018', 'Widya Veronita Silalahi', 'Operasional'),
('O-019', 'Muhammad Fadhil Syam', 'Operasional'),
('O-020', 'Irfansyah', 'Operasional'),
('O-021', 'Suhendi', 'Operasional'),
('O-022', 'Kusmaja Supriyatna', 'Operasional'),
('O-023', 'Nanang', 'Operasional'),
('O-024', 'Olivia Setiawan', 'Operasional'),
('O-025', 'Pratiwi Rizky Fitriany', 'Operasional'),
('O-026', 'Rian Hidayat', 'Operasional'),
('O-027', 'Siti Aminah', 'Operasional'),
('O-028', 'Alfina Nur Yunita', 'Operasional'),
('O-029', 'Utami Dewi', 'Operasional'),
('O-030', 'Bima Noorchamarendra', 'Operasional'),
('O-031', 'Wahyu Hidayat', 'Operasional'),
('O-032', 'Raden Bima Irwanto', 'Operasional'),
('O-033', 'Novi Restu Wibawa', 'Operasional'),
('O-034', 'Silvia Pawitri', 'Operasional'),
('O-035', 'Bagus Adi', 'Operasional'),
('O-036', 'Cahya Ningrum', 'Operasional'),
('O-037', 'Syaiful Santoso', 'Operasional'),
('O-038', 'Endah Lestari', 'Operasional'),
('O-039', 'Fajar Siddiq', 'Operasional'),
('O-040', 'Wahyu Anwarudin', 'Operasional'),
('O-041', 'Larasati Putri', 'Operasional'),
('O-042', 'Muhammad Mahendra', 'Operasional'),
('O-043', 'Nanda Saputra', 'Operasional'),
('O-044', 'Hanny Hidayati', 'Operasional'),
('O-045', 'Dian Chairani Safitri', 'Operasional'),
('O-046', 'Kenny Izul Wardi', 'Operasional'),
('O-047', 'Muchamad Gandi Yusup', 'Operasional'),
('O-048', 'Khairian Refra', 'Operasional'),
('O-049', 'Gusti Alpiansyah', 'Operasional'),
('O-050', 'Meylissa Maylinda Marpaung', 'Operasional'),
('O-051', 'Setyo Lukman', 'Operasional'),
('O-052', 'Indah Safarani', 'Operasional'),
('O-053', 'Salma Dyah Eka Suci', 'Operasional'),
('O-054', 'Desva Pratiwi', 'Operasional'),
('O-055', 'Hikmawati Fhiqriah Gundar', 'Operasional'),
('O-056', 'Adnan Safroji', 'Operasional'),
('O-057', 'Tian Syauqi', 'Operasional'),
('O-058', 'Djian Gracia Sari', 'Operasional'),
('O-059', 'Mochamad Gunawan', 'Operasional'),
('O-060', 'Puji Hasri', 'Operasional'),
('O-061', 'Adiyanto Saputra', 'Operasional'),
('O-062', 'Muhammad Jamalludin', 'Operasional'),
('O-063', 'Maretta Deliza', 'Operasional'),
('O-064', 'Virly Arviyana Martha', 'Operasional'),
('O-065', 'Angelina Fadhila', 'Operasional'),
('O-066', 'Syifa Nur Pramudita', 'Operasional'),
('O-067', 'Sumarno', 'Operasional'),
('O-068', 'Agus Nandang', 'Operasional'),
('O-069', 'Jamaludin Gunawan', 'Operasional'),
('O-070', 'Jiel Novianti Siagian', 'Operasional'),
('O-071', 'Ruth Augustina Natasya', 'Operasional'),
('O-072', 'Kiagus Del Piero', 'Operasional'),
('O-073', 'Ahmad Fajri Maulana', 'Operasional'),
('O-074', 'Deza Alwi', 'Operasional'),
('O-075', 'Gusirman ula Alisana', 'Operasional'),
('O-076', 'Ade Sulistiawan', 'Operasional'),
('O-077', 'Rudiana', 'Operasional'),
('O-078', 'Rizki Yuliwan', 'Operasional'),
('O-079', 'Heni Novelita', 'Operasional'),
('O-080', 'Yaman', 'Operasional'),
('O-081', 'Winda Aulia Khafifa', 'Operasional'),
('O-082', 'Dinda Nur Febriani', 'Operasional'),
('O-083', 'Usep Saripudin', 'Operasional'),
('O-084', 'Budi Cahyono', 'Operasional'),
('O-085', 'Furqon Utomo', 'Operasional'),
('O-086', 'Maulana Setyo Hidayat', 'Operasional'),
('O-087', 'Vivih Manik', 'Operasional'),
('O-088', 'Achmad Badru', 'Operasional'),
('O-089', 'Muhammad Dede Salam', 'Operasional'),
('O-090', 'Mahrus Riswanda Salam', 'Operasional'),
('O-091', 'Putriani Dwi', 'Operasional'),
('O-092', 'Kadmari', 'Operasional'),
('O-093', 'M. Yusuf', 'Operasional'),
('O-094', 'Sari Sabrian Permata Intan', 'Operasional'),
('O-095', 'Nisa Sibulo', 'Operasional'),
('O-096', 'Rhayi Pratama', 'Operasional'),
('O-097', 'Jayanih', 'Operasional'),
('O-098', 'Claryn Khairani Labina', 'Operasional'),
('O-099', 'Siska Noviyanti', 'Operasional'),
('O-100', 'Shinta Harun', 'Operasional');

-- Selesai. Total karyawan: 200 (100 Bisnis + 100 Operasional)
-- Total kriteria: 16 (8 Bisnis + 8 Operasional)