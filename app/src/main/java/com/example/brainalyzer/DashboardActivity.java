package com.example.brainalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private LinearLayout taskContainer;
    private List<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        CalendarView calendarView = findViewById(R.id.calendarView);
        Button btnAddTask = findViewById(R.id.btnAddTask);
        Button btnHelp = findViewById(R.id.btnHelp); // Need Help button
        taskContainer = findViewById(R.id.taskContainer);

        // Add Task button click listener
        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, InputTaskActivity.class);
            startActivityForResult(intent, 1); // Request code 1 for returning results
        });

        // Need Help button click listener
        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpCenterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("task")) {
                Task newTask = (Task) data.getSerializableExtra("task");
                if (newTask != null) {
                    addTaskToDashboard(newTask);
                }
            }
        }
    }

    private void addTaskToDashboard(Task task) {
        taskList.add(task);

        // Dynamically add task views
        TextView taskView = new TextView(this);
        taskView.setText(
                "Task: " + task.getName() + "\n" +
                        "Due: " + task.getDueDate() + "\n" +
                        "Difficulty: " + task.getDifficulty() + "\n" +
                        "Type: " + task.getType()
        );
        taskView.setPadding(8, 8, 8, 8);
        taskView.setBackgroundResource(android.R.drawable.edit_text);
        taskView.setTextSize(16);

        taskContainer.addView(taskView);
    }
}
