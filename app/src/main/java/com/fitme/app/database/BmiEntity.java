package com.fitme.app.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bmi_table")
public class BmiEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public float bmi;
    public String category;
    public String date;
    public String age;
    public String gender;
    public String location;
    public float weight;
    public float height;
}