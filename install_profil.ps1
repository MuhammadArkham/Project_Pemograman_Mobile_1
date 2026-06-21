# FITME Profil - Direct Installer
# Jalankan: .\install_profil.ps1

param([string]$PackageName = "com.fitme.app")

$pkg = $PackageName -replace "\.", "/"
$j   = "app/src/main/java/$pkg"
$r   = "app/src/main/res"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FITME Profil - Installing..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Buat direktori
@("$j/ui/profil","$r/layout","$r/drawable") | ForEach-Object {
    New-Item -ItemType Directory -Force -Path $_ | Out-Null
    Write-Host "  [DIR] $_" -ForegroundColor DarkGray
}

# ── ProfilActivity.java ──────────────────────────────────────
$java = @"
package $PackageName.ui.profil;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import $PackageName.R;
import $PackageName.ui.bmi.GenderSelectionActivity;
import $PackageName.utils.PrefManager;

public class ProfilActivity extends AppCompatActivity {
    private PrefManager pref;
    private TextView tvBmiValue, tvBmiCategory, tvBmiDate;
    private View viewBmiIndicator;
    private LinearLayout btnUpdateData, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        pref = new PrefManager(this);
        bindViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateData();
    }

    private void bindViews() {
        tvBmiValue       = findViewById(R.id.tvBmiValue);
        tvBmiCategory    = findViewById(R.id.tvBmiCategory);
        tvBmiDate        = findViewById(R.id.tvBmiDate);
        viewBmiIndicator = findViewById(R.id.viewBmiIndicator);
        btnUpdateData    = findViewById(R.id.btnUpdateData);
        btnReset         = findViewById(R.id.btnReset);
        bindStatCard(R.id.statWeight, "BERAT",  formatNum(pref.getWeight()) + " kg");
        bindStatCard(R.id.statHeight, "TINGGI", formatNum(pref.getHeight()) + " cm");
        bindStatCard(R.id.statAge,    "USIA",   pref.getUserAge() + " thn");
        bindStatCard(R.id.statTdee,   "TARGET", pref.getDailyCalorieTarget() + " kkal");
        bindInfoRow(R.id.rowGender,   "Jenis Kelamin",   pref.isMale() ? "Pria" : "Wanita");
        bindInfoRow(R.id.rowAge,      "Usia",            pref.getUserAge() + " tahun");
        bindInfoRow(R.id.rowWeight,   "Berat Badan",     formatNum(pref.getWeight()) + " kg");
        bindInfoRow(R.id.rowHeight,   "Tinggi Badan",    formatNum(pref.getHeight()) + " cm");
        bindInfoRow(R.id.rowBmi,      "Indeks Massa Tubuh", String.format(java.util.Locale.US,"%.1f", pref.getLastBmi()));
        bindInfoRow(R.id.rowCategory, "Kategori BMI",   pref.getBmiCategory());
        bindInfoRow(R.id.rowTdee,     "Target Kalori",  pref.getDailyCalorieTarget() + " kkal/hari");
    }

    @SuppressLint("SetTextI18n")
    private void populateData() {
        float bmi = pref.getLastBmi();
        if (bmi > 0) {
            tvBmiValue.setText(String.format(java.util.Locale.US, "%.1f", bmi));
            tvBmiCategory.setText(pref.getBmiCategory());
            tvBmiCategory.setTextColor(bmiColor(bmi));
            tvBmiDate.setText("Terakhir diukur");
            positionBmiIndicator(bmi);
        } else {
            tvBmiValue.setText("--");
            tvBmiCategory.setText("Belum diukur");
            tvBmiDate.setText("Tap Perbarui Data untuk mulai");
        }
    }

    private void setupListeners() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnUpdateData != null) btnUpdateData.setOnClickListener(v -> {
            Intent i = new Intent(this, GenderSelectionActivity.class);
            i.putExtra("COUNTRY_CODE", pref.getCountryCode());
            startActivity(i);
        });
        if (btnReset != null) btnReset.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Reset Semua Data?")
                .setMessage("Semua data profil dan riwayat akan dihapus.")
                .setPositiveButton("Reset", (d, w) -> {
                    pref.clearAll();
                    Intent i = new Intent(this, GenderSelectionActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i); finish();
                })
                .setNegativeButton("Batal", null).show()
        );
    }

    private void bindStatCard(int viewId, String label, String value) {
        View card = findViewById(viewId);
        if (card == null) return;
        TextView lbl = card.findViewById(R.id.tvStatLabel);
        TextView val = card.findViewById(R.id.tvStatValue);
        if (lbl != null) lbl.setText(label);
        if (val != null) val.setText(value);
    }

    private void bindInfoRow(int viewId, String label, String value) {
        View row = findViewById(viewId);
        if (row == null) return;
        TextView lbl = row.findViewById(R.id.tvRowLabel);
        TextView val = row.findViewById(R.id.tvRowValue);
        if (lbl != null) lbl.setText(label);
        if (val != null) val.setText(value != null && !value.isEmpty() ? value : "--");
    }

    private void positionBmiIndicator(float bmi) {
        if (viewBmiIndicator == null) return;
        viewBmiIndicator.post(() -> {
            View bar = (View) viewBmiIndicator.getParent();
            if (bar == null) return;
            float f = bmiToFraction(bmi);
            viewBmiIndicator.setTranslationX(f * bar.getWidth() - viewBmiIndicator.getWidth() / 2f);
        });
    }

    private float bmiToFraction(float bmi) {
        if (bmi <= 10f)  return 0.02f;
        if (bmi < 18.5f) return 0.02f + ((bmi - 10f) / 8.5f) * 0.23f;
        if (bmi < 25f)   return 0.25f + ((bmi - 18.5f) / 6.5f) * 0.25f;
        if (bmi < 30f)   return 0.50f + ((bmi - 25f) / 5f) * 0.25f;
        return Math.min(0.97f, 0.75f + ((bmi - 30f) / 10f) * 0.22f);
    }

    private int bmiColor(float bmi) {
        if (bmi < 18.5f) return Color.parseColor("#378ADD");
        if (bmi < 25f)   return Color.parseColor("#1D9E75");
        if (bmi < 30f)   return Color.parseColor("#EF9F27");
        return Color.parseColor("#E24B4A");
    }

    private String formatNum(float v) {
        return v <= 0 ? "--" : v % 1 == 0 ? String.valueOf((int)v) : String.valueOf(v);
    }
}
"@
Set-Content -Path "$j/ui/profil/ProfilActivity.java" -Value $java -Encoding UTF8
Write-Host "  [OK] ProfilActivity.java" -ForegroundColor Green

