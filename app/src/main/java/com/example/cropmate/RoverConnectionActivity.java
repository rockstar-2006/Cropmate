package com.example.cropmate;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RoverConnectionActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    
    private View viewStatusDot;
    private TextView tvStatusText;
    private Button btnConnect, btnStartAnalysis;
    private LinearProgressIndicator loader;
    private String selectedCrop;
    private int selectedDepth;

    // Knowledge Base UI
    private TextView tvKnowledgeTitle, tvSoilReason, tvPestsTitle, tvFertilizerTitle, tvFertilizerInfo;
    private LinearLayout llPestsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rover_connection);

        selectedCrop = getIntent().getStringExtra("crop_name");
        selectedDepth = getIntent().getIntExtra("crop_depth", 15);

        TextView tvCropName = findViewById(R.id.tv_selected_crop_name);
        TextView tvDepth = findViewById(R.id.tv_depth_recommendation);
        
        tvCropName.setText(selectedCrop);
        tvDepth.setText("Recommended Soil Depth: " + selectedDepth + " cm");

        viewStatusDot = findViewById(R.id.view_status_dot);
        tvStatusText = findViewById(R.id.tv_rover_status_text);
        btnConnect = findViewById(R.id.btn_connect_rover);
        btnStartAnalysis = findViewById(R.id.btn_start_analysis);
        loader = findViewById(R.id.loader_connecting);

        // Knowledge Base Init
        tvKnowledgeTitle = findViewById(R.id.tv_knowledge_title);
        tvSoilReason = findViewById(R.id.tv_soil_reason);
        tvPestsTitle = findViewById(R.id.tv_pests_title);
        llPestsContainer = findViewById(R.id.ll_pests_container);
        tvFertilizerTitle = findViewById(R.id.tv_fertilizer_title);
        tvFertilizerInfo = findViewById(R.id.tv_fertilizer_info);

        if (isNetworkAvailable()) {
            fetchAIKnowledge(selectedCrop);
        } else {
            loadOfflineKnowledge(selectedCrop);
        }

        btnConnect.setOnClickListener(v -> startConnectingFlow());
        btnStartAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(RoverConnectionActivity.this, LiveSoilTestingActivity.class);
            intent.putExtra("crop_name", selectedCrop);
            intent.putExtra("crop_depth", selectedDepth);
            startActivity(intent);
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void fetchAIKnowledge(String cropName) {
        tvSoilReason.setText("Fetching AI insights (Online)...");
        tvFertilizerInfo.setText("Loading AI recommendations...");

        String lang = LocaleHelper.getLanguage(this);
        String languageName = lang.equals("kn") ? "Kannada" : "English";
        
        String prompt = "Act as an agricultural expert. For the crop '" + cropName + "', provide strictly valid JSON output with no markdown formatting. " +
                "The JSON structure must be: " +
                "{ \"depth_reason\": \"Short reason for " + selectedDepth + "cm soil depth\", " +
                "\"pests\": [{\"name\": \"Pest Name\", \"symptoms\": \"Symptoms\", \"control\": \"Control measures\"}, ... (limit to 3 major pests)], " +
                "\"fertilizer\": \"Detailed fertilizer recommendation (NPK) and application schedule.\" } " +
                "Respond in " + languageName + ".";

        GeminiHelper gemini = new GeminiHelper();
        gemini.getRecommendation(prompt, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        // Clean markdown code blocks if present
                        String jsonString = response.replace("```json", "").replace("```", "").trim();
                        JSONObject aiData = new JSONObject(jsonString);

                        // 1. Soil Reason
                        if (aiData.has("depth_reason")) {
                            tvSoilReason.setText(aiData.getString("depth_reason") + " (Source: AI)");
                        }

                        // 2. Pests
                        if (aiData.has("pests")) {
                            JSONArray pests = aiData.getJSONArray("pests");
                            llPestsContainer.removeAllViews();
                            boolean isKannada = "kn".equals(lang); // Use lang variable from outer scope logic or re-fetch
                            
                            for (int i = 0; i < pests.length(); i++) {
                                JSONObject pest = pests.getJSONObject(i);
                                String name = pest.optString("name", "Unknown");
                                String symptoms = pest.optString("symptoms", "");
                                String control = pest.optString("control", "");
                                addPestView(name, symptoms, control, isKannada);
                            }
                        }

                        // 3. Fertilizer
                        if (aiData.has("fertilizer")) {
                            tvFertilizerInfo.setText(aiData.getString("fertilizer"));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        tvSoilReason.setText("AI Parsing Error. Switching to Offline Mode.");
                        loadOfflineKnowledge(cropName); // Fallback
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    tvSoilReason.setText("AI Request Failed (" + throwable.getMessage() + "). Switching to Offline Mode.");
                    loadOfflineKnowledge(cropName); // Fallback
                });
            }
        });
    }

    private void loadOfflineKnowledge(String cropName) {
        try {
            InputStream is = getAssets().open("crop_knowledge.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray cropsArray = jsonObject.getJSONArray("crops");

            String lang = LocaleHelper.getLanguage(this);
            boolean isKannada = "kn".equals(lang);

            if (isKannada) {
                tvKnowledgeTitle.setText("ಬೆಳೆ ಮಾರ್ಗಸೂಚಿಗಳು ಮತ್ತು ಕೀಟಗಳು (ಆಫ್‌ಲೈನ್)");
                tvPestsTitle.setText("ಪ್ರಮುಖ ಕೀಟಗಳು ಮತ್ತು ನಿರ್ವಹಣೆ");
                tvFertilizerTitle.setText("ಗೊಬ್ಬರ ಶಿಫಾರಸು (ಆಫ್‌ಲೈನ್)");
                TextView tvDepthLabel = findViewById(R.id.tv_depth_recommendation);
                tvDepthLabel.setText("ಶಿಫಾರಸು ಮಾಡಿದ ಮಣ್ಣಿನ ಆಳ: " + selectedDepth + " cm");
            } else {
                 tvKnowledgeTitle.setText("Crop Guidelines & Pests (Offline)");
            }

            for (int i = 0; i < cropsArray.length(); i++) {
                JSONObject cropObj = cropsArray.getJSONObject(i);
                String name = cropObj.getString("name");

                if (name.equalsIgnoreCase(cropName)) {
                    // Populate Soil Depth Reason
                    String reasonKey = isKannada ? "depth_reason_kn" : "depth_reason";
                    String reason = cropObj.has(reasonKey) ? cropObj.getString(reasonKey) : cropObj.getString("depth_reason");
                    
                    tvSoilReason.setText(reason);

                    // Pests
                    if (cropObj.has("insects")) {
                        JSONArray insects = cropObj.getJSONArray("insects");
                        llPestsContainer.removeAllViews();

                        for (int j = 0; j < insects.length(); j++) {
                            JSONObject insect = insects.getJSONObject(j);
                            String iName = isKannada && insect.has("name_kn") ? insect.getString("name_kn") : insect.getString("name");
                            String iSymptoms = isKannada && insect.has("symptoms_kn") ? insect.getString("symptoms_kn") : insect.getString("symptoms");
                            String iControl = isKannada && insect.has("control_kn") ? insect.getString("control_kn") : insect.getString("control");

                            addPestView(iName, iSymptoms, iControl, isKannada);
                        }
                    }
                    
                    // Fertilizer - Offline placeholder
                    tvFertilizerInfo.setText(isKannada ? 
                        "ದಯವಿಟ್ಟು ಇಂಟರ್ನೆಟ್ ಸಂಪರ್ಕವನ್ನು ಪರಿಶೀಲಿಸಿ." : 
                        "Connect to internet for real-time fertilizer advice.");
                    return;
                }
            }
            tvSoilReason.setText(isKannada ? "ಮಾಹಿತಿ ಲಭ್ಯವಿಲ್ಲ." : "Detailed information not available for this crop yet.");

        } catch (Exception e) {
            e.printStackTrace();
            tvSoilReason.setText("Error loading data: " + e.getMessage());
        }
    }

    private void addPestView(String name, String symptoms, String control, boolean isKannada) {
        View pestView = LayoutInflater.from(this).inflate(R.layout.item_pest_detail, llPestsContainer, false);
        
        TextView tvName = pestView.findViewById(R.id.tv_pest_name);
        TextView tvSymptoms = pestView.findViewById(R.id.tv_pest_symptoms);
        TextView tvControl = pestView.findViewById(R.id.tv_pest_control);

        tvName.setText(name);
        if (isKannada) {
            tvSymptoms.setText("ಲಕ್ಷಣಗಳು: " + symptoms);
            tvControl.setText("ನಿರ್ವಹಣೆ: " + control);
        } else {
            tvSymptoms.setText("Symptoms: " + symptoms);
            tvControl.setText("Control: " + control);
        }

        llPestsContainer.addView(pestView);
    }

    private void startConnectingFlow() {
        btnConnect.setText("Connecting...");
        btnConnect.setEnabled(false);
        loader.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            viewStatusDot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_connecting)));
            tvStatusText.setText("Status: Establishing Link...");
        }, 1500);

        new Handler().postDelayed(() -> {
            viewStatusDot.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_ready)));
            tvStatusText.setText("Status: Rover Connected Successfully");
            loader.setVisibility(View.GONE);
            btnConnect.setVisibility(View.GONE);
            btnStartAnalysis.setVisibility(View.VISIBLE);
        }, 3000);
    }
}
