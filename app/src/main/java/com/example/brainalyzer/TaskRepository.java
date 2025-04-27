package com.example.brainalyzer;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService executorService;
    private final FirebaseFirestore db;

    public TaskRepository(Application application) {
        // Room Database
        AppDataBase database = AppDataBase.getInstance(application);
        taskDao = database.taskDao();

        // Firestore Database
        db = FirebaseFirestore.getInstance();

        // Single-threaded executor for database operations
        executorService = Executors.newSingleThreadExecutor();
    }

    // 🔹 Insert Task in Room & Firestore
    public void insertTask(Task task) {
        executorService.execute(() -> {
            // Check if taskId exists in Room database
            Task existingTask = taskDao.getTaskById(task.getTaskId()); // Ensure a method to fetch by taskId exists
            if (existingTask == null) {
                taskDao.insertTask(task);  // Room Database
                addTaskToFirestore(task);  // Firestore
            } else {
                Log.d("TaskRepository", "Task with this taskId already exists.");
            }
        });
    }

    // 🔹 Update Task in Room & Firestore
    public void updateTask(Task task) {
        executorService.execute(() -> {
            taskDao.updateTask(task);
            updateTaskInFirestore(task);
        });
    }

    // 🔹 Delete Task from Room & Firestore
    public void deleteTask(Task task) {
        executorService.execute(() -> {
            taskDao.deleteTask(task);
            deleteTaskFromFirestore(task.getTaskId());
        });
    }

    // 🔹 Delete Task by ID from Room & Firestore
    public void deleteTaskById(String taskId) {
        executorService.execute(() -> {
            taskDao.deleteTaskById(taskId);
            deleteTaskFromFirestore(taskId);
        });
    }

    // 🔹 Retrieve all tasks for a user from Room
    public LiveData<List<Task>> getTasksByUserId(String userId) {
        return taskDao.getTasksByUserId(userId);
    }

    // 🔹 Retrieve tasks by category
    public LiveData<List<Task>> getTasksByCategory(String userId, String category) {
        return taskDao.getTasksByCategory(userId, category);
    }

    // 🔹 Retrieve tasks sorted by difficulty
    public LiveData<List<Task>> getTasksByDifficulty(String userId) {
        return taskDao.getTasksByDifficulty(userId);
    }

    // 🔹 Retrieve tasks within a date range
    public LiveData<List<Task>> getTasksDueInRange(String userId, String startDate, String endDate) {
        return taskDao.getTasksDueInRange(userId, startDate, endDate);
    }

    // 🔹 Retrieve overdue tasks
    public LiveData<List<Task>> getOverdueTasks(String userId, String currentDate) {
        return taskDao.getOverdueTasks(userId, currentDate);
    }

    // 🔥 Firestore: Add Task with Auto-generated Document ID
    private void addTaskToFirestore(Task task) {
        // If taskId is not set, generate a new document reference with auto-generated ID
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            task.setTaskId(UUID.randomUUID().toString()); // Assign a new unique task ID
        }

        db.collection("tasks")
                .document(task.getTaskId())  // Use the custom taskId if it exists, else use the auto-generated one
                .set(task)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Task added successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding task", e));
    }

    // 🔥 Firestore: Update Task
    private void updateTaskInFirestore(Task task) {
        db.collection("tasks")
                .document(task.getTaskId())
                .set(task, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Task updated successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating task", e));
    }

    // 🔥 Firestore: Delete Task
    private void deleteTaskFromFirestore(String taskId) {
        db.collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Task deleted from Firestore"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting task", e));
    }

    // 🔥 Firestore: Fetch User Tasks
    public void fetchTasksFromFirestore(String userId, OnSuccessListener<List<Task>> onSuccess) {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task> taskList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        taskList.add(task);
                    }
                    onSuccess.onSuccess(taskList);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching tasks", e));
    }
}
