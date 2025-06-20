package com.example.cs446.backend.data.repository

import com.example.cs446.backend.data.model.Permissions
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import kotlinx.datetime.Instant
import java.util.UUID

class UUIDAdapter {

    @ToJson
    fun toJson(uuid: UUID): String = uuid.toString()

    @FromJson
    fun fromJson(uuid: String): UUID = UUID.fromString(uuid)
}

class InstantAdapter {
    @FromJson
    fun fromJson(value: String?): Instant? = value?.let { Instant.parse(it) }

    @ToJson
    fun toJson(value: Instant?): String? = value?.toString()
}

fun parsePermissions(list: List<String>): Permissions {
    return Permissions(
        editLogs = "edit_logs" in list,
        setReminders = "set_reminders" in list,
        inviteHandlers = "invite_handlers" in list,
        makePosts = "make_posts" in list,
        editPermissionsOfOthers = "edit_permissionsOfOthers" in list
    )
}