package com.fitme.app.utils;

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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqHelper {

    private static final String TAG      = "GroqHelper";
    private static final String API_KEY  = ApiConfig.GROQ_API_KEY;
    private static final String ENDPOINT = ApiConfig.GROQ_ENDPOINT;
    private static final String MODEL    = ApiConfig.MODEL_TEXT;

    private static final Map<String, String> adviceCache = new HashMap<>();
    private static long lastRequestTime = 0;
    private static final long DEBOUNCE_DELAY_MS = 3000;

    public interface GroqCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void getHealthAdvice(
            float bmi,
            String category,
            String age,
            String gender,
            String country,
            String city,
            GroqCallback callback) {

        String code = (country != null) ? country.toUpperCase() : "ID";

        boolean isEn = "US".equals(code) || "GB".equals(code)
                || "AU".equals(code) || "CA".equals(code)
                || "NZ".equals(code);

        boolean isEs = "ES".equals(code) || "MX".equals(code)
                || "AR".equals(code) || "CO".equals(code)
                || "PE".equals(code) || "CL".equals(code);

        String langKey = isEn ? "en" : (isEs ? "es" : "id");

        String cityName = (city != null && !city.trim().isEmpty()) ? city.trim() : "";
        boolean hasCity = !cityName.isEmpty();

        String cacheKey = String.format(java.util.Locale.US, "%.1f_%s_%s_%s_%s_%s",
                bmi, category, age, gender, langKey, cityName);

        if (adviceCache.containsKey(cacheKey)) {
            Log.d(TAG, "Cache HIT: " + cacheKey);
            new Handler(Looper.getMainLooper()).post(
                    () -> callback.onSuccess(adviceCache.get(cacheKey)));
            return;
        }

        if (System.currentTimeMillis() - lastRequestTime < DEBOUNCE_DELAY_MS) {
            Log.w(TAG, "Rate limit active. Tolak request beruntun.");
            new Handler(Looper.getMainLooper()).post(() -> callback.onError("rate_limit"));
            return;
        }
        lastRequestTime = System.currentTimeMillis();

        Log.d(TAG, "Cache MISS — key: " + cacheKey);

        String bmiStr = String.format(java.util.Locale.US, "%.1f", bmi);

        int ageInt = 25;
        try { ageInt = Integer.parseInt(age); } catch (Exception ignored) {}

        String safeCity = cityName.replace("\"", "");
        String safeCountry = country != null ? country.replace("\"", "") : "";

        String idealRange;
        if      (ageInt < 18) idealRange = isEn ? "17.5–22.9" : (isEs ? "17.5–22.9" : "17,5–22,9");
        else if (ageInt < 35) idealRange = isEn ? "18.5–24.9" : (isEs ? "18.5–24.9" : "18,5–24,9");
        else if (ageInt < 60) idealRange = isEn ? "18.5–25.9" : (isEs ? "18.5–25.9" : "18,5–25,9");
        else                  idealRange = isEn ? "22.0–27.0" : (isEs ? "22.0–27.0" : "22,0–27,0");

        String systemPrompt;
        String userPrompt;

        if (isEs) {
            systemPrompt =
                    "Eres un coach de salud certificado en FITME. " +
                            "Educas al usuario sobre su IMC de forma clara, empática y motivadora. " +
                            "Usas datos reales del usuario para dar consejos específicos y personalizados. " +
                            "Nunca usas emojis ni primera persona. Respondes siempre en español con HTML simple.";

            String locationLine = hasCity
                    ? "• Ciudad: \"\"\"" + safeCity + ", " + safeCountry + "\"\"\"\n"
                    : "• País: \"\"\"" + safeCountry + "\"\"\"\n";

            userPrompt =
                    "Datos del usuario:\n" +
                            "• Género: " + gender + "\n" +
                            "• Edad: " + age + " años\n" +
                            "• IMC actual: " + bmiStr + " (" + category + ")\n" +
                            "• IMC ideal para esta edad: " + idealRange + "\n" +
                            locationLine + "\n" +
                            "Genera una evaluación EXACTAMENTE en este formato HTML:\n\n" +
                            "<b>Evaluación</b><br/>" +
                            "2 oraciones: qué significa IMC " + bmiStr + " para " + gender + " de " + age + " años y qué riesgo conlleva.\n\n" +
                            "<b>Objetivo IMC</b><br/>" +
                            "1 oración: rango ideal (" + idealRange + ") y cuánto peso ajustar aproximadamente.\n\n" +
                            "<b>Nutrición</b><br/>" +
                            "• Acción dietética específica y medible para este perfil.<br/>" +
                            "• Segunda acción dietética concreta.\n\n" +
                            "<b>Actividad Física</b><br/>" +
                            (hasCity
                                    ? "• Actividad específica con frecuencia, menciona lugares conocidos en " + cityName + " si los conoces.<br/>"
                                    : "• Actividad específica con frecuencia recomendada.<br/>") +
                            "• Segunda actividad o hábito físico diario.\n\n" +
                            "<b>Siguiente Paso</b><br/>" +
                            "1 frase motivadora mencionando el IMC objetivo.\n\n" +
                            "Sin corchetes. Sin texto fuera del formato. Máx 15 palabras por punto.";

        } else if (isEn) {
            systemPrompt =
                    "You are a certified health coach in the FITME app. " +
                            "You educate users about their BMI clearly, empathetically, and motivationally. " +
                            "You use real user data for specific, personalized advice. " +
                            "No emojis. No first-person phrases. Always reply in English with simple HTML.";

            String locationLine = hasCity
                    ? "• City: \"\"\"" + safeCity + ", " + safeCountry + "\"\"\"\n"
                    : "• Country: \"\"\"" + safeCountry + "\"\"\"\n";

            userPrompt =
                    "User data:\n" +
                            "• Gender: " + gender + "\n" +
                            "• Age: " + age + " years old\n" +
                            "• Current BMI: " + bmiStr + " (" + category + ")\n" +
                            "• Ideal BMI for this age: " + idealRange + "\n" +
                            locationLine + "\n" +
                            "Generate an assessment EXACTLY in this HTML format:\n\n" +
                            "<b>Assessment</b><br/>" +
                            "2 sentences: what BMI " + bmiStr + " means for a " + age + "-year-old " + gender + " and the health risk.\n\n" +
                            "<b>BMI Target</b><br/>" +
                            "1 sentence: ideal range (" + idealRange + ") and approximate weight adjustment needed.\n\n" +
                            "<b>Nutrition</b><br/>" +
                            "• Specific measurable dietary action for this profile.<br/>" +
                            "• Second specific dietary action.\n\n" +
                            "<b>Physical Activity</b><br/>" +
                            (hasCity
                                    ? "• Specific exercise with frequency, mention well-known sport venues in " + cityName + " if you know them.<br/>"
                                    : "• Specific exercise with recommended frequency.<br/>") +
                            "• Second exercise or daily physical habit.\n\n" +
                            "<b>Next Step</b><br/>" +
                            "1 motivating sentence mentioning the BMI target.\n\n" +
                            "No brackets. No text outside the format. Max 15 words per bullet.";

        } else {
            systemPrompt =
                    "Kamu adalah health coach bersertifikat di aplikasi FITME. " +
                            "Kamu mengedukasi pengguna tentang BMI mereka secara jelas, empatik, dan memotivasi. " +
                            "Kamu menggunakan data nyata pengguna untuk memberi saran spesifik dan personal. " +
                            "Tidak ada emoji. Tidak ada kalimat orang pertama. Selalu jawab dalam Bahasa Indonesia dengan HTML sederhana.";

            String locationLine = hasCity
                    ? "• Kota: \"\"\"" + safeCity + ", " + safeCountry + "\"\"\"\n"
                    : "• Negara: \"\"\"" + safeCountry + "\"\"\"\n";

            userPrompt =
                    "Data pengguna:\n" +
                            "• Jenis Kelamin: " + gender + "\n" +
                            "• Usia: " + age + " tahun\n" +
                            "• BMI saat ini: " + bmiStr + " (" + category + ")\n" +
                            "• BMI ideal untuk usia ini: " + idealRange + "\n" +
                            locationLine + "\n" +
                            "Buat penilaian kesehatan PERSIS dalam format HTML ini:\n\n" +
                            "<b>Kondisi Saat Ini</b><br/>" +
                            "2 kalimat: apa arti BMI " + bmiStr + " bagi " + gender + " usia " + age + " tahun dan risiko kesehatannya.\n\n" +
                            "<b>Target BMI Ideal</b><br/>" +
                            "1 kalimat: rentang ideal (" + idealRange + ") dan perkiraan berapa kg yang perlu disesuaikan.\n\n" +
                            "<b>Nutrisi</b><br/>" +
                            "• Tindakan pola makan spesifik dan terukur untuk profil ini.<br/>" +
                            "• Tindakan pola makan spesifik kedua.\n\n" +
                            "<b>Aktivitas Fisik</b><br/>" +
                            (hasCity
                                    ? "• Olahraga spesifik dengan frekuensi, sebutkan tempat olahraga yang dikenal di " + cityName + " jika kamu tahu.<br/>"
                                    : "• Olahraga spesifik dengan frekuensi yang direkomendasikan.<br/>") +
                            "• Olahraga atau kebiasaan fisik harian kedua.\n\n" +
                            "<b>Langkah Selanjutnya</b><br/>" +
                            "1 kalimat memotivasi yang menyebutkan target BMI.\n\n" +
                            "Tanpa tanda kurung. Tanpa teks di luar format. Maksimal 15 kata per poin.";
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30,    TimeUnit.SECONDS)
                .build();

        try {
            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "system"); sysMsg.put("content", systemPrompt);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user"); userMsg.put("content", userPrompt);

            JSONArray messages = new JSONArray();
            messages.put(sysMsg); messages.put(userMsg);

            JSONObject reqBody = new JSONObject();
            reqBody.put("model",       MODEL);
            reqBody.put("messages",    messages);
            reqBody.put("max_tokens",  500);
            reqBody.put("temperature", 0.25);

            RequestBody requestBody = RequestBody.create(
                    reqBody.toString(),
                    MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(ENDPOINT)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type",  "application/json")
                    .post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "onFailure: " + e.getMessage());
                    new Handler(Looper.getMainLooper()).post(
                            () -> callback.onError("network_error"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            new Handler(Looper.getMainLooper()).post(
                                    () -> callback.onError("server_error_" + response.code()));
                            return;
                        }
                        String raw = response.body().string();
                        String text = new JSONObject(raw)
                                .getJSONArray("choices").getJSONObject(0)
                                .getJSONObject("message").getString("content").trim();

                        String formatted = text
                                .replaceAll("(\r\n|\r|\n){2,}", "<br/>")
                                .replace("\n", "<br/>");

                        adviceCache.put(cacheKey, formatted);
                        new Handler(Looper.getMainLooper()).post(
                                () -> callback.onSuccess(formatted));
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(
                                () -> callback.onError("parse_error"));
                    }
                }
            });
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onError("build_error"));
        }
    }

    public static void clearCache() {
        adviceCache.clear();
    }
}