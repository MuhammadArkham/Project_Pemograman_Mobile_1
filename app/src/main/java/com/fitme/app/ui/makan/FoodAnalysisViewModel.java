package com.fitme.app.ui.makan;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fitme.app.api.NutritionService;
import com.fitme.app.database.FoodEntity;
import com.fitme.app.database.FoodRepository;
import com.fitme.app.utils.GeminiFoodHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodAnalysisViewModel extends AndroidViewModel {

    private final FoodRepository repository;

    private final MutableLiveData<Boolean>        _isLoading      = new MutableLiveData<>(false);
    private final MutableLiveData<String>         _loadingMessage = new MutableLiveData<>();
    private final MutableLiveData<String>         _errorMessage   = new MutableLiveData<>();
    private final MutableLiveData<Boolean>        _saveSuccess    = new MutableLiveData<>();
    private final MutableLiveData<AnalysisResult> _analysisResult = new MutableLiveData<>();

    public LiveData<Boolean>        getIsLoading()      { return _isLoading; }
    public LiveData<String>         getLoadingMessage() { return _loadingMessage; }
    public LiveData<String>         getErrorMessage()   { return _errorMessage; }
    public LiveData<Boolean>        getSaveSuccess()    { return _saveSuccess; }
    public LiveData<AnalysisResult> getAnalysisResult() { return _analysisResult; }

    public FoodAnalysisViewModel(@NonNull Application application) {
        super(application);
        repository = new FoodRepository(application);
    }

    public void startHybridAnalysis(android.graphics.Bitmap bitmap, String countryCode) {
        _isLoading.setValue(true);
        // ✅ Pakai getApplication().getString() — bukan resource ID mentah
        _loadingMessage.setValue(
                getApplication().getString(com.fitme.app.R.string.loading_detecting));

        GeminiFoodHelper.analyzeFood(bitmap, countryCode, new GeminiFoodHelper.GeminiCallback() {
            @Override
            public void onSuccess(String name, int calories, int protein, int carbs, int fat) {
                float fbCal = calories / 100f;
                float fbPro = protein  / 100f;
                float fbCar = carbs    / 100f;
                float fbFat = fat      / 100f;
                verifyWithDatabase(name, fbCal, fbPro, fbCar, fbFat);
            }

            @Override
            public void onError(String errorMsg) {
                _isLoading.setValue(false);
                _errorMessage.setValue(errorMsg);
            }
        });
    }

    private void verifyWithDatabase(String foodName,
                                    float fbCal, float fbPro,
                                    float fbCar, float fbFat) {
        _loadingMessage.setValue(
                getApplication().getString(com.fitme.app.R.string.loading_verifying));

        NutritionService.fetchNutrition(foodName, 100, new NutritionService.NutritionCallback() {
            @Override
            public void onSuccess(NutritionService.NutritionResult result) {
                _isLoading.setValue(false);
                _analysisResult.setValue(new AnalysisResult(
                        foodName,
                        result.calories / 100f,
                        result.protein  / 100f,
                        result.carbs    / 100f,
                        result.fat      / 100f,
                        true));
            }

            @Override
            public void onError(String errorMsg) {
                _isLoading.setValue(false);
                // Fallback ke data estimasi Gemini
                _analysisResult.setValue(new AnalysisResult(
                        foodName, fbCal, fbPro, fbCar, fbFat, false));
            }
        });
    }

    public void saveToDatabase(String foodName, int calories, int protein,
                               int carbs, int fat,
                               String weightStr, String imagePath) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        String now   = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());

        FoodEntity item = new FoodEntity();
        item.name      = foodName;
        item.emoji     = "🍽️";
        item.calories  = calories;
        item.protein   = protein;
        item.carbs     = carbs;
        item.fat       = fat;
        item.time      = now;
        item.imagePath = imagePath;
        item.portion   = weightStr + "g";
        item.note      = "Hybrid AI";
        item.date      = today;

        // ✅ Pakai Repository — bukan AppDatabase langsung
        repository.insertFood(item);
        _saveSuccess.postValue(true);
    }

    // ─── Model ───────────────────────────────────────────────────────────────

    public static class AnalysisResult {
        public final String  name;
        public final float   calPerGram;
        public final float   proPerGram;
        public final float   carPerGram;
        public final float   fatPerGram;
        public final boolean verified;

        public AnalysisResult(String name, float cal, float pro,
                              float car, float fat, boolean verified) {
            this.name       = name;
            this.calPerGram = cal;
            this.proPerGram = pro;
            this.carPerGram = car;
            this.fatPerGram = fat;
            this.verified   = verified;
        }
    }
}