package com.example.brainalyzer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private static final int ADD_TASK_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 2;  // Added a constant for settings request

    private LinearLayout taskContainer;
    private List<Task> taskList = new ArrayList<>();
    private MaterialCalendarView materialCalendarView;
    private boolean isCalendarVisible = true;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private ListenerRegistration taskListener;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button btnCalendarToggle;

    private String prioritizationMethod = "Due Date";  // Default sorting method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        materialCalendarView = findViewById(R.id.calendarView);
        taskContainer = findViewById(R.id.taskContainer);
        btnCalendarToggle = findViewById(R.id.btnToggleCalendar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ImageButton menuButton = findViewById(R.id.menuButton);

        firebaseAuth.addAuthStateListener(auth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                loadUserPreferences();
                listenForTaskUpdates();
            }
        });

        btnCalendarToggle.setOnClickListener(v -> toggleCalendar());
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else if (id == R.id.menu_settings) {
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST);  // Changed to start for result
            } else if (id == R.id.menu_help) {
                startActivity(new Intent(this, HelpCenterActivity.class));
            } else if (id == R.id.menu_logout) {
                logoutUser();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        findViewById(R.id.btnAddTask).setOnClickListener(v ->
                startActivityForResult(new Intent(this, InputTaskActivity.class), ADD_TASK_REQUEST)
        );
    }

    // Load user preferences for prioritization method
    private void loadUserPreferences() {
        if (currentUser == null) return;

        SharedPreferences preferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        prioritizationMethod = preferences.getString("prioritization", "Due Date");

        listenForTaskUpdates(); // Listen for task updates after preferences are loaded
    }

    // Listen for task updates from Firestore
    private void listenForTaskUpdates() {
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskListener != null) {
            taskListener.remove();
        }

        taskListener = db.collection("tasks")
                .document(currentUser.getUid())
                .collection("userTasks")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to listen for task updates!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        taskList.clear();

                        for (QueryDocumentSnapshot document : value) {
                            Task retrievedTask = document.toObject(Task.class);
                            retrievedTask.setTaskId(document.getId());
                            taskList.add(retrievedTask);
                        }

                        sortTasks();  // Sort tasks based on the updated prioritization method
                        displayTasks(); // Display updated tasks
                    }
                });
    }

    // Sort tasks based on the current prioritization method
    private void sortTasks() {
        if ("Difficulty".equals(prioritizationMethod)) {
            taskList.sort((t1, t2) -> Integer.compare(t2.getDifficulty(), t1.getDifficulty()));
        } else if ("Due Date".equals(prioritizationMethod)) {
            taskList.sort(Comparator.comparing(Task::getDueDate));
        } else if ("Task Type".equals(prioritizationMethod)) {
            taskList.sort(Comparator.comparing(Task::getTaskCategory));
        }
    }

    // Display tasks in the UI
    private void displayTasks() {
        taskContainer.removeAllViews();
        for (Task task : taskList) {
            CardView taskCard = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            taskCard.setLayoutParams(params);
            taskCard.setCardElevation(8f);
            taskCard.setRadius(12f);
            taskCard.setUseCompatPadding(true);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            TextView taskView = new TextView(this);
            taskView.setText(String.format("Task: %s\nDue: %s\nDifficulty: %s\nCategory: %s",
                    task.getName(),
                    task.getFormattedDueDate(),
                    getDifficultyLabel(task.getDifficulty()),
                    task.getTaskCategory()));

            Button btnComplete = new Button(this);
            btnComplete.setText("Complete");
            btnComplete.setOnClickListener(v -> markTaskAsComplete(task));

            Button btnDelete = new Button(this);
            btnDelete.setText("Delete");
            btnDelete.setOnClickListener(v -> deleteTask(task));

            layout.addView(taskView);
            layout.addView(btnComplete);
            layout.addView(btnDelete);

            taskCard.addView(layout);
            taskContainer.addView(taskCard);
        }
    }

    private String getDifficultyLabel(int difficulty) {
        return difficulty == 3 ? "High" : difficulty == 2 ? "Medium" : "Low";
    }

    private void markTaskAsComplete(Task task) {
        // Logic for completing a task
    }

    private void deleteTask(Task task) {
        if (currentUser != null) {
            db.collection("tasks")
                    .document(currentUser.getUid())
                    .collection("userTasks")
                    .document(task.getTaskId())
                    .delete()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Task deleted successfully!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to delete task.", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void toggleCalendar() {
        if (isCalendarVisible) {
            materialCalendarView.setVisibility(View.GONE);
        } else {
            materialCalendarView.setVisibility(View.VISIBLE);
        }
        isCalendarVisible = !isCalendarVisible;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    // This method is triggered when SettingsActivity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST && resultCode == RESULT_OK) {
            loadUserPreferences(); // Reload preferences if settings were changed
        }
    }
}
