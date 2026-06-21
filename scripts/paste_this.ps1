$path = "app\src\main\java\com\fitme\app\api\NutritionService.java"
$content = @'
package com.fitme.app.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * NutritionService — dua lapis pencarian:
 *
 * LAPIS 1: Database lokal makanan Indonesia (offline, instan, akurat)
 *          Berisi 80+ makanan Indonesia & Asia umum per 100g
 *
 * LAPIS 2: Open Food Facts API (online, fallback untuk makanan internasional)
 *
 * Jika keduanya gagal → FoodAnalysisActivity fallback ke estimasi Groq.
 */
public class NutritionService {

    private static final String TAG = "NutritionService";

    // ── Database Lokal per 100g (kalori, protein, karbo, lemak) ──────────
    // Sumber: TKPI (Tabel Komposisi Pangan Indonesia) & data USDA
    private static final Map<String, int[]> LOCAL_DB = new HashMap<>();

    static {
        // Format: "keyword" -> {kalori, protein, karbo, lemak} per 100g
        // ── Nasi & Karbohidrat ────────────────────────────────────────────
        LOCAL_DB.put("nasi putih",       new int[]{175, 3,  40, 0});
        LOCAL_DB.put("white rice",       new int[]{175, 3,  40, 0});
        LOCAL_DB.put("steamed rice",     new int[]{175, 3,  40, 0});
        LOCAL_DB.put("rice",             new int[]{175, 3,  40, 0});
        LOCAL_DB.put("nasi goreng",      new int[]{195, 4,  28, 8});
        LOCAL_DB.put("fried rice",       new int[]{195, 4,  28, 8});
        LOCAL_DB.put("nasi kebuli",      new int[]{210, 7,  30, 8});
        LOCAL_DB.put("kebuli",           new int[]{210, 7,  30, 8});
        LOCAL_DB.put("biryani",          new int[]{210, 7,  30, 8});
        LOCAL_DB.put("nasi uduk",        new int[]{200, 4,  30, 8});
        LOCAL_DB.put("nasi kuning",      new int[]{185, 3,  35, 4});
        LOCAL_DB.put("nasi padang",      new int[]{220, 8,  28, 9});
        LOCAL_DB.put("lontong",          new int[]{145, 2,  32, 0});
        LOCAL_DB.put("ketupat",          new int[]{145, 2,  32, 0});
        LOCAL_DB.put("bubur ayam",       new int[]{90,  5,  13, 2});
        LOCAL_DB.put("congee",           new int[]{90,  5,  13, 2});
        LOCAL_DB.put("mie goreng",       new int[]{220, 7,  32, 8});
        LOCAL_DB.put("fried noodle",     new int[]{220, 7,  32, 8});
        LOCAL_DB.put("mie rebus",        new int[]{150, 5,  25, 3});
        LOCAL_DB.put("noodle soup",      new int[]{150, 5,  25, 3});
        LOCAL_DB.put("indomie",          new int[]{430, 9,  62, 16});
        LOCAL_DB.put("instant noodle",   new int[]{430, 9,  62, 16});
        LOCAL_DB.put("bihun",            new int[]{160, 2,  36, 0});
        LOCAL_DB.put("rice vermicelli",  new int[]{160, 2,  36, 0});
        LOCAL_DB.put("kwetiau",          new int[]{140, 3,  30, 1});
        LOCAL_DB.put("flat noodle",      new int[]{140, 3,  30, 1});
        LOCAL_DB.put("roti",             new int[]{265, 8,  50, 4});
        LOCAL_DB.put("bread",            new int[]{265, 8,  50, 4});

        // ── Protein / Lauk ────────────────────────────────────────────────
        LOCAL_DB.put("ayam goreng",      new int[]{260, 27, 8,  14});
        LOCAL_DB.put("fried chicken",    new int[]{260, 27, 8,  14});
        LOCAL_DB.put("ayam bakar",       new int[]{200, 28, 2,  9});
        LOCAL_DB.put("grilled chicken",  new int[]{200, 28, 2,  9});
        LOCAL_DB.put("chicken",          new int[]{215, 25, 0,  12});
        LOCAL_DB.put("daging sapi",      new int[]{250, 26, 0,  16});
        LOCAL_DB.put("beef",             new int[]{250, 26, 0,  16});
        LOCAL_DB.put("rendang",          new int[]{320, 28, 5,  20});
        LOCAL_DB.put("ikan goreng",      new int[]{190, 22, 5,  9});
        LOCAL_DB.put("fried fish",       new int[]{190, 22, 5,  9});
        LOCAL_DB.put("ikan bakar",       new int[]{160, 24, 0,  7});
        LOCAL_DB.put("grilled fish",     new int[]{160, 24, 0,  7});
        LOCAL_DB.put("fish",             new int[]{130, 22, 0,  4});
        LOCAL_DB.put("udang",            new int[]{100, 20, 1,  2});
        LOCAL_DB.put("shrimp",           new int[]{100, 20, 1,  2});
        LOCAL_DB.put("prawn",            new int[]{100, 20, 1,  2});
        LOCAL_DB.put("telur goreng",     new int[]{185, 13, 1,  14});
        LOCAL_DB.put("fried egg",        new int[]{185, 13, 1,  14});
        LOCAL_DB.put("telur rebus",      new int[]{155, 13, 1,  11});
        LOCAL_DB.put("boiled egg",       new int[]{155, 13, 1,  11});
        LOCAL_DB.put("egg",              new int[]{155, 13, 1,  11});
        LOCAL_DB.put("tahu",             new int[]{80,  9,  2,  5});
        LOCAL_DB.put("tofu",             new int[]{80,  9,  2,  5});
        LOCAL_DB.put("tempe",            new int[]{195, 19, 10, 11});
        LOCAL_DB.put("tempeh",           new int[]{195, 19, 10, 11});
        LOCAL_DB.put("bakso",            new int[]{130, 8,  12, 5});
        LOCAL_DB.put("meatball",         new int[]{130, 8,  12, 5});
        LOCAL_DB.put("sate",             new int[]{235, 22, 8,  13});
        LOCAL_DB.put("satay",            new int[]{235, 22, 8,  13});
        LOCAL_DB.put("soto ayam",        new int[]{100, 10, 8,  3});
        LOCAL_DB.put("chicken soup",     new int[]{100, 10, 8,  3});

        // ── Sayur & Kuah ──────────────────────────────────────────────────
        LOCAL_DB.put("sayur sop",        new int[]{45,  3,  6,  1});
        LOCAL_DB.put("vegetable soup",   new int[]{45,  3,  6,  1});
        LOCAL_DB.put("gado gado",        new int[]{170, 8,  14, 10});
        LOCAL_DB.put("cap cay",          new int[]{85,  5,  8,  4});
        LOCAL_DB.put("capcay",           new int[]{85,  5,  8,  4});
        LOCAL_DB.put("kangkung",         new int[]{35,  3,  5,  1});
        LOCAL_DB.put("bayam",            new int[]{25,  2,  4,  0});
        LOCAL_DB.put("spinach",          new int[]{25,  2,  4,  0});
        LOCAL_DB.put("brokoli",          new int[]{35,  3,  7,  0});
        LOCAL_DB.put("broccoli",         new int[]{35,  3,  7,  0});

        // ── Jajanan & Snack ───────────────────────────────────────────────
        LOCAL_DB.put("martabak",         new int[]{280, 9,  35, 12});
        LOCAL_DB.put("pisang goreng",    new int[]{175, 2,  30, 6});
        LOCAL_DB.put("banana fritter",   new int[]{175, 2,  30, 6});
        LOCAL_DB.put("kerupuk",          new int[]{380, 6,  68, 9});
        LOCAL_DB.put("crackers",         new int[]{380, 6,  68, 9});
        LOCAL_DB.put("gorengan",         new int[]{260, 5,  28, 14});

        // ── Makanan Internasional Umum ────────────────────────────────────
        LOCAL_DB.put("pizza",            new int[]{266, 11, 33, 10});
        LOCAL_DB.put("burger",           new int[]{295, 17, 24, 14});
        LOCAL_DB.put("hamburger",        new int[]{295, 17, 24, 14});
        LOCAL_DB.put("pasta",            new int[]{220, 8,  43, 2});
        LOCAL_DB.put("spaghetti",        new int[]{220, 8,  43, 2});
        LOCAL_DB.put("sandwich",         new int[]{250, 12, 30, 9});
        LOCAL_DB.put("salad",            new int[]{60,  2,  8,  3});
        LOCAL_DB.put("soup",             new int[]{70,  4,  8,  2});
        LOCAL_DB.put("steak",            new int[]{270, 26, 0,  18});
        LOCAL_DB.put("french fries",     new int[]{312, 4,  41, 15});
        LOCAL_DB.put("sushi",            new int[]{145, 6,  27, 1});
        LOCAL_DB.put("ramen",            new int[]{190, 9,  27, 5});
        LOCAL_DB.put("dim sum",          new int[]{170, 9,  18, 7});
        LOCAL_DB.put("dumpling",         new int[]{170, 9,  18, 7});
        LOCAL_DB.put("pancake",          new int[]{227, 6,  28, 10});
        LOCAL_DB.put("waffle",           new int[]{291, 8,  37, 13});

        // ── Minuman ───────────────────────────────────────────────────────
        LOCAL_DB.put("es teh",           new int[]{30,  0,  8,  0});
        LOCAL_DB.put("teh manis",        new int[]{60,  0,  16, 0});
        LOCAL_DB.put("jus jeruk",        new int[]{45,  1,  10, 0});
        LOCAL_DB.put("orange juice",     new int[]{45,  1,  10, 0});
        LOCAL_DB.put("susu",             new int[]{61,  3,  5,  3});
        LOCAL_DB.put("milk",             new int[]{61,  3,  5,  3});
        LOCAL_DB.put("kopi",             new int[]{5,   0,  1,  0});
        LOCAL_DB.put("coffee",           new int[]{5,   0,  1,  0});
    }

