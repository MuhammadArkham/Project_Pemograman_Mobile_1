package com.fitme.app.ui.history;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import com.fitme.app.R;
import com.fitme.app.database.BmiEntity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

public class DeleteConfirmSheet {

    private final Context   ctx;
    private final BmiEntity item;
    private final Runnable  onConfirm;

    public DeleteConfirmSheet(Context ctx, BmiEntity item, Runnable onConfirm) {
        this.ctx       = ctx;
        this.item      = item;
        this.onConfirm = onConfirm;
    }

    public void show() {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        dialog.setContentView(R.layout.bottom_sheet_delete);

        TextView tvTitle    = dialog.findViewById(R.id.tvDeleteTitle);
        TextView tvSubtitle = dialog.findViewById(R.id.tvDeleteSubtitle);
        TextView tvBmi      = dialog.findViewById(R.id.tvDeleteBmi);
        TextView tvRange    = dialog.findViewById(R.id.tvDeleteRange);
        TextView tvMeta     = dialog.findViewById(R.id.tvDeleteMeta);
        TextView btnCancel  = dialog.findViewById(R.id.btnDeleteCancel);
        TextView btnConfirm = dialog.findViewById(R.id.btnDeleteConfirm);

        setText(tvTitle,    R.string.delete_title);
        setText(tvSubtitle, R.string.delete_subtitle);
        setText(btnCancel,  R.string.dialog_cancel);
        setText(btnConfirm, R.string.btn_delete);

        if (tvBmi != null)
            tvBmi.setText(String.format(Locale.US, "%.1f", item.bmi));

        if (tvRange != null) {
            tvRange.setText(rangeLabel(item.bmi));
            tvRange.setTextColor(accentColor(item.bmi));
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(40f);
            bg.setColor(badgeBg(item.bmi));
            tvRange.setBackground(bg);
        }

        if (tvMeta != null) {
            tvMeta.setText((item.date != null ? item.date : "")
                    + "   " + fmt(item.weight) + " kg"
                    + "   " + fmt(item.height) + " cm");
        }

        if (btnCancel  != null) btnCancel.setOnClickListener(v -> dialog.dismiss());
        if (btnConfirm != null) btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        });

        dialog.show();
    }

    private void setText(TextView tv, int resId) {
        if (tv != null) tv.setText(resId);
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