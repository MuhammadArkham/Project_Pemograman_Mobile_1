package com.fitme.app.ui.makan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.fitme.app.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodAnalysisActivity extends AppCompatActivity {

    private static final int    REQ_CAMERA     = 100;
    private static final int    REQ_GALLERY    = 101;
    private static final int    REQ_PERMISSION = 200;
    private static final String AUTHORITY      = "com.fitme.app.fileprovider";

    private FoodAnalysisViewModel viewModel;
    private Uri    cameraImageUri;
    private String savedImagePath;
    private String countryCode = "ID";

    private View         layoutHeader, layoutLoading, layoutResult;
    private ImageView    ivPreview;
    private LinearLayout tvPlaceholder;
    private TextView     tvChangePhoto, tvFoodName, tvPortion, tvCalories;
    private TextView     tvProtein, tvCarbs, tvFat, tvLoadingText, tvWeightUnit, tvDataSource;
    private MaterialButton btnAnalyze, btnSave;
    private ImageButton  btnCancel;
    private EditText     etWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_analysis);

        viewModel = new ViewModelProvider(this).get(FoodAnalysisViewModel.class);

        if (getIntent() != null && getIntent().hasExtra("COUNTRY_CODE"))
            countryCode = getIntent().getStringExtra("COUNTRY_CODE");

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        bindViews();
        setupInsets();
        setupButtons();
        observeViewModel();

        ivPreview.postDelayed(this::showImageSourceDialog, 100);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            btnAnalyze.setEnabled(!isLoading);
            btnAnalyze.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            layoutLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // FIX 1: Mengubah getLoadingMessageRes() menjadi getLoadingMessage()
        // dan menyesuaikan parameter menjadi String 'message'
        viewModel.getLoadingMessage().observe(this, message -> {
            if (message != null && tvLoadingText != null) {
                tvLoadingText.setText(message);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.getAnalysisResult().observe(this, result -> {
            if (result == null) return;

            tvFoodName.setText("🍽️  " + result.name);
            tvPortion.setText(getString(R.string.profil_bmi_not_measured).equals("Not measured") ? "Estimated (100g)" : "Estimasi (100g)");

            etWeight.setText("");
            etWeight.setText("100");

            tvDataSource.setVisibility(View.VISIBLE);
            if (result.verified) {
                tvDataSource.setText(getString(R.string.msg_data_verified));
                tvDataSource.setTextColor(Color.parseColor("#1D9E75"));
            } else {
                tvDataSource.setText(getString(R.string.msg_ai_estimate));
                tvDataSource.setTextColor(Color.parseColor("#BA7517"));
                Toast.makeText(this, getString(R.string.msg_ai_estimate_toast), Toast.LENGTH_SHORT).show();
            }

            layoutResult.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            btnAnalyze.setVisibility(View.GONE);
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void startAnalysis() {
        if (savedImagePath == null) {
            Toast.makeText(this, getString(R.string.err_select_photo), Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bmp = BitmapFactory.decodeFile(savedImagePath);
        if (bmp == null) return;

        viewModel.startHybridAnalysis(bmp, countryCode);
    }

    private void saveAndReturn() {
        FoodAnalysisViewModel.AnalysisResult res = viewModel.getAnalysisResult().getValue();
        if (res == null || res.name.isEmpty()) return;

        viewModel.saveToDatabase(
                res.name, parseTV(tvCalories, 0), parseTV(tvProtein, 0),
                parseTV(tvCarbs, 0), parseTV(tvFat, 0),
                etWeight.getText().toString().trim(), savedImagePath
        );
    }

    private void recalculateFromWeight(String weightStr) {
        FoodAnalysisViewModel.AnalysisResult res = viewModel.getAnalysisResult().getValue();
        if (res == null) return;

        try {
            int w = Integer.parseInt(weightStr.trim());
            if (w <= 0 || w > 5000) return;
            tvCalories.setText(String.valueOf(Math.round(res.calPerGram * w)));
            tvProtein.setText(String.valueOf(Math.round(res.proPerGram * w)));
            tvCarbs.setText(String.valueOf(Math.round(res.carPerGram * w)));
            tvFat.setText(String.valueOf(Math.round(res.fatPerGram * w)));
        } catch (NumberFormatException ignored) {}
    }

    private void bindViews() {
        layoutHeader  = findViewById(R.id.layoutHeader);
        ivPreview     = findViewById(R.id.iv_preview);
        tvPlaceholder = findViewById(R.id.tv_placeholder);
        tvChangePhoto = findViewById(R.id.tv_change_photo);
        btnAnalyze    = findViewById(R.id.btn_analyze);
        btnSave       = findViewById(R.id.btn_save);
        btnCancel     = findViewById(R.id.btn_cancel);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutResult  = findViewById(R.id.layout_result);
        tvFoodName    = findViewById(R.id.tv_result_name);
        tvPortion     = findViewById(R.id.tv_result_portion);
        tvCalories    = findViewById(R.id.tv_result_calories);
        tvProtein     = findViewById(R.id.tv_result_protein);
        tvCarbs       = findViewById(R.id.tv_result_carbs);
        tvFat         = findViewById(R.id.tv_result_fat);
        tvLoadingText = findViewById(R.id.tv_loading_text);
        etWeight      = findViewById(R.id.et_weight_gram);
        tvWeightUnit  = findViewById(R.id.tv_weight_unit);
        tvDataSource  = findViewById(R.id.tv_data_source);
    }

    private void setupInsets() {
        if (layoutHeader != null) {
            layoutHeader.setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(v.getPaddingLeft(), insets.getSystemWindowInsetTop() + 16, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    private void setupButtons() {
        btnAnalyze.setOnClickListener(v -> startAnalysis());
        btnSave.setOnClickListener(v -> saveAndReturn());
        btnCancel.setOnClickListener(v -> finish());
        ivPreview.setOnClickListener(v -> showImageSourceDialog());
        tvPlaceholder.setOnClickListener(v -> showImageSourceDialog());
        tvChangePhoto.setOnClickListener(v -> showImageSourceDialog());

        etWeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { recalculateFromWeight(s.toString()); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    // FIX 2: Mengubah referensi R.id ke R.string untuk memanggil text dari strings.xml
    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_select_source))
                .setItems(new String[]{getString(R.string.source_camera), getString(R.string.source_gallery)},
                        (dialog, which) -> { if (which == 0) openCamera(); else openGallery(); })
                .setNegativeButton(android.R.string.cancel, (d, w) -> { if (savedImagePath == null) finish(); })
                .setOnCancelListener(d -> { if (savedImagePath == null) finish(); }).show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_PERMISSION);
            return;
        }
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this, AUTHORITY, photoFile);
            Intent intent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, REQ_CAMERA);
        } catch (IOException e) { Toast.makeText(this, getString(R.string.err_open_camera), Toast.LENGTH_SHORT).show(); }
    }

    private void openGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQ_GALLERY);
    }

    private File createImageFile() throws IOException {
        String stamp   = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File   dir     = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File   imgFile = File.createTempFile("FITME_" + stamp, ".jpg", dir);
        savedImagePath = imgFile.getAbsolutePath();
        return imgFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) { if (savedImagePath == null) finish(); return; }

        if (requestCode == REQ_CAMERA) {
            ivPreview.setImageURI(cameraImageUri);
        } else if (requestCode == REQ_GALLERY && data != null) {
            Uri uri = data.getData();
            ivPreview.setImageURI(uri);
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bmp     = BitmapFactory.decodeStream(is);
                if (is != null) is.close();
                File copy = createImageFile();
                java.io.FileOutputStream fos = new java.io.FileOutputStream(copy);
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.close();
            } catch (IOException ignored) {}
        }
        tvPlaceholder.setVisibility(View.GONE);
        ivPreview.setVisibility(View.VISIBLE);
        tvChangePhoto.setVisibility(View.VISIBLE);
        btnAnalyze.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
    }

    private int parseTV(TextView tv, int fallback) {
        try { return Integer.parseInt(tv.getText().toString().trim()); } catch (Exception e) { return fallback; }
    }
}