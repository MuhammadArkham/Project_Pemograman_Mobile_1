package com.fitme.app;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.fitme.app.utils.LocaleHelper;
import com.fitme.app.utils.PrefManager;
import com.fitme.app.workers.ArticleRefreshWorker;

import java.util.concurrent.TimeUnit;

public class FitMeApp extends Application {

    private static final String WORK_ARTICLE_REFRESH = "fitme_article_refresh";

    @Override
    public void onCreate() {
        super.onCreate();

        // Set locale otomatis saat app pertama kali start
        PrefManager pref = new PrefManager(this);
        String countryCode = pref.getCountryCode();
        LocaleHelper.setLocale(this, countryCode);

        // Jadwalkan background refresh artikel setiap 1 jam
        scheduleArticleRefresh();
    }

    /**
     * Mendaftarkan WorkManager untuk refresh artikel setiap 1 jam.
     * - KEEP: jika sudah ada jadwal, tidak di-override
     * - Hanya berjalan saat ada koneksi internet
     * - Tidak crash jika gagal
     */
    private void scheduleArticleRefresh() {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest refreshWork = new PeriodicWorkRequest.Builder(
                    ArticleRefreshWorker.class,
                    1, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .setInitialDelay(15, TimeUnit.MINUTES) // Delay awal 15 menit setelah install
                    .build();

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    WORK_ARTICLE_REFRESH,
                    ExistingPeriodicWorkPolicy.KEEP, // Jangan ganti yang sudah ada
                    refreshWork);

            Log.d("FitMeApp", "Article refresh worker scheduled (every 1 hour)");
        } catch (Exception e) {
            Log.e("FitMeApp", "Failed to schedule worker: " + e.getMessage());
        }
    }
}