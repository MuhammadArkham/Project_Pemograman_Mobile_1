package com.fitme.app.ui.articles;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fitme.app.database.ArticleEntity;
import com.fitme.app.database.ArticleRepository;

import java.util.List;

/**
 * ArticlesViewModel — State management untuk fitur artikel.
 *
 * States:
 *   LOADING    → spinner tampil, tidak ada data
 *   SUCCESS    → artikel tampil, tidak ada spinner
 *   EMPTY      → tidak ada artikel, tampilkan empty state
 *   ERROR      → fetch gagal, cache kosong, tampilkan error state
 *   REFRESHING → spinner SwipeRefresh, data lama masih tampil
 */
public class ArticlesViewModel extends AndroidViewModel {

    // State enum
    public enum ArticleState { LOADING, SUCCESS, EMPTY, ERROR, REFRESHING }

    private final ArticleRepository repository;

    // ── LiveData yang di-observe UI ──
    public final LiveData<List<ArticleEntity>> articlesLive;
    private final MutableLiveData<ArticleState> stateLive  = new MutableLiveData<>(ArticleState.LOADING);
    private final MutableLiveData<String>       errorLive  = new MutableLiveData<>();
    private final MutableLiveData<Boolean>      swipeLive  = new MutableLiveData<>(false);

    public ArticlesViewModel(@NonNull Application application) {
        super(application);
        repository   = new ArticleRepository(application);
        articlesLive = repository.getAllArticlesLive();

        // Gunakan logic cache 3 jam dari repository
        stateLive.postValue(ArticleState.LOADING);
        checkAndFetch();
    }

    public LiveData<ArticleState> getState()     { return stateLive; }
    public LiveData<String>       getError()     { return errorLive; }
    public LiveData<Boolean>      getSwipeAnim() { return swipeLive; }

    public LiveData<List<ArticleEntity>> getBookmarkedArticlesLive() {
        return repository.getBookmarkedArticlesLive();
    }

    public LiveData<List<ArticleEntity>> searchArticlesLive(String query) {
        return repository.searchArticlesLive(query);
    }

    public void toggleBookmark(ArticleEntity article) {
        repository.toggleBookmark(article);
    }

    /**
     * Cek cache dan fetch jika lebih dari 3 jam atau kosong.
     */
    private void checkAndFetch() {
        stateLive.postValue(ArticleState.LOADING);
        errorLive.postValue(null);
        repository.checkCacheAndFetchIfNeeded(new ArticleRepository.RefreshCallback() {
            @Override
            public void onSuccess(int count) {
                if (count > 0) {
                    stateLive.postValue(ArticleState.SUCCESS);
                } else {
                    stateLive.postValue(ArticleState.EMPTY);
                }
            }
            @Override
            public void onError(String message) {
                List<ArticleEntity> current = articlesLive.getValue();
                if (current != null && !current.isEmpty()) {
                    stateLive.postValue(ArticleState.SUCCESS); // tampilkan cache
                } else {
                    errorLive.postValue(message);
                    stateLive.postValue(ArticleState.ERROR);
                }
            }
        });
    }

    /**
     * Refresh dengan SwipeRefreshLayout — dipanggil saat pull-to-refresh.
     */
    public void refreshArticles() {
        swipeLive.setValue(true);
        errorLive.setValue(null);
        repository.refreshArticles(new ArticleRepository.RefreshCallback() {
            @Override
            public void onSuccess(int count) {
                swipeLive.postValue(false);
                if (count > 0) stateLive.postValue(ArticleState.SUCCESS);
                else           stateLive.postValue(ArticleState.EMPTY);
            }
            @Override
            public void onError(String message) {
                swipeLive.postValue(false);
                // Tidak ubah state ke ERROR jika sudah SUCCESS (cache masih ada)
                ArticleState current = stateLive.getValue();
                if (current != ArticleState.SUCCESS) {
                    errorLive.postValue(message);
                    stateLive.postValue(ArticleState.ERROR);
                }
            }
        });
    }

    /**
     * Refresh tanpa loading indicator — dipanggil saat cache sudah ada.
     * Room LiveData otomatis update UI jika ada data baru.
     */
    public void refreshQuietly() {
        errorLive.postValue(null); // postValue — aman dari background thread
        repository.refreshArticles(new ArticleRepository.RefreshCallback() {
            @Override public void onSuccess(int count) {
                if (count > 0 && stateLive.getValue() == ArticleState.EMPTY)
                    stateLive.postValue(ArticleState.SUCCESS);
            }
            @Override public void onError(String ignored) { /* cache tetap tampil */ }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdown();
    }
}