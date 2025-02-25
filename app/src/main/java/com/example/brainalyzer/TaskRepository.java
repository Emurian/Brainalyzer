package com.example.brainalyzer;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService executorService;

    public TaskRepository(Application application) {
        // Get the TaskDao instance from Room Database
        AppDataBase db = AppDataBase.getInstance(application);
        taskDao = db.taskDao();

        // Create an executor service for background tasks
        executorService = Executors.newFixedThreadPool(4);
    }

    // Insert task into the database asynchronously
    public void insertTask(Task task) {
        executorService.execute(() -> taskDao.insertTask(task));
    }

    // Update task in the database asynchronously
    public void updateTask(Task task) {
        executorService.execute(() -> taskDao.updateTask(task));
    }

    // Delete a task asynchronously
    public void deleteTask(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }

    // Delete a task by ID asynchronously
    public void deleteTaskById(int taskId) {
        executorService.execute(() -> taskDao.deleteTaskById(taskId));
    }

    // Retrieve all tasks for a specific user (LiveData for real-time UI updates)
    public LiveData<List<Task>> getTasksByUserId(String userId) {
        return taskDao.getTasksByUserId(userId);
    }

    // Retrieve tasks by category for a specific user
    public LiveData<List<Task>> getTasksByCategory(String userId, String category) {
        return taskDao.getTasksByCategory(userId, category);
    }

    // Retrieve tasks sorted by difficulty
    public LiveData<List<Task>> getTasksByDifficulty(String userId) {
        return taskDao.getTasksByDifficulty(userId);
    }

    // Retrieve tasks due within a specific date range
    public LiveData<List<Task>> getTasksDueInRange(String userId, String startDate, String endDate) {
        return taskDao.getTasksDueInRange(userId, startDate, endDate);
    }

    // Retrieve overdue tasks
    public LiveData<List<Task>> getOverdueTasks(String userId, String currentDate) {
        return taskDao.getOverdueTasks(userId, currentDate);
    }
}
