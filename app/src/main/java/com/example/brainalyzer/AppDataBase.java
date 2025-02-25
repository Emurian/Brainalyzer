package com.example.brainalyzer;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Task.class}, version = 3, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    private static volatile AppDataBase INSTANCE;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public abstract TaskDao taskDao();

    public static AppDataBase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDataBase.class, "task_database")
                            .addMigrations(MIGRATION_2_3)
                            .fallbackToDestructiveMigration()  // WARNING: Resets database if migration fails
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    // ✅ **Fixed Migration from version 2 to 3**
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Step 1: Create new `tasks_new` table with correct constraints
            database.execSQL("CREATE TABLE IF NOT EXISTS tasks_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "dueDate TEXT NOT NULL, " +
                    "difficulty TEXT, " +
                    "taskCategory TEXT, " +
                    "userId TEXT NOT NULL DEFAULT '' " +
                    ")");

            // Step 2: Copy old data to new table (fill `userId` with default value)
            database.execSQL("INSERT INTO tasks_new (id, name, dueDate, difficulty, taskCategory, userId) " +
                    "SELECT id, name, dueDate, difficulty, taskCategory, '' FROM tasks");

            // Step 3: Remove old `tasks` table
            database.execSQL("DROP TABLE tasks");

            // Step 4: Rename `tasks_new` to `tasks`
            database.execSQL("ALTER TABLE tasks_new RENAME TO tasks");
        }
    };
}
