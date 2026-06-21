package utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LocationHelper {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public interface LocationCallback {
        void onResult(LocationResult result);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationCallback callback) {
        CancellationTokenSource cts = new CancellationTokenSource();

        // Memaksa mencari sinyal GPS terbaru, bukan mengambil sisa cache dari Fake GPS
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        processLocation(location, callback);
                    } else {
                        // Jika satelit GPS buta/nyangkut, langsung beralih ke Mode Offline (Kartu SIM)
                        fallbackToOffline(callback);
                    }
                })
                .addOnFailureListener(e -> fallbackToOffline(callback));
    }

    private void processLocation(Location location, LocationCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                // Mencoba mencari nama kota menggunakan Internet
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    String cityOnly = address.getLocality();
                    if (cityOnly == null) cityOnly = address.getSubAdminArea();
                    if (cityOnly == null) cityOnly = address.getAdminArea();
                    if (cityOnly == null) cityOnly = "Lokasi Ditemukan";

                    // Pembersih Kata
                    cityOnly = cityOnly.replace("Greater ", "")
                            .replace("Kabupaten ", "")
                            .replace("Kota ", "")
                            .trim();

                    if (cityOnly.isEmpty()) cityOnly = "Lokasi Ditemukan";

                    String cCode = address.getCountryCode() != null ? address.getCountryCode() : getOfflineCountryCode();
                    String cName = address.getCountryName() != null ? address.getCountryName() : getOfflineCountryName(cCode);

                    LocationResult result = new LocationResult(
                            location.getLatitude(), location.getLongitude(),
                            cCode, cName, address.getAdminArea() != null ? address.getAdminArea() : "", cityOnly
                    );
                    postResult(callback, result);
                    return; // Sukses Online!
                }
            } catch (Exception e) {
                // Internet mati (Geocoder gagal), tapi kita punya titik koordinat GPS
            }

            // --- JIKA INTERNET MATI (OFFLINE) ---
            String fallbackCode = getOfflineCountryCode();
            LocationResult result = new LocationResult(
                    location.getLatitude(), location.getLongitude(),
                    fallbackCode, getOfflineCountryName(fallbackCode),
                    "", "Mode Offline (GPS Aktif)"
            );
            postResult(callback, result);
        });
    }

    private void fallbackToOffline(LocationCallback callback) {
        // --- JIKA GPS NYANGKUT ATAU MATI TOTAL ---
        String fallbackCode = getOfflineCountryCode();
        LocationResult result = new LocationResult(
                0.0, 0.0,
                fallbackCode, getOfflineCountryName(fallbackCode),
                "", "Mode Offline"
        );
        postResult(callback, result);
    }

    private void postResult(LocationCallback callback, LocationResult result) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(result));
    }

    // ─── FITUR SPESIAL: MENDETEKSI NEGARA DARI KARTU SIM / HP (TANPA GPS & INTERNET) ───
    private String getOfflineCountryCode() {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String simCountry = tm.getNetworkCountryIso();
            if (simCountry != null && simCountry.length() == 2) {
                return simCountry.toUpperCase();
            }
        } catch (Exception ignored) {}

        // Jika tidak ada SIM Card, ambil dari pengaturan bahasa HP
        String localeCountry = Locale.getDefault().getCountry();
        return (localeCountry != null && !localeCountry.isEmpty()) ? localeCountry.toUpperCase() : "ID";
    }

    private String getOfflineCountryName(String code) {
        Locale loc = new Locale("", code);
        return loc.getDisplayCountry();
    }
}