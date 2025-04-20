package com.example.cropmate;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    private CircularProgressIndicator progressNitrogen, progressPhosphorus, progressPotassium, progressPH;
    private TextView textNitrogen, textPhosphorus, textPotassium, textPH;
    private DatabaseReference sensorDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Initialize Firebase reference
        sensorDataRef = FirebaseDatabase.getInstance().getReference("SensorData/6141");

        // Initialize UI elements
        progressNitrogen = findViewById(R.id.progressNitrogen);
        textNitrogen = findViewById(R.id.textNitrogen);
        progressPhosphorus = findViewById(R.id.progressPhosphorus);
        textPhosphorus = findViewById(R.id.textPhosphorus);
        progressPotassium = findViewById(R.id.progressPotassium);
        textPotassium = findViewById(R.id.textPotassium);
        progressPH = findViewById(R.id.progressPH);
        textPH = findViewById(R.id.textPH);

        // Fetch data from Firebase
        sensorDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateProgress(progressNitrogen, textNitrogen, snapshot.child("N").getValue(Long.class), "N", 500);
                    updateProgress(progressPhosphorus, textPhosphorus, snapshot.child("P").getValue(Long.class), "P", 100);
                    updateProgress(progressPotassium, textPotassium, snapshot.child("K").getValue(Long.class), "K", 500);
                    updatePH(snapshot);

                } else {
                    textNitrogen.setText("No data found");
                    textPhosphorus.setText("No data found");
                    textPotassium.setText("No data found");
                    textPH.setText("No data found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseError", "Error fetching data: " + error.getMessage());
                textNitrogen.setText("Error fetching data");
            }
        });
    }

    // Update progress for N, P, and K
    private void updateProgress(CircularProgressIndicator progressBar, TextView textView, Long value, String key, int maxValue) {
        if (value != null) {
            int percentage = (int) ((value.doubleValue() / maxValue) * 100);
            progressBar.setProgress(percentage);
            textView.setText(key + ": " + percentage + "%");

            if (percentage < 30) {
                progressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (percentage < 70) {
                progressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                progressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        } else {
            textView.setText(key + " value not available");
        }
    }

    // Update progress for pH (scale 0-14 converted to percentage)
    private void updatePH(DataSnapshot snapshot) {
        Double value = snapshot.child("pH").getValue(Double.class);
        if (value != null) {
            int percentage = (int) ((value / 14) * 100);
            progressPH.setProgress(percentage);
            textPH.setText("pH: " + value);

            if (value < 5.5) {
                progressPH.setIndicatorColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (value < 7.5) {
                progressPH.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                progressPH.setIndicatorColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        } else {
            textPH.setText("pH value not available");
        }
    }
}
