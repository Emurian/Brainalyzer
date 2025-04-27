package com.example.brainalyzer.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class TaskScheduler {

    public static void scheduleTaskNotification(Context context, long dueDateMillis, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);

        // Unique PendingIntent for each task
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule notification at the specified due date
        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    dueDateMillis,
                    pendingIntent
            );
        }
    }

    public static void cancelTaskNotification(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    // Example: Schedule notification 1 day before due date
    public static void scheduleReminderBeforeDueDate(Context context, long dueDateMillis, int taskId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dueDateMillis);
        calendar.add(Calendar.DAY_OF_YEAR, -1); // 1 day before the due date

        scheduleTaskNotification(context, calendar.getTimeInMillis(), taskId);
    }
}
