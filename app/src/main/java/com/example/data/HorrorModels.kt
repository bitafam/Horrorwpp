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
    val ratingCount: Int
        get() = rating_count
    val viewsCount: Int
        get() = view_count
    val coverUrl: String?
        get() = cover_image_url
    val cover_url: String?
        get() = cover_image_url ?: "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=600&auto=format&fit=crop&q=80"
    val prompt_used: String?
        get() = tags ?: "هوش مصنوعی"
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
)

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
    val POSTERS = listOf(
        "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop", // Dark haunted mansion
        "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop", // Foggy eerie forest
        "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?q=80&w=800&auto=format&fit=crop", // Dark stormy clouds and castle
        "https://images.unsplash.com/photo-1519074069444-1ba4fff16def?q=80&w=800&auto=format&fit=crop", // Silhouette in shadows
        "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop", // Neon gothic dark alley
        "https://images.unsplash.com/photo-1542281286-9e0a16bb7366?q=80&w=800&auto=format&fit=crop", // Misty road
        "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?q=80&w=800&auto=format&fit=crop", // Blood moon darkness
        "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=800&auto=format&fit=crop", // Dark abstract nightmare
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=800&auto=format&fit=crop", // Mysterious gothic portrait
        "https://images.unsplash.com/photo-1518709766631-a6a7f45921c3?q=80&w=800&auto=format&fit=crop", // Creepy abandoned hallway
        "https://images.unsplash.com/photo-1509248961158-e54f6934749c?q=80&w=800&auto=format&fit=crop", // Burning embers dark void
        "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?q=80&w=800&auto=format&fit=crop"  // Antique skull & gothic alchemy
    )

    fun getRandomPoster(): String {
        return POSTERS.random()
    }
}
