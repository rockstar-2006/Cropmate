package com.example.cropmate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Vibrator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class Dashboard extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private static final UUID ESP32_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private DatabaseReference databaseRef;
    private AnimationDrawable rippleAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        databaseRef = FirebaseDatabase.getInstance().getReference("SensorData"); // Corrected DB Reference
        ImageView rippleEffect = findViewById(R.id.ripple_effect);
        rippleAnimation = (AnimationDrawable) rippleEffect.getDrawable();


        Button btnConnect = findViewById(R.id.btn_connect);

        btnConnect.setOnClickListener(v -> {
            // Vibrate on click
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (!rippleAnimation.isRunning()) {
                rippleAnimation.start();
            }
            simulateConnection();

            if (vibrator != null) {
                vibrator.vibrate(100); // Vibrate for 100ms
            }

            // Start ripple animation
            Animation rippleAnim = AnimationUtils.loadAnimation(this, R.anim.ripple_animation);
            btnConnect.startAnimation(rippleAnim);

            // Connect to ESP32
            connectToESP32();

            // Redirect to MainActivity2
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(Dashboard.this, MainActivity2.class);
                startActivity(intent);
                finish(); // Close Dashboard activity if needed
            }, 2000); // Delay for 2 seconds before navigation (adjust as needed)
        });

    }
    private void simulateConnection() {
        new android.os.Handler().postDelayed(() -> {
            if (rippleAnimation.isRunning()) {
                rippleAnimation.stop();
            }
        }, 5000); // Stops after 5 seconds (adjust as needed)
    }
    private void connectToESP32() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().contains("ESP32")) {  // Connect to any ESP32
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP32_UUID);
                    bluetoothSocket.connect();
                    Toast.makeText(this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();

                    inputStream = bluetoothSocket.getInputStream(); // Start reading data
                    readDataFromESP32();
                    runOnUiThread(() -> {
                        ImageView rippleEffect = findViewById(R.id.ripple_effect);
                        AnimationDrawable rippleAnimation = (AnimationDrawable) rippleEffect.getDrawable();
                        rippleAnimation.stop();
                    });
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Connection failed!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        Toast.makeText(this, " It's connected to the Data base", Toast.LENGTH_SHORT).show();
    }

    private void readDataFromESP32() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String receivedData = new String(buffer, 0, bytes);
                    parseAndSaveData(receivedData);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    private void parseAndSaveData(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);

            double nitrogen = jsonData.getDouble("N");
            double phosphorus = jsonData.getDouble("P");
            double potassium = jsonData.getDouble("K");
            double moisture = jsonData.getDouble("Moisture");
            double pH = jsonData.getDouble("pH");

            String key = databaseRef.push().getKey();
            databaseRef.child(key).setValue(new SensorData(nitrogen, phosphorus, potassium, moisture, pH))
                    .addOnSuccessListener(aVoid -> runOnUiThread(() ->
                            Toast.makeText(Dashboard.this, "Data Saved!", Toast.LENGTH_SHORT).show()))
                    .addOnFailureListener(e -> runOnUiThread(() ->
                            Toast.makeText(Dashboard.this, "Failed to Save!", Toast.LENGTH_SHORT).show()));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class SensorData {
        public double N, P, K, Moisture, pH;

        public SensorData() {}

        public SensorData(double N, double P, double K, double Moisture, double pH) {
            this.N = N;
            this.P = P;
            this.K = K;
            this.Moisture = Moisture;
            this.pH = pH;
        }
    }
}
