package com.example.cs446.backend.data.repository

import io.github.jan.supabase.postgrest.from
import java.util.UUID

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.Reminder
import com.example.cs446.backend.data.model.ReminderRaw

class ReminderRepository {
    private val remindersTable = SupabaseClient.supabase.from("reminders")

    suspend fun getRemindersForPet(petId: UUID): List<Reminder> {
        return remindersTable.select {
            filter {
                eq("pet_id", petId)
            }
        }.decodeList<ReminderRaw>()
            .map { it.toReminder() }
            .sortedBy { it.time }
    }

    suspend fun addReminder(reminder: Reminder) {
        remindersTable.insert(reminder.toReminderRaw())
    }

    // TODO: update reminder

    suspend fun deleteReminder(reminderId: UUID) {
        remindersTable.delete {
            filter {
                eq("id", reminderId)
            }
        }
    }
}