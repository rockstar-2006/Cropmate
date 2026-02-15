package com.example.cropmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        CardView cardStartTest = findViewById(R.id.card_start_test);
        CardView cardManualInput = findViewById(R.id.card_manual_input);
        CardView cardHistory = findViewById(R.id.card_history);
        TextView tvStatus = findViewById(R.id.tv_status);

        // System is ready by default for demo
        tvStatus.setText("System Ready");

        cardStartTest.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, CropSelectionActivity.class);
            intent.putExtra("is_manual", false); // Indicate this is not a manual input
            startActivity(intent);
        });

        cardManualInput.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, ManualInputActivity.class);
            startActivity(intent);
        });

        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, HistoryActivity.class);
            startActivity(intent);
        });

        loadAITip();
        loadMarketPrices();

        Button btnScanLeaf = findViewById(R.id.btn_scan_leaf);
        btnScanLeaf.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, CropHospitalActivity.class);
            startActivity(intent);
        });
    }

    private void loadMarketPrices() {
        TextView tvPaddyPrice = findViewById(R.id.tv_price_paddy);
        TextView tvTomatoPrice = findViewById(R.id.tv_price_tomato);
        TextView tvSugarcanePrice = findViewById(R.id.tv_price_sugarcane);
        
        // Show immediate realistic prices (Karnataka average rates)
        tvPaddyPrice.setText("₹2,850");
        tvTomatoPrice.setText("₹1,200");
        tvSugarcanePrice.setText("₹3,500");
        
        // Try to fetch AI-updated prices in background (optional enhancement)
        // This runs async and updates if successful, but doesn't block the UI
        new Thread(() -> {
            try {
                GeminiHelper gemini = new GeminiHelper();
                String prompt = "Provide current wholesale market prices in Karnataka for: " +
                        "Paddy (quintal), Tomato (quintal), Sugarcane (ton). " +
                        "Format: Paddy:XXXX,Tomato:XXXX,Sugarcane:XXXX (numbers only)";
                
                gemini.getRecommendation(prompt, new GeminiHelper.GeminiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            try {
                                String cleaned = response.replaceAll("[^0-9,:]", "");
                                String[] items = cleaned.split(",");
                                
                                for (String item : items) {
                                    if (item.contains(":")) {
                                        String[] parts = item.split(":");
                                        if (parts.length == 2) {
                                            String price = parts[1];
                                            if (item.toLowerCase().contains("paddy") || items[0].equals(item)) {
                                                tvPaddyPrice.setText("₹" + formatPrice(price));
                                            } else if (item.toLowerCase().contains("tomato") || items[1].equals(item)) {
                                                tvTomatoPrice.setText("₹" + formatPrice(price));
                                            } else if (item.toLowerCase().contains("sugarcane") || items[2].equals(item)) {
                                                tvSugarcanePrice.setText("₹" + formatPrice(price));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Keep default values
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Keep default values - already set above
                    }
                });
            } catch (Exception e) {
                // Keep default values
            }
        }).start();
    }

    private String formatPrice(String price) {
        try {
            int p = Integer.parseInt(price);
            if (p > 100) {
                return String.format("%,d", p);
            }
        } catch (Exception e) {}
        return price;
    }

    private void loadAITip() {
        TextView tvTip = findViewById(R.id.tv_ai_tip);
        GeminiHelper gemini = new GeminiHelper();
        
        String lang = LocaleHelper.getLanguage(this);
        String prompt = "Give a 1-sentence farming advice for today. Location: Udupi, Karnataka. Weather: 28C, Partly Cloudy. " +
                (lang.equals("kn") ? "Respond in Kannada." : "Respond in English.");
        
        gemini.getRecommendation(prompt, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> tvTip.setText("AI ADVISORY: " + response.trim()));
            }

            @Override
            public void onError(Throwable throwable) {
                // fallback if AI fails or rate limited
            }
        });
    }
}

