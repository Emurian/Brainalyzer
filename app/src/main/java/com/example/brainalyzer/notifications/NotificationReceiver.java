package com.example.brainalyzer.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("com.example.brainalyzer.NOTIFY_TASK")) {
            String taskTitle = intent.getStringExtra("task_title");
            String dueDateStr = intent.getStringExtra("task_due_date");

            if (taskTitle != null && dueDateStr != null) {
                Log.d("NotificationReceiver", "Received notification trigger for task: " + taskTitle);
                NotificationHelper.sendTaskNotification(context, taskTitle, dueDateStr);
            } else {
                Log.e("NotificationReceiver", "Missing taskTitle or dueDateStr in intent!");
            }
        } else {
            Log.w("NotificationReceiver", "Received unrecognized intent action.");
        }
    }
}
