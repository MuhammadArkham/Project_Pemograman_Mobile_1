package com.fitme.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private static final String PREF_NAME = "fitme_prefs";

    private static final String KEY_ONBOARDING_DONE    = "onboarding_done";
    private static final String KEY_USER_GENDER        = "user_gender";
    private static final String KEY_USER_AGE           = "user_age";
    private static final String KEY_USER_WEIGHT        = "user_weight";
    private static final String KEY_USER_HEIGHT        = "user_height";
    private static final String KEY_USER_BMI           = "user_bmi";
    private static final String KEY_BMI_CATEGORY       = "bmi_category";
    private static final String KEY_DAILY_CALORIE      = "daily_calorie_target";
    private static final String KEY_COUNTRY_CODE       = "country_code";
    private static final String KEY_DETECTED_CITY      = "detected_city";
    private static final String KEY_USER_NAME          = "user_name";
    private static final String KEY_PROFILE_PHOTO_PATH = "profile_photo_path";

    private final SharedPreferences prefs;

    public PrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ─── Onboarding ──────────────────────────────────────────────────────────

    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    // ─── Profil & Data Fisik ─────────────────────────────────────────────────

    public void saveUserProfile(boolean isMale, int age, float weight, float height) {
        prefs.edit()
                .putBoolean(KEY_USER_GENDER, isMale)
                .putInt(KEY_USER_AGE,        age)
                .putFloat(KEY_USER_WEIGHT,   weight)
                .putFloat(KEY_USER_HEIGHT,   height)
                .apply();
    }

    public boolean isMale()      { return prefs.getBoolean(KEY_USER_GENDER, true); }
    public int     getUserAge()  { return prefs.getInt(KEY_USER_AGE,        0);    }
    public float   getWeight()   { return prefs.getFloat(KEY_USER_WEIGHT,   0f);   }
    public float   getHeight()   { return prefs.getFloat(KEY_USER_HEIGHT,   0f);   }

    // ─── Hasil BMI ───────────────────────────────────────────────────────────

    public void saveBmiResult(float bmi, String category, int dailyCalorieTarget) {
        prefs.edit()
                .putFloat(KEY_USER_BMI,      bmi)
                .putString(KEY_BMI_CATEGORY, category)
                .putInt(KEY_DAILY_CALORIE,   dailyCalorieTarget)
                .apply();
    }

    public float  getLastBmi()            { return prefs.getFloat(KEY_USER_BMI,      0f);   }
    public String getBmiCategory()        { return prefs.getString(KEY_BMI_CATEGORY, "");   }
    public int    getDailyCalorieTarget() { return prefs.getInt(KEY_DAILY_CALORIE,   1800); }

    // ─── Data Personal (Nama & Foto) ─────────────────────────────────────────

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getProfilePhotoPath() {
        return prefs.getString(KEY_PROFILE_PHOTO_PATH, "");
    }

    public void setProfilePhotoPath(String path) {
        prefs.edit().putString(KEY_PROFILE_PHOTO_PATH, path).apply();
    }

    // ─── Lokasi & Bahasa ─────────────────────────────────────────────────────

    public void saveLocation(String countryCode, String city) {
        SharedPreferences.Editor editor = prefs.edit();
        // Hanya simpan jika countryCode benar-benar ada isinya
        if (countryCode != null && !countryCode.trim().isEmpty()) {
            editor.putString(KEY_COUNTRY_CODE, countryCode);
        }
        if (city != null) {
            editor.putString(KEY_DETECTED_CITY, city);
        }
        editor.apply();
    }

    public String getCountryCode()  { return prefs.getString(KEY_COUNTRY_CODE,  "ID"); }
    public String getDetectedCity() { return prefs.getString(KEY_DETECTED_CITY, "");   }

    // ─── Kalkulasi BMR/TDEE ──────────────────────────────────────────────────

    public static int calculateTDEE(boolean isMale, int age, float weightKg, float heightCm) {
        double bmr;
        if (isMale) {
            bmr = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0;
        } else {
            bmr = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0;
        }
        int tdee = (int) Math.round(bmr * 1.375);
        return Math.max(tdee, 1200);
    }

    // ─── Reset Data ──────────────────────────────────────────────────────────

    /**
     * FIX: Sebelumnya onboarding_done ikut diselamatkan → user tidak kena onboarding setelah reset.
     * Sekarang hanya country_code dan detected_city yang dipertahankan.
     * onboarding_done sengaja dibiarkan terhapus agar user kembali ke flow onboarding.
     */
    public void clearAll() {
        String currentCountry = getCountryCode();
        String currentCity    = getDetectedCity();

        prefs.edit()
                .clear()
                .putString(KEY_COUNTRY_CODE,  currentCountry != null && !currentCountry.isEmpty() ? currentCountry : "ID")
                .putString(KEY_DETECTED_CITY, currentCity != null ? currentCity : "")
                // KEY_ONBOARDING_DONE sengaja TIDAK dikembalikan → user wajib onboarding ulang
                .commit();
    }
}