package com.example.cs446.backend.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.ActivityLog
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import java.util.UUID

class ActivityLogRepository {
    private val activityLogsTable = SupabaseClient.supabase.from("activity-logs")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getActivityLogsTableForPet(petId: UUID): List<ActivityLog> {
        return activityLogsTable.select {
            filter({
                eq("pet_id", petId)
            })
        }.decodeList<ActivityLog>()
    }

    suspend fun addActivityLog(activityLog: ActivityLog) {
        activityLogsTable.insert(activityLog)
    }
}
