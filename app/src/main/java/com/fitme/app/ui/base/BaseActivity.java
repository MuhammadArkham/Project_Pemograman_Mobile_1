package com.fitme.app.ui.base;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;

// Mengambil dari folder utils yang terlihat di screenshot Anda
import com.fitme.app.utils.LocaleHelper;
import com.fitme.app.utils.PrefManager;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context base) {
        PrefManager pref = new PrefManager(base);
        // Membungkus context dengan LocaleHelper agar bahasa menyesuaikan preferensi
        super.attachBaseContext(LocaleHelper.wrap(base, pref.getCountryCode()));
    }
}