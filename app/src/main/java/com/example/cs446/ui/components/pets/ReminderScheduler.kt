package com.example.cs446.ui.components.pets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.cs446.backend.data.model.Reminder
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun scheduleReminder(context: Context, reminder: Reminder): Boolean {
        if (!reminder.active) {
            Log.d(TAG, "Not scheduling inactive reminder ${reminder.id}")
            return false
        }

        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Check for exact alarm permission (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Exact alarm permission not granted")
                return false
            }

            // Convert reminder time to system timezone first
            val zonedTime = reminder.time.atZone(ZoneId.systemDefault())
            val triggerTime = zonedTime.toInstant().toEpochMilli()

            // Verify the time is in the future
            if (triggerTime <= System.currentTimeMillis()) {
                Log.w(TAG, "Reminder time is in the past: ${reminder.time}")
                return false
            }

            val pendingIntent = createPendingIntent(context, reminder)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Log.d(TAG, "Reminder scheduled for ${reminder.time} (trigger time: $triggerTime)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder", e)
            false
        }
    }

    fun cancelReminder(context: Context, reminderId: UUID) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
                Log.d(TAG, "Cancelled reminder $reminderId")
            } ?: Log.d(TAG, "No pending intent found for reminder $reminderId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminder $reminderId", e)
        }
    }

    private fun createPendingIntent(context: Context, reminder: Reminder): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.cs446.REMINDER_ACTION"
                putExtra("reminder_id", reminder.id.toString())
                putExtra("title", reminder.title)
                putExtra("description", reminder.description ?: "")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }
}
