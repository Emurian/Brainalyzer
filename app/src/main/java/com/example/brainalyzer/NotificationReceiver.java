package com.example.brainalyzer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_notifications";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "Receiver triggered");

        // Create notification channel (Android 8+)
        createNotificationChannel(context);

        // Retrieve task details from intent
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskDueDate = intent.getStringExtra("dueDate");

        if (taskTitle == null || taskDueDate == null) {
            Log.e("NotificationReceiver", "Missing task data. Notification canceled.");
            return;
        }

        // Calculate remaining time
        String notificationMessage = getNotificationMessage(taskDueDate);
        if (notificationMessage == null) {
            Log.d("NotificationReceiver", "Task not due soon. No notification sent.");
            return;
        }

        // Open Dashboard when clicked
        Intent openAppIntent = new Intent(context, DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build and send notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with actual drawable
                .setContentTitle("Reminder: " + taskTitle)
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d("NotificationReceiver", "Notification sent: " + taskTitle);
        } else {
            Log.e("NotificationReceiver", "NotificationManager is null");
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Notifications";
            String description = "Reminders for scheduled tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("NotificationReceiver", "Notification channel created.");
            }
        }
    }

    private String getNotificationMessage(String taskDueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(sdf.parse(taskDueDate));

            long diffInMillis = dueDate.getTimeInMillis() - System.currentTimeMillis();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (diffInDays <= 0) {
                return "Task is due today! Complete it now.";
            } else if (diffInDays == 1) {
                return "Your task is due tomorrow!";
            } else if (diffInDays <= 3) {
                return "Task due in " + diffInDays + " days. Stay ahead!";
            }
        } catch (Exception e) {
            Log.e("NotificationReceiver", "Error parsing due date: " + e.getMessage());
        }
        return null;
    }
}
