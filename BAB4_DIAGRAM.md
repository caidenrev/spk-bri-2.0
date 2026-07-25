# Diagram UML — SPK Pemilihan Karyawan Terbaik Bank BRI KCP Arundina
# Metode MOORA | Sistem Multi-Role: Administrator & Pimpinan

---

## 1. Use Case Diagram

Menggambarkan interaksi dua aktor (Administrator dan Pimpinan) dengan seluruh fitur sistem.

```plantuml
@startuml UseCaseDiagram
skinparam packageStyle rectangle
skinparam actorStyle awesome
skinparam usecase {
  BackgroundColor #EBF5FB
  BorderColor #2980B9
  ArrowColor #2C3E50
}
left to right direction
title Use Case Diagram — SPK MOORA BRI KCP Arundina (Multi-Role)

actor "Administrator" as admin
actor "Pimpinan" as pimpinan

rectangle "Sistem Pendukung Keputusan MOORA" {
  usecase "Login" as UC1
  usecase "Logout" as UC2

  usecase "Melihat Dashboard" as UC3
  usecase "Kelola Data Karyawan" as UC4
  usecase "Tambah Karyawan" as UC4a
  usecase "Edit Karyawan" as UC4b
  usecase "Hapus Karyawan" as UC4c
  usecase "Cari Karyawan" as UC4d
  usecase "Kelola Data Kriteria" as UC5
  usecase "Tambah Kriteria" as UC5a
  usecase "Edit Kriteria" as UC5b
  usecase "Hapus Kriteria" as UC5c
  usecase "Input Penilaian Karyawan" as UC6
  usecase "Lihat Laporan Ranking MOORA" as UC7
  usecase "Ekspor Laporan PDF" as UC8
  usecase "Ekspor Laporan Excel/CSV" as UC9

  usecase "Lihat Data Karyawan (Read-Only)" as UC10
  usecase "Input & Update Nilai Kinerja" as UC11
  usecase "Lihat Hasil Ranking" as UC12
}

admin --> UC1
admin --> UC2
admin --> UC3
admin --> UC4
admin --> UC5
admin --> UC6
admin --> UC7

pimpinan --> UC1
pimpinan --> UC2
pimpinan --> UC10
pimpinan --> UC11
pimpinan --> UC12

UC4 <.. UC4a : <<include>>
UC4 <.. UC4b : <<include>>
UC4 <.. UC4c : <<include>>
UC4 <.. UC4d : <<include>>
UC5 <.. UC5a : <<include>>
UC5 <.. UC5b : <<include>>
UC5 <.. UC5c : <<include>>
UC7 <.. UC8 : <<extend>>
UC7 <.. UC9 : <<extend>>
note "Setelah login berhasil:\nadmin → Dashboard\npimpinan → Data Karyawan" as NLogin
UC1 .. NLogin

@enduml
```

---

## 2. Activity Diagram — Login dengan Routing Role

Menggambarkan alur autentikasi dan routing berdasarkan role pengguna.

```plantuml
@startuml ActivityLogin
skinparam activityBackgroundColor #EBF5FB
skinparam activityBorderColor #2980B9
skinparam arrowColor #2C3E50
title Activity Diagram — Login & Routing Role

start
:Buka Aplikasi;
:Tampil Halaman Login;
:Input Username dan Password;
if (Username/Password kosong?) then (Ya)
  :Tampil pesan "Tidak boleh kosong";
  :Kembali ke form login;
else (Tidak)
  :Query ke tabel users\nSELECT * FROM users\nWHERE username=? AND password=?;
  if (Kredensial valid?) then (Ya)
    :Baca kolom role dari ResultSet;
    if (role = 'pimpinan'?) then (Ya)
      :Tutup LoginFrame;
      :Buka PimpinanFrame;
      :Tampil halaman Data Karyawan;
      stop
    else (Tidak / role = 'admin')
      :Tutup LoginFrame;
      :Buka MainFrame;
      :Tampil Dashboard;
      stop
    endif
  else (Tidak)
    :Tampil pesan\n"Username atau password salah";
    :Kembali ke form login;
  endif
endif
@enduml
```

