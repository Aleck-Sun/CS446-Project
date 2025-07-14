package com.example.cs446.common.badges

import com.example.cs446.backend.data.model.Badge
import com.example.cs446.backend.data.model.BadgeTier
import com.example.cs446.backend.data.model.BadgeType
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.repository.ActivityLogRepository
import com.example.cs446.backend.data.repository.BadgeRepository
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.PostRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.common.AppEvent
import com.example.cs446.common.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System
import java.util.UUID

class BadgeComponent {
    private var _badges: Map<UUID, List<Badge>> = mapOf()
    private var _badgeRepository = BadgeRepository()
    private val _petRepository = PetRepository()
    private val _userRepository = UserRepository()
    private val _activityRepository = ActivityLogRepository()
    private val _postRepository = PostRepository()
    private var _userPets: List<Pet> = listOf()

    fun startObserving(
        scope: CoroutineScope
    ) {
        scope.launch {
            onStartup()
            EventBus.events.collect { event ->
                when (event) {
                    is AppEvent.PostCreated -> checkPosts(event.petId)
                    is AppEvent.ImageUploaded -> checkUploadFirstPhoto(event.petId)
                    is AppEvent.ActivityLogged -> checkActivityLogs(event.petId)
                    else -> null
                }
            }
        }
    }

    suspend fun onStartup() {
        val uid = _userRepository.getCurrentUserId()
        _userPets = _petRepository.getPetsRelatedToUser(uid!!)
        loadBadges()
        _userPets.forEach {
            checkDaysInApp(it)
        }
    }

    suspend fun loadBadges() {
        _badges = _badgeRepository.getAllBadgesForPets(
            _userPets.map{it.id}
        )
    }

    suspend fun insertNewBadge(
        type: BadgeType, tier: BadgeTier, petId: UUID
    ) {
        val now = System.now()
        _badgeRepository.createOrUpdateBadgeForPet(
            Badge(
                type,
                petId,
                tier,
                now,
                now
            )
        )
        loadBadges()
        EventBus.emit(AppEvent.BadgeEarned(petId, type, tier))
    }

    suspend fun updateExistingBadge(
        badge: Badge, tier: BadgeTier
    ) {
        val now = System.now()
        _badgeRepository.createOrUpdateBadgeForPet(
            badge.copy(
                lastUpdated = now,
                tier = tier
            )
        )
        loadBadges()
        EventBus.emit(AppEvent.BadgeEarned(badge.petId, badge.type, tier))
    }

    suspend fun checkDaysInApp(pet: Pet) {
        val curBadge = _badges[pet.id]?.let {
            it.firstOrNull {
                it.type == BadgeType.DAYS_IN_APP
            }
        }
        val now = System.now()
        when (curBadge?.tier) {
            null -> {
                insertNewBadge(
                    BadgeType.DAYS_IN_APP,
                    BadgeTier.FIRST,
                    pet.id
                )
            }
            BadgeTier.FIRST -> {
                if (
                    (now - curBadge.lastUpdated).inWholeDays >= 10
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.TENTH
                    )
                }
            }
            BadgeTier.TENTH -> {
                if (
                    (now - curBadge.lastUpdated).inWholeDays >= 50
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.FIFTIETH
                    )
                }
            }
            BadgeTier.FIFTIETH -> {
                if (
                    (now - curBadge.lastUpdated).inWholeDays >= 100
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.HUNDREDTH
                    )
                }
            }
            BadgeTier.HUNDREDTH -> {
                if (
                    (now - curBadge.lastUpdated).inWholeDays >= 365
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.YEAR
                    )
                }
            }
            BadgeTier.YEAR -> {
                if (
                    (now - curBadge.lastUpdated).inWholeDays >= 2*365
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.TWO_YEAR
                    )
                }
            }
            BadgeTier.TWO_YEAR -> {
                if (
                    (now - curBadge.lastUpdated).inWholeDays >= 3*365
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.THREE_YEAR
                    )
                }
            }
            BadgeTier.THREE_YEAR -> {
                if (
                    (now - curBadge.lastUpdated).inWholeDays >= 3*365
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.FIVE_YEAR
                    )
                }
            }
            else -> null
        }
    }

    suspend fun checkUploadFirstPhoto(petId: UUID) {
        if (_badges[petId] == null) return
        else if (_badges[petId]?.firstOrNull { it.type == BadgeType.UPLOAD_PHOTO } == null) {
            insertNewBadge(
                BadgeType.UPLOAD_PHOTO,
                BadgeTier.FIRST,
                petId
            )
        }
    }

    suspend fun checkActivityLogs(petId: UUID) {
        val numLogs = _activityRepository.getNumberOfLogs(
            petId
        )

        checkBadgeByCounter(petId, BadgeType.LOG_ACTIVITY, numLogs)
    }

    suspend fun checkPosts(petId: UUID) {
        val numLogs = _postRepository.getNumberOfPosts(
            petId
        )

        checkBadgeByCounter(petId, BadgeType.MAKE_POST, numLogs)
    }

    suspend fun checkBadgeByCounter(petId: UUID, type: BadgeType, count: Int) {
        val curBadge = _badges[petId]?.let {
            it.firstOrNull {
                it.type == type
            }
        }
        when (curBadge?.tier) {
            null -> {
                insertNewBadge(
                    type,
                    BadgeTier.FIRST,
                    petId
                )
            }
            BadgeTier.FIRST -> {
                if (
                    count >= 10
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.TENTH
                    )
                }
            }
            BadgeTier.TENTH -> {
                if (
                   count >= 50
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.FIFTIETH
                    )
                }
            }
            BadgeTier.FIFTIETH -> {
                if (
                    count >= 100
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.HUNDREDTH
                    )
                }
            }
            BadgeTier.HUNDREDTH -> {
                if (
                   count >= 500
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.FIVE_HUNDREDTH
                    )
                }
            }
            BadgeTier.FIVE_HUNDREDTH -> {
                if (
                    count >= 1000
                ) {
                    updateExistingBadge(
                        curBadge, BadgeTier.THOUSANDTH
                    )
                }
            }
            else -> null
        }
    }
}