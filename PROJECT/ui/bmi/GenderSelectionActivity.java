package com.fitme.app.ui.bmi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView; // REVISI: Pakai CardView biasa sesuai XML
import com.fitme.app.R;

public class GenderSelectionActivity extends AppCompatActivity {

    // REVISI: Gunakan CardView biasa agar cocok dengan XML
    private CardView cardMale, cardFemale;
    private ImageView imgMale, imgFemale;
    private TextView tvTitle, tvSubtitle, tvMale, tvFemale;
    private AppCompatButton btnContinue;

    private boolean isMaleSelected = true; // Default Male
    private String country = "England";

    // Definisi Warna Modern
    final int COLOR_MALE_ACTIVE_TEXT = Color.parseColor("#4CAF50"); // Hijau Teks
    final int COLOR_MALE_BG = Color.parseColor("#E8F5E9"); // Hijau Background

    final int COLOR_FEMALE_ACTIVE_TEXT = Color.parseColor("#E91E63"); // Pink Teks
    final int COLOR_FEMALE_BG = Color.parseColor("#FCE4EC"); // Pink Background

    final int COLOR_INACTIVE_TEXT = Color.parseColor("#BDBDBD"); // Abu-abu
    final int COLOR_WHITE = Color.parseColor("#FFFFFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_selection);

        // Ambil data negara jika ada
        if (getIntent() != null && getIntent().hasExtra("DETECTED_COUNTRY")) {
            country = getIntent().getStringExtra("DETECTED_COUNTRY");
        }

        // INIT VIEWS
        cardMale = findViewById(R.id.cardMale);
        cardFemale = findViewById(R.id.cardFemale);
        imgMale = findViewById(R.id.imgMale);
        imgFemale = findViewById(R.id.imgFemale);

        // REVISI PENTING: ID disesuaikan dengan XML (txtMale & txtFemale)
        tvMale = findViewById(R.id.txtMale);
        tvFemale = findViewById(R.id.txtFemale);

        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnContinue = findViewById(R.id.btnContinue);
        ImageView btnBack = findViewById(R.id.btnBack);

        setLanguage();

        // Set tampilan awal
        updateSelectionUI(true);

        // Listeners
        cardMale.setOnClickListener(v -> updateSelectionUI(true));
        cardFemale.setOnClickListener(v -> updateSelectionUI(false));
        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            // Lanjut ke halaman berikutnya
            Intent intent = new Intent(GenderSelectionActivity.this, InputDataActivity.class);
            intent.putExtra("USER_GENDER", isMaleSelected);
            intent.putExtra("DETECTED_COUNTRY", country);
            startActivity(intent);
        });
    }

    private void updateSelectionUI(boolean selectMale) {
        this.isMaleSelected = selectMale;

        if (selectMale) {
            // === MALE AKTIF ===
            cardMale.setCardBackgroundColor(COLOR_MALE_BG);
            cardMale.setCardElevation(8f); // Timbul
            imgMale.setColorFilter(COLOR_MALE_ACTIVE_TEXT);
            tvMale.setTextColor(COLOR_MALE_ACTIVE_TEXT);

            // === FEMALE PASIF ===
            cardFemale.setCardBackgroundColor(COLOR_WHITE);
            cardFemale.setCardElevation(0f); // Datar
            imgFemale.setColorFilter(COLOR_INACTIVE_TEXT);
            tvFemale.setTextColor(COLOR_INACTIVE_TEXT);

        } else {
            // === FEMALE AKTIF ===
            cardFemale.setCardBackgroundColor(COLOR_FEMALE_BG);
            cardFemale.setCardElevation(8f); // Timbul
            imgFemale.setColorFilter(COLOR_FEMALE_ACTIVE_TEXT);
            tvFemale.setTextColor(COLOR_FEMALE_ACTIVE_TEXT);

            // === MALE PASIF ===
            cardMale.setCardBackgroundColor(COLOR_WHITE);
            cardMale.setCardElevation(0f); // Datar
            imgMale.setColorFilter(COLOR_INACTIVE_TEXT);
            tvMale.setTextColor(COLOR_INACTIVE_TEXT);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setLanguage() {
        if (country != null && country.equalsIgnoreCase("Indonesia")) {
            tvTitle.setText("Pilih Gender");
            tvSubtitle.setText("Diperlukan untuk perhitungan akurat.");
            tvMale.setText("Pria");
            tvFemale.setText("Wanita");
            btnContinue.setText("Lanjutkan");
        } else if (country != null && country.equalsIgnoreCase("Spain")) {
            tvTitle.setText("Selecciona Género");
            tvSubtitle.setText("Necesario para un cálculo preciso.");
            tvMale.setText("Hombre");
            tvFemale.setText("Mujer");
            btnContinue.setText("Continuar");
        } else {
            // Default English
            tvTitle.setText("Select Gender");
            tvSubtitle.setText("We need this to calculate accurately.");
            tvMale.setText("Male");
            tvFemale.setText("Female");
            btnContinue.setText("Continue");
        }
    }
}