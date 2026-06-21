package com.fitme.app.ui.bmi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fitme.app.R;
import com.fitme.app.ui.base.BaseActivity;
import com.fitme.app.ui.home.HomeActivity;
import com.fitme.app.utils.PrefManager;
import com.fitme.app.utils.ProgressBarHelper;
import com.fitme.app.utils.ShareHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class ResultActivity extends BaseActivity {

    private float bmiResult, userWeight, userHeight;
    private int userAge;
    private boolean isMale = true;

    private String countryCode = "";
    private String city = "";
    private String savedCategory = "";

    private TextView tvBmiInt, tvBmiDec, tvBmiStatus, tvWeightVal, tvHeightVal, tvAgeVal,
            tvAiContent, tvAiStatusLabel, tvAiTitle, tvAiMode, tvPoweredBy, tvOfflineTips;

    private MaterialCardView cardStatusBadge;
    private View imgIndicator, barProgress;
    private LinearLayout layoutOfflineTips;
    private MaterialButton btnFinish;
    private View btnShare, btnRecalculate;

    private BmiViewModel bmiViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        bmiViewModel = new ViewModelProvider(this).get(BmiViewModel.class);

        // Inisialisasi PrefManager untuk pengamanan data bahasa
        PrefManager pref = new PrefManager(this);

        if (getIntent() != null) {
            bmiResult  = getIntent().getFloatExtra("BMI_RESULT", 0f);
            userAge    = getIntent().getIntExtra("USER_AGE", 25);
            isMale     = getIntent().getBooleanExtra("USER_GENDER", true);
            userWeight = getIntent().getFloatExtra("USER_WEIGHT", 0f);
            userHeight = getIntent().getFloatExtra("USER_HEIGHT", 0f);

            // ✅ AMAN: Ambil dari Intent. Jika kosong, ambil dari memori (PrefManager)
            countryCode = getIntent().hasExtra("COUNTRY_CODE")
                    ? getIntent().getStringExtra("COUNTRY_CODE")
                    : pref.getCountryCode();

            city = getIntent().hasExtra("DETECTED_CITY")
                    ? getIntent().getStringExtra("DETECTED_CITY")
                    : pref.getDetectedCity();
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        initViews();
        displayBmiResult();
        setupListeners();
        setupBottomSheet();
        observeViewModel();

        bmiViewModel.fetchAiAdvice(
                bmiResult, savedCategory, userAge, isMale, userHeight, countryCode, city);

        bmiViewModel.saveAllData(
                bmiResult, savedCategory, userAge, userWeight, userHeight,
                isMale, countryCode, city,
                isMale ? getString(R.string.gender_male) : getString(R.string.gender_female));

        if (barProgress != null) {
            ProgressBarHelper.updateBarWeight(barProgress, 67f);
            ProgressBarHelper.animateProgressBar(barProgress, 67f, 100f, 380, 450);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Arahkan ke Home saat menekan tombol back hardware
                Intent i = new Intent(ResultActivity.this, HomeActivity.class);
                i.putExtra("COUNTRY_CODE", countryCode);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        });
    }

    private void observeViewModel() {
        bmiViewModel.getAiAdviceState().observe(this, aiResult -> {
            if (aiResult.isOnline) {
                showOnlineMode(aiResult.content);
            } else {
                showOfflineMode(aiResult.content);
            }
        });
    }

    private void initViews() {
        tvBmiInt         = findViewById(R.id.tvBmiInt);
        tvBmiDec         = findViewById(R.id.tvBmiDec);
        tvBmiStatus      = findViewById(R.id.tvBmiStatus);
        cardStatusBadge  = findViewById(R.id.cardStatusBadge);
        imgIndicator     = findViewById(R.id.imgIndicator);
        tvWeightVal      = findViewById(R.id.tvWeightVal);
        tvHeightVal      = findViewById(R.id.tvHeightVal);
        tvAgeVal         = findViewById(R.id.tvAgeVal);
        tvAiContent      = findViewById(R.id.tvAiContent);
        tvAiStatusLabel  = findViewById(R.id.tvAiStatusLabel);
        tvAiTitle        = findViewById(R.id.tvAiTitle);
        tvAiMode         = findViewById(R.id.tvAiMode);
        tvPoweredBy      = findViewById(R.id.tvPoweredBy);
        tvOfflineTips    = findViewById(R.id.tvOfflineTips);
        layoutOfflineTips = findViewById(R.id.layoutOfflineTips);
        btnFinish        = findViewById(R.id.btnFinish);
        btnShare         = findViewById(R.id.btnShare);
        btnRecalculate   = findViewById(R.id.btnRecalculate);
        barProgress      = findViewById(R.id.barProgress);
    }

    private void displayBmiResult() {
        String[] p = String.format(Locale.US, "%.1f", bmiResult).split("\\.");
        tvBmiInt.setText(p[0]);
        tvBmiDec.setText("." + p[1]);
        tvWeightVal.setText(String.valueOf((int) userWeight));
        tvHeightVal.setText(String.valueOf((int) userHeight));
        tvAgeVal.setText(String.valueOf(userAge));

        int statusColor, badgeBg;

        if (bmiResult < 18.5f) {
            savedCategory = getString(R.string.bmi_underweight);
            statusColor   = ContextCompat.getColor(this, R.color.bmi_underweight);
            badgeBg       = Color.parseColor("#EEF4F5");
        } else if (bmiResult < 25.0f) {
            savedCategory = getString(R.string.bmi_normal);
            statusColor   = ContextCompat.getColor(this, R.color.bmi_normal);
            badgeBg       = Color.parseColor("#EEF5F1");
        } else if (bmiResult < 30.0f) {
            savedCategory = getString(R.string.bmi_overweight);
            statusColor   = ContextCompat.getColor(this, R.color.bmi_overweight);
            badgeBg       = Color.parseColor("#F0F4F2");
        } else {
            savedCategory = getString(R.string.bmi_obese);
            statusColor   = ContextCompat.getColor(this, R.color.bmi_obese);
            badgeBg       = Color.parseColor("#F5EEEC");
        }

        tvBmiStatus.setText(savedCategory);
        tvBmiStatus.setTextColor(statusColor);
        cardStatusBadge.setCardBackgroundColor(badgeBg);

        imgIndicator.post(() -> imgIndicator.setTranslationX(
                bmiViewModel.calculateBarFraction(bmiResult)
                        * ((View) imgIndicator.getParent()).getWidth()
                        - imgIndicator.getWidth() / 2f
        ));
    }

    private void setupBottomSheet() {
        ImageView arr = findViewById(R.id.imgSheetArrow);
        BottomSheetBehavior.from(findViewById(R.id.aiBottomSheet))
                .addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View b, int s) {
                        arr.setRotation(
                                s == BottomSheetBehavior.STATE_EXPANDED ? 90f : 270f);
                    }
                    @Override
                    public void onSlide(@NonNull View b, float o) {}
                });
    }

    private void showOnlineMode(String advice) {
        tvAiContent.setVisibility(View.VISIBLE);
        layoutOfflineTips.setVisibility(View.GONE);
        tvPoweredBy.setVisibility(View.VISIBLE);
        tvAiContent.setText(HtmlCompat.fromHtml(advice, HtmlCompat.FROM_HTML_MODE_COMPACT));
        tvAiStatusLabel.setText(getString(R.string.ai_done));
        tvAiMode.setText("ONLINE");
        tvAiMode.setTextColor(Color.parseColor("#7DDEAA"));
    }

    private void showOfflineMode(String offlineTips) {
        tvAiContent.setVisibility(View.GONE);
        layoutOfflineTips.setVisibility(View.VISIBLE);
        tvPoweredBy.setVisibility(View.GONE);
        tvOfflineTips.setText(
                HtmlCompat.fromHtml(offlineTips, HtmlCompat.FROM_HTML_MODE_COMPACT));
        tvAiStatusLabel.setText(getString(R.string.ai_offline_tips));
        tvAiTitle.setText(getString(R.string.ai_tips_title));
        tvAiMode.setText("OFFLINE");
        tvAiMode.setTextColor(Color.parseColor("#FFC87A"));
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Intent i = new Intent(ResultActivity.this, HomeActivity.class);
            i.putExtra("COUNTRY_CODE", countryCode);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });

        if (btnFinish != null) {
            btnFinish.setOnClickListener(v -> {
                Intent i = new Intent(this, HomeActivity.class);
                i.putExtra("COUNTRY_CODE", countryCode);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            });
        }

        if (btnRecalculate != null) {
            btnRecalculate.setOnClickListener(v -> {
                // ✅ REVISI: Langsung ke InputDataActivity tanpa lewat Gender lagi
                Intent i = new Intent(this, InputDataActivity.class);
                i.putExtra("COUNTRY_CODE", countryCode);
                i.putExtra("DETECTED_CITY", city);
                i.putExtra("USER_GENDER", isMale); // Bawa data gender sebelumnya
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v ->
                    ShareHelper.shareBmiResult(this, bmiResult, savedCategory, userAge, isMale));
        }
    }
}