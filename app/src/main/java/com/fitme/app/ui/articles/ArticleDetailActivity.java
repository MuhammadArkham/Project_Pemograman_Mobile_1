package com.fitme.app.ui.articles;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitme.app.R;
import com.fitme.app.database.AppDatabase;
import com.fitme.app.database.ArticleDao;
import com.fitme.app.database.ArticleEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArticleDetailActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";

    private WebView webView;
    private ProgressBar progressBar;
    private ImageButton btnBack, btnShare, btnBookmark;
    private TextView tvTitle;

    private String currentUrl;
    private String currentTitle;
    private boolean isBookmarked = false;
    private ArticleEntity currentArticle;

    private ExecutorService executor;
    private ArticleDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setStatusBarColor(Color.WHITE);
        //noinspection deprecation
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        
        setContentView(R.layout.activity_article_detail);

        currentUrl = getIntent().getStringExtra(EXTRA_URL);
        currentTitle = getIntent().getStringExtra(EXTRA_TITLE);

        if (currentUrl == null || currentUrl.isEmpty()) {
            Toast.makeText(this, "URL tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);
        btnShare = findViewById(R.id.btn_share);
        btnBookmark = findViewById(R.id.btn_bookmark);
        tvTitle = findViewById(R.id.tv_toolbar_title);

        tvTitle.setText(currentTitle != null ? currentTitle : "");

        btnBack.setOnClickListener(v -> finish());
        
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentTitle);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Cek artikel kesehatan ini: " + currentUrl);
            startActivity(Intent.createChooser(shareIntent, "Bagikan via"));
        });

        setupWebView();
        setupDatabaseAndBookmark();
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });

        webView.loadUrl(currentUrl);
    }

    private void setupDatabaseAndBookmark() {
        dao = AppDatabase.getInstance(this).articleDao();
        executor = Executors.newSingleThreadExecutor();

        // Check if article is bookmarked
        executor.execute(() -> {
            try {
                currentArticle = dao.getArticleByUrl(currentUrl);
                if (currentArticle != null) {
                    isBookmarked = currentArticle.isBookmarked;
                    runOnUiThread(this::updateBookmarkIcon);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updateBookmarkIcon();
            Toast.makeText(this, isBookmarked ? "Disimpan ke Bookmark" : "Dihapus dari Bookmark", Toast.LENGTH_SHORT).show();
            
            executor.execute(() -> {
                if (currentArticle != null) {
                    currentArticle.isBookmarked = isBookmarked;
                    dao.updateArticle(currentArticle);
                }
            });
        });
    }

    private void updateBookmarkIcon() {
        btnBookmark.setImageResource(isBookmarked ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        btnBookmark.setColorFilter(isBookmarked ? Color.parseColor("#E74C3C") : Color.parseColor("#0F6E56"));
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        super.onDestroy();
    }
}
