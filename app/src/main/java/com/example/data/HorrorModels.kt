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
        
        // Regex pattern to capture scene/stage separators:
        // Examples: ---صحنه ۱---, ===صحنه ۲===, ### صحنه ۳, صحنه ۱:, مرحله ۱:, گام ۱:, فصل ۱:, Scene 1:, Stage 1:
        val stageSplitRegex = Regex(
            "(?:---|===|###|##|#|\\[|\\()?\\s*(?:صحنه|مرحله|بخش|فصل|گام|Scene|Stage)\\s*(\\d+|[۰-۹]+|[یک|دو|سه|چهار|پنج|اول|دوم|سوم|چهارم|پنجم]+)[\\s:\n\\-—=\\]\\)]*",
            RegexOption.IGNORE_CASE
        )

        val hasExplicitStages = rawDescription.contains("صحنه") || rawDescription.contains("مرحله") ||
                rawDescription.contains("---") || rawDescription.contains("===") || rawDescription.contains("###")

        if (hasExplicitStages) {
            val rawBlocks = rawDescription.split(stageSplitRegex)
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (rawBlocks.size > 1) {
                var introHeader = ""
                val sceneBlocks = mutableListOf<String>()
                rawBlocks.forEach { blk ->
                    val isJustHeader = !blk.contains("گزینه") && !blk.contains("پاسخ") && !blk.contains("انتخاب") &&
                            !blk.contains("۱-") && !blk.contains("1-") && !blk.contains("1.") && !blk.contains("الف)") &&
                            (blk.startsWith("عنوان") || blk.contains("مطلب اصلی") || blk.length < 35)

                    if (isJustHeader && sceneBlocks.isEmpty()) {
                        val clean = blk.replace(Regex("^(?:عنوان|مطلب اصلی|مطلب)\\s*[:\\-]?.*"), "").trim()
                        if (clean.isNotBlank()) introHeader = clean
                    } else {
                        sceneBlocks.add(blk)
                    }
                }

                val blocksToProcess = if (sceneBlocks.isNotEmpty()) sceneBlocks else rawBlocks
                blocksToProcess.forEachIndexed { index, block ->
                    val stageNum = index + 1
                    val blockToParse = if (stageNum == 1 && introHeader.isNotBlank()) "$introHeader\n\n$block" else block
                    val parsed = parseSingleBlock(stageNum, blockToParse, scenarioTitle)
                    stages.add(parsed)
                }
            } else {
                stages.add(parseSingleBlock(1, rawDescription, scenarioTitle))
            }
        } else {
            stages.add(parseSingleBlock(1, rawDescription, scenarioTitle))
        }

        // If only 1 stage was extracted and has choices, enrich into progressive 3-stage game so the user experiences scene transitions
        if (stages.size == 1) {
            val s1 = stages[0]
            if (s1.choices.size >= 2) {
                val stage2 = ScenarioParsedStage(
                    stageNumber = 2,
                    stageTitle = "صحنه ۲: دالان نجواهای پنهان",
                    narrative = "با اتخاذ این تصمیم، وارد دالانی عمیق‌تر از عمارت شوم شدید. بوی کهنگی و خاک نمناک فضا را پر کرده و صدای سایه‌های متحرک به گوش می‌رسد. ناگهان دو مسیر با نشانه‌های طلسم‌شده در برابرتان پدیدار می‌شوند.",
                    choices = listOf(
                        ScenarioParsedChoice(1, "درب با نشان اژدهای خونین را باز کن", "تله مرگبار فعال شد و اسیر ارواح شدید!", nextStageIndex = 3, isDeath = true),
                        ScenarioParsedChoice(2, "درب با کلید نقره‌ای را انتخاب کن", "راه به سوی برج دیده‌بانی باز شد...", nextStageIndex = 3, isDeath = false),
                        ScenarioParsedChoice(3, "به آرامی از دالان عقب‌نشینی کن", "سایه شما را محاصره کرد و در تاریکی گرفتار شدید!", isDeath = true)
                    )
                )
                val stage3 = ScenarioParsedStage(
                    stageNumber = 3,
                    stageTitle = "صحنه ۳: مواجهه با طلسم نهایی",
                    narrative = "به اتاق مرکزی و محراب باستانی عمارت رسیدید. نبض دیوارهای سنگی شنیده می‌شود و روح کاتب باستانی در انتظار آخرین انتخاب شماست.",
                    choices = listOf(
                        ScenarioParsedChoice(1, "طلسم باستانی را با نور کتیبه بشکن", "شما پیروز شدید و با سلامت از عمارت گریختید!", isVictory = true),
                        ScenarioParsedChoice(2, "به محراب تعظیم کن و تسلیم شو", "روح شما برای همیشه در عمارت اسیر شد!", isDeath = true)
                    )
                )
                return listOf(s1, stage2, stage3)
            }
        }

        return if (stages.isNotEmpty()) stages else listOf(
            ScenarioParsedStage(
                stageNumber = 1,
                stageTitle = "صحنه ۱: $scenarioTitle",
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

        val choicePrefixRegex = Regex(
            "^(?:گزینه\\s*\\d*|پاسخ\\s*\\d*|انتخاب\\s*\\d*|اقدام\\s*\\d*|تصمیم\\s*\\d*|\\d+[\\.\\-]|[-*•]|\\[\\d+\\]|[الف-ی][\\-\\)])\\s*[:\\-]?\\s*",
            RegexOption.IGNORE_CASE
        )

        for (line in lines) {
            val isChoiceLine = line.matches(Regex("^(?:گزینه|پاسخ|انتخاب|اقدام|تصمیم|\\d+[\\.\\-]|[-*•]|\\[\\d+\\]|[الف-ی][\\-\\)]).+")) ||
                    line.contains("گزینه") || line.contains("پاسخ") || line.contains("انتخاب") ||
                    line.startsWith("۱-") || line.startsWith("۲-") || line.startsWith("۳-") || line.startsWith("۴-") ||
                    line.startsWith("1-") || line.startsWith("2-") || line.startsWith("3-") || line.startsWith("4-") ||
                    line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") || line.startsWith("4.") ||
                    line.startsWith("الف)") || line.startsWith("ب)") || line.startsWith("ج)") || line.startsWith("د)")

            if (isChoiceLine && !line.startsWith("عنوان") && !line.startsWith("روایت:") && !line.startsWith("داستان:") && !line.startsWith("شرح:")) {
                // Extract choice text and possible outcome
                var clean = line
                    .replace(choicePrefixRegex, "")
                    .replace("#", "")
                    .replace("*", "")
                    .trim()

                var outcome: String? = null
                var isDeath = false
                var isVictory = false

                if (clean.contains("->") || clean.contains("=>") || clean.contains("←") || clean.contains("—") || clean.contains("–")) {
                    val parts = clean.split(Regex("(?:->|=>|←|—|–)"))
                    clean = parts[0].trim()
                    outcome = parts.drop(1).joinToString(" - ").trim()
                }

                val lowerLine = line.lowercase() + " " + (outcome ?: "").lowercase()
                if (lowerLine.contains("مرگ") || lowerLine.contains("تله") || lowerLine.contains("هلاکت") ||
                    lowerLine.contains("کشته") || lowerLine.contains("اسیر") || lowerLine.contains("نابودی")) {
                    isDeath = true
                }
                if (lowerLine.contains("بقا") || lowerLine.contains("نجات") || lowerLine.contains("پیروز") ||
                    lowerLine.contains("رهایی") || lowerLine.contains("فرار موفق") || lowerLine.contains("پیروزی")) {
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
                if (!line.startsWith("عنوان:") && !line.startsWith("عنوان") && !line.startsWith("---") && !line.startsWith("===") && !line.startsWith("###")) {
                    val cleanNarrative = line
                        .replace(Regex("^(?:روایت|داستان|شرح صحنه|شرح موقعیت|ماجرا)\\s*[:\\-]?"), "")
                        .replace("#", "")
                        .replace("*", "")
                        .trim()
                    if (cleanNarrative.isNotBlank()) {
                        narrativeLines.add(cleanNarrative)
                    }
                }
            }
        }

        // Context-aware fallback choices for the scene if none detected
        if (choices.isEmpty()) {
            val narrativeSample = narrativeLines.joinToString(" ")
            if (narrativeSample.contains("کتاب") || narrativeSample.contains("کتیبه")) {
                choices.add(ScenarioParsedChoice(1, "بررسی دقیق کتیبه و طلسم‌های باستانی", "طلسم نجات آشکار شد...", nextStageIndex = stageNum + 1))
                choices.add(ScenarioParsedChoice(2, "نادیده گرفتن کتیبه و حرکت به دالان بعدی", "صدای قدم‌های نامرئی نزدیک‌تر شد...", nextStageIndex = stageNum + 1))
                choices.add(ScenarioParsedChoice(3, "لمس مستقیم جوهر سیاه کتیبه", "سم طلسم به خون شما نفوذ کرد!", isDeath = true))
            } else if (narrativeSample.contains("آینه") || narrativeSample.contains("انعکاس")) {
                choices.add(ScenarioParsedChoice(1, "خیره شدن به تصویر و خواندن ورد تطهیر", "تصویر شیطانی ناپدید شد...", nextStageIndex = stageNum + 1))
                choices.add(ScenarioParsedChoice(2, "شکستن آینه با مشعل سنگی", "تکه‌های آینه درگاه خروج را نشان دادند...", nextStageIndex = stageNum + 1))
                choices.add(ScenarioParsedChoice(3, "دست کشیدن روی سطح شیشه‌ای آینه", "دست شما درون آینه کشیده شد و اسیر گشتید!", isDeath = true))
            } else {
                choices.add(ScenarioParsedChoice(1, "پیشروی هوشیارانه در این صحنه با مشعل", "مسیر امن برای صحنه بعد گشوده شد...", nextStageIndex = stageNum + 1))
                choices.add(ScenarioParsedChoice(2, "جستجو و بررسی نشانه‌های مرموز صحنه", "کشف رازهای پنهان...", nextStageIndex = stageNum + 1))
                choices.add(ScenarioParsedChoice(3, "دویدن سراسیمه به سمت تاریکی", "در تله مرگبار سقوط کردید!", isDeath = true))
            }
        }

        val narrative = if (narrativeLines.isNotEmpty()) narrativeLines.joinToString("\n\n") else block

        return ScenarioParsedStage(
            stageNumber = stageNum,
            stageTitle = "صحنه $stageNum: $scenarioTitle",
            narrative = narrative,
            choices = choices
        )
    }
}

@JsonClass(generateAdapter = true)
data class AppSetting(
    val key: String,
    val value: String,
    val description: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AutomationConfig(
    val id: String, // 'SCHEDULED_NOTIFICATIONS', 'AUTO_GRIM_FORTUNES', 'AUTO_SCENARIOS'
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
    val story_type: String, // 'REAL' or 'USER'
    val reason: String,
    @Json(name = "created_at") val createdAt: String? = null
)

