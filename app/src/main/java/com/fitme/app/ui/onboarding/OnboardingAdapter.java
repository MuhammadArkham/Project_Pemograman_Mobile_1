package com.fitme.app.ui.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitme.app.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageHolder> {

    private final Context              context;
    private final List<OnboardingPage> pages;
    private       RecyclerView         recyclerView;

    public OnboardingAdapter(Context context, List<OnboardingPage> pages) {
        this.context = context;
        this.pages   = pages;
    }

    // ─── RecyclerView lifecycle ───────────────────────────────────────────────

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);
        this.recyclerView = rv;
        rv.setItemAnimator(null); // Matikan animasi bawaan item RecyclerView
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView rv) {
        super.onDetachedFromRecyclerView(rv);
        this.recyclerView = null;
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new PageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder h, int position) {
        OnboardingPage page = pages.get(position);

        // Batalkan animasi yang mungkin masih berjalan
        h.ivIcon.animate().cancel();
        h.tvTitle.animate().cancel();
        h.tvDesc.animate().cancel();

        // Set konten
        h.ivIcon.setImageResource(page.iconRes);
        h.tvTitle.setText(page.title);
        h.tvDesc.setText(page.description);

        // ✅ Reset ke state invisible — TIDAK ada animasi di sini.
        // onBindViewHolder dipanggil kapan saja oleh RecyclerView (recycle, prefetch, dll).
        // Animasi masuk HANYA dijalankan oleh triggerAnimation() saat SCROLL_STATE_IDLE.
        applyResetState(h);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Jalankan animasi masuk untuk halaman di posisi tertentu.
     * Dipanggil dari Activity hanya saat SCROLL_STATE_IDLE,
     * sehingga animasi selalu dimulai setelah halaman benar-benar berhenti.
     *
     * ✅ TIDAK memanggil notifyItemChanged — tidak ada rebind, tidak ada flash.
     */
    public void triggerAnimation(int position) {
        PageHolder h = findHolder(position);
        if (h == null) return;

        // Cancel + reset dulu agar animasi selalu mulai dari titik yang sama
        h.ivIcon.animate().cancel();
        h.tvTitle.animate().cancel();
        h.tvDesc.animate().cancel();
        applyResetState(h);

        // ── Animasi 1: Gambar fade in + Ken Burns Effect (membesar perlahan) ──
        h.ivIcon.animate()
                .alpha(1f)
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(1200)
                .setStartDelay(0)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // ── Animasi 2: Judul masuk dari kiri ──────────────────────────────────
        h.tvTitle.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();

        // ── Animasi 3: Deskripsi masuk dari kiri ──────────────────────────────
        h.tvDesc.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(600)
                .setStartDelay(350)
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /** Cari ViewHolder aktif untuk posisi tertentu tanpa rebind. */
    private PageHolder findHolder(int position) {
        if (recyclerView == null) return null;
        RecyclerView.ViewHolder vh =
                recyclerView.findViewHolderForAdapterPosition(position);
        return (vh instanceof PageHolder) ? (PageHolder) vh : null;
    }

    /** Reset semua elemen ke state awal (invisible, di luar posisi). */
    private void applyResetState(PageHolder h) {
        h.ivIcon.setAlpha(0f);
        h.ivIcon.setScaleX(1.0f);
        h.ivIcon.setScaleY(1.0f);

        h.tvTitle.setAlpha(0f);
        h.tvTitle.setTranslationX(-60f);

        h.tvDesc.setAlpha(0f);
        h.tvDesc.setTranslationX(-60f);
    }

    // ─── Boilerplate ──────────────────────────────────────────────────────────

    @Override
    public int getItemCount() { return pages.size(); }

    static class PageHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView  tvTitle, tvDesc;

        PageHolder(View v) {
            super(v);
            ivIcon  = v.findViewById(R.id.ivOnboardIcon);
            tvTitle = v.findViewById(R.id.tvOnboardTitle);
            tvDesc  = v.findViewById(R.id.tvOnboardDesc);
        }
    }
}