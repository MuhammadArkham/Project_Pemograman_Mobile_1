package com.fitme.app.ui.bmi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.fitme.app.R;

public class InputDataActivity extends AppCompatActivity {

    // Kita butuh akses ke "Pembungkus" (Layout) untuk mengatur teks Label/Hint agar rapi
    private TextInputLayout layoutAge, layoutWeight, layoutHeight;
    private TextInputEditText etAge, etWeight, etHeight;

    private TextView tvTitle, tvSubtitle;
    private MaterialButton btnCalculate;

    private boolean isMale;
    // Default String kita set kosong dulu biar logika di bawah jalan benar
    private String country = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);

        // 1. AMBIL DATA DARI HALAMAN SEBELUMNYA
        if (getIntent() != null) {
            isMale = getIntent().getBooleanExtra("USER_GENDER", true);
            if (getIntent().hasExtra("DETECTED_COUNTRY")) {
                country = getIntent().getStringExtra("DETECTED_COUNTRY");
            }
        }

        // 2. INISIALISASI VIEWS
        // Pembungkus (TextInputLayout)
        layoutAge = findViewById(R.id.layoutAge);
        layoutWeight = findViewById(R.id.layoutWeight);
        layoutHeight = findViewById(R.id.layoutHeight);

        // Inputan (TextInputEditText)
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);

        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnCalculate = findViewById(R.id.btnCalculate);
        ImageView btnBack = findViewById(R.id.btnBack);

        // 3. JALANKAN LOGIKA BAHASA
        setLanguage();

        // 4. LISTENERS
        btnBack.setOnClickListener(v -> finish());
        btnCalculate.setOnClickListener(v -> calculateBmi());
    }

    private void setLanguage() {
        // LOGIKA: Cek apakah spesifik negara asing.
        // Jika tidak dikenal (atau null/kosong), otomatis ke Default (Indonesia).

        boolean isEnglish = country != null && (country.contains("United Kingdom") || country.contains("United States") || country.contains("England"));
        boolean isSpanish = country != null && (country.contains("Spain") || country.contains("España"));

        if (isSpanish) {
            // === BAHASA SPANYOL ===
            tvTitle.setText("Datos Personales");
            tvSubtitle.setText("Complete sus datos físicos.");

            layoutAge.setHint("Edad (Años)");
            layoutWeight.setHint("Peso (kg)");
            layoutHeight.setHint("Altura (cm)");

            btnCalculate.setText("Calcular");

        } else if (isEnglish) {
            // === BAHASA INGGRIS ===
            tvTitle.setText("Enter Details");
            tvSubtitle.setText("Complete your physical data.");

            layoutAge.setHint("Age (Years)");
            layoutWeight.setHint("Weight (kg)");
            layoutHeight.setHint("Height (cm)");

            btnCalculate.setText("Calculate");

        } else {
            // === DEFAULT: INDONESIA ===
            // Logika "Else" ini menangkap semua lokasi lain (termasuk kecamatan lokal)
            tvTitle.setText("Isi Data Diri");
            tvSubtitle.setText("Lengkapi data fisik Anda.");

            layoutAge.setHint("Usia (Tahun)");
            layoutWeight.setHint("Berat Badan (kg)");
            layoutHeight.setHint("Tinggi Badan (cm)");

            btnCalculate.setText("Hitung Hasil");
        }
    }

    private void calculateBmi() {
        String strAge = etAge.getText().toString();
        String strWeight = etWeight.getText().toString();
        String strHeight = etHeight.getText().toString();

        // Validasi Input Kosong
        boolean hasError = false;

        if (TextUtils.isEmpty(strAge)) {
            layoutAge.setError("Wajib diisi");
            hasError = true;
        } else {
            layoutAge.setError(null);
        }

        if (TextUtils.isEmpty(strWeight)) {
            layoutWeight.setError("Wajib diisi");
            hasError = true;
        } else {
            layoutWeight.setError(null);
        }

        if (TextUtils.isEmpty(strHeight)) {
            layoutHeight.setError("Wajib diisi");
            hasError = true;
        } else {
            layoutHeight.setError(null);
        }

        if (hasError) return;

        try {
            float weight = Float.parseFloat(strWeight);
            float height = Float.parseFloat(strHeight);
            int age = Integer.parseInt(strAge);

            if (height <= 0 || weight <= 0) {
                Toast.makeText(this, "Angka harus lebih dari 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Rumus BMI
            float heightInMeter = height / 100;
            float bmiResult = weight / (heightInMeter * heightInMeter);

            // Pindah Halaman
            Intent intent = new Intent(InputDataActivity.this, ResultActivity.class);
            intent.putExtra("BMI_RESULT", bmiResult);
            intent.putExtra("USER_AGE", age);
            intent.putExtra("USER_GENDER", isMale);

            // --- REVISI PENTING: KIRIM DATA NEGARA ---
            // Agar ResultActivity tahu harus pakai Bahasa Indonesia
            intent.putExtra("DETECTED_COUNTRY", country);
            // ----------------------------------------

            startActivity(intent);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format angka salah", Toast.LENGTH_SHORT).show();
        }
    }
}