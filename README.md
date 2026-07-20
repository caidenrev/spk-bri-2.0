# Dokumentasi Proyek: SPK MOORA Karyawan Terbaik - Bank BRI KCP Arundina

## 📌 Ringkasan Proyek
**SPK MOORA Karyawan Terbaik** adalah aplikasi Sistem Pendukung Keputusan (SPK) yang dirancang untuk membantu Bank BRI KCP Arundina dalam menentukan karyawan terbaik berdasarkan kriteria-kriteria tertentu. Aplikasi ini menggunakan metode **MOORA (Multi-Objective Optimization on the basis of Ratio Analysis)** untuk melakukan perankingan.

Aplikasi ini dibangun menggunakan Java Swing dengan database MySQL (XAMPP) dan memiliki fitur manajemen data karyawan, kriteria, penilaian, serta ekspor laporan.

---

## 🛠️ Arsitektur & Teknologi
- **Bahasa Pemrograman:** Java 8
- **Framework UI:** Java Swing dengan **FlatLaf** (untuk tampilan modern)
- **Database:** MySQL
- **Metode SPK:** MOORA (Multi-Objective Optimization on the basis of Ratio Analysis)
- **Library Pihak Ketiga:**
  - `mysql-connector-j`: Konektivitas database MySQL.
  - `OpenPDF`: Pembuatan laporan dalam format PDF.
  - `Apache POI`: Dukungan untuk ekspor Excel (via CSV).
  - `JUnit`: Untuk pengujian unit.

---

## 📐 Struktur Folder
```text
src/main/java/com/spkbri/
├── core/
│   └── MooraEngine.java            # Logika perhitungan metode MOORA
├── database/
│   └── DatabaseHelper.java         # Manajemen koneksi database MySQL
├── model/
│   ├── Karyawan.java               # Model data Karyawan
│   ├── Kriteria.java               # Model data Kriteria
│   ├── Penilaian.java              # Model data Penilaian
│   └── RankingResult.java          # Model hasil perankingan
├── ui/
│   ├── LoginFrame.java             # Layar autentikasi pengguna & routing role
│   ├── MainFrame.java              # Window utama Administrator (Dashboard & Navigasi)
│   ├── DashboardPanel.java         # Panel ringkasan data
│   ├── KaryawanPanel.java          # Manajemen data karyawan (CRUD)
│   ├── KriteriaPanel.java          # Manajemen data kriteria (CRUD)
│   ├── PenilaianPanel.java         # Input nilai karyawan
│   ├── ReportPanel.java            # Tampilan ranking & ekspor laporan
│   ├── PimpinanFrame.java          # Window utama Pimpinan
│   ├── PimpinanKaryawanPanel.java  # Daftar karyawan (Read-Only)
│   ├── PimpinanPenilaianPanel.java # Input & update nilai kinerja
│   └── PimpinanRankingPanel.java   # Lihat hasil ranking MOORA (tanpa ekspor)
└── util/
    └── ExportHelper.java           # Utilitas ekspor data ke PDF dan CSV
```

---

## 🧮 Implementasi Metode MOORA
Metode MOORA dalam aplikasi ini bekerja dengan langkah-langkah berikut:

1. **Pembentukan Matriks Keputusan:** Mengumpulkan nilai setiap karyawan untuk setiap kriteria pada divisi yang dipilih.
2. **Normalisasi:** Mengubah nilai mentah menjadi nilai ternormalisasi menggunakan rumus:
   $$x'_{ij} = \frac{x_{ij}}{\sqrt{\sum_{i=1}^n x_{ij}^2}}$$
3. **Optimasi Terbobot:** Mengalikan nilai ternormalisasi dengan bobot kriteria:
   $$v_{ij} = x'_{ij} \cdot w_j$$
4. **Perhitungan Nilai Akhir ($Y_i$):** Menghitung selisih antara jumlah nilai kriteria *Benefit* dan kriteria *Cost*:
   $$Y_i = \sum(\text{Benefit}) - \sum(\text{Cost})$$
5. **Perankingan:** Mengurutkan karyawan berdasarkan nilai $Y_i$ tertinggi.

---

## 🗄️ Skema Database
Aplikasi menggunakan database MySQL (`spk_moora`) dengan tabel sebagai berikut:

- **`users`**: Menyimpan data akun login beserta role (`admin` / `pimpinan`).
- **`karyawan`**: Menyimpan data karyawan (Kode Karyawan, nama, divisi).
- **`kriteria`**: Menyimpan kriteria penilaian (kode, nama, sifat [Benefit/Cost], bobot, divisi).
- **`penilaian`**: Menyimpan nilai yang diberikan untuk setiap karyawan per kriteria (kombinasi `id_karyawan` dan `id_kriteria` bersifat unik).

**Divisi yang didukung:**
- Bisnis
- Operasional

---

## 🚀 Fitur Utama
- **Sistem Multi-Role:** Akses terpisah untuk **Administrator** (kelola data karyawan/kriteria, input nilai, ekspor laporan) dan **Pimpinan** (lihat karyawan, input/update nilai, lihat hasil perankingan).
- **Manajemen Karyawan:** Tambah, edit, dan hapus data karyawan per divisi (hanya Admin).
- **Manajemen Kriteria:** Pengaturan bobot dan sifat kriteria (Benefit/Cost) per divisi (hanya Admin).
- **Input Penilaian:** Pemberian skor (0-100) untuk setiap karyawan berdasarkan kriteria yang ada (Admin & Pimpinan).
- **Perankingan Otomatis:** Menghitung karyawan terbaik secara real-time menggunakan engine MOORA.
- **Ekspor Laporan:** Mengunduh hasil perankingan dalam format **PDF** dan **CSV** (hanya Admin).
- **Autentikasi:** Sistem login untuk mengamankan akses data dengan routing otomatis sesuai peran.

---

## ⚙️ Cara Menjalankan Aplikasi
1. Pastikan database MySQL (`spk_moora`) sudah diimpor menggunakan file `schema.sql`.
2. Pastikan JDK 8 atau lebih baru sudah terinstal.
3. Gunakan Maven untuk mengunduh dependensi:
   ```bash
   mvn clean install
   ```
4. Jalankan kelas utama `com.spkbri.App`.
5. Gunakan salah satu akun default berikut untuk login:
   - **Administrator (Full CRUD & Ekspor):**
     - **Username:** `admin`
     - **Password:** `admin123`
   - **Pimpinan Divisi Bisnis (Read-Only & Input Nilai):**
     - **Username:** `pimpinan_bisnis`
     - **Password:** `pimpinan123`
   - **Pimpinan Divisi Operasional (Read-Only & Input Nilai):**
     - **Username:** `pimpinan_ops`
     - **Password:** `pimpinan123`
