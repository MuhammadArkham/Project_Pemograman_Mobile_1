package com.fitme.app.ui.makan;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitme.app.R;
import com.fitme.app.database.FoodEntity;
import com.fitme.app.ui.base.BaseActivity;
import com.fitme.app.ui.home.CurvedBottomBarBg;
import com.fitme.app.ui.home.HomeActivity;
import com.fitme.app.ui.history.HistoryActivity;
import com.fitme.app.utils.PrefManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MakanActivity extends BaseActivity {

    public int TARGET_CALORIES = 1800;
    public static final int REQUEST_ANALYZE = 1001;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final int COLOR_REMAINING_OK  = 0xFF1BC99A;
    private static final int COLOR_REMAINING_OVR = 0xFFE24B4A;
    private static final int COLOR_PROGRESS_TRACK= 0xFFD4EDE8;
    private static final int COLOR_PROGRESS_FILL = 0xFF1BC99A;

    private FoodLogAdapter   adapter;
    private List<FoodEntity> foodList = new ArrayList<>();
    private String           todayDate;
    private String           countryCode = "ID";

    private TextView    tvTotalCalories, tvRemaining, tvItemCount;
    private TextView    tvProtein, tvKarbo, tvLemak;
    private TextView    tvTargetCalories;
    private ProgressBar progressCalories;

    private MaterialCardView cardFoodSection;
    private LinearLayout     layoutEmptyState;

    private LinearLayout  btnNavHome, btnNavMakan, btnNavHistory;
    private ImageView     icNavHome, icNavMakan, icNavHistory, icNavFloating;
    private TextView      tvNavHome, tvNavMakan, tvNavHistory;
    private CardView      navIndicator;
    private CurvedBottomBarBg curvedBg;

    private static final int NAV_HOME    = 0;
    private static final int NAV_MAKAN   = 1;
    private static final int NAV_HISTORY = 2;
    private static final int NAV_ANIM_MS = 220;

    private int currentNav = NAV_MAKAN;
    private int COLOR_ACTIVE;
    private int COLOR_INACTIVE;

    private MakanViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makan);

        viewModel = new ViewModelProvider(this).get(MakanViewModel.class);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);

        PrefManager pref = new PrefManager(this);
        countryCode = pref.getCountryCode() != null ? pref.getCountryCode() : "ID";

        if (getIntent() != null && getIntent().hasExtra("COUNTRY_CODE")) {
            String passed = getIntent().getStringExtra("COUNTRY_CODE");
            if (passed != null && !passed.isEmpty()) countryCode = passed;
        }

        TARGET_CALORIES = pref.getDailyCalorieTarget();
        todayDate = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());

        COLOR_ACTIVE   = ContextCompat.getColor(this, R.color.fitme_green);
        COLOR_INACTIVE = ContextCompat.getColor(this, R.color.nav_inactive);

        bindViews();
        initBottomNav();
        setupRecyclerView();
        setupFab();

        // FIX 4: Panggil observer di onCreate
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TARGET_CALORIES = new PrefManager(this).getDailyCalorieTarget();
        progressCalories.setMax(TARGET_CALORIES);
        updateTargetDisplay();
        moveIndicatorTo(NAV_MAKAN, false);
        // FIX 4: reloadData() sudah dihapus dari sini (akan dihandle otomatis oleh LiveData)
    }

    private void bindViews() {
        tvTotalCalories  = findViewById(R.id.tv_total_calories);
        tvRemaining      = findViewById(R.id.tv_remaining);
        tvItemCount      = findViewById(R.id.tv_item_count);
        tvProtein        = findViewById(R.id.tv_protein);
        tvKarbo          = findViewById(R.id.tv_karbo);
        tvLemak          = findViewById(R.id.tv_lemak);
        tvTargetCalories = findViewById(R.id.tv_target_calories);
        progressCalories = findViewById(R.id.progress_calories);
        cardFoodSection  = findViewById(R.id.card_food_section);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        progressCalories.setMax(TARGET_CALORIES);
        styleProgressBar();
        updateTargetDisplay();
    }

    private void styleProgressBar() {
        if (progressCalories == null) return;

        GradientDrawable track = new GradientDrawable();
        track.setShape(GradientDrawable.RECTANGLE);
        track.setCornerRadius(999f);
        track.setColor(COLOR_PROGRESS_TRACK);

        GradientDrawable fill = new GradientDrawable();
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(999f);
        fill.setColor(COLOR_PROGRESS_FILL);

        ClipDrawable clip = new ClipDrawable(fill, android.view.Gravity.START, ClipDrawable.HORIZONTAL);

        LayerDrawable layer = new LayerDrawable(new android.graphics.drawable.Drawable[]{track, clip, clip});
        layer.setId(0, android.R.id.background);
        layer.setId(1, android.R.id.secondaryProgress);
        layer.setId(2, android.R.id.progress);

        progressCalories.setProgressDrawable(layer);
    }

    private void updateTargetDisplay() {
        if (tvTargetCalories == null) return;
        String formatted = String.format(Locale.US, "%,d", TARGET_CALORIES).replace(',', '.');
        tvTargetCalories.setText(formatted + " " + getString(R.string.food_calories));
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_food_log);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // FIX 3: Hapus callback dari parameter deleteFood, hanya terima item.id
        adapter = new FoodLogAdapter(this, foodList, (item, position) ->
                viewModel.deleteFood(item.id));

        rv.setAdapter(adapter);
    }

    private void setupFab() {
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add_food);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, FoodAnalysisActivity.class);
            intent.putExtra("COUNTRY_CODE", countryCode);
            startActivityForResult(intent, REQUEST_ANALYZE);
        });
    }

    // FIX 4: Buat method observeViewModel() untuk me-listen data LiveData dari MakanViewModel
    private void observeViewModel() {
        viewModel.getFoodByDate(todayDate).observe(this, data -> {
            foodList.clear();
            if (data != null) {
                foodList.addAll(data);
            }
            adapter.notifyDataSetChanged();
            updateHeader();
        });
    }

    private void updateHeader() {
        int totalCal = 0, totalP = 0, totalK = 0, totalL = 0;
        for (FoodEntity item : foodList) {
            totalCal += item.calories;
            totalP   += item.protein;
            totalK   += item.carbs;
            totalL   += item.fat;
        }

        tvTotalCalories.setText(String.valueOf(totalCal));
        animateProgress(Math.min(totalCal, TARGET_CALORIES));

        int remaining = TARGET_CALORIES - totalCal;
        if (remaining >= 0) {
            tvRemaining.setText(remaining + " " + getString(R.string.food_calories));
            tvRemaining.setTextColor(COLOR_REMAINING_OK);
        } else {
            tvRemaining.setText("-" + Math.abs(remaining) + " " + getString(R.string.food_calories));
            tvRemaining.setTextColor(COLOR_REMAINING_OVR);
        }

        tvProtein.setText(String.valueOf(totalP));
        tvKarbo.setText(String.valueOf(totalK));
        tvLemak.setText(String.valueOf(totalL));

        int count = foodList.size();
        if (cardFoodSection  != null) cardFoodSection.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(count > 0 ? View.GONE : View.VISIBLE);

        String countLabel = getString(
                count == 1 ? R.string.record_count_single
                        : R.string.record_count_plural,
                count);
        tvItemCount.setText(countLabel);
    }

    private void animateProgress(int targetProgress) {
        ValueAnimator anim = ValueAnimator.ofInt(progressCalories.getProgress(), targetProgress);
        anim.setDuration(500);
        anim.setInterpolator(new OvershootInterpolator(0.6f));
        anim.addUpdateListener(a -> progressCalories.setProgress((int) a.getAnimatedValue()));
        anim.start();
    }

    private void initBottomNav() {
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
        if (container != null) {
            container.setClipChildren(false);
            container.setClipToPadding(false);
        }
        if (navBar != null) {
            navBar.setClipChildren(false);
            navBar.setClipToPadding(false);
            navBar.setElevation(20f);
        }
        if (icNavFloating != null) icNavFloating.setVisibility(View.VISIBLE);

        if (btnNavHome != null) {
            btnNavHome.setOnClickListener(v -> {
                if (currentNav == NAV_HOME) return;
                moveIndicatorTo(NAV_HOME, true);
                btnNavHome.postDelayed(() -> {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra("COUNTRY_CODE", countryCode);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }, NAV_ANIM_MS);
            });
        }

        if (btnNavMakan != null)
            btnNavMakan.setOnClickListener(v -> moveIndicatorTo(NAV_MAKAN, true));

        if (btnNavHistory != null) {
            btnNavHistory.setOnClickListener(v -> {
                if (currentNav == NAV_HISTORY) return;
                moveIndicatorTo(NAV_HISTORY, true);
                btnNavHistory.postDelayed(() -> {
                    Intent intent = new Intent(this, HistoryActivity.class);
                    intent.putExtra("COUNTRY_CODE", countryCode);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }, NAV_ANIM_MS);
            });
        }
    }

    private void moveIndicatorTo(int index, boolean animate) {
        currentNav = index;
        if (navIndicator == null || curvedBg == null) return;

        float screenW = getResources().getDisplayMetrics().widthPixels;
        float targetCenterX;
        if      (index == NAV_HOME)  targetCenterX = screenW * (1f / 6f);
        else if (index == NAV_MAKAN) targetCenterX = screenW * (3f / 6f);
        else                         targetCenterX = screenW * (5f / 6f);

        float indicatorW   = 46f * getResources().getDisplayMetrics().density;
        float targetTransX = targetCenterX - (indicatorW / 2f);

        if (animate) {
            ObjectAnimator slideCircle = ObjectAnimator.ofFloat(
                    navIndicator, "translationX",
                    navIndicator.getTranslationX(), targetTransX);
            slideCircle.setDuration(NAV_ANIM_MS);
            slideCircle.setInterpolator(new OvershootInterpolator(1.0f));
            slideCircle.start();

            float fromCurve = curvedBg.getCutoutX() > 0
                    ? curvedBg.getCutoutX() : targetCenterX;
            ValueAnimator slideCurve = ValueAnimator.ofFloat(fromCurve, targetCenterX);
            slideCurve.setDuration(NAV_ANIM_MS);
            slideCurve.setInterpolator(new OvershootInterpolator(1.0f));
            slideCurve.addUpdateListener(
                    a -> curvedBg.setCutoutX((float) a.getAnimatedValue()));
            slideCurve.start();
        } else {
            navIndicator.setTranslationX(targetTransX);
            curvedBg.setCutoutX(targetCenterX);
        }

        updateNavColors(index, animate);
    }

    private void updateNavColors(int index, boolean animate) {
        int colorHome    = (index == NAV_HOME)    ? COLOR_ACTIVE : COLOR_INACTIVE;
        int colorMakan   = (index == NAV_MAKAN)   ? COLOR_ACTIVE : COLOR_INACTIVE;
        int colorHistory = (index == NAV_HISTORY) ? COLOR_ACTIVE : COLOR_INACTIVE;

        if (icNavFloating != null) {
            if      (index == NAV_HOME)  icNavFloating.setImageResource(R.drawable.ic_home);
            else if (index == NAV_MAKAN) icNavFloating.setImageResource(R.drawable.ic_restaurant);
            else                         icNavFloating.setImageResource(R.drawable.ic_history);
        }

        float textUp = -4f * getResources().getDisplayMetrics().density;

        if (animate) {
            if (icNavHome != null)
                icNavHome.animate().alpha(index == NAV_HOME ? 0f : 1f).setDuration(NAV_ANIM_MS).start();
            if (icNavMakan != null)
                icNavMakan.animate().alpha(index == NAV_MAKAN ? 0f : 1f).setDuration(NAV_ANIM_MS).start();
            if (icNavHistory != null)
                icNavHistory.animate().alpha(index == NAV_HISTORY ? 0f : 1f).setDuration(NAV_ANIM_MS).start();

            if (tvNavHome != null) {
                tvNavHome.animate().translationY(index == NAV_HOME ? textUp : 0f).setDuration(NAV_ANIM_MS).start();
                animateTextColor(tvNavHome, colorHome);
            }
            if (tvNavMakan != null) {
                tvNavMakan.animate().translationY(index == NAV_MAKAN ? textUp : 0f).setDuration(NAV_ANIM_MS).start();
                animateTextColor(tvNavMakan, colorMakan);
            }
            if (tvNavHistory != null) {
                tvNavHistory.animate().translationY(index == NAV_HISTORY ? textUp : 0f).setDuration(NAV_ANIM_MS).start();
                animateTextColor(tvNavHistory, colorHistory);
            }
        } else {
            if (icNavHome    != null) icNavHome.setAlpha(index == NAV_HOME ? 0f : 1f);
            if (icNavMakan   != null) icNavMakan.setAlpha(index == NAV_MAKAN ? 0f : 1f);
            if (icNavHistory != null) icNavHistory.setAlpha(index == NAV_HISTORY ? 0f : 1f);

            if (tvNavHome != null) {
                tvNavHome.setTranslationY(index == NAV_HOME ? textUp : 0f);
                tvNavHome.setTextColor(colorHome);
            }
            if (tvNavMakan != null) {
                tvNavMakan.setTranslationY(index == NAV_MAKAN ? textUp : 0f);
                tvNavMakan.setTextColor(colorMakan);
            }
            if (tvNavHistory != null) {
                tvNavHistory.setTranslationY(index == NAV_HISTORY ? textUp : 0f);
                tvNavHistory.setTextColor(colorHistory);
            }
        }
    }

    private void animateTextColor(TextView tv, int toColor) {
        ValueAnimator va = ValueAnimator.ofObject(new ArgbEvaluator(), tv.getCurrentTextColor(), toColor);
        va.setDuration(NAV_ANIM_MS);
        va.addUpdateListener(a -> tv.setTextColor((int) a.getAnimatedValue()));
        va.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // FIX 4: reloadData() sudah dihapus. Jika data bertambah, ViewModel yang menggunakan LiveData
        // akan langsung mengirimkan update ke observer kita secara otomatis!
    }

    @Override
    public void onBackPressed() {
        if (btnNavHome != null) btnNavHome.performClick();
        else super.onBackPressed();
    }
}