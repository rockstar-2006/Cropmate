package com.example.cropmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CropHospitalActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 101;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private ImageView ivLeafPreview;
    private Button btnCapturePhoto, btnAnalyze;
    private CardView cardResult;
    private TextView tvResult;
    private GeminiHelper geminiHelper;
    private Bitmap capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_hospital);

        ivLeafPreview = findViewById(R.id.iv_leaf_preview);
        btnCapturePhoto = findViewById(R.id.btn_capture_photo);
        btnAnalyze = findViewById(R.id.btn_analyze_leaf);
        cardResult = findViewById(R.id.card_diagnosis_result);
        tvResult = findViewById(R.id.tv_diagnosis_result);

        geminiHelper = new GeminiHelper();

        btnCapturePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        btnAnalyze.setOnClickListener(v -> analyzeLeaf());
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            capturedImage = (Bitmap) data.getExtras().get("data");
            ivLeafPreview.setImageBitmap(capturedImage);
            btnAnalyze.setEnabled(true);
        }
    }

    private void analyzeLeaf() {
        cardResult.setVisibility(View.VISIBLE);
        tvResult.setText("AI is analyzing the leaf...");
        btnAnalyze.setEnabled(false);

        String lang = LocaleHelper.getLanguage(this);
        String prompt = "Analyze this plant leaf image. Identify any disease, pest damage, or nutrient deficiency. " +
                "Provide: 1) Disease/Issue name 2) Cause 3) Treatment recommendation. " +
                (lang.equals("kn") ? "Respond in Kannada." : "Respond in English.");

        geminiHelper.getRecommendation(prompt, new GeminiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    tvResult.setText(response);
                    btnAnalyze.setEnabled(true);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    tvResult.setText("Error: " + throwable.getMessage());
                    btnAnalyze.setEnabled(true);
                    Toast.makeText(CropHospitalActivity.this, 
                        "AI analysis failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
