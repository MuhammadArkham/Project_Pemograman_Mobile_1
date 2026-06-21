# =========================================================
# FITME - SECURITY & OBFUSCATION RULES
# =========================================================

# 1. Aturan Pengacakan Dasar (Obfuscation)
# Mengacak nama file dan folder agar tidak bisa dibaca oleh hacker
-repackageclasses ''
-allowaccessmodification

# Menyimpan baris error (opsional tapi penting) agar jika aplikasi
# crash di Play Store, Anda tetap bisa membaca laporannya.
-keepattributes SourceFile,LineNumberTable

# 2. Amankan Komponen Penting Android Sistem
# Mencegah sistem Android kebingungan saat memanggil layar/Activity
-keeppublicclass * extends androidx.activity.ComponentActivity
-keeppublicclass * extends androidx.appcompat.app.AppCompatActivity
-keeppublicclass * extends androidx.lifecycle.ViewModel

# 3. Proteksi Kelas Database Room
# Wajib dipertahankan nama aslinya agar data riwayat makan pengguna
# tidak korup/hilang saat aplikasi di-update
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**
-keep class com.fitme.app.database.** { *; }

# 4. Amankan OkHttp & JSON (Sistem Komunikasi Gemini AI)
# Mencegah crash saat mengirim gambar makanan ke server Google
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# 5. Amankan File BuildConfig (Benteng API Key)
# Mencegah API Key Gemini dan konfigurasi rahasia lainnya dihancurkan oleh ProGuard
-keep class com.fitme.app.BuildConfig { *; }