package com.fitme.app.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.fitme.app.BuildConfig; // MENGAMBIL DARI BUILDCONFIG YANG AMAN

public class GeminiFoodHelper {

    private static final String TAG = "GeminiFoodHelper";

    // 1. Pecah string GEMINI_API_KEYS dari BuildConfig menjadi array berdasarkan koma
    private static final String[] API_KEYS = BuildConfig.GEMINI_API_KEYS.split(",");

    // Variabel statis untuk melacak index API Key yang sedang digunakan
    private static int currentKeyIndex = 0;

    // Rate Limiting
    private static long lastRequestTime = 0;
    private static final long DEBOUNCE_DELAY_MS = 5000;

    // Menggunakan model Flash Lite sesuai pengaturan dasbor Google AI Studio Anda
    private static final String MODEL = "gemini-2.5-flash-lite";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    public interface GeminiCallback {
        void onSuccess(String name, int calories, int protein, int carbs, int fat);
        void onError(String errorMsg);
    }

    // Mengambil URL dengan API Key yang aktif saat ini, ditambah .trim() untuk keamanan
    private static String getActiveApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent?key=" + API_KEYS[currentKeyIndex].trim();
    }

    public static void analyzeFood(Bitmap bitmap, GeminiCallback callback) {
        analyzeFood(bitmap, null, callback, 0);
    }

    public static void analyzeFood(Bitmap bitmap, String countryCode, GeminiCallback callback) {
        analyzeFood(bitmap, countryCode, callback, 0);
    }

    private static void analyzeFood(Bitmap bitmap, String countryCode, GeminiCallback callback, int retryCount) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        final String finalCountryCode = (countryCode == null || countryCode.isEmpty()) ? "ID" : countryCode;

        if (retryCount == 0 && System.currentTimeMillis() - lastRequestTime < DEBOUNCE_DELAY_MS) {
            Log.w(TAG, "Gemini Rate limit active. Tolak request beruntun.");
            mainHandler.post(() -> callback.onError(getErrorMsg("rate_limit", finalCountryCode)));
            return;
        }
        if (retryCount == 0) lastRequestTime = System.currentTimeMillis();

        Bitmap resized = resizeBitmap(bitmap, 512);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

        try {
            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);

            JSONObject imagePart = new JSONObject();
            imagePart.put("inline_data", inlineData);

            JSONObject textPart = new JSONObject();
            textPart.put("text", buildPrompt(finalCountryCode));

            JSONArray parts = new JSONArray();
            parts.put(textPart);
            parts.put(imagePart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("thinkingBudget", 0);

            JSONObject genConfig = new JSONObject();
            genConfig.put("temperature", 0.1);
            genConfig.put("maxOutputTokens", 256);
            genConfig.put("thinkingConfig", thinkingConfig);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);
            requestBody.put("generationConfig", genConfig);

            RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8"));

            // Panggil API menggunakan active URL (URL dengan key saat ini)
            Request request  = new Request.Builder().url(getActiveApiUrl()).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network failure", e);
                    mainHandler.post(() -> callback.onError(getErrorMsg("network", finalCountryCode) + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String rawBody = response.body() != null ? response.body().string() : "";
                    int code = response.code();

                    // Tangani Server Sibuk (503)
                    if (code == 503) {
                        if (retryCount < 2) {
                            mainHandler.postDelayed(() -> analyzeFood(bitmap, finalCountryCode, callback, retryCount + 1), 2000);
                        } else {
                            mainHandler.post(() -> callback.onError(getErrorMsg("network", finalCountryCode)));
                        }
                        return;
                    }

                    // TANGANI LIMIT (429) & ROTASI API KEY DENGAN SYNCHRONIZED
                    if (code == 429) {
                        if (retryCount < API_KEYS.length - 1) {

                            // Mencegah tabrakan (race condition) saat mengganti API Key
                            synchronized (GeminiFoodHelper.class) {
                                currentKeyIndex = (currentKeyIndex + 1) % API_KEYS.length;
                            }

                            Log.w(TAG, "API Key Limit. Memutar ke kunci index: " + currentKeyIndex);

                            // Coba ulang otomatis dengan kunci baru
                            mainHandler.post(() -> analyzeFood(bitmap, finalCountryCode, callback, retryCount + 1));
                        } else {
                            // Jika semua kunci sudah limit
                            mainHandler.post(() -> callback.onError(getErrorMsg("limit",  finalCountryCode)));
                        }
                        return;
                    }

                    if (code == 404) { mainHandler.post(() -> callback.onError(getErrorMsg("model",  finalCountryCode))); return; }

                    // Tangani Error "API key not valid" secara otomatis
                    if (!response.isSuccessful()) {
                        String msg = "Error " + code;
                        try {
                            JSONObject errorObj = new JSONObject(rawBody).optJSONObject("error");
                            if (errorObj != null) {
                                msg = errorObj.optString("message", msg);
                            }
                        } catch (Exception ignored) {}

                        // Jika kunci ditolak (karena kadaluarsa, salah, dll), lompat ke kunci berikutnya
                        if (msg.contains("API key not valid") && retryCount < API_KEYS.length - 1) {
                            synchronized (GeminiFoodHelper.class) {
                                currentKeyIndex = (currentKeyIndex + 1) % API_KEYS.length;
                            }
                            Log.w(TAG, "Kunci tidak valid. Mencoba kunci berikutnya di index: " + currentKeyIndex);
                            mainHandler.post(() -> analyzeFood(bitmap, finalCountryCode, callback, retryCount + 1));
                            return;
                        }

                        final String finalMsg = "Gemini: " + msg;
                        mainHandler.post(() -> callback.onError(finalMsg));
                        return;
                    }

                    try {
                        JSONObject json       = new JSONObject(rawBody);
                        JSONArray  candidates = json.getJSONArray("candidates");
                        JSONObject first      = candidates.getJSONObject(0);

                        String finishReason = first.optString("finishReason", "STOP");
                        if (!finishReason.equals("STOP") && !finishReason.equals("MAX_TOKENS")) {
                            mainHandler.post(() -> callback.onError(getErrorMsg("refused", finalCountryCode)));
                            return;
                        }

                        JSONArray partsList = first.getJSONObject("content").getJSONArray("parts");
                        String jsonText = null;
                        for (int i = 0; i < partsList.length(); i++) {
                            JSONObject p = partsList.getJSONObject(i);
                            if (p.optBoolean("thought", false)) continue;
                            String t = p.optString("text", "");
                            if (t.contains("{")) { jsonText = t; break; }
                        }

                        if (jsonText == null) { mainHandler.post(() -> callback.onError(getErrorMsg("parse", finalCountryCode))); return; }

                        String clean = jsonText.replace("```json", "").replace("```", "").trim();
                        int start = clean.indexOf('{'), end = clean.lastIndexOf('}');
                        if (start >= 0 && end > start) clean = clean.substring(start, end + 1);

                        JSONObject food = new JSONObject(clean);
                        String name    = food.optString("name",     "Food");
                        int calories   = food.optInt("calories",    200);
                        int protein    = food.optInt("protein",      10);
                        int carbs      = food.optInt("carbs",        25);
                        int fat        = food.optInt("fat",           7);

                        mainHandler.post(() -> callback.onSuccess(name, calories, protein, carbs, fat));

                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError(getErrorMsg("parse", finalCountryCode)));
                    }
                }
            });

        } catch (Exception e) {
            mainHandler.post(() -> callback.onError(getErrorMsg("image", finalCountryCode)));
        }
    }

    private static String buildPrompt(String countryCode) {
        String cc = (countryCode == null) ? "ID" : countryCode.toUpperCase();
        if (isEnglishCountry(cc)) {
            return "You are an expert nutritionist AI specializing in international and local cuisines. " +
                    "Analyze the food in this image carefully. " +
                    "Identify the food as accurately as possible. If it is a mixed dish, identify the dominant food. " +
                    "Return ONLY a valid JSON object — no markdown, no explanation, no extra text: " +
                    "{\"name\": \"Grilled Chicken Breast\", \"calories\": 165, \"protein\": 31, \"carbs\": 0, \"fat\": 4}. " +
                    "Rules: 1. 'name' must be in English, specific and descriptive. " +
                    "2. All nutrition values are PER 100 GRAMS. 3. All values must be integers. " +
                    "4. Use standard USDA nutrition references. 5. If the image is unclear, make your best reasonable estimate.";
        } else if (isSpanishCountry(cc)) {
            return "Eres un nutricionista AI experto en cocinas internacionales y locales. " +
                    "Analiza cuidadosamente el alimento en esta imagen. " +
                    "Identifica el alimento con la mayor precisión posible. Si es un plato mixto, identifica el alimento principal. " +
                    "Devuelve SOLO un objeto JSON válido — sin markdown, sin explicación, sin texto extra: " +
                    "{\"name\": \"Tacos de Pollo\", \"calories\": 218, \"protein\": 14, \"carbs\": 20, \"fat\": 8}. " +
                    "Reglas: 1. 'name' debe estar en español, específico y descriptivo. " +
                    "2. Todos los valores nutricionales son POR 100 GRAMOS. 3. Todos los valores deben ser enteros. " +
                    "4. Usa referencias nutricionales estándar. 5. Si la imagen no está clara, haz tu mejor estimación razonable.";
        } else {
            return "Kamu adalah AI nutrisionis ahli yang menguasai masakan Indonesia dan internasional. " +
                    "Analisis makanan dalam gambar ini dengan teliti. " +
                    "Identifikasi makanan seakurat mungkin. Jika makanan campuran, identifikasi komponen utamanya. " +
                    "Kembalikan HANYA objek JSON valid — tanpa markdown, tanpa penjelasan, tanpa teks lain: " +
                    "{\"name\": \"Nasi Goreng Spesial\", \"calories\": 182, \"protein\": 6, \"carbs\": 28, \"fat\": 6}. " +
                    "Aturan: 1. 'name' harus dalam Bahasa Indonesia, spesifik dan deskriptif. " +
                    "2. Semua nilai nutrisi adalah PER 100 GRAM. 3. Semua nilai harus bilangan bulat. " +
                    "4. Gunakan referensi nutrisi standar DKBM Indonesia. 5. Jika gambar kurang jelas, berikan estimasi terbaik.";
        }
    }

    private static String getErrorMsg(String type, String countryCode) {
        boolean en = isEnglishCountry(countryCode), es = isSpanishCountry(countryCode);
        switch (type) {
            case "network":  return en ? "Connection failed: "                                        : es ? "Conexión fallida: "                                           : "Koneksi gagal: ";
            case "limit":    return en ? "All AI keys reached their limits. Please try again tomorrow.": es ? "Todos los límites alcanzados. Intenta mañana."               : "Semua Kunci AI telah limit. Coba lagi besok.";
            case "rate_limit":return en ? "Please wait a moment before sending another request."       : es ? "Por favor, espere un momento antes de enviar otra solicitud." : "Tunggu sebentar sebelum mengirim permintaan lagi.";
            case "model":    return en ? "AI model is currently unavailable."                         : es ? "El modelo de IA no está disponible."                          : "Model AI tidak tersedia saat ini.";
            case "refused":  return en ? "AI could not analyze this image. Please try another photo." : es ? "La IA no pudo analizar esta imagen. Prueba con otra foto."    : "AI menolak analisis. Coba foto lain.";
            case "parse":    return en ? "Failed to read AI response. Please try another photo."      : es ? "No se pudo leer la respuesta de la IA. Prueba con otra foto." : "Gagal membaca respons AI. Coba foto lain.";
            case "image":    return en ? "Failed to process image."                                   : es ? "No se pudo procesar la imagen."                               : "Gagal memproses gambar.";
            default:         return en ? "An unknown error occurred."                                 : es ? "Ocurrió un error desconocido."                                : "Terjadi kesalahan tidak diketahui.";
        }
    }

    public static boolean isEnglishCountry(String cc) {
        if (cc == null) return false;
        switch (cc.toUpperCase()) {
            case "US": case "GB": case "AU": case "CA": case "NZ":
            case "IE": case "ZA": case "SG": case "PH": case "IN":
                return true;
            default: return false;
        }
    }

    public static boolean isSpanishCountry(String cc) {
        if (cc == null) return false;
        switch (cc.toUpperCase()) {
            case "ES": case "MX": case "AR": case "CO": case "PE":
            case "CL": case "VE": case "EC": case "BO": case "PY":
            case "UY": case "CR": case "GT": case "HN": case "SV":
            case "NI": case "PA": case "DO": case "CU": return true;
            default: return false;
        }
    }

    private static Bitmap resizeBitmap(Bitmap src, int maxSize) {
        int w = src.getWidth(), h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;
        float ratio = (float) maxSize / Math.max(w, h);
        return Bitmap.createScaledBitmap(src, Math.round(w * ratio), Math.round(h * ratio), true);
    }
}