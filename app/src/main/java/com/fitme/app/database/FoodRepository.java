package com.fitme.app.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodRepository {

    private final FoodDao        foodDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FoodRepository(Application app) {
        foodDao = AppDatabase.getInstance(app).foodDao();
    }

    // ✅ Reaktif — dipakai MakanViewModel
    public LiveData<List<FoodEntity>> getFoodByDateLive(String date) {
        return foodDao.getFoodByDateLive(date);
    }

    public void insertFood(FoodEntity entity) {
        executor.execute(() -> foodDao.insertFood(entity));
    }

    public void deleteFood(long id) {
        executor.execute(() -> foodDao.deleteById(id));
    }

    public void deleteAllFood() {
        executor.execute(foodDao::deleteAllFood);
    }
}