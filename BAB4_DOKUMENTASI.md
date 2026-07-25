# BAB 4 — IMPLEMENTASI DAN PEMBAHASAN SISTEM

---

## 4.1 Implementasi Sistem

Sistem Pendukung Keputusan (SPK) Pemilihan Karyawan Terbaik Bank BRI KCP Arundina dibangun menggunakan bahasa pemrograman Java dengan antarmuka grafis berbasis Java Swing, database MySQL sebagai penyimpanan data, dan metode MOORA (*Multi-Objective Optimization on the basis of Ratio Analysis*) sebagai engine kalkulasi perankingan. Library pendukung yang digunakan adalah FlatLaf untuk tema modern UI dan OpenPDF untuk ekspor laporan PDF.

Sistem dirancang untuk dua peran pengguna: **Administrator** yang mengelola seluruh data dan konfigurasi sistem, serta **Pimpinan** yang berwenang menginput nilai kinerja karyawan dan melihat hasil perankingan.

---

## 4.2 Struktur Arsitektur Sistem

Sistem dibangun mengikuti pola arsitektur berlapis (*layered architecture*) yang terdiri dari lima lapisan:

| Lapisan | Package | Keterangan |
|---|---|---|
| **Presentation Layer** | `com.spkbri.ui` | Komponen antarmuka pengguna (GUI) |
| **Business Logic Layer** | `com.spkbri.core` | Engine kalkulasi metode MOORA |
| **Data Access Layer** | `com.spkbri.database` | Koneksi dan query ke database MySQL |
| **Model Layer** | `com.spkbri.model` | Representasi entitas data |
| **Utility Layer** | `com.spkbri.util` | Helper ekspor PDF dan Excel/CSV |

### Struktur Direktori Proyek

```
src/main/java/com/spkbri/
├── App.java                        ← Entry point aplikasi
├── core/
│   └── MooraEngine.java            ← Kalkulasi metode MOORA
├── database/
│   └── DatabaseHelper.java         ← Koneksi MySQL
├── model/
│   ├── Karyawan.java
│   ├── Kriteria.java
│   ├── Penilaian.java
│   ├── RankingResult.java
│   └── MooraCalculationResult.java
├── ui/
│   ├── LoginFrame.java             ← Halaman login (routing berdasarkan role)
│   ├── MainFrame.java              ← Window utama Administrator
│   ├── DashboardPanel.java
│   ├── KaryawanPanel.java
│   ├── KriteriaPanel.java
│   ├── PenilaianPanel.java
│   ├── ReportPanel.java
│   ├── PimpinanFrame.java          ← Window utama Pimpinan
│   ├── PimpinanKaryawanPanel.java  ← Lihat karyawan (read-only)
│   ├── PimpinanPenilaianPanel.java ← Input/update nilai kinerja
│   └── PimpinanRankingPanel.java   ← Lihat hasil ranking MOORA
└── util/
    └── ExportHelper.java
```

---

## 4.3 Implementasi Basis Data

Basis data yang digunakan adalah MySQL dengan nama database `spk_moora`. Skema basis data terdiri dari empat tabel dengan relasi sebagai berikut:

### Tabel 4.1 — Daftar Tabel Basis Data

| Nama Tabel | Fungsi |
|---|---|
| `users` | Menyimpan akun login beserta role (admin / pimpinan) |
| `karyawan` | Menyimpan data karyawan yang akan dinilai |
| `kriteria` | Menyimpan kriteria penilaian beserta bobot dan sifatnya |
| `penilaian` | Menyimpan nilai kinerja karyawan per kriteria |

### Skema Tabel users (dengan Role)

Kolom `role` ditambahkan untuk mendukung sistem multi-peran. Nilai yang valid adalah `admin` dan `pimpinan`.

