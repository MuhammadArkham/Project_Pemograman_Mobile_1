package com.fitme.app.ui.profil;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fitme.app.database.BmiRepository;
import com.fitme.app.database.FoodRepository;
import com.fitme.app.utils.PrefManager;

public class ProfilViewModel extends AndroidViewModel {

    public static class ProfilUiState {
        public final boolean isMale;
        public final float   bmi;
        public final float   weight;
        public final float   height;
        public final int     age;
        public final int     tdee;
        public final String  name;
        public final String  profilePhotoPath;

        public ProfilUiState(boolean isMale, float bmi, float weight, float height,
                             int age, int tdee, String name, String profilePhotoPath) {
            this.isMale           = isMale;
            this.bmi              = bmi;
            this.weight           = weight;
            this.height           = height;
            this.age              = age;
            this.tdee             = tdee;
            this.name             = name;
            this.profilePhotoPath = profilePhotoPath;
        }
    }

    public static class BmiBadge {
        public final int bgColor;
        public final int fgColor;

        public BmiBadge(int bgColor, int fgColor) {
            this.bgColor = bgColor;
            this.fgColor = fgColor;
        }
    }

    private final PrefManager    pref;
    private final BmiRepository  bmiRepo;
    private final FoodRepository foodRepo;

    private final MutableLiveData<ProfilUiState> _uiState  = new MutableLiveData<>();
    public  final LiveData<ProfilUiState>        uiState   = _uiState;

    private final MutableLiveData<Boolean> _resetDone = new MutableLiveData<>();
    public  final LiveData<Boolean>        resetDone  = _resetDone;

    public ProfilViewModel(@NonNull Application application) {
        super(application);
        pref     = new PrefManager(application);
        bmiRepo  = new BmiRepository(application);
        foodRepo = new FoodRepository(application);
    }

    public void loadProfile() {
        _uiState.setValue(new ProfilUiState(
                pref.isMale(),
                pref.getLastBmi(),
                pref.getWeight(),
                pref.getHeight(),
                pref.getUserAge(),
                pref.getDailyCalorieTarget(),
                pref.getUserName(),
                pref.getProfilePhotoPath()
        ));
    }

    /** Simpan nama, lalu reload state supaya UI langsung update. */
    public void saveName(String name) {
        pref.setUserName(name.trim());
        loadProfile();
    }

    /** Simpan path foto profil (internal storage), lalu reload. */
    public void saveProfilePhotoPath(String path) {
        pref.setProfilePhotoPath(path);
        loadProfile();
    }

    /**
     * Reset semua data user.
     *
     * ✅ CATATAN PENTING:
     * Setelah clearAll() dipanggil, PrefManager tidak lagi punya countryCode.
     * ProfilActivity.resetDone observer akan menggunakan Locale.getDefault().getCountry()
     * sebagai fallback — JANGAN ambil pref.getCountryCode() setelah clearAll().
     */
    public void resetAllData() {
        pref.clearAll();
        bmiRepo.deleteAllBmi();
        foodRepo.deleteAllFood();
        _resetDone.postValue(true);
    }

    public BmiBadge getBmiBadge(float bmi) {
        if (bmi < 18.5f) return new BmiBadge(
                Color.parseColor("#E6F1FB"), Color.parseColor("#185FA5"));
        if (bmi < 25f)   return new BmiBadge(
                Color.parseColor("#E1F5EE"), Color.parseColor("#0F6E56"));
        if (bmi < 30f)   return new BmiBadge(
                Color.parseColor("#FAEEDA"), Color.parseColor("#854F0B"));
        return             new BmiBadge(
                Color.parseColor("#FCEBEB"), Color.parseColor("#A32D2D"));
    }

    public String fmtFloat(float v) {
        if (v <= 0) return "--";
        return (v % 1 == 0) ? String.valueOf((int) v) : String.valueOf(v);
    }
}