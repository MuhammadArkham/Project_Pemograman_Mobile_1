package com.fitme.app.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitme.app.R;
import com.fitme.app.database.AppDatabase;
import com.fitme.app.database.BmiEntity;

import java.util.ArrayList; // PENTING: Jangan lupa import ArrayList
import java.util.List;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.rvHistory);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        ImageView btnBack = findViewById(R.id.btnBack);

        // --- BAGIAN YANG DIPERBAIKI ---
        // Kita masukkan "new ArrayList<>()" sebagai data awal (kosong)
        // Agar sesuai dengan permintaan Constructor: (List, Listener)
        adapter = new HistoryAdapter(new ArrayList<>(), new HistoryAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(BmiEntity item) {
                deleteHistoryItem(item);
            }
        });
        // -----------------------------

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Tombol Kembali
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Load Data
        loadHistoryData();
    }

    private void loadHistoryData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            // Pastikan nama method di DAO Anda benar (getAllBmiHistory atau getAll)
            // Jika merah, ganti dengan db.bmiDao().getAll();
            List<BmiEntity> data = db.bmiDao().getAllBmiHistory();

            runOnUiThread(() -> {
                if (data == null || data.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);

                    // Panggil method updateData yang baru kita buat di Adapter
                    adapter.updateData(data);
                }
            });
        });
    }

    private void deleteHistoryItem(BmiEntity item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.bmiDao().delete(item);

            runOnUiThread(() -> {
                Toast.makeText(this, "Data dihapus", Toast.LENGTH_SHORT).show();
                loadHistoryData(); // Refresh data setelah hapus
            });
        });
    }
}