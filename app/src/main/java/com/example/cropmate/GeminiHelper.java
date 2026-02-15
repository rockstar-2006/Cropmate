package com.example.cropmate;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.ai.client.generativeai.type.GenerationConfig;

public class GeminiHelper {
    private static final String[] API_KEYS = {
            "AIzaSyA0tXF_qsHvXLRKLOewe19iQDwRO-I-HbI",
            "AIzaSyCtTL6CDK15Q_SD0zXj_6BZberN3TxuaI0",
            "AIzaSyDCnLtVFc7i-2q-q7678gvgMYns87Pgejw"
    };
    private static int currentKeyIndex = 0;
    private GenerativeModelFutures model;

    public GeminiHelper() {
        initModel();
    }

    private void initModel() {
        // Updated to gemini-2.5-flash as requested. 
        // This is a high-capability model released in late 2025/early 2026.
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEYS[currentKeyIndex]);
        model = GenerativeModelFutures.from(gm);
    }

    private void rotateKey() {
        currentKeyIndex = (currentKeyIndex + 1) % API_KEYS.length;
        initModel();
    }

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(Throwable throwable);
    }

    public void getRecommendation(String prompt, GeminiCallback callback) {
        getRecommendationWithRetry(prompt, callback, 0);
    }

    private void getRecommendationWithRetry(String prompt, GeminiCallback callback, int attempt) {
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Executor executor = Executors.newSingleThreadExecutor();

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text != null && !text.isEmpty()) {
                    callback.onSuccess(text);
                } else {
                    callback.onError(new Exception("Empty response from AI"));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (attempt < API_KEYS.length - 1) {
                    android.util.Log.w("GeminiRetry", "Key failed, rotating and retrying in 2s... Attempt: " + (attempt + 1));
                    rotateKey();
                    // Delay retry by 2 seconds to let the server breathe
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        getRecommendationWithRetry(prompt, callback, attempt + 1);
                    }, 2000);
                } else {
                    callback.onError(t);
                }
            }
        }, executor);
    }
}
