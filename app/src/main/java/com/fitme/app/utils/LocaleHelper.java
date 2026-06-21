package com.fitme.app.utils;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

public class LocaleHelper {

    /**
     * Set locale berdasarkan countryCode.
     * Panggil di FitMeApp.onCreate() dan setiap Activity.attachBaseContext()
     */
    public static void setLocale(Context context, String countryCode) {
        String language = getLangFromCountry(countryCode);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration(
                context.getResources().getConfiguration());
        config.setLocale(locale);

        context.getResources().updateConfiguration(
                config, context.getResources().getDisplayMetrics());
    }

    /**
     * Wrap context — pakai di attachBaseContext setiap Activity
     */
    public static Context wrap(Context context, String countryCode) {
        String language = getLangFromCountry(countryCode);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration(
                context.getResources().getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    public static String getLangFromCountry(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) return "in";
        switch (countryCode.toUpperCase()) {
            case "US": case "GB": case "AU": case "CA": case "NZ":
                return "en";
            case "ES": case "MX": case "AR": case "CO": case "PE": case "CL":
                return "es";
            default:
                return "in"; // Indonesia
        }
    }
}