```sql
CREATE TABLE IF NOT EXISTS `users` (
  `id_user`      INT          AUTO_INCREMENT PRIMARY KEY,
  `username`     VARCHAR(100) UNIQUE NOT NULL,
  `password`     VARCHAR(255) NOT NULL,
  `nama_lengkap` VARCHAR(255) NOT NULL,
  `role`         VARCHAR(20)  NOT NULL DEFAULT 'admin'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Akun Default Sistem

| Username | Password | Nama | Role |
|---|---|---|---|
| `admin` | `admin123` | Administrator SPK | `admin` |
| `pimpinan_bisnis` | `pimpinan123` | Pimpinan Divisi Bisnis | `pimpinan` |
| `pimpinan_ops` | `pimpinan123` | Pimpinan Divisi Operasional | `pimpinan` |

### Relasi Antar Tabel

Tabel `penilaian` berelasi ke tabel `karyawan` dan `kriteria` menggunakan *named foreign key constraint* dengan aturan `ON DELETE CASCADE ON UPDATE CASCADE`. Kombinasi `(id_karyawan, id_kriteria)` dijadikan *unique constraint* untuk mencegah duplikasi nilai.

```sql
CONSTRAINT `fk_penilaian_karyawan`
  FOREIGN KEY (`id_karyawan`) REFERENCES `karyawan`(`id_karyawan`)
  ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `fk_penilaian_kriteria`
  FOREIGN KEY (`id_kriteria`) REFERENCES `kriteria`(`id_kriteria`)
  ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `unique_penilaian`
  UNIQUE KEY (`id_karyawan`, `id_kriteria`)
```

### Koneksi Database

Koneksi database dikelola secara terpusat melalui kelas `DatabaseHelper`. Setiap operasi database membuka koneksi baru menggunakan `DriverManager.getConnection()` dengan konfigurasi:

| Parameter | Nilai |
|---|---|
| Host | `localhost` |
| Port | `3306` |
| Database | `spk_moora` |
| Driver | `com.mysql.cj.jdbc.Driver` |

```java
String url = "jdbc:mysql://localhost:3306/spk_moora"
           + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
return DriverManager.getConnection(url, USER, PASSWORD);
```

---

## 4.4 Implementasi Sistem Multi-Role

### 4.4.1 Konsep Role-Based Access Control

Sistem menerapkan mekanisme kendali akses berbasis peran (*Role-Based Access Control / RBAC*) sederhana dengan dua role:

| Role | Window | Hak Akses |
|---|---|---|
| `admin` | `MainFrame` | Kelola karyawan, kriteria, input penilaian, ekspor laporan |
| `pimpinan` | `PimpinanFrame` | Input/update nilai, lihat data karyawan (read-only), lihat ranking |

### 4.4.2 Routing Berdasarkan Role di LoginFrame

Setelah autentikasi berhasil, kolom `role` dibaca dari hasil query dan dijadikan dasar routing ke window yang sesuai:

```java
String namaLengkap = rs.getString("nama_lengkap");
String role        = rs.getString("role");