---

## 3. Activity Diagram — Kelola Data Karyawan (Admin)

```plantuml
@startuml ActivityKaryawan
skinparam activityBackgroundColor #EBF5FB
skinparam activityBorderColor #2980B9
skinparam arrowColor #2C3E50
title Activity Diagram — Kelola Data Karyawan (Administrator)

start
:Pilih menu "Data Karyawan";
:Tampil daftar karyawan dari database;

fork
  :TAMBAH KARYAWAN;
  :Input Kode Karyawan, Nama, Divisi;
  if (Field kosong?) then (Ya)
    :Tampil pesan warning;
  else (Tidak)
    :INSERT INTO karyawan;
    if (Kode Karyawan sudah ada?) then (Ya)
      :Tampil pesan error duplikat Kode Karyawan;
    else (Tidak)
      :Data berhasil disimpan;
      :Refresh tabel & Dashboard;
    endif
  endif
fork again
  :EDIT KARYAWAN;
  :Pilih baris di tabel;
  :Data tampil di form;
  :Ubah data yang diperlukan;
  :Klik "Update";
  :UPDATE karyawan SET ...\nWHERE id_karyawan=?;
  :Refresh tabel & Dashboard;
fork again
  :HAPUS KARYAWAN;
  :Pilih baris di tabel;
  :Konfirmasi dialog hapus;
  if (Konfirmasi = Ya?) then (Ya)
    :DELETE FROM karyawan\nWHERE id_karyawan=?;
    :Data penilaian terhapus otomatis\n(ON DELETE CASCADE);
    :Refresh tabel & Dashboard;
  else (Tidak)
    :Batalkan operasi;
  endif
fork again
  :CARI KARYAWAN;
  :Input kata kunci;
  :SELECT * FROM karyawan\nWHERE nama LIKE ? OR kode_karyawan LIKE ?;
  :Tampil hasil pencarian;
end fork
stop
@enduml
```

---

## 4. Activity Diagram — Input Penilaian (Admin & Pimpinan)

```plantuml
@startuml ActivityPenilaian
skinparam activityBackgroundColor #EBF5FB
skinparam activityBorderColor #2980B9
skinparam arrowColor #2C3E50
title Activity Diagram — Input Penilaian Karyawan (Admin & Pimpinan)

start
:Pilih menu "Input Penilaian";
:Pilih tab divisi (Bisnis / Operasional);
:Klik "CARI DATA KARYAWAN";
:Pilih maksimal 5 karyawan pada dialog;
:Load nilai yang sudah ada dari database;
:Aktifkan baris matriks keputusan terkait;
:Input nilai kinerja pada grid (skala 1-100);

fork
  :Klik "MULAI HITUNG";
  :Kalkulasi normalisasi lokal;
  :Tampilkan hasil di grid matriks normalisasi;
fork again
  :Validasi semua nilai (skala 1-100);
  if (Valid?) then (Ya)
    :Mulai transaksi database;
    :INSERT/UPDATE nilai ke tabel penilaian;
    :COMMIT transaksi;
    :Tampil pesan sukses;
    :Refresh tabel riwayat di bawah;
  else (Tidak)
    :Tampil pesan "Nilai harus 1-100";
  endif
end fork
stop
@enduml
```

---

## 5. Activity Diagram — Kalkulasi MOORA & Laporan Ranking

