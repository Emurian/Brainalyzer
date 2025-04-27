package com.example.brainalyzer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Entity(tableName = "tasks")
public class Task implements Parcelable {

    @PrimaryKey
    @NonNull
    private String taskId;

    @NonNull
    private String name;

    private long dueDate; // Stored as Unix timestamp

    private int difficulty;  // Difficulty as int (1 = Low, 2 = Medium, 3 = High)

    @NonNull
    private String taskCategory;

    @NonNull
    private String userId;

    private boolean completed; // ✅ Added field to track completion status

    // 🔹 No-argument constructor (Required for Firestore & Room)
    public Task() {
        this.taskId = UUID.randomUUID().toString(); // Ensures unique ID
        this.name = "Untitled Task";
        this.dueDate = System.currentTimeMillis();  // Default to current time
        this.difficulty = 1; // Default to Low difficulty
        this.taskCategory = "General";
        this.userId = "unknown_user"; // Prevents null userId
        this.completed = false; // Default to incomplete task
    }

    // 🔹 Constructor for new task creation
    @Ignore
    public Task(@NonNull String name, long dueDate, int difficulty, String taskCategory, @NonNull String userId, boolean completed) {
        this.taskId = UUID.randomUUID().toString();
        this.name = name;
        this.dueDate = dueDate;
        this.difficulty = validateDifficulty(difficulty);
        this.taskCategory = (taskCategory == null || taskCategory.isEmpty()) ? "General" : taskCategory;
        this.userId = userId;
        this.completed = completed;
    }

    // 🔹 Constructor with taskId (used for Firestore updates)
    @Ignore
    public Task(@NonNull String taskId, @NonNull String name, long dueDate, int difficulty, String taskCategory, @NonNull String userId, boolean completed) {
        this.taskId = (taskId == null || taskId.isEmpty()) ? UUID.randomUUID().toString() : taskId;
        this.name = name;
        this.dueDate = dueDate;
        this.difficulty = validateDifficulty(difficulty);
        this.taskCategory = (taskCategory == null || taskCategory.isEmpty()) ? "General" : taskCategory;
        this.userId = userId;
        this.completed = completed;
    }

    // 🔹 Parcelable constructor (includes completed status)
    protected Task(Parcel in) {
        taskId = in.readString();
        name = in.readString();
        dueDate = in.readLong();
        difficulty = validateDifficulty(in.readInt());
        taskCategory = (in.readString() == null || in.readString().trim().isEmpty()) ? "General" : in.readString();
        userId = in.readString();
        completed = in.readByte() != 0; // Read completed status as boolean
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(name);
        dest.writeLong(dueDate);
        dest.writeInt(difficulty);
        dest.writeString(taskCategory);
        dest.writeString(userId);
        dest.writeByte((byte) (completed ? 1 : 0)); // Store completed status
    }

    // 🔹 Getters & Setters
    @NonNull
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(@NonNull String taskId) {
        this.taskId = (taskId.isEmpty()) ? UUID.randomUUID().toString() : taskId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = validateDifficulty(difficulty);
    }

    @NonNull
    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = (taskCategory == null || taskCategory.isEmpty()) ? "General" : taskCategory;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    // ✅ Getters and Setters for Completion Status
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // 🔹 Convert Due Date to Readable Format
    @Exclude
    public String getFormattedDueDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date(dueDate));
    }

    // 🔹 Validate Difficulty
    private int validateDifficulty(int difficulty) {
        return (difficulty >= 1 && difficulty <= 3) ? difficulty : 1;  // Ensures valid range
    }
}
