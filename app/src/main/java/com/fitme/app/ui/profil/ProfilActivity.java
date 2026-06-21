package com.fitme.app.ui.profil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fitme.app.R;
import com.fitme.app.ui.base.BaseActivity;
import com.fitme.app.ui.bmi.GenderSelectionActivity;
import com.fitme.app.utils.PrefManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

public class ProfilActivity extends BaseActivity {

    private ProfilViewModel vm;

    // Views
    private LinearLayout   layoutHeader;
    private FrameLayout    frameAvatar;
    private ImageView      ivProfilePhoto;
    private TextView       tvUserName, tvUserSummary, tvUserPhysical;
    private LinearLayout   layoutEditName;
    private TextView       tvBmiValue, tvBmiCategory;
    private TextView       tvWeightValue, tvHeightValue, tvAgeValue, tvTdeeValue;
    private View btnUpdateData, btnReset;

    // Photo picker
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) handlePhotoSelected(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIX: Biarkan layout meluas ke belakang status bar secara benar
        // WindowCompat.setDecorFitsSystemWindows(false) → konten bisa ke balik status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // --- FIX STATUS BAR HITAM ---
        // Memaksa ikon status bar (jam, baterai, sinyal) menjadi PUTIH
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);
        // ----------------------------

        setContentView(R.layout.activity_profil);

        vm = new ViewModelProvider(this).get(ProfilViewModel.class);

        bindViews();
        applyWindowInsets(); // FIX: padding dinamis sesuai tinggi status bar device
        setupStaticLabels();
        setupClickListeners();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vm.loadProfile();
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    private void bindViews() {
        layoutHeader   = findViewById(R.id.layoutHeaderInner); // LinearLayout di dalam FrameLayout header
        frameAvatar    = findViewById(R.id.frameAvatar);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvUserName     = findViewById(R.id.tvUserName);
        tvUserSummary  = findViewById(R.id.tvUserSummary);
        tvUserPhysical = findViewById(R.id.tvUserPhysical);
        layoutEditName = findViewById(R.id.layoutEditName);
        tvBmiValue     = findViewById(R.id.tvBmiValue);
        tvBmiCategory  = findViewById(R.id.tvBmiCategory);
        tvWeightValue  = findViewById(R.id.tvWeightValue);
        tvHeightValue  = findViewById(R.id.tvHeightValue);
        tvAgeValue     = findViewById(R.id.tvAgeValue);
        tvTdeeValue    = findViewById(R.id.tvTdeeValue);
        btnUpdateData  = findViewById(R.id.btnUpdateData);
        btnReset       = findViewById(R.id.btnReset);
    }

    /**
     * FIX: Dulu paddingTop header di-hardcode 52dp di XML.
     * Sekarang dihandle secara programatik agar selalu pas di setiap device,
     * termasuk device dengan status bar tinggi (punch-hole camera, dsb).
     */
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.layoutHeader), (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                    // Terapkan padding top dinamis ke LinearLayout konten header
                    if (layoutHeader != null) {
                        layoutHeader.setPadding(
                                layoutHeader.getPaddingLeft(),
                                insets.top + dpToPx(16), // status bar + breathing room 16dp
                                layoutHeader.getPaddingRight(),
                                layoutHeader.getPaddingBottom()
                        );
                    }
                    return WindowInsetsCompat.CONSUMED;
                });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupStaticLabels() {
        if (findViewById(R.id.tv_profil_title) != null)
            ((TextView) findViewById(R.id.tv_profil_title)).setText(R.string.profil_title);

        if (findViewById(R.id.tv_bmi_section_label) != null)
            ((TextView) findViewById(R.id.tv_bmi_section_label)).setText(R.string.profil_bmi_section);

        if (findViewById(R.id.tv_weight_label) != null)
            ((TextView) findViewById(R.id.tv_weight_label)).setText(R.string.profil_label_weight);

        if (findViewById(R.id.tv_height_label) != null)
            ((TextView) findViewById(R.id.tv_height_label)).setText(R.string.profil_label_height);

        if (findViewById(R.id.tv_age_label) != null)
            ((TextView) findViewById(R.id.tv_age_label)).setText(R.string.profil_label_age);

        if (findViewById(R.id.tv_target_label) != null)
            ((TextView) findViewById(R.id.tv_target_label)).setText(R.string.profil_label_target);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        frameAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        layoutEditName.setOnClickListener(v -> showEditNameDialog());

        btnUpdateData.setOnClickListener(v -> {
            PrefManager pref = new PrefManager(ProfilActivity.this);
            Intent i = new Intent(ProfilActivity.this, GenderSelectionActivity.class);
            i.putExtra("COUNTRY_CODE", getSafeCountryCode(pref));
            i.putExtra("DETECTED_CITY", pref.getDetectedCity());
            startActivity(i);
        });

        btnReset.setOnClickListener(v ->
                new AlertDialog.Builder(ProfilActivity.this)
                        .setTitle(getString(R.string.profil_dialog_reset_title))
                        .setMessage(getString(R.string.profil_dialog_reset_msg))
                        .setPositiveButton(R.string.profil_dialog_reset_positive,
                                (d, w) -> vm.resetAllData())
                        .setNegativeButton(R.string.profil_dialog_reset_negative, null)
                        .show());
    }

    // ─── Observe ─────────────────────────────────────────────────────────────

    private void observeViewModel() {
        vm.uiState.observe(this, this::renderProfile);

        vm.resetDone.observe(this, done -> {
            if (Boolean.TRUE.equals(done)) {
                String safeCountryCode = Locale.getDefault().getCountry();
                if (safeCountryCode == null || safeCountryCode.isEmpty()) {
                    safeCountryCode = "US";
                }
                Intent i = new Intent(ProfilActivity.this, GenderSelectionActivity.class);
                i.putExtra("IS_FROM_ONBOARDING", true);
                i.putExtra("COUNTRY_CODE", safeCountryCode);
                i.putExtra("DETECTED_CITY", "");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
    }

    // ─── Render ──────────────────────────────────────────────────────────────

    @SuppressLint("SetTextI18n")
    private void renderProfile(ProfilViewModel.ProfilUiState s) {

        // Foto profil
        if (s.profilePhotoPath != null && !s.profilePhotoPath.isEmpty()) {
            File photoFile = new File(s.profilePhotoPath);
            if (photoFile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(s.profilePhotoPath);
                ivProfilePhoto.setImageBitmap(bmp);
            } else {
                setDefaultAvatar(s.isMale);
            }
        } else {
            setDefaultAvatar(s.isMale);
        }

        // Nama
        String displayName = (s.name != null && !s.name.isEmpty())
                ? s.name
                : getString(R.string.profil_default_name);
        tvUserName.setText(displayName);

        // Gender & usia
        String genderStr = getString(s.isMale
                ? R.string.profil_gender_male
                : R.string.profil_gender_female);
        String ageUnit = getString(R.string.profil_unit_age);
        String ageStr  = s.age > 0 ? s.age + ageUnit : "--";
        tvUserSummary.setText(genderStr + " · " + ageStr);

        // Berat & tinggi
        tvUserPhysical.setText(vm.fmtFloat(s.weight) + " kg  |  "
                + vm.fmtFloat(s.height) + " cm");

        // BMI
        if (s.bmi > 0) {
            tvBmiValue.setText(String.format(java.util.Locale.US, "%.1f", s.bmi));
            tvBmiCategory.setText(getBmiCategoryString(s.bmi));
            ProfilViewModel.BmiBadge badge = vm.getBmiBadge(s.bmi);
            tvBmiCategory.setBackgroundTintList(
                    ColorStateList.valueOf(badge.bgColor));
            tvBmiCategory.setTextColor(badge.fgColor);
        } else {
            tvBmiValue.setText("--");
            tvBmiCategory.setText(R.string.profil_bmi_not_measured);
            tvBmiCategory.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#EAF2EF")));
            tvBmiCategory.setTextColor(Color.parseColor("#4A7A6D"));
        }

        // Stats
        String calUnit = getString(R.string.profil_unit_calorie);
        tvWeightValue.setText(vm.fmtFloat(s.weight) + " kg");
        tvHeightValue.setText(vm.fmtFloat(s.height) + " cm");
        tvAgeValue.setText(s.age > 0 ? s.age + ageUnit : "--");
        tvTdeeValue.setText(s.tdee > 0 ? s.tdee + calUnit : "--");
    }

    // ─── Photo ───────────────────────────────────────────────────────────────

    private void handlePhotoSelected(Uri uri) {
        try {
            InputStream is       = getContentResolver().openInputStream(uri);
            File dest            = new File(getFilesDir(), "profile_photo.jpg");
            FileOutputStream fos = new FileOutputStream(dest);

            Bitmap raw     = BitmapFactory.decodeStream(is);
            Bitmap resized = resizeBitmap(raw, 512);

            if (resized != null) {
                resized.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            }

            fos.close();
            if (is != null) is.close();

            vm.saveProfilePhotoPath(dest.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap resizeBitmap(Bitmap src, int maxSize) {
        if (src == null) return null;
        int w = src.getWidth(), h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;
        float ratio = (float) maxSize / Math.max(w, h);
        return Bitmap.createScaledBitmap(src,
                Math.round(w * ratio), Math.round(h * ratio), true);
    }

    private void setDefaultAvatar(boolean isMale) {
        ivProfilePhoto.setImageResource(isMale
                ? R.drawable.img_male_character
                : R.drawable.img_female_character);
    }

    // ─── Name Dialog ─────────────────────────────────────────────────────────

    private void showEditNameDialog() {
        EditText input = new EditText(ProfilActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(40) });
        input.setHint(getString(R.string.profil_hint_name));

        if (tvUserName.getText() != null &&
                !tvUserName.getText().toString().equals(getString(R.string.profil_default_name))) {
            input.setText(tvUserName.getText());
            input.selectAll();
        }

        int dp16 = Math.round(16 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(ProfilActivity.this);
        container.setPadding(dp16 * 2, dp16, dp16 * 2, dp16 / 2);
        container.addView(input);

        new AlertDialog.Builder(ProfilActivity.this)
                .setTitle(R.string.profil_dialog_name_title)
                .setView(container)
                .setPositiveButton(R.string.profil_dialog_name_save, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) vm.saveName(name);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private String getBmiCategoryString(float bmi) {
        if (bmi < 18.5f) return getString(R.string.profil_bmi_underweight);
        if (bmi < 25f)   return getString(R.string.profil_bmi_normal);
        if (bmi < 30f)   return getString(R.string.profil_bmi_overweight);
        return                   getString(R.string.profil_bmi_obese);
    }

    private String getSafeCountryCode(PrefManager pref) {
        String saved = pref.getCountryCode();
        if (saved != null && !saved.isEmpty()) return saved;
        String deviceCountry = Locale.getDefault().getCountry();
        return (deviceCountry != null && !deviceCountry.isEmpty()) ? deviceCountry : "US";
    }
}