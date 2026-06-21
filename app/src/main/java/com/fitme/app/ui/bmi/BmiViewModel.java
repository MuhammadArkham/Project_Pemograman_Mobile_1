package com.fitme.app.ui.bmi;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fitme.app.R;
import com.fitme.app.database.BmiEntity;
import com.fitme.app.database.BmiRepository;
import com.fitme.app.utils.GroqHelper;
import com.fitme.app.utils.PrefManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BmiViewModel extends AndroidViewModel {

    private final BmiRepository repository;

    private final MutableLiveData<AiResultState> aiAdviceState = new MutableLiveData<>();

    public BmiViewModel(@NonNull Application application) {
        super(application);
        repository = new BmiRepository(application);
    }

    public LiveData<AiResultState> getAiAdviceState() {
        return aiAdviceState;
    }

    public float getIdealMin(float height) {
        return 18.5f * (height / 100f) * (height / 100f);
    }

    public float getIdealMax(float height) {
        return 25.0f * (height / 100f) * (height / 100f);
    }

    public float calculateBarFraction(float bmi) {
        float f;
        if (bmi <= 10f) f = 0.02f;
        else if (bmi < 18.5f) f = ((bmi - 10f) / 8.5f) * 0.25f;
        else if (bmi < 25.0f) f = 0.25f + ((bmi - 18.5f) / 6.5f) * 0.25f;
        else if (bmi < 30.0f) f = 0.50f + ((bmi - 25.0f) / 5.0f) * 0.25f;
        else f = 0.75f + Math.min(1f, (bmi - 30f) / 10f) * 0.25f;
        return Math.max(0.02f, Math.min(0.97f, f));
    }

    public void fetchAiAdvice(float bmi, String category, int age, boolean isMale, float height, String countryCode, String city) {
        float min = getIdealMin(height);
        float max = getIdealMax(height);

        String extraPrompt = String.format(Locale.US,
                " User height is %.1f cm. Calculate and mention their ideal weight range " +
                        "(between %.1f kg and %.1f kg) in your advice.", height, min, max);

        GroqHelper.getHealthAdvice(bmi, category + extraPrompt, String.valueOf(age),
                isMale ? "male" : "female", countryCode, city, new GroqHelper.GroqCallback() {
                    @Override
                    public void onSuccess(String advice) {
                        aiAdviceState.postValue(new AiResultState(true, advice));
                    }

                    @Override
                    public void onError(String error) {
                        String offlineTips = generateOfflineTips(bmi, min, max);
                        aiAdviceState.postValue(new AiResultState(false, offlineTips));
                    }
                });
    }

    private String generateOfflineTips(float bmi, float min, float max) {
        Context context = getApplication().getApplicationContext();

        if (bmi < 18.5f) {
            return context.getString(R.string.offline_tip_underweight, min, max);
        } else if (bmi < 25.0f) {
            return context.getString(R.string.offline_tip_normal, min, max);
        } else if (bmi < 30.0f) {
            return context.getString(R.string.offline_tip_overweight, min, max);
        } else {
            return context.getString(R.string.offline_tip_obese, min, max);
        }
    }

    // ✅ PERBAIKAN: Parameter Context Dihapus
    public void saveAllData(float bmi, String category, int age, float weight, float height, boolean isMale, String countryCode, String city, String genderString) {
        // ✅ Menggunakan getApplication()
        PrefManager pref = new PrefManager(getApplication());
        pref.saveUserProfile(isMale, age, weight, height);
        pref.saveBmiResult(bmi, category, PrefManager.calculateTDEE(isMale, age, weight, height));
        pref.saveLocation(countryCode, city);
        pref.setOnboardingDone(true);

        try {
            BmiEntity entity = new BmiEntity();
            entity.bmi = bmi;
            entity.category = category;
            entity.age = String.valueOf(age);
            entity.weight = weight;
            entity.height = height;
            entity.gender = genderString;
            entity.date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

            repository.insert(entity);
        } catch (Exception ignored) {}
    }

    public static class AiResultState {
        public final boolean isOnline;
        public final String content;

        public AiResultState(boolean isOnline, String content) {
            this.isOnline = isOnline;
            this.content = content;
        }
    }
}