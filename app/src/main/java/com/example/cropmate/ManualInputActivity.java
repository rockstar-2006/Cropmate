package com.example.cropmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ManualInputActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private EditText etN, etP, etK;
    private Button btnProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);

        etN = findViewById(R.id.et_n);
        etP = findViewById(R.id.et_p);
        etK = findViewById(R.id.et_k);
        btnProceed = findViewById(R.id.btn_proceed_to_crops);

        btnProceed.setOnClickListener(v -> {
            String valN = etN.getText().toString();
            String valP = etP.getText().toString();
            String valK = etK.getText().toString();

            if (valN.isEmpty() || valP.isEmpty() || valK.isEmpty()) {
                Toast.makeText(this, "Please enter all N-P-K values", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, CropSelectionActivity.class);
            intent.putExtra("is_manual", true);
            intent.putExtra("manual_n", Integer.parseInt(valN));
            intent.putExtra("manual_p", Integer.parseInt(valP));
            intent.putExtra("manual_k", Integer.parseInt(valK));
            startActivity(intent);
        });
    }
}