if ("pimpinan".equalsIgnoreCase(role)) {
    new PimpinanFrame(namaLengkap).setVisible(true);
} else {
    new MainFrame(namaLengkap).setVisible(true);
}
```

### 4.4.3 Perbandingan Hak Akses Per Modul

| Modul | Administrator | Pimpinan |
|---|---|---|
| Dashboard & statistik | ✅ | ❌ |
| Kelola data karyawan (CRUD) | ✅ | ❌ |
| Kelola data kriteria (CRUD) | ✅ | ❌ |
| Lihat daftar karyawan | ✅ | ✅ (read-only) |
| Input nilai kinerja | ✅ | ✅ |
| Update nilai yang sudah ada | ✅ | ✅ |
| Hapus data penilaian | ✅ | ❌ |
| Lihat hasil ranking MOORA | ✅ | ✅ |
| Ekspor laporan PDF/Excel | ✅ | ❌ |

---

## 4.5 Implementasi Metode MOORA

Kalkulasi perankingan dijalankan oleh kelas `MooraEngine` dengan metode statis `calculate(String divisi)`. Proses kalkulasi mengikuti tahapan baku metode MOORA sebagai berikut:

### Tahap 1 — Pembentukan Matriks Keputusan

Data nilai kinerja karyawan diambil dari tabel `penilaian` dengan *join* ke tabel `karyawan` berdasarkan divisi. Hasilnya disusun menjadi matriks keputusan X berukuran **m × n** (m karyawan, n kriteria). Nilai yang belum diinput diisi dengan `0.0`.

### Tahap 2 — Normalisasi Matriks

Setiap elemen matriks dinormalisasi menggunakan rumus MOORA:

$$x^*_{ij} = \frac{x_{ij}}{\sqrt{\sum_{i=1}^{m} x_{ij}^2}}$$

Penyebut dihitung per kolom kriteria. Jika penyebut bernilai nol maka diganti dengan `1.0` untuk menghindari pembagian dengan nol.

### Tahap 3 — Optimasi Berbobot

Nilai normalisasi dikalikan dengan bobot kriteria masing-masing:

$$x'_{ij} = x^*_{ij} \times w_j$$

### Tahap 4 — Perhitungan Nilai Optimasi Yi

$$Y_i = \sum_{j=1}^{g} x'_{ij} - \sum_{j=g+1}^{n} x'_{ij}$$

- $g$ = jumlah kriteria bertipe **Benefit**
- $n - g$ = jumlah kriteria bertipe **Cost**

### Tahap 5 — Perankingan

Karyawan diurutkan secara *descending* berdasarkan nilai Yi. Karyawan dengan Yi tertinggi menempati peringkat pertama sebagai rekomendasi karyawan terbaik.

```java
double terbiasa = nilaiMentah / pembagi;         // Normalisasi
double terbobot = terbiasa * kr.getBobot();      // Pembobotan
if ("Benefit".equalsIgnoreCase(kr.getSifat())) {
    sumBenefit += terbobot;
} else {
    sumCost += terbobot;
}
double score = sumBenefit - sumCost;             // Yi
```

---

## 4.6 Implementasi Antarmuka Pengguna

### 4.6.1 Halaman Login (`LoginFrame`)

Halaman login ditampilkan pertama kali saat aplikasi dijalankan. Desain dua panel: kiri berisi branding BRI, kanan berisi formulir username dan password. Setelah login berhasil, routing ditentukan otomatis berdasarkan kolom `role` di database.

### 4.6.2 Antarmuka Administrator (`MainFrame`)

Jendela utama berukuran **1024 × 680** piksel dengan sidebar navigasi tema gelap (`#0F172A`) dan `CardLayout` di area konten.

**Menu navigasi Administrator:**

| Menu | Panel |
|---|---|
| Dashboard (Beranda) | `DashboardPanel` |
| Data Karyawan | `KaryawanPanel` — CRUD lengkap |
| Data Kriteria | `KriteriaPanel` — CRUD per divisi |
| Input Penilaian | `PenilaianPanel` |
| Laporan & Ranking | `ReportPanel` + ekspor PDF/Excel |
| Logout | Kembali ke `LoginFrame` |

### 4.6.3 Dashboard (`DashboardPanel`)

Menampilkan tiga kartu statistik ringkasan dan pratinjau Top 3 karyawan terbaik per divisi dari hasil kalkulasi MOORA *real-time*. Dashboard diperbarui otomatis setiap ada perubahan data.

### 4.6.4 Kelola Data Karyawan (`KaryawanPanel`)

Operasi CRUD karyawan dengan field Kode Karyawan, Nama, Divisi. Dilengkapi pencarian live (`WHERE nama LIKE ? OR kode_karyawan LIKE ?`). Tombol Update dan Hapus hanya aktif saat ada baris terpilih.

### 4.6.5 Kelola Data Kriteria (`KriteriaPanel`)

Menggunakan `JTabbedPane` per divisi. Field: Kode Kriteria, Nama Kriteria, Sifat (Benefit/Cost), Bobot (desimal).

**Kriteria default Divisi Bisnis:**

| Kode | Nama Kriteria | Sifat | Bobot |
|---|---|---|---|
| C1 | Target Simpanan | Benefit | 0.40 |
| C2 | Target Kredit | Benefit | 0.30 |
| C3 | Akuisisi Rekening Baru | Benefit | 0.20 |

**Kriteria default Divisi Operasional:**

| Kode | Nama Kriteria | Sifat | Bobot |
|---|---|---|---|
| C1 | Kualitas Layanan / CS | Benefit | 0.30 |
| C2 | Ketelitian Transaksi | Benefit | 0.30 |
| C3 | Kehadiran / Kedisiplinan | Benefit | 0.20 |
| C4 | Bebas Komplain Nasabah | Benefit | 0.20 |

### 4.6.6 Input Penilaian (`PenilaianPanel`)

Tab per divisi. Alur: pilih karyawan → form nilai dinamis muncul per kriteria → input nilai 1–100 → simpan. Menggunakan upsert:

