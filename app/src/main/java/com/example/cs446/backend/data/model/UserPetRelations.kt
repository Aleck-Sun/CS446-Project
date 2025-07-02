package com.example.cs446.backend.data.model

import com.squareup.moshi.Json
import java.util.UUID

data class UserPetRelationRaw(
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "pet_id") val petId: UUID,
    val relation: String?,
    val permissions: List<String>
){
    fun toUserPetRelation(): UserPetRelation {
        return UserPetRelation(
            userId = this.userId,
            petId = this.petId,
            relation = this.relation,
            permissions = Permissions.fromList(this.permissions)
        )
    }
}

data class UserPetRelation(
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "pet_id") val petId: UUID,
    val relation: String?,
    val permissions: Permissions
)