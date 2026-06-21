package com.fitme.app.ui.bmi;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.fitme.app.R;
import com.fitme.app.ui.base.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class GenderSelectionActivity extends BaseActivity {

    private TextView tvTitle, tvStepLabel, tvDescription, txtMale, txtFemale, tvStepHint;
    private MaterialCardView cardMale, cardFemale;
    private ImageView imgMale, imgFemale;
    private MaterialButton btnContinue;
    private View scrollContent, layoutBottom;

    private View barProgress;

    private Boolean currentSelection = null;
    private String countryCode = "ID", city = "";
    private boolean isFromOnboarding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_selection);

        if (getIntent() != null) {
            countryCode = getIntent().getStringExtra("COUNTRY_CODE") != null
                    ? getIntent().getStringExtra("COUNTRY_CODE") : "ID";
            city = getIntent().getStringExtra("DETECTED_CITY") != null
                    ? getIntent().getStringExtra("DETECTED_CITY") : "";
            isFromOnboarding = getIntent().getBooleanExtra("IS_FROM_ONBOARDING", false);
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        initViews();

        ImageView back = findViewById(R.id.btnBack);
        if (back != null) {
            back.setVisibility(isFromOnboarding ? View.GONE : View.VISIBLE);
            back.setOnClickListener(v -> handleBack());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });

        resetToIdle();

        if (barProgress != null) {
            updateBarWeight(barProgress, 0f);
            animateProgressBar(0f, 33f, 380, 420);
        }

        cardMale.setOnClickListener(v -> onGenderClicked(true));
        cardFemale.setOnClickListener(v -> onGenderClicked(false));

        btnContinue.setOnClickListener(v -> {
            if (currentSelection == null) return;
            Intent intent = new Intent(this, InputDataActivity.class);
            intent.putExtra("USER_GENDER", currentSelection);
            intent.putExtra("COUNTRY_CODE", countryCode);
            intent.putExtra("DETECTED_CITY", city);
            intent.putExtra("IS_FROM_ONBOARDING", isFromOnboarding);
            startActivity(intent);
        });
    }

    private void handleBack() {
        if (!isFromOnboarding) {
            finish();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvStepLabel = findViewById(R.id.tvStepLabel);
        tvDescription = findViewById(R.id.tvDescription);
        cardMale = findViewById(R.id.cardMale);
        cardFemale = findViewById(R.id.cardFemale);
        imgMale = findViewById(R.id.imgMale);
        imgFemale = findViewById(R.id.imgFemale);
        txtMale = findViewById(R.id.txtMale);
        txtFemale = findViewById(R.id.txtFemale);
        btnContinue = findViewById(R.id.btnContinue);

        tvStepHint = findViewById(R.id.tvBtnSubtext);

        scrollContent = findViewById(R.id.scrollContent);
        layoutBottom = findViewById(R.id.layoutBottom);
        barProgress = findViewById(R.id.barProgress);
    }

    private void updateBarWeight(View bar, float weight) {
        if (bar == null) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.weight = weight;
        bar.setLayoutParams(params);
        LinearLayout parent = (LinearLayout) bar.getParent();
        if (parent != null && parent.getChildCount() > 1) {
            View empty = parent.getChildAt(1);
            if (empty != null) {
                LinearLayout.LayoutParams ep = (LinearLayout.LayoutParams) empty.getLayoutParams();
                ep.weight = 100f - weight;
                empty.setLayoutParams(ep);
            }
        }
    }

    private void animateProgressBar(float start, float end, long duration, long delay) {
        ValueAnimator anim = ValueAnimator.ofFloat(start, end);
        anim.setDuration(duration);
        anim.setStartDelay(delay);
        anim.setInterpolator(new DecelerateInterpolator(2.0f));
        anim.addUpdateListener(a -> updateBarWeight(barProgress, (float) a.getAnimatedValue()));
        anim.start();
    }

    private void onGenderClicked(boolean isMale) {
        if (currentSelection != null && currentSelection == isMale) return;
        currentSelection = isMale;

        MaterialCardView sel = isMale ? cardMale : cardFemale;
        MaterialCardView unsel = isMale ? cardFemale : cardMale;
        ImageView selImg = isMale ? imgMale : imgFemale;
        ImageView unselImg = isMale ? imgFemale : imgMale;

        sel.animate().translationY(0f).scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(280)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
        sel.setStrokeColor(ContextCompat.getColor(this, R.color.fitme_green));
        sel.setStrokeWidth((int) (2.5f * getResources().getDisplayMetrics().density));

        selImg.animate().scaleX(1.06f).scaleY(1.06f)
                .setDuration(280)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        unsel.animate().translationY(0f).scaleX(0.88f).scaleY(0.88f).alpha(0.45f)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();
        unsel.setStrokeColor(ContextCompat.getColor(this, R.color.fitme_border));
        unsel.setStrokeWidth((int) (1.5f * getResources().getDisplayMetrics().density));

        unselImg.animate().scaleX(1f).scaleY(1f)
                .setDuration(200)
                .start();

        txtMale.setTextColor(ContextCompat.getColor(this, isMale ? R.color.fitme_green : R.color.fitme_text_secondary));
        txtMale.setAlpha(isMale ? 1f : 0.4f);
        txtFemale.setTextColor(ContextCompat.getColor(this, !isMale ? R.color.fitme_green : R.color.fitme_text_secondary));
        txtFemale.setAlpha(!isMale ? 1f : 0.4f);

        btnContinue.setEnabled(true);
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.fitme_green)));
        btnContinue.setTextColor(ContextCompat.getColor(this, R.color.white));
        btnContinue.setScaleX(0.96f);
        btnContinue.setScaleY(0.96f);
        btnContinue.animate().scaleX(1f).scaleY(1f)
                .setDuration(260)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .start();

        if (tvStepHint != null) {
            tvStepHint.setTextColor(ContextCompat.getColor(this, R.color.fitme_text_hint));
            tvStepHint.setText(getString(R.string.step_hint_filled));
        }
    }

    private void resetToIdle() {
        currentSelection = null;
        int dp2 = (int) (2 * getResources().getDisplayMetrics().density);

        cardMale.setStrokeColor(ContextCompat.getColor(this, R.color.fitme_border));
        cardFemale.setStrokeColor(ContextCompat.getColor(this, R.color.fitme_border));
        cardMale.setStrokeWidth(dp2);
        cardFemale.setStrokeWidth(dp2);
        txtMale.setTextColor(ContextCompat.getColor(this, R.color.fitme_text_secondary));
        txtMale.setAlpha(1f);
        txtFemale.setTextColor(ContextCompat.getColor(this, R.color.fitme_text_secondary));
        txtFemale.setAlpha(1f);

        btnContinue.setEnabled(false);
        btnContinue.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.fitme_green_disabled)));
        btnContinue.setTextColor(ContextCompat.getColor(this, R.color.white));

        if (tvStepHint != null) {
            tvStepHint.setTextColor(ContextCompat.getColor(this, R.color.fitme_text_hint));
            tvStepHint.setText(getString(R.string.step_hint_empty));
        }
    }
}