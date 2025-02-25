package com.example.brainalyzer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private LinearLayout taskContainer;
    private List<Task> taskList = new ArrayList<>();
    private String taskPreference;
    private String sortingPreference;
    private MaterialCalendarView materialCalendarView;
    private Button btnCalendarToggle, btnNeedHelp;
    private boolean isCalendarVisible = true; // Tracks calendar visibility

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        materialCalendarView = findViewById(R.id.calendarView);
        taskContainer = findViewById(R.id.taskContainer);
        Button btnAddTask = findViewById(R.id.btnAddTask);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnCalendarToggle = findViewById(R.id.btnToggleCalendar);
        btnNeedHelp = findViewById(R.id.btnHelp);

        // 🔹 Calendar Toggle Button Logic
        btnCalendarToggle.setOnClickListener(v -> {
            if (isCalendarVisible) {
                materialCalendarView.setVisibility(View.GONE);
                btnCalendarToggle.setText("Show Calendar");
            } else {
                materialCalendarView.setVisibility(View.VISIBLE);
                btnCalendarToggle.setText("Hide Calendar");
            }
            isCalendarVisible = !isCalendarVisible;
        });

        // 🔹 Need Help Button Logic
        btnNeedHelp.setOnClickListener(v -> {
            Intent helpIntent = new Intent(DashboardActivity.this, HelpCenterActivity.class);
            startActivity(helpIntent);
        });

        // Retrieve preferences from Intent or SharedPreferences
        SharedPreferences preferences = getSharedPreferences("SurveyPrefs", MODE_PRIVATE);
        Intent intent = getIntent();
        taskPreference = intent.getStringExtra("taskCategoryPreference");
        sortingPreference = intent.getStringExtra("dueDateSortingPreference");

        if (taskPreference == null) {
            taskPreference = preferences.getString("taskCategoryPreference", "Academic");
        }
        if (sortingPreference == null) {
            sortingPreference = preferences.getString("dueDateSortingPreference", "Due Date");
        }

        Log.d("DashboardActivity", "Task Preference: " + taskPreference);
        Log.d("DashboardActivity", "Sorting Preference: " + sortingPreference);

        // Open task input screen
        btnAddTask.setOnClickListener(v -> {
            Intent addTaskIntent = new Intent(this, InputTaskActivity.class);
            startActivityForResult(addTaskIntent, 1);
        });

        // Logout functionality
        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(DashboardActivity.this, "You have been logged out.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DashboardActivity.this, SignInActivity.class));
            finish();
        });

        // Display tasks based on preference
        displayTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Task newTask = data.getParcelableExtra("task");
            if (newTask != null) {
                addTaskToDashboard(newTask);
            }
        }
    }

    private void addTaskToDashboard(Task task) {
        taskList.add(task);
        sortAndDisplayTasks();
    }

    private void sortAndDisplayTasks() {
        if (taskList.isEmpty()) {
            displayNoTasksMessage();
            return;
        }

        // Sort tasks based on category preference
        taskList.sort((task1, task2) -> {
            boolean isTask1Preferred = task1.getTaskCategory().equals(taskPreference);
            boolean isTask2Preferred = task2.getTaskCategory().equals(taskPreference);
            return Boolean.compare(isTask2Preferred, isTask1Preferred);
        });

        // Sort by difficulty or due date
        if ("Difficulty".equals(sortingPreference)) {
            taskList.sort(Comparator.comparingInt(task -> getDifficultyLevel(task.getDifficulty())));
        } else {
            taskList.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        }

        displayTasks();
    }

    private int getDifficultyLevel(String difficulty) {
        switch (difficulty) {
            case "High": return 3;
            case "Medium": return 2;
            case "Low": return 1;
            default: return 0;
        }
    }

    private void displayTasks() {
        taskContainer.removeAllViews();
        if (taskList.isEmpty()) {
            displayNoTasksMessage();
            return;
        }

        for (Task task : taskList) {
            TextView taskView = new TextView(this);
            taskView.setText(
                    "Task: " + task.getName() + "\n" +
                            "Due: " + (task.getDueDate() != null ? task.getDueDate() : "No due date") + "\n" +
                            "Difficulty: " + task.getDifficulty() + "\n" +
                            "Category: " + task.getTaskCategory()
            );
            taskView.setPadding(8, 8, 8, 8);
            taskView.setBackgroundResource(android.R.color.darker_gray);
            taskContainer.addView(taskView);
        }
    }

    private void displayNoTasksMessage() {
        TextView noTasksView = new TextView(this);
        noTasksView.setText("No tasks available.");
        noTasksView.setPadding(8, 8, 8, 8);
        taskContainer.addView(noTasksView);
    }
}
