package com.example.brainalyzer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize views and FirebaseAuth
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);

        // Check if the user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            // If logged in, go directly to DashboardActivity
            Intent mainIntent = new Intent(SignInActivity.this, DashboardActivity.class);
            startActivity(mainIntent);
            finish(); // Close this activity
        }

        // Set up login button click listener
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(SignInActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(SignInActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sign in with Firebase
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Login successful, save login status in SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            // Show success message and go to DashboardActivity
                            Toast.makeText(SignInActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignInActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // Show error message
                            Toast.makeText(SignInActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
