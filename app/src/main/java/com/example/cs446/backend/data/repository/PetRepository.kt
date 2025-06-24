package com.example.cs446.backend.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.Permissions
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.model.UserPetRelation
import com.example.cs446.backend.data.model.UserPetRelationRaw
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class PetRepository {
    private val petsTable = SupabaseClient.supabase.from("pets")
    private val relationsTable = SupabaseClient.supabase.from("user-pet-relations")

    suspend fun getPetsCreatedByUser(userId: UUID): List<Pet> {
        return petsTable.select {
            filter {
                eq("creator_id", userId)
            }
        }.decodeList<Pet>()
    }

    suspend fun getPetsByPostPermission(): List<Pet> {
        val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()?.id
        val validPetIds = relationsTable
            .select()
            .decodeList<UserPetRelationRaw>()
            .filter {
                it.userId.toString() == currentUser
            }
            .map {
                UserPetRelation(
                    userId = it.userId,
                    petId = it.petId,
                    relation = it.relation,
                    permissions = parsePermissions(it.permissions)
                )
            }
            .filter {
                it.permissions.makePosts
            }
            .map { it.petId }

        val pets = petsTable
            .select()
            .decodeList<Pet>()
            .filter{
                validPetIds.contains(it.id)
            }
        return pets
    }

    suspend fun addPet(pet: Pet) {
        petsTable.insert(pet)
    }

    suspend fun updatePetImage(petId: UUID, imageUrl: String) {
        petsTable.update({
            set("image_url", imageUrl)
        }) {
            filter {
                eq("id", petId)
            }
        }
    }

    suspend fun getPet(petId: UUID): Pet {
        // TODO: Remove mocked data
        return Pet(
            petId,
            kotlinx.datetime.Instant.parse("2021-02-03T00:00:00Z"),
            "Charlie",
            "Dog", "Golden Retriever",
            UUID.randomUUID(),
            kotlinx.datetime.Instant.parse("2025-05-28T00:00:00Z"),
            65.0
        )
//        return petsTable.select {
//            filter {
//                eq("id", petId)
//            }
//        }.decodeSingle()
    }

    suspend fun getPetsByIds(petIds: List<UUID>): List<Pet> {
        // TODO: Remove mocked data
        return petIds.map{
            Pet(
                it,
                kotlinx.datetime.Instant.parse("2021-02-03T00:00:00Z"),
                "Charlie",
                "Dog", "Golden Retriever",
                UUID.randomUUID(),
                kotlinx.datetime.Instant.parse("2025-05-28T00:00:00Z"),
                65.0
            )
        }
    }
}
