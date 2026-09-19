package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: String,
    val email: String?,
    val role: String?, // 'ADMIN' or 'USER'
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class GrimFortune(
    val id: String,
    val month_index: Int, // 1 to 12 (Farvardin to Esfand)
    val month_name: String, // e.g. "فروردین", "اردیبهشت", ...
    val title: String,
    val omen_poem: String?, // بیت یا شعر فال شوم حافظ
    val fortune_text: String, // تفسیر طالع و پیش‌گویی هولناک
    val doom_level: String?, // e.g. "شوم", "بسیار شوم", "نفرین ابدی"
    val status: String, // 'DRAFT', 'PUBLISHED', 'ARCHIVED'
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AiStory(
    val id: String,
    val title: String,
    val content: String,
    val genre: String? = "روانشناختی", // 'روانشناختی', 'ماورایی', 'جنایی', 'جن و ارواح', 'هیولایی', 'علمی‌تخیلی', 'افسانه ایرانی', 'گوتیک'
    val synopsis: String? = null,
    val cover_image_url: String? = null,
    val tags: String? = null,
    val status: String = "PUBLISHED", // 'PUBLISHED', 'DRAFT'
    val rating: Float = 4.8f,
    val rating_count: Int = 12,
    val view_count: Int = 185,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    val doomScore: Int
        get() = ((rating * 18).toInt()).coerceIn(60, 99)
    val doom_score: Int
        get() = doomScore
    val ratingScore: Float
        get() = rating
    val rating_score: Float
        get() = rating
    val ratingCount: Int
        get() = rating_count
    val viewsCount: Int
        get() = view_count
    val viewCount: Int
        get() = view_count
    val coverUrl: String?
        get() = cover_image_url
    val cover_url: String
        get() = if (!cover_image_url.isNullOrBlank()) cover_image_url else HorrorPosterPresets.getPoster(id)
    val prompt_used: String?
        get() = tags ?: "هوش تاریکی"
}

@JsonClass(generateAdapter = true)
data class RealStory(
    val id: String,
    val title: String,
    val content: String,
    val author: String? = null,
    val source: String? = null,
    val cover_image_url: String? = null,
    val tags: String? = null,
    val status: String, // 'DRAFT', 'PUBLISHED', 'ARCHIVED'
    val rating: Float = 0.0f,
    val rating_count: Int = 0,
    val view_count: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    val cover_url: String
        get() = if (!cover_image_url.isNullOrBlank()) cover_image_url else HorrorPosterPresets.getPoster(id)
}

@JsonClass(generateAdapter = true)
data class UserStorySubmission(
    val id: String,
    val title: String,
    val content: String,
    val author_name: String,
    val cover_image_url: String? = null,
    val tags: String? = null,
    val status: String, // 'PENDING', 'PUBLISHED', 'REJECTED'
    val admin_notes: String? = null,
    val rating: Float = 0.0f,
    val rating_count: Int = 0,
    val view_count: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    val cover_url: String
        get() = if (!cover_image_url.isNullOrBlank()) cover_image_url else HorrorPosterPresets.getPoster(id)

    fun toRealStory(): RealStory = RealStory(
        id = id,
        title = title,
        content = content,
        author = author_name,
        source = "روایات و اعترافات شما",
        cover_image_url = cover_image_url,
        tags = tags ?: "روایت کاربر, اعترافات",
        status = status,
        rating = rating,
        rating_count = rating_count,
        view_count = view_count,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@JsonClass(generateAdapter = true)
data class AiPrompt(
    val id: String,
    val prompt_key: String, // 'TIME_MIRROR_PROMPT' or 'AI_STORY_PROMPT'
    val prompt_text: String,
    @Json(name = "updated_at") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class AiProviderSetting(
    val id: String,
    val provider_name: String, // 'Gemini', 'OpenAI', 'OpenRouter', 'Compatible API'
    val model_name: String,
    val is_active: Boolean,
    @Json(name = "updated_at") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class AppSetting(
    val key: String,
    val value: String,
    val description: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AutomationConfig(
    val id: String, // 'AUTO_GRIM_FORTUNES', 'AUTO_AI_STORIES'
    val is_active: Boolean = false,
    val frequency: String = "DAILY", // 'HOURLY', 'DAILY', 'TWICE_DAILY'
    val schedule_hour_1: Int = 0, // 0 to 23
    val schedule_minute_1: Int = 0, // 0 to 59
    val schedule_hour_2: Int = 12, // 0 to 23
    val schedule_minute_2: Int = 0, // 0 to 59
    val batch_count: Int = 1,
    val custom_prompt: String? = null,
    val last_run_at: String? = null,
    val next_run_at: String? = null,
    val last_status: String? = null, // 'SUCCESS', 'FAILED', 'PENDING'
    val last_log: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AutomationLog(
    val id: String,
    val task_type: String,
    val status: String,
    val message: String,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class StoryReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val story_id: String,
    val story_title: String,
    val story_author: String,
    val story_type: String, // 'REAL' or 'USER' or 'AI'
    val reason: String,
    @Json(name = "created_at") val createdAt: String? = null
)

object HorrorPosterPresets {
    // Pure horrifying, dark gothic, paranormal and supernatural local poster identifiers
    val POSTERS = listOf(
        "poster_dark_demon",
        "poster_ghost_corridor",
        "poster_blood_ritual",
        "poster_screaming_wraith",
        "poster_cemetery_curse",
        "universal_horror",
        "nightmare_crypt",
        "haunted_chamber",
        "img_ai_story_poster_1",
        "img_ai_story_poster_2",
        "img_poster_1",
        "img_poster_2",
        "img_poster_3"
    )

    fun getPoster(storyId: String? = null): String {
        if (storyId.isNullOrBlank()) return POSTERS.random()
        val index = (storyId.hashCode().toLong() and 0x7FFFFFFF).toInt() % POSTERS.size
        return POSTERS[index]
    }

    fun getRandomPoster(): String {
        return POSTERS.random()
    }
}
