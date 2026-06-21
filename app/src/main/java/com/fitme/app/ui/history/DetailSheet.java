package com.fitme.app.ui.history;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import com.fitme.app.R;
import com.fitme.app.database.BmiEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

public class DetailSheet {

    private final Context   ctx;
    private final BmiEntity item;

    public DetailSheet(Context ctx, BmiEntity item) {
        this.ctx  = ctx;
        this.item = item;
    }

    public void show() {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        dialog.setContentView(R.layout.bottom_sheet_detail);

        // ─── Views data ──────────────────────────────────────────
        TextView tvBmi    = dialog.findViewById(R.id.tvDetailBmi);
        TextView tvCat    = dialog.findViewById(R.id.tvDetailCat);
        TextView tvWeight = dialog.findViewById(R.id.tvDetailWeight);
        TextView tvHeight = dialog.findViewById(R.id.tvDetailHeight);
        TextView tvAge    = dialog.findViewById(R.id.tvDetailAge);
        TextView tvGender = dialog.findViewById(R.id.tvDetailGender);
        TextView tvIdeal  = dialog.findViewById(R.id.tvDetailIdeal);
        TextView tvDate   = dialog.findViewById(R.id.tvDetailDate);
        View     btnClose = dialog.findViewById(R.id.btnDetailClose);

        // ─── Views label statis ──────────────────────────────────
        TextView lblScore  = dialog.findViewById(R.id.lblDetailScore);
        TextView lblWeight = dialog.findViewById(R.id.lblDetailWeight);
        TextView lblHeight = dialog.findViewById(R.id.lblDetailHeight);
        TextView lblAge    = dialog.findViewById(R.id.lblDetailAge);
        TextView lblGender = dialog.findViewById(R.id.lblDetailGenderLabel);
        TextView lblIdeal  = dialog.findViewById(R.id.lblDetailIdeal);
        TextView lblDate   = dialog.findViewById(R.id.lblDetailDate);

        setText(lblScore,  R.string.lbl_bmi_score);
        setText(lblWeight, R.string.lbl_weight);
        setText(lblHeight, R.string.lbl_height);
        setText(lblAge,    R.string.lbl_age);
        setText(lblGender, R.string.lbl_gender);
        setText(lblIdeal,  R.string.lbl_ideal_weight);
        setText(lblDate,   R.string.lbl_date);
        if (btnClose instanceof TextView)
            ((TextView) btnClose).setText(R.string.btn_close);

        // ─── Isi data ────────────────────────────────────────────
        if (tvBmi != null) {
            tvBmi.setText(String.format(Locale.US, "%.1f", item.bmi));
            tvBmi.setTextColor(accentColor(item.bmi));
        }

        if (tvCat != null) {
            tvCat.setText(rangeLabel(item.bmi));
            tvCat.setTextColor(accentColor(item.bmi));
            tvCat.setBackground(pillDrawable(badgeBg(item.bmi)));
        }

        if (tvWeight != null) tvWeight.setText(fmt(item.weight) + " kg");
        if (tvHeight != null) tvHeight.setText(fmt(item.height) + " cm");

        if (tvAge != null)
            tvAge.setText(item.age + ctx.getString(R.string.unit_age_full));

        if (tvGender != null)
            tvGender.setText(normalizeGender(item.gender));

        if (tvDate != null)
            tvDate.setText(item.date != null ? item.date : "-");

        if (tvIdeal != null) {
            float h = item.height / 100f;
            tvIdeal.setText(String.format(Locale.US, "%.1f - %.1f kg",
                    18.5f * h * h, 24.9f * h * h));
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    /**
     * Normalisasi gender tersimpan ke bahasa aktif device.
     */
    private String normalizeGender(String raw) {
        if (raw == null) return "-";
        String lo = raw.toLowerCase(Locale.ROOT);

        // ✅ PERBAIKAN: Deteksi keyword yang lebih toleran terhadap berbagai variasi bahasa
        if (lo.contains("pria") || lo.contains("male") || lo.contains("hombre") || lo.contains("laki")) {
            return ctx.getString(R.string.gender_male_display);
        } else if (lo.contains("wanita") || lo.contains("female") || lo.contains("mujer") || lo.contains("perempuan")) {
            return ctx.getString(R.string.gender_female_display);
        }
        return raw;
    }

    private void setText(TextView tv, int resId) {
        if (tv != null) tv.setText(resId);
    }

    private GradientDrawable pillDrawable(int bgColor) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(40f);
        d.setColor(bgColor);
        return d;
    }

    private String rangeLabel(float bmi) {
        if (bmi < 18.5f) return "< 18.5";
        if (bmi < 25.0f) return "18.5 - 25";
        if (bmi < 30.0f) return "25 - 30";
        return "> 30";
    }

    private int accentColor(float bmi) {
        if (bmi < 18.5f) return Color.parseColor("#4A7A8A");
        if (bmi < 25.0f) return Color.parseColor("#3A7A55");
        if (bmi < 30.0f) return Color.parseColor("#8A7A40");
        return Color.parseColor("#C5786A");
    }

    private int badgeBg(float bmi) {
        if (bmi < 18.5f) return Color.parseColor("#ECF4F6");
        if (bmi < 25.0f) return Color.parseColor("#E4F2E9");
        if (bmi < 30.0f) return Color.parseColor("#F5F2E4");
        return Color.parseColor("#F5ECEB");
    }

    private String fmt(float v) {
        return v % 1 == 0 ? String.valueOf((int) v) : String.valueOf(v);
    }
}