```sql
INSERT INTO penilaian (id_karyawan, id_kriteria, nilai)
VALUES (?, ?, ?)
ON DUPLICATE KEY UPDATE nilai = VALUES(nilai)
```

### 4.6.7 Laporan dan Ranking (`ReportPanel`)

Animasi 4 langkah proses MOORA, tabel ranking dengan kolom Rank/Kode Karyawan/Nama/Divisi/Score(Yi), panel kesimpulan karyawan terbaik. Ekspor ke PDF (OpenPDF) dan CSV (Java IO dengan UTF-8 BOM).

---

## 4.7 Implementasi Antarmuka Pimpinan (`PimpinanFrame`)

### 4.7.1 Deskripsi Umum

`PimpinanFrame` adalah window terpisah khusus role `pimpinan` berukuran **1024 × 680** piksel dengan sidebar biru gelap BRI (`#0A326E`) dan badge "PORTAL PIMPINAN". Navigasi hanya memuat tiga menu sesuai hak akses.

**Menu navigasi Pimpinan:**

| Menu | Panel | Keterangan |
|---|---|---|
| Data Karyawan | `PimpinanKaryawanPanel` | Read-only, pencarian tersedia |
| Input Penilaian | `PimpinanPenilaianPanel` | Input + update nilai, tanpa hapus |
| Hasil Ranking | `PimpinanRankingPanel` | Lihat ranking MOORA, tanpa ekspor |
| Logout | — | Kembali ke `LoginFrame` |

### 4.7.2 Data Karyawan — Read-Only (`PimpinanKaryawanPanel`)

Tabel karyawan seluruh divisi dengan fitur pencarian. Tidak ada tombol tambah, edit, maupun hapus. Terdapat label informasi yang menegaskan bahwa perubahan data dilakukan oleh Administrator.

### 4.7.3 Input Penilaian Pimpinan (`PimpinanPenilaianPanel`)

Alur yang sama dengan `PenilaianPanel` milik admin, namun dengan perbedaan:
- **Tidak ada tombol Hapus** — pimpinan hanya dapat menginput dan memperbarui nilai
- Nilai yang sudah pernah diinput ditampilkan dengan warna biru sebagai penanda visual
- Terdapat catatan informasi: *"Nilai yang sudah ada akan diperbarui otomatis"*
- Penyimpanan tetap menggunakan upsert `ON DUPLICATE KEY UPDATE`

### 4.7.4 Hasil Ranking Pimpinan (`PimpinanRankingPanel`)

- Animasi proses MOORA 4 langkah
- Highlight baris rank 1 dengan warna emas (latar `#FFF8DC`)
- Panel kesimpulan menampilkan nama, Kode Karyawan, dan skor Yi karyawan terbaik
- **Tidak ada tombol ekspor PDF/Excel** — hanya tombol "Perbarui Ranking"

---

## 4.8 User Flow Sistem

### 4.8.1 User Flow — Administrator

```
[MULAI]
    │
    ▼
┌──────────────────────────────┐
│  1. LOGIN                    │
│  - Input username & password │
│  - Cek role = 'admin'        │
│  - Buka MainFrame            │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  2. DASHBOARD                │
│  - Statistik & Top 3 ranking │
└──────────────┬───────────────┘
               │
       ┌───────┴────────┐
       ▼                ▼
┌─────────────┐  ┌─────────────┐
│ 3. KARYAWAN │  │ 4. KRITERIA │  ← Prasyarat
│  CRUD penuh │  │  CRUD penuh │
└──────┬──────┘  └──────┬──────┘
       └────────┬────────┘
                ▼
┌──────────────────────────────┐
│  5. INPUT PENILAIAN          │
│  - Pilih karyawan            │
│  - Input nilai 1–100         │
│  - Simpan (upsert)           │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  6. LAPORAN & RANKING        │
│  - Kalkulasi MOORA otomatis  │
│  - Ekspor PDF / Excel        │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  7. LOGOUT                   │
└──────────────────────────────┘
```

### 4.8.2 User Flow — Pimpinan

