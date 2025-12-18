package com.fitme.app.ui.splash;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context; // Import Context
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Import untuk Google Location Services
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import com.fitme.app.R;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashViewModel extends AndroidViewModel {

    private final FusedLocationProviderClient fusedLocationClient;
    private final MutableLiveData<SplashState> uiState = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        // Inisialisasi FusedLocation langsung disini
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
    }

    public LiveData<SplashState> getUiState() {
        return uiState;
    }

    // Fungsi Start menerima Context dari Activity
    public void startSplashSequence(boolean hasPermission, Context context) {
        processLocation(hasPermission, context);
    }

    @SuppressLint("MissingPermission")
    private void processLocation(boolean hasPermission, Context context) {
        // DATA DEFAULT: Gunakan Bahasa Inggris (Global) agar netral saat loading
        final String[] data = {
                "Hello! Welcome",
                "Detecting...",
                "Please wait...",
                "Your Healthy Life Companion"
        };
        final int[] flag = {R.drawable.flag_uk}; // Default bendera sementara

        if (hasPermission) {
            // --- LOGIKA "FORCE UPDATE" (High Accuracy) ---
            // Ini memaksa HP mencari sinyal GPS baru, tidak pakai data cache lama
            CancellationTokenSource cancellationToken = new CancellationTokenSource();

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            new Thread(() -> {
                                // Gunakan Locale.US agar nama negara standar internasional
                                Geocoder geocoder = new Geocoder(context, Locale.US);
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        Address address = addresses.get(0);

                                        // 1. Ambil Nama Kota (Prioritas Bertingkat)
                                        String cityName = address.getSubAdminArea();
                                        if (cityName == null) cityName = address.getLocality();
                                        if (cityName == null) cityName = address.getAdminArea();

                                        if (cityName != null) data[2] = cityName;

                                        // 2. DETEKSI NEGARA (LOGIKA UTAMA)
                                        String code = address.getCountryCode();
                                        if (code != null) {
                                            switch (code.toUpperCase()) {
                                                case "ID": // KASUS INDONESIA
                                                    data[0] = "Hallo! Selamat Datang";
                                                    data[1] = "Indonesia";
                                                    data[3] = "Sahabat Hidup Sehat";
                                                    flag[0] = R.drawable.flag_id;
                                                    break;

                                                case "ES": // KASUS SPANYOL
                                                    data[0] = "¡Hola! Bienvenido";
                                                    data[1] = "Spain";
                                                    data[3] = "Tu Compañero de Vida Saludable";
                                                    flag[0] = R.drawable.flag_es;
                                                    break;

                                                case "GB": // United Kingdom
                                                case "US": // USA
                                                    data[0] = "Hello! Welcome";
                                                    // Tampilkan Nama Asli Negara dari GPS (misal: United Kingdom)
                                                    data[1] = address.getCountryName();
                                                    data[3] = "Your Healthy Life Companion";
                                                    flag[0] = R.drawable.flag_uk;
                                                    break;

                                                default: // Negara Lain -> Bahasa Inggris
                                                    data[0] = "Hello! Welcome";
                                                    data[1] = address.getCountryName();
                                                    data[3] = "Your Healthy Life Companion";
                                                    flag[0] = R.drawable.flag_uk;
                                                    break;
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // Sukses: Kirim Data Terbaru
                                finalizeSplash(data, flag[0]);

                            }).start();
                        } else {
                            // Gagal dapat satelit -> Pakai Fallback Indonesia
                            fallbackToIndonesia(data, flag);
                            finalizeSplash(data, flag[0]);
                        }
                    }).addOnFailureListener(e -> {
                        // Error GPS -> Pakai Fallback Indonesia
                        fallbackToIndonesia(data, flag);
                        finalizeSplash(data, flag[0]);
                    });
        } else {
            // Izin Ditolak -> Pakai Fallback Indonesia
            fallbackToIndonesia(data, flag);
            finalizeSplash(data, flag[0]);
        }
    }

    // Fungsi Cadangan jika GPS Mati Total
    private void fallbackToIndonesia(String[] data, int[] flag) {
        data[0] = "Hallo! Selamat Datang";
        data[1] = "Indonesia";
        data[2] = "Indonesia";
        data[3] = "Sahabat Hidup Sehat";
        flag[0] = R.drawable.flag_id;
    }

    private void finalizeSplash(String[] data, int flagId) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            uiState.setValue(new SplashState(data[0], data[1], data[2], data[3], flagId, true, false));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                SplashState currentState = uiState.getValue();
                if (currentState != null) {
                    currentState.isFinished = true;
                    uiState.setValue(currentState);
                }
            }, 2500);
        }, 1000);
    }
}