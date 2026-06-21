package com.fitme.app.ui.splash;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.murgupluoglu.flagkit.FlagKit;
import com.fitme.app.R;
import com.fitme.app.ui.home.HomeActivity;
import com.fitme.app.ui.onboarding.OnboardingActivity;
import com.fitme.app.utils.LocaleHelper;
import com.fitme.app.utils.PrefManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private SplashViewModel viewModel;
    private ImageView       imgLogo, imgFlag;
    private LinearLayout    layoutLocationInfo, layoutLoading, layoutFitmeLetters, layoutTitleFinal;
    private TextView        txtCountry, txtCity, txtTagline;
    private TextView        txtLetterF, txtLetterI, txtLetterT, txtLetterM, txtLetterE;
    private ProgressBar     progressLoading;

    private String  countryCode           = "ID";
    private String  detectedCountryString = "Indonesia";
    private String  detectedCityString    = "";
    private boolean isNavigating          = false;
    private boolean revealDone            = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long splashStartTime;

    // ── Timing ──────────────────────────────────────────────
    private static final long LOGO_FADE_IN_DURATION = 1100L;
    private static final long LOADING_IN_DURATION   = 400L;
    private static final long LOADING_OUT_MS        = 300L;
    private static final long HEADER_SLIDE_IN_MS    = 500L;
    private static final long HEADER_TO_FITME_GAP   = 100L;
    private static final long FITME_STAGGER_MS      = 60L;
    private static final long FITME_LETTER_DURATION = 500L;
    private static final long TAGLINE_DELAY_AFTER_E = 100L;
    private static final long TAGLINE_DURATION      = 450L;
    private static final long SHOW_CONTENT_MS       = 800L;
    private static final long TRANSITION_ANIM_MS    = 500L;

    // ── Gradient colors (sama dengan bg_splash_gradient.xml) ──
    private static final int[] GRADIENT_COLORS = {
            Color.parseColor("#13B89A"), // startColor
            Color.parseColor("#0D8F72"), // centerColor
            Color.parseColor("#065C4F")  // endColor
    };

    private GradientDrawable buildGradient() {
        return new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                GRADIENT_COLORS
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener(splashScreenView -> {
                splashScreenView.getView().setBackground(buildGradient());
                splashScreenView.remove();
            });
        }

        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(buildGradient());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        setContentView(R.layout.activity_splash);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        bindViews();
        resetAllToInitialState();

        imgLogo.post(this::phase1LogoEntrance);

        observeViewModel();
        requestLocationPermission();
        splashStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void bindViews() {
        imgLogo            = findViewById(R.id.imgLogo);
        imgFlag            = findViewById(R.id.imgFlag);
        layoutLocationInfo = findViewById(R.id.layoutLocationInfo);
        layoutLoading      = findViewById(R.id.layoutLoading);
        layoutTitleFinal   = findViewById(R.id.layoutTitleFinal);
        layoutFitmeLetters = findViewById(R.id.layoutFitmeLetters);
        txtCountry         = findViewById(R.id.txtCountry);
        txtCity            = findViewById(R.id.txtCity);
        txtTagline         = findViewById(R.id.txtTagline);
        txtLetterF         = findViewById(R.id.txtLetterF);
        txtLetterI         = findViewById(R.id.txtLetterI);
        txtLetterT         = findViewById(R.id.txtLetterT);
        txtLetterM         = findViewById(R.id.txtLetterM);
        txtLetterE         = findViewById(R.id.txtLetterE);
        progressLoading    = findViewById(R.id.progressLoading);
    }

    private void resetAllToInitialState() {
        if (imgLogo != null) {
            imgLogo.setAlpha(0.01f);
            imgLogo.setTranslationY(60f);
            imgLogo.setScaleX(0.85f);
            imgLogo.setScaleY(0.85f);
        }

        setV(layoutLocationInfo, 0f, -30f, 1f, 1f);
        setV(layoutLoading,      0f,  30f, 1f, 1f);

        if (layoutTitleFinal != null) {
            layoutTitleFinal.setVisibility(View.INVISIBLE);
            layoutTitleFinal.setAlpha(0f);
        }

        for (TextView tv : fitmeLetters()) {
            if (tv != null) setV(tv, 0f, 40f, 0.8f, 0.8f);
        }

        if (txtTagline != null) setV(txtTagline, 0f, 15f, 1f, 1f);
    }

    private void phase1LogoEntrance() {
        if (imgLogo == null || isFinishing() || isDestroyed()) return;

        imgLogo.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(LOGO_FADE_IN_DURATION)
                .setInterpolator(new DecelerateInterpolator(2.0f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        phase2ShowLoading();
                    }
                })
                .start();
    }

    private void phase2ShowLoading() {
        if (layoutLoading == null || isFinishing() || isDestroyed()) return;

        layoutLoading.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(LOADING_IN_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            if (state == null || revealDone) return;
            if (state.countryCode == null) return;

            revealDone = true;
            long elapsed = System.currentTimeMillis() - splashStartTime;
            long delay   = Math.max(0, 2000 - elapsed);

            handler.postDelayed(() -> {
                if (isFinishing() || isDestroyed()) return;

                detectedCountryString = state.greetingCountry  != null
                        ? state.greetingCountry  : "Indonesia";
                detectedCityString    = state.greetingLocation != null
                        ? state.greetingLocation : "";
                countryCode           = state.countryCode;

                LocaleHelper.setLocale(this, countryCode);
                new PrefManager(this).saveLocation(countryCode, detectedCityString);

                if (txtTagline != null && state.tagline != null)
                    txtTagline.setText(state.tagline);

                setFlagByCode(countryCode);
                updateHeaderViews();

                if (progressLoading != null)
                    progressLoading.animate().alpha(0f).setDuration(200).start();

                handler.postDelayed(this::phase3RevealContent, 700);
            }, delay);
        });
    }

    private void phase3RevealContent() {
        if (layoutLoading == null || isFinishing() || isDestroyed()) return;

        layoutLoading.animate()
                .alpha(0f)
                .translationY(-30f)
                .setDuration(LOADING_OUT_MS)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(this::phase3bShowLocationHeader)
                .start();
    }

    private void phase3bShowLocationHeader() {
        if (layoutLocationInfo == null || isFinishing() || isDestroyed()) return;

        layoutLocationInfo.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(HEADER_SLIDE_IN_MS)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() ->
                        handler.postDelayed(this::phase4ShowTitle, HEADER_TO_FITME_GAP))
                .start();
    }

    private void phase4ShowTitle() {
        if (isFinishing() || isDestroyed()) return;

        if (layoutTitleFinal != null) {
            layoutTitleFinal.setVisibility(View.VISIBLE);
            layoutTitleFinal.setAlpha(1f);
        }

        TextView[] letters = fitmeLetters();
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] == null) continue;
            letters[i].animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay((long) i * FITME_STAGGER_MS)
                    .setDuration(FITME_LETTER_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        long totalDelay = (letters.length * FITME_STAGGER_MS)
                + FITME_LETTER_DURATION + TAGLINE_DELAY_AFTER_E;

        handler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;

            if (txtTagline != null) {
                txtTagline.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(TAGLINE_DURATION)
                        .start();
            }

            handler.postDelayed(this::navigateNext, TAGLINE_DURATION + SHOW_CONTENT_MS);
        }, totalDelay);
    }

    private void navigateNext() {
        if (isNavigating || isFinishing() || isDestroyed()) return;
        isNavigating = true;

        PrefManager pref   = new PrefManager(this);
        Class<?>    target = pref.isOnboardingDone()
                ? HomeActivity.class : OnboardingActivity.class;

        Intent intent = new Intent(this, target);
        intent.putExtra("COUNTRY_CODE", countryCode);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,
                    R.anim.anim_splash_enter, R.anim.anim_splash_exit);
        } else {
            overridePendingTransition(R.anim.anim_splash_enter, R.anim.anim_splash_exit);
        }

        handler.postDelayed(this::finish, TRANSITION_ANIM_MS);
    }

    private TextView[] fitmeLetters() {
        return new TextView[]{txtLetterF, txtLetterI, txtLetterT, txtLetterM, txtLetterE};
    }

    private void updateHeaderViews() {
        if (txtCountry != null)
            txtCountry.setText(detectedCountryString);

        if (txtCity != null && !detectedCityString.isEmpty()) {
            txtCity.setVisibility(View.VISIBLE);
            txtCity.setText(detectedCityString);
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            viewModel.startSplashSequence(true, this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            viewModel.startSplashSequence(granted, this);
        }
    }

    private void setFlagByCode(String code) {
        if (imgFlag == null || code == null) return;
        try {
            int id = FlagKit.INSTANCE.getResId(code.toLowerCase());
            imgFlag.setImageResource(id != 0 ? id : R.drawable.flag_id);
        } catch (Exception e) {
            imgFlag.setImageResource(R.drawable.flag_id);
        }
    }

    private void setV(View v, float a, float ty, float sx, float sy) {
        if (v != null) {
            v.setAlpha(a);
            v.setTranslationY(ty);
            v.setScaleX(sx);
            v.setScaleY(sy);
        }
    }
}