package com.fitme.app.ui.bmi;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.fitme.app.R;
import com.fitme.app.ui.base.BaseActivity; // ✅ Memanggil BaseActivity
import com.fitme.app.utils.PrefManager;
import com.fitme.app.utils.ProgressBarHelper; // ✅ Memanggil ProgressBarHelper
import com.google.android.material.button.MaterialButton;

public class InputDataActivity extends BaseActivity { // ✅ Extends BaseActivity

    private TextView tvUsiaValue, tvBeratValue, tvTinggiValue, tvBtnSubtext,
            tvStepLabel, tvTitle, tvDescription;
    private LinearLayout rowUsia, rowBerat, rowTinggi;
    private MaterialButton btnCalculate;
    private View barProgress, scrollContent, layoutBottom;

    private int usia = 0;
    private float berat = 0f, tinggi = 0f;
    private boolean isMale = false;
    private String countryCode = "ID", city = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);

        if (getIntent() != null) {
            isMale = getIntent().getBooleanExtra("USER_GENDER", false);
            countryCode = getIntent().getStringExtra("COUNTRY_CODE") != null
                    ? getIntent().getStringExtra("COUNTRY_CODE") : "ID";
            city = getIntent().getStringExtra("DETECTED_CITY") != null
                    ? getIntent().getStringExtra("DETECTED_CITY") : "";
        }

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        initViews();
        // ✅ Tidak perlu setLanguage(), UI otomatis mengikuti string resource dari XML
        preFillFromPrefManager();

        if (barProgress != null) {
            // ✅ Menggunakan ProgressBarHelper
            ProgressBarHelper.updateBarWeight(barProgress, 33f);
            ProgressBarHelper.animateProgressBar(barProgress, 33f, 67f, 380, 420);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });

        rowUsia.setOnClickListener(v -> showDialog("usia"));
        rowBerat.setOnClickListener(v -> showDialog("berat"));
        rowTinggi.setOnClickListener(v -> showDialog("tinggi"));
        findViewById(R.id.btnBack).setOnClickListener(v -> handleBack());

        btnCalculate.setOnClickListener(v -> {
            float hm = tinggi / 100f;
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("BMI_RESULT", berat / (hm * hm));
            intent.putExtra("USER_GENDER", isMale);
            intent.putExtra("USER_AGE", usia);
            intent.putExtra("USER_WEIGHT", berat);
            intent.putExtra("USER_HEIGHT", tinggi);
            intent.putExtra("COUNTRY_CODE", countryCode);
            intent.putExtra("DETECTED_CITY", city);
            startActivity(intent);
        });
    }

    private void handleBack() {
        finish();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvStepLabel = findViewById(R.id.tvStepLabel);
        tvDescription = findViewById(R.id.tvDescription);
        tvUsiaValue = findViewById(R.id.tvUsiaValue);
        tvBeratValue = findViewById(R.id.tvBeratValue);
        tvTinggiValue = findViewById(R.id.tvTinggiValue);
        rowUsia = findViewById(R.id.rowUsia);
        rowBerat = findViewById(R.id.rowBerat);
        rowTinggi = findViewById(R.id.rowTinggi);
        btnCalculate = findViewById(R.id.btnCalculate);
        tvBtnSubtext = findViewById(R.id.tvBtnSubtext);
        barProgress = findViewById(R.id.barProgress);
        scrollContent = findViewById(R.id.scrollContent);
        layoutBottom = findViewById(R.id.layoutBottom);
    }

    @SuppressLint("SetTextI18n")
    private void preFillFromPrefManager() {
        PrefManager pref = new PrefManager(this);
        if (pref.getUserAge() > 0)
            saveValue("usia", String.valueOf(pref.getUserAge()), getString(R.string.unit_age));
        if (pref.getWeight() > 0)
            saveValue("berat", String.valueOf(pref.getWeight()), getString(R.string.unit_weight));
        if (pref.getHeight() > 0)
            saveValue("tinggi", String.valueOf(pref.getHeight()), getString(R.string.unit_height));
        updateButton();
    }

    private void showDialog(final String field) {
        String title, hint, unit;
        String currentVal = "";

        if (field.equals("usia")) {
            title = getString(R.string.label_age);
            hint = getString(R.string.hint_age);
            unit = getString(R.string.unit_age);
            if (usia > 0) currentVal = String.valueOf(usia);
        } else if (field.equals("berat")) {
            title = getString(R.string.label_weight);
            hint = getString(R.string.hint_weight);
            unit = getString(R.string.unit_weight);
            if (berat > 0)
                currentVal = berat % 1 == 0 ? String.valueOf((int) berat) : String.valueOf(berat);
        } else {
            title = getString(R.string.label_height);
            hint = getString(R.string.hint_height);
            unit = getString(R.string.unit_height);
            if (tinggi > 0)
                currentVal = tinggi % 1 == 0 ? String.valueOf((int) tinggi) : String.valueOf(tinggi);
        }

        final String finalUnit = unit;

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView tvDTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvDUnit = dialog.findViewById(R.id.tvDialogUnit);
        EditText etInput = dialog.findViewById(R.id.etDialogInput);
        TextView btnSave = dialog.findViewById(R.id.btnDialogSave);
        TextView btnCancel = dialog.findViewById(R.id.btnDialogCancel);

        tvDTitle.setText(title);
        tvDUnit.setText(finalUnit);
        etInput.setHint(hint);
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (!currentVal.isEmpty()) {
            etInput.setText(currentVal);
            etInput.setSelection(currentVal.length());
        }
        etInput.requestFocus();

        btnSave.setText(getString(R.string.dialog_save));
        btnCancel.setText(getString(R.string.dialog_cancel));
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String val = etInput.getText() != null ? etInput.getText().toString().trim() : "";
            if (!val.isEmpty()) {
                saveValue(field, val, finalUnit);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void saveValue(String field, String val, String unit) {
        try {
            if (field.equals("usia")) {
                usia = Integer.parseInt(val);
                tvUsiaValue.setText(usia + " " + unit);
                setValueFilled(tvUsiaValue);
            } else if (field.equals("berat")) {
                berat = Float.parseFloat(val);
                tvBeratValue.setText((berat % 1 == 0
                        ? String.valueOf((int) berat) : String.valueOf(berat)) + " " + unit);
                setValueFilled(tvBeratValue);
            } else if (field.equals("tinggi")) {
                tinggi = Float.parseFloat(val);
                tvTinggiValue.setText((tinggi % 1 == 0
                        ? String.valueOf((int) tinggi) : String.valueOf(tinggi)) + " " + unit);
                setValueFilled(tvTinggiValue);
            }
        } catch (NumberFormatException ignored) {}
        updateButton();
    }

    private void setValueFilled(TextView tv) {
        tv.setTextColor(ContextCompat.getColor(this, R.color.fitme_green));
        tv.setTextSize(16f); // ✅ Ukuran font lebih proporsional
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
    }

    private void updateButton() {
        boolean ok = usia > 0 && berat > 0 && tinggi > 0;
        btnCalculate.setEnabled(ok);
        if (ok) {
            btnCalculate.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.fitme_green)));
            btnCalculate.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnCalculate.setText(getString(R.string.btn_calculate_ready));
            if (tvBtnSubtext != null) tvBtnSubtext.setVisibility(View.GONE);
            btnCalculate.setScaleX(0.96f);
            btnCalculate.setScaleY(0.96f);
            btnCalculate.animate().scaleX(1f).scaleY(1f).setDuration(260).start();
        } else {
            btnCalculate.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.fitme_green_disabled)));
            btnCalculate.setTextColor(ContextCompat.getColor(this, R.color.white));
            if (tvBtnSubtext != null) tvBtnSubtext.setVisibility(View.VISIBLE);
        }
    }
}