# ============================================================
#  FITME — Setup Onboarding
#  Jalankan dari root folder project Android Studio:
#  PS> .\setup_fitme_onboarding.ps1
# ============================================================

param([string]$PackageName = "com.fitme.app")

$pkg = $PackageName -replace "\.", "/"
$j   = "app/src/main/java/$pkg"
$r   = "app/src/main/res"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  FITME Onboarding - Installing..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

@("$j/ui/onboarding","$r/layout","$r/drawable") | ForEach-Object {
    New-Item -ItemType Directory -Force -Path $_ | Out-Null
    Write-Host "  [DIR] $_" -ForegroundColor DarkGray
}

# ── OnboardingPage.java ──────────────────────────────────────
@"
package $PackageName.ui.onboarding;

public class OnboardingPage {
    public final int    iconRes;
    public final String title;
    public final String description;
    public final String bgColor;
    public final String accentColor;

    public OnboardingPage(int iconRes, String title, String description,
                          String bgColor, String accentColor) {
        this.iconRes     = iconRes;
        this.title       = title;
        this.description = description;
        this.bgColor     = bgColor;
        this.accentColor = accentColor;
    }
}
"@ | Set-Content -Path "$j/ui/onboarding/OnboardingPage.java" -Encoding UTF8
Write-Host "  [OK] OnboardingPage.java" -ForegroundColor Green

# ── OnboardingAdapter.java ───────────────────────────────────
@"
package $PackageName.ui.onboarding;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import $PackageName.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageHolder> {

    private final Context              context;
    private final List<OnboardingPage> pages;

    public OnboardingAdapter(Context context, List<OnboardingPage> pages) {
        this.context = context;
        this.pages   = pages;
    }

    @NonNull @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new PageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder h, int position) {
        OnboardingPage page = pages.get(position);
        h.ivIcon.setImageResource(page.iconRes);
        h.tvTitle.setText(page.title);
        h.tvDesc.setText(page.description);
        h.layoutBg.setBackgroundColor(Color.parseColor(page.bgColor));
        h.ivIcon.setColorFilter(Color.parseColor(page.accentColor));
        h.tvTitle.setTextColor(Color.parseColor(page.accentColor));
    }

    @Override public int getItemCount() { return pages.size(); }

    static class PageHolder extends RecyclerView.ViewHolder {
        View      layoutBg;
        ImageView ivIcon;
        TextView  tvTitle, tvDesc;
        PageHolder(View v) {
            super(v);
            layoutBg = v.findViewById(R.id.layoutPageBg);
            ivIcon   = v.findViewById(R.id.ivOnboardIcon);
            tvTitle  = v.findViewById(R.id.tvOnboardTitle);
            tvDesc   = v.findViewById(R.id.tvOnboardDesc);
        }
    }
}
"@ | Set-Content -Path "$j/ui/onboarding/OnboardingAdapter.java" -Encoding UTF8
Write-Host "  [OK] OnboardingAdapter.java" -ForegroundColor Green

# ── OnboardingActivity.java ──────────────────────────────────
@"
package $PackageName.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import $PackageName.R;
import $PackageName.ui.bmi.GenderSelectionActivity;
import $PackageName.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2     viewPager;
    private LinearLayout   dotsContainer;
    private TextView       btnNext, btnSkip;

    private static final int TOTAL_PAGES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

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
            R.drawable.ic_onboard_health,
            "Selamat Datang di FITME",
            "Perjalanan menuju tubuh yang lebih sehat dimulai dari satu langkah kecil — mengenal diri sendiri.",
            "#E1F5EE", "#0F6E56"
        ));
        pages.add(new OnboardingPage(
            R.drawable.ic_onboard_bmi,
            "Ukur & Pahami Tubuh Anda",
            "BMI bukan sekadar angka. Kami mengubahnya menjadi insight personal yang membantu keputusan kesehatan lebih cerdas.",
            "#E6F1FB", "#185FA5"
        ));
        pages.add(new OnboardingPage(
            R.drawable.ic_onboard_food,
            "Catat Makanan Hanya dengan Foto",
            "AI kami mengenali makanan Indonesia secara otomatis — kalori, protein, karbo, dan lemak langsung tercatat.",
            "#FAEEDA", "#854F0B"
        ));

        OnboardingAdapter adapter = new OnboardingAdapter(this, pages);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                updateButtons(position);
            }
        });
    }

    private void setupDots(int current) {
        dotsContainer.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < TOTAL_PAGES; i++) {
            View dot  = new View(this);
            int  w    = i == current ? Math.round(24 * density) : Math.round(8 * density);
            int  h    = Math.round(8 * density);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
            p.setMargins(Math.round(3 * density), 0, Math.round(3 * density), 0);
            dot.setLayoutParams(p);
            dot.setBackgroundResource(i == current
                    ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    private void updateButtons(int position) {
        if (position == TOTAL_PAGES - 1) {
            btnNext.setText("Mulai Sekarang");
            btnSkip.setVisibility(View.GONE);
        } else {
            btnNext.setText("Lanjut  \u2192");
            btnSkip.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            int cur = viewPager.getCurrentItem();
            if (cur < TOTAL_PAGES - 1) {
                viewPager.setCurrentItem(cur + 1, true);
            } else {
                goToGenderSelection();
            }
        });
        btnSkip.setOnClickListener(v -> goToGenderSelection());
    }

    private void goToGenderSelection() {
        PrefManager pref = new PrefManager(this);
        Intent intent = new Intent(this, GenderSelectionActivity.class);
        intent.putExtra("COUNTRY_CODE", pref.getCountryCode());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.home_enter, R.anim.splash_exit);
        finish();
    }
}
"@ | Set-Content -Path "$j/ui/onboarding/OnboardingActivity.java" -Encoding UTF8
Write-Host "  [OK] OnboardingActivity.java" -ForegroundColor Green