    // ── Open Food Facts (fallback online) ────────────────────────────────
    private static final String OFF_ENDPOINT =
            "https://world.openfoodfacts.org/cgi/search.pl" +
            "?action=process&json=true&page_size=1" +
            "&fields=nutriments,product_name&search_terms=";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20,    TimeUnit.SECONDS)
            .build();

    // ── Result & Callback ─────────────────────────────────────────────────

    public static class NutritionResult {
        public int    calories;
        public int    protein;
        public int    carbs;
        public int    fat;
        public int    fiber;
        public String foodName;
        public boolean fromCache;
        public String  source; // "local_db" atau "open_food_facts"
    }

    public interface NutritionCallback {
        void onSuccess(NutritionResult result);
        void onError(String errorMessage);
    }

    private static final Map<String, NutritionResult> cache = new HashMap<>();

    // ── Public API ────────────────────────────────────────────────────────

    public static void fetchNutrition(String foodNameEnglish,
                                      int weightGram,
                                      NutritionCallback callback) {

        String cleanName = foodNameEnglish.trim().toLowerCase()
                .replaceAll("[^a-z0-9 ]", "").trim();
        String cacheKey  = cleanName + "_" + weightGram;

        if (cache.containsKey(cacheKey)) {
            NutritionResult cached = cache.get(cacheKey);
            cached.fromCache = true;
            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(cached));
            return;
        }

        // ── LAPIS 1: Cek database lokal ───────────────────────────────────
        NutritionResult localResult = searchLocalDB(cleanName, weightGram, foodNameEnglish);
        if (localResult != null) {
            Log.d(TAG, "LOCAL DB HIT: " + cleanName + " -> " + localResult.calories + "kcal");
            cache.put(cacheKey, localResult);
            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(localResult));
            return;
        }

        // ── LAPIS 2: Open Food Facts ──────────────────────────────────────
        Log.d(TAG, "LOCAL DB MISS, coba Open Food Facts: " + cleanName);
        String[] words    = cleanName.split("\\s+");
        String shortName  = words.length > 3 ? words[0] + " " + words[1] : cleanName;
        fetchFromOpenFoodFacts(cleanName, shortName, weightGram, foodNameEnglish, cacheKey, callback);
    }

    // ── Pencarian database lokal ──────────────────────────────────────────

    private static NutritionResult searchLocalDB(String cleanName, int weightGram, String originalName) {
        // Coba exact match dulu
        if (LOCAL_DB.containsKey(cleanName)) {
            return buildFromLocal(LOCAL_DB.get(cleanName), weightGram, originalName);
        }

        // Coba partial match — cari keyword yang ada di nama makanan
        int bestMatchLen = 0;
        int[] bestData   = null;
        for (Map.Entry<String, int[]> entry : LOCAL_DB.entrySet()) {
            String key = entry.getKey();
            if (cleanName.contains(key) || key.contains(cleanName)) {
                if (key.length() > bestMatchLen) {
                    bestMatchLen = key.length();
                    bestData     = entry.getValue();
                }
            }
        }

        if (bestData != null) {
            Log.d(TAG, "LOCAL DB partial match untuk: " + cleanName);
            return buildFromLocal(bestData, weightGram, originalName);
        }

        // Coba kata pertama saja
        String firstWord = cleanName.split("\\s+")[0];
        if (LOCAL_DB.containsKey(firstWord)) {
            return buildFromLocal(LOCAL_DB.get(firstWord), weightGram, originalName);
        }

        return null;
    }

    private static NutritionResult buildFromLocal(int[] data, int weightGram, String name) {
        double scale = weightGram / 100.0;
        NutritionResult r = new NutritionResult();
        r.foodName = name;
        r.calories = (int) Math.round(data[0] * scale);
        r.protein  = (int) Math.round(data[1] * scale);
        r.carbs    = (int) Math.round(data[2] * scale);
        r.fat      = (int) Math.round(data[3] * scale);
        r.source   = "local_db";
        return r;
    }

    // ── Open Food Facts API ───────────────────────────────────────────────

    private static void fetchFromOpenFoodFacts(String primaryQuery,
                                               String fallbackQuery,
                                               int weightGram,
                                               String foodNameEnglish,
                                               String cacheKey,
                                               NutritionCallback callback) {
        String encodedQuery;
        try {
            encodedQuery = java.net.URLEncoder.encode(primaryQuery, "UTF-8");
        } catch (Exception e) {
            encodedQuery = primaryQuery.replace(" ", "+");
        }

        Request request = new Request.Builder()
                .url(OFF_ENDPOINT + encodedQuery)
                .addHeader("User-Agent", "FITME-Android/1.0")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!primaryQuery.equals(fallbackQuery)) {
                    fetchFromOpenFoodFacts(fallbackQuery, fallbackQuery, weightGram,
                            foodNameEnglish, cacheKey, callback);
                } else {
                    postError(callback, "not_found");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String raw = "";
                try {
                    raw = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) { postError(callback, "not_found"); return; }

                    JSONObject json     = new JSONObject(raw);
                    JSONArray  products = json.optJSONArray("products");

                    if (products == null || products.length() == 0) {
                        if (!primaryQuery.equals(fallbackQuery)) {
                            fetchFromOpenFoodFacts(fallbackQuery, fallbackQuery, weightGram,
                                    foodNameEnglish, cacheKey, callback);
                        } else {
                            postError(callback, "not_found");
                        }
                        return;
                    }

                    JSONObject nutriments = products.getJSONObject(0).optJSONObject("nutriments");
                    if (nutriments == null) { postError(callback, "not_found"); return; }

                    double scale   = weightGram / 100.0;
                    double cal100  = getDouble(nutriments, "energy-kcal_100g", "energy-kcal", "energy_100g");
                    double pro100  = getDouble(nutriments, "proteins_100g",    "proteins");
                    double carb100 = getDouble(nutriments, "carbohydrates_100g","carbohydrates");
                    double fat100  = getDouble(nutriments, "fat_100g",         "fat");
                    double fib100  = getDouble(nutriments, "fiber_100g",       "fiber");
                    if (cal100 == 0) {
                        double kj = getDouble(nutriments, "energy_100g", "energy-kj_100g");
                        if (kj > 0) cal100 = kj / 4.184;
                    }

                    NutritionResult result = new NutritionResult();
                    result.foodName = foodNameEnglish;
                    result.calories = (int) Math.round(cal100  * scale);
                    result.protein  = (int) Math.round(pro100  * scale);
                    result.carbs    = (int) Math.round(carb100 * scale);
                    result.fat      = (int) Math.round(fat100  * scale);
                    result.fiber    = (int) Math.round(fib100  * scale);
                    result.source   = "open_food_facts";

                    if (result.calories == 0 && result.protein == 0 && result.carbs == 0) {
                        postError(callback, "not_found"); return;
                    }

                    Log.d(TAG, "OFF OK: " + result.calories + "kcal");
                    cache.put(cacheKey, result);
                    postSuccess(callback, result);

                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + e.getMessage());
                    postError(callback, "not_found");
                }
            }
        });
    }

    private static double getDouble(JSONObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key)) {
                try { return obj.getDouble(key); } catch (Exception ignored) {}
            }
        }
        return 0.0;
    }

    private static void postSuccess(NutritionCallback cb, NutritionResult r) {
        new Handler(Looper.getMainLooper()).post(() -> cb.onSuccess(r));
    }

    private static void postError(NutritionCallback cb, String msg) {
        new Handler(Looper.getMainLooper()).post(() -> cb.onError(msg));
    }

    public static void clearCache() { cache.clear(); }
}

'@
[System.IO.File]::WriteAllText((Resolve-Path $path).Path, $content, [System.Text.Encoding]::UTF8)
Write-Host "BERHASIL! NutritionService.java diupdate." -ForegroundColor Green