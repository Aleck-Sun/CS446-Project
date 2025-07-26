package com.example.cs446.view.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.backend.data.model.Handler
import com.example.cs446.backend.data.repository.UserPetRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.backend.data.model.UserPetRelation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PermissionsViewModel : ViewModel() {
    private val userPetRepository = UserPetRepository()

    private val _handlers = MutableStateFlow<List<Handler>>(emptyList())
    val handlers: StateFlow<List<Handler>> = _handlers

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadHandlers(petId: UUID) {
        viewModelScope.launch {
            try {
                val handlerList = userPetRepository.getHandlersForPet(petId)
                _handlers.value = handlerList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load handlers: ${e.message}"
            }
        }
    }

    fun updatePermission(handlerName: String, permission: String, value: Boolean, petId: UUID) {
        viewModelScope.launch {
            try {
                val handler = _handlers.value.find { it.name == handlerName } ?: return@launch

                val relation = userPetRepository.getRelationForUserAndPet(
                    petId = petId,
                    userId = handler.userId
                ) ?: return@launch

                val updatedPermissions = when (permission) {
                    "editStatistics" -> relation.permissions.copy(editStatistics = value)
                    "editLogs" -> relation.permissions.copy(editLogs = value)
                    "setReminders" -> relation.permissions.copy(setReminders = value)
                    "inviteHandlers" -> relation.permissions.copy(inviteHandlers = value)
                    "makePosts" -> relation.permissions.copy(makePosts = value)
                    "editPermissionsOfOthers" -> relation.permissions.copy(editPermissionsOfOthers = value)
                    else -> relation.permissions
                }

                val updatedRelation = relation.copy(permissions = updatedPermissions)

                userPetRepository.updateRelation(updatedRelation)

                _handlers.value = _handlers.value.map {
                    if (it.userId == handler.userId) {
                        it.copy(permissions = updatedPermissions)
                    } else it
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to update permission: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun inviteHandlerToPet(petId: UUID, usernameOrEmail: String, relationName: String, permissions: com.example.cs446.backend.data.model.Permissions, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // Try to find user by username or email
                val userRepo = UserRepository()
                val user = try {
                    userRepo.getAllUsers().find { it.username == usernameOrEmail || it.email == usernameOrEmail }
                        ?: userRepo.getUserById(UUID.fromString(usernameOrEmail)) // fallback if UUID
                } catch (e: Exception) { null }

                if (user == null) {
                    onResult(false, "User not found")
                    return@launch
                }
                // Check if already a handler
                val existing = userPetRepository.getRelationForUserAndPet(petId, user.id)
                if (existing != null) {
                    onResult(false, "User is already a handler for this pet")
                    return@launch
                }
                // Add relation with selected permissions and relation name
                val relation = UserPetRelation(
                    userId = user.id,
                    petId = petId,
                    relation = relationName,
                    permissions = permissions
                )
                userPetRepository.addUserPetRelation(relation)
                loadHandlers(petId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun deleteUserAndPetRelation(petId: UUID, userId: UUID) {
        viewModelScope.launch {
            try {
                userPetRepository.deleteUserAndPetRelation(petId, userId)
                loadHandlers(petId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete pet: ${e.message}"
            }
        }
    }
}