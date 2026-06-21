package com.fitme.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LocationHelper {

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;

    // Interface untuk mengirim hasil ke ViewModel
    public interface LocationCallback {
        void onResult(LocationResult result);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationCallback callback) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // ✅ REVISI: Pindahkan Geocoder ke Background Thread agar animasi UI tidak lag/freeze!
                Executors.newSingleThreadExecutor().execute(() -> {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);

                            // Prioritas: SubAdminArea (Kabupaten/Kota)
                            String cityOnly = address.getSubAdminArea();
                            if (cityOnly == null) cityOnly = address.getLocality();
                            if (cityOnly == null) cityOnly = address.getAdminArea();
                            if (cityOnly == null) cityOnly = "Unknown City";

                            // Pembersih Kata
                            cityOnly = cityOnly.replace("Greater ", "")
                                    .replace("Kabupaten ", "")
                                    .replace("Kota ", "");

                            LocationResult result = new LocationResult(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    address.getCountryCode() != null ? address.getCountryCode() : "XX",
                                    address.getCountryName() != null ? address.getCountryName() : "Unknown",
                                    address.getAdminArea() != null ? address.getAdminArea() : "",
                                    cityOnly.trim()
                            );

                            // ✅ REVISI: Kembalikan hasilnya ke Main Thread untuk di-update ke UI
                            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(result));
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Jika gagal baca Geocoder (misal internet mati), kirim null kembali ke Main Thread
                    new Handler(Looper.getMainLooper()).post(() -> callback.onResult(null));
                });
            } else {
                // Jika lokasi GPS belum didapat sama sekali
                callback.onResult(null);
            }
        });
    }
}