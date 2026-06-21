package com.fitme.app.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.fitme.app.database.ArticleEntity;
import com.fitme.app.database.ArticleRepository;
import com.fitme.app.database.BmiEntity;
import com.fitme.app.database.BmiRepository;
import com.fitme.app.utils.PrefManager;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final PrefManager                    pref;
    private final LiveData<List<BmiEntity>>      allBmiLive;
    private final ArticleRepository              articleRepo;

    public final LiveData<BmiEntity>                        lastBmiLive;
    public final LiveData<List<ArticleEntity>>              articlesLive;

    // Loading state untuk section artikel di Home
    private final MutableLiveData<Boolean> articleLoadingLive = new MutableLiveData<>(false);
    public LiveData<Boolean> getArticleLoading() { return articleLoadingLive; }

    public HomeViewModel(@NonNull Application application) {
        super(application);
        pref       = new PrefManager(application);
        allBmiLive = new BmiRepository(application).getAllBmiLive();

        lastBmiLive = Transformations.map(allBmiLive,
                list -> (list != null && !list.isEmpty()) ? list.get(0) : null);

        articleRepo  = new ArticleRepository(application);
        articlesLive = articleRepo.getAllArticlesLive();

        // Strategi 2 tahap:
        // 1. Cek cache → jika ada, tampilkan langsung (0 detik)
        // 2. Quick fetch (RSS only ~5-10 detik) → tampilkan
        // 3. Full fetch (dengan OG scraping ~30 detik) → update gambar
        articleRepo.hasCachedArticles(hasCached -> {
            if (hasCached) {
                // Ada cache → tampilkan langsung, refresh quietly di background
                articleLoadingLive.postValue(false);
                refreshFull(); // Full refresh dengan gambar di background
            } else {
                // Tidak ada cache → tampilkan loading, fetch cepat dulu
                articleLoadingLive.postValue(true);
                refreshQuick();
            }
        });
    }

    /**
     * Quick fetch: hanya RSS feed bergambar, ~5-10 detik.
     * Setelah selesai, lanjut full fetch di background.
     */
    private void refreshQuick() {
        articleRepo.refreshArticlesQuick(new ArticleRepository.RefreshCallback() {
            @Override
            public void onSuccess(int count) {
                // Artikel cepat sudah tampil, hilangkan loading
                articleLoadingLive.postValue(false);
                // Lanjut full fetch (OG images) di background — Room akan auto-update
                refreshFull();
            }
            @Override
            public void onError(String message) {
                articleLoadingLive.postValue(false);
                // Coba full fetch sebagai fallback
                refreshFull();
            }
        });
    }

    /**
     * Full fetch: semua feed + OG scraping ~25-35 detik.
     * Berjalan di background, Room auto-update LiveData.
     */
    private void refreshFull() {
        articleRepo.refreshArticles(new ArticleRepository.RefreshCallback() {
            @Override public void onSuccess(int count) { /* Room auto-update */ }
            @Override public void onError(String message) { /* Cache tetap tampil */ }
        });
    }

    public void refreshArticles() {
        articleLoadingLive.setValue(true);
        refreshQuick();
    }

    public void toggleBookmark(ArticleEntity article) {
        articleRepo.toggleBookmark(article);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        articleRepo.shutdown();
    }

    public String getCountryCode() {
        String c = pref.getCountryCode();
        return (c != null && !c.isEmpty()) ? c : "ID";
    }

    public String getDetectedCity() {
        String city = pref.getDetectedCity();
        return city != null ? city : "";
    }

    public String getUserName() { return pref.getUserName(); }
    public boolean isUserMale() { return pref.isMale(); }
    public boolean hasProfile() { return pref.getUserAge() > 0; }

    public float calculateBarFraction(float bmi) {
        float sN = 1.85f / 4.0f, sG = 2.50f / 4.0f, sO = 3.00f / 4.0f;
        float f;
        if      (bmi <= 10f)  f = 0.02f;
        else if (bmi < 18.5f) f = ((bmi - 10f) / 8.5f) * sN;
        else if (bmi < 25f)   f = sN + ((bmi - 18.5f) / 6.5f) * (0.65f / 4.0f);
        else if (bmi < 30f)   f = sG + ((bmi - 25f) / 5f) * (0.50f / 4.0f);
        else                  f = sO + Math.min(1f, (bmi - 30f) / 10f) * (1.00f / 4.0f);
        return Math.max(0.02f, Math.min(0.97f, f));
    }
}