```plantuml
@startuml ActivityMoora
skinparam activityBackgroundColor #FEF9E7
skinparam activityBorderColor #F39C12
skinparam arrowColor #2C3E50
title Activity Diagram — Kalkulasi MOORA & Laporan Ranking

start
:Pilih menu "Laporan & Ranking"\natau "Hasil Ranking" (Pimpinan);
:Pilih tab divisi (Bisnis / Operasional);
:Tampil animasi proses kalkulasi;

:TAHAP 1: Ambil data karyawan;
:TAHAP 2: Ambil data kriteria;
:TAHAP 3: Ambil data penilaian;

if (Data tersedia?) then (Tidak)
  :Tampil pesan "Belum ada data";
  stop
else (Ya)
  :Bentuk matriks keputusan [m x n];
  :NORMALISASI: x*ij = xij / sqrt(Σxij²);
  :PEMBOBOTAN: x'ij = x*ij × wj;
  :HITUNG Yi = Σ(Benefit) - Σ(Cost);
  :Urutkan Yi descending;
  :Assign Rank (1, 2, 3, ...);
  :Tampil Tab Langkah Perhitungan\n(Matriks Keputusan, Normalisasi, Normalisasi Terbobot, Hasil Akhir);
  :Tampil panel rekomendasi;
  :Cek role pengguna;

  if (Role = Admin?) then (Ya)
    :Tersedia tombol\n"Cetak PDF" dan "Cetak Excel";
    fork
      :Klik "Cetak PDF";
      :ExportHelper.exportToPDF();
      :File PDF tersimpan;
    fork again
      :Klik "Cetak Excel";
      :ExportHelper.exportToCSV();
      :File CSV tersimpan;
    end fork
  else (Tidak / Pimpinan)
    :Tampil highlight rank 1 (warna emas);
    :Tidak ada tombol ekspor;
  endif
endif
stop
@enduml
```

---

## 6. Sequence Diagram — Login dengan Routing Role

```plantuml
@startuml SequenceLogin
skinparam sequenceArrowThickness 2
skinparam sequenceParticipantBackgroundColor #EBF5FB
skinparam sequenceParticipantBorderColor #2980B9
title Sequence Diagram — Login & Role Routing

actor Pengguna as user
participant "LoginFrame" as login
participant "DatabaseHelper" as db
database "MySQL\n(tabel users)" as mysql
participant "MainFrame\n(Admin)" as mainframe
participant "PimpinanFrame\n(Pimpinan)" as pimpinanframe

user -> login : Klik "Masuk"
activate login

login -> login : Validasi input tidak kosong
alt Input kosong
  login --> user : Pesan error "Tidak boleh kosong"
else Input terisi
  login -> db : getConnection()
  activate db
  db --> login : Connection
  deactivate db

  login -> mysql : "SELECT * FROM users\nWHERE username=? AND password=?"
  activate mysql
  mysql --> login : "ResultSet (nama_lengkap, role)"
  deactivate mysql

  alt Kredensial tidak valid
    login --> user : Pesan "Username atau password salah"
  else role = 'admin'
    login -> login : dispose()
    login -> mainframe : "new MainFrame(namaLengkap)"
    activate mainframe
    mainframe --> user : Tampil Dashboard Admin
    deactivate mainframe
  else role = 'pimpinan'
    login -> login : dispose()
    login -> pimpinanframe : "new PimpinanFrame(namaLengkap)"
    activate pimpinanframe
    pimpinanframe --> user : Tampil Portal Pimpinan
    deactivate pimpinanframe
  end
end
deactivate login
@enduml
```

---

## 7. Sequence Diagram — Kalkulasi MOORA

