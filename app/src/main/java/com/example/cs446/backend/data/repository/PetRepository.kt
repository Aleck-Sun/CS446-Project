package com.example.cs446.backend.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.Pet
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class PetRepository {
    private val petsTable = SupabaseClient.supabase.from("pets")

    suspend fun getPetsForUser(userId: UUID): List<Pet> {
        return petsTable.select {
            filter {
                eq("creator_id", userId)
            }
        }.decodeList<Pet>()
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
            1, "Golden Retriever",
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
