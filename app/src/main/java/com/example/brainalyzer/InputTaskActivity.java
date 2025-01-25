package com.example.brainalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InputTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etDueDate;
    private RatingBar ratingBar;
    private RadioGroup rgTaskType;
    private TextView tvDifficultyLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_task);

        // Initialize views
        etTaskName = findViewById(R.id.etTaskName);
        etDueDate = findViewById(R.id.etDueDate);
        ratingBar = findViewById(R.id.ratingBar2);
        rgTaskType = findViewById(R.id.rgTaskType);
        tvDifficultyLevel = findViewById(R.id.tvDifficultyLevel);
        Button btnAddTask = findViewById(R.id.btnAddTask);

        // Update difficulty level dynamically
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            tvDifficultyLevel.setText("Difficulty Level: " + (int) rating);
        });

        btnAddTask.setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString();
            String dueDate = etDueDate.getText().toString();
            int difficulty = (int) ratingBar.getRating();
            int selectedTypeId = rgTaskType.getCheckedRadioButtonId();
            String taskType = ((RadioButton) findViewById(selectedTypeId)).getText().toString();

            Task task = new Task(taskName, dueDate, difficulty, taskType);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("task", task);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        Button btnHelp = findViewById(R.id.btnHelp);

        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpCenterActivity.class);
            startActivity(intent);
        });

        // Restore state if applicable
        if (savedInstanceState != null) {
            ratingBar.setRating(savedInstanceState.getFloat("rating", 0));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("rating", ratingBar.getRating());
    }
}
