package com.fitme.app.ui.splash;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import com.fitme.app.R;
import com.fitme.app.ui.home.HomeActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private SplashViewModel viewModel;

    // UI Components
    private ImageView imgLogo, imgFlag;
    private LinearLayout layoutLocationInfo, layoutLoading, layoutTitleFinal;
    private TextView txtGreeting, txtCountry, txtCity, txtTagline;

    // Default Variabel (Indonesia sebagai Safety Net saat GPS mati/loading)
    private String detectedCountryString = "Indonesia";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        // 1. INIT VIEWS
        imgLogo = findViewById(R.id.imgLogo);
        layoutLocationInfo = findViewById(R.id.layoutLocationInfo);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutTitleFinal = findViewById(R.id.layoutTitleFinal);
        txtGreeting = findViewById(R.id.txtGreeting);
        txtCountry = findViewById(R.id.txtCountry);
        txtCity = findViewById(R.id.txtCity);
        txtTagline = findViewById(R.id.txtTagline);
        imgFlag = findViewById(R.id.imgFlag);

        // 2. SETUP ANIMASI AWAL (Sembunyikan elemen sebelum animasi)
        imgLogo.setAlpha(0f); imgLogo.setScaleX(0f); imgLogo.setScaleY(0f);
        layoutLocationInfo.setAlpha(0f); layoutLocationInfo.setTranslationY(-50f);
        txtGreeting.setAlpha(0f); txtGreeting.setTranslationY(50f);
        layoutLoading.setAlpha(0f);

        if (layoutTitleFinal != null) {
            layoutTitleFinal.setAlpha(0f);
            layoutTitleFinal.setVisibility(View.INVISIBLE);
        }

        // 3. JALANKAN ANIMASI MASUK
        imgLogo.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setDuration(1200).start();

        layoutLoading.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(1000)
                .start();

        // 4. OBSERVER DATA (Menerima Data dari ViewModel)
        viewModel.getUiState().observe(this, state -> {
            // Update Teks UI
            if (state.greetingLine1 != null) txtGreeting.setText(state.greetingLine1);
            if (state.greetingCountry != null) txtCountry.setText(state.greetingCountry);

            // Update Pin Lokasi (Nama Kota/Kecamatan)
            if (state.greetingLocation != null) txtCity.setText(state.greetingLocation);

            if (state.tagline != null && txtTagline != null) txtTagline.setText(state.tagline);

            // Update Bendera
            if (state.flagResId != 0) imgFlag.setImageResource(state.flagResId);

            // Simpan Negara untuk dikirim ke Home
            if (state.greetingCountry != null) {
                detectedCountryString = state.greetingCountry;
            }

            // Transisi Animasi (Saat Lokasi Ditemukan)
            // Mengubah Loading Bar menjadi Judul Aplikasi
            if (state.isLocationFound) {
                layoutLoading.animate()
                        .alpha(0f)
                        .translationY(50f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(300)
                        .withEndAction(() -> {
                            layoutLoading.setVisibility(View.GONE);
                            if (layoutTitleFinal != null) {
                                layoutTitleFinal.setVisibility(View.VISIBLE);
                                layoutTitleFinal.animate().alpha(1f).translationY(0f)
                                        .setInterpolator(new DecelerateInterpolator()).setDuration(800).start();
                            }
                            // Refresh posisi elemen lain agar terlihat dinamis
                            txtGreeting.animate().alpha(1f).translationY(0f).setDuration(800).start();
                            layoutLocationInfo.animate().alpha(1f).translationY(0f).setDuration(800).start();
                        }).start();
            }

            // Pindah ke Home
            if (state.isFinished) {
                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                intent.putExtra("DETECTED_COUNTRY", detectedCountryString);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        // 5. CEK IZIN & MULAI
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // PENTING: Kirim 'this' (Context) ke ViewModel
            viewModel.startSplashSequence(true, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        // PENTING: Kirim 'this' (Context) ke ViewModel di sini juga
        viewModel.startSplashSequence(isGranted, this);
    }
}