package com.example.cs446.backend.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.ActivityLog
import com.example.cs446.backend.data.model.ActivityLogType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import java.time.Instant
import java.util.UUID

class ActivityLogRepository {
    private val activityLogsTable = SupabaseClient.supabase.from("activity-logs")
    private val activityLogsTypeTable = SupabaseClient.supabase.from("activity-logs-type")

    suspend fun getActivityLogsTableForPet(petId: UUID): List<ActivityLog> {
        return activityLogsTable.select {
            filter{
                eq("pet_id", petId)
            }
        }.decodeList<ActivityLog>().sortedByDescending { it.createdAt }
    }

    suspend fun getNumberOfLogs(petId: UUID): Int {
        return try {
            activityLogsTable.select {
                filter {
                    eq("pet_id", petId)
                }
                count(Count.EXACT)
            }.countOrNull()!!.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    suspend fun addActivityLog(activityLog: ActivityLog) {
        activityLogsTable.insert(activityLog)
    }

    suspend fun getActivityLogsTypeTableForPet(petId: UUID): List<ActivityLogType> {
        return activityLogsTypeTable.select {
            filter({
                eq("pet_id", petId)
            })
        }.decodeList<ActivityLogType>()
    }

    suspend fun addActivityLogsType(activityLogType: ActivityLogType) {
        activityLogsTypeTable.insert(activityLogType)
    }
}
