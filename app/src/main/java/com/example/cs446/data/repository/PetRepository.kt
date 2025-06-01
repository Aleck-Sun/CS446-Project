package com.example.cs446.data.repository

import com.example.cs446.SupabaseClient
import com.example.cs446.data.model.Pet
import io.github.jan.supabase.postgrest.from

class PetRepository {
    private val petsTable = SupabaseClient.supabase.from("pets")

    suspend fun getPetsForUser(userId: Int): List<Pet> {
        return petsTable.select {
            filter( {
                eq("creator_id", 0)
            })
        }.decodeList<Pet>()
    }

    suspend fun addPet(pet: Pet) {
        petsTable.insert(pet)
    }
}