# MANUAL BOOK (PANDUAN PENGGUNAAN APLIKASI)
## Sistem Pendukung Keputusan (SPK) Pemilihan Karyawan Terbaik
### Bank BRI KCP Arundina — Metode MOORA (Multi-Objective Optimization on the basis of Ratio Analysis)

---

## 1. PENDAHULUAN
Aplikasi SPK Pemilihan Karyawan Terbaik Bank BRI KCP Arundina dirancang untuk membantu pihak manajemen dalam menilai, membandingkan, dan mengurutkan kinerja karyawan berdasarkan kriteria-kriteria objektif. Sistem ini menggunakan metode **MOORA** untuk meminimalisasi subjektivitas penilaian dengan menyajikan transparansi seluruh proses perhitungan matriks secara bertahap.

Aplikasi ini mendukung sistem **Multi-Role**:
1.  **Administrator**: Mengelola data karyawan, data kriteria beserta bobotnya, menginput penilaian berbasis matriks, melihat laporan, dan mengekspor laporan ke format PDF dan Excel/CSV.
2.  **Pimpinan**: Memiliki hak akses khusus untuk melihat data karyawan (read-only), menginput/mengupdate nilai kinerja berbasis matriks, dan memantau hasil kalkulasi ranking MOORA lengkap dengan visualisasi matriksnya.

---

## 2. PRASYARAT & INSTALASI SISTEM
Sebelum menjalankan aplikasi, pastikan komputer Anda telah memenuhi persyaratan berikut:

