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

data class ScenarioParsedChoice(
    val id: Int,
    val text: String,
    val outcomeText: String? = null,
    val nextStageIndex: Int? = null,
    val isDeath: Boolean = false,
    val isVictory: Boolean = false
)

data class ScenarioParsedStage(
    val stageNumber: Int,
    val stageTitle: String,
    val narrative: String,
    val choices: List<ScenarioParsedChoice>,
    val defaultEnding: String? = null // "DEAD", "SURVIVED", or null
)

object ScenarioParser {
    fun parse(rawDescription: String, scenarioTitle: String): List<ScenarioParsedStage> {
        val stages = mutableListOf<ScenarioParsedStage>()
        val stageDelimiters = listOf("---مرحله", "==مرحله", "مرحله ", "صحنه ", "گام ")
        
        val hasExplicitStages = rawDescription.contains("مرحله") || rawDescription.contains("---") || rawDescription.contains("===")

        if (hasExplicitStages) {
            // Split by stage separators
            val stageBlocks = rawDescription.split(Regex("(?:---|===)?\\s*مرحله\\s*(\\d+)[:\\-—=]*|---\\s*صحنه\\s*(\\d+)[:\\-—=]*"))
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (stageBlocks.size > 1) {
                stageBlocks.forEachIndexed { index, block ->
                    val stageNum = index + 1
                    val parsed = parseSingleBlock(stageNum, block, scenarioTitle)
                    stages.add(parsed)
                }
            } else {
                stages.add(parseSingleBlock(1, rawDescription, scenarioTitle))
            }
        } else {
            // Single block: parse choices and create multi-step progression
            stages.add(parseSingleBlock(1, rawDescription, scenarioTitle))
        }

        // If only 1 stage was extracted, create continuation steps so the user can play multi-stage
        if (stages.size == 1) {
            val s1 = stages[0]
            if (s1.choices.size >= 2) {
                // Add rich procedural stages 2 and 3 if not fully provided
                val stage2 = ScenarioParsedStage(
                    stageNumber = 2,
                    stageTitle = "دالان نجواهای پنهان",
                    narrative = "با اتخاذ این تصمیم، به دالانی عمیق‌تر از عمارت شوم کشانده شدید. هوا سنگین و بوی خاک نمناک فضا را پر کرده است. ناگهان دو درب با نشانه‌های طلسم‌شده در برابرتان پدیدار می‌شوند.",
                    choices = listOf(
                        ScenarioParsedChoice(1, "درب با نشان اژدهای خونین را باز کن", "تله مرگبار باز شد و ارواح به شما حمله کردند!", nextStageIndex = 3, isDeath = true),
                        ScenarioParsedChoice(2, "درب با کلید نقره‌ای را انتخاب کن", "راه به سمت برج دیده‌بانی باز شد...", nextStageIndex = 3, isDeath = false),
                        ScenarioParsedChoice(3, "به آرامی از دالان عقب‌نشینی کن", "سایه شما را محاصره کرد و در تاریکی گرفتار شدید!", isDeath = true)
                    )
                )
                val stage3 = ScenarioParsedStage(
                    stageNumber = 3,
                    stageTitle = "مواجهه با طلسم نهایی",
                    narrative = "به اتاق مرکزی و محراب باستانی عمارت رسیدید. نبض دیوارهای سنگی شنیده می‌شود و روح کاتب باستانی در انتظار آخرین انتخاب شماست.",
                    choices = listOf(
                        ScenarioParsedChoice(1, "طلسم باستانی را با خون کتیبه بشکن", "شما پیروز شدید و از عمارت با سلامت گریختید!", isVictory = true),
                        ScenarioParsedChoice(2, "به محراب تعظیم کن و تسلیم نجوا شو", "روح شما برای همیشه به دیوار عمارت دوخته شد!", isDeath = true)
                    )
                )
                return listOf(s1, stage2, stage3)
            }
        }

        return if (stages.isNotEmpty()) stages else listOf(
            ScenarioParsedStage(
                stageNumber = 1,
                stageTitle = scenarioTitle,
                narrative = rawDescription,
                choices = listOf(
                    ScenarioParsedChoice(1, "پیشروی به سمت ناشناخته‌ها", "وارد دالان تاریک شدید...", nextStageIndex = 2),
                    ScenarioParsedChoice(2, "فرار از گذرگاه هولناک", "در تله افتادید!", isDeath = true)
                )
            )
        )
    }