```plantuml
@startuml SequenceMoora
skinparam sequenceArrowThickness 2
skinparam sequenceParticipantBackgroundColor #FEF9E7
skinparam sequenceParticipantBorderColor #F39C12
title Sequence Diagram — Kalkulasi MOORA

actor Pengguna as user
participant "ReportPanel /\nPimpinanRankingPanel" as report
participant "MooraEngine" as engine
participant "DatabaseHelper" as db
database "MySQL" as mysql

user -> report : Buka menu Ranking
activate report

report -> report : "Jalankan animasi proses (Timer 350ms)"
report -> engine : "calculate(divisi)"
activate engine

engine -> db : getConnection()
activate db
db --> engine : Connection
deactivate db

engine -> mysql : "SELECT * FROM karyawan WHERE divisi=?"
activate mysql
mysql --> engine : "List<Karyawan>"
deactivate mysql

engine -> mysql : "SELECT * FROM kriteria WHERE divisi=?"
activate mysql
mysql --> engine : "List<Kriteria>"
deactivate mysql

engine -> mysql : "SELECT penilaian JOIN karyawan WHERE divisi=?"
activate mysql
mysql --> engine : "Data penilaian"
deactivate mysql

engine -> engine : "Bentuk matriks keputusan [m x n]"

loop setiap kriteria j
  engine -> engine : "pembagi = sqrt(Σxij²)"
end

loop setiap karyawan i
  engine -> engine : "x*ij = xij / pembagi"
  engine -> engine : "x'ij = x*ij × wj"
  engine -> engine : "Yi += x'ij (Benefit) / Yi -= x'ij (Cost)"
end

engine -> engine : Sort descending by Yi
engine -> engine : Assign Rank
engine --> report : "MooraCalculationResult"
deactivate engine

report -> report : Render tab matriks keputusan, normalisasi, normalisasi terbobot, dan hasil ranking
report -> report : Tampil panel rekomendasi

alt Role = Admin (ReportPanel)
  report --> user : Tampil tab langkah perhitungan + tombol ekspor PDF/Excel
else Role = Pimpinan (PimpinanRankingPanel)
  report --> user : Tampil tab langkah perhitungan + highlight rank 1 emas
end
deactivate report
@enduml
```

---

## 8. Sequence Diagram — Input Penilaian (Pimpinan)

```plantuml
@startuml SequencePenilaianPimpinan
skinparam sequenceArrowThickness 2
skinparam sequenceParticipantBackgroundColor #EBF5FB
skinparam sequenceParticipantBorderColor #2980B9
title Sequence Diagram — Input Penilaian oleh Pimpinan

actor Pimpinan as user
participant "PimpinanPenilaianPanel" as panel
participant "DatabaseHelper" as db
database "MySQL" as mysql

user -> panel : Pilih tab divisi
user -> panel : Klik "CARI DATA KARYAWAN"
activate panel
user -> panel : Pilih maksimal 5 karyawan dan klik "PILIH"

panel -> db : getConnection()
activate db
db --> panel : Connection
deactivate db

panel -> mysql : "SELECT id_kriteria, nilai FROM penilaian\nWHERE id_karyawan IN (...)"
activate mysql
mysql --> panel : Nilai-nilai yang sudah ada
deactivate mysql

panel -> panel : "Render grid matriks keputusan\ndan isi nilai lama jika ada"
panel --> user : Grid matriks keputusan diaktifkan

user -> panel : "Input / ubah nilai pada grid (skala 1-100)"
user -> panel : Klik "SIMPAN"

panel -> panel : "Validasi semua nilai 1 ≤ nilai ≤ 100"
alt Validasi gagal
  panel --> user : Tampil pesan error validasi
else Validasi berhasil
  panel -> db : getConnection()
  activate db
  db --> panel : Connection
  deactivate db

  panel -> mysql : "setAutoCommit(false)"
  loop setiap karyawan dan kriteria terpilih
    panel -> mysql : "INSERT INTO penilaian (id_karyawan, id_kriteria, nilai)\nON DUPLICATE KEY UPDATE nilai=VALUES(nilai)"
  end
  panel -> mysql : "executeBatch() + commit()"
  activate mysql
  mysql --> panel : Success
  deactivate mysql

  panel -> panel : Reload tabel riwayat di bawah
  panel --> user : Pesan "Penilaian matriks berhasil disimpan"
end
deactivate panel
@enduml
```

---

## 9. Class Diagram (Lengkap dengan Role)

