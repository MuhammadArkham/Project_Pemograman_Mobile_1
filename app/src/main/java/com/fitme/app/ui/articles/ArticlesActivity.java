package com.fitme.app.ui.articles;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fitme.app.R;
import com.fitme.app.database.ArticleEntity;
import com.google.android.material.button.MaterialButton;

/**
 * ArticlesActivity — Halaman daftar artikel kesehatan.
 *
 * States:
 *  LOADING  → layout_loading (skeleton) tampil, swipe_refresh GONE
 *  SUCCESS  → swipe_refresh + rv_articles VISIBLE
 *  EMPTY    → layout_error VISIBLE dengan pesan kosong
 *  ERROR    → layout_error VISIBLE dengan pesan error + tombol retry
 */
public class ArticlesActivity extends AppCompatActivity {

    // ─── Kategori ─────────────────────────────────────────────────────────────
    private static final String CAT_ALL       = "All";
    private static final String CAT_NUTRISI   = "Nutrisi";
    private static final String CAT_OLAHRAGA  = "Olahraga";
    private static final String CAT_DIET      = "Diet";
    private static final String CAT_KESEHATAN = "Kesehatan";
    private static final String CAT_GAYAHIDUP = "Gaya Hidup";
    private static final String CAT_TERSIMPAN = "Tersimpan";

    // ─── Views ────────────────────────────────────────────────────────────────
    private RecyclerView       rvArticles;
    private SwipeRefreshLayout swipeRefresh;
    private EditText           etSearch;
    private LinearLayout       layoutError;
    private LinearLayout       layoutLoading;
    private TextView           tvErrorMsg;
    private ImageButton        btnBack;
    private MaterialButton     btnRetry;

    private MaterialButton chipSemua, chipNutrisi, chipOlahraga,
                           chipDiet, chipKesehatan, chipGayaHidup, chipTersimpan;

