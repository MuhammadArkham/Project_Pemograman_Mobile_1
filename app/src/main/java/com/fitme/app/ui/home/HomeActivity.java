package com.fitme.app.ui.home;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitme.app.R;
import com.fitme.app.database.ArticleEntity;
import com.fitme.app.database.BmiEntity;
import com.fitme.app.ui.articles.ArticleAdapter;
import com.fitme.app.ui.articles.ArticlesActivity;
import com.fitme.app.ui.base.BaseActivity;
import com.fitme.app.ui.bmi.GenderSelectionActivity;
import com.fitme.app.ui.bmi.InputDataActivity;
import com.fitme.app.ui.history.HistoryActivity;
import com.fitme.app.ui.makan.MakanActivity;
import com.fitme.app.ui.profil.ProfilActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends BaseActivity {

    // ─── Konstanta ────────────────────────────────────────────────────────────
    private static final int NAV_HOME    = 0;
    private static final int NAV_MAKAN   = 1;
    private static final int NAV_HISTORY = 2;
    private static final int NAV_ANIM_MS = 220;

    // ─── State ────────────────────────────────────────────────────────────────
    private String countryCode  = "ID";
    private String detectedCity = "";
    private int    currentNav   = NAV_HOME;
    private int    COLOR_ACTIVE, COLOR_INACTIVE;

    // ─── Views ────────────────────────────────────────────────────────────────
    private TextView          tvHeaderDate, tvGreetingTime, tvGreetingName;
    private TextView          btnCalculate, tvProgressBmi;
    private View              viewProgressFill;
    private LinearLayout      btnNavHome, btnNavHistory, btnNavMakan;
    private ImageView         icNavHome, icNavHistory, icNavMakan, icNavFloating;
    private TextView          tvNavHome, tvNavHistory, tvNavMakan;
    private CardView          navIndicator;
    private CurvedBottomBarBg curvedBg;
    private RecyclerView      rvHomeArticles;

    // ─── ViewModel & Adapter ──────────────────────────────────────────────────
    private HomeViewModel  viewModel;
    private ArticleAdapter homeArticleAdapter;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        viewModel    = new ViewModelProvider(this).get(HomeViewModel.class);
        countryCode  = viewModel.getCountryCode();
        detectedCity = viewModel.getDetectedCity();

        if (getIntent() != null && getIntent().hasExtra("COUNTRY_CODE")) {
            String passed = getIntent().getStringExtra("COUNTRY_CODE");
            if (passed != null && !passed.isEmpty()) countryCode = passed;
        }

        COLOR_ACTIVE   = ContextCompat.getColor(this, R.color.fitme_green);
        COLOR_INACTIVE = ContextCompat.getColor(this, R.color.nav_inactive);

        initViews();
        applyDynamicGreeting();
        applyDynamicDate();
        setupListeners();
        setupArticlesSlider();
        observeViewModel();

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override public void handleOnBackPressed() { finish(); }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        moveIndicatorTo(NAV_HOME, false);
        updateGreetingName();
    }

    // ─── Init Views ───────────────────────────────────────────────────────────
    private void initViews() {
        tvHeaderDate     = findViewById(R.id.tvHeaderDate);
        tvGreetingTime   = findViewById(R.id.tvGreetingTime);
        tvGreetingName   = findViewById(R.id.tvGreetingName);
        btnCalculate     = findViewById(R.id.btnCalculate);
        tvProgressBmi    = findViewById(R.id.tvProgressBmi);
        viewProgressFill = findViewById(R.id.viewProgressFill);

        btnNavHome    = findViewById(R.id.btnNavHome);
        btnNavMakan   = findViewById(R.id.btnNavMakan);
        btnNavHistory = findViewById(R.id.btnNavHistory);
        icNavHome     = findViewById(R.id.icNavHome);
        icNavMakan    = findViewById(R.id.icNavMakan);
        icNavHistory  = findViewById(R.id.icNavHistory);
        tvNavHome     = findViewById(R.id.tvNavHome);
        tvNavMakan    = findViewById(R.id.tvNavMakan);
        tvNavHistory  = findViewById(R.id.tvNavHistory);
        icNavFloating = findViewById(R.id.icNavFloating);
        navIndicator  = findViewById(R.id.navIndicator);
        curvedBg      = findViewById(R.id.curvedBg);

        float density = getResources().getDisplayMetrics().density;
        if (navIndicator != null) {
            navIndicator.getLayoutParams().width  = (int) (46 * density);
            navIndicator.getLayoutParams().height = (int) (46 * density);
            navIndicator.setRadius(23 * density);
            navIndicator.setTranslationY(8f * density);
            navIndicator.setCardBackgroundColor(COLOR_ACTIVE);
            navIndicator.requestLayout();
        }

        FrameLayout  container = findViewById(R.id.layoutBottomNavContainer);
        LinearLayout navBar    = findViewById(R.id.navBar);
        if (container != null) { container.setClipChildren(false); container.setClipToPadding(false); }
        if (navBar    != null) { navBar.setClipChildren(false); navBar.setClipToPadding(false); navBar.setElevation(20f); }
        if (icNavFloating != null) icNavFloating.setVisibility(View.VISIBLE);

        // Responsive Symmetrical Sizing for BMI Categories
        View cardUnderweight = findViewById(R.id.cardUnderweight);
        View cardNormal      = findViewById(R.id.cardNormal);
        View cardOverweight  = findViewById(R.id.cardOverweight);
        View cardObese       = findViewById(R.id.cardObese);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        // Target container width is (screenWidth - 40dp margins)
        // We want 2 full cards + 2 margins (10dp each) + 24dp peek of the 3rd card = container width
        // 2 * cardWidth + 20dp + 24dp = screenWidth - 40dp -> 2 * cardWidth = screenWidth - 84dp
        int formulaMarginPx = (int) (84 * density);
        int categoryCardWidth = (screenWidth - formulaMarginPx) / 2;

        if (cardUnderweight != null) cardUnderweight.getLayoutParams().width = categoryCardWidth;
        if (cardNormal      != null) cardNormal.getLayoutParams().width      = categoryCardWidth;
        if (cardOverweight  != null) cardOverweight.getLayoutParams().width  = categoryCardWidth;
        if (cardObese       != null) cardObese.getLayoutParams().width       = categoryCardWidth;
    }

    // ─── Articles Slider ──────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void setupArticlesSlider() {
        rvHomeArticles = findViewById(R.id.rvHomeArticles);
        if (rvHomeArticles == null) return;

        android.widget.LinearLayout layoutShimmer = findViewById(R.id.layoutArticleShimmer);

        rvHomeArticles.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHomeArticles.setNestedScrollingEnabled(false);
        homeArticleAdapter = new ArticleAdapter(this, ArticleAdapter.VIEW_TYPE_HORIZONTAL, new ArticleAdapter.OnArticleClickListener() {
            @Override
            public void onArticleClick(ArticleEntity article) {
                if (article.link == null || article.link.isEmpty()) return;
                try {
                    Intent intent = new Intent(HomeActivity.this, com.fitme.app.ui.articles.ArticleDetailActivity.class);
                    intent.putExtra(com.fitme.app.ui.articles.ArticleDetailActivity.EXTRA_URL, article.link);
                    intent.putExtra(com.fitme.app.ui.articles.ArticleDetailActivity.EXTRA_TITLE, article.title != null ? article.title : "");
                    startActivity(intent);
                } catch (Exception ignored) {}
            }

            @Override
            public void onBookmarkClick(ArticleEntity article) {
                // Di HomeActivity, kita biarkan saja atau update database via viewModel
                if (viewModel != null) {
                    viewModel.toggleBookmark(article);
                }
            }
        });
        rvHomeArticles.setAdapter(homeArticleAdapter);

        // Observe loading state → tampilkan shimmer saat loading
        viewModel.getArticleLoading().observe(this, isLoading -> {
            if (layoutShimmer == null) return;
            if (Boolean.TRUE.equals(isLoading)) {
                layoutShimmer.setVisibility(View.VISIBLE);
                rvHomeArticles.setVisibility(View.GONE);
            } else {
                layoutShimmer.setVisibility(View.GONE);
                if (homeArticleAdapter.getItemCount() > 0)
                    rvHomeArticles.setVisibility(View.VISIBLE);
            }
        });

        // Observe artikel dari Room — update otomatis saat DB berubah
        viewModel.articlesLive.observe(this, articles -> {
            if (articles != null && !articles.isEmpty()) {
                homeArticleAdapter.setArticles(
                        (List<ArticleEntity>) articles.subList(0, Math.min(articles.size(), 10))
                );
                // Tampilkan RV, sembunyikan shimmer
                if (layoutShimmer != null) layoutShimmer.setVisibility(View.GONE);
                rvHomeArticles.setVisibility(View.VISIBLE);
            }
        });
    }

    // ─── Observe ─────────────────────────────────────────────────────────────
    private void observeViewModel() {
        viewModel.lastBmiLive.observe(this, this::updateBmiProgress);
    }

    // ─── Listeners ───────────────────────────────────────────────────────────
    private void setupListeners() {
        // Profil
        View btnProfileIcon = findViewById(R.id.btnProfileIcon);
        if (btnProfileIcon != null) {
            btnProfileIcon.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfilActivity.class)
                            .putExtra("COUNTRY_CODE", countryCode)));
        }

        // Hitung BMI
        if (btnCalculate != null) {
            btnCalculate.setOnClickListener(v -> {
                Intent i = viewModel.hasProfile()
                        ? new Intent(this, InputDataActivity.class)
                        .putExtra("USER_GENDER", viewModel.isUserMale())
                        : new Intent(this, GenderSelectionActivity.class);
                i.putExtra("COUNTRY_CODE",  countryCode);
                i.putExtra("DETECTED_CITY", detectedCity);
                startActivity(i);
            });
        }

        // Navigasi bawah
        if (btnNavHome != null) {
            btnNavHome.setOnClickListener(v -> {
                if (currentNav != NAV_HOME) moveIndicatorTo(NAV_HOME, true);
            });
        }
        if (btnNavMakan != null) {
            btnNavMakan.setOnClickListener(v -> {
                if (currentNav == NAV_MAKAN) return;
                moveIndicatorTo(NAV_MAKAN, true);
                btnNavMakan.postDelayed(() -> startActivitySmooth(
                        new Intent(this, MakanActivity.class)
                                .putExtra("COUNTRY_CODE", countryCode)), NAV_ANIM_MS);
            });
        }
        if (btnNavHistory != null) {
            btnNavHistory.setOnClickListener(v -> {
                if (currentNav == NAV_HISTORY) return;
                moveIndicatorTo(NAV_HISTORY, true);
                btnNavHistory.postDelayed(() -> startActivitySmooth(
                        new Intent(this, HistoryActivity.class)
                                .putExtra("COUNTRY_CODE", countryCode)), NAV_ANIM_MS);
            });
        }

        // Kartu kategori BMI → ArticlesActivity dengan filter kategori
        setupCategoryCard(R.id.cardUnderweight, "Nutrisi");
        setupCategoryCard(R.id.cardNormal,      "Kesehatan");
        setupCategoryCard(R.id.cardOverweight,  "Diet");
        setupCategoryCard(R.id.cardObese,       "Olahraga");

        // Tombol Lihat Semua
        View btnViewAll = findViewById(R.id.btnViewAllArticles);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v ->
                    startActivity(new Intent(this, ArticlesActivity.class)
                            .putExtra("FILTER_CATEGORY", "Semua")));
        }
    }

    private void setupCategoryCard(int viewId, String filterCategory) {
        View card = findViewById(viewId);
        if (card != null) {
            card.setOnClickListener(v ->
                    startActivity(new Intent(this, ArticlesActivity.class)
                            .putExtra("FILTER_CATEGORY", filterCategory)));
        }
    }

    private void startActivitySmooth(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    // ─── BMI Progress ─────────────────────────────────────────────────────────
    private void updateBmiProgress(BmiEntity latest) {
        if (tvProgressBmi == null || viewProgressFill == null) return;

        if (latest == null) {
            tvProgressBmi.setText("--");
            setProgressWeight(viewProgressFill, 0f);
            viewProgressFill.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.fitme_green_disabled));
            return;
        }

        float bmi = latest.bmi;
        tvProgressBmi.setText(String.format(Locale.US, "%.1f", bmi));
        setProgressWeight(viewProgressFill, viewModel.calculateBarFraction(bmi) * 100f);

        int colorRes;
        if      (bmi < 18.5f) colorRes = R.color.bmi_underweight;
        else if (bmi < 25.0f) colorRes = R.color.bmi_normal;
        else if (bmi < 30.0f) colorRes = R.color.bmi_overweight;
        else                   colorRes = R.color.bmi_obese;
        viewProgressFill.setBackgroundColor(ContextCompat.getColor(this, colorRes));
    }

    private void setProgressWeight(View view, float weight) {
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) view.getLayoutParams();
        p.weight = weight;
        view.setLayoutParams(p);
    }

    private void updateGreetingName() {
        if (tvGreetingName == null) return;
        String name = viewModel.getUserName();
        if (name != null && !name.trim().isEmpty()) {
            tvGreetingName.setText(getString(R.string.greeting_hello_name,
                    name.split("\\s+")[0]));
        } else {
            tvGreetingName.setText(R.string.greeting_name);
        }
    }

    // ─── Dynamic Header ───────────────────────────────────────────────────────
    private void applyDynamicGreeting() {
        if (tvGreetingTime == null) return;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int resId;
        if      (h >= 5  && h < 12) resId = R.string.greeting_morning;
        else if (h >= 12 && h < 15) resId = R.string.greeting_afternoon;
        else if (h >= 15 && h < 19) resId = R.string.greeting_evening;
        else                         resId = R.string.greeting_night;
        tvGreetingTime.setText(resId);
    }

    private void applyDynamicDate() {
        if (tvHeaderDate == null) return;
        Locale loc = getResources().getConfiguration().getLocales().get(0);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, dd MMM", loc);
        String d = sdf.format(new java.util.Date());
        if (!d.isEmpty()) d = Character.toUpperCase(d.charAt(0)) + d.substring(1);
        tvHeaderDate.setText(d);
    }

    // ─── Bottom Navigation ────────────────────────────────────────────────────
    private void moveIndicatorTo(int index, boolean animate) {
        currentNav = index;
        if (navIndicator == null || curvedBg == null) return;

        float density  = getResources().getDisplayMetrics().density;
        float screenW  = getResources().getDisplayMetrics().widthPixels;
        float fraction = (index == NAV_HOME) ? 1f/6f : (index == NAV_MAKAN) ? 3f/6f : 5f/6f;
        float centerX  = screenW * fraction;
        float targetX  = centerX - (46f * density / 2f);

        if (animate) {
            ObjectAnimator slideCircle = ObjectAnimator.ofFloat(
                    navIndicator, "translationX", navIndicator.getTranslationX(), targetX);
            slideCircle.setDuration(NAV_ANIM_MS);
            slideCircle.setInterpolator(new OvershootInterpolator(1f));
            slideCircle.start();

            float fromX = curvedBg.getCutoutX() > 0 ? curvedBg.getCutoutX() : centerX;
            ValueAnimator slideCurve = ValueAnimator.ofFloat(fromX, centerX);
            slideCurve.setDuration(NAV_ANIM_MS);
            slideCurve.setInterpolator(new OvershootInterpolator(1f));
            slideCurve.addUpdateListener(a -> curvedBg.setCutoutX((float) a.getAnimatedValue()));
            slideCurve.start();
        } else {
            navIndicator.setTranslationX(targetX);
            curvedBg.setCutoutX(centerX);
        }
        updateNavColors(index, animate);
    }

    private void updateNavColors(int index, boolean animate) {
        int cHome    = index == NAV_HOME    ? COLOR_ACTIVE : COLOR_INACTIVE;
        int cMakan   = index == NAV_MAKAN   ? COLOR_ACTIVE : COLOR_INACTIVE;
        int cHistory = index == NAV_HISTORY ? COLOR_ACTIVE : COLOR_INACTIVE;
        float up     = -4f * getResources().getDisplayMetrics().density;

        if (icNavFloating != null) {
            icNavFloating.setImageResource(
                    index == NAV_HOME    ? R.drawable.ic_home :
                            index == NAV_MAKAN   ? R.drawable.ic_restaurant :
                                    R.drawable.ic_history);
        }

        if (animate) {
            animateNavItem(icNavHome,    tvNavHome,    index == NAV_HOME,    cHome,    up);
            animateNavItem(icNavMakan,   tvNavMakan,   index == NAV_MAKAN,   cMakan,   up);
            animateNavItem(icNavHistory, tvNavHistory, index == NAV_HISTORY, cHistory, up);
        } else {
            setNavItem(icNavHome,    tvNavHome,    index == NAV_HOME,    cHome,    up);
            setNavItem(icNavMakan,   tvNavMakan,   index == NAV_MAKAN,   cMakan,   up);
            setNavItem(icNavHistory, tvNavHistory, index == NAV_HISTORY, cHistory, up);
        }
    }

    private void animateNavItem(ImageView icon, TextView label,
                                boolean active, int color, float up) {
        if (icon  != null) icon.animate().alpha(active ? 0f : 1f).setDuration(NAV_ANIM_MS).start();
        if (label != null) {
            label.animate().translationY(active ? up : 0f).setDuration(NAV_ANIM_MS).start();
            ValueAnimator va = ValueAnimator.ofObject(
                    new ArgbEvaluator(), label.getCurrentTextColor(), color);
            va.setDuration(NAV_ANIM_MS);
            va.addUpdateListener(a -> label.setTextColor((int) a.getAnimatedValue()));
            va.start();
        }
    }

    private void setNavItem(ImageView icon, TextView label,
                            boolean active, int color, float up) {
        if (icon  != null) icon.setAlpha(active ? 0f : 1f);
        if (label != null) { label.setTranslationY(active ? up : 0f); label.setTextColor(color); }
    }
}