```
[MULAI]
    │
    ▼
┌──────────────────────────────┐
│  1. LOGIN                    │
│  - Input username & password │
│  - Cek role = 'pimpinan'     │
│  - Buka PimpinanFrame        │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  2. DATA KARYAWAN (read-only)│
│  - Lihat daftar karyawan     │
│  - Cari karyawan             │
│  - Tidak bisa ubah data      │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  3. INPUT PENILAIAN          │
│  - Pilih tab divisi          │
│  - Pilih karyawan            │
│  - Input / update nilai      │
│  - Tidak bisa hapus nilai    │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  4. HASIL RANKING            │
│  - Lihat hasil MOORA         │
│  - Rank 1 highlight emas     │
│  - Tidak ada fitur ekspor    │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  5. LOGOUT                   │
└──────────────────────────────┘
```

---

## 4.9 Alur Data (Data Flow)

```
Input Nilai Karyawan
(PenilaianPanel / PimpinanPenilaianPanel)
             │
             ▼ INSERT ... ON DUPLICATE KEY UPDATE
    Tabel penilaian (MySQL)
             │
             ▼
    MooraEngine.calculate(divisi)
             │
    ┌────────┴────────┐
    ▼                 ▼
Tabel karyawan   Tabel kriteria
(filter divisi)  (filter divisi)
    │                 │
    └────────┬────────┘
             ▼
    Matriks Keputusan [m × n]
             ▼
    Normalisasi: x*ij = xij / √(Σxij²)
             ▼
    Pembobotan: x'ij = x*ij × wj
             ▼
    Yi = Σ(Benefit) - Σ(Cost)
             ▼
    Urutkan Yi descending → Assign Rank
             │
    ┌────────┴─────────────────────────┐
    │                                  │
    ▼                                  ▼
ReportPanel (Admin)          PimpinanRankingPanel
- Tabel ranking              - Tabel ranking
- Panel rekomendasi          - Panel rekomendasi
- Ekspor PDF / CSV           - Highlight rank 1
    │
    ▼
DashboardPanel
- Top 3 preview
```

---

## 4.10 Implementasi Keamanan dan Validasi

### 4.10.1 Pencegahan SQL Injection

Seluruh query menggunakan `PreparedStatement` dengan parameter binding.

```java
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, username);
pstmt.setString(2, password);
```

### 4.10.2 Transaksi Atomik

Penyimpanan penilaian menggunakan transaksi dengan rollback otomatis:

```java
conn.setAutoCommit(false);
try {
    pstmt.executeBatch();
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
} finally {
    conn.setAutoCommit(true);
}
```

### 4.10.3 Validasi Input

| Lokasi | Validasi |
|---|---|
| Login | Username dan password tidak boleh kosong |
| Input Penilaian (Admin & Pimpinan) | Nilai harus angka desimal rentang 1–100 |
| Input Kriteria | Bobot harus berupa angka desimal valid |
| Input Karyawan | Kode Karyawan dan nama tidak boleh kosong |

### 4.10.4 Constraint Database

| Constraint | Tabel | Keterangan |
|---|---|---|
| `UNIQUE` | `users.username` | Mencegah duplikasi akun |
| `UNIQUE` | `karyawan.kode_karyawan` | Mencegah duplikasi Kode Karyawan |
| `UNIQUE KEY unique_penilaian` | `penilaian(id_karyawan, id_kriteria)` | Mencegah duplikasi penilaian |
| `ON DELETE CASCADE` | `penilaian → karyawan` | Hapus otomatis saat karyawan dihapus |
| `ON DELETE CASCADE` | `penilaian → kriteria` | Hapus otomatis saat kriteria dihapus |

---

## 4.11 Spesifikasi Teknologi

| Komponen | Teknologi / Versi |
|---|---|
| Bahasa Pemrograman | Java (JDK 11+) |
| Framework UI | Java Swing (native JDK) |
| Tema UI | FlatLaf (`FlatLightLaf`) |
| Database | MySQL 8.x |
| JDBC Driver | MySQL Connector/J (`com.mysql.cj.jdbc.Driver`) |
| Build Tool | Apache Maven |
| Ekspor PDF | OpenPDF (fork iText) |
| Ekspor Excel | CSV — Java IO (UTF-8 BOM) |
| Metode SPK | MOORA (*Multi-Objective Optimization on the basis of Ratio Analysis*) |
| Access Control | Role-Based: `admin` \| `pimpinan` |
