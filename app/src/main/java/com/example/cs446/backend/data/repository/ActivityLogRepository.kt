package com.example.cs446.backend.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.ActivityLog
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import java.util.UUID

class ActivityLogRepository {
    private val activityLogsTable = SupabaseClient.supabase.from("activityLogs")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getActivityLogsTableForPet(petId: UUID): List<ActivityLog> {
        // TODO: Remove mock data in future
        val sampleActivityLogs = listOf(
            ActivityLog(
                userId = UUID.randomUUID(),
                petId = petId,
                createdAt = Instant.parse("2025-01-15T10:00:00Z"),
                activityType = "Walk",
                comment = "Took Charlie for a morning walk"
            ),
            ActivityLog(
                userId = UUID.randomUUID(),
                petId = petId,
                createdAt = Instant.parse("2025-01-16T15:30:00Z"),
                activityType = "Vet Visit",
                comment = "Routine checkup for Charlie. Everything looks good!"
            ),
            ActivityLog(
                userId = UUID.randomUUID(),
                petId = petId,
                createdAt = Instant.parse("2025-01-17T19:00:00Z"),
                activityType = "Feeding",
                comment = "Fed Charlie her dinner. She had water during the meal as well."
            )
        )
        return sampleActivityLogs
//        return activityLogsTable.select {
//            filter({
//                eq("pet_id", petId)
//            })
//        }.decodeList<ActivityLog>()
    }

    suspend fun addActivityLog(activityLog: ActivityLog) {
        activityLogsTable.insert(activityLog)
    }
}
