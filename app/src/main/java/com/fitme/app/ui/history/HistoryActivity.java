package com.fitme.app.ui.history;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitme.app.R;
import com.fitme.app.database.BmiEntity;
import com.fitme.app.ui.base.BaseActivity;
import com.fitme.app.ui.home.HomeActivity;
import com.fitme.app.ui.home.CurvedBottomBarBg;
import com.fitme.app.ui.makan.MakanActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends BaseActivity {

    // ── Chart colors ─────────────────────────────────────────────────────────
    private static final String HEX_DOT_UNDER  = "#378ADD";
    private static final String HEX_DOT_NORMAL = "#1D9E75";
    private static final String HEX_DOT_OVER   = "#EF9F27";
    private static final String HEX_DOT_OBESE  = "#E24B4A";

    private static final String HEX_LINE_185   = "#378ADD";
    private static final String HEX_LINE_250   = "#EF9F27";
    private static final String HEX_LINE_300   = "#E24B4A";
    private static final String HEX_CHART_LINE = "#1D9E75";

    // ── State ─────────────────────────────────────────────────────────────────
    private List<BmiEntity> allData      = new ArrayList<>();
    private int             activePeriod = 3;
    private String          countryCode  = "ID";

    // ── Views ─────────────────────────────────────────────────────────────────
    private RecyclerView    recyclerView;
    private HistoryAdapter  adapter;

    private TextView        tvHeaderBmiValue, tvHeaderBmiRange, tvHeaderBmiMeta;
    private TextView        tvCountBadge, btnPeriod3, btnPeriod6, btnPeriodAll;
    private LinearLayout    layoutEmpty;
    private LineChart       lineChart;

    // ── Bottom Nav ────────────────────────────────────────────────────────────
    private LinearLayout    btnNavHome, btnNavMakan, btnNavHistory;
    private ImageView       icNavHome, icNavMakan, icNavHistory, icNavFloating;
    private TextView        tvNavHome, tvNavMakan, tvNavHistory;
    private CardView        navIndicator;
    private CurvedBottomBarBg curvedBg;

    private static final int NAV_HOME    = 0;
    private static final int NAV_MAKAN   = 1;
    private static final int NAV_HISTORY = 2;

    private int currentNav = NAV_HISTORY;
    private int COLOR_ACTIVE;
    private int COLOR_INACTIVE;
    private static final int NAV_ANIM_MS = 220;

    // ── ViewModel ─────────────────────────────────────────────────────────────
    private HistoryViewModel viewModel;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_history);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        if (getIntent() != null && getIntent().hasExtra("COUNTRY_CODE")) {
            countryCode = getIntent().getStringExtra("COUNTRY_CODE");
        }

        COLOR_ACTIVE   = ContextCompat.getColor(this, R.color.fitme_green);
        COLOR_INACTIVE = ContextCompat.getColor(this, R.color.nav_inactive);

        initViews();
        initBottomNav();
        setupListeners();
        setupChart();

        // ✅ Observer LiveData — Room push update otomatis
        observeViewModel();
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    private void initViews() {
        tvHeaderBmiValue = findViewById(R.id.tvHeaderBmiValue);
        tvHeaderBmiRange = findViewById(R.id.tvHeaderBmiRange);
        tvHeaderBmiMeta  = findViewById(R.id.tvHeaderBmiMeta);
        tvCountBadge     = findViewById(R.id.tvCountBadge);
        btnPeriod3       = findViewById(R.id.btnPeriod3);
        btnPeriod6       = findViewById(R.id.btnPeriod6);
        btnPeriodAll     = findViewById(R.id.btnPeriodAll);
        layoutEmpty      = findViewById(R.id.layoutEmpty);
        lineChart        = findViewById(R.id.lineChart);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(
                this, allData, this::onDeleteItem, this::onDetailItem);
        recyclerView.setAdapter(adapter);

        lineChart.setNoDataText(getString(R.string.no_bmi_data));
        lineChart.setNoDataTextColor(Color.parseColor("#8D9994"));
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
    }

    // ── ViewModel Observer ────────────────────────────────────────────────────
    private void observeViewModel() {
        // ✅ Room LiveData — update otomatis tanpa loadData() manual
        viewModel.getHistory().observe(this, data -> {
            allData.clear();
            allData.addAll(data);
            renderStats();
            renderChart(activePeriod);
            renderList();
        });
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Tidak perlu panggil loadData() — observer sudah aktif
        moveIndicatorTo(NAV_HISTORY, false);
    }

    @Override
    public void onBackPressed() {
        if (currentNav != NAV_HOME && btnNavHome != null) {
            btnNavHome.performClick();
        } else {
            super.onBackPressed();
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────
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
            if (icNavHome    != null) icNavHome.animate().alpha(index == NAV_HOME ? 0f : 1f).setDuration(NAV_ANIM_MS).start();
            if (icNavMakan   != null) icNavMakan.animate().alpha(index == NAV_MAKAN ? 0f : 1f).setDuration(NAV_ANIM_MS).start();
            if (icNavHistory != null) icNavHistory.animate().alpha(index == NAV_HISTORY ? 0f : 1f).setDuration(NAV_ANIM_MS).start();
            if (tvNavHome    != null) { tvNavHome.animate().translationY(index == NAV_HOME ? textUp : 0f).setDuration(NAV_ANIM_MS).start(); animateTextColor(tvNavHome, colorHome); }
            if (tvNavMakan   != null) { tvNavMakan.animate().translationY(index == NAV_MAKAN ? textUp : 0f).setDuration(NAV_ANIM_MS).start(); animateTextColor(tvNavMakan, colorMakan); }
            if (tvNavHistory != null) { tvNavHistory.animate().translationY(index == NAV_HISTORY ? textUp : 0f).setDuration(NAV_ANIM_MS).start(); animateTextColor(tvNavHistory, colorHistory); }
        } else {
            if (icNavHome    != null) icNavHome.setAlpha(index == NAV_HOME ? 0f : 1f);
            if (icNavMakan   != null) icNavMakan.setAlpha(index == NAV_MAKAN ? 0f : 1f);
            if (icNavHistory != null) icNavHistory.setAlpha(index == NAV_HISTORY ? 0f : 1f);
            if (tvNavHome    != null) { tvNavHome.setTranslationY(index == NAV_HOME ? textUp : 0f); tvNavHome.setTextColor(colorHome); }
            if (tvNavMakan   != null) { tvNavMakan.setTranslationY(index == NAV_MAKAN ? textUp : 0f); tvNavMakan.setTextColor(colorMakan); }
            if (tvNavHistory != null) { tvNavHistory.setTranslationY(index == NAV_HISTORY ? textUp : 0f); tvNavHistory.setTextColor(colorHistory); }
        }
    }

    private void animateTextColor(TextView tv, int toColor) {
        ValueAnimator va = ValueAnimator.ofObject(
                new ArgbEvaluator(), tv.getCurrentTextColor(), toColor);
        va.setDuration(NAV_ANIM_MS);
        va.addUpdateListener(a -> tv.setTextColor((int) a.getAnimatedValue()));
        va.start();
    }

    // ── Listeners ─────────────────────────────────────────────────────────────
    private void setupListeners() {
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

        if (btnNavMakan != null) {
            btnNavMakan.setOnClickListener(v -> {
                if (currentNav == NAV_MAKAN) return;
                moveIndicatorTo(NAV_MAKAN, true);
                btnNavMakan.postDelayed(() -> {
                    Intent intent = new Intent(this, MakanActivity.class);
                    intent.putExtra("COUNTRY_CODE", countryCode);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }, NAV_ANIM_MS);
            });
        }

        if (btnNavHistory != null) {
            btnNavHistory.setOnClickListener(v -> moveIndicatorTo(NAV_HISTORY, true));
        }

        if (btnPeriod3   != null) btnPeriod3.setOnClickListener(v -> setPeriod(3));
        if (btnPeriod6   != null) btnPeriod6.setOnClickListener(v -> setPeriod(6));
        if (btnPeriodAll != null) btnPeriodAll.setOnClickListener(v -> setPeriod(0));
    }

    // ── Chart ─────────────────────────────────────────────────────────────────
    private void setupChart() {
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setExtraBottomOffset(8f);

        XAxis x = lineChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(Color.parseColor("#8D9994"));
        x.setTextSize(10f);
        x.setGranularity(1f);
        x.setDrawGridLines(true);
        x.setGridColor(Color.parseColor("#F0F4F2"));
        x.enableGridDashedLine(10f, 10f, 0f);

        YAxis y = lineChart.getAxisLeft();
        y.setTextColor(Color.parseColor("#8D9994"));
        y.setTextSize(10f);
        y.setAxisMinimum(14f);
        y.setAxisMaximum(32f);
        y.setLabelCount(4, false);
        y.setDrawGridLines(true);
        y.setGridColor(Color.parseColor("#F0F4F2"));
        y.enableGridDashedLine(10f, 10f, 0f);

        addLimitLine(y, 18.5f, HEX_LINE_185);
        addLimitLine(y, 25.0f, HEX_LINE_250);
        addLimitLine(y, 30.0f, HEX_LINE_300);

        lineChart.getAxisRight().setEnabled(false);
    }

    private void addLimitLine(YAxis axis, float val, String hex) {
        LimitLine ll = new LimitLine(val, "");
        ll.setLineColor(Color.parseColor(hex));
        ll.setLineWidth(1f);
        ll.enableDashedLine(6f, 4f, 0f);
        axis.addLimitLine(ll);
    }

    // ── Render ────────────────────────────────────────────────────────────────
    private void renderStats() {
        if (allData.isEmpty()) {
            if (tvHeaderBmiValue != null) tvHeaderBmiValue.setText("—");
            if (tvHeaderBmiRange != null) tvHeaderBmiRange.setVisibility(View.GONE);
            if (tvHeaderBmiMeta  != null) tvHeaderBmiMeta.setVisibility(View.GONE);
            return;
        }
        BmiEntity latest = allData.get(0);

        if (tvHeaderBmiValue != null)
            tvHeaderBmiValue.setText(String.format(Locale.US, "%.1f", latest.bmi));

        if (tvHeaderBmiRange != null) {
            tvHeaderBmiRange.setVisibility(View.VISIBLE);
            tvHeaderBmiRange.setText(rangeLabel(latest.bmi));
            tvHeaderBmiRange.setTextColor(Color.WHITE);
        }

        if (tvHeaderBmiMeta != null) {
            tvHeaderBmiMeta.setVisibility(View.VISIBLE);
            tvHeaderBmiMeta.setText(
                    (latest.date != null ? latest.date : "") + " • " + fmt(latest.weight) + " kg");
        }
    }

    private void renderChart(int period) {
        if (allData.isEmpty()) { lineChart.clear(); return; }

        List<BmiEntity> chrono = new ArrayList<>(allData);
        Collections.reverse(chrono);
        List<BmiEntity> slice = (period > 0 && period < chrono.size())
                ? chrono.subList(chrono.size() - period, chrono.size())
                : chrono;

        final List<String> labels  = new ArrayList<>();
        List<Entry>        entries = new ArrayList<>();
        List<Integer>      colors  = new ArrayList<>();

        for (int i = 0; i < slice.size(); i++) {
            BmiEntity item = slice.get(i);
            entries.add(new Entry(i, item.bmi));
            labels.add(item.date != null && item.date.length() >= 6
                    ? item.date.substring(0, 6) : String.valueOf(i + 1));
            colors.add(dotColor(item.bmi));
        }

        LineDataSet ds = new LineDataSet(entries, "BMI");
        ds.setColor(Color.parseColor(HEX_CHART_LINE));
        ds.setCircleColors(colors);
        ds.setCircleHoleColor(Color.WHITE);
        ds.setLineWidth(2f);
        ds.setCircleRadius(5f);
        ds.setCircleHoleRadius(2.5f);
        ds.setValueTextSize(10f);
        ds.setValueTextColor(Color.parseColor("#6B9B8E"));
        ds.setDrawFilled(true);
        ds.setFillColor(Color.parseColor(HEX_CHART_LINE));
        ds.setFillAlpha(18);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return String.format(Locale.US, "%.1f", v);
            }
        });

        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                int i = (int) v;
                return (i >= 0 && i < labels.size()) ? labels.get(i) : "";
            }
        });
        lineChart.getXAxis().setLabelCount(Math.min(slice.size(), 5), false);
        lineChart.setData(new LineData(ds));
        lineChart.animateX(600);
        lineChart.invalidate();
    }

    private void renderList() {
        int c = allData.size();
        if (tvCountBadge != null) {
            String format = getString(
                    c == 1 ? R.string.record_count_single : R.string.record_count_plural);
            tvCountBadge.setText(String.format(format, c));
        }

        if (layoutEmpty != null)
            layoutEmpty.setVisibility(allData.isEmpty() ? View.VISIBLE : View.GONE);

        adapter.setHistoryList(new ArrayList<>(allData), true);
    }

    // ── Period ────────────────────────────────────────────────────────────────
    private void setPeriod(int p) {
        activePeriod = p;
        updatePeriodBtn(btnPeriod3,   p == 3);
        updatePeriodBtn(btnPeriod6,   p == 6);
        updatePeriodBtn(btnPeriodAll, p == 0);
        renderChart(p);
    }

    private void updatePeriodBtn(TextView btn, boolean active) {
        if (btn == null) return;
        int padL = btn.getPaddingLeft();
        int padT = btn.getPaddingTop();
        int padR = btn.getPaddingRight();
        int padB = btn.getPaddingBottom();
        btn.setBackgroundResource(
                active ? R.drawable.bg_period_btn_active : R.drawable.bg_period_btn);
        btn.setTextColor(active
                ? Color.WHITE : Color.parseColor("#8D9994"));
        btn.setPadding(padL, padT, padR, padB);
    }

    // ── Item Actions ──────────────────────────────────────────────────────────
    private void onDeleteItem(BmiEntity item) {
        DeleteConfirmSheet s = new DeleteConfirmSheet(this, item, () -> {
            // ✅ Tidak perlu callback loadData() — LiveData update otomatis
            viewModel.deleteHistory(item);
        });
        s.show();
    }

    private void onDetailItem(BmiEntity item) {
        DetailSheet s = new DetailSheet(this, item);
        s.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String rangeLabel(float b) {
        if (b < 18.5f) return "< 18.5";
        if (b < 25.0f) return "18.5\u201325";
        if (b < 30.0f) return "25\u201330";
        return "> 30";
    }

    private int dotColor(float b) {
        if (b < 18.5f) return Color.parseColor(HEX_DOT_UNDER);
        if (b < 25.0f) return Color.parseColor(HEX_DOT_NORMAL);
        if (b < 30.0f) return Color.parseColor(HEX_DOT_OVER);
        return Color.parseColor(HEX_DOT_OBESE);
    }

    private String fmt(float v) {
        return v % 1 == 0 ? String.valueOf((int) v) : String.valueOf(v);
    }
}