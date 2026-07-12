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
│   └── MooraEngine.java        # Logika perhitungan metode MOORA
├── database/
│   └── DatabaseHelper.java     # Manajemen koneksi dan inisialisasi database SQLite
├── model/
│   ├── Karyawan.java           # Model data Karyawan
│   ├── Kriteria.java           # Model data Kriteria
│   ├── Penilaian.java          # Model data Penilaian
│   └── RankingResult.java      # Model hasil perankingan
├── ui/
│   ├── MainFrame.java          # Frame utama aplikasi (Dashboard & Navigasi)
│   ├── LoginFrame.java         # Layar autentikasi pengguna
│   ├── DashboardPanel.java     # Panel ringkasan data
│   ├── KaryawanPanel.java      # Manajemen data karyawan
│   ├── KriteriaPanel.java      # Manajemen data kriteria
│   ├── PenilaianPanel.java     # Input nilai karyawan
│   └── ReportPanel.java        # Tampilan rankinlg dan ekspor laporan
└── util/
    └── ExportHelper.java       # Utilitas ekspor data ke PDF dan CSV
```

---s

## 🧮 Implementasi Metode MOORA
Metode MOORA dalam aplikasi ini bekerja dengan langkah-langkah berikut:

1. **Pembentukan Matriks Keputusan:** Mengumpulkan nilai setiap karyawan untuk setiap kriteria pada divisi yang dipilih.
2. **Normalisasi:** Mengubah nilai mentah menjadi nilai ternormalisasi menggunakan rumus:
   $$x'_{ij} = \frac{x_{ij}}{\sqrt{\sum_{i=1}^n x_{ij}^2}}$$
3. **Optimasi Terbobot:** Mengalikan nilai ternormalisasi dengan bobot kriteria:
   $$v_{ij} = x'_{ij} \cdot w_j$$
4. **Perhitungan Nilai Akhir ($Y_i$):** Menghitung selisih antara jumlah nilai kriteria *Benefit* dan kriteria *Cost*:
   $$Y_i = \sum(	ext{Benefit}) - \sum(	ext{Cost})$$
5. **Perankingan:** Mengurutkan karyawan berdasarkan nilai $Y_i$ tertinggi.

---

## 🗄️ Skema Database
Aplikasi menggunakan database SQLite dengan tabel sebagai berikut:

- **`users`**: Menyimpan data akun administrator (username, password, nama).
- **`karyawan`**: Menyimpan data karyawan (NIK, nama, divisi).
- **`kriteria`**: Menyimpan kriteria penilaian (kode, nama, sifat [Benefit/Cost], bobot, divisi).
- **`penilaian`**: Menyimpan nilai yang diberikan untuk setiap karyawan per kriteria.

**Divisi yang didukung:**
- Bisnis
- Operasional

---

## 🚀 Fitur Utama
- **Manajemen Karyawan:** Tambah, edit, dan hapus data karyawan per divisi.
- **Manajemen Kriteria:** Pengaturan bobot dan sifat kriteria (Benefit/Cost).
- **Input Penilaian:** Pemberian skor untuk setiap karyawan berdasarkan kriteria yang ada.
- **Perankingan Otomatis:** Menghitung karyawan terbaik secara real-time menggunakan engine MOORA.
- **Ekspor Laporan:** Mengunduh hasil perankingan dalam format **PDF** dan **CSV**.
- **Autentikasi:** Sistem login untuk mengamankan akses data.

---

## ⚙️ Cara Menjalankan Aplikasi
1. Pastikan JDK 8 atau lebih baru sudah terinstal.
2. Gunakan Maven untuk mengunduh dependensi:
   ```bash
   mvn clean install
   ```
3. Jalankan kelas utama `com.spkbri.App`.
4. Gunakan akun default untuk login pertama kali:
   - **Username:** `admin`
   - **Password:** `admin123`
