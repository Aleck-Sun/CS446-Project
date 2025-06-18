package com.example.cs446.data.model

data class Permissions(
    val editLogs: Boolean = false,
    val setReminders: Boolean = false,
    val inviteHandlers: Boolean = false,
    val makePosts: Boolean = false,
    val editPermissionsOfOthers: Boolean = false
)