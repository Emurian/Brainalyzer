package com.example.brainalyzer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationHelper {
    private static final String CHANNEL_ID = "task_notifications";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "Reminders for scheduled tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("NotificationHelper", "Notification channel created.");
            }
        }
    }

    public static void sendTaskNotification(Context context, String taskTitle, String dueDateStr) {
        // ✅ Check if notification permission is granted (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationHelper", "Notification permission not granted. Skipping notification.");
                return; //
            }
        }

        // ✅ Calculate the time difference
        String message = getNotificationMessage(dueDateStr);
        if (message == null) {
            Log.d("NotificationHelper", "No notification needed for this task.");
            return; // 🚨 Exit if no relevant message
        }

        // ✅ Create intent to open the Dashboard when the notification is clicked
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // ✅ Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Task Reminder: " + taskTitle)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // ✅ Send notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(taskTitle.hashCode(), builder.build());
        Log.d("NotificationHelper", "Notification sent: " + taskTitle + " - " + message);
    }

    private static String getNotificationMessage(String dueDateStr) {
        try {
            // ✅ Parse the due date (Now in dd-MM-yyyy format)
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date dueDate = sdf.parse(dueDateStr);
            if (dueDate == null) return null;

            // ✅ Get current date and calculate difference
            Calendar today = Calendar.getInstance();
            long diffInMillis = dueDate.getTime() - today.getTimeInMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            // ✅ Determine the message based on days left
            if (diffInDays == 7) {
                return "Your task is due in a week. Start working on it!";
            } else if (diffInDays == 3) {
                return "Only 3 days left! Keep going!";
            } else if (diffInDays == 1) {
                return "Your task is due tomorrow!";
            } else if (diffInDays == 0) {
                return "Your task is due today! Don't miss it!";
            } else if (diffInDays < 0) {
                return "Your task is overdue! Complete it as soon as possible!";
            }
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error parsing due date: " + e.getMessage());
        }
        return null;
    }
}