1.  **Java Runtime Environment (JRE)**: Versi 11 atau yang lebih baru (JRE 17+ sangat direkomendasikan).
2.  **Database Server**: MySQL (menggunakan Laragon, XAMPP, atau MySQL installer mandiri).
3.  **Pengaturan Database**:
    *   Buat database baru dengan nama `spk_moora` pada MySQL Server Anda.
    *   Impor berkas [schema.sql](file:///c:/Users/Pongo/spk-bri-2.0/schema.sql) untuk menginisialisasi tabel (`users`, `karyawan`, `kriteria`, `penilaian`) dan data awal (*seed data*).
4.  **Akun Login Default**:
    *   **Administrator**: Username: `admin` | Password: `admin123`
    *   **Pimpinan Bisnis**: Username: `pimpinan_bisnis` | Password: `pimpinan123`
    *   **Pimpinan Operasional**: Username: `pimpinan_ops` | Password: `pimpinan123`

---

## 3. ALUR PENGGUNAAN SISTEM (STEP-BY-STEP)

### 3.1 Halaman Login
1.  Jalankan aplikasi hingga muncul jendela **Login**.
2.  Masukkan **Username** dan **Password** sesuai dengan peran Anda.
3.  Klik **Masuk** atau tekan tombol Enter. Sistem secara otomatis mengarahkan Anda ke dashboard/portal yang sesuai berdasarkan hak akses.

---

### 3.2 Panduan untuk Administrator

#### A. Menu Dashboard (Beranda)
*   Menampilkan rangkuman statistik berupa total jumlah karyawan aktif serta jumlah kriteria pada masing-masing divisi (Bisnis & Operasional).
*   Menampilkan panel **Top 3 Karyawan Terbaik** sementara untuk masing-masing divisi.

#### B. Menu Data Karyawan
*   **Menambah Karyawan**: Isi form di panel kiri (Kode Karyawan, Nama, dan Divisi), lalu klik **Simpan**.
*   **Mengubah Karyawan**: Klik salah satu baris karyawan pada tabel kanan, ubah informasi pada form kiri, lalu klik **Edit**.
*   **Menghapus Karyawan**: Klik baris karyawan pada tabel, lalu klik **Hapus**. Data penilaian karyawan tersebut akan otomatis terhapus (*cascade*).
*   **Pencarian**: Ketik nama atau kode karyawan pada kolom pencarian di atas tabel untuk memfilter data secara cepat.

#### C. Menu Data Kriteria
*   **Menambah Kriteria**: Isi Kode Kriteria (contoh: C1), Nama Kriteria, Sifat (Benefit/Cost), dan Bobot (desimal, contoh: 0.25) pada form kiri, lalu klik **Simpan**.
*   **Mengubah/Menghapus Kriteria**: Pilih kriteria pada tabel kanan, lalu gunakan tombol **Edit** atau **Hapus** pada form kiri.

#### D. Menu Input Penilaian (Model Matriks)
Menu ini didesain persis seperti matriks pembanding untuk memudahkan input massal:
1.  Klik **CARI DATA KARYAWAN** di panel kiri.
2.  Centang nama-nama karyawan yang ingin dinilai (maksimal 5 karyawan sekaligus), lalu klik **PILIH**.
3.  Baris pada **Matriks Keputusan (Skala 1-100)** di sebelah kanan akan aktif secara otomatis untuk karyawan yang dipilih.
4.  Ketik nilai performa untuk masing-masing kriteria dengan ketentuan **skala 1 sampai 100** (misal: 85).
5.  *(Opsional)* Klik **MULAI HITUNG** untuk memicu kalkulasi matriks normalisasi lokal secara realtime pada grid **Matriks Normalisasi**.
6.  Klik **SIMPAN** untuk merekam seluruh nilai keputusan ke database.
7.  Tabel **Riwayat Perankingan** di bagian bawah kanan akan terisi secara otomatis memperlihatkan rekapitulasi nilai dan skor akhir Yi.

#### E. Menu Laporan
Menu Laporan sekarang dipisah menjadi submenu berdasarkan divisi (Bisnis dan Operasional) dan jenis laporan:
*   **Laporan Data Karyawan**: Menampilkan daftar karyawan dan kriteria yang relevan untuk divisi tersebut.
*   **Perhitungan MOORA**: Menyajikan hasil evaluasi komprehensif MOORA per divisi dalam 3 sub-tab:
    1.  **Matriks Keputusan**: Nilai rating skala 1-100 yang tersimpan.
    2.  **Matriks Normalisasi**: Hasil normalisasi rasio pembagi kuadrat.
    3.  **Normalisasi Terbobot**: Hasil kali matriks normalisasi dengan bobot kriteria.
*   **Hasil Ranking**: Peringkat karyawan dari skor optimasi tertinggi ($Yi$). *Catatan: Jika penilaian belum diinputkan (skor 0), sistem tidak akan menampilkan rekomendasi terbaik dan akan meminta pengguna untuk mengisi penilaian terlebih dahulu.*
*   **Ekspor Dokumen**: Tersedia tombol **Cetak PDF** dan **Cetak Excel** pada masing-masing submenu laporan untuk mengunduh laporan fisik.

#### F. Menu Manajemen Akun
*   Menampilkan daftar pengguna aplikasi (Admin & Pimpinan).
*   **Menambah Akun**: Isi Username, Password, Nama Lengkap, dan pilih Role (admin/pimpinan), lalu klik **Simpan**.
*   **Mengubah Akun**: Pilih akun pada tabel, ubah detail di sebelah kiri (biarkan password kosong jika tidak ingin mengubah), lalu klik **Update**.
*   **Menghapus Akun**: Pilih akun pada tabel, lalu klik **Hapus**.

---

### 3.3 Panduan untuk Pimpinan

#### A. Menu Data Karyawan
*   Menampilkan daftar karyawan yang terdaftar secara *read-only* (hanya bisa dicari dan dilihat, tanpa tombol tambah/edit/hapus untuk menjaga integritas data master).

#### B. Menu Input Penilaian
*   Memiliki fungsi yang sama dengan Administrator. Pimpinan dapat memilih hingga 5 karyawan, mengisi matriks keputusan skala 1-100, melakukan simulasi kalkulasi matriks normalisasi, serta menyimpannya ke database.

#### C. Menu Hasil Ranking
*   Menampilkan visualisasi 4 sub-tab perhitungan MOORA secara transparan beserta kesimpulan rekomendasi karyawan terbaik di panel bawah (tanpa tombol cetak/ekspor).
*   Peringkat pertama (alternatif terbaik) akan disorot secara otomatis menggunakan **warna emas muda** untuk kenyamanan visual pimpinan.

---

## 4. PENJELASAN FORMULA PERHITUNGAN MOORA
Proses perhitungan di dalam mesin aplikasi (`MooraEngine.java`) berjalan secara otomatis mengikuti kaidah matematika berikut:

1.  **Normalisasi Matriks**:
    $$x^*_{ij} = \frac{x_{ij}}{\sqrt{\sum_{i=1}^{m} x_{ij}^2}}$$
    Membagi setiap nilai sel keputusan ($x_{ij}$) dengan akar kuadrat dari jumlah total kuadrat nilai alternatif pada kolom kriteria tersebut.
2.  **Mengalikan Bobot**:
    $$x'_{ij} = x^*_{ij} \times w_j$$
    Mengalikan nilai ternormalisasi dengan bobot kriteria ($w_j$) masing-masing.
3.  **Menghitung Nilai Optimasi Yi**:
    $$Y_i = \sum_{j=1}^{g} x'_{ij} - \sum_{j=g+1}^{n} x'_{ij}$$
    Menjumlahkan seluruh nilai terbobot kriteria bertipe **Benefit** lalu dikurangi dengan jumlah nilai terbobot kriteria bertipe **Cost**. Alternatif dengan nilai Yi paling tinggi adalah peringkat 1 (Karyawan Terbaik).
