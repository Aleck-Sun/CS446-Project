package com.example.cs446.view.pets

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.backend.data.model.Reminder
import com.example.cs446.backend.data.repository.ReminderRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.backend.data.model.UserPetRelation
import com.example.cs446.backend.data.repository.UserPetRepository
import com.example.cs446.ui.components.pets.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class RemindersViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val userPetRelationRepository = UserPetRepository()
    private val reminderRepository = ReminderRepository()

    private val _selectedPetId = MutableStateFlow<UUID?>(null)
    val selectedPetId: StateFlow<UUID?> = _selectedPetId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentUserId = MutableStateFlow<UUID?>(null)
    val currentUserId: StateFlow<UUID?> = _currentUserId

    private val _userPetRelations = MutableStateFlow<List<UserPetRelation>>(emptyList())
    val userPetRelations: StateFlow<List<UserPetRelation>> = _userPetRelations

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId()
                    ?: throw IllegalStateException("User not logged in")
                _currentUserId.value = userId
                _userPetRelations.value = userPetRelationRepository.getPetRelationsForUser(userId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load user data"
            }
        }
    }

    fun loadRemindersForPet(petId: UUID) {
        viewModelScope.launch {
            try {
                _reminders.value = reminderRepository.getRemindersForPet(petId)
                _selectedPetId.value = petId
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load reminders: ${e.message}"
            }
        }
    }
    
    fun addReminder(
        context: Context,
        petId: UUID,
        title: String,
        description: String,
        time: ZonedDateTime,
        repeatIntervalDays: Int,
        repeatTimes: Int
    ) {
        viewModelScope.launch {
            try {
                // Validation
                if (title.isBlank()) {
                    _errorMessage.value = "Title cannot be empty"
                    return@launch
                }

                if (time.isBefore(LocalDateTime.now().atZone(ZoneId.of("America/Toronto")))) {
                    _errorMessage.value = "Reminder time must be in the future"
                    return@launch
                }

                if (repeatIntervalDays <= 0) {
                    _errorMessage.value = "Repeat interval days must be greater than 0"
                    return@launch
                }

                if (repeatTimes <= 0) {
                    _errorMessage.value = "Repeat times must be greater than 0"
                    return@launch
                }

                val userId = _currentUserId.value ?: return@launch

                val reminders = (0 until repeatTimes).map { i ->
                    Reminder(
                        id = UUID.randomUUID(),
                        createdAt = Instant.now(),
                        petId = petId,
                        userId = userId,
                        title = title,
                        description = description,
                        time = time.plusDays(i.toLong() * repeatIntervalDays),
                        active = true
                    )
                }.filter {
                    it.time.isAfter(LocalDateTime.now().atZone(ZoneId.of("America/Toronto")))
                            || it.time.isEqual(LocalDateTime.now().atZone(ZoneId.of("America/Toronto"))) }

                if (reminders.isEmpty()) {
                    _errorMessage.value = "All reminder times are in the past"
                    return@launch
                }

                // Schedule each reminder
                reminders.forEach { reminder ->
                    reminderRepository.addReminder(reminder)
                    val scheduled = ReminderScheduler.scheduleReminder(context, reminder)
                    if (!scheduled) {
                        _errorMessage.value = "Couldn't schedule notification for reminder at ${reminder.time}"
                    }
                }

                _errorMessage.value = null
                loadRemindersForPet(petId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add reminder: ${e.message}"
            }
        }
    }

    fun updateReminder(
        context: Context,
        reminderId: UUID,
        petId: UUID,
        title: String,
        description: String,
        time: LocalDateTime
    ) {
        viewModelScope.launch {
            try {
                reminderRepository.updateReminder(
                    reminderId = reminderId,
                    title = title,
                    description = description,
                    time = time
                )

                ReminderScheduler.cancelReminder(context, reminderId)
                val updatedReminder = reminderRepository.getReminder(reminderId)
                if (updatedReminder != null && updatedReminder.active && updatedReminder.time.isAfter(LocalDateTime.now().atZone(ZoneId.of("America/Toronto")))) {
                    ReminderScheduler.scheduleReminder(context, updatedReminder)
                }

                loadRemindersForPet(petId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update reminder: ${e.message}"
            }
        }
    }

    fun scheduleAllUpcomingReminders(context: Context) {
        viewModelScope.launch {
            try {
                // Cancel all existing alarms
                _reminders.value.forEach { reminder ->
                    ReminderScheduler.cancelReminder(context, reminder.id)
                }

                // Schedule upcoming active reminders
                val (upcoming, _) = getPartitionedReminders()
                upcoming.forEach { reminder ->
                    if (reminder.active) {
                        ReminderScheduler.scheduleReminder(context, reminder)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to schedule reminders: ${e.message}"
            }
        }
    }

    fun toggleActiveReminder(context: Context, reminderId: UUID, petId: UUID) {
        viewModelScope.launch {
            try {
                val reminder = reminderRepository.getReminder(reminderId)
                    ?: throw IllegalStateException("Reminder not found")

                val newActiveState = !reminder.active

                if (newActiveState) {
                    activateReminder(context, reminderId, petId)
                } else {
                    deactivateReminder(context, reminderId, petId)
                }
                reminderRepository.updateReminder(
                    reminderId = reminderId,
                    active = newActiveState
                )

                loadRemindersForPet(petId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle reminder: ${e.message}"
            }
        }
    }

    fun activateReminder(context: Context, reminderId: UUID, petId: UUID) {
        viewModelScope.launch {
            try {
                reminderRepository.updateReminder(reminderId, active = true)

                val reminder = reminderRepository.getReminder(reminderId)
                    ?: throw IllegalStateException("Reminder not found")

                // Reschedule if the reminder time is in the future
                if (reminder.time.isAfter(LocalDateTime.now().atZone(ZoneId.of("America/Toronto")))) {
                    val scheduled = ReminderScheduler.scheduleReminder(context, reminder)
                    if (!scheduled) {
                        _errorMessage.value = "Couldn't reschedule notification"
                    }
                }

                loadRemindersForPet(petId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to activate reminder: ${e.message}"
            }
        }
    }

    fun deactivateReminder(context: Context, reminderId: UUID, petId: UUID) {
        viewModelScope.launch {
            try {
                reminderRepository.updateReminder(reminderId, active=false)
                // Cancel if deactivating
                ReminderScheduler.cancelReminder(context, reminderId)
                loadRemindersForPet(petId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to deactivate reminder: ${e.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun reloadData() {
        loadUserData()
        _selectedPetId.value?.let { loadRemindersForPet(it) }
    }

    // partition reminders by past and upcoming
    fun getPartitionedReminders(): Pair<List<Reminder>, List<Reminder>> {
        val now = LocalDateTime.now(ZoneId.of("America/Toronto"))
        return _reminders.value.partition { reminder ->
            reminder.time.isAfter(now.atZone(ZoneId.of("America/Toronto")))
        }
    }
}