    private fun parseSingleBlock(stageNum: Int, block: String, scenarioTitle: String): ScenarioParsedStage {
        val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
        val narrativeLines = mutableListOf<String>()
        val choices = mutableListOf<ScenarioParsedChoice>()

        var choiceCounter = 1

        for (line in lines) {
            val isChoiceLine = line.matches(Regex("^(?:گزینه\\s*\\d*|\\d+[\\.\\-]|[-*•]|[الف-ی][\\-\\)])\\s*[:\\-]?\\s*.+")) ||
                    line.contains("گزینه") || line.startsWith("۱-") || line.startsWith("۲-") || line.startsWith("۳-") ||
                    line.startsWith("1-") || line.startsWith("2-") || line.startsWith("3-") || line.startsWith("الف)") || line.startsWith("ب)")

            if (isChoiceLine) {
                // Extract choice text and possible outcome
                var clean = line
                    .replace(Regex("^(?:گزینه\\s*\\d*|\\d+[\\.\\-]|[-*•]|[الف-ی][\\-\\)])\\s*[:\\-]?"), "")
                    .replace("#", "")
                    .replace("*", "")
                    .trim()

                var outcome: String? = null
                var isDeath = false
                var isVictory = false

                if (clean.contains("->") || clean.contains("=>") || clean.contains("←") || clean.contains("—")) {
                    val parts = clean.split(Regex("(?:->|=>|←|—)"))
                    clean = parts[0].trim()
                    outcome = parts.drop(1).joinToString(" - ").trim()
                }

                val lowerLine = line.lowercase() + " " + (outcome ?: "").lowercase()
                if (lowerLine.contains("مرگ") || lowerLine.contains("تله") || lowerLine.contains("هلاکت") || lowerLine.contains("کشته") || lowerLine.contains("اسیر")) {
                    isDeath = true
                }
                if (lowerLine.contains("بقا") || lowerLine.contains("نجات") || lowerLine.contains("پیروز") || lowerLine.contains("رهایی") || lowerLine.contains("فرار موفق")) {
                    isVictory = true
                }

                if (clean.isNotBlank()) {
                    choices.add(
                        ScenarioParsedChoice(
                            id = choiceCounter++,
                            text = clean,
                            outcomeText = outcome,
                            nextStageIndex = stageNum + 1,
                            isDeath = isDeath,
                            isVictory = isVictory
                        )
                    )
                }
            } else {
                if (!line.startsWith("عنوان:") && !line.startsWith("عنوان") && !line.startsWith("---") && !line.startsWith("===")) {
                    val cleanNarrative = line.replace("روایت:", "").replace("روایت", "").trim()
                    if (cleanNarrative.isNotBlank()) {
                        narrativeLines.add(cleanNarrative)
                    }
                }
            }
        }

        // If no choices were explicitly detected from text lines, extract options or provide default choices
        if (choices.isEmpty()) {
            choices.add(ScenarioParsedChoice(1, "پیشروی به عمق دالان تاریک", "به دالان بعدی راه یافتید...", nextStageIndex = stageNum + 1))
            choices.add(ScenarioParsedChoice(2, "بررسی اشیای مرموز روی دیوار", "طلسم قدیمی فعال شد!", isDeath = true))
            choices.add(ScenarioParsedChoice(3, "تلاش برای باز کردن دریچه مخفی", "دریچه راه خروج را نشان داد...", nextStageIndex = stageNum + 1, isVictory = stageNum >= 3))
        }

        val narrative = if (narrativeLines.isNotEmpty()) narrativeLines.joinToString("\n\n") else block

        return ScenarioParsedStage(
            stageNumber = stageNum,
            stageTitle = "مرحله $stageNum: $scenarioTitle",
            narrative = narrative,
            choices = choices
        )
    }
}

