package com.example.brainalyzer;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface TaskDao {

    // Insert a new task
    @Insert
    void insertTask(Task task);

    // Update an existing task
    @Update
    void updateTask(Task task);

    // Delete a specific task
    @Delete
    void deleteTask(Task task);

    // Delete a task by its ID
    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    void deleteTaskById(String taskId);

    // Delete all tasks for a user (Useful for logout scenarios)
    @Query("DELETE FROM tasks WHERE userId = :userId")
    void deleteAllTasksForUser(String userId);

    // Get all tasks for a specific user
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksByUserId(String userId);

    // Get tasks for a user filtered by category
    @Query("SELECT * FROM tasks WHERE userId = :userId AND taskCategory = :category ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksByCategory(String userId, String category);

    // Get tasks sorted by difficulty (highest first), then due date
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY difficulty DESC, dueDate ASC")
    LiveData<List<Task>> getTasksByDifficulty(String userId);

    // Get tasks due within a date range
    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksDueInRange(String userId, String startDate, String endDate);

    // Get overdue tasks for a user
    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueDate < :currentDate ORDER BY dueDate ASC")
    LiveData<List<Task>> getOverdueTasks(String userId, String currentDate);

    // Get a task by its taskId
    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    Task getTaskById(String taskId);
}