# ── activity_profil.xml ──────────────────────────────────────
$xmlActivity = @'
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#EBF5F7">

    <LinearLayout
        android:id="@+id/headerLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_splash_gradient"
        android:orientation="vertical"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingTop="44dp"
        android:paddingBottom="36dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="20dp">
            <ImageView
                android:id="@+id/btnBack"
                android:layout_width="36dp"
                android:layout_height="36dp"
                android:src="@android:drawable/ic_menu_close_clear_cancel"
                android:tint="#AAFFFFFF"
                android:background="@drawable/bg_circle_translucent"
                android:padding="8dp"
                android:contentDescription="Kembali"/>
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Profil Saya"
                android:textColor="#FFFFFF"
                android:textSize="18sp"
                android:textStyle="bold"
                android:gravity="center"/>
            <View android:layout_width="36dp" android:layout_height="36dp"/>
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:gravity="center"
            android:layout_marginBottom="18dp">
            <FrameLayout
                android:layout_width="72dp"
                android:layout_height="72dp"
                android:layout_marginBottom="8dp">
                <View
                    android:layout_width="72dp"
                    android:layout_height="72dp"
                    android:background="@drawable/bg_avatar_circle"/>
                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:text="F"
                    android:textColor="#FFFFFF"
                    android:textSize="26sp"
                    android:textStyle="bold"
                    android:gravity="center"/>
            </FrameLayout>
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Pengguna FITME"
                android:textColor="#FFFFFF"
                android:textSize="15sp"
                android:textStyle="bold"/>
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/bg_card_translucent"
            android:orientation="vertical"
            android:padding="14dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="10dp">
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="BMI SAAT INI"
                        android:textColor="#80FFFFFF"
                        android:textSize="9sp"
                        android:letterSpacing="0.1"
                        android:textStyle="bold"
                        android:layout_marginBottom="2dp"/>
                    <LinearLayout
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:orientation="horizontal"
                        android:gravity="bottom">
                        <TextView
                            android:id="@+id/tvBmiValue"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="--"
                            android:textColor="#FFFFFF"
                            android:textSize="36sp"
                            android:textStyle="bold"/>
                        <TextView
                            android:id="@+id/tvBmiCategory"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text=""
                            android:textSize="12sp"
                            android:textStyle="bold"
                            android:paddingBottom="8dp"
                            android:paddingStart="8dp"/>
                    </LinearLayout>
                    <TextView
                        android:id="@+id/tvBmiDate"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Belum diukur"
                        android:textColor="#80FFFFFF"
                        android:textSize="10sp"/>
                </LinearLayout>
                <View
                    android:layout_width="52dp"
                    android:layout_height="52dp"
                    android:background="@drawable/bg_emoji_circle"/>
            </LinearLayout>

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="14dp"
                android:layout_marginBottom="4dp">
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="8dp"
                    android:layout_gravity="center_vertical"
                    android:orientation="horizontal">
                    <View android:layout_width="0dp" android:layout_height="match_parent"
                        android:layout_weight="1.85" android:background="#378ADD"/>
                    <View android:layout_width="0dp" android:layout_height="match_parent"
                        android:layout_weight="0.65" android:background="#1D9E75"/>
                    <View android:layout_width="0dp" android:layout_height="match_parent"
                        android:layout_weight="0.50" android:background="#EF9F27"/>
                    <View android:layout_width="0dp" android:layout_height="match_parent"
                        android:layout_weight="1.00" android:background="#E24B4A"/>
                </LinearLayout>
                <View
                    android:id="@+id/viewBmiIndicator"
                    android:layout_width="14dp"
                    android:layout_height="14dp"
                    android:background="@drawable/bg_bmi_indicator"
                    android:layout_gravity="center_vertical|start"/>
            </FrameLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">
                <TextView android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_weight="1.85" android:text="Kurus"
                    android:textColor="#80FFFFFF" android:textSize="8sp"/>
                <TextView android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_weight="0.65" android:text="Normal"
                    android:textColor="#80FFFFFF" android:textSize="8sp"/>
                <TextView android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_weight="0.50" android:text="Gemuk"
                    android:textColor="#80FFFFFF" android:textSize="8sp"/>
                <TextView android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_weight="1.00" android:text="Obesitas"
                    android:textColor="#80FFFFFF" android:textSize="8sp"
                    android:gravity="end"/>
            </LinearLayout>
        </LinearLayout>
    </LinearLayout>

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior"
        android:scrollbars="none"
        android:overScrollMode="never">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:paddingTop="16dp"
            android:paddingBottom="40dp">

            <GridLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:columnCount="2"
                android:layout_marginBottom="16dp">
                <include layout="@layout/item_stat_card" android:id="@+id/statWeight"
                    android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_columnWeight="1" android:layout_marginEnd="6dp" android:layout_marginBottom="6dp"/>
                <include layout="@layout/item_stat_card" android:id="@+id/statHeight"
                    android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_columnWeight="1" android:layout_marginStart="6dp" android:layout_marginBottom="6dp"/>
                <include layout="@layout/item_stat_card" android:id="@+id/statAge"
                    android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_columnWeight="1" android:layout_marginEnd="6dp"/>
                <include layout="@layout/item_stat_card" android:id="@+id/statTdee"
                    android:layout_width="0dp" android:layout_height="wrap_content"
                    android:layout_columnWeight="1" android:layout_marginStart="6dp"/>
            </GridLayout>

            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="DATA FISIK" android:textColor="#888888" android:textSize="10sp"
                android:letterSpacing="0.1" android:textStyle="bold" android:layout_marginBottom="8dp"/>

            <androidx.cardview.widget.CardView
                android:layout_width="match_parent" android:layout_height="wrap_content"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="16dp" app:cardElevation="0dp" app:cardBackgroundColor="#FFFFFF">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
                    android:orientation="vertical" android:paddingStart="16dp" android:paddingEnd="16dp"
                    android:paddingTop="4dp" android:paddingBottom="4dp">
                    <include layout="@layout/item_info_row" android:id="@+id/rowGender"/>
                    <include layout="@layout/item_info_row" android:id="@+id/rowAge"/>
                    <include layout="@layout/item_info_row" android:id="@+id/rowWeight"/>
                    <include layout="@layout/item_info_row" android:id="@+id/rowHeight"/>
                </LinearLayout>
            </androidx.cardview.widget.CardView>

            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                android:text="HASIL KESEHATAN" android:textColor="#888888" android:textSize="10sp"
                android:letterSpacing="0.1" android:textStyle="bold" android:layout_marginBottom="8dp"/>

            <androidx.cardview.widget.CardView
                android:layout_width="match_parent" android:layout_height="wrap_content"
                android:layout_marginBottom="20dp"
                app:cardCornerRadius="16dp" app:cardElevation="0dp" app:cardBackgroundColor="#FFFFFF">
                <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
                    android:orientation="vertical" android:paddingStart="16dp" android:paddingEnd="16dp"
                    android:paddingTop="4dp" android:paddingBottom="4dp">
                    <include layout="@layout/item_info_row" android:id="@+id/rowBmi"/>
                    <include layout="@layout/item_info_row" android:id="@+id/rowCategory"/>
                    <include layout="@layout/item_info_row" android:id="@+id/rowTdee"/>
                </LinearLayout>
            </androidx.cardview.widget.CardView>

            <LinearLayout android:id="@+id/btnUpdateData"
                android:layout_width="match_parent" android:layout_height="52dp"
                android:background="@drawable/bg_button_primary_green"
                android:gravity="center" android:layout_marginBottom="10dp"
                android:clickable="true" android:focusable="true">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                    android:text="Perbarui Data" android:textColor="#FFFFFF"
                    android:textSize="15sp" android:textStyle="bold"/>
            </LinearLayout>

            <LinearLayout android:id="@+id/btnReset"
                android:layout_width="match_parent" android:layout_height="50dp"
                android:background="@drawable/bg_button_outline_red"
                android:gravity="center" android:clickable="true" android:focusable="true">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                    android:text="Reset Semua Data" android:textColor="#E24B4A"
                    android:textSize="14sp" android:textStyle="bold"/>
            </LinearLayout>
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.coordinatorlayout.widget.CoordinatorLayout>
'@
Set-Content -Path "$r/layout/activity_profil.xml" -Value $xmlActivity -Encoding UTF8
Write-Host "  [OK] activity_profil.xml" -ForegroundColor Green

