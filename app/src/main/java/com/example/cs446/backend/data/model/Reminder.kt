package com.example.cs446.backend.data.model

import com.squareup.moshi.Json
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.UUID

data class ReminderRaw(
    @Json(name = "id") val id: UUID,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "pet_id") val petId: UUID,
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "time") val time: String,
    @Json(name = "active") val active: Boolean
) {
    fun toReminder(): Reminder {
        return Reminder(
            id = this.id,
            createdAt = Instant.parse(this.createdAt),
            petId = this.petId,
            userId = this.userId,
            title = this.title,
            description = this.description,
            time = OffsetDateTime.parse(this.time).toZonedDateTime(),
            active = this.active
        )
    }
}

data class Reminder(
    val id: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val petId: UUID,
    val userId: UUID,
    val title: String,
    val description: String,
    val time: ZonedDateTime,
    val active: Boolean = true
) {
    fun toReminderRaw(): ReminderRaw {
        return ReminderRaw(
            id = this.id,
            createdAt = this.createdAt.toString(),
            petId = this.petId,
            userId = this.userId,
            title = this.title,
            description = this.description,
            time = this.time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            active = this.active
        )
    }
}