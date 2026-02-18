package com.example.cropmate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalysisResultActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    
    private TabLayout tabLayout;
    private LinearLayout layoutSoilHealth, layoutFertilizer, layoutInsect, layoutDisease, layoutChat;
    private String cropName;
    private int valN, valP, valK, moisture;
    private double valPH;
    private GeminiHelper geminiHelper;
    private TextToSpeech tts;

    // Chat components
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend, btnMic;
    private List<ChatMessage> chatHistory = new ArrayList<>();
    private ChatAdapter chatAdapter;

    private static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_results);

        cropName = getIntent().getStringExtra("crop_name");
        valN = getIntent().getIntExtra("N", 40);
        valP = getIntent().getIntExtra("P", 20);
        valK = getIntent().getIntExtra("K", 35);
        valPH = getIntent().getDoubleExtra("pH", 6.5);
        moisture = getIntent().getIntExtra("Moisture", 60);

        geminiHelper = new GeminiHelper();
        tts = new TextToSpeech(this, this);

        tabLayout = findViewById(R.id.tab_layout);
        layoutSoilHealth = findViewById(R.id.layout_soil_health);
        layoutFertilizer = findViewById(R.id.layout_fertilizer);
        layoutInsect = findViewById(R.id.layout_insect);
        layoutDisease = findViewById(R.id.layout_disease);
        layoutChat = findViewById(R.id.layout_chat);

        setupTabs();
        setupSoilHealthTab();
        runMasterAnalysis();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: showLayout(layoutSoilHealth); break;
                    case 1: showLayout(layoutFertilizer); break;
                    case 2: showLayout(layoutInsect); break;
                    case 3: showLayout(layoutDisease); break;
                    case 4: 
                        showLayout(layoutChat);
                        if (chatHistory.isEmpty()) setupChatTab();
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            String lang = LocaleHelper.getLanguage(this);
            int result = tts.setLanguage(new Locale(lang));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English if Kannada TTS not available
                tts.setLanguage(Locale.US);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_soil_health));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_fertilizer));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_insect));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_disease));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_chat));
    }

    private void showLayout(View layout) {
        layoutSoilHealth.setVisibility(View.GONE);
        layoutFertilizer.setVisibility(View.GONE);
        layoutInsect.setVisibility(View.GONE);
        layoutDisease.setVisibility(View.GONE);
        layoutChat.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
    }

    private void setupSoilHealthTab() {
        addResultCard(layoutSoilHealth, getString(R.string.label_nitrogen), valN + " mg/kg", valN < 40 ? getString(R.string.status_low) : getString(R.string.status_optimal), valN < 40 ? R.color.red : R.color.status_ready);
        addResultCard(layoutSoilHealth, getString(R.string.label_phosphorus), valP + " mg/kg", valP < 25 ? getString(R.string.status_low) : getString(R.string.status_optimal), valP < 25 ? R.color.red : R.color.status_ready);
        addResultCard(layoutSoilHealth, getString(R.string.label_potassium), valK + " mg/kg", valK < 40 ? getString(R.string.status_low) : getString(R.string.status_optimal), valK < 40 ? R.color.red : R.color.status_ready);
        addResultCard(layoutSoilHealth, getString(R.string.label_ph), String.format("%.1f", valPH), valPH < 6.0 || valPH > 7.5 ? getString(R.string.status_imbalanced) : getString(R.string.status_optimal), valPH < 6.0 || valPH > 7.5 ? R.color.accent : R.color.status_ready);
        addResultCard(layoutSoilHealth, getString(R.string.label_moisture), moisture + " %", getString(R.string.status_good), R.color.status_ready);
    }

    private void addResultCard(ViewGroup parent, String label, String value, String status, int colorRes) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_result_card, parent, false);
        TextView tvLabel = card.findViewById(R.id.tv_label);
        TextView tvVal = card.findViewById(R.id.tv_value_text);
        TextView tvStatus = card.findViewById(R.id.tv_status_badge);
        View indicator = card.findViewById(R.id.indicator_line);

        tvLabel.setText(label);
        tvVal.setText(value);
        tvStatus.setText(status);
        tvStatus.setTextColor(ContextCompat.getColor(this, colorRes));
        indicator.setBackgroundColor(ContextCompat.getColor(this, colorRes));

        parent.addView(card);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void runMasterAnalysis() {
        if (!isNetworkAvailable()) {
            fetchOfflineSection("FERT", layoutFertilizer);
            fetchOfflineSection("INSECT", layoutInsect);
            fetchOfflineSection("DISEASE", layoutDisease);
            return;
        }

        // SHOW ANALYZING STATE FOR ALL
        showAnalyzing(layoutFertilizer);
        showAnalyzing(layoutInsect);
        showAnalyzing(layoutDisease);

        String lang = LocaleHelper.getLanguage(this);
        String langInstruction = "Respond in " + (lang.equalsIgnoreCase("kn") ? "Kannada" : "English");
        String sensorData = String.format("N=%d, P=%d, K=%d, pH=%.1f, Moisture=%d%%", valN, valP, valK, valPH, moisture);

        String masterPrompt = "You are an expert agronomist. For the crop " + cropName + " and these sensor readings: " + sensorData + 
                ", provide three separate sections exactly as follows:\n" +
                "###FERT###\n[Fertilizer advice]\n" +
                "###INSECT###\n[2 major insects and prevention]\n" +
                "###DISEASE###\n[2 major diseases and control]\n" +
                langInstruction + ". Keep each section concise but informative.";

        geminiHelper.getRecommendation(masterPrompt, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    parseMasterResponse(response);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    showErrorWithRetry(layoutFertilizer, "FERT", true, 0, throwable.getMessage());
                    showErrorWithRetry(layoutInsect, "INSECT", false, 0, throwable.getMessage());
                    showErrorWithRetry(layoutDisease, "DISEASE", false, 0, throwable.getMessage());
                });
            }
        });
    }

    private void showAnalyzing(LinearLayout layout) {
        layout.removeAllViews();
        TextView tvStatus = new TextView(this);
        tvStatus.setText(getString(R.string.ai_analyzing));
        tvStatus.setPadding(0, 32, 0, 16);
        tvStatus.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.primary));
        layout.addView(tvStatus);
        
        android.widget.ProgressBar progress = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        layout.addView(progress);
    }

    private void parseMasterResponse(String response) {
        layoutFertilizer.removeAllViews();
        layoutInsect.removeAllViews();
        layoutDisease.removeAllViews();

        String fertText = "", insectText = "", diseaseText = "";

        try {
            if (response.contains("###FERT###")) {
                int start = response.indexOf("###FERT###") + 10;
                int end = response.indexOf("###INSECT###");
                if (end == -1) end = response.length();
                fertText = response.substring(start, end).trim();
            }
            if (response.contains("###INSECT###")) {
                int start = response.indexOf("###INSECT###") + 12;
                int end = response.indexOf("###DISEASE###");
                if (end == -1) end = response.length();
                insectText = response.substring(start, end).trim();
            }
            if (response.contains("###DISEASE###")) {
                int start = response.indexOf("###DISEASE###") + 13;
                diseaseText = response.substring(start).trim();
            }
        } catch (Exception e) {
            fertText = response; // Fallback
        }

        displayResult(layoutFertilizer, fertText.isEmpty() ? "No fertilizer data returned." : fertText, true);
        displayResult(layoutInsect, insectText.isEmpty() ? "No insect data returned." : insectText, false);
        displayResult(layoutDisease, diseaseText.isEmpty() ? "No disease data returned." : diseaseText, false);
    }
    
    private void fetchOfflineSection(String type, LinearLayout layout) {
        layout.removeAllViews();
        String lang = LocaleHelper.getLanguage(this);
        boolean isKannada = "kn".equals(lang);

        if (type.equals("FERT")) {
            StringBuilder advice = new StringBuilder();
            if (isKannada) {
                advice.append("ಹವಾಮಾನ ಮತ್ತು ಮಣ್ಣಿನ ಫಲವತ್ತತೆ (ಆಫ್‌ಲೈನ್ ವಿಶ್ಲೇಷಣೆ):\n\n");
                if (valN < 40) advice.append("● ಸಾರಜನಕ ಕಡಿಮೆಯಾಗಿದೆ. ಯೂರಿಯಾ ಅಥವಾ ಕಾಂಪೋಸ್ಟ್ ಬಳಸಿ.\n");
                if (valP < 25) advice.append("● ರಂಜಕ ಕಡಿಮೆಯಾಗಿದೆ. ಸಿಂಗಲ್ ಸೂಪರ್ ಫಾಸ್ಫೇಟ್ ಬಳಸಿ.\n");
                if (valK < 40) advice.append("● ಪೊಟ್ಯಾಸಿಯಮ್ ಕಡಿಮೆಯಾಗಿದೆ. ಪೊಟ್ಯಾಶ್ ಗೊಬ್ಬರ ಬಳಸಿ.\n");
                if (valN >= 40 && valP >= 25 && valK >= 40) advice.append("● ಮಣ್ಣಿನ ಪೋಷಕಾಂಶಗಳು ಉತ್ತಮ ಸ್ಥಿತಿಯಲ್ಲಿವೆ.\n");
            } else {
                advice.append("Offline Analysis Based on Sensor Data:\n\n");
                if (valN < 40) advice.append("● Nitrogen is LOW. Consider applying Urea or Nitrogen-rich compost.\n");
                if (valP < 25) advice.append("● Phosphorus is LOW. Apply diammonium phosphate (DAP) or Single Super Phosphate.\n");
                if (valK < 40) advice.append("● Potassium is LOW. Apply Muriate of Potash (MOP).\n");
                if (valN >= 40 && valP >= 25 && valK >= 40) advice.append("● Soil nutrients are at optimal levels for this crop.\n");
            }
            displayResult(layout, advice.toString(), false);
        } else {
            loadPestData(layout, isKannada, type);
        }
    }

    private void loadPestData(LinearLayout layout, boolean isKannada, String type) {
        try {
            InputStream is = getAssets().open("crop_knowledge.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray cropsArray = jsonObject.getJSONArray("crops");
            boolean found = false;

            String arrayKey = type.equalsIgnoreCase("DISEASE") ? "diseases" : "insects";

            for (int i = 0; i < cropsArray.length(); i++) {
                JSONObject cropObj = cropsArray.getJSONObject(i);
                if (cropObj.getString("name").equalsIgnoreCase(cropName)) {
                    found = true;
                    if (cropObj.has(arrayKey)) {
                         JSONArray items = cropObj.getJSONArray(arrayKey);
                         for(int j=0; j<items.length(); j++){
                             JSONObject item = items.getJSONObject(j);
                             String name = isKannada && item.has("name_kn") ? item.getString("name_kn") : item.getString("name");
                             String control = isKannada && item.has("control_kn") ? item.getString("control_kn") : item.getString("control");
                             addInfoCard(layout, name, control);
                         }
                    } else {
                        displayResult(layout, isKannada ? "ಮಾಹಿತಿ ಲಭ್ಯವಿಲ್ಲ" : "No specific data available for " + type.toLowerCase() + ".", false);
                    }
                    break;
                }
            }
            if (!found) {
                displayResult(layout, isKannada ? "ಈ ಬೆಳೆಗೆ ಆಫ್‌ಲೈನ್ ಡೇಟಾ ಲಭ್ಯವಿಲ್ಲ." : "No offline data for this crop.", false);
            }

        } catch (Exception e) {
            displayResult(layout, "Error loading offline data.", false);
        }
    }
    
    private void fetchSection(String type, LinearLayout layout, boolean showSpeak, int delayMs) {
        layout.removeAllViews(); 
        TextView tvStatus = new TextView(this);
        tvStatus.setText(getString(R.string.ai_analyzing));
        tvStatus.setPadding(0, 32, 0, 16);
        tvStatus.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.primary));
        layout.addView(tvStatus);
        
        android.widget.ProgressBar progress = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        layout.addView(progress);

        String lang = LocaleHelper.getLanguage(this);
        String langInstruction = "Respond briefly in " + (lang.equalsIgnoreCase("kn") ? "Kannada" : "English");
        
        String sensorData = String.format("Sensor Data: N=%d, P=%d, K=%d, pH=%.1f, Moisture=%d%%.", valN, valP, valK, valPH, moisture);
        
        String prompt = "";
        if (type.equals("FERT")) {
            prompt = "Short expert fertilizer advice for " + cropName + " based on " + sensorData + " " + langInstruction;
        } else if (type.equals("INSECT")) {
            prompt = "List 2 potential insects for " + cropName + " considering " + sensorData + " and give short prevention. " + langInstruction;
        } else {
            prompt = "List 2 potential diseases for " + cropName + " considering " + sensorData + " and give short control. " + langInstruction;
        }

        final String finalPrompt = prompt;
        final LinearLayout finalLayout = layout;
        final boolean finalShowSpeak = showSpeak;

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            geminiHelper.getRecommendation(finalPrompt, new GeminiHelper.GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        finalLayout.removeAllViews();
                        displayResult(finalLayout, response, finalShowSpeak);
                    });
                }
                @Override
                public void onError(Throwable throwable) {
                    runOnUiThread(() -> {
                        finalLayout.removeAllViews();
                        showErrorWithRetry(finalLayout, type, finalShowSpeak, delayMs, throwable.getMessage());
                    });
                }
            });
        }, delayMs);
    }

    private void showErrorWithRetry(LinearLayout layout, String type, boolean showSpeak, int delayMs, String errorMsg) {
        TextView tvError = new TextView(this);
        tvError.setText("AI Error: " + (errorMsg.contains("429") ? "Quota Exceeded (Server Busy)" : "Service Unreachable"));
        tvError.setTextColor(ContextCompat.getColor(this, R.color.red));
        tvError.setPadding(0, 16, 0, 8);
        tvError.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        layout.addView(tvError);

        Button btnRetry = new Button(this);
        btnRetry.setText("RETRY AI ANALYSIS");
        btnRetry.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
        btnRetry.setTextColor(ContextCompat.getColor(this, R.color.white));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.CENTER;
        btnRetry.setLayoutParams(params);
        btnRetry.setOnClickListener(v -> fetchSection(type, layout, showSpeak, 0));
        layout.addView(btnRetry);

        Button btnOffline = new Button(this);
        btnOffline.setText("VIEW OFFLINE INFO");
        btnOffline.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey)));
        btnOffline.setTextColor(ContextCompat.getColor(this, R.color.white));
        params.topMargin = 16;
        btnOffline.setLayoutParams(params);
        btnOffline.setOnClickListener(v -> fetchOfflineSection(type, layout));
        layout.addView(btnOffline);
    }

    private void displayResult(LinearLayout layout, String text, boolean showSpeak) {
        if (text.isEmpty()) text = "Analysis data not generated properly.";
        
        if (showSpeak) {
            String speechText = text;
            Button btnSpeak = new Button(this);
            btnSpeak.setText(R.string.btn_speak);
            btnSpeak.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent)));
            btnSpeak.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnSpeak.setOnClickListener(v -> speak(speechText));
            layout.addView(btnSpeak);
        }

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setLineSpacing(0, 1.3f);
        tv.setPadding(0, 24, 0, 0);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        layout.addView(tv);
    }

    private void setupChatTab() {
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnMic = findViewById(R.id.btn_mic);

        chatAdapter = new ChatAdapter(chatHistory);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage(false)); // Normal send (no auto speak)
        
        // Microphone Logic
        btnMic.setOnClickListener(v -> startVoiceInput());

        chatHistory.add(ChatMessage.fromAI(getString(R.string.chat_welcome, cropName)));
        chatAdapter.notifyItemInserted(0);
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LocaleHelper.getLanguage(this));
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.voice_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etMessage.setText(result.get(0));
                sendMessage(true); // Auto-send and Auto-speak
            }
        }
    }

    private void sendMessage(boolean autoSpeak) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.msg_connect_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        String query = etMessage.getText().toString().trim();
        if (query.isEmpty()) return;

        chatHistory.add(new ChatMessage(query, true));
        chatAdapter.notifyItemInserted(chatHistory.size() - 1);
        
        String thinkingMsg = LocaleHelper.getLanguage(this).equalsIgnoreCase("kn") ? "ಆಲೋಚಿಸುತ್ತಿದೆ..." : "Thinking...";
        chatHistory.add(new ChatMessage(thinkingMsg, false));
        int thinkingPos = chatHistory.size() - 1;
        chatAdapter.notifyItemInserted(thinkingPos);
        
        rvChat.scrollToPosition(chatHistory.size() - 1);
        etMessage.setText("");

        String lang = LocaleHelper.getLanguage(this);
        String langInstruction = lang.equalsIgnoreCase("kn") ? "Respond in Kannada language." : "Respond in English language.";
        String prompt = "Act as an expert agronomist specialized in " + cropName + ". " + langInstruction + 
                       " The user asks: " + query + ". ONLY answer if it is about " + cropName + 
                       ". If not, politely say you only talk about " + cropName + ". Keep it helpful and concise.";

        geminiHelper.getRecommendation(prompt, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    chatHistory.remove(thinkingPos);
                    chatHistory.add(thinkingPos, new ChatMessage(response, false));
                    chatAdapter.notifyItemChanged(thinkingPos);
                    rvChat.scrollToPosition(chatHistory.size() - 1);
                    
                    if (autoSpeak) {
                        speak(response);
                    }
                });
            }
            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    chatHistory.remove(thinkingPos);
                    String errorMsg = throwable.getMessage();
                    String displayError = "AI Server busy/overloaded. Please Wait.";
                    if (errorMsg != null && errorMsg.contains("429")) {
                        displayError = "Quota exceeded! Please try again in 1 minute.";
                    }
                    chatHistory.add(thinkingPos, new ChatMessage(displayError, false));
                    chatAdapter.notifyItemChanged(thinkingPos);
                });
            }
        });
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void addInfoCard(ViewGroup parent, String title, String desc) {
        androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
        card.setRadius(24);
        card.setCardElevation(4);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(24, 24, 24, 24);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.primary));
        tvTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextSize(15);
        tvDesc.setLineSpacing(0, 1.2f);
        tvDesc.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvDesc.setPadding(0, 8, 0, 0);

        inner.addView(tvTitle);
        inner.addView(tvDesc);
        card.addView(inner);
        parent.addView(card);
    }
}
