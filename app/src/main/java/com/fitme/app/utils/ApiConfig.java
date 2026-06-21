package com.fitme.app.utils;

public class ApiConfig {

    // Groq AI
    public static final String GROQ_API_KEY  = com.fitme.app.BuildConfig.GROQ_API_KEY;
    public static final String GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    // Groq Models
    public static final String MODEL_TEXT   = "llama-3.1-8b-instant";
    public static final String MODEL_VISION = "meta-llama/llama-4-scout-17b-16e-instruct";

    // CalorieNinjas — Nutrition Database (gratis 10.000 req/bulan)
    public static final String CALORIE_NINJAS_API_KEY  = com.fitme.app.BuildConfig.CALORIE_NINJAS_API_KEY;
    public static final String CALORIE_NINJAS_ENDPOINT = "https://api.calorieninjas.com/v1/nutrition";
}
