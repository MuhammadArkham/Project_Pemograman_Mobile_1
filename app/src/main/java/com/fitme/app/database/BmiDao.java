package com.fitme.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BmiDao {

    @Insert
    void insertBmi(BmiEntity bmi);

    // ✅ Reaktif — Room handle threading otomatis, HistoryViewModel observe ini
    @Query("SELECT * FROM bmi_table ORDER BY id DESC")
    LiveData<List<BmiEntity>> getAllBmiLive();

    // One-shot — untuk ProfilViewModel reset & delete operasi
    @Query("SELECT * FROM bmi_table ORDER BY id DESC")
    List<BmiEntity> getAllBmiDescSync();

    @Delete
    void deleteBmi(BmiEntity bmi);

    @Query("DELETE FROM bmi_table")
    void deleteAllBmi();
}