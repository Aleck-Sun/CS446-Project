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
import com.example.cs446.ui.pages.main.SYSTEM_TIMEZONE
import com.example.cs446.ui.pages.main.SYSTEM_ZONE_ID

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun scheduleReminder(context: Context, reminder: Reminder): Boolean {
        // Don't schedule if not active
        if (!reminder.active) {
            Log.d(TAG, "Not scheduling inactive reminder ${reminder.id}")
            return false
        }

        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Exact alarm permission not granted")
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return false
                }
            }

            val pendingIntent = createPendingIntent(context, reminder)

            val triggerTime = reminder.time
                .atZone(SYSTEM_ZONE_ID)
                .toInstant()
                .toEpochMilli()

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

            Log.d(TAG, "Reminder scheduled for ${reminder.time}")
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
            } ?: run {
                Log.d(TAG, "No pending intent found for reminder $reminderId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminder", e)
        }
    }

    private fun createPendingIntent(context: Context, reminder: Reminder): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title", reminder.title)
                putExtra("description", reminder.description ?: "")
                putExtra("reminder_id", reminder.id.toString())
                putExtra("is_active", reminder.active)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
