package com.example.brainalyzer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SurveyActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private String taskCategoryPreference;
    private String dueDateSortingPreference;
    private String difficultySortingPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Check if the survey has already been completed
        SharedPreferences preferences = getSharedPreferences("SurveyPrefs", MODE_PRIVATE);
        boolean isSurveyCompleted = preferences.getBoolean("isSurveyCompleted", false);

        if (isSurveyCompleted) {
            Toast.makeText(this, "You have already completed the survey.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_survey);

        // Initialize UI elements
        RadioGroup rgCategory = findViewById(R.id.rgCategory);
        RadioGroup rgDueDateSorting = findViewById(R.id.rgDueDateSorting);
        RadioGroup rgDifficultySorting = findViewById(R.id.rgDifficultySorting);

        findViewById(R.id.btnCompleteSurvey).setOnClickListener(v -> {
            // Ensure all fields are selected
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

            // Save preferences locally
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isSurveyCompleted", true);
            editor.putString("taskCategoryPreference", taskCategoryPreference);
            editor.putString("dueDateSortingPreference", dueDateSortingPreference);
            editor.putString("difficultySortingPreference", difficultySortingPreference);
            editor.apply();

            // Save to Firestore if user is authenticated
            if (currentUser != null) {
                savePreferencesToFirestore();
            }

            // Redirect to DashboardActivity
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void savePreferencesToFirestore() {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("taskCategoryPreference", taskCategoryPreference);
        preferences.put("prioritization", dueDateSortingPreference); // Align with Dashboard
        preferences.put("difficulty_sorting", difficultySortingPreference);

        db.collection("users").document(currentUser.getUid())
                .set(preferences)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save preferences.", Toast.LENGTH_SHORT).show());
    }
}
