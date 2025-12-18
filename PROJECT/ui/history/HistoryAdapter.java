package com.fitme.app.ui.history;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fitme.app.R;
import com.fitme.app.database.BmiEntity;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<BmiEntity> bmiList;
    private OnDeleteClickListener listener;

    // Interface
    public interface OnDeleteClickListener {
        void onDeleteClick(BmiEntity item);
    }

    // Constructor
    public HistoryAdapter(List<BmiEntity> bmiList, OnDeleteClickListener listener) {
        this.bmiList = bmiList;
        this.listener = listener;
    }

    // --- [BAGIAN INI YANG DITAMBAHKAN] ---
    // Method ini wajib ada agar Activity bisa memasukkan data baru ke sini
    public void updateData(List<BmiEntity> newBmiList) {
        this.bmiList = newBmiList;
        notifyDataSetChanged(); // Memberitahu tampilan untuk refresh
    }
    // -------------------------------------

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BmiEntity item = bmiList.get(position);
        Context context = holder.itemView.getContext();

        // Set Data
        holder.tvDate.setText(item.date);
        holder.tvScore.setText(String.format("%.1f", item.bmi));

        // Logika Warna & Bahasa
        String labelStatus;
        int colorResId;

        if (item.bmi < 18.5) {
            labelStatus = context.getString(R.string.status_underweight);
            colorResId = android.R.color.holo_blue_light;
        } else if (item.bmi < 24.9) {
            labelStatus = context.getString(R.string.status_normal);
            colorResId = android.R.color.holo_green_dark;
        } else if (item.bmi < 29.9) {
            labelStatus = context.getString(R.string.status_overweight);
            colorResId = android.R.color.holo_orange_dark;
        } else {
            labelStatus = context.getString(R.string.status_obese);
            colorResId = android.R.color.holo_red_dark;
        }

        holder.tvCategory.setText(labelStatus);
        holder.tvCategory.setTextColor(ContextCompat.getColor(context, colorResId));

        // Logic Progress Bar
        holder.progressBar.setMax(40);
        holder.progressBar.setProgress((int) item.bmi);
        holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorResId)));

        // Tombol Delete
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bmiList != null ? bmiList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvScore, tvCategory;
        ProgressBar progressBar;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvScore = itemView.findViewById(R.id.tvHistoryScore);
            tvCategory = itemView.findViewById(R.id.tvHistoryCategory);
            progressBar = itemView.findViewById(R.id.progressBMI);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}