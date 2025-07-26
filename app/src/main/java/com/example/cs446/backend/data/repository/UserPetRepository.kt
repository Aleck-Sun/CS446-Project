package com.example.cs446.backend.data.repository

import android.util.Log
import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.Handler
import com.example.cs446.backend.data.model.UserPetRelation
import com.example.cs446.backend.data.model.UserPetRelationRaw
import io.github.jan.supabase.postgrest.from
import java.util.UUID

class UserPetRepository {
    private val userPetsTable = SupabaseClient.supabase.from("user-pet-relations")
    private val userRepository = UserRepository()

    private suspend fun getUsersForPet(petId: UUID): List<UserPetRelation> {
        return try {
            userPetsTable
                .select {
                    filter {
                        eq("pet_id", petId)
                    }
                }
                .decodeList<UserPetRelationRaw>()
                .map { it.toUserPetRelation() }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun getHandlersForPet(petId: UUID): List<Handler> {
        val userPetRelations = getUsersForPet(petId)

        if (userPetRelations.isEmpty()) {
            return emptyList()
        }

        val handlers = userPetRelations.mapNotNull { userPetRelation ->
            val user =
                userPetRelation.let { userRepository.getUserById(it.userId) }

            if (user == null) {
                Log.w(
                    "UserPetRepo",
                    "User not found for userId: ${userPetRelation?.userId} in pet $petId. Skipping handler."
                )
                null
            } else {
                Handler(
                    userId = user.id,
                    name = user.username,
                    role = userPetRelation.relation ?: "Unknown Role",
                    permissions = userPetRelation.permissions
                )
            }
        }
        return handlers.sortedWith(compareByDescending { it.role == "Owner" })
    }

    suspend fun getPetRelationsForUser(userId: UUID): List<UserPetRelation> {
        return try {
            userPetsTable.select {
                filter {
                    eq("user_id", userId)
                }
            }
                .decodeList<UserPetRelationRaw>()
                .map { it.toUserPetRelation() }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun getRelationForUserAndPet(petId: UUID, userId: UUID): UserPetRelation? {
        return try {
            userPetsTable.select {
                filter {
                    eq("pet_id", petId)
                    eq("user_id", userId)
                }
            }
                .decodeSingleOrNull<UserPetRelationRaw>()
                ?.let { it.toUserPetRelation() }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun updateRelation(userPetRelation: UserPetRelation) {
        val userPetRelationRaw = userPetRelationToRaw(userPetRelation)
        userPetsTable.update({
            set("relation", userPetRelationRaw.relation)
            set("permissions", userPetRelationRaw.permissions)
        }) {
            filter {
                eq("pet_id", userPetRelationRaw.petId)
                eq("user_id", userPetRelationRaw.userId)
            }
        }
    }

    suspend fun addUserPetRelation(relation: UserPetRelation) {
        userPetsTable.insert(userPetRelationToRaw(relation))
    }

    suspend fun deleteUserAndPetRelation(petId: UUID, userId: UUID) {
        userPetsTable.delete {
            filter {
                eq("pet_id", petId)
                eq("user_id", userId)
            }
        }
    }
}