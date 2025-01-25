package com.example.brainalyzer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpCenterActivity extends AppCompatActivity {

    private EditText etFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_center);

        // Initialize the feedback input field and button
        etFeedback = findViewById(R.id.etFeedback);
        Button btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        // Set the click listener for the "Send Feedback" button
        btnSubmitFeedback.setOnClickListener(v -> {
            String feedback = etFeedback.getText().toString().trim();

            if (feedback.isEmpty()) {
                // Show a warning if the feedback field is empty
                Toast.makeText(this, "Please enter your feedback before submitting.", Toast.LENGTH_SHORT).show();
            } else {
                // Simulate sending feedback to the developer
                sendFeedbackToDeveloper(feedback);

                // Clear the input field and thank the user
                etFeedback.setText("");
                Toast.makeText(this, "Thank you for your feedback! It helps us improve the app.", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Simulate sending feedback to the developer.
     * Replace this with real functionality like sending feedback via an API or email.
     *
     * @param feedback The feedback content from the user.
     */
    private void sendFeedbackToDeveloper(String feedback) {
        // Simulate sending feedback logic
        // For now, just log or print the feedback to confirm it was "sent"
        System.out.println("Feedback sent to developer: " + feedback);

        // Replace the above logic with actual API integration or email sending functionality
        // Example:
        // sendToServer(feedback);
        // sendEmailToDeveloper(feedback);
    }
}
