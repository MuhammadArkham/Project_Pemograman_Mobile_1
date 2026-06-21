package com.fitme.app.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fitme.app.api.ArticleService;
import com.fitme.app.database.AppDatabase;
import com.fitme.app.database.ArticleDao;
import com.fitme.app.database.ArticleEntity;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ArticleSyncWorker extends Worker {

    public ArticleSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("ArticleSyncWorker", "Starting background article sync...");
        
        ArticleService service = new ArticleService();
        ArticleDao dao = AppDatabase.getInstance(getApplicationContext()).articleDao();
        
        // Use a CountDownLatch to wait for the async fetch to complete
        CountDownLatch latch = new CountDownLatch(1);
        final Result[] result = {Result.failure()};
        
        service.fetchHealthArticles(new ArticleService.ArticleCallback() {
            @Override
            public void onSuccess(List<ArticleEntity> articles) {
                if (articles != null && !articles.isEmpty()) {
                    // Update the database with new fetched articles
                    dao.insertArticles(articles);
                    Log.d("ArticleSyncWorker", "Successfully synced " + articles.size() + " articles.");
                    result[0] = Result.success();
                } else {
                    Log.d("ArticleSyncWorker", "Sync successful but no articles returned.");
                    result[0] = Result.success();
                }
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                Log.e("ArticleSyncWorker", "Failed to sync articles: " + message);
                result[0] = Result.retry();
                latch.countDown();
            }
        });
        
        try {
            // Wait up to 30 seconds for the fetch to complete
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("ArticleSyncWorker", "Sync interrupted", e);
            return Result.failure();
        } finally {
            service.shutdown();
        }
        
        return result[0];
    }
}
