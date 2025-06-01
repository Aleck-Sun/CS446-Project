package com.example.cs446.data.model
import java.time.OffsetDateTime
import java.time.LocalDate

class Pet(
    val id: Int,
    val created_at: String,    // or OffsetDateTime if you parse manually
    val name: String,
    val species: Int,
    val breed: String?,        // nullable because it can be null
    val creator_id: Int,
    val birthdate: String,     // or LocalDate if you want date parsing
    val weight: Double
) {
}