```plantuml
@startuml ClassDiagram
skinparam classBackgroundColor #EBF5FB
skinparam classBorderColor #2980B9
skinparam arrowColor #2C3E50
skinparam classHeaderBackgroundColor #2980B9
skinparam classHeaderFontColor white
title Class Diagram — SPK MOORA BRI KCP Arundina (Multi-Role)

class Karyawan {
  - idKaryawan : int
  - kodeKaryawan : String
  - nama : String
  - divisi : String
  --
  + getIdKaryawan() : int
  + getKodeKaryawan() : String
  + getNama() : String
  + getDivisi() : String
}

class Kriteria {
  - idKriteria : int
  - kodeKriteria : String
  - namaKriteria : String
  - sifat : String
  - bobot : double
  - divisi : String
  --
  + getIdKriteria() : int
  + getKodeKriteria() : String
  + getSifat() : String
  + getBobot() : double
}

class Penilaian {
  - idPenilaian : int
  - idKaryawan : int
  - idKriteria : int
  - nilai : double
}

class RankingResult {
  - karyawan : Karyawan
  - score : double
  - rank : int
  --
  + getKaryawan() : Karyawan
  + getScore() : double
  + getRank() : int
  + setRank(rank : int) : void
}

class DatabaseHelper {
  {static} - HOST : String
  {static} - PORT : String
  {static} - DB_NAME : String
  {static} - USER : String
  {static} - PASSWORD : String
  --
  {static} + getConnection() : Connection
}

class MooraEngine {
  {static} + calculate(divisi : String) : MooraCalculationResult
}

class MooraCalculationResult {
  - karyawanList : List<Karyawan>
  - kriteriaList : List<Kriteria>
  - matriksKeputusan : Map
  - matriksNormalisasi : Map
  - matriksNormalisasiTerbobot : Map
  - rankingResults : List<RankingResult>
  --
  + getKaryawanList() : List<Karyawan>
  + getKriteriaList() : List<Kriteria>
  + getMatriksKeputusan() : Map
  + getMatriksNormalisasi() : Map
  + getMatriksNormalisasiTerbobot() : Map
  + getRankingResults() : List<RankingResult>
}

class ExportHelper {
  {static} + exportToCSV(results, divisi, file) : void
  {static} + exportToPDF(results, divisi, file) : void
}

class App {
  {static} + main(args : String[]) : void
}

class LoginFrame {
  - txtUsername : JTextField
  - txtPassword : JPasswordField
  - lblError : JLabel
  --
  + LoginFrame()
  - attemptLogin() : void
}

note right of LoginFrame
  Routing berdasarkan role:
  • role=admin → MainFrame
  • role=pimpinan → PimpinanFrame
end note

class MainFrame {
  - cardLayout : CardLayout
  - dashboardPanel : DashboardPanel
  - karyawanPanel : KaryawanPanel
  - kriteriaPanel : KriteriaPanel
  - penilaianPanel : PenilaianPanel
  - reportPanel : ReportPanel
  --
  + MainFrame(adminName : String)
  - switchCard(name, source) : void
  - logout() : void
}

class PimpinanFrame {
  - cardLayout : CardLayout
  - karyawanPanel : PimpinanKaryawanPanel
  - penilaianPanel : PimpinanPenilaianPanel
  - rankingPanel : PimpinanRankingPanel
  --
  + PimpinanFrame(namaLengkap : String)
  - switchCard(name, source) : void
  - logout() : void
}

class DashboardPanel {
  + refreshData() : void
}

class KaryawanPanel {
  - selectedId : int
  --
  - saveKaryawan() : void
  - updateKaryawan() : void
  - deleteKaryawan() : void
}

class KriteriaPanel {}

class PenilaianPanel {
  + refreshTabs() : void
}

class ReportPanel {
  + refreshData() : void
}

class PimpinanKaryawanPanel {
  - txtSearch : JTextField
  - tableModel : DefaultTableModel
  --
  - loadTableData(keyword : String) : void
}

note right of PimpinanKaryawanPanel
  Read-only panel
  Tidak ada CRUD
  Hanya pencarian
end note

class PimpinanPenilaianPanel {
  - selectedKaryawanId : int
  - kriteriaList : List
  - fieldsMap : Map
  --
  - loadKaryawan() : void
  - loadKriteria() : void
  - savePenilaian() : void
}

note right of PimpinanPenilaianPanel
  Input & update nilai
  Tidak ada hapus data
  Upsert menggunakan
  ON DUPLICATE KEY UPDATE
end note

class PimpinanRankingPanel {
  - rankingResults : List
  - animationTimer : Timer
  --
  + loadData() : void
}

note right of PimpinanRankingPanel
  Tidak ada ekspor PDF/Excel
  Highlight rank 1 warna emas
end note

App --> LoginFrame
LoginFrame --> MainFrame : role = admin
LoginFrame --> PimpinanFrame : role = pimpinan

MainFrame --> DashboardPanel
MainFrame --> KaryawanPanel
MainFrame --> KriteriaPanel
MainFrame --> PenilaianPanel
MainFrame --> ReportPanel

PimpinanFrame --> PimpinanKaryawanPanel
PimpinanFrame --> PimpinanPenilaianPanel
PimpinanFrame --> PimpinanRankingPanel

DashboardPanel --> MooraEngine
ReportPanel --> MooraEngine
PimpinanRankingPanel --> MooraEngine
ReportPanel --> ExportHelper

MooraEngine --> DatabaseHelper
LoginFrame --> DatabaseHelper
KaryawanPanel --> DatabaseHelper
KriteriaPanel --> DatabaseHelper
PenilaianPanel --> DatabaseHelper
PimpinanKaryawanPanel --> DatabaseHelper
PimpinanPenilaianPanel --> DatabaseHelper

RankingResult --> Karyawan
Penilaian --> Karyawan
Penilaian --> Kriteria

MooraEngine --> MooraCalculationResult
MooraCalculationResult --> Karyawan
MooraCalculationResult --> Kriteria
MooraCalculationResult --> RankingResult

@enduml
```