# ── activity_onboarding.xml ──────────────────────────────────
@'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#FFFFFF">

    <FrameLayout android:layout_width="match_parent" android:layout_height="60dp">
        <TextView android:id="@+id/btnSkip"
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:layout_gravity="center_vertical|end" android:layout_marginEnd="20dp"
            android:text="Lewati" android:textColor="#BBBBBB" android:textSize="13sp"
            android:padding="8dp" android:clickable="true" android:focusable="true"/>
    </FrameLayout>

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>

    <LinearLayout android:id="@+id/dotsContainer"
        android:layout_width="match_parent" android:layout_height="24dp"
        android:orientation="horizontal" android:gravity="center"
        android:layout_marginBottom="20dp"/>

    <TextView android:id="@+id/btnNext"
        android:layout_width="match_parent" android:layout_height="54dp"
        android:layout_marginStart="24dp" android:layout_marginEnd="24dp"
        android:layout_marginBottom="40dp"
        android:background="@drawable/bg_splash_gradient"
        android:gravity="center" android:text="Lanjut  &#8594;"
        android:textColor="#FFFFFF" android:textSize="16sp" android:textStyle="bold"
        android:clickable="true" android:focusable="true"/>

</LinearLayout>
'@ | Set-Content -Path "$r/layout/activity_onboarding.xml" -Encoding UTF8
Write-Host "  [OK] activity_onboarding.xml" -ForegroundColor Green

# ── item_onboarding_page.xml ─────────────────────────────────
@'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/layoutPageBg"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:paddingStart="36dp"
    android:paddingEnd="36dp">

    <FrameLayout android:layout_width="180dp" android:layout_height="180dp"
        android:layout_marginBottom="48dp">
        <View android:layout_width="180dp" android:layout_height="180dp"
            android:background="@drawable/bg_onboard_icon_circle" android:alpha="0.45"/>
        <ImageView android:id="@+id/ivOnboardIcon"
            android:layout_width="96dp" android:layout_height="96dp"
            android:layout_gravity="center" android:scaleType="fitCenter"
            android:contentDescription="Ilustrasi"/>
    </FrameLayout>

    <TextView android:id="@+id/tvOnboardTitle"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:textSize="22sp" android:textStyle="bold"
        android:gravity="center" android:lineSpacingMultiplier="1.2"
        android:layout_marginBottom="16dp"/>

    <TextView android:id="@+id/tvOnboardDesc"
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:textSize="14sp" android:textColor="#777777"
        android:gravity="center" android:lineSpacingMultiplier="1.6"/>
</LinearLayout>
'@ | Set-Content -Path "$r/layout/item_onboarding_page.xml" -Encoding UTF8
Write-Host "  [OK] item_onboarding_page.xml" -ForegroundColor Green

# ── Drawables ────────────────────────────────────────────────
@'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#FFFFFF"/>
</shape>
'@ | Set-Content -Path "$r/drawable/bg_onboard_icon_circle.xml" -Encoding UTF8
Write-Host "  [OK] bg_onboard_icon_circle.xml" -ForegroundColor Green

@'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#1D9E75"/>
    <corners android:radius="99dp"/>
</shape>
'@ | Set-Content -Path "$r/drawable/bg_dot_active.xml" -Encoding UTF8
Write-Host "  [OK] bg_dot_active.xml" -ForegroundColor Green

@'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#DDDDDD"/>
    <corners android:radius="99dp"/>
</shape>
'@ | Set-Content -Path "$r/drawable/bg_dot_inactive.xml" -Encoding UTF8
Write-Host "  [OK] bg_dot_inactive.xml" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  SELESAI! 8 file berhasil dibuat" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  LANGKAH WAJIB SETELAH INI:" -ForegroundColor Cyan
Write-Host ""
Write-Host "  1. Tambah ke AndroidManifest.xml:" -ForegroundColor White
Write-Host '     <activity android:name=".ui.onboarding.OnboardingActivity"/>' -ForegroundColor Yellow
Write-Host ""
Write-Host "  2. Di SplashActivity.java, ganti navigateNext():" -ForegroundColor White
Write-Host '     if (!pref.isOnboardingDone()) {' -ForegroundColor Yellow
Write-Host '         intent = new Intent(this, OnboardingActivity.class);' -ForegroundColor Yellow
Write-Host '     }' -ForegroundColor Yellow
Write-Host ""
Write-Host "  3. Tambah 3 ikon drawable (lihat catatan di bawah):" -ForegroundColor White
Write-Host "     ic_onboard_health.xml" -ForegroundColor Yellow
Write-Host "     ic_onboard_bmi.xml" -ForegroundColor Yellow
Write-Host "     ic_onboard_food.xml" -ForegroundColor Yellow
Write-Host ""
Write-Host "  CATATAN IKON:" -ForegroundColor Magenta
Write-Host "  Gunakan ikon dari Android Studio built-in:" -ForegroundColor White
Write-Host "  klik kanan res/drawable -> New -> Vector Asset" -ForegroundColor White
Write-Host "  Cari: 'favorite', 'monitor_weight', 'restaurant'" -ForegroundColor White
Write-Host "  Simpan dengan nama ic_onboard_health/bmi/food" -ForegroundColor White
Write-Host ""
