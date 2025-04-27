package com.example.brainalyzer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup dueDateGroup, difficultyGroup, taskTypeGroup;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ImageButton menuButton = findViewById(R.id.menuButton);
        dueDateGroup = findViewById(R.id.dueDateGroup);
        difficultyGroup = findViewById(R.id.difficultyGroup);
        taskTypeGroup = findViewById(R.id.taskTypeGroup);  // New task type radio group
        Button btnDone = findViewById(R.id.btnDone);

        // Navigation drawer setup
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else if (id == R.id.menu_settings) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.menu_help) {
                startActivity(new Intent(this, HelpCenterActivity.class));
                finish();
            } else if (id == R.id.menu_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Load default prioritization and task type from Firestore
        fetchUserPrioritization();
        fetchUserTaskType();

        // Handle sorting by Due Date
        dueDateGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String newDueDatePreference = (checkedId == R.id.radioClosestDue) ? "closest_due" : "farthest_due";
            savePrioritizationToFirestore(newDueDatePreference, null);
        });

        // Handle sorting by Difficulty
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String newDifficultyPreference = (checkedId == R.id.radioLowDifficulty) ? "low_difficulty" :
                    (checkedId == R.id.radioMediumDifficulty) ? "medium_difficulty" : "high_difficulty";
            savePrioritizationToFirestore(null, newDifficultyPreference);
        });

        // Handle task type selection
        taskTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String newTaskTypePreference = (checkedId == R.id.radioPersonal) ? "personal" : "academic";
            saveTaskTypeToFirestore(newTaskTypePreference);
        });

        // Handle Done button
        btnDone.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void fetchUserPrioritization() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String prioritization = documentSnapshot.getString("prioritization");
                        String difficultyPreference = documentSnapshot.getString("difficulty_sorting");

                        // Default values if not set
                        if (prioritization == null) prioritization = "closest_due";
                        if (difficultyPreference == null) difficultyPreference = "medium_difficulty";

                        // Save to SharedPreferences
                        sharedPreferences.edit()
                                .putString("due_date_sorting", prioritization)
                                .putString("difficulty_sorting", difficultyPreference)
                                .apply();

                        // Set UI for prioritization
                        ((RadioButton) findViewById(R.id.radioClosestDue)).setChecked("closest_due".equals(prioritization));
                        ((RadioButton) findViewById(R.id.radioFarthestDue)).setChecked("farthest_due".equals(prioritization));

                        // Set UI for difficulty
                        ((RadioButton) findViewById(R.id.radioLowDifficulty)).setChecked("low_difficulty".equals(difficultyPreference));
                        ((RadioButton) findViewById(R.id.radioMediumDifficulty)).setChecked("medium_difficulty".equals(difficultyPreference));
                        ((RadioButton) findViewById(R.id.radioHighDifficulty)).setChecked("high_difficulty".equals(difficultyPreference));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch prioritization settings.", Toast.LENGTH_SHORT).show());
    }

    private void fetchUserTaskType() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String taskTypePreference = documentSnapshot.getString("task_type");

                        // Default value if not set
                        if (taskTypePreference == null) taskTypePreference = "personal";

                        // Save to SharedPreferences
                        sharedPreferences.edit()
                                .putString("task_type", taskTypePreference)
                                .apply();

                        // Set UI for task type
                        ((RadioButton) findViewById(R.id.radioPersonal)).setChecked("personal".equals(taskTypePreference));
                        ((RadioButton) findViewById(R.id.radioAcademic)).setChecked("academic".equals(taskTypePreference));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch task type settings.", Toast.LENGTH_SHORT).show());
    }

    private void savePrioritizationToFirestore(String dueDatePreference, String difficultyPreference) {
        if (currentUser == null) return;

        // Prepare update data
        if (dueDatePreference != null) {
            sharedPreferences.edit().putString("due_date_sorting", dueDatePreference).apply();
            db.collection("users").document(currentUser.getUid())
                    .update("prioritization", dueDatePreference)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Prioritization updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update prioritization.", Toast.LENGTH_SHORT).show());
        }

        if (difficultyPreference != null) {
            sharedPreferences.edit().putString("difficulty_sorting", difficultyPreference).apply();
            db.collection("users").document(currentUser.getUid())
                    .update("difficulty_sorting", difficultyPreference)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Difficulty sorting updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update difficulty sorting.", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveTaskTypeToFirestore(String taskTypePreference) {
        if (currentUser == null) return;

        sharedPreferences.edit().putString("task_type", taskTypePreference).apply();

        db.collection("users").document(currentUser.getUid())
                .update("task_type", taskTypePreference)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Task type updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update task type.", Toast.LENGTH_SHORT).show());
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
        super.onBackPressed();
    }
}
