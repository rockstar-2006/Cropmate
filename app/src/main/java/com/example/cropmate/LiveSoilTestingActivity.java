package com.example.cropmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class LiveSoilTestingActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private LinearLayout sensorContainer;
    private View cardCompleted;
    private Button btnViewReport;
    private String selectedCrop;
    private int selectedDepth;

    // TO MANIPULATE VALUES ACROSS DEVICES:
    // Set the same SEED_OFFSET on both devices and they will show the same values!
    private static final long SEED_OFFSET = 12345; 

    private int[] sensorStringIds = {
            R.string.sensor_temp,
            R.string.sensor_moisture,
            R.string.sensor_ph,
            R.string.sensor_ec,
            R.string.sensor_n,
            R.string.sensor_p,
            R.string.sensor_k
    };

    private int[] finalValues = new int[sensorStringIds.length];
    private String[] units = {"°C", "%", "pH", "mS/cm", "mg/kg", "mg/kg", "mg/kg"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_testing);

        selectedCrop = getIntent().getStringExtra("crop_name");
        selectedDepth = getIntent().getIntExtra("crop_depth", 15);

        sensorContainer = findViewById(R.id.sensor_container);
        cardCompleted = findViewById(R.id.card_completed);
        btnViewReport = findViewById(R.id.btn_view_report);

        btnViewReport.setOnClickListener(v -> {
            Intent intent = new Intent(LiveSoilTestingActivity.this, AnalysisResultActivity.class);
            intent.putExtra("crop_name", selectedCrop);
            intent.putExtra("N", finalValues[4]);
            intent.putExtra("P", finalValues[5]);
            intent.putExtra("K", finalValues[6]);
            intent.putExtra("pH", (double)finalValues[2] / 10.0); // pH stored as int*10
            intent.putExtra("Moisture", finalValues[1]);
            startActivity(intent);
        });

        startSequentialTesting();
    }

    private void startSequentialTesting() {
        Handler handler = new Handler();
        for (int i = 0; i < sensorStringIds.length; i++) {
            final int index = i;
            handler.postDelayed(() -> addSensorAndAnimate(index), i * 1500L);
        }

        handler.postDelayed(() -> cardCompleted.setVisibility(View.VISIBLE), sensorStringIds.length * 1500L + 500);
    }

    private void addSensorAndAnimate(int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_sensor_status, sensorContainer, false);
        TextView tvName = view.findViewById(R.id.sensor_name);
        TextView tvValue = view.findViewById(R.id.sensor_value);
        ProgressBar progress = view.findViewById(R.id.sensor_progress);
        ImageView check = view.findViewById(R.id.sensor_check);

        tvName.setText(getString(sensorStringIds[index]));
        sensorContainer.addView(view);

        // Deterministic Sync Logic: 
        // Syncs every 30 seconds (30000ms)
        long timeWindow = System.currentTimeMillis() / 30000;
        long seed = timeWindow + SEED_OFFSET + index; // index ensures different sensors have different values
        Random random = new Random(seed);

        final int targetValue;
        if (index == 2) targetValue = 55 + random.nextInt(20); // pH 5.5 - 7.5
        else if (index == 0) targetValue = 20 + random.nextInt(15); // Temp
        else targetValue = 10 + random.nextInt(80);

        finalValues[index] = targetValue;

        if (index > 0) { // Don't animate first one which is just "Connected"
            new Thread(() -> {
                for (int val = 0; val <= targetValue; val++) {
                    final int currentVal = val;
                    runOnUiThread(() -> {
                        if (index == 2) tvValue.setText(String.format("%.1f %s", currentVal / 10.0, units[index]));
                        else tvValue.setText(currentVal + " " + units[index]);
                    });
                    try { Thread.sleep(20); } catch (InterruptedException e) {}
                }
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    check.setVisibility(View.VISIBLE);
                });
            }).start();
        } else {
            progress.setVisibility(View.GONE);
            check.setVisibility(View.VISIBLE);
            tvValue.setText("OK");
        }
    }
}
