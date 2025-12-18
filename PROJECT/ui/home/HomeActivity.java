package com.fitme.app.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.fitme.app.R;
import com.fitme.app.ui.bmi.GenderSelectionActivity;
import com.fitme.app.ui.history.HistoryActivity;

public class HomeActivity extends AppCompatActivity {

    // Default "Indonesia" sebagai safety net
    private String detectedCountry = "Indonesia";

    // Variabel UI Header & Kartu Utama
    private TextView tvGreetingName, tvGreetingSub, tvHeadline, tvSubhead, btnCalculate;

    // Variabel Judul Kategori
    private TextView tvInfoTitle;

    // === VARIABEL BARU (SOLUSI MASALAH GAMBAR) ===
    // Untuk label kecil di dalam kotak: "Thin", "Normal", "Obese"
    private TextView tvLabelThin, tvLabelNormal, tvLabelObese;

    // Navigasi Bawah
    private TextView tvNavHome, tvNavHistory;
    private LinearLayout btnNavHistory;

    // Tombol Hitung (CardView wrapper di XML)
    private CardView cardBtnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. TERIMA DATA DARI SPLASH SCREEN
        if (getIntent() != null && getIntent().hasExtra("DETECTED_COUNTRY")) {
            String country = getIntent().getStringExtra("DETECTED_COUNTRY");
            if (country != null) {
                detectedCountry = country;
            }
        }

        // 2. INISIALISASI VIEWS
        initViews();

        // 3. TERJEMAHKAN BAHASA
        applyLanguage(detectedCountry);

        // 4. LISTENER TOMBOL
        setupListeners();
    }

    private void initViews() {
        // Header
        tvGreetingName = findViewById(R.id.tvGreetingName);
        tvGreetingSub = findViewById(R.id.tvGreetingSub);

        // Kartu Utama (Hitung BMI)
        tvHeadline = findViewById(R.id.tvHeadline);
        tvSubhead = findViewById(R.id.tvSubhead);
        btnCalculate = findViewById(R.id.btnCalculate); // Teks tombol "Start Now"

        // Kita butuh listener pada tombol di dalam kartu
        // (Bisa dipasang di Button-nya langsung atau CardView-nya)

        // Judul Kategori
        tvInfoTitle = findViewById(R.id.tvInfoTitle);

        // === LABEL KATEGORI (MASALAH UTAMA) ===
        tvLabelThin = findViewById(R.id.tvLabelThin);
        tvLabelNormal = findViewById(R.id.tvLabelNormal);
        tvLabelObese = findViewById(R.id.tvLabelObese);

        // Navigasi Bawah
        btnNavHistory = findViewById(R.id.btnNavHistory);
        tvNavHome = findViewById(R.id.tvNavHome);
        tvNavHistory = findViewById(R.id.tvNavHistory);
    }

    private void setupListeners() {
        // 1. Tombol Mulai / Hitung
        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, GenderSelectionActivity.class);
                intent.putExtra("DETECTED_COUNTRY", detectedCountry); // Bawa info negara
                startActivity(intent);
            });
        }

        // 2. Tombol Navigasi Riwayat
        if (btnNavHistory != null) {
            btnNavHistory.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                // Opsional: Bawa info negara ke history jika perlu
                startActivity(intent);
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void applyLanguage(String country) {
        // Normalisasi string agar tidak error huruf besar/kecil
        String lang = (country != null) ? country.toLowerCase() : "indonesia";

        // Cek apakah varian Inggris (US, UK, England, dll)
        boolean isEnglish = lang.contains("united") || lang.contains("england") || lang.contains("us");
        boolean isSpanish = lang.contains("spain") || lang.contains("españa");

        if (isSpanish) {
            // === BAHASA SPANYOL ===
            if (tvGreetingName != null) tvGreetingName.setText("¡Hola, Amigo!");
            if (tvGreetingSub != null) tvGreetingSub.setText("Vamos a revisar tu salud hoy.");

            if (tvHeadline != null) tvHeadline.setText("Calcular IMC");
            if (tvSubhead != null) tvSubhead.setText("Monitorea tu salud con precisión.");
            if (btnCalculate != null) btnCalculate.setText("Empezar Ahora");

            if (tvInfoTitle != null) tvInfoTitle.setText("Categorías IMC");

            // Perbaikan Label Gambar
            if (tvLabelThin != null) tvLabelThin.setText("Delgado");
            if (tvLabelNormal != null) tvLabelNormal.setText("Normal");
            if (tvLabelObese != null) tvLabelObese.setText("Obesidad");

            if (tvNavHome != null) tvNavHome.setText("Inicio");
            if (tvNavHistory != null) tvNavHistory.setText("Historial");

        } else if (isEnglish) {
            // === BAHASA INGGRIS ===
            if (tvGreetingName != null) tvGreetingName.setText("Hello, Friend!");
            if (tvGreetingSub != null) tvGreetingSub.setText("Let's check your health today.");

            if (tvHeadline != null) tvHeadline.setText("Calculate BMI");
            if (tvSubhead != null) tvSubhead.setText("Monitor your health accurately.");
            if (btnCalculate != null) btnCalculate.setText("Start Now");

            if (tvInfoTitle != null) tvInfoTitle.setText("BMI Categories");

            // Label Inggris
            if (tvLabelThin != null) tvLabelThin.setText("Thin");
            if (tvLabelNormal != null) tvLabelNormal.setText("Normal");
            if (tvLabelObese != null) tvLabelObese.setText("Obese");

            if (tvNavHome != null) tvNavHome.setText("Home");
            if (tvNavHistory != null) tvNavHistory.setText("History");

        } else {
            // === BAHASA INDONESIA (Default) ===
            if (tvGreetingName != null) tvGreetingName.setText("Halo, Kawan!");
            if (tvGreetingSub != null) tvGreetingSub.setText("Mari cek kesehatan tubuhmu hari ini.");

            if (tvHeadline != null) tvHeadline.setText("Hitung BMI");
            if (tvSubhead != null) tvSubhead.setText("Pantau kesehatan tubuhmu dengan akurat.");
            if (btnCalculate != null) btnCalculate.setText("Mulai Sekarang");

            if (tvInfoTitle != null) tvInfoTitle.setText("Kategori BMI");

            // Perbaikan Label Gambar: Thin -> Kurus
            if (tvLabelThin != null) tvLabelThin.setText("Kurus");
            if (tvLabelNormal != null) tvLabelNormal.setText("Normal");
            if (tvLabelObese != null) tvLabelObese.setText("Obesitas");

            if (tvNavHome != null) tvNavHome.setText("Beranda");
            if (tvNavHistory != null) tvNavHistory.setText("Riwayat");
        }
    }
}