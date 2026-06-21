package com.fitme.app.ui.onboarding;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.fitme.app.R;
import com.fitme.app.ui.base.BaseActivity;
import com.fitme.app.ui.bmi.GenderSelectionActivity;
import com.fitme.app.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends BaseActivity {

    private ViewPager2        viewPager;
    private LinearLayout      dotsContainer;
    private MaterialButton    btnNext;
    private TextView          btnSkip;
    private OnboardingAdapter adapter;

    private static final int TOTAL_PAGES = 3;
    private String countryCode  = "ID";
    private String detectedCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Membuat layar menjadi Immersive/Fullscreen penuh
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            // Mengubah icon status bar menjadi gelap agar terlihat di background terang
            controller.setAppearanceLightStatusBars(true);
        }

        setContentView(R.layout.activity_onboarding);

        if (getIntent() != null) {
            if (getIntent().hasExtra("COUNTRY_CODE"))
                countryCode = getIntent().getStringExtra("COUNTRY_CODE");
            if (getIntent().hasExtra("DETECTED_CITY"))
                detectedCity = getIntent().getStringExtra("DETECTED_CITY");
        }

        viewPager     = findViewById(R.id.viewPager);
        dotsContainer = findViewById(R.id.dotsContainer);
        btnNext       = findViewById(R.id.btnNext);
        btnSkip       = findViewById(R.id.btnSkip);

        setupPages();
        setupDots(0);
        setupListeners();
    }

    private void setupPages() {
        List<OnboardingPage> pages = new ArrayList<>();

        pages.add(new OnboardingPage(
                R.drawable.ic_onboard_food,
                getString(R.string.onboard_1_title),
                getString(R.string.onboard_1_desc),
                "#FFFFFF", "#068562"));

        pages.add(new OnboardingPage(
                R.drawable.ic_onboard_health,
                getString(R.string.onboard_2_title),
                getString(R.string.onboard_2_desc),
                "#FFFFFF", "#068562"));

        pages.add(new OnboardingPage(
                R.drawable.ic_onboard_bmi,
                getString(R.string.onboard_3_title),
                getString(R.string.onboard_3_desc),
                "#FFFFFF", "#068562"));

        adapter = new OnboardingAdapter(this, pages);
        viewPager.setAdapter(adapter);

        // ✅ BUG FIX: Set offscreen limit agar halaman selanjutnya sudah di-load (mencegah kedipan)
        viewPager.setOffscreenPageLimit(2);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                animateDots(position);
                updateButtons(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // ✅ BUG FIX: Hanya trigger animasi konten saat viewpager benar-benar berhenti (IDLE)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    adapter.triggerAnimation(viewPager.getCurrentItem());
                }
            }
        });

        // ✅ BUG FIX: Delay sedikit diperbesar agar transisi dari splash screen benar-benar selesai
        viewPager.postDelayed(() -> {
            if (adapter != null && viewPager.getCurrentItem() == 0) {
                adapter.triggerAnimation(0);
            }
        }, 500);
    }

    private void setupDots(int current) {
        dotsContainer.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;

        // ✅ BUG FIX: Menyamakan tinggi dot agar animasi lebar tidak mempengaruhi tinggi layout
        int h = Math.round(6 * dp);

        for (int i = 0; i < TOTAL_PAGES; i++) {
            View dot = new View(this);
            int  w   = i == current ? Math.round(24 * dp) : Math.round(6 * dp);

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
            p.setMargins(Math.round(4 * dp), 0, Math.round(4 * dp), 0);
            dot.setLayoutParams(p);

            // ✅ Menggunakan resource drawable kustom yang harus ada di folder Anda
            dot.setBackgroundResource(i == current
                    ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    private void animateDots(int current) {
        float dp        = getResources().getDisplayMetrics().density;
        int   activeW   = Math.round(24 * dp);
        int   inactiveW = Math.round(6  * dp);

        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            if (dot == null) continue;

            boolean isActive = (i == current);
            int     targetW  = isActive ? activeW : inactiveW;

            // Cek apakah animasi diperlukan (jika lebar saat ini tidak sama dengan target)
            if (dot.getLayoutParams().width != targetW) {
                ValueAnimator wa = ValueAnimator.ofInt(dot.getLayoutParams().width, targetW);
                wa.setDuration(300); // ✅ Durasi sedikit diperpanjang agar lebih smooth
                wa.setInterpolator(new DecelerateInterpolator(1.5f));
                wa.addUpdateListener(a -> {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dot.getLayoutParams();
                    lp.width = (int) a.getAnimatedValue();
                    dot.setLayoutParams(lp);
                });
                wa.start();
            }

            dot.setBackgroundResource(isActive
                    ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        }
    }

    private void updateButtons(int position) {
        if (position == TOTAL_PAGES - 1) {
            btnNext.setText(getString(R.string.btn_start_onboard));
            btnSkip.setVisibility(View.INVISIBLE); // Gunakan INVISIBLE agar posisi titik tidak bergeser
        } else {
            btnNext.setText(getString(R.string.btn_next));
            btnSkip.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            int cur = viewPager.getCurrentItem();
            if (cur < TOTAL_PAGES - 1) {
                viewPager.setCurrentItem(cur + 1, true); // true = smooth scroll
            } else {
                goToGenderSelection();
            }
        });

        btnSkip.setOnClickListener(v -> goToGenderSelection());
    }

    private void goToGenderSelection() {
        PrefManager pref = new PrefManager(this);
        String code = (countryCode != null && !countryCode.equals("ID"))
                ? countryCode : pref.getCountryCode();

        Intent intent = new Intent(this, GenderSelectionActivity.class);
        intent.putExtra("COUNTRY_CODE",       code);
        intent.putExtra("DETECTED_CITY",      detectedCity);
        intent.putExtra("IS_FROM_ONBOARDING", true);

        // Membersihkan tumpukan activity (stack) agar user tidak bisa kembali ke onboarding
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                    R.anim.anim_slide_up_fade_in, R.anim.anim_fade_out_smooth);
        } else {
            //noinspection deprecation
            overridePendingTransition(R.anim.anim_slide_up_fade_in, R.anim.anim_fade_out_smooth);
        }
        finish();
    }
}