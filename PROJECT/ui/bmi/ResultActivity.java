package com.fitme.app.ui.bmi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Tambahan untuk notifikasi kecil
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import com.fitme.app.R;
import com.fitme.app.database.AppDatabase; // Import Database
import com.fitme.app.database.BmiEntity;   // Import Entity
import com.fitme.app.ui.home.HomeActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors; // Import untuk Background Thread

public class ResultActivity extends AppCompatActivity {

    // Variabel UI
    private TextView tvBmiValue, tvBmiStatus, tvBmiDesc, tvTitle;
    private AppCompatButton btnFinish;
    private TextView btnRecalculate;
    private CardView cardStatusBadge;

    // Data Penting
    private float bmiResult;
    private String country = "Indonesia";

    // Data Tambahan untuk Disimpan ke Database (Diterima dari halaman sebelumnya)
    private int userAge;
    private boolean isMale;

    // Variabel penampung kategori untuk disimpan
    private String savedCategory = "Normal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result); // Layout TETAP, tidak berubah

        // 1. AMBIL DATA DARI INPUT
        if (getIntent() != null) {
            bmiResult = getIntent().getFloatExtra("BMI_RESULT", 0f);

            // Ambil Data Umur & Gender (Penting untuk Riwayat)
            userAge = getIntent().getIntExtra("USER_AGE", 25);
            isMale = getIntent().getBooleanExtra("USER_GENDER", true);

            if (getIntent().hasExtra("DETECTED_COUNTRY")) {
                country = getIntent().getStringExtra("DETECTED_COUNTRY");
            }
        }

        // 2. INISIALISASI VIEW (Sesuai Layout Anda)
        tvTitle = findViewById(R.id.tvTitle);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        tvBmiDesc = findViewById(R.id.tvBmiDesc);
        btnFinish = findViewById(R.id.btnFinish);
        btnRecalculate = findViewById(R.id.btnRecalculate);
        cardStatusBadge = findViewById(R.id.cardStatusBadge);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Tampilkan Skor di Layar
        if (tvBmiValue != null) {
            tvBmiValue.setText(String.format(Locale.US, "%.1f", bmiResult));
        }

        // 3. ATUR TEKS & WARNA (Logika Bahasa)
        setResultData();

        // 4. PERINTAH SIMPAN: Panggil fungsi simpan otomatis di sini
        saveToDatabase();

