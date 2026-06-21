package com.fitme.app.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fitme.app.api.ArticleService;
import com.fitme.app.database.AppDatabase;
import com.fitme.app.database.ArticleEntity;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * ArticleRefreshWorker — Background refresh artikel setiap 1 jam.
 *
 * Dipanggil oleh WorkManager. Fetch artikel terbaru dari RSS,
 * simpan ke Room DB. Gagal silently (tidak crash app).
 */
public class ArticleRefreshWorker extends Worker {

    private static final String TAG = "ArticleRefreshWorker";

    public ArticleRefreshWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting background article refresh...");
        ArticleService service = new ArticleService();
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        try {
            service.fetchHealthArticles(new ArticleService.ArticleCallback() {
                @Override
                public void onSuccess(List<ArticleEntity> articles) {
                    try {
                        // Insert langsung ke Room (sudah di background thread)
                        AppDatabase.getInstance(getApplicationContext())
                                .articleDao()
                                .insertArticles(articles);
                        success[0] = true;
                        Log.d(TAG, "Refreshed " + articles.size() + " articles");
                    } catch (Exception e) {
                        Log.e(TAG, "DB insert error: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onError(String message) {
                    Log.w(TAG, "Fetch error: " + message);
                    latch.countDown();
                }
            });

            // Tunggu fetch selesai (max 60 detik)
            latch.await(60, java.util.concurrent.TimeUnit.SECONDS);

        } catch (Exception e) {
            Log.e(TAG, "Worker exception: " + e.getMessage());
        } finally {
            service.shutdown();
        }

        return success[0] ? Result.success() : Result.retry();
    }
}
