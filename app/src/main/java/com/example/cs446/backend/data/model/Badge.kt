package com.example.cs446.backend.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.datetime.Instant
import java.util.UUID

data class Badge(
    val type: BadgeType,
    @Json(name = "pet_id") val petId: UUID,
    val tier: BadgeTier,
    @Json(name = "last_updated") val lastUpdated: Instant,
    @Json(name = "created_at") val createdAt: Instant,
    val imageUrl: String? = null,
    val text: String? = null
)

@JsonClass(generateAdapter = false)
enum class BadgeType {
    @Json(name = "upload_photo")
    UPLOAD_PHOTO,
    @Json(name = "log_activity")
    LOG_ACTIVITY,
    @Json(name = "make_post")
    MAKE_POST,
    @Json(name = "days_in_app")
    DAYS_IN_APP;

    override fun toString(): String {
        return name.lowercase()
    }
}

@JsonClass(generateAdapter = false)
enum class BadgeTier {
    @Json(name = "first")
    FIRST,
    @Json(name = "tenth")
    TENTH,
    @Json(name = "fiftieth")
    FIFTIETH,
    @Json(name = "hundredth")
    HUNDREDTH,
    @Json(name = "five_hundredth")
    FIVE_HUNDREDTH,
    @Json(name = "thousandth")
    THOUSANDTH,
    @Json(name = "year")
    YEAR,
    @Json(name = "two_year")
    TWO_YEAR,
    @Json(name = "three_year")
    THREE_YEAR,
    @Json(name = "five_year")
    FIVE_YEAR;

    override fun toString(): String {
        return name.lowercase()
    }
}

fun getBadgeText(type: BadgeType, tier: BadgeTier): String = when (type) {
    BadgeType.UPLOAD_PHOTO -> {
        when (tier) {
            BadgeTier.FIRST -> "Upload a picture of your pet."
            else -> ""
        }
    }
    BadgeType.LOG_ACTIVITY -> {
        when (tier) {
            BadgeTier.FIRST -> "Log an activity for your pet."
            BadgeTier.TENTH -> "Log 10 activities for your pet."
            BadgeTier.FIFTIETH -> "Log 50 activities for your pet."
            BadgeTier.HUNDREDTH -> "Log 100 activities for your pet."
            BadgeTier.FIVE_HUNDREDTH -> "Log 500 activities for your pet."
            BadgeTier.THOUSANDTH -> "Log 1000 activities for your pet. Good work!"
            else -> ""
        }
    }
    BadgeType.MAKE_POST -> {
        when (tier) {
            BadgeTier.FIRST -> "Make a post about your pet."
            BadgeTier.TENTH -> "Make 10 posts about your pet"
            BadgeTier.FIFTIETH -> "Make 50 posts about your pet."
            BadgeTier.HUNDREDTH -> "Make 100 posts about your pet."
            BadgeTier.FIVE_HUNDREDTH -> "Make 500 posts about your pet."
            BadgeTier.THOUSANDTH -> "Make 1000 posts about your pet."
            else -> ""
        }
    }
    BadgeType.DAYS_IN_APP -> {
        when (tier) {
            BadgeTier.FIRST -> "You've added your pet to Petfolio. Glad to have you here!"
            BadgeTier.TENTH -> "Your pet has been in Petfolio for 10 days."
            BadgeTier.FIFTIETH -> "Your pet has been in Petfolio for 50 days."
            BadgeTier.HUNDREDTH -> "Your pet has been in Petfolio for 100 days."
            BadgeTier.YEAR -> "Your pet has been in Petfolio for a year."
            BadgeTier.TWO_YEAR -> "Your pet has been in Petfolio for 2 years."
            BadgeTier.THREE_YEAR -> "Your pet has been in Petfolio for 3 years. Wow!"
            BadgeTier.FIVE_YEAR -> "Your pet has been in Petfolio for 5 years. How the time passes!"
            else -> ""
        }
    }
    else -> ""
}

fun getUrlEndingForBadge(type: BadgeType): String {
    return "${type.name.lowercase()}.png"
}
