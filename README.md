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

## 
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

### Detail Storyboard Aplikasi "FITME"

<details>
<summary><b>Lihat Detail Frame Storyboard</b></summary>

<br>

#### **Frame 01 -- Splash Screen**
* **Visual**: Layar pembuka menampilkan logo FITME dengan tagline "Fit Today, Better Tomorrow" di atas latar gradasi hijau tua.
* **Deskripsi**: Tampilan pertama saat aplikasi dijalankan. Berfungsi sebagai branding dan proses loading awal.

#### **Frame 02 -- Onboarding 1**
* **Visual**: Ilustrasi orang makan sehat dengan judul "Makan Sehat, Hidup Lebih Baik" dan tombol "Lanjut".
* **Deskripsi**: Slide pertama onboarding yang mengedukasi pengguna tentang pentingnya pola makan sehat.

#### **Frame 03 -- Onboarding 2**
* **Visual**: Ilustrasi orang menimbang badan dengan judul "Pantau Berat Badan, Pahami Progressmu".
* **Deskripsi**: Slide kedua yang memperkenalkan fitur pemantauan berat badan secara berkala.

#### **Frame 04 -- Onboarding 3**
* **Visual**: Ilustrasi kebiasaan sehat dengan judul "Bangun Kebiasaan, Raih Hidup Sehat" dan tombol "Lanjut".
* **Deskripsi**: Slide terakhir onboarding yang memotivasi pengguna untuk membangun kebiasaan hidup sehat.

#### **Frame 05 -- Pilih Peran**
* **Visual**: Dua ikon pilihan (Laki-laki & Perempuan) dengan tombol "Lanjutkan".
* **Deskripsi**: Pemilihan jenis kelamin yang menjadi dasar penentuan kebutuhan kalori dan rekomendasi.

#### **Frame 06 -- Pilih Tujuan**
* **Visual**: Tiga opsi tujuan: Menurunkan Berat, Menjaga Berat, dan Menaikkan Berat dengan tombol "Lanjutkan".
* **Deskripsi**: Pengguna memilih target spesifik mereka agar rekomendasi nutrisi lebih personal.

#### **Frame 07 -- Input Data Diri**
* **Visual**: Form input angka untuk Usia (20), Tinggi Badan, Berat Badan, dan Aktivitas dengan tombol "Simpan & Lanjut".
* **Deskripsi**: Pengisian parameter fisik tubuh untuk kebutuhan perhitungan kalori dan BMI.

#### **Frame 08 -- Target Kalori**
* **Visual**: Angka besar "2010" kcal sebagai target harian, konsumsi di pagi minimal 3 kue, dan tombol navigasi.
* **Deskripsi**: Menampilkan target kebutuhan makronutrisi harian yang dipersonalisasi berdasarkan data pengguna.

#### **Frame 09 -- Tips Harian**
* **Visual**: Daftar tips kesehatan seperti "Sarapan setiap hari" dan "Makan sayur" dengan tombol "Siap, Mulai!".
* **Deskripsi**: Halaman edukasi berisi panduan praktis untuk menjaga konsistensi pola hidup sehat.

#### **Frame 10 -- Login**
* **Visual**: Logo FITME, ucapan "Selamat, Datang Kembali!", form Email & Password, serta tombol masuk.
* **Deskripsi**: Layar autentikasi pengguna untuk masuk ke akun yang sudah terdaftar.

#### **Frame 11 -- Beranda**
* **Visual**: Sapaan "Halo, Selamat Pagi!", ringkasan kalori harian, menu cepat, dan rekomendasi aktivitas.
* **Deskripsi**: Halaman utama yang menampilkan ringkasan aktivitas dan pintasan ke fitur-fitur utama.

#### **Frame 12 -- Dashboard**
* **Visual**: Ringkasan kalori (1200/2010 kcal), progress bar nutrisi (Karbo, Protein, Lemak), dan daftar makanan.
* **Deskripsi**: Layar detail pemantauan kebutuhan kalori harian beserta asupan nutrisi yang sudah terpenuhi.

#### **Frame 13 -- Menu Makanan**
* **Visual**: Kolom pencarian dan daftar menu (Nasi Ayam Panggang, Salad Sayur, Oatmeal Buah, dll.) dengan kalori.
* **Deskripsi**: Katalog berbagai opsi menu makanan sehat yang dapat dipilih pengguna.

#### **Frame 14 -- Rekomendasi**
* **Visual**: Judul "Rekomendasi untuk Kamu" dengan daftar makanan rekomendasi (Telur, Tahu, dll.).
* **Deskripsi**: Daftar kurasi makanan sehat berdasarkan tujuan spesifik pengguna.

#### **Frame 15 -- Detail Makanan**
* **Visual**: Gambar makanan, informasi kalori (450 kcal), detail nutrisi (Karbohidrat, Protein, Lemak), dan tombol "Tambahkan ke Catatan".
* **Deskripsi**: Menampilkan informasi gizi lengkap dari sebuah makanan yang dipilih.

#### **Frame 16 -- Resep**
* **Visual**: Kategori resep (Sarapan, Makan Siang, dll.) dengan daftar resep bergambar.
* **Deskripsi**: Daftar inspirasi resep makanan sehat untuk berbagai waktu makan.

#### **Frame 17 -- Detail Resep**
* **Visual**: Gambar makanan, daftar bahan-bahan, langkah memasak, dan tombol "Simpan Resep".
* **Deskripsi**: Rincian bahan dan langkah-langkah praktis untuk memasak resep terkait.

#### **Frame 18 -- Catatan Harian**
* **Visual**: Tanggal dan ringkasan harian, daftar makanan yang dimakan (Oatmeal Buah, Nasi Ayam, dll.) dengan kalori.
* **Deskripsi**: Halaman log makanan tempat pengguna mencatat apa yang dikonsumsi pada hari tersebut.

#### **Frame 19 -- Detail Catatan**
* **Visual**: Judul "Detail Catatan" dengan tanggal, detail makanan (Nasi Ayam Panggang), nutrisi rinci, dan tombol "Edit Catatan".
* **Deskripsi**: Menampilkan rincian nutrisi dari makanan tertentu yang sudah dicatat pengguna.

#### **Frame 20 -- Progress**
* **Visual**: Tab (Berat Badan, Kalori, Aktivitas), grafik garis tren berat badan, dan tombol "Catat Berat".
* **Deskripsi**: Pemantauan hasil program berupa grafik yang menggambarkan tren berat badan dari waktu ke waktu.

#### **Frame 21 -- Detail Progress**
* **Visual**: Angka besar "60.2 kg", perubahan "-1.5 kg", BMI "20.8" (Normal), dan tombol "Catat Berat Baru".
* **Deskripsi**: Laporan rinci pencapaian berat badan serta status Indeks Massa Tubuh (BMI) pengguna saat ini.

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

## Demo Aplikasi FITME

* 🎥 **Demo Aplikasi FITME**: [Tonton di YouTube](https://youtube.com/shorts/_H6tcHBekvo?si=apevXHHTCkccnWtz)


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
