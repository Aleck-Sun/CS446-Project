package com.example.cs446.backend.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.Badge
import com.example.cs446.backend.data.model.BadgeType
import com.example.cs446.backend.data.model.getBadgeText
import com.example.cs446.backend.data.model.getUrlEndingForBadge
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class BadgeRepository {
    private val badgesTable = SupabaseClient.supabase.from("badges")
    val badgesUrlBase = "https://vyruhoywijnvfombpnuz.supabase.co/storage/v1/object/public/badges//"

    suspend fun getAllBadgesForPets(petIds: List<UUID>): Map<UUID, List<Badge>> {
        return try {
            badgesTable.select {
                filter {
                    isIn("pet_id", petIds)
                }
            }.decodeList<Badge>().map{
                it.copy(
                    imageUrl = getImageUrlForBadge(it.type),
                    text = getBadgeText(it.type, it.tier)
                )
            }.groupBy { it.petId }
        } catch (e: Exception) {
            e.printStackTrace()
            return mapOf()
        }
    }

    suspend fun createOrUpdateBadgeForPet(
        badge: Badge
    ) {
        try {
            badgesTable.upsert(
                badge
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getImageUrlForBadge(
        type: BadgeType
    ): String {
        return "${badgesUrlBase}${getUrlEndingForBadge(type)}"
    }
}