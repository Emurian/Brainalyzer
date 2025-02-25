package com.example.brainalyzer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.lifecycle.LiveData;

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
    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTaskById(int taskId);

    // Get all tasks for a specific user
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksByUserId(String userId);

    // Get tasks for a specific user, filtered by category
    @Query("SELECT * FROM tasks WHERE userId = :userId AND taskCategory = :category ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksByCategory(String userId, String category);

    // Get tasks for a specific user, sorted by difficulty (higher difficulty first)
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY difficulty DESC, dueDate ASC")
    LiveData<List<Task>> getTasksByDifficulty(String userId);

    // Get tasks for a specific user that are due in the next X days
    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    LiveData<List<Task>> getTasksDueInRange(String userId, String startDate, String endDate);

    // Get overdue tasks for a user
    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueDate < :currentDate ORDER BY dueDate ASC")
    LiveData<List<Task>> getOverdueTasks(String userId, String currentDate);
}
