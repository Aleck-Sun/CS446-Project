package com.example.cs446.data.repository

import com.squareup.moshi.FromJson
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