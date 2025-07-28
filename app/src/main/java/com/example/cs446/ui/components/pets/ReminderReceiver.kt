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
import com.example.cs446.backend.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val reminderId = intent.getStringExtra("reminder_id") ?: ""
            val active = intent.getBooleanExtra("active", false)

            if (!active) {
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

            // Deliver notification
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

            // Delete reminder
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uuid = UUID.fromString(reminderId)
                    ReminderRepository().deleteReminder(uuid)
                    Log.d("ReminderReceiver", "Deleted reminder $reminderId after triggering")
                } catch (e: Exception) {
                    Log.e("ReminderReceiver", "Failed to delete reminder $reminderId", e)
                }
            }
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