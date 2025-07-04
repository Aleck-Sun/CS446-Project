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
import io.github.jan.supabase.storage.storage
import java.util.UUID
import kotlin.time.Duration

class PetRepository {
    private val petsTable = SupabaseClient.supabase.from("pets")
    private val relationsTable = SupabaseClient.supabase.from("user-pet-relations")
    private val storage = SupabaseClient.supabase.storage

    suspend fun getPetsCreatedByUser(userId: UUID): List<Pet> {
        val petRawList = petsTable.select {
            filter {
                eq("created_by", userId)
            }
        }.decodeList<PetRaw>()
        return petRawList.map { parsePet(it) }
    }

    suspend fun getPetsRelatedToUser(userId: UUID): List<Pet> {
        val relatedPetIds = relationsTable.select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<UserPetRelationRaw>()
            .map { it.petId }

        if (relatedPetIds.isEmpty()) {
            return emptyList()
        }
        val petRawList = petsTable.select()
            .decodeList<PetRaw>()
            .filter { relatedPetIds.contains(it.id) }

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

    suspend fun updatePet(pet: Pet) {
        val petRaw = petToRaw(pet)
        petsTable.update({
            set("name", petRaw.name)
            set("species", petRaw.species)
            set("breed", petRaw.breed)
            set("birthdate", petRaw.birthdate)
            set("weight", petRaw.weight)
            set("image_url", petRaw.imageUrl)
        }) {
            filter {
                eq("id", pet.id)
            }
        }
    }

    // Delete all relations for the pet
    // Does not delete the actual pet from the petsTable
    //   because it might still be referred to in other tables, such as posts.
    suspend fun deletePet(petId: UUID) {
        relationsTable.delete {
            filter {
                eq("pet_id", petId)
            }
        }
    }

    suspend fun getPet(petId: UUID): Pet {
        return parsePet(
            petsTable.select {
                filter {
                    eq("id", petId)
                }
            }.decodeSingle<PetRaw>()
        ).let {
            it.copy(
                imageUrl = getSignedPetImageUrl(it.imageUrl?:"user.png")
            )
        }
    }

    suspend fun getPetsByIds(petIds: List<UUID>): List<Pet> {
        return petsTable.select {
            filter {
                isIn("id", petIds)
            }
        }.decodeList<PetRaw>().map{
            parsePet(it).copy(
                imageUrl = getSignedPetImageUrl(it.imageUrl?:"user.png")
            )
        }
    }

    suspend fun getSignedPetImageUrl(imageUrl: String): String {
        return try {
            storage.from("avatars").createSignedUrl( // TODO - this is for user avatars, need to fetch from pets
                path = imageUrl,
                expiresIn = Duration.parse("12h")  // expires in 12 hours
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
