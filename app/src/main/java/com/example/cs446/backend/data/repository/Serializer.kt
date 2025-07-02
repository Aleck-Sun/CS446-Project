package com.example.cs446.backend.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.cs446.backend.data.model.Breed
import com.example.cs446.backend.data.model.Permissions
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.model.PetRaw
import com.example.cs446.backend.data.model.Species
import com.example.cs446.backend.data.model.UserPetRelation
import com.example.cs446.backend.data.model.UserPetRelationRaw
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

// TODO: Merge with kotlin instant adapter
class JavaTimeInstantAdapter {
    @RequiresApi(Build.VERSION_CODES.O)
    @FromJson
    fun fromJson(value: String?): java.time.Instant? = value?.let { java.time.Instant.parse(it) }

    @ToJson
    fun toJson(value: java.time.Instant?): String? = value?.toString()
}

fun parsePermissions(list: List<String>): Permissions {
    return Permissions(
        editLogs = "edit_logs" in list,
        setReminders = "set_reminders" in list,
        inviteHandlers = "invite_handlers" in list,
        makePosts = "make_posts" in list,
        editPermissionsOfOthers = "edit_permissionsOfOthers" in list
    )
}

fun parsePet(petRaw: PetRaw): Pet {
    val species = try {
        Species.valueOf(petRaw.species.uppercase())
    } catch (e: IllegalArgumentException) {
        Species.OTHER
    }
    val breed = try {
        Breed.valueOf(petRaw.breed.uppercase())
    } catch (e: IllegalArgumentException) {
        Breed.OTHER
    }
    return Pet(
        id = petRaw.id,
        createdAt = petRaw.createdAt,
        name = petRaw.name,
        species = species,
        breed = breed,
        createdBy = petRaw.createdBy,
        birthdate = petRaw.birthdate,
        weight = petRaw.weight,
        imageUrl = petRaw.imageUrl
    )
}

fun petToRaw(pet: Pet): PetRaw {
    return PetRaw(
        id = pet.id,
        createdAt = pet.createdAt,
        name = pet.name,
        species = pet.species.name.lowercase(),
        breed = pet.breed.name.lowercase(),
        createdBy = pet.createdBy,
        birthdate = pet.birthdate,
        weight = pet.weight,
        imageUrl = pet.imageUrl
    )
}

fun userPetRelationToRaw(userPetRelation: UserPetRelation): UserPetRelationRaw {
    return UserPetRelationRaw(
        userId = userPetRelation.userId,
        petId = userPetRelation.petId,
        relation = userPetRelation.relation,
        permissions = userPetRelation.permissions.toList()
    )
}

fun userPetRelationFromRaw(userPetRelationRaw: UserPetRelationRaw): UserPetRelation {
    return UserPetRelation(
        userId = userPetRelationRaw.userId,
        petId = userPetRelationRaw.petId,
        relation = userPetRelationRaw.relation,
        permissions = Permissions(
            editLogs = "edit_logs" in userPetRelationRaw.permissions,
            setReminders = "set_reminders" in userPetRelationRaw.permissions,
            inviteHandlers = "invite_handlers" in userPetRelationRaw.permissions,
            makePosts = "make_posts" in userPetRelationRaw.permissions,
            editPermissionsOfOthers = "edit_permissions_of_others" in userPetRelationRaw.permissions
        )
    )
}