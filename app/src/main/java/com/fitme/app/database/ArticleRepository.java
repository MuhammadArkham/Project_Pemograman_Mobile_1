package com.fitme.app.database;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.fitme.app.api.ArticleService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ArticleRepository — Cache-first strategy.
 *
 * Alur:
 * 1. Tampilkan cache dari Room DB (langsung via LiveData)
 * 2. Fetch artikel baru dari network di background
 * 3. Jika fetch sukses → update DB → LiveData auto-update UI
 * 4. Jika fetch gagal → cache tetap ada, tidak dihapus
 *
 * Thread safety:
 * - DAO insert dilakukan dari ExecutorService (background thread)
 * - LiveData observe dari main thread (aman)
 */
public class ArticleRepository {

    private final ArticleDao     dao;
    private final ArticleService service;
    private final ExecutorService dbExecutor;
    private final android.content.SharedPreferences prefs;

    public ArticleRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dao       = db.articleDao();
        this.service   = new ArticleService();
        this.dbExecutor = Executors.newSingleThreadExecutor();
        this.prefs     = context.getSharedPreferences("fitme_prefs", Context.MODE_PRIVATE);
    }

    /**
     * Ambil semua artikel dari Room sebagai LiveData.
     * Dipanggil sekali, observer auto-update saat DB berubah.
     */
    public LiveData<List<ArticleEntity>> getAllArticlesLive() {
        return dao.getAllArticles();
    }

    /**
     * Fetch CEPAT — hanya RSS feed dengan gambar, tanpa OG scraping.
     * ~5-10 detik. Dipakai Home agar artikel muncul lebih cepat.
     */
    public void refreshArticlesQuick(RefreshCallback callback) {
        refreshArticles(callback); // Redirect to full refresh since we use single source now
    }

    /**
     * Fetch dari network. Update SharedPreferences timestamp.
     */
    public void refreshArticles(RefreshCallback callback) {
        service.fetchHealthArticles(new ArticleService.ArticleCallback() {
            @Override
            public void onSuccess(List<ArticleEntity> articles) {
                if (dbExecutor.isShutdown() || dbExecutor.isTerminated()) return;
                dbExecutor.execute(() -> {
                    try {
                        if (articles != null && !articles.isEmpty()) {
                            // Hapus seluruh cache lama HANYA JIKA ada artikel baru yang valid
                            dao.deleteAllUnbookmarkedArticles();
                            
                            // Simpan artikel baru yang sudah tersaring ketat
                            dao.insertArticles(articles);
                            prefs.edit().putLong("last_article_sync", System.currentTimeMillis()).apply();
                        }
                        if (callback != null) callback.onSuccess(articles != null ? articles.size() : 0);
                    } catch (Exception e) {
                        if (callback != null) callback.onError("DB error: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onError(message);
            }
        });
    }

    /**
     * Cek apakah cache ada DAN umurnya kurang dari 3 jam.
     */
    public void checkCacheAndFetchIfNeeded(RefreshCallback callback) {
        dbExecutor.execute(() -> {
            try {
                int count = dao.getArticleCount();
                // FORCED REFRESH: Agar perubahan logic (Jsoup & Smart Image) langsung terasa oleh pengguna
                // Mengabaikan limit 3 jam untuk sementara
                long lastSync = prefs.getLong("last_article_sync", 0);
                long threeHours = 3 * 3600 * 1000L;

                // Memaksa aplikasi untuk selalu mencoba fetch ulang di background setiap kali dibuka
                if (count == 0 || true) {
                    // Jika ya (atau kosong), fetch ulang dari ArticleService
                    refreshArticles(callback);
                } else {
                    // Cache masih valid (kurang dari 3 jam)
                    if (callback != null) callback.onSuccess(count);
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Cek apakah ada artikel di DB.
     * SAFE: jika dbExecutor sudah shut down, langsung return false (tidak crash).
     */
    public void hasCachedArticles(CacheCheckCallback callback) {
        if (callback == null) return;
        if (dbExecutor == null || dbExecutor.isShutdown() || dbExecutor.isTerminated()) {
            callback.onResult(false);
            return;
        }
        try {
            dbExecutor.execute(() -> {
                try {
                    int count = dao.getArticleCount();
                    callback.onResult(count > 0);
                } catch (Exception e) {
                    callback.onResult(false);
                }
            });
        } catch (Exception e) {
            // Jika executor baru saja shut down di antara pengecekan dan submit
            callback.onResult(false);
        }
    }

    public void shutdown() {
        if (service != null) {
            service.shutdown();
        }
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
        }
    }

    public LiveData<List<ArticleEntity>> getBookmarkedArticlesLive() {
        return dao.getBookmarkedArticles();
    }

    public LiveData<List<ArticleEntity>> searchArticlesLive(String query) {
        return dao.searchArticles(query);
    }

    public void toggleBookmark(ArticleEntity article) {
        dbExecutor.execute(() -> {
            article.isBookmarked = !article.isBookmarked;
            dao.updateArticle(article);
        });
    }

    public interface RefreshCallback {
        void onSuccess(int count);
        void onError(String message);
    }

    public interface CacheCheckCallback {
        void onResult(boolean hasCached);
    }
}