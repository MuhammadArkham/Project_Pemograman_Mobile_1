package com.fitme.app.ui.history;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fitme.app.database.BmiEntity;
import com.fitme.app.database.BmiRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final BmiRepository             repository;
    private final LiveData<List<BmiEntity>> historyLive;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository  = new BmiRepository(application);
        historyLive = repository.getAllBmiLive();
    }

    // ✅ Activity cukup observe sekali — Room push update otomatis
    public LiveData<List<BmiEntity>> getHistory() {
        return historyLive;
    }

    public void deleteHistory(BmiEntity entity) {
        repository.deleteBmi(entity);
        // LiveData akan otomatis update setelah delete — tidak perlu callback
    }
}