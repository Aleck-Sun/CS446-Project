package com.example.cs446.view.pets

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import java.util.UUID

import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.ImageRepository
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.backend.data.model.UserPetRelation
import com.example.cs446.backend.data.model.Permissions
import com.example.cs446.backend.data.model.Species
import com.example.cs446.backend.data.model.Breed
import com.example.cs446.common.AppEvent
import com.example.cs446.common.EventBus

class PetsViewModel : ViewModel() {
    private val petRepository = PetRepository()
    private val imageRepository = ImageRepository()
    private val userRepository = UserRepository()

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    private val _selectedPetId = MutableStateFlow<UUID?>(null)
    val selectedPetId: StateFlow<UUID?> = _selectedPetId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentUserId = MutableStateFlow<UUID?>(null)

    init {
        loadPets(clearSelect = true)
    }

    private fun loadPets(clearSelect: Boolean = false) {
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId()
                    ?: throw IllegalStateException("User not logged in")
                _currentUserId.value = userId
                _pets.value = petRepository.getPetsRelatedToUser(userId)
                if (clearSelect) {
                    _selectedPetId.value = _pets.value.firstOrNull()?.id
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load pets: ${e.message}"
            }
        }
    }

    fun selectPet(petId: UUID) {
        _selectedPetId.value = petId
    }

    fun addPet(
        context: Context,
        name: String,
        species: Species,
        breed: Breed,
        birthdate: Instant,
        weight: Double,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                val petId = UUID.randomUUID()
                val userId = _currentUserId.value ?: return@launch

                val imageUrl = imageUri?.let {
                    imageRepository.uploadPetImage(context, it, petId)
                }

                val newPet = Pet(
                    id = petId,
                    name = name,
                    species = species,
                    breed = breed,
                    weight = weight,
                    createdBy = userId,
                    birthdate = birthdate,
                    createdAt = Clock.System.now(),
                    imageUrl = imageUrl
                )

                petRepository.addPet(newPet)
                petRepository.addUserPetRelation(
                    UserPetRelation(
                        userId = userId,
                        petId = petId,
                        relation = "Owner",
                        permissions = Permissions(
                            editLogs = true,
                            setReminders = true,
                            inviteHandlers = true,
                            makePosts = true,
                            editPermissionsOfOthers = true
                        )
                    )
                )
                loadPets()
                _selectedPetId.value = petId
            } catch (e: Exception) {
                _errorMessage.value =  "Failed to add pet: ${e.message}"
            }
        }
    }

    fun updatePet(
        context: Context,
        originalPet: Pet,
        newName: String,
        newSpecies: Species,
        newBreed: Breed,
        newBirthdate: Instant,
        newWeight: Double,
        newImageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                val imageUrl = newImageUri?.let {
                    imageRepository.uploadPetImage(context, it, originalPet.id)
                } ?: originalPet.imageUrl

                val updatedPet = originalPet.copy(
                    name = newName,
                    species = newSpecies,
                    breed = newBreed,
                    birthdate = newBirthdate,
                    weight = newWeight,
                    imageUrl = imageUrl
                )
                petRepository.updatePet(updatedPet)
                if (originalPet.imageUrl != updatedPet.imageUrl) {
                    EventBus.emit(
                        AppEvent.ImageUploaded(originalPet.id)
                    )
                }
                loadPets()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update pet: ${e.message}"
            }
        }
    }

    fun deletePet(petId: UUID) {
        viewModelScope.launch {
            try {
                petRepository.deletePet(petId)
                loadPets(clearSelect = true)
                _selectedPetId.value = _pets.value.firstOrNull()?.id
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete pet: ${e.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
