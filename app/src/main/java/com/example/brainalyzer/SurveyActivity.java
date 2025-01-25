package com.example.brainalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class SurveyActivity extends AppCompatActivity {
    private String taskPreference;
    private String sortingPreference;
    private int difficulty = 0; // Default value if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        RadioGroup rgType = findViewById(R.id.rgType);
        RadioGroup rgSorting = findViewById(R.id.rgSorting);

        findViewById(R.id.btnCompleteSurvey).setOnClickListener(v -> {
            int selectedTypeId = rgType.getCheckedRadioButtonId();
            RadioButton selectedType = findViewById(selectedTypeId);
            taskPreference = selectedType.getText().toString();

            int selectedSortingId = rgSorting.getCheckedRadioButtonId();
            RadioButton selectedSorting = findViewById(selectedSortingId);
            sortingPreference = selectedSorting.getText().toString();

            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("taskPreference", taskPreference);
            intent.putExtra("sortingPreference", sortingPreference);
            intent.putExtra("difficulty", difficulty); // Can be removed if not used
            startActivity(intent);
        });
    }
}
