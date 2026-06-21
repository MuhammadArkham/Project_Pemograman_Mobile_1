package com.fitme.app.database;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BmiRepository {

    private final BmiDao         dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BmiRepository(Application app) {
        dao = AppDatabase.getInstance(app).bmiDao();
    }

    // ✅ Reaktif — dipakai HistoryViewModel & HomeViewModel
    public LiveData<List<BmiEntity>> getAllBmiLive() {
        return dao.getAllBmiLive();
    }

    // ✅ Background thread + callback — dipakai ProfilViewModel.resetAllData()
    public void getAllBmiDesc(OnDataCallback<List<BmiEntity>> callback) {
        executor.execute(() -> {
            List<BmiEntity> data = dao.getAllBmiDescSync();
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(data));
        });
    }

    public void insert(BmiEntity entity) {
        executor.execute(() -> dao.insertBmi(entity));
    }

    public void deleteBmi(BmiEntity entity) {
        executor.execute(() -> dao.deleteBmi(entity));
    }

    public void deleteAllBmi() {
        executor.execute(dao::deleteAllBmi);
    }

    public interface OnDataCallback<T> {
        void onResult(T data);
    }
}