---

## 10. Entity Relationship Diagram (ERD)

```plantuml
@startuml ERDiagram
skinparam linetype ortho
skinparam classBackgroundColor #FEF9E7
skinparam classBorderColor #F39C12
skinparam arrowColor #2C3E50
title ERD — Database spk_moora (dengan kolom role)

entity "users" as users {
  * id_user      : INT <<PK, AUTO_INCREMENT>>
  --
  * username     : VARCHAR(100) <<UNIQUE>>
  * password     : VARCHAR(255)
  * nama_lengkap : VARCHAR(255)
  * role         : VARCHAR(20) DEFAULT 'admin'
  -- Nilai role: "admin" | "pimpinan" --
}

entity "karyawan" as karyawan {
  * id_karyawan : INT <<PK, AUTO_INCREMENT>>
  --
  * kode_karyawan : VARCHAR(100) <<UNIQUE>>
  * nama        : VARCHAR(255)
  * divisi      : VARCHAR(50)
}

entity "kriteria" as kriteria {
  * id_kriteria   : INT <<PK, AUTO_INCREMENT>>
  --
  * kode_kriteria : VARCHAR(50)
  * nama_kriteria : VARCHAR(255)
  * sifat         : VARCHAR(50)
  * bobot         : DOUBLE
  * divisi        : VARCHAR(50)
}

entity "penilaian" as penilaian {
  * id_penilaian : INT <<PK, AUTO_INCREMENT>>
  --
  * id_karyawan  : INT <<FK>>
  * id_kriteria  : INT <<FK>>
  * nilai        : DOUBLE
  -- UNIQUE (id_karyawan, id_kriteria) --
}

karyawan ||--o{ penilaian : "fk_penilaian_karyawan\n(ON DELETE CASCADE)"
kriteria ||--o{ penilaian : "fk_penilaian_kriteria\n(ON DELETE CASCADE)"

@enduml
```

---

## 11. Deployment Diagram

