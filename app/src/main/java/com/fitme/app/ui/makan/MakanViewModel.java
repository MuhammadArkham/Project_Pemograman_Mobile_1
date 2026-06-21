package com.fitme.app.ui.makan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fitme.app.database.FoodEntity;
import com.fitme.app.database.FoodRepository;

import java.util.List;

public class MakanViewModel extends AndroidViewModel {

    private final FoodRepository repository;

    public MakanViewModel(@NonNull Application application) {
        super(application);
        repository = new FoodRepository(application);
    }

    // ✅ Reaktif — MakanActivity observe ini, update otomatis saat data berubah
    public LiveData<List<FoodEntity>> getFoodByDate(String date) {
        return repository.getFoodByDateLive(date);
    }

    public void insertFood(FoodEntity entity) {
        repository.insertFood(entity);
    }

    public void deleteFood(long id) {
        repository.deleteFood(id);
        // LiveData otomatis push update ke observer — tidak perlu callback Runnable
    }
}