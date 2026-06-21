package com.fitme.app.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_log")
public class FoodEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String emoji;
    public int calories;
    public int protein;
    public int carbs;
    public int fat;
    public String time;
    public String imagePath;
    public String portion;
    public String note;
    public String date; // Format: YYYY-MM-DD
}