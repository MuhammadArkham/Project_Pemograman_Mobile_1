package com.fitme.app.ui.makan;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fitme.app.R;
import com.fitme.app.database.FoodEntity;

import java.io.File;
import java.util.List;

public class FoodLogAdapter extends RecyclerView.Adapter<FoodLogAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(FoodEntity item, int position);
    }

    private final Context          context;
    private final List<FoodEntity> items;
    private final OnDeleteListener deleteListener;

    public FoodLogAdapter(Context context, List<FoodEntity> items,
                          OnDeleteListener deleteListener) {
        this.context        = context;
        this.items          = items;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_food_log, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        FoodEntity item = items.get(position);

        h.tvName.setText(item.name);

        // Waktu di atas, macro pills di bawah (terpisah)
        h.tvTime.setText(item.time != null ? item.time : "--:--");

        // Macro pills — masing-masing tampil terpisah
        if (h.tvMacroP != null) h.tvMacroP.setText("P " + item.protein + "g");
        if (h.tvMacroK != null) h.tvMacroK.setText("K " + item.carbs   + "g");
        if (h.tvMacroL != null) h.tvMacroL.setText("L " + item.fat     + "g");

        // Kalori
        h.tvCalories.setText(String.valueOf(item.calories));

        String calUnit = context.getString(R.string.food_calories);
        if (h.tvCalUnit != null) h.tvCalUnit.setText(calUnit);

        if (h.btnDelete != null)
            h.btnDelete.setContentDescription(context.getString(R.string.btn_delete));

        // Thumbnail atau emoji
        if (!TextUtils.isEmpty(item.imagePath)) {
            h.ivThumb.setVisibility(View.VISIBLE);
            h.tvEmoji.setVisibility(View.GONE);
            Glide.with(context)
                    .load(new File(item.imagePath))
                    .centerCrop()
                    .into(h.ivThumb);
        } else {
            h.ivThumb.setVisibility(View.GONE);
            h.tvEmoji.setVisibility(View.VISIBLE);
            h.tvEmoji.setText(item.emoji != null ? item.emoji : "🍽️");
        }

        h.btnDelete.setOnClickListener(v -> deleteListener.onDelete(item, position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void removeAt(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView   ivThumb;
        TextView    tvEmoji, tvName, tvTime, tvCalories, tvCalUnit;
        TextView    tvMacroP, tvMacroK, tvMacroL;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            ivThumb    = v.findViewById(R.id.iv_food_thumb);
            tvEmoji    = v.findViewById(R.id.tv_food_emoji);
            tvName     = v.findViewById(R.id.tv_food_name);
            tvTime     = v.findViewById(R.id.tv_food_time);
            tvCalories = v.findViewById(R.id.tv_food_calories);
            tvCalUnit  = v.findViewById(R.id.tv_food_cal_unit);
            tvMacroP   = v.findViewById(R.id.tv_macro_p);
            tvMacroK   = v.findViewById(R.id.tv_macro_k);
            tvMacroL   = v.findViewById(R.id.tv_macro_l);
            btnDelete  = v.findViewById(R.id.btn_delete);
        }
    }
}