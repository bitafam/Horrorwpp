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
data class WrongChoiceScenario(
    val id: String,
    val title: String,
    val description: String,
    val status: String, // 'DRAFT', 'PUBLISHED', 'ARCHIVED'
    val initial_scene_id: String?,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class WrongChoiceScene(
    val id: String,
    val scenario_id: String,
    val scene_text: String,
    val ending_type: String?, // 'SURVIVED', 'DEAD', 'MYSTERY', 'SECRET', null if ongoing
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class WrongChoiceChoice(
    val id: String,
    val scene_id: String,
    val choice_text: String,
    val next_scene_id: String?,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class RealStory(
    val id: String,
    val title: String,
    val content: String,
    val author: String?,
    val source: String?,
    val cover_image_url: String?,
    val tags: String?,
    val status: String, // 'DRAFT', 'PUBLISHED', 'ARCHIVED'
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class UserStorySubmission(
    val id: String,
    val title: String,
    val content: String,
    val author_name: String,
    val status: String, // 'PENDING', 'PUBLISHED', 'REJECTED'
    val admin_notes: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class AiPrompt(
    val id: String,
    val prompt_key: String, // 'TIME_MIRROR_PROMPT' or 'WRONG_CHOICE_PROMPT'
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