        // 5. LISTENERS (Tombol-tombol)
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnFinish != null) {
            btnFinish.setOnClickListener(v -> {
                // Saat selesai, balik ke Home membawa data negara
                Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("DETECTED_COUNTRY", country);
                startActivity(intent);
                finish();
            });
        }

        if (btnRecalculate != null) {
            btnRecalculate.setOnClickListener(v -> finish()); // Balik ke Input
        }
    }

    // === LOGIKA BARU: MENYIMPAN KE DATABASE ===
    private void saveToDatabase() {
        // Gunakan Executor agar tidak memberatkan UI (Main Thread)
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // A. Siapkan Data
                BmiEntity record = new BmiEntity();
                record.bmi = bmiResult;           // Skor BMI
                record.category = savedCategory;  // Kategori (Normal/Obesitas dll)
                record.age = String.valueOf(userAge);
                record.gender = isMale ? "Pria" : "Wanita";

                // Format Tanggal (Contoh: 12 Des 2025)
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
                record.date = sdf.format(new Date());

                // B. Masukkan ke Database
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.bmiDao().insertBmi(record);

                // (Opsional) Beri tahu user kalau sudah tersimpan
                // runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Hasil disimpan!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setResultData() {
        boolean isEnglish = country != null && (country.contains("United Kingdom") || country.contains("United States"));
        boolean isSpanish = country != null && (country.contains("Spain"));

        String statusText, descText, titleText, doneText, againText;
        int statusColor, badgeColor;

        // --- ALGORITMA KATEGORI ---
        if (bmiResult < 18.5) {
            // KURUS
            statusColor = Color.parseColor("#FF9800");
            badgeColor = Color.parseColor("#FFF3E0");
            savedCategory = "Berat Kurang"; // Untuk Database

            if (isEnglish) {
                statusText = "Underweight";
                descText = "You need to increase your nutritious food intake.";
                titleText = "Your Result";
                doneText = "Done";
                againText = "Calculate Again";
            } else if (isSpanish) {
                statusText = "Bajo Peso";
                descText = "Necesitas comer más alimentos nutritivos.";
                titleText = "Tu Resultado";
                doneText = "Listo";
                againText = "Calcular de Nuevo";
            } else {
                statusText = "Berat Kurang";
                descText = "Anda perlu menambah asupan makanan bergizi.";
                titleText = "Hasil Anda";
                doneText = "Selesai";
                againText = "Hitung Lagi";
            }

        } else if (bmiResult < 24.9) {
            // NORMAL
            statusColor = Color.parseColor("#4CAF50");
            badgeColor = Color.parseColor("#E8F5E9");
            savedCategory = "Normal"; // Untuk Database

            if (isEnglish) {
                statusText = "Normal";
                descText = "Great job! Your weight is ideal. Keep it up.";
                titleText = "Your Result";
                doneText = "Done";
                againText = "Calculate Again";
            } else if (isSpanish) {
                statusText = "Normal";
                descText = "¡Buen trabajo! Tu peso es ideal.";
                titleText = "Tu Resultado";
                doneText = "Listo";
                againText = "Calcular de Nuevo";
            } else {
                statusText = "Normal";
                descText = "Kerja bagus! Berat badan Anda ideal. Pertahankan.";
                titleText = "Hasil Anda";
                doneText = "Selesai";
                againText = "Hitung Lagi";
            }

        } else if (bmiResult < 29.9) {
            // GEMUK
            statusColor = Color.parseColor("#FF9800");
            badgeColor = Color.parseColor("#FFF3E0");
            savedCategory = "Berat Berlebih"; // Untuk Database

            if (isEnglish) {
                statusText = "Overweight";
                descText = "Consider regular exercise and a balanced diet.";
                titleText = "Your Result";
                doneText = "Done";
                againText = "Calculate Again";
            } else if (isSpanish) {
                statusText = "Sobrepeso";
                descText = "Considere hacer ejercicio regularmente.";
                titleText = "Tu Resultado";
                doneText = "Listo";
                againText = "Calcular de Nuevo";
            } else {
                statusText = "Berat Berlebih";
                descText = "Pertimbangkan olahraga rutin dan diet seimbang.";
                titleText = "Hasil Anda";
                doneText = "Selesai";
                againText = "Hitung Lagi";
            }

        } else {
            // OBESITAS
            statusColor = Color.parseColor("#F44336");
            badgeColor = Color.parseColor("#FFEBEE");
            savedCategory = "Obesitas"; // Untuk Database

            if (isEnglish) {
                statusText = "Obese";
                descText = "Please consult a doctor for better health advice.";
                titleText = "Your Result";
                doneText = "Done";
                againText = "Calculate Again";
            } else if (isSpanish) {
                statusText = "Obesidad";
                descText = "Consulte a un médico por su salud.";
                titleText = "Tu Resultado";
                doneText = "Listo";
                againText = "Calcular de Nuevo";
            } else {
                statusText = "Obesitas";
                descText = "Sebaiknya konsultasikan ke dokter demi kesehatan.";
                titleText = "Hasil Anda";
                doneText = "Selesai";
                againText = "Hitung Lagi";
            }
        }

        // Set Data ke Layar
        if (tvBmiStatus != null) {
            tvBmiStatus.setText(statusText);
            tvBmiStatus.setTextColor(statusColor);
        }
        if (cardStatusBadge != null) {
            cardStatusBadge.setCardBackgroundColor(badgeColor);
        }
        if (tvBmiDesc != null) tvBmiDesc.setText(descText);
        if (tvTitle != null) tvTitle.setText(titleText);

        if (btnFinish != null) btnFinish.setText(doneText);
        if (btnRecalculate != null) btnRecalculate.setText(againText);
    }
}