package com.example.cs446.backend.data.repository

import io.github.jan.supabase.postgrest.from
import java.util.UUID

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.Reminder
import com.example.cs446.backend.data.model.ReminderRaw
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReminderRepository {
    private val remindersTable = SupabaseClient.supabase.from("reminders")

    suspend fun getReminder(reminderId: UUID): Reminder? {
        return try {
            remindersTable.select {
                filter {
                    eq("id", reminderId)
                }
            }.decodeSingle<ReminderRaw>().toReminder()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRemindersForPet(petId: UUID): List<Reminder> {
        return try {
            remindersTable.select {
                filter {
                    eq("pet_id", petId)
                }
            }.decodeList<ReminderRaw>()
                .map { it.toReminder() }
                .sortedBy { it.time }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addReminder(reminder: Reminder) {
        remindersTable.insert(reminder.toReminderRaw())
    }

    suspend fun updateReminder(
        reminderId: UUID,
        title: String? = null,
        description: String? = null,
        time: LocalDateTime? = null,
        active: Boolean? = null
    ) {
        remindersTable.update({
            title?.let { set("title", it) }
            description?.let { set("description", it) }
            time?.let {
                set(
                    "time",
                    it.atZone(ZoneId.of("America/Toronto")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                )
            }
            active?.let { set("active", it) }
        }) {
            filter {
                eq("id", reminderId)
            }
        }
    }
}