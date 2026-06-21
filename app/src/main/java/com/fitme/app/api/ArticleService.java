package com.fitme.app.api;

import android.util.Xml;
import com.fitme.app.database.ArticleEntity;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ArticleService {

    private static final String CNN_HEALTH = "https://www.cnnindonesia.com/gaya-hidup/rss";
    private static final String REPUBLIKA_LIFESTYLE = "https://www.republika.co.id/rss/gaya-hidup";
    private static final String ANTARA_TERKINI = "https://www.antaranews.com/rss/terkini.xml";
    private static final String SINDONEWS_LIFESTYLE = "https://lifestyle.sindonews.com/rss";
    private static final String MERDEKA_SEHAT = "https://www.merdeka.com/sehat/rss";
    private static final String TEMPO_GAYA = "https://rss.tempo.co/gaya";
    private static final String CNBC_LIFESTYLE = "https://www.cnbcindonesia.com/lifestyle/rss";
    private static final String OKEZONE_LIFESTYLE = "https://sindikasi.okezone.com/index.php/rss/13/RSS2.0";
    private static final String HIPWEE = "https://www.hipwee.com/feed/";
    private static final String TRIBUN_HEALTH = "https://health.tribunnews.com/rss";

    private static final FeedSource[][] CATEGORY_FEEDS = {
        {
            new FeedSource(MERDEKA_SEHAT, "Merdeka Sehat", "Kesehatan"),
            new FeedSource(CNN_HEALTH, "CNN Indonesia", "Kesehatan"),
            new FeedSource(ANTARA_TERKINI, "Antara News", "Kesehatan"),
            new FeedSource(REPUBLIKA_LIFESTYLE, "Republika", "Kesehatan"),
            new FeedSource(SINDONEWS_LIFESTYLE, "Sindonews", "Kesehatan"),
            new FeedSource(TEMPO_GAYA, "Tempo", "Kesehatan"),
            new FeedSource(CNBC_LIFESTYLE, "CNBC", "Kesehatan"),
            new FeedSource(OKEZONE_LIFESTYLE, "Okezone", "Kesehatan"),
            new FeedSource(HIPWEE, "Hipwee", "Kesehatan"),
            new FeedSource(TRIBUN_HEALTH, "Tribun Health", "Kesehatan")
        },
        {
            new FeedSource(MERDEKA_SEHAT, "Merdeka Sehat", "Nutrisi"),
            new FeedSource(CNN_HEALTH, "CNN Indonesia", "Nutrisi"),
            new FeedSource(SINDONEWS_LIFESTYLE, "Sindonews", "Nutrisi"),
            new FeedSource(REPUBLIKA_LIFESTYLE, "Republika", "Nutrisi"),
            new FeedSource(TEMPO_GAYA, "Tempo", "Nutrisi"),
            new FeedSource(HIPWEE, "Hipwee", "Nutrisi"),
            new FeedSource(TRIBUN_HEALTH, "Tribun Health", "Nutrisi")
        },
        {
            new FeedSource(CNN_HEALTH, "CNN Indonesia", "Olahraga"),
            new FeedSource(MERDEKA_SEHAT, "Merdeka Sehat", "Olahraga"),
            new FeedSource(ANTARA_TERKINI, "Antara News", "Olahraga"),
            new FeedSource(REPUBLIKA_LIFESTYLE, "Republika", "Olahraga"),
            new FeedSource(SINDONEWS_LIFESTYLE, "Sindonews", "Olahraga"),
            new FeedSource(TEMPO_GAYA, "Tempo", "Olahraga"),
            new FeedSource(CNBC_LIFESTYLE, "CNBC", "Olahraga")
        },
        {
            new FeedSource(MERDEKA_SEHAT, "Merdeka Sehat", "Mental Health"),
            new FeedSource(CNN_HEALTH, "CNN Indonesia", "Mental Health"),
            new FeedSource(SINDONEWS_LIFESTYLE, "Sindonews", "Mental Health"),
            new FeedSource(REPUBLIKA_LIFESTYLE, "Republika", "Mental Health"),
            new FeedSource(TEMPO_GAYA, "Tempo", "Mental Health"),
            new FeedSource(HIPWEE, "Hipwee", "Mental Health"),
            new FeedSource(TRIBUN_HEALTH, "Tribun Health", "Mental Health")
        },
        {
            new FeedSource(MERDEKA_SEHAT, "Merdeka Sehat", "Medis"),
            new FeedSource(CNN_HEALTH, "CNN Indonesia", "Medis"),
            new FeedSource(ANTARA_TERKINI, "Antara News", "Medis"),
            new FeedSource(REPUBLIKA_LIFESTYLE, "Republika", "Medis"),
            new FeedSource(TEMPO_GAYA, "Tempo", "Medis"),
            new FeedSource(OKEZONE_LIFESTYLE, "Okezone", "Medis"),
            new FeedSource(TRIBUN_HEALTH, "Tribun Health", "Medis")
        },
        {
            new FeedSource(CNN_HEALTH, "CNN Indonesia", "Gaya Hidup"),
            new FeedSource(SINDONEWS_LIFESTYLE, "Sindonews", "Gaya Hidup"),
            new FeedSource(MERDEKA_SEHAT, "Merdeka Sehat", "Gaya Hidup"),
            new FeedSource(REPUBLIKA_LIFESTYLE, "Republika", "Gaya Hidup"),
            new FeedSource(ANTARA_TERKINI, "Antara News", "Gaya Hidup"),
            new FeedSource(TEMPO_GAYA, "Tempo", "Gaya Hidup"),
            new FeedSource(CNBC_LIFESTYLE, "CNBC", "Gaya Hidup"),
            new FeedSource(OKEZONE_LIFESTYLE, "Okezone", "Gaya Hidup"),
            new FeedSource(HIPWEE, "Hipwee", "Gaya Hidup"),
            new FeedSource(TRIBUN_HEALTH, "Tribun Health", "Gaya Hidup")
        }
    };

    private static final int CAT_ALL = 0;
    private static final int CAT_NUTRITION = 1;
    private static final int CAT_FITNESS = 2;
    private static final int CAT_MENTAL = 3;
    private static final int CAT_MEDICAL = 4;
    private static final int CAT_LIFESTYLE = 5;

    private static final Set<String> KW_WHITE = new HashSet<>(Arrays.asList(
        "obesitas","kegemukan","kurus","berat badan","bmi","indeks massa tubuh","lemak","langsing","diet","metabolisme","ideal",
        "nutrisi","gizi","makanan sehat","kalori","protein","vitamin","mineral","sayur","buah","karbohidrat","gula","pola makan","vegan",
        "kolesterol","diabetes","gula darah","jantung","hipertensi","darah tinggi","kardiovaskular","stroke",
        "olahraga","kebugaran","otot","tulang","imun","stres","tidur","sehat","kesehatan","medis","dokter","terapi","suplemen",
        "nutrition","diet","fitness","workout","calorie","protein","vitamin","immune",
        "disease","medicine","medical","diabetes","cholesterol","obesity","cardiovascular",
        "antioxidant","supplement","health","wellness","exercise","mental","sleep"
    ));

    private static final Set<String> KW_BLACK = new HashSet<>(Arrays.asList(
        "politik","election","pemilu","presiden","gubernur","dpr","senator","congress",
        "war","perang","militer","military","nato","ukraine","russia","israel","gaza",
        "konflik","demonstrasi","unjuk rasa","aksi massa",
        "crypto","bitcoin","ethereum","blockchain","saham","stock market","forex",
        "economy","ekonomi","inflasi","inflation","recession","bank sentral","fed rate",
        "investasi","trading","bursa","rupiah","dolar",
        "celebrity","artis","gosip","scandal","entertainment","film","movie","musik",
        "music","konser","award","grammy","oscar","nonton","streaming","netflix",
        "youtube","influencer","selebgram","sinetron","ftv",
        "crime","kriminal","pembunuhan","murder","korupsi","corruption","penipuan","fraud",
        "pencurian","perampokan","penangkapan","polisi","kejaksaan","pengadilan",
        "gaming","gadget","smartphone","iphone","android phone","laptop","computer",
        "artificial intelligence","ai model","chatgpt","social media","tiktok","instagram",
        "facebook","twitter","meta","google","apple","microsoft","tesla",
        "sepak bola","liga","football league","nba","transfer pemain","world cup","piala dunia",
        "final","juara","liga champions","premier league","euro","olimpiade",
        "ulang tahun","baper","pacar","kekasih","asmara","menikah","pernikahan","cerai","perceraian",
        "selingkuh","hamil di luar nikah","skandal asmara","putus cinta","jomblo","kencan",
        "meninggal","pemakaman","duka cita","jenazah","makam","kuburan","artis","selebriti",
        "aktor","aktris","penyanyi","kucing","anjing","hewan peliharaan","film","musik","konser",
        "drama korea","drakor","kpop","sinetron","gossip","gosip","menangis","haru"
    ));

    private static final Set<String> KW_STRONG = new HashSet<>(Arrays.asList(
        // PENYAKIT & KONDISI TUBUH (Murni Fitness/Obesitas)
        "obesitas","kegemukan","bmi","indeks massa tubuh","kolesterol","diabetes","hipertensi",
        "kardiovaskular","jantung koroner","stroke","asam urat","gula darah",
        "tekanan darah","perut buncit","lemak perut","kelebihan berat badan",
        
        // GEJALA MASALAH BERAT BADAN
        "resistensi insulin","sleep apnea","sesak napas","mudah lelah","nyeri sendi",
        "kolesterol tinggi","mendengkur","ngorok",
        
        // NUTRISI & DIET (Super Ketat)
        "gizi","kalori","protein","vitamin","karbohidrat","metabolisme","antioksidan",
        "diet ketat","defisit kalori","bakar lemak","penurun berat badan","langsing","makanan sehat",
        
        // OLAH RAGA & KEBUGARAN (Super Ketat)
        "workout","senam","aerobik","angkat beban","pembentukan otot","massa otot",
        "kebugaran jasmani","bakar kalori"
    ));

    private static final Set<String> KW_WEAK = new HashSet<>(Arrays.asList(
        // KATA UMUM YANG HARUS MUNCUL BERSAMAAN (Minimal 3)
        "kesehatan","sehat","medis","dokter","klinik","terapi","suplemen",
        "olahraga","fitness","gym","yoga","lari","diet","vegan",
        "stres","depresi","psikologi","cemas","gejala","obesitas","berat badan"
    ));

    private final OkHttpClient client;
    private final ExecutorService executor;

    public interface ArticleCallback {
        void onSuccess(List<ArticleEntity> articles);
        void onError(String message);
    }

    public ArticleService() {
        Interceptor uaInterceptor = chain -> {
            Request req = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
                    .build();
            return chain.proceed(req);
        };

        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(uaInterceptor)
                .followRedirects(true)
                .retryOnConnectionFailure(true)
                .build();

        executor = Executors.newFixedThreadPool(4);
    }

    public void fetchArticlesByCategory(String category, ArticleCallback callback) {
        if (executor.isShutdown()) {
            callback.onError("Service is shutdown");
            return;
        }
        
        executor.execute(() -> {
            try {
                int categoryIndex = getCategoryIndex(category);
                FeedSource[] feeds = CATEGORY_FEEDS[categoryIndex];
                List<ArticleEntity> allArticles = fetchParallel(feeds);
                List<ArticleEntity> processed = processArticles(allArticles);
                callback.onSuccess(processed);
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
            }
        });
    }
    
    public void fetchHealthArticlesQuick(ArticleCallback callback) {
        fetchArticlesByCategory("all", callback);
    }
    
    public void fetchHealthArticles(ArticleCallback callback) {
        fetchArticlesByCategory("all", callback);
    }

    private List<ArticleEntity> fetchParallel(FeedSource[] feeds) {
        List<ArticleEntity> allArticles = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(feeds.length);
        
        for (FeedSource feed : feeds) {
            executor.execute(() -> {
                try {
                    List<ArticleEntity> articles = fetchSingleFeed(feed);
                    synchronized (allArticles) {
                        allArticles.addAll(articles);
                    }
                } catch (Throwable e) {
                    System.err.println("Error fetching " + feed.sourceName + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return allArticles;
    }

    private List<ArticleEntity> fetchSingleFeed(FeedSource feed) throws Exception {
        Request req = new Request.Builder()
                .url(feed.url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 Chrome/120")
                .addHeader("Accept", "application/rss+xml, application/xml, text/xml, */*")
                .build();
                
        try (Response r = client.newCall(req).execute()) {
            if (!r.isSuccessful()) {
                throw new Exception("HTTP " + r.code() + " for " + feed.sourceName);
            }
            
            ResponseBody body = r.body();
            if (body == null) {
                throw new Exception("Empty body from " + feed.sourceName);
            }
            
            return parseRSS(body.byteStream(), feed);
        }
    }

    private List<ArticleEntity> parseRSS(InputStream is, FeedSource feed) throws Exception {
        List<ArticleEntity> list = new ArrayList<>();
        XmlPullParser p = Xml.newPullParser();
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        p.setInput(is, null);

        ArticleEntity cur = null;
        boolean inItem = false;
        StringBuilder textBuilder = new StringBuilder();
        int ogFetchCount = 0; // Batasi scraping agar tidak timeout

        int evt = p.getEventType();
        while (evt != XmlPullParser.END_DOCUMENT) {
            String tag = p.getName();
            if (tag == null) tag = "";
            
            switch (evt) {
                case XmlPullParser.START_TAG:
                    if (tag.equalsIgnoreCase("item") || tag.equalsIgnoreCase("entry")) {
                        inItem = true;
                        cur = new ArticleEntity();
                        cur.source   = feed.sourceName;
                        cur.category = feed.defaultCategory;
                    } else if (inItem && cur != null) {
                        if (tag.equalsIgnoreCase("link")) {
                            String href = p.getAttributeValue(null, "href");
                            String rel  = p.getAttributeValue(null, "rel");
                            if (!isNullOrEmpty(href) && 
                                (rel == null || rel.equalsIgnoreCase("alternate") || rel.isEmpty()) &&
                                isNullOrEmpty(cur.link)) {
                                cur.link = href.trim();
                            }
                        }
                        
                        String tagLc = tag.toLowerCase(Locale.ROOT);
                        if (tagLc.contains("thumbnail") || tagLc.contains("content") || tagLc.contains("image")) {
                            String u = p.getAttributeValue(null, "url");
                            if (isNullOrEmpty(u)) u = p.getAttributeValue("", "url");
                            if (isNullOrEmpty(u)) u = p.getAttributeValue(null, "href");
                            if (isNullOrEmpty(u)) u = p.getAttributeValue(null, "src");
                            if (!isNullOrEmpty(u) && (u.endsWith(".jpg") || u.endsWith(".png") || u.endsWith(".jpeg") || u.endsWith(".webp") || u.contains("image") || u.contains("pict"))) {
                                if (isNullOrEmpty(cur.imageUrl)) {
                                    cur.imageUrl = toHttps(decodeHtml(u));
                                }
                            }
                        }
                        
                        if (tag.equalsIgnoreCase("enclosure")) {
                            String u = p.getAttributeValue(null, "url");
                            String t = p.getAttributeValue(null, "type");
                            if (!isNullOrEmpty(u) && 
                                (t == null || t.startsWith("image")) && 
                                isNullOrEmpty(cur.imageUrl)) {
                                cur.imageUrl = toHttps(decodeHtml(u));
                            }
                        }
                    }
                    textBuilder.setLength(0);
                    break;
                    
                case XmlPullParser.TEXT:
                    if (p.getText() != null) {
                        textBuilder.append(p.getText());
                    }
                    break;
                    
                case XmlPullParser.END_TAG:
                    String text = textBuilder.toString().trim();
                    if (!inItem || cur == null) break;
                    
                    switch (tag.toLowerCase(Locale.ROOT)) {
                        case "item":
                        case "entry":
                            if (!isNullOrEmpty(cur.link) && !isNullOrEmpty(cur.title)) {
                                // Ekstrak OG:Image untuk berita yang tidak menyediakan gambar di RSS
                                if (isNullOrEmpty(cur.imageUrl) && ogFetchCount < 10) {
                                    cur.imageUrl = fetchOgImage(cur.link);
                                    ogFetchCount++;
                                }
                                
                                // PRIORITAS 3: Fallback Unsplash bertema medis/fitness jika tak ada gambar
                                if (isNullOrEmpty(cur.imageUrl)) {
                                    cur.imageUrl = getFallbackImage(cur.title);
                                }
                                sanitize(cur);
                                list.add(cur);
                                if (list.size() >= 150) return list; // Ambil sebanyak mungkin sebelum difilter
                            }
                            inItem = false;
                            cur = null;
                            break;
                            
                        case "title":
                            if (isNullOrEmpty(cur.title)) {
                                cur.title = decodeHtml(text);
                            }
                            break;
                            
                        case "link":
                        case "guid":
                        case "id":
                            if (isNullOrEmpty(cur.link) && text.startsWith("http")) {
                                cur.link = text;
                            }
                            break;
                            
                        case "description":
                        case "summary":
                        case "content":
                        case "content:encoded":
                            if (isNullOrEmpty(cur.imageUrl)) {
                                String img = extractFirstImg(text);
                                if (img != null) {
                                    cur.imageUrl = toHttps(decodeHtml(img));
                                }
                            }
                            if (isNullOrEmpty(cur.description)) {
                                cur.description = truncate(stripHtml(text), 300);
                            }
                            break;
                            
                        case "pubdate":
                        case "published":
                        case "updated":
                        case "dc:date":
                            if (isNullOrEmpty(cur.pubDate)) {
                                cur.pubDate = parseDate(text);
                            }
                            break;
                    }
                    break;
            }
            evt = p.next();
        }
        return list;
    }

    private List<ArticleEntity> processArticles(List<ArticleEntity> raw) {
        List<ArticleEntity> unique = deduplicate(raw);
        List<ArticleEntity> filtered = new ArrayList<>();
        
        for (ArticleEntity a : unique) {
            String searchText = buildText(a);
            if (passesBlacklist(searchText) && passesWhitelist(searchText)) {
                if (isNullOrEmpty(a.category) || a.category.equals("Kesehatan")) {
                    a.category = detectCategory(searchText);
                }
                filtered.add(a);
            }
        }
        
        java.util.Collections.shuffle(filtered);
        return filtered.size() > 100 ? filtered.subList(0, 100) : filtered;
    }

    private boolean passesWhitelist(String text) {
        String lowerText = " " + text.toLowerCase(Locale.ROOT) + " ";
        int strongCount = 0;
        int weakCount = 0;

        for (String kw : KW_STRONG) {
            if (lowerText.contains(kw)) {
                strongCount++;
            }
        }
        
        for (String kw : KW_WEAK) {
            if (lowerText.contains(" " + kw + " ") || 
                lowerText.contains(" " + kw + ",") || 
                lowerText.contains(" " + kw + ".")) {
                weakCount++;
            }
        }

        // BERITA VALID JIKA: 
        // 1. Punya minimal 1 kata medis/diet BERAT (Strong)
        // 2. ATAU punya minimal 3 kata kesehatan RINGAN (Super Ketat)
        return strongCount >= 1 || weakCount >= 3;
    }

    private boolean passesBlacklist(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (String kw : KW_BLACK) {
            if (lowerText.contains(kw)) {
                return false;
            }
        }
        return true;
    }

    private String detectCategory(String text) {
        int diet = 0, nutrisi = 0, olahraga = 0, gayaHidup = 0, mental = 0;
        
        String[] dKw = {"diet","weight loss","kalori","keto","obesitas","langsing","berat badan","lemak","kegemukan","bmi"};
        String[] nKw = {"nutrisi","gizi","protein","vitamin","makanan sehat","mineral","buah","sayur","karbohidrat","pola makan","gula"};
        String[] oKw = {"olahraga","gym","lari","kebugaran","latihan","senam","yoga","otot","kardio"};
        String[] gKw = {"gaya hidup sehat","tidur","kebiasaan","pola tidur","metabolisme"};
        String[] mKw = {"stres","stress","depresi","cemas","kesehatan mental","psikologi"};
        
        for (String k : dKw) if (text.contains(k)) diet++;
        for (String k : nKw) if (text.contains(k)) nutrisi++;
        for (String k : oKw) if (text.contains(k)) olahraga++;
        for (String k : gKw) if (text.contains(k)) gayaHidup++;
        for (String k : mKw) if (text.contains(k)) mental++;
        
        int max = Math.max(diet, Math.max(nutrisi, Math.max(olahraga, Math.max(gayaHidup, mental))));
        
        if (max == 0)         return "Kesehatan";
        if (max == mental)    return "Mental Health";
        if (max == diet)      return "Diet";
        if (max == nutrisi)   return "Nutrisi";
        if (max == olahraga)  return "Olahraga";
        return "Gaya Hidup";
    }

    private String buildText(ArticleEntity a) {
        return ((a.title != null ? a.title : "") + " " +
                (a.description != null ? a.description : "") + " " +
                (a.source != null ? a.source : "")).toLowerCase(Locale.ROOT);
    }

    private List<ArticleEntity> deduplicate(List<ArticleEntity> list) {
        List<ArticleEntity> out = new ArrayList<>();
        Set<String> seenUrl = new HashSet<>();
        Set<String> seenTitle = new HashSet<>();
        
        for (ArticleEntity a : list) {
            String normTitle = a.title != null ? a.title.toLowerCase(Locale.ROOT).trim() : "";
            
            if (!isNullOrEmpty(a.link) && 
                seenUrl.add(a.link) && 
                (normTitle.isEmpty() || seenTitle.add(normTitle))) {
                out.add(a);
            }
        }
        return out;
    }

    private void sanitize(ArticleEntity a) {
        if (a.title != null) {
            a.title = decodeHtml(a.title).trim();
            int dash = a.title.lastIndexOf(" - ");
            if (dash > 10) {
                a.title = a.title.substring(0, dash).trim();
            }
        }
        
        if (a.description != null) {
            a.readTime = calculateReadingTime(a.description);
        } else {
            a.readTime = 2;
        }
        
        if (isNullOrEmpty(a.pubDate)) {
            a.pubDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date());
        }
    }

    private String decodeHtml(String s) {
        if (s == null) return "";
        return s.replace("&amp;","&").replace("&lt;","<").replace("&gt;",">")
                .replace("&quot;","\"").replace("&#39;","'").replace("&nbsp;"," ")
                .replace("&#8217;","'").replace("&#8216;","'").replace("&#8220;","\"")
                .replace("&#8221;","\"").replaceAll("&#\\d+;","")
                .replaceAll("<[^>]+>","").trim();
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>"," ").replaceAll("\\s+"," ").trim();
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max) + "…";
    }

    private String toHttps(String url) {
        if (url == null) return null;
        String httpsUrl = url.startsWith("http://") ? "https://" + url.substring(7) : url;
        
        // SERVER SINDONEWS MEMBLOKIR APLIKASI ANDROID (Dalvik User-Agent) DENGAN ERROR 403 FORBIDDEN.
        // Solusi: Menggunakan proxy gambar publik yang sangat cepat untuk mem-bypass blokir tersebut.
        if (httpsUrl.contains("sindonews.com") && !httpsUrl.contains("weserv.nl")) {
            return "https://images.weserv.nl/?url=" + httpsUrl.replace("https://", "").replace("http://", "");
        }
        return httpsUrl;
    }

    private int calculateReadingTime(String content) {
        if (isNullOrEmpty(content)) return 2;
        int words = content.split("\\s+").length;
        int avgWordsPerMinute = 200;
        return Math.max(1, words / avgWordsPerMinute);
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // FUNGSI REGEX EKSTRAKSI GAMBAR LAPIS BAJA
    private String extractImageUrlFromHtml(String html) {
        if (isNullOrEmpty(html)) return null;
        try {
            Matcher m = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.CASE_INSENSITIVE).matcher(html);
            if (m.find()) {
                String u = m.group(1);
                if (u != null && u.length() > 10) return u;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // SCRAPE METADATA GAMBAR DARI HALAMAN WEB ASLI (MENGGUNAKAN JSOUP SEBAGAI FALLBACK OG:IMAGE)
    private String fetchOgImage(String url) {
        if (isNullOrEmpty(url)) return null;
        try {
            // Jsoup otomatis akan melakukan HTTP GET dan mem-parsing struktur HTML secara clean
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(3000) // Timeout 3 detik
                    .followRedirects(true)
                    .get();
            
            // Mencari tag <meta property="og:image"> atau <meta name="twitter:image">
            String imgUrl = doc.select("meta[property=og:image]").attr("content");
            if (isNullOrEmpty(imgUrl)) {
                imgUrl = doc.select("meta[name=twitter:image]").attr("content");
            }
            
            // Cegah pengambilan logo Google News dari halaman redirect internal
            if (!isNullOrEmpty(imgUrl) && (imgUrl.contains("googleusercontent.com") || imgUrl.contains("gstatic.com") || imgUrl.contains("google.com"))) {
                return null;
            }
            
            if (!isNullOrEmpty(imgUrl)) {
                return imgUrl;
            }
        } catch (Throwable ignored) {
            // Jika gagal scrape (NoClassDefFoundError karena lupa Sync, atau timeout), otomatis di-handle getFallbackImage
        }
        return null;
    }

    private String extractFirstImg(String html) {
        if (isNullOrEmpty(html)) return null;
        try {
            Matcher m = Pattern.compile(
                "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", 
                Pattern.CASE_INSENSITIVE
            ).matcher(html);
            
            if (m.find()) {
                String u = m.group(1);
                if (u != null && u.length() > 10) {
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // FALLBACK GAMBAR JIKA ARTIKEL BENAR-BENAR TANPA GAMBAR (SMART CONTEXTUAL MAPPING)
    private String getFallbackImage(String title) {
        if (title == null) title = "";
        String t = title.toLowerCase(Locale.ROOT);
        
        // 1. Makanan / Nutrisi / Sayur / Buah
        if (t.contains("bawang") || t.contains("sayur") || t.contains("buah") || t.contains("makanan") || t.contains("nutrisi") || t.contains("diet") || t.contains("kalori") || t.contains("vitamin")) {
            return "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&q=80"; // Salad / Healthy Food
        }
        // 2. Wajah / Kulit / Rambut / Anti-Aging
        if (t.contains("wajah") || t.contains("kulit") || t.contains("rambut") || t.contains("kolagen") || t.contains("glowing") || t.contains("jerawat") || t.contains("awet muda")) {
            return "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=400&q=80"; // Skincare / Clean Face
        }
        // 3. Otak / Mental / Stres / Tidur
        if (t.contains("otak") || t.contains("stres") || t.contains("mental") || t.contains("cemas") || t.contains("depresi") || t.contains("tidur") || t.contains("pelupa") || t.contains("psikologi")) {
            return "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&q=80"; // Meditasi / Relaksasi
        }
        // 4. Medis / Penyakit / Hipertensi / Jantung
        if (t.contains("hipertensi") || t.contains("darah") || t.contains("jantung") || t.contains("penyakit") || t.contains("obat") || t.contains("dokter") || t.contains("gejala") || t.contains("kanker")) {
            return "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=400&q=80"; // Stetoskop / Peralatan Medis
        }
        // 5. Berat Badan / Obesitas / BMI / Timbangan
        if (t.contains("berat badan") || t.contains("obesitas") || t.contains("kurus") || t.contains("gemuk") || t.contains("bmi") || t.contains("timbangan") || t.contains("lemak")) {
            return "https://images.unsplash.com/photo-1522844990619-4951c40f7eda?w=400&q=80"; // Meteran / Timbangan / Diet
        }
        // 6. Anak / Bayi / Parenting
        if (t.contains("anak") || t.contains("bayi") || t.contains("balita") || t.contains("hamil") || t.contains("ibu")) {
            return "https://images.unsplash.com/photo-1519689680058-324335c77eba?w=400&q=80"; // Ibu & Anak
        }
        // 7. Olahraga / Kebugaran / Otot
        if (t.contains("olahraga") || t.contains("lari") || t.contains("otot") || t.contains("gym") || t.contains("kebugaran") || t.contains("latihan") || t.contains("olahragawan")) {
            return "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=400&q=80"; // Gym / Workout
        }
        
        // DEFAULT FALLBACK (Acak berdasarkan Judul)
        String[] fallbacks = {
            "https://images.unsplash.com/photo-1505576399279-565b52d4ac71?w=400&q=80",
            "https://images.unsplash.com/photo-1534258936925-c58bed479fcb?w=400&q=80",
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&q=80",
            "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=400&q=80",
            "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400&q=80"
        };
        int hash = Math.abs(title.hashCode());
        return fallbacks[hash % fallbacks.length];
    }

    private String parseDate(String raw) {
        if (isNullOrEmpty(raw)) return "";
        try {
            if (raw.contains(",")) {
                String[] p = raw.substring(raw.indexOf(',') + 1).trim().split("\\s+");
                if (p.length >= 3) {
                    return p[0] + " " + p[1] + " " + p[2];
                }
            }
            if (raw.contains("T")) {
                String norm = raw.length() > 19 ? raw.substring(0, 19) : raw;
                SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.US);
                in.setTimeZone(TimeZone.getTimeZone("UTC"));
                return out.format(in.parse(norm));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return raw.length() > 20 ? raw.substring(0, 20) : raw;
    }

    private int getCategoryIndex(String category) {
        if (category == null) return CAT_ALL;
        switch (category.toLowerCase(Locale.ROOT)) {
            case "nutrition": case "nutrisi": return CAT_NUTRITION;
            case "fitness": case "olahraga": case "exercise": return CAT_FITNESS;
            case "mental": case "mental_health": case "mental health": return CAT_MENTAL;
            case "medical": case "medis": case "penyakit": return CAT_MEDICAL;
            case "lifestyle": case "gaya_hidup": case "gaya hidup": return CAT_LIFESTYLE;
            default: return CAT_ALL;
        }
    }

    public void shutdown() {
        if (client != null) {
            client.dispatcher().cancelAll();
        }
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    private static final class FeedSource {
        final String url;
        final String sourceName;
        final String defaultCategory;
        
        FeedSource(String u, String s, String c) {
            url = u;
            sourceName = s;
            defaultCategory = c;
        }
    }
}