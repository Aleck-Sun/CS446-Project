package com.example.cs446.backend.data.model

data class Permissions(
    val editLogs: Boolean = false,
    val setReminders: Boolean = false,
    val inviteHandlers: Boolean = false,
    val makePosts: Boolean = false,
    val editPermissionsOfOthers: Boolean = false
) {
    fun toList(): List<String> {
        val result = mutableListOf<String>()
        if (editLogs) result.add("edit_logs")
        if (setReminders) result.add("set_reminders")
        if (inviteHandlers) result.add("invite_handlers")
        if (makePosts) result.add("make_posts")
        if (editPermissionsOfOthers) result.add("edit_permissions_of_others")
        return result
    }

    companion object {
        fun fromList(permissions: List<String>): Permissions {
            return Permissions(
                editLogs = "edit_logs" in permissions,
                setReminders = "set_reminders" in permissions,
                inviteHandlers = "invite_handlers" in permissions,
                makePosts = "make_posts" in permissions,
                editPermissionsOfOthers = "edit_permissions_of_others" in permissions
            )
        }
    }
}