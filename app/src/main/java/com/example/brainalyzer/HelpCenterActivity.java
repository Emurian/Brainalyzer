package com.example.brainalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HelpCenterActivity extends AppCompatActivity {

    private EditText etFeedback;
    private DatabaseReference databaseRef;
    private FirebaseAuth firebaseAuth;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_center);

        etFeedback = findViewById(R.id.etFeedback);
        Button btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);
        ImageButton menuButton = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // 🔹 Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("feedback");

        // ✅ Sidebar Menu Button
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // ✅ Sidebar Navigation Handling
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else if (id == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.menu_help) {
                drawerLayout.closeDrawer(GravityCompat.START); // Stay on Help Page
            } else if (id == R.id.menu_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // ✅ Feedback Submission
        btnSubmitFeedback.setOnClickListener(v -> {
            String feedback = etFeedback.getText().toString().trim();

            if (feedback.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback before submitting.", Toast.LENGTH_SHORT).show();
            } else {
                sendFeedbackToFirebase(feedback);
                etFeedback.setText(""); // Clear input field after submission
            }
        });
    }

    private void sendFeedbackToFirebase(String feedback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to submit feedback.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String feedbackId = databaseRef.push().getKey();

        FeedbackModel feedbackData = new FeedbackModel(userId, feedback, System.currentTimeMillis());

        if (feedbackId != null) {
            databaseRef.child(feedbackId).setValue(feedbackData)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to send feedback. Try again later.", Toast.LENGTH_SHORT).show());
        }
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    // 🔹 Feedback Model Class
    public static class FeedbackModel {
        public String userId;
        public String message;
        public long timestamp;

        public FeedbackModel() {} // Required for Firebase

        public FeedbackModel(String userId, String message, long timestamp) {
            this.userId = userId;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
