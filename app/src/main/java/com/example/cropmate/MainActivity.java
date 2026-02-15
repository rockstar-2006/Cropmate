package com.example.cropmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LanguageAdapter adapter;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        doneButton = findViewById(R.id.btn_done);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        // Sample Language List
        List<Language> languageList = new ArrayList<>();
        languageList.add(new Language("English"));
        languageList.add(new Language("Kannada"));
        languageList.add(new Language("Tulu"));
        languageList.add(new Language("Hindi"));
        languageList.add(new Language("Punjabi"));
        languageList.add(new Language("Tamil"));
        languageList.add(new Language("Telugu"));

        adapter = new LanguageAdapter(this, languageList);
        recyclerView.setAdapter(adapter);

        // Handle Done Button Click
        doneButton.setOnClickListener(v -> {
            String selectedLanguage = adapter.getSelectedLanguage();
            if (selectedLanguage != null) {
                String langCode = "en";
                if (selectedLanguage.equalsIgnoreCase("Kannada")) {
                    langCode = "kn";
                }
                
                // Update Locale
                LocaleHelper.setLocale(this, langCode);

                // Re-launch app logic to apply changes globally or just navigate
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
