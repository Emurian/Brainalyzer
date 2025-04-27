package com.example.brainalyzer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.example.brainalyzer.notifications.NotificationHelper;

public class InputTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etDueDate;
    private RadioGroup rgDifficultySorting, rgTaskType;
    private int selectedYear, selectedMonth, selectedDay;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_task);

        // Initialize UI components
        etTaskName = findViewById(R.id.etTaskName);
        etDueDate = findViewById(R.id.etDueDate);
        rgDifficultySorting = findViewById(R.id.rgDifficultySorting);
        rgTaskType = findViewById(R.id.rgTaskType);
        Button btnAddTask = findViewById(R.id.btnAddTask);
        menuButton = findViewById(R.id.menuButton);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Sidebar menu button click listener
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        } else {
            Log.e("InputTaskActivity", "menuButton is NULL! Check XML ID.");
        }

        // Initialize current date
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
        btnAddTask.setOnClickListener(v -> saveTaskToFirestore());

        // Sidebar menu item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else if (id == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.menu_help) {
                startActivity(new Intent(this, HelpCenterActivity.class));
            } else if (id == R.id.menu_logout) {
                logoutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void saveTaskToFirestore() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskName = etTaskName.getText().toString().trim();
        String dueDateString = etDueDate.getText().toString().trim();

        // Get selected difficulty as an Integer (using radio button IDs)
        int selectedDifficulty = getDifficultyValue();

        // Get selected task type as a String
        RadioButton selectedTypeButton = findViewById(rgTaskType.getCheckedRadioButtonId());
        String selectedTaskType = (selectedTypeButton != null) ? selectedTypeButton.getText().toString() : "";

        // Validation checks
        if (TextUtils.isEmpty(taskName)) {
            Toast.makeText(this, "Please enter a task name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(dueDateString)) {
            Toast.makeText(this, "Please select a due date.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedTaskType)) {
            Toast.makeText(this, "Please select a task type.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert dueDateString to timestamp
        long dueDateTimestamp = convertDateToTimestamp(dueDateString);
        if (dueDateTimestamp == -1) {
            Toast.makeText(this, "Invalid date format. Please select a valid date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debugging logs to verify values before saving
        Log.d("TaskDebug", "Task Name: " + taskName);
        Log.d("TaskDebug", "Due Date: " + dueDateString);
        Log.d("TaskDebug", "Difficulty: " + selectedDifficulty);
        Log.d("TaskDebug", "Task Type: " + selectedTaskType);

        // ✅ FIXED: Updated Task constructor with 'false' for 'completed'
        Task newTask = new Task(taskName, dueDateTimestamp, selectedDifficulty, selectedTaskType, currentUser.getUid(), false);

        // Save task to Firestore
        db.collection("tasks")
                .document(currentUser.getUid())
                .collection("userTasks")
                .add(newTask)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();

                    // ✅ Schedule notification after saving task
                    NotificationHelper.sendTaskNotification(this, taskName, dueDateString);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("task", newTask);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save task. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    // Convert date (dd/MM/yyyy) to timestamp
    private long convertDateToTimestamp(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ✅ FIXED: Get difficulty using Radio Button IDs
    private int getDifficultyValue() {
        int selectedId = rgDifficultySorting.getCheckedRadioButtonId();

        if (selectedId == R.id.rbHighDifficulty) {
            return 3;
        } else if (selectedId == R.id.rbMediumDifficulty) {
            return 2;
        } else if (selectedId == R.id.rbLowDifficulty) {
            return 1;
        } else {
            return 1; // Default to Low if nothing is selected
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
