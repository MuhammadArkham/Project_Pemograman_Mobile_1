package com.fitme.app.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FoodVisionService {

    private static final String TAG     = "FoodVisionService";
    private static final String API_KEY = "AIzaSyA2mZY3h7l2hCpHGC_8SmiBxVg72qighes";

    // ✅ FIX: gemini-2.5-flash-lite — stabil, 1000 RPD free tier
    private static final String MODEL   = "gemini-2.5-flash-lite";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=" + API_KEY;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    // ─── Result model ────────────────────────────────────────────────────────

    public static class FoodAnalysisResult {
        public String name;
        public String foodNameEnglish;
        public String emoji;
        public String portion;
        public String note;
        public int    weightGram;
        public int    calories;
        public int    protein;
        public int    carbs;
        public int    fat;
    }

    // ─── Callback ────────────────────────────────────────────────────────────

    public interface AnalysisCallback {
        void onSuccess(FoodAnalysisResult result);
        void onError(String errorMessage);
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    public static void analyzeFood(byte[] imageBytes,
                                   String countryCode,
                                   AnalysisCallback callback) {
        analyzeFood(imageBytes, countryCode, callback, false);
    }

    private static void analyzeFood(byte[] imageBytes,
                                    String countryCode,
                                    AnalysisCallback callback,
                                    boolean isRetry) {

        Handler mainHandler = new Handler(Looper.getMainLooper());
        String  base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        try {
            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);

            JSONObject imagePart = new JSONObject();
            imagePart.put("inline_data", inlineData);

            JSONObject textPart = new JSONObject();
            textPart.put("text", buildPrompt(countryCode));

            JSONArray parts = new JSONArray();
            parts.put(textPart);
            parts.put(imagePart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);
            content.put("role", "user");

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("thinkingBudget", 0);

            JSONObject genConfig = new JSONObject();
            genConfig.put("temperature",     0.1);
            genConfig.put("maxOutputTokens", 512);
            genConfig.put("thinkingConfig",  thinkingConfig);

            JSONObject body = new JSONObject();
            body.put("contents",         contents);
            body.put("generationConfig", genConfig);

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .build();

            Log.d(TAG, "Sending request to: " + MODEL);

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network failure", e);
                    mainHandler.post(() ->
                            callback.onError("Koneksi gagal: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String rawBody = response.body() != null
                            ? response.body().string() : "";
                    int code = response.code();

                    Log.d(TAG, "HTTP " + code);

                    // ✅ 503 — server sementara down, retry 1x
                    if (code == 503 && !isRetry) {
                        Log.w(TAG, "503 received, retrying after 2s...");
                        mainHandler.postDelayed(() ->
                                        analyzeFood(imageBytes, countryCode, callback, true),
                                2000);
                        return;
                    }

                    // ✅ 429 — limit harian tercapai
                    if (code == 429) {
                        mainHandler.post(() -> callback.onError(
                                "Batas harian AI tercapai. Coba lagi besok."));
                        return;
                    }

                    // ✅ 404 — model salah atau deprecated
                    if (code == 404) {
                        Log.e(TAG, "404 - model not found: " + MODEL);
                        mainHandler.post(() -> callback.onError(
                                "Model AI tidak tersedia saat ini."));
                        return;
                    }

                    if (!response.isSuccessful()) {
                        String msg = "Error " + code;
                        try {
                            JSONObject err = new JSONObject(rawBody).getJSONObject("error");
                            msg = "Gemini: " + err.getString("message");
                        } catch (Exception ignored) {}
                        final String finalMsg = msg;
                        mainHandler.post(() -> callback.onError(finalMsg));
                        return;
                    }

                    parseAndReturn(rawBody, mainHandler, callback);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Request build error", e);
            mainHandler.post(() -> callback.onError("Gagal memproses gambar."));
        }
    }

    // ─── Parser ──────────────────────────────────────────────────────────────

    private static void parseAndReturn(String rawBody,
                                       Handler mainHandler,
                                       AnalysisCallback callback) {
        try {
            JSONObject json       = new JSONObject(rawBody);
            JSONArray  candidates = json.getJSONArray("candidates");
            JSONObject first      = candidates.getJSONObject(0);

            String finishReason = first.optString("finishReason", "STOP");
            if (!finishReason.equals("STOP") && !finishReason.equals("MAX_TOKENS")) {
                mainHandler.post(() -> callback.onError(
                        "AI menolak analisis (" + finishReason + "). Coba foto lain."));
                return;
            }

            JSONArray parts = first.getJSONObject("content").getJSONArray("parts");

            // Cari part JSON — lewati thought-parts Gemini 2.5
            String jsonText = null;
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.optBoolean("thought", false)) continue;
                String text = part.optString("text", "");
                if (text.contains("{")) { jsonText = text; break; }
            }

            if (jsonText == null || jsonText.isEmpty()) {
                Log.e(TAG, "No JSON part found in response");
                mainHandler.post(() -> callback.onError(
                        "Gagal membaca respons AI. Coba foto lain."));
                return;
            }

            // Bersihkan markdown
            String clean = jsonText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            int start = clean.indexOf('{');
            int end   = clean.lastIndexOf('}');
            if (start >= 0 && end > start)
                clean = clean.substring(start, end + 1);

            Log.d(TAG, "Parsed JSON: " + clean);

            JSONObject food = new JSONObject(clean);

            FoodAnalysisResult result = new FoodAnalysisResult();
            result.name            = food.optString("name",            "Makanan");
            result.foodNameEnglish = food.optString("foodNameEnglish", result.name);
            result.emoji           = food.optString("emoji",           "🍽️");
            result.portion         = food.optString("portion",         "1 Porsi");
            result.note            = food.optString("note",            "");
            result.weightGram      = food.optInt("weightGram", 200);
            result.calories        = food.optInt("calories",   300);
            result.protein         = food.optInt("protein",     10);
            result.carbs           = food.optInt("carbs",       40);
            result.fat             = food.optInt("fat",          8);

            mainHandler.post(() -> callback.onSuccess(result));

        } catch (Exception e) {
            Log.e(TAG, "Parse error: " + e.getMessage(), e);
            mainHandler.post(() -> callback.onError(
                    "Gagal membaca respons AI. Coba foto lain."));
        }
    }

    // ─── Prompt ──────────────────────────────────────────────────────────────

    private static String buildPrompt(String countryCode) {
        String region = "ID".equals(countryCode) ? "Indonesian" :
                "MY".equals(countryCode) ? "Malaysian" :
                        "US".equals(countryCode) || "GB".equals(countryCode) ? "Western" :
                                "local";

        return "You are a professional nutritionist AI specialized in " + region + " cuisine.\n" +
                "Analyze the food in this image carefully.\n\n" +
                "IMPORTANT: Return ONLY a valid JSON object. " +
                "No markdown, no explanation, no text before or after.\n\n" +
                "{\n" +
                "  \"name\": \"<local food name in Bahasa Indonesia if ID>\",\n" +
                "  \"foodNameEnglish\": \"<english name for database lookup>\",\n" +
                "  \"emoji\": \"<single food emoji>\",\n" +
                "  \"portion\": \"<e.g. 1 Porsi>\",\n" +
                "  \"note\": \"<one short sentence about the dish>\",\n" +
                "  \"weightGram\": <integer>,\n" +
                "  \"calories\": <integer>,\n" +
                "  \"protein\": <integer>,\n" +
                "  \"carbs\": <integer>,\n" +
                "  \"fat\": <integer>\n" +
                "}\n\n" +
                "Rules:\n" +
                "- All numeric values are plain integers, no units\n" +
                "- Estimate nutrition for the FULL visible portion\n" +
                "- Return ONLY the JSON object, nothing else";
    }
}