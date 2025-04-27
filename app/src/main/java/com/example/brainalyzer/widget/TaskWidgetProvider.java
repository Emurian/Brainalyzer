package com.example.brainalyzer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.brainalyzer.InputTaskActivity;
import com.example.brainalyzer.R;
import com.example.brainalyzer.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "TaskWidgetProvider";
    private static final List<Task> taskList = new ArrayList<>();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static FirebaseAuth firebaseAuth;
    private static FirebaseFirestore db;
    private static FirebaseUser currentUser;
    private static ListenerRegistration taskListener;
    private static String prioritizationMethod = "Due Date"; // Default method

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (firebaseAuth == null) firebaseAuth = FirebaseAuth.getInstance();
        if (db == null) db = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "No user logged in.");
            updateWidgetUI(context, appWidgetManager, appWidgetIds, true);
            return;
        }

        loadUserPreferences(context, appWidgetManager, appWidgetIds);
    }

    private void loadUserPreferences(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("prioritization")) {
                        prioritizationMethod = document.getString("prioritization");
                        Log.d(TAG, "Loaded prioritization method: " + prioritizationMethod);
                    } else {
                        Log.d(TAG, "Using default prioritization method.");
                    }
                    fetchTasks(context, appWidgetManager, appWidgetIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load prioritization preference", e);
                    fetchTasks(context, appWidgetManager, appWidgetIds);
                });
    }

    private void fetchTasks(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (taskListener != null) taskListener.remove();

        taskListener = db.collection("tasks")
                .document(currentUser.getUid())
                .collection("userTasks")
                .whereEqualTo("completed", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching tasks", error);
                        return;
                    }

                    taskList.clear();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Task task = doc.toObject(Task.class);
                            task.setTaskId(doc.getId());

                            long dueDate = 0; // Default to 0 (invalid date) if no valid dueDate is found
                            try {
                                // Attempt to get dueDate as a Timestamp
                                Timestamp ts = doc.getTimestamp("dueDate");
                                if (ts != null) {
                                    dueDate = ts.toDate().getTime(); // Convert Timestamp to milliseconds
                                } else {
                                    // Handle the case where dueDate is stored as a long (milliseconds)
                                    Long dueDateLong = doc.getLong("dueDate");
                                    if (dueDateLong != null) {
                                        dueDate = dueDateLong;
                                    } else {
                                        Log.e(TAG, "Due date is null or invalid for task ID: " + doc.getId());
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing due date for task ID: " + doc.getId(), e);
                            }
                            task.setDueDate(dueDate);

                            // Only future tasks
                            if (dueDate >= System.currentTimeMillis()) {
                                taskList.add(task);
                            }
                        }
                    }

                    sortTasks();

                    handler.post(() -> updateWidgetUI(context, appWidgetManager, appWidgetIds, taskList.isEmpty()));
                });
    }

    private void sortTasks() {
        if ("Difficulty".equalsIgnoreCase(prioritizationMethod)) {
            taskList.sort(Comparator.comparingInt(Task::getDifficulty).reversed());
        } else {
            taskList.sort(Comparator.comparingLong(Task::getDueDate));
        }
    }

    private void updateWidgetUI(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, boolean showNoTasks) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task);

            // ✅ Fixed date format (show full date if preferred)
            String date = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date());
            views.setTextViewText(R.id.widgetDateHeader, date);

            if (showNoTasks) {
                views.setTextViewText(R.id.widgetTaskText, "Nothing today");
                Log.d(TAG, "No tasks to display");
            } else {
                String formattedTasks = formatTaskList();
                views.setTextViewText(R.id.widgetTaskText, formattedTasks);
                Log.d(TAG, "Updated widget with tasks:\n" + formattedTasks);
            }

            // Add Task Button
            Intent intent = new Intent(context, InputTaskActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widgetAddTask, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private String formatTaskList() {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(taskList.size(), 5);

        for (int i = 0; i < limit; i++) {
            Task t = taskList.get(i);
            sb.append("📌 ").append(t.getName())
                    .append("\n⚡ Difficulty: ").append(t.getDifficulty())
                    .append("\n📅 ").append(formatDate(t.getDueDate()))
                    .append("\n\n");
        }

        return sb.toString().trim();
    }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(timestamp));
    }
}
