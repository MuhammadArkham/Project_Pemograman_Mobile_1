package com.fitme.app.ui.history;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitme.app.R;
import com.fitme.app.database.BmiEntity;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    public interface OnDeleteClick { void onDelete(BmiEntity item); }
    public interface OnDetailClick { void onDetail(BmiEntity item); }

    // ── Warna badge foreground ────────────────────────────────────────────
    private static final int C_UNDER  = 0xFF185FA5;
    private static final int C_NORMAL = 0xFF0F6E56;
    private static final int C_OVER   = 0xFF854F0B;
    private static final int C_OBESE  = 0xFFA32D2D;

    // ── Warna badge background ────────────────────────────────────────────
    private static final int BG_UNDER  = 0xFFE6F1FB;
    private static final int BG_NORMAL = 0xFFE1F5EE;
    private static final int BG_OVER   = 0xFFFAEEDA;
    private static final int BG_OBESE  = 0xFFFCEBEB;

    // ── Warna Teks BMI dinamis sesuai kategori ────────────────────────────
    private static final int ACC_UNDER  = 0xFF378ADD;
    private static final int ACC_NORMAL = 0xFF1D9E75;
    private static final int ACC_OVER   = 0xFFEF9F27;
    private static final int ACC_OBESE  = 0xFFE24B4A;

    private final Context       ctx;
    private List<BmiEntity>     list;
    private final OnDeleteClick onDelete;
    private final OnDetailClick onDetail;
    private boolean             showNewestBadge = true;

    public HistoryAdapter(Context ctx, List<BmiEntity> list,
                          OnDeleteClick onDelete, OnDetailClick onDetail) {
        this.ctx      = ctx;
        this.list     = list;
        this.onDelete = onDelete;
        this.onDetail = onDetail;
    }

    public void setHistoryList(List<BmiEntity> newList, boolean showNewest) {
        this.list            = newList;
        this.showNewestBadge = showNewest;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx)
                .inflate(R.layout.item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        BmiEntity item  = list.get(pos);
        boolean   isNew = (pos == 0 && showNewestBadge);

        // Nilai BMI — warna dinamis sesuai kategori
        h.tvItemBmi.setText(String.format(Locale.US, "%.1f", item.bmi));
        h.tvItemBmi.setTextColor(accentColor(item.bmi));

        // Badge range (pill warna kategori di footer card)
        h.tvBadgeRange.setText(rangeLabel(item.bmi));
        h.tvBadgeRange.setTextColor(Color.WHITE);
        h.tvBadgeRange.setBackground(pillDrawable(accentColor(item.bmi), 0, 0));

        // Tanggal
        h.tvItemDate.setText(item.date != null ? item.date : "");

        // Badge "Terbaru"
        if (h.tvBadgeNew != null) {
            h.tvBadgeNew.setVisibility(isNew ? View.VISIBLE : View.GONE);
            if (isNew) {
                h.tvBadgeNew.setText(R.string.badge_latest);
                h.tvBadgeNew.setBackground(
                        pillDrawable(badgeBg(item.bmi), 1, badgeFg(item.bmi)));
                h.tvBadgeNew.setTextColor(badgeFg(item.bmi));
            }
        }

        // Info fisik
        String ageUnit    = ctx.getString(R.string.unit_age_short);
        String unitWeight = ctx.getString(R.string.unit_weight);
        String unitHeight = ctx.getString(R.string.unit_height);

        if (h.tvItemWeight != null) h.tvItemWeight.setText(fmt(item.weight) + " " + unitWeight);
        if (h.tvItemHeight != null) h.tvItemHeight.setText(fmt(item.height) + " " + unitHeight);
        if (h.tvItemAge    != null) h.tvItemAge.setText(item.age + " " + ageUnit);
        if (h.tvItemGender != null) h.tvItemGender.setText(item.gender != null ? item.gender : "—");

        // Tombol detail (hanya set click listener, teksnya sudah diurus oleh XML)
        if (h.btnDetail != null) {
            h.btnDetail.setOnClickListener(v -> onDetail.onDetail(item));
        }

        // Tombol hapus
        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> onDelete.onDelete(item));
        }
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    // ── Helpers ──────────────────────────────────────────────────────────

    private GradientDrawable pillDrawable(int bgColor, int strokeWidth, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(40f);
        d.setColor(bgColor);
        if (strokeWidth > 0) d.setStroke(strokeWidth, strokeColor);
        return d;
    }

    private String rangeLabel(float bmi) {
        if (bmi < 18.5f) return "Underweight  < 18.5";
        if (bmi < 25.0f) return "Normal  18.5 \u2013 25";
        if (bmi < 30.0f) return "Overweight  25 \u2013 30";
        return                   "Obese  > 30";
    }

    private int badgeFg(float bmi) {
        if (bmi < 18.5f) return C_UNDER;
        if (bmi < 25.0f) return C_NORMAL;
        if (bmi < 30.0f) return C_OVER;
        return C_OBESE;
    }

    private int badgeBg(float bmi) {
        if (bmi < 18.5f) return BG_UNDER;
        if (bmi < 25.0f) return BG_NORMAL;
        if (bmi < 30.0f) return BG_OVER;
        return BG_OBESE;
    }

    private int accentColor(float bmi) {
        if (bmi < 18.5f) return ACC_UNDER;
        if (bmi < 25.0f) return ACC_NORMAL;
        if (bmi < 30.0f) return ACC_OVER;
        return ACC_OBESE;
    }

    private String fmt(float v) {
        return v % 1 == 0 ? String.valueOf((int) v) : String.valueOf(v);
    }

    // ── ViewHolder ────────────────────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {

        // REVISI: Keduanya menggunakan View agar kebal terhadap perubahan tipe layout di XML
        View btnDetail;
        View btnDelete;

        TextView tvItemBmi, tvBadgeRange, tvBadgeNew, tvItemDate,
                tvItemWeight, tvItemHeight, tvItemAge, tvItemGender;

        VH(View v) {
            super(v);
            tvItemBmi    = v.findViewById(R.id.tvItemBmi);
            tvBadgeRange = v.findViewById(R.id.tvBadgeRange);
            tvBadgeNew   = v.findViewById(R.id.tvBadgeNew);
            tvItemDate   = v.findViewById(R.id.tvItemDate);
            tvItemWeight = v.findViewById(R.id.tvItemWeight);
            tvItemHeight = v.findViewById(R.id.tvItemHeight);
            tvItemAge    = v.findViewById(R.id.tvItemAge);
            tvItemGender = v.findViewById(R.id.tvItemGender);

            btnDetail    = v.findViewById(R.id.btnDetail);
            btnDelete    = v.findViewById(R.id.btnDelete);
        }
    }
}