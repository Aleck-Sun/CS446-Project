package com.example.cs446.ui.components.pets

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val reminderId = intent.getStringExtra("reminder_id") ?: ""
            val isActive = intent.getBooleanExtra("is_active", true)

            if (!isActive) {
                Log.d("ReminderReceiver", "Skipping inactive reminder $reminderId")
                return
            }

            val title = intent.getStringExtra("title") ?: "Reminder"
            val description = intent.getStringExtra("description") ?: ""

            createNotificationChannelOnce(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w("ReminderReceiver", "Notification permission not granted")
                    return
                }
            }

            NotificationManagerCompat.from(context).notify(
                reminderId.hashCode(),
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Error in onReceive", e)
        }
    }

    private fun createNotificationChannelOnce(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager

            // Check if channel already exists
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for pet reminders"
                    enableLights(true)
                    lightColor = android.graphics.Color.GREEN
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "reminder_channel"
    }
}