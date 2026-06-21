package com.fitme.app.ui.splash;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fitme.app.utils.LocationHelper;

public class SplashViewModel extends AndroidViewModel {

    private final MutableLiveData<SplashState> uiState = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<SplashState> getUiState() {
        return uiState;
    }

    public void startSplashSequence(boolean hasPermission, android.content.Context context) {
        if (!hasPermission) {
            finalizeSplash(buildData("ID", "", "Indonesia"), "ID");
            return;
        }

        LocationHelper helper = new LocationHelper(context);
        helper.getCurrentLocation(result -> {
            if (result != null) {
                String code        = result.countryCode  != null ? result.countryCode.toUpperCase()  : "ID";
                String city        = result.specificLocation != null ? result.specificLocation : "";
                String countryName = result.countryName  != null ? result.countryName  : code;
                finalizeSplash(buildData(code, city, countryName), code);
            } else {
                finalizeSplash(buildData("ID", "", "Indonesia"), "ID");
            }
        });
    }

    /**
     * Buat array data splash berdasarkan country code.
     * @param code        ISO 3166-1 alpha-2, e.g. "ID", "GB", "US"
     * @param city        nama kota dari Geocoder
     * @param countryName nama negara lengkap dari Geocoder
     */
    private String[] buildData(String code, String city, String countryName) {
        String[] data = new String[4];
        data[2] = city;

        // Gunakan nama dari Geocoder jika ada, jika tidak ada baru gunakan fallback
        String displayCountry = (countryName != null && !countryName.isEmpty()) ? countryName : code;

        switch (code) {
            // ── Indonesia ──────────────────────────────────────────────
            case "ID":
                data[0] = "Hallo! Selamat Datang";
                data[1] = "Indonesia";
                data[3] = "Sahabat Hidup Sehat";
                break;

            // ── Spanyol & Amerika Latin ────────────────────────────────
            case "ES": case "MX": case "AR": case "CO": case "PE": case "CL":
                data[0] = "¡Hola! Bienvenido";
                data[1] = displayCountry;
                data[3] = "Tu Compañero de Vida Saludable";
                break;

            // ── Inggris & negara berbahasa Inggris ─────────────────────
            case "GB": case "US": case "AU": case "CA": case "NZ":
                data[0] = "Hello! Welcome";
                data[1] = displayCountry;
                data[3] = "Your Healthy Life Companion";
                break;

            // ── Default (Negara Lainnya) ───────────────
            default:
                data[0] = "Hello! Welcome";
                data[1] = displayCountry;
                data[3] = "Your Healthy Life Companion";
                break;
        }
        return data;
    }

    private void finalizeSplash(String[] data, String code) {
        // postValue aman dipanggil dari background thread (callback lokasi)
        // Activity akan menggunakan "0" untuk icon flag karena akan di-override oleh FlagKit
        uiState.postValue(new SplashState(
                data[0], data[1], data[2], data[3],
                0, true, false, code));
    }
}