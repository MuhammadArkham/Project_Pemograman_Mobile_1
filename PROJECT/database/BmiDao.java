package com.fitme.app.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BmiDao {
    @Insert
    void insertBmi(BmiEntity bmiEntity); // Nama harus insertBmi

    @Query("SELECT * FROM bmi_table ORDER BY id DESC")
    List<BmiEntity> getAllBmiHistory();

    // Tambahan untuk hapus (jika nanti butuh)
    @androidx.room.Delete
    void delete(BmiEntity bmiEntity);
}