package com.example.cs446.backend.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.Breed
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.model.PetRaw
import com.example.cs446.backend.data.model.Species
import com.example.cs446.backend.data.model.UserPetRelation
import com.example.cs446.backend.data.model.UserPetRelationRaw
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class PetRepository {
    private val petsTable = SupabaseClient.supabase.from("pets")
    private val relationsTable = SupabaseClient.supabase.from("user-pet-relations")

    suspend fun getPetsCreatedByUser(userId: UUID): List<Pet> {
        val petRawList = petsTable.select {
            filter {
                eq("created_by", userId)
            }
        }.decodeList<PetRaw>()
        return petRawList.map { parsePet(it) }
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

        val petRawList = petsTable
            .select()
            .decodeList<PetRaw>()
            .filter{
                validPetIds.contains(it.id)
            }
        return petRawList.map { parsePet(it) }
    }

    suspend fun addPet(pet: Pet) {
        petsTable.insert(petToRaw(pet))
    }

    suspend fun addUserPetRelation(relation: UserPetRelation) {
        relationsTable.insert(userPetRelationToRaw(relation))
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
            Species.DOG, Breed.GOLDEN_RETRIEVER,
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
}
