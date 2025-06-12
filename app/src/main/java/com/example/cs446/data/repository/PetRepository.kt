package com.example.cs446.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.data.model.Pet
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
}
