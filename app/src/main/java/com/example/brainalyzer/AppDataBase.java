package com.example.brainalyzer;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Task.class}, version = 5, exportSchema = false)  // 🔹 Increase version
public abstract class AppDataBase extends RoomDatabase {

    private static volatile AppDataBase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public abstract TaskDao taskDao();

    public static AppDataBase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDataBase.class, "task_database")
                            .addMigrations(MIGRATION_4_5) // ✅ Apply the correct migration
                            .fallbackToDestructiveMigration() // 🔹 Resets DB if migration fails
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    // ✅ **Fixed Migration: Preserves "taskId" as Primary Key**
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Step 1: Create new table with "taskId" as the primary key
            database.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (" +
                    "taskId TEXT PRIMARY KEY NOT NULL, " +  // 🔹 Keep "taskId" instead of "id"
                    "name TEXT NOT NULL, " +
                    "dueDate TEXT NOT NULL, " +
                    "difficulty TEXT, " +
                    "taskCategory TEXT, " +
                    "userId TEXT NOT NULL)");

            // Step 2: Copy data from old table to new table
            database.execSQL("INSERT INTO tasks_new (taskId, name, dueDate, difficulty, taskCategory, userId) " +
                    "SELECT taskId, name, dueDate, difficulty, taskCategory, userId FROM tasks");

            // Step 3: Drop the old "tasks" table
            database.execSQL("DROP TABLE tasks");

            // Step 4: Rename "tasks_new" to "tasks"
            database.execSQL("ALTER TABLE tasks_new RENAME TO tasks");
        }
    };
}
