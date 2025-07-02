package com.example.cs446.backend.data.model

import java.util.UUID

data class Handler(
    val userId: UUID,
    val name: String,
    val role: String,
    val permissions: Permissions
)