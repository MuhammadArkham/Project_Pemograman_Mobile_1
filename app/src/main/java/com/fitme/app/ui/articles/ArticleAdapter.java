package com.fitme.app.ui.articles;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fitme.app.R;
import com.fitme.app.database.ArticleEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ArticleAdapter — Adapter untuk RecyclerView artikel.
 *
 * Mendukung dua layout:
 *   - VIEW_TYPE_HORIZONTAL → item_article_horizontal.xml (card di Home)
 *   - VIEW_TYPE_VERTICAL   → item_article.xml (list di halaman Artikel)
 *
 * Aturan gambar:
 *   - imageUrl valid & berhasil load → tampilkan fl_image_container (VISIBLE)
 *   - imageUrl null/kosong → sembunyikan fl_image_container (GONE) → card text-only
 *   - imageUrl gagal load → sembunyikan fl_image_container (GONE) → card text-only
 *   - TIDAK menggunakan placeholder/fallback/default image
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    public static final int VIEW_TYPE_HORIZONTAL = 0;
    public static final int VIEW_TYPE_VERTICAL   = 1;

    public interface OnArticleClickListener {
        void onArticleClick(ArticleEntity article);
        void onBookmarkClick(ArticleEntity article);
    }

    private final Context context;
    private final int     viewType;
    private final OnArticleClickListener listener;

    private final List<ArticleEntity> originalList = new ArrayList<>();
    private final List<ArticleEntity> filteredList = new ArrayList<>();

    private String currentQuery    = "";
    private String currentCategory = "All";

    public ArticleAdapter(Context context, int viewType, OnArticleClickListener listener) {
        this.context  = context;
        this.viewType = viewType;
        this.listener = listener;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DATA
    // ─────────────────────────────────────────────────────────────────────────
    public void setArticles(List<ArticleEntity> articles) {
        synchronized(originalList) {
            originalList.clear();
            if (articles != null) originalList.addAll(articles);
        }
        applyFilter(currentQuery, currentCategory);
    }

    public interface FilterCallback {
        void onFilterComplete(int count);
    }
    private FilterCallback filterCallback;

    public void setFilterCallback(FilterCallback cb) {
        this.filterCallback = cb;
    }

    public void filter(String query, String category) {
        currentQuery    = query    != null ? query.trim()    : "";
        currentCategory = category != null ? category.trim() : "All";
        applyFilter(currentQuery, currentCategory);
    }

    private void applyFilter(String query, String category) {
        final List<ArticleEntity> oldList = new ArrayList<>(filteredList);
        new Thread(() -> {
            List<ArticleEntity> newList = new ArrayList<>();
            // originalList read is safe here since setArticles also runs on UI thread,
            // but to be absolutely safe against CME from rapid setArticles calls:
            List<ArticleEntity> originalCopy;
            synchronized(originalList) {
                originalCopy = new ArrayList<>(originalList);
            }
            for (ArticleEntity a : originalCopy) {
                if (matchesQuery(a, query) && matchesCategory(a, category)) newList.add(a);
            }
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new ArticleDiff(oldList, newList));
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                filteredList.clear();
                filteredList.addAll(newList);
                diff.dispatchUpdatesTo(this);
                if (filterCallback != null) {
                    filterCallback.onFilterComplete(newList.size());
                }
            });
        }).start();
    }

    private boolean matchesQuery(ArticleEntity a, String query) {
        if (query.isEmpty()) return true;
        String q = query.toLowerCase(Locale.ROOT);
        return (a.title != null && a.title.toLowerCase(Locale.ROOT).contains(q))
            || (a.description != null && a.description.toLowerCase(Locale.ROOT).contains(q));
    }

    private boolean matchesCategory(ArticleEntity a, String category) {
        if (category.equalsIgnoreCase("Tersimpan")) return a.isBookmarked;
        if (category.equalsIgnoreCase("All") || category.equalsIgnoreCase("Semua")) return true;
        if (a.category != null && a.category.equalsIgnoreCase(category)) return true;

        String text = ((a.title != null ? a.title : "") + " " +
                       (a.description != null ? a.description : "")).toLowerCase(Locale.ROOT);
        switch (category.toLowerCase(Locale.ROOT)) {
            case "nutrisi":
                return text.contains("nutrisi") || text.contains("nutrition") || text.contains("gizi")
                    || text.contains("vitamin") || text.contains("protein") || text.contains("makan")
                    || text.contains("food") || text.contains("meal");
            case "olahraga":
                return text.contains("olahraga") || text.contains("exercise") || text.contains("fitness")
                    || text.contains("workout") || text.contains("gym") || text.contains("sport")
                    || text.contains("cardio") || text.contains("yoga");
            case "diet":
                return text.contains("diet") || text.contains("weight") || text.contains("calorie")
                    || text.contains("kalori") || text.contains("keto") || text.contains("meal plan");
            case "gaya hidup":
                return text.contains("wellness") || text.contains("lifestyle") || text.contains("sleep")
                    || text.contains("tidur") || text.contains("stress") || text.contains("mental")
                    || text.contains("habit") || text.contains("gaya hidup");
            case "kesehatan":
                return text.contains("health") || text.contains("sehat") || text.contains("medical")
                    || text.contains("prevent") || text.contains("disease") || text.contains("immune");
            default: return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  RECYCLERVIEW
    // ─────────────────────────────────────────────────────────────────────────
    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
        int layout = (viewType == VIEW_TYPE_HORIZONTAL)
                ? R.layout.item_article_horizontal : R.layout.item_article;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        
        // Use the defined XML width instead of dynamic sizing for a compact, professional look
        // (This prevents the cards from being overly stretched on larger screens)
        
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        ArticleEntity a = filteredList.get(position);

        // ── RESET dulu sebelum bind (fix RecyclerView recycling bug) ──
        // Tanpa ini, card bergambar yang di-recycle untuk artikel tanpa gambar
        // akan tetap menampilkan gambar dari artikel sebelumnya
        if (holder.flImageContainer != null)
            holder.flImageContainer.setVisibility(View.GONE);
        if (holder.ivThumb != null)
            Glide.with(context).clear(holder.ivThumb);

        if (holder.tvTitle != null)  holder.tvTitle.setText(safe(a.title));
        if (holder.tvDate  != null)  holder.tvDate.setText(safe(a.pubDate, "Baru saja"));

        // Source badge — dalam gambar (tvSource)
        String sourceName = safe(a.source, "Health");
        if (holder.tvSource != null) holder.tvSource.setText(sourceName);

        if (holder.ivBookmark != null) {
            if (a.isBookmarked) {
                holder.ivBookmark.setImageResource(R.drawable.ic_heart_filled);
                holder.ivBookmark.setColorFilter(android.graphics.Color.parseColor("#E74C3C"));
            } else {
                holder.ivBookmark.setImageResource(R.drawable.ic_heart);
                holder.ivBookmark.setColorFilter(android.graphics.Color.parseColor("#A8C4BE"));
            }
            holder.ivBookmark.setOnClickListener(v -> {
                if (listener != null) listener.onBookmarkClick(a);
            });
        }

        // Deskripsi
        if (holder.tvDesc != null) {
            boolean hasDesc = a.description != null && !a.description.isEmpty();
            holder.tvDesc.setVisibility(hasDesc ? View.VISIBLE : View.GONE);
            if (hasDesc) holder.tvDesc.setText(a.description);
        }

        // ── GAMBAR: VISIBLE/GONE logic ──
        loadImage(holder, a);

        // Klik → delegate ke Activity via listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onArticleClick(a);
        });
    }

    @Override
    public int getItemCount() { return filteredList.size(); }

    // ─────────────────────────────────────────────────────────────────────────
    //  IMAGE LOADING — kunci dari fitur ini
    private void loadImage(@NonNull ArticleViewHolder holder, ArticleEntity a) {
        if (holder.flImageContainer == null) return;
        
        // Selalu tampilkan container
        holder.flImageContainer.setVisibility(View.VISIBLE);

        String url = a.imageUrl;
        if (url != null && url.startsWith("http://")) url = "https://" + url.substring(7);

        if (holder.ivThumb == null) return;

        Glide.with(context)
                .load(url)
                .centerCrop()
                .into(holder.ivThumb);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    // (openLink logic dipindahkan ke ArticlesActivity)

    private String safe(String s)              { return s != null && !s.isEmpty() ? s : ""; }
    private String safe(String s, String def)  { return s != null && !s.isEmpty() ? s : def; }

    // ─────────────────────────────────────────────────────────────────────────
    //  VIEWHOLDER
    // ─────────────────────────────────────────────────────────────────────────
    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flImageContainer;
        ImageView   ivThumb, ivBookmark;
        TextView    tvTitle, tvSource, tvDate, tvDesc;

        ArticleViewHolder(@NonNull View v) {
            super(v);
            flImageContainer = v.findViewById(R.id.fl_image_container);
            ivThumb          = v.findViewById(R.id.iv_article_thumb);
            tvTitle          = v.findViewById(R.id.tv_article_title);
            tvSource         = v.findViewById(R.id.tv_article_source);
            tvDate           = v.findViewById(R.id.tv_article_date);
            tvDesc           = v.findViewById(R.id.tv_article_desc);
            ivBookmark       = v.findViewById(R.id.iv_bookmark);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DIFFUTIL
    // ─────────────────────────────────────────────────────────────────────────
    private static class ArticleDiff extends DiffUtil.Callback {
        private final List<ArticleEntity> o, n;
        ArticleDiff(List<ArticleEntity> o, List<ArticleEntity> n) { this.o=o; this.n=n; }
        @Override public int getOldListSize() { return o.size(); }
        @Override public int getNewListSize() { return n.size(); }
        @Override public boolean areItemsTheSame(int op, int np) {
            String a=o.get(op).link, b=n.get(np).link;
            return a != null && a.equals(b);
        }
        @Override public boolean areContentsTheSame(int op, int np) {
            ArticleEntity a=o.get(op), b=n.get(np);
            return eq(a.title,b.title) && eq(a.imageUrl,b.imageUrl);
        }
        private boolean eq(String a, String b) {
            if (a==null&&b==null) return true;
            if (a==null||b==null) return false;
            return a.equals(b);
        }
    }
}