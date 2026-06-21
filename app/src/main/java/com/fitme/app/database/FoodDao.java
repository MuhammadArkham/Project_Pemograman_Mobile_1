package com.fitme.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodDao {

    @Insert
    void insertFood(FoodEntity entity);

    // ✅ Reaktif — MakanActivity observe ini, otomatis update saat data berubah
    @Query("SELECT * FROM food_log WHERE date = :date ORDER BY id DESC")
    LiveData<List<FoodEntity>> getFoodByDateLive(String date);

    @Query("DELETE FROM food_log WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM food_log")
    void deleteAllFood();
}