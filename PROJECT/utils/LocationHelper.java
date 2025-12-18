package com.fitme.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);

                            // Logic Prioritas Kota
                            String cityOnly = address.getLocality();
                            if (cityOnly == null) cityOnly = address.getSubAdminArea();
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

                            callback.onResult(result);
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Jika gagal/null
                callback.onResult(null);
            }
        });
    }
}