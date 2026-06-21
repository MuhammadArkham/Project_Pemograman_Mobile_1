<div align="center">
  <h1>FITME - Aplikasi Kalkulator BMI</h1>
  <p>Tugas Mata Kuliah Pemrograman Mobile 2</p>
</div>

---

## Identitas Mahasiswa

| Informasi | Keterangan |
| :--- | :--- |
| **Nama** | Muhammad Arkhamullah Rifai Asshidiq |
| **NIM** | 312410545 |
| **Kelas** | I241E |
| **Matakuliah** | Pemrograman Mobile 2 |
| **Dosen Pengampu** | Donny Maulana, S.Kom., M.M.S.I. |

---

## Daftar Isi
- [Identitas Mahasiswa](#identitas-mahasiswa)
- [1. Splash Screen](#1-splash-screen)
- [2. StoryBoard Project](#2-storyboard-project)
- [3. Mockup Project](#3-mockup-project)
- [4. UI (User Interface) Project & Color Palette](#4-ui-user-interface-project--color-palette)
- [5. UX (User Experience) Video Prototype Project](#5-ux-user-experience-video-prototype-project)
- [6. Link Jadwal pada ClickUp](#6-link-jadwal-pada-clickup)
- [7. Notifikasi Firebase](#7-notifikasi-firebase)
- [8. Implementasi Android Studio](#8-implementasi-android-studio)

---

## 1. Splash Screen
*(Deteksi location, Bendera, Say Hello/Selamat Datang sesuai lokasi daerah masing-masing)*

<div align="center">
  <img src="FOTO%20PROJECT/Splashscreen.png" alt="Splash Screen" width="300">
</div>

---

## 2. StoryBoard Project

<div align="center">
  <img src="FOTO%20PROJECT/Storyboard.png" alt="Storyboard Project" width="800">
</div>

### Detail Storyboard Aplikasi “FITME” – Kalkulator BMI

<details>
<summary><b>Lihat Detail Frame Storyboard</b></summary>

<br>

#### **Frame 1 — Splash Screen**
* **Visual**: Layar penuh menampilkan logo “FITME” (ikon hati) di tengah. Nama aplikasi “FITME” berada di bawah logo.
* **Deskripsi**: Tampilan pertama ketika aplikasi dibuka. Berfungsi sebagai branding dan proses loading awal sebelum masuk ke halaman utama.

#### **Frame 2 — Halaman Utama**
* **Visual**: Ilustrasi di bagian atas. Dua tombol utama: **“Hitung BMI Sekarang”** dan **“Hitung BMI”**. Terdapat navigasi bawah (navbar).
* **Deskripsi**: Menjadi dashboard utama. Pengguna dapat langsung memulai proses perhitungan BMI.

#### **Frame 3 — Pilih Jenis Kelamin**
* **Visual**: Judul: *“Pilih Jenis Kelamin”*. Dua pilihan dengan ikon: Laki-laki & Perempuan. Tombol **“Simpan”** di bagian bawah.
* **Deskripsi**: Langkah pertama sebelum menghitung BMI. Pengguna wajib memilih jenis kelamin karena dapat berpengaruh pada perhitungan.

#### **Frame 4 — Input Data**
* **Visual**: Header dengan tombol kembali (“<”) dan judul *“Input Data”*. Tiga kolom input: Tinggi Badan, Berat Badan, Tingkat Aktivitas. Tombol **“Hitung”** di bagian bawah.
* **Deskripsi**: Pengguna mengisi data fisik untuk proses perhitungan BMI. Setelah lengkap, pengguna menekan “Hitung”.

#### **Frame 5 — Result Screen**
* **Visual**: Judul: *“Hasil BMI”*. Angka BMI besar di tengah (misal: **22.5**). Status BMI (Normal / Kurus / Gemuk). Grafik batang sederhana untuk menunjukkan rentang atau kategori BMI. Tombol **“Simpan”**.
* **Deskripsi**: Menampilkan hasil akhir perhitungan. Pengguna mengetahui nilai BMI dan kategorinya. Hasil dapat disimpan ke riwayat.

#### **Frame 6 — History Screen**
* **Visual**: Judul: *“Riwayat Perhitungan”*. Grafik garis (line chart) untuk menunjukkan tren BMI dari waktu ke waktu. Tabel data berisi tanggal dan nilai BMI (misal: Hari ini, Kemarin). Tombol **“Simpan”** atau opsi tambahan seperti “Export” bila dibutuhkan.
* **Deskripsi**: Menampilkan catatan hasil perhitungan sebelumnya. Membantu pengguna memantau perubahan berat dan status kesehatan.

</details>

---

## 3. Mockup Project

<div align="center">
  <img src="FOTO%20PROJECT/Mockup.png" alt="Mockup Project" width="800">
</div>

---

## 4. UI (User Interface) Project & Color Palette

<div align="center">
  <img src="FOTO%20PROJECT/UI.png" alt="UI Project" width="800">
  <br><br>
  <b>Color Palette</b><br>
  <img src="FOTO%20PROJECT/CP.png" alt="Color Palette" width="800">
</div>

### Penjelasan Desain UI (User Interface)
Desain UI terdiri dari 21 layar utama yang dirancang untuk memandu pengguna mulai dari membuka aplikasi hingga memantau progres harian.

<details>
<summary><b>Lihat Detail Keterangan Layar UI</b></summary>

<br>

1. **01. Splash Screen**: Layar pembuka dengan logo identitas FITME saat aplikasi pertama kali dijalankan.
2. **02 - 04. Onboarding 1-3**: Pengenalan fitur-fitur unggulan aplikasi kepada pengguna baru, seperti memantau pola makan dan kebiasaan sehat.
3. **05. Pilih Peran**: Layar pemilihan jenis kelamin (Laki-laki / Perempuan) yang menjadi dasar penentuan kebutuhan kalori.
4. **06. Pilih Tujuan**: Pengguna dapat memilih target spesifik mereka: menurunkan berat badan, menjaga berat badan, atau menambah massa otot.
5. **07. Input Data Diri**: Pengisian parameter fisik tubuh seperti Usia, Tinggi Badan, Berat Badan, dan Tingkat Aktivitas Harian.
6. **08. Target Kalori**: Menampilkan target kebutuhan makronutrisi harian (Karbohidrat, Protein, Lemak) yang dipersonalisasi berdasarkan data pengguna.
7. **09. Tips Harian**: Halaman edukasi yang berisi panduan kesehatan untuk menjaga konsistensi pengguna.
8. **10. Login**: Layar masuk dengan form email/password, serta opsi otentikasi menggunakan pihak ketiga.
9. **11. Beranda**: Layar utama beranda yang menampilkan ringkasan kalori, menu cepat, dan rekomendasi harian.
10. **12. Dashboard**: Layar detail pemantauan kebutuhan kalori harian yang telah dipenuhi beserta asupan nutrisinya.
11. **13. Menu Makanan**: Katalog berbagai opsi menu sehat yang dikategorikan berdasarkan preferensi.
12. **14. Rekomendasi**: Daftar kurasi makanan sehat dengan tujuan spesifik (contoh: tinggi protein atau rendah kalori).
13. **15. Detail Makanan**: Menampilkan informasi gizi lengkap (Karbo, Lemak, Protein) dari sebuah makanan.
14. **16. Resep**: Daftar inspirasi resep makanan sehat untuk berbagai waktu makan.
15. **17. Detail Resep**: Menyajikan rincian bahan-bahan dan langkah-langkah praktis untuk memasak resep terkait.
16. **18. Catatan Harian**: Halaman log makanan tempat pengguna mencatat makanan yang dikonsumsi pada hari tersebut.
17. **19. Detail Catatan**: Menampilkan rincian nutrisi yang tercatat pada makanan tertentu yang sudah dimakan.
18. **20. Progress**: Pemantauan hasil program pengguna berupa grafik garis yang menggambarkan tren berat badan dari waktu ke waktu.
19. **21. Detail Progress**: Laporan rinci dari pencapaian selisih berat badan serta status Indeks Massa Tubuh (BMI) pengguna saat ini.
</details>

---

## 5. UX (User Experience) Video Prototype Project

- **Penjelasan project aplikasi**: [Tonton di YouTube](https://youtube.com/shorts/rz2nW9jxM30?si=rgG1yV6PPaogpApr)
- **Tampilan Video Project UI/UX**: [Tonton di YouTube](https://youtube.com/shorts/YG2sh5dMyGQ?si=ND3No_8X5PhxlHR3)

### Demo Aplikasi FITME

Berikut adalah video demonstrasi fitur-fitur pada aplikasi FITME:

[**▶ Tonton Video Demo Aplikasi (MP4)**](FOTO%20PROJECT/Demo_App.mp4)

*(Jika video tidak terputar otomatis, silakan klik tautan di atas).*

---

## 6. Link Jadwal pada ClickUp

- **Link ClickUp (Board)**: [Akses Board](https://sharing.clickup.com/90181791504/b/2kzm1vrg-558/board)
- **Link ClickUp (Gantt)**: [Akses Gantt](https://sharing.clickup.com/90181791504/g/2kzm1vrg-578/gantt)

---

## 7. Notifikasi Firebase

<div align="center">
  <img src="FOTO%20PROJECT/Screenshot%202025-12-19%20005452.png" alt="Notifikasi Firebase 1" width="300">
  <img src="FOTO%20PROJECT/WhatsApp%20Image%202025-12-23%20at%2010.34.15_734c87b3.jpg" alt="Notifikasi Firebase 2" width="300">
</div>

---

## 8. Implementasi Android Studio

<table>
  <tr>
    <td align="center"><b>Tampilan Splashscreen</b><br><img src="FOTO%20PROJECT/Screenshot%202026-01-15%20211819.png" width="200"></td>
    <td align="center"><b>Tampilan Home (Dashboard)</b><br><img src="FOTO%20PROJECT/Screenshot%202026-01-15%20211844.png" width="200"></td>
    <td align="center"><b>Tampilan Gender Selection</b><br><img src="FOTO%20PROJECT/Screenshot%202026-01-15%20211557.png" width="200"></td>
  </tr>
  <tr>
    <td align="center"><b>Tampilan Input Data</b><br><img src="FOTO%20PROJECT/Screenshot%202026-01-15%20211751.png" width="200"></td>
    <td align="center"><b>Tampilan Result</b><br><img src="FOTO%20PROJECT/Screenshot%202026-01-15%20211808.png" width="200"></td>
    <td align="center"><b>Tampilan Riwayat</b><br><img src="FOTO%20PROJECT/Screenshot%202026-01-15%20211620.png" width="200"></td>
  </tr>
</table>
