package com.example.brainalyzer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class InputTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etDueDate;
    private RadioGroup rgDifficultySorting, rgTaskType;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_task);

        etTaskName = findViewById(R.id.etTaskName);
        etDueDate = findViewById(R.id.etDueDate);
        rgDifficultySorting = findViewById(R.id.rgDifficultySorting);
        rgTaskType = findViewById(R.id.rgTaskType);
        Button btnAddTask = findViewById(R.id.btnAddTask);

        // Initialize the current date
        final Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Task due date selection (DatePickerDialog)
        etDueDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    InputTaskActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;
                        etDueDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    },
                    selectedYear, selectedMonth, selectedDay
            );
            datePickerDialog.show();
        });

        // Add Task button click listener
        btnAddTask.setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString().trim();
            String dueDate = etDueDate.getText().toString().trim();

            // Get selected difficulty as a String
            RadioButton selectedDifficultyButton = findViewById(rgDifficultySorting.getCheckedRadioButtonId());
            String selectedDifficulty = (selectedDifficultyButton != null) ? selectedDifficultyButton.getText().toString() : "";

            // Get selected task type as a String
            RadioButton selectedTypeButton = findViewById(rgTaskType.getCheckedRadioButtonId());
            String selectedTaskType = (selectedTypeButton != null) ? selectedTypeButton.getText().toString() : "";

            // Get the userId (Ensuring the user is logged in with Firebase)
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in. Please log in first.", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();

            // Validation checks
            if (TextUtils.isEmpty(taskName)) {
                Toast.makeText(this, "Please enter a task name.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(dueDate)) {
                Toast.makeText(this, "Please select a due date.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(selectedDifficulty)) {
                Toast.makeText(this, "Please select a difficulty level.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(selectedTaskType)) {
                Toast.makeText(this, "Please select a task type.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new Task object with userId
            Task newTask = new Task(taskName, dueDate, selectedDifficulty, selectedTaskType, userId);

            // Save task to the database (ensure TaskRepository is properly set up)
            TaskRepository repository = new TaskRepository(getApplication());
            repository.insertTask(newTask);

            // Send the new task back to DashboardActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("task", newTask); // Task is Parcelable
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
