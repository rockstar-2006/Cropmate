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

public class GeminiHelper {
    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GeminiHelper() {
        // Model version as requested (using 1.5 as it's the actual flash version)
        // Correct initialization for the Java SDK
        GenerativeModel gm = new GenerativeModel(
            "gemini-2.5-flash", 
            "AIzaSyBNWVRLWtlOuBgKhWmhA4gWh3USMI54luo"
        );
        this.model = GenerativeModelFutures.from(gm);
    }

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(Throwable throwable);
    }

    public void getRecommendation(String prompt, GeminiCallback callback) {
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text != null && !text.isEmpty()) {
                    callback.onSuccess(text);
                } else {
                    callback.onError(new Exception("AI response was empty"));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t);
            }
        }, executor);
    }
}
