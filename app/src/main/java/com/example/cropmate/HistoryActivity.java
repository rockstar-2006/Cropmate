package com.example.cropmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private RecyclerView rvHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        List<Report> mockReports = new ArrayList<>();
        mockReports.add(new Report("Paddy", "Feb 10, 2026", "N:45 | P:25 | K:30", "Healthy - Continue routine"));
        mockReports.add(new Report("Tomato", "Feb 08, 2026", "N:10 | P:15 | K:20", "Nitrogen Deficient - Apply Urea"));
        mockReports.add(new Report("Sugarcane", "Jan 25, 2026", "N:60 | P:40 | K:50", "Optimal Nutrient Balance"));
        mockReports.add(new Report("Matta Gulla", "Jan 12, 2026", "N:30 | P:10 | K:15", "Low Potassium - Add Potash"));

        rvHistory.setAdapter(new HistoryAdapter(mockReports, r -> {
            Intent intent = new Intent(HistoryActivity.this, AnalysisResultActivity.class);
            intent.putExtra("crop_name", r.crop);
            // Splitting npk string "N:45 | P:25 | K:30" back to values for demo
            intent.putExtra("N", 45); 
            intent.putExtra("P", 25);
            intent.putExtra("K", 30);
            intent.putExtra("is_history", true);
            startActivity(intent);
        }));
    }

    private static class Report {
        String crop, date, npk, status;
        Report(String c, String d, String n, String s) {
            this.crop = c; this.date = d; this.npk = n; this.status = s;
        }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final List<Report> reports;
        private final OnReportClickListener listener;

        interface OnReportClickListener {
            void onReportClick(Report report);
        }

        HistoryAdapter(List<Report> reports, OnReportClickListener listener) { 
            this.reports = reports; 
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Report r = reports.get(position);
            holder.tvCrop.setText(r.crop);
            holder.tvDate.setText(r.date);
            holder.tvNPK.setText(r.npk);
            holder.tvStatus.setText("AI: " + r.status);
            holder.itemView.setOnClickListener(v -> listener.onReportClick(r));
        }

        @Override
        public int getItemCount() { return reports.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCrop, tvDate, tvNPK, tvStatus;
            ViewHolder(View itemView) {
                super(itemView);
                tvCrop = itemView.findViewById(R.id.tv_history_crop);
                tvDate = itemView.findViewById(R.id.tv_history_date);
                tvNPK = itemView.findViewById(R.id.tv_history_npk);
                tvStatus = itemView.findViewById(R.id.tv_history_status);
            }
        }
    }
}
