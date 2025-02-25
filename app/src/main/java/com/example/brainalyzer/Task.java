package com.example.brainalyzer;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;  // Ensuring name is non-null

    @NonNull
    private String dueDate;  // Format: dd-MM-yyyy

    private String difficulty;
    private String taskCategory;

    @NonNull
    private String userId;  // Ensuring userId is non-null for multi-user support

    // No-argument constructor for Room
    public Task() {
        this.name = "";
        this.dueDate = "";
        this.userId = "";  // Ensuring a default userId
    }

    // Constructor for inserting tasks manually (now includes userId)
    @Ignore
    public Task(@NonNull String name, @NonNull String dueDate, String difficulty, String taskCategory, @NonNull String userId) {
        this.name = name;
        this.dueDate = dueDate;
        this.difficulty = difficulty;
        this.taskCategory = taskCategory;
        this.userId = userId;
    }

    // Constructor with ID for updating or reading tasks
    public Task(int id, @NonNull String name, @NonNull String dueDate, String difficulty, String taskCategory, @NonNull String userId) {
        this.id = id;
        this.name = name;
        this.dueDate = dueDate;
        this.difficulty = difficulty;
        this.taskCategory = taskCategory;
        this.userId = userId;
    }

    // Parcelable constructor
    protected Task(Parcel in) {
        id = in.readInt();
        name = in.readString();
        dueDate = in.readString();
        difficulty = in.readString();
        taskCategory = in.readString();
        userId = in.readString();
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
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(dueDate);
        dest.writeString(difficulty);
        dest.writeString(taskCategory);
        dest.writeString(userId);
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(@NonNull String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
}