    // ─── State ────────────────────────────────────────────────────────────────
    private ArticlesViewModel viewModel;
    private ArticleAdapter    adapter;
    private String            currentQuery    = "";
    private String            currentCategory = CAT_ALL;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //noinspection deprecation
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_articles);

        initViews();
        setupRecyclerView();
        setupSearch();
        setupChips();

        viewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
        observeViewModel();

        // Navigasi
        if (btnBack   != null) btnBack.setOnClickListener(v -> finish());
        if (btnRetry  != null) btnRetry.setOnClickListener(v -> viewModel.refreshArticles());
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> viewModel.refreshArticles());
            swipeRefresh.setColorSchemeResources(R.color.fitme_green);
        }

        // Default chip aktif
        activateChip(chipSemua);

        // Kategori dari intent (tombol "Lihat Semua" di Home)
        if (getIntent() != null && getIntent().hasExtra("FILTER_CATEGORY")) {
            String cat = getIntent().getStringExtra("FILTER_CATEGORY");
            if (cat != null && !cat.isEmpty()) selectChip(cat);
        }
    }

    // ─── Init Views ───────────────────────────────────────────────────────────
    private void initViews() {
        rvArticles    = findViewById(R.id.rv_articles);
        swipeRefresh  = findViewById(R.id.swipe_refresh);
        etSearch      = findViewById(R.id.et_search);
        layoutError   = findViewById(R.id.layout_error);
        layoutLoading = findViewById(R.id.layout_loading);
        tvErrorMsg    = findViewById(R.id.tv_error_msg);
        btnBack       = findViewById(R.id.btn_back_articles);
        btnRetry      = findViewById(R.id.btn_retry);

        chipSemua     = findViewById(R.id.chip_semua);
        chipNutrisi   = findViewById(R.id.chip_nutrisi);
        chipOlahraga  = findViewById(R.id.chip_olahraga);
        chipDiet      = findViewById(R.id.chip_diet);
        chipKesehatan = findViewById(R.id.chip_kesehatan);
        chipGayaHidup = findViewById(R.id.chip_gaya_hidup);
        chipTersimpan = findViewById(R.id.chip_tersimpan);
    }

    private void setupRecyclerView() {
        if (rvArticles == null) return;
        adapter = new ArticleAdapter(this, ArticleAdapter.VIEW_TYPE_VERTICAL, new ArticleAdapter.OnArticleClickListener() {
            @Override
            public void onArticleClick(ArticleEntity article) {
                if (article.link == null || article.link.isEmpty()) return;
                try {
                    Intent intent = new Intent(ArticlesActivity.this, ArticleDetailActivity.class);
                    intent.putExtra(ArticleDetailActivity.EXTRA_URL, article.link);
                    intent.putExtra(ArticleDetailActivity.EXTRA_TITLE, article.title != null ? article.title : "");
                    startActivity(intent);
                } catch (Exception ignored) {}
            }

            @Override
            public void onBookmarkClick(ArticleEntity article) {
                if (viewModel != null) viewModel.toggleBookmark(article);
            }
        });
        adapter.setFilterCallback(count -> updateEmptyState());
        rvArticles.setLayoutManager(new LinearLayoutManager(this));
        rvArticles.setAdapter(adapter);
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString();
                if (adapter != null) {
                    adapter.filter(currentQuery, currentCategory);
                }
            }
        });
    }

    // ─── Chips ────────────────────────────────────────────────────────────────
    private void setupChips() {
        if (chipSemua     != null) chipSemua.setOnClickListener(v     -> selectChip(CAT_ALL));
        if (chipNutrisi   != null) chipNutrisi.setOnClickListener(v   -> selectChip(CAT_NUTRISI));
        if (chipOlahraga  != null) chipOlahraga.setOnClickListener(v  -> selectChip(CAT_OLAHRAGA));
        if (chipDiet      != null) chipDiet.setOnClickListener(v      -> selectChip(CAT_DIET));
        if (chipKesehatan != null) chipKesehatan.setOnClickListener(v -> selectChip(CAT_KESEHATAN));
        if (chipGayaHidup != null) chipGayaHidup.setOnClickListener(v -> selectChip(CAT_GAYAHIDUP));
        if (chipTersimpan != null) chipTersimpan.setOnClickListener(v -> selectChip(CAT_TERSIMPAN));
    }

    private void selectChip(String category) {
        currentCategory = category;

        // Reset semua
        deactivateChip(chipSemua);
        deactivateChip(chipNutrisi);
        deactivateChip(chipOlahraga);
        deactivateChip(chipDiet);
        deactivateChip(chipKesehatan);
        deactivateChip(chipGayaHidup);
        deactivateChip(chipTersimpan);

        // Aktifkan yang dipilih
        if      (CAT_ALL.equalsIgnoreCase(category) || "Semua".equalsIgnoreCase(category))
            activateChip(chipSemua);
        else if (CAT_NUTRISI.equalsIgnoreCase(category))   activateChip(chipNutrisi);
        else if (CAT_OLAHRAGA.equalsIgnoreCase(category))  activateChip(chipOlahraga);
        else if (CAT_DIET.equalsIgnoreCase(category))      activateChip(chipDiet);
        else if (CAT_KESEHATAN.equalsIgnoreCase(category)) activateChip(chipKesehatan);
        else if (CAT_GAYAHIDUP.equalsIgnoreCase(category)) activateChip(chipGayaHidup);
        else if (CAT_TERSIMPAN.equalsIgnoreCase(category)) activateChip(chipTersimpan);
        else activateChip(chipSemua);

        if (adapter != null) {
            adapter.filter(currentQuery, currentCategory);
        }
    }

    private void activateChip(MaterialButton chip) {
        if (chip == null) return;
        chip.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.fitme_green));
        chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        chip.setStrokeWidth(0);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void deactivateChip(MaterialButton chip) {
        if (chip == null) return;
        chip.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.fitme_surface));
        chip.setTextColor(ContextCompat.getColor(this, R.color.fitme_green));
        chip.setStrokeColor(ContextCompat.getColorStateList(this, R.color.fitme_green_border));
        chip.setStrokeWidth(dpToPx(1));
        chip.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    // ─── Observe ──────────────────────────────────────────────────────────────
    private void observeViewModel() {
        // Artikel dari Room DB — auto-update saat cache berubah
        viewModel.articlesLive.observe(this, articles -> {
            if (articles != null && adapter != null) {
                adapter.setArticles(articles);
            }
        });

        // State machine
        viewModel.getState().observe(this, state -> {
            if (state == null) return;
            switch (state) {
                case LOADING:
                    showLoading();
                    break;
                case SUCCESS:
                    showContent();
                    updateEmptyState();
                    break;
                case EMPTY:
                    showEmpty("Belum ada artikel kesehatan.\nTarik ke bawah untuk memuat.");
                    break;
                case ERROR:
                    showError();
                    break;
                case REFRESHING:
                    // SwipeRefresh dihandle oleh swipeLive
                    break;
            }
        });

        // SwipeRefresh spinner
        viewModel.getSwipeAnim().observe(this, refreshing -> {
            if (swipeRefresh != null)
                swipeRefresh.setRefreshing(Boolean.TRUE.equals(refreshing));
        });

        // Error message
        viewModel.getError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty() && tvErrorMsg != null)
                tvErrorMsg.setText(msg);
        });
    }

    // ─── State Visibility ─────────────────────────────────────────────────────
    /** State: LOADING — tampilkan skeleton, sembunyikan konten */
    private void showLoading() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
        if (swipeRefresh  != null) swipeRefresh.setVisibility(View.GONE);
        if (layoutError   != null) layoutError.setVisibility(View.GONE);
    }

    /** State: SUCCESS — tampilkan konten, sembunyikan skeleton */
    private void showContent() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (swipeRefresh  != null) swipeRefresh.setVisibility(View.VISIBLE);
        if (layoutError   != null) layoutError.setVisibility(View.GONE);
    }

    /** State: EMPTY — tampilkan pesan kosong */
    private void showEmpty(String message) {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (swipeRefresh  != null) swipeRefresh.setVisibility(View.GONE);
        if (layoutError   != null) {
            layoutError.setVisibility(View.VISIBLE);
            if (tvErrorMsg != null) tvErrorMsg.setText(message);
            if (btnRetry   != null) btnRetry.setVisibility(View.VISIBLE);
        }
    }

    /** State: ERROR — tampilkan pesan error + tombol retry */
    private void showError() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (swipeRefresh  != null) swipeRefresh.setVisibility(View.GONE);
        if (layoutError   != null) {
            layoutError.setVisibility(View.VISIBLE);
            if (tvErrorMsg != null)
                tvErrorMsg.setText("Gagal memuat artikel.\nPeriksa koneksi internet Anda.");
            if (btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
        }
    }

    /** Update empty state saat filter aktif menghasilkan 0 hasil */
    private void updateEmptyState() {
        if (adapter == null || swipeRefresh == null) return;
        boolean hasItems = adapter.getItemCount() > 0;

        if (swipeRefresh.getVisibility() != View.VISIBLE) return; // Jangan override state lain

        if (!hasItems && layoutError != null) {
            layoutError.setVisibility(View.VISIBLE);
            if (tvErrorMsg != null) {
                boolean isDefault = currentQuery.isEmpty()
                        && (CAT_ALL.equalsIgnoreCase(currentCategory)
                            || "Semua".equalsIgnoreCase(currentCategory));
                tvErrorMsg.setText(isDefault
                        ? "Artikel sedang dimuat...\nTarik ke bawah untuk refresh."
                        : "Tidak ada artikel untuk kategori \"" + currentCategory + "\".");
            }
            if (btnRetry != null) btnRetry.setVisibility(View.GONE);
        } else if (layoutError != null) {
            layoutError.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}