```plantuml
@startuml DeploymentDiagram
skinparam nodeBackgroundColor #EBF5FB
skinparam nodeBorderColor #2980B9
skinparam arrowColor #2C3E50
title Deployment Diagram — SPK MOORA BRI KCP Arundina

node "Komputer Admin\n(Windows Desktop)" as pcAdmin {
  node "Java Runtime Environment (JRE 11+)" as jreAdmin {
    artifact "spk-bri.jar" as jar
    component "MainFrame (Admin UI)" as adminUI
    component "MooraEngine" as engine
    component "DatabaseHelper (JDBC)" as jdbc
  }
  component "FlatLaf Library" as flatlaf
  component "OpenPDF Library" as openpdf
}

node "Komputer Pimpinan\n(Windows Desktop)" as pcPimpinan {
  node "Java Runtime Environment (JRE 11+)" as jrePimpinan {
    artifact "spk-bri.jar" as jar2
    component "PimpinanFrame (Pimpinan UI)" as pimpinanUI
  }
}

node "Database Server\n(localhost / LAN)" as dbserver {
  node "MySQL Server 8.x" as mysql {
    database "spk_moora" as db {
      artifact "users (+ role)"
      artifact "karyawan"
      artifact "kriteria"
      artifact "penilaian"
    }
  }
}

folder "Output Files" as output {
  artifact "Laporan_Ranking_*.pdf"
  artifact "Laporan_Ranking_*.csv"
}

jar --> mysql : JDBC\njdbc:mysql://localhost:3306/spk_moora
jar2 --> mysql : JDBC\njdbc:mysql://localhost:3306/spk_moora
jar --> output : Ekspor laporan (Admin only)
adminUI ..> flatlaf : tema UI
jar ..> openpdf : ekspor PDF

@enduml
```

---

## 12. Component Diagram

```plantuml
@startuml ComponentDiagram
skinparam componentBackgroundColor #EBF5FB
skinparam componentBorderColor #2980B9
skinparam arrowColor #2C3E50
title Component Diagram — SPK MOORA BRI KCP Arundina (Multi-Role)

package "Presentation Layer — Administrator\n(com.spkbri.ui)" {
  [LoginFrame]
  [MainFrame]
  [DashboardPanel]
  [KaryawanPanel]
  [KriteriaPanel]
  [PenilaianPanel]
  [ReportPanel]
}

package "Presentation Layer — Pimpinan\n(com.spkbri.ui)" {
  [PimpinanFrame]
  [PimpinanKaryawanPanel]
  [PimpinanPenilaianPanel]
  [PimpinanRankingPanel]
}

package "Business Logic Layer\n(com.spkbri.core)" {
  [MooraEngine]
}

package "Data Access Layer\n(com.spkbri.database)" {
  [DatabaseHelper]
}

package "Model Layer\n(com.spkbri.model)" {
  [Karyawan]
  [Kriteria]
  [Penilaian]
  [RankingResult]
}

package "Utility Layer\n(com.spkbri.util)" {
  [ExportHelper]
}

database "MySQL\nspk_moora" as db

[LoginFrame] --> [DatabaseHelper]
[KaryawanPanel] --> [DatabaseHelper]
[KriteriaPanel] --> [DatabaseHelper]
[PenilaianPanel] --> [DatabaseHelper]
[PimpinanKaryawanPanel] --> [DatabaseHelper]
[PimpinanPenilaianPanel] --> [DatabaseHelper]

[DashboardPanel] --> [MooraEngine]
[ReportPanel] --> [MooraEngine]
[PimpinanRankingPanel] --> [MooraEngine]

[ReportPanel] --> [ExportHelper]

[MooraEngine] --> [DatabaseHelper]
[MooraEngine] --> [Karyawan]
[MooraEngine] --> [Kriteria]
[MooraEngine] --> [RankingResult]

[DatabaseHelper] --> db

@enduml
```

## 13. Flowchart Sistem yang Berjalan (Gambar 3)

```plantuml
@startuml FlowchartSistemBerjalan
skinparam Style strictuml
skinparam activity {
  BackgroundColor #FADBD8
  BorderColor #C0392B
  ArrowColor #2C3E50
}

|admin|
start
:mulai|
:buat akun juri]
:input Fakultas dan Prodi/
:input Kategori & Subkategori/
:buat form penilaian/

|sistem|
database database as db

|admin|
:buat akun juri] --> db
:input Fakultas dan Prodi/ --> db
:input Kategori & Subkategori/ --> db
:buat form penilaian/ --> db

|juri|
:masuk akun]
:input penilaian/

|sistem|
|juri|
:input penilaian/ --> |sistem| db

|juri|
:data penilaian]
|sistem|
db --> |juri| :data penilaian]

|admin|
:rekapitulasi penilaian setiap juri|
|juri|
:data penilaian] --> |admin| :rekapitulasi penilaian setiap juri|

|admin|
:rekapitulasi penilaian setiap juri| --> :data rekapitulasi]
:data rekapitulasi] --> :selesai|
stop
@enduml
```

