package com.example.brainalyzer.notifications;

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
import androidx.core.content.ContextCompat;

import com.example.brainalyzer.DashboardActivity;
import com.example.brainalyzer.R;

import java.text.SimpleDateFormat;
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
        if (!isNotificationPermissionGranted(context)) {
            Log.w("NotificationHelper", "Permission not granted. Cannot show task notification.");
            return;
        }

        if (taskTitle == null || dueDateStr == null) {
            Log.e("NotificationHelper", "Task title or due date is null.");
            return;
        }

        String message = getImmediateNotificationMessage(dueDateStr);
        if (message != null) {
            sendNotification(context, "Task Reminder: " + taskTitle, message, taskTitle.hashCode());
        }
    }

    public static void sendTaskAddedNotification(Context context, String taskTitle, String dueDateStr) {
        if (!isNotificationPermissionGranted(context)) {
            Log.w("NotificationHelper", "Permission not granted. Cannot show task added notification.");
            return;
        }

        if (taskTitle == null || dueDateStr == null) {
            Log.e("NotificationHelper", "Task title or due date is null.");
            return;
        }

        long remainingDays = getDaysRemaining(dueDateStr);
        String message = (remainingDays > 0)
                ? "Task Added! " + remainingDays + " day(s) left to finish!"
                : "Task Added! Already overdue!";

        sendNotification(context, "New Task: " + taskTitle, message, (taskTitle + "_added").hashCode());
    }

    private static void sendNotification(Context context, String title, String message, int notificationId) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int icon = R.drawable.ic_notification; // Replace if needed
        if (icon == 0) {
            icon = android.R.drawable.ic_dialog_info;
            Log.w("NotificationHelper", "Fallback icon used for notification.");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            Log.d("NotificationHelper", "Notification sent: " + title + " | " + message);
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "SecurityException: " + e.getMessage());
        }
    }

    private static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission automatically granted on lower versions
    }

    private static String getImmediateNotificationMessage(String dueDateStr) {
        try {
            long days = getDaysRemaining(dueDateStr);
            if (days > 1) {
                return "Your task is due in " + days + " days.";
            } else if (days == 1) {
                return "Your task is due tomorrow!";
            } else if (days == 0) {
                return "Your task is due today! Don't miss it!";
            } else {
                return "Your task is overdue! Complete it ASAP!";
            }
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error parsing due date: " + e.getMessage());
            return null;
        }
    }

    private static long getDaysRemaining(String dueDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date dueDate = sdf.parse(dueDateStr);
            if (dueDate == null) return -1;
            long diffInMillis = dueDate.getTime() - System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toDays(diffInMillis);
        } catch (Exception e) {
            Log.e("NotificationHelper", "Error calculating remaining days: " + e.getMessage());
            return -1;
        }
    }
}
