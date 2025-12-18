package com.fitme.app.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bmi_table")
public class BmiEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "bmi")
    public float bmi;        // Pastikan namanya 'bmi' (kecil semua)

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "gender")
    public String gender;

    @ColumnInfo(name = "age")
    public String age;       // Pastikan namanya 'age' (kecil semua)
}