---

## 14. Flowchart Sistem yang Akan Dikembangkan (Gambar 4)

```plantuml
@startuml FlowchartSistemAkanDikembangkan
skinparam Style strictuml
skinparam activity {
  BackgroundColor #EBF5FB
  BorderColor #2980B9
  ArrowColor #2C3E50
}

|admin|
start
:mulai|
:buat akun juri]
:input Fakultas dan Prodi/
:input Kategori & Subkategori/
:buat form penilaian/

|sistem|
database database as db

|admin|
:buat akun juri] --> db
:input Fakultas dan Prodi/ --> db
:input Kategori & Subkategori/ --> db
:buat form penilaian/ --> db

|juri|
:masuk akun]
:input penilaian/

|sistem|
:metode moora|
|juri|
:input penilaian/ --> |sistem| :metode moora|

|sistem|
:metode moora| --> :penjumlahan matriks keputusan|
:penjumlahan matriks keputusan| --> :normalisasi matriks keputusan|
:normalisasi matriks keputusan| --> :normalisasi matriks keputusan berbobot|
:normalisasi matriks keputusan berbobot| --> :hasil perhitungan moora|
:hasil perhitungan moora| --> db

|juri|
:data penilaian]
|sistem|
db --> |juri| :data penilaian]

|admin|
:rekapitulasi penilaian setiap juri|
|juri|
:data penilaian] --> |admin| :rekapitulasi penilaian setiap juri|

|admin|
:rekapitulasi penilaian setiap juri| --> :data rekapitulasi]
:data rekapitulasi] --> :selesai|
stop
@enduml
```

---

## Catatan Penggunaan

Untuk merender diagram, gunakan salah satu:

1. **VS Code** — Install ekstensi `PlantUML` (jebbs), tekan `Alt+D` untuk preview
2. **PlantUML Online** — https://www.plantuml.com/plantuml/uml/
3. **IntelliJ IDEA** — Install plugin `PlantUML Integration`

---

## Daftar Diagram

| No | Nama Diagram | Jenis | Kegunaan di Skripsi |
|---|---|---|---|
| 1 | Use Case Diagram (Multi-Role) | UML Behavioral | Bab 3 — Analisis Kebutuhan |
| 2 | Activity Diagram Login + Role Routing | UML Behavioral | Bab 3 / Bab 4 |
| 3 | Activity Diagram Kelola Karyawan (Admin) | UML Behavioral | Bab 3 / Bab 4 |
| 4 | Activity Diagram Input Penilaian (Admin & Pimpinan) | UML Behavioral | Bab 3 / Bab 4 |
| 5 | Activity Diagram Kalkulasi MOORA | UML Behavioral | Bab 3 / Bab 4 |
| 6 | Sequence Diagram Login + Routing | UML Behavioral | Bab 4 — Implementasi |
| 7 | Sequence Diagram Kalkulasi MOORA | UML Behavioral | Bab 4 — Implementasi |
| 8 | Sequence Diagram Input Penilaian (Pimpinan) | UML Behavioral | Bab 4 — Implementasi |
| 9 | Class Diagram (Multi-Role) | UML Structural | Bab 3 / Bab 4 |
| 10 | ERD (dengan kolom role) | Database Design | Bab 3 — Perancangan Database |
| 11 | Deployment Diagram | UML Structural | Bab 4 — Implementasi |
| 12 | Component Diagram (Multi-Role) | UML Structural | Bab 3 / Bab 4 |
| 13 | Flowchart Sistem yang Berjalan | Flowchart | Bab 3 / Bab 4 |
| 14 | Flowchart Sistem yang Akan Dikembangkan | Flowchart | Bab 3 / Bab 4 |

