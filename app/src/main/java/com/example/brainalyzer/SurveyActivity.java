package com.example.brainalyzer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SurveyActivity extends AppCompatActivity {
    private String taskCategoryPreference;
    private String dueDateSortingPreference;
    private String difficultySortingPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the survey has already been completed
        SharedPreferences preferences = getSharedPreferences("SurveyPrefs", MODE_PRIVATE);
        boolean isSurveyCompleted = preferences.getBoolean("isSurveyCompleted", false);

        if (isSurveyCompleted) {
            // If survey is completed, go to DashboardActivity
            Toast.makeText(this, "You have already completed the survey.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_survey);

        // Initialize RadioGroups
        RadioGroup rgCategory = findViewById(R.id.rgCategory);
        RadioGroup rgDueDateSorting = findViewById(R.id.rgDueDateSorting);
        RadioGroup rgDifficultySorting = findViewById(R.id.rgDifficultySorting);

        // Button to complete the survey
        findViewById(R.id.btnCompleteSurvey).setOnClickListener(v -> {
            // Check if all required options are selected
            int selectedCategoryId = rgCategory.getCheckedRadioButtonId();
            int selectedDueDateSortingId = rgDueDateSorting.getCheckedRadioButtonId();
            int selectedDifficultySortingId = rgDifficultySorting.getCheckedRadioButtonId();

            if (selectedCategoryId == -1 || selectedDueDateSortingId == -1 || selectedDifficultySortingId == -1) {
                Toast.makeText(this, "Please answer all questions before proceeding.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected values
            RadioButton selectedCategory = findViewById(selectedCategoryId);
            RadioButton selectedDueDateSorting = findViewById(selectedDueDateSortingId);
            RadioButton selectedDifficultySorting = findViewById(selectedDifficultySortingId);

            taskCategoryPreference = selectedCategory.getText().toString();
            dueDateSortingPreference = selectedDueDateSorting.getText().toString();
            difficultySortingPreference = selectedDifficultySorting.getText().toString();

            // Save preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isSurveyCompleted", true);
            editor.putString("taskCategoryPreference", taskCategoryPreference);
            editor.putString("dueDateSortingPreference", dueDateSortingPreference);
            editor.putString("difficultySortingPreference", difficultySortingPreference);
            editor.apply();

            // Redirect to DashboardActivity with preferences
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("taskCategoryPreference", taskCategoryPreference);
            intent.putExtra("dueDateSortingPreference", dueDateSortingPreference);
            intent.putExtra("difficultySortingPreference", difficultySortingPreference);
            startActivity(intent);
            finish();
        });
    }
}
