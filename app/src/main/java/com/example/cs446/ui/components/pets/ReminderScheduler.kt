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

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun scheduleReminder(context: Context, reminder: Reminder): Boolean {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
                    return false
                }
            }

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title", reminder.title)
                putExtra("description", reminder.description ?: "")
                putExtra("reminder_id", reminder.id.toString())
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = reminder.time
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )

            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when scheduling exact alarm", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder", e)
            false
        }
    }
}