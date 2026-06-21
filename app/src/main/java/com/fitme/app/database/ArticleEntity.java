package com.fitme.app.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.room.Index;

@Entity(tableName = "articles", indices = {
    @Index("category"),
    @Index("timestamp")
})
public class ArticleEntity {

    @PrimaryKey
    @NonNull
    public String link = "";

    public String title;
    public String description;
    public String imageUrl;
    public String pubDate;
    public String source;
    public String category;   // "Nutrisi" / "Olahraga" / "Diet" / "Kesehatan"
    public long   timestamp;

    public boolean isBookmarked;
    public int     readTime;  // in minutes
    public String  tags;      // JSON string or comma separated

    public ArticleEntity() {
        this.timestamp = System.currentTimeMillis();
        this.isBookmarked = false;
        this.readTime = 1;
    }
}