# ── item_stat_card.xml ──────────────────────────────────────
$statCard = @'
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="14dp"
    app:cardElevation="0dp"
    app:cardBackgroundColor="#FFFFFF">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="14dp">
        <TextView android:id="@+id/tvStatLabel"
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="LABEL" android:textColor="#AAAAAA" android:textSize="9sp"
            android:letterSpacing="0.08" android:textStyle="bold" android:layout_marginBottom="4dp"/>
        <TextView android:id="@+id/tvStatValue"
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="--" android:textColor="#1A3028" android:textSize="22sp"
            android:textStyle="bold"/>
    </LinearLayout>
</androidx.cardview.widget.CardView>
'@
Set-Content -Path "$r/layout/item_stat_card.xml" -Value $statCard -Encoding UTF8
Write-Host "  [OK] item_stat_card.xml" -ForegroundColor Green

# ── item_info_row.xml ──────────────────────────────────────
$infoRow = @'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="52dp"
    android:orientation="horizontal"
    android:gravity="center_vertical">
    <TextView android:id="@+id/tvRowLabel"
        android:layout_width="0dp" android:layout_height="wrap_content"
        android:layout_weight="1" android:text="Label"
        android:textColor="#888888" android:textSize="13sp"/>
    <TextView android:id="@+id/tvRowValue"
        android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:text="--" android:textColor="#1A3028"
        android:textSize="14sp" android:textStyle="bold"/>
</LinearLayout>
'@
Set-Content -Path "$r/layout/item_info_row.xml" -Value $infoRow -Encoding UTF8
Write-Host "  [OK] item_info_row.xml" -ForegroundColor Green

# ── Drawables ──────────────────────────────────────────────
$drawables = @{
    "bg_avatar_circle"       = '<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval"><solid android:color="#1D9E75"/><stroke android:width="2dp" android:color="#9FE1CB"/></shape>'
    "bg_circle_translucent"  = '<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval"><solid android:color="#22FFFFFF"/></shape>'
    "bg_emoji_circle"        = '<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval"><solid android:color="#22FFFFFF"/></shape>'
    "bg_bmi_indicator"       = '<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval"><solid android:color="#FFFFFF"/><stroke android:width="2dp" android:color="#013F4A"/></shape>'
    "bg_button_primary_green"= '<shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="#0F6E56"/><corners android:radius="14dp"/></shape>'
    "bg_button_outline_red"  = '<shape xmlns:android="http://schemas.android.com/apk/res/android"><solid android:color="#00000000"/><corners android:radius="14dp"/><stroke android:width="1.5dp" android:color="#E24B4A"/></shape>'
}

foreach ($name in $drawables.Keys) {
    $xml = "<?xml version=`"1.0`" encoding=`"utf-8`"?>`n" + $drawables[$name]
    Set-Content -Path "$r/drawable/$name.xml" -Value $xml -Encoding UTF8
    Write-Host "  [OK] $name.xml" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  SELESAI! 10 file berhasil dibuat" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  LANGKAH SELANJUTNYA:" -ForegroundColor Cyan
Write-Host "  1. Tambah ke AndroidManifest.xml:" -ForegroundColor White
Write-Host "     <activity android:name=`".ui.profil.ProfilActivity`"/>" -ForegroundColor Yellow
Write-Host "  2. Sync Gradle di Android Studio" -ForegroundColor White
Write-Host "  3. Build & Run" -ForegroundColor White
Write-Host ""
