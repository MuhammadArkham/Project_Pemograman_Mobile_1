package com.fitme.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticles(List<ArticleEntity> articles);

    @Query("SELECT * FROM articles ORDER BY timestamp DESC")
    LiveData<List<ArticleEntity>> getAllArticles();

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY timestamp DESC")
    LiveData<List<ArticleEntity>> getArticlesByCategory(String category);

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    LiveData<List<ArticleEntity>> getBookmarkedArticles();

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    LiveData<List<ArticleEntity>> searchArticles(String query);

    @Query("SELECT * FROM articles WHERE link = :url LIMIT 1")
    ArticleEntity getArticleByUrl(String url);

    @androidx.room.Update
    void updateArticle(ArticleEntity article);

    @Query("SELECT COUNT(*) FROM articles")
    int getArticleCount();

    @Query("DELETE FROM articles WHERE isBookmarked = 0")
    void deleteAllUnbookmarkedArticles();

    @Query("DELETE FROM articles WHERE isBookmarked = 0 AND timestamp < :cutoffTimestamp")
    void deleteOldArticles(long cutoffTimestamp);

    @Query("DELETE FROM articles WHERE source LIKE '%Google%' OR link LIKE '%google%'")
    void deleteGoogleNews();
}