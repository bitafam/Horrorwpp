package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

enum class AppMode {
    USER, ADMIN_LOGIN, ADMIN_PANEL
}

class HorrorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HorrorRepository(application)
    private val prefs = application.getSharedPreferences("horror_admin_prefs", android.content.Context.MODE_PRIVATE)

    private val api: SupabaseApi
        get() = SupabaseClientProvider.api

    // Network connectivity monitoring
    private val _isNetworkOnline = MutableStateFlow(NetworkUtils.isOnline(application))
    val isNetworkOnline: StateFlow<Boolean> = _isNetworkOnline.asStateFlow()

    companion object {
        const val PREF_GEMINI_KEY = "pref_gemini_api_key"
        const val PREF_GEMINI_MODEL = "pref_gemini_model"
        const val PREF_GRIM_FORTUNE_PROMPT = "pref_grim_fortune_prompt"
        const val PREF_SCENARIO_PROMPT = "pref_scenario_prompt"
        const val PREF_SUPABASE_URL = "pref_supabase_url"
        const val PREF_SUPABASE_ANON_KEY = "pref_supabase_anon_key"

        val SUPPORTED_GEMINI_MODELS = listOf(
            "gemini-3.7-flash",
            "gemini-3.6-flash",
            "gemini-3.5-flash",
            "gemini-3.5-flash-lite",
            "gemini-3.1-flash-lite"
        )

        const val PREF_AI_STORY_PROMPT = "pref_ai_story_prompt"

        val DEFAULT_AI_STORY_PROMPT = """
تو یک نویسنده حرفه‌ای داستان‌های ترسناک ایرانی هستی که در داستان‌گویی سینمایی، وحشت روانشناختی، افسانه‌های عامیانه و تعلیق مهارت بسیار بالایی دارد.

یک داستان ترسناک کاملاً تخیلی، اورجینال و غیرقابل‌پیش‌بینی بنویس که هدف اصلی آن میخکوب کردن مخاطب تا آخرین جمله باشد.

مهم‌ترین اصل:
هر بار خودت تشخیص بده چه نوع داستانی برای این ایده مناسب‌تر است. خودت را به یک ژانر محدود نکن.
داستان می‌تواند یکی از این‌ها باشد یا ترکیبی از چند مورد:
- وحشت روانشناختی
- جنایی و معمایی
- ماورایی
- موجودات ناشناخته
- افسانه‌ها و باورهای عامیانه ایرانی
- جن و موجودات فولکلور ایرانی
- روستاها و مناطق دورافتاده
- آپارتمان و زندگی شهری
- جاده، کویر، جنگل، کوهستان یا شمال ایران
- داستان‌های مربوط به خانه‌های قدیمی
- وحشت خانوادگی
- سفر و گم‌شدن
- اتفاقات عجیب و غیرقابل توضیح
- علمی‌تخیلی تاریک
- سفر در زمان یا دستکاری حافظه
- یا یک ایده کاملاً جدید که در هیچ‌کدام از این دسته‌ها قرار نمی‌گیرد.

ایرانی بودن داستان:
فضا، رفتار شخصیت‌ها، دیالوگ‌ها، مکان‌ها و جزئیات زندگی باید برای مخاطب ایرانی طبیعی و قابل لمس باشد.
از جزئیات واقعی زندگی روزمره ایران استفاده کن؛ مثل: خانه‌های قدیمی، آپارتمان، کوچه، پارکینگ، آسانسور، پشت‌بام، روستا، جاده‌های بین‌شهری، مغازه، نانوایی، مسجد، قهوه‌خانه، مدرسه، بیمارستان، باغ، خانه مادربزرگ، انباری، زیرزمین، تلفن ثابت، ماشین‌های معمولی و هر چیزی که به فضای داستان کمک می‌کند.

اگر داستان به فرهنگ عامه یا افسانه‌های ایرانی مربوط است، از آن‌ها الهام بگیر؛ برای مثال موجودات و باورهایی مانند: جن، آل، بختک، اجنه، دیو، پری، سایه، مرده‌زنده، موجودات ناشناخته محلی و افسانه‌های مناطق مختلف ایران.
اما:
- موجودات را کلیشه‌ای و کارتونی ننویس.
- هر موجود یا پدیده باید قوانین، رفتار و ویژگی‌های خاص خودش را داشته باشد.
- لازم نیست همیشه موجودات واقعیِ فولکلور را دقیقاً همان‌طور که در افسانه‌ها آمده‌اند استفاده کنی؛ می‌توانی نسخه‌ای تازه و ترسناک از آن‌ها بسازی.
- اگر استفاده از موجودات فولکلور داستان را مصنوعی می‌کند، اصلاً از آن‌ها استفاده نکن.

ساختار داستان:
داستان باید از یک اتفاق نسبتاً عادی شروع شود.
در چند پاراگراف اول، یک چیز کوچک اما غیرعادی وارد داستان کن که مخاطب را کنجکاو کند.
سپس اطلاعات را قطره‌قطره آشکار کن.
مخاطب باید دائماً خودش نظریه بسازد: «نکنه دلیلش اینه؟» اما بعداً بفهمد اشتباه کرده است.
حداقل دو بار برداشت مخاطب از اتفاقات را تغییر بده.
سرنخ‌های مهم را از ابتدا داخل داستان قرار بده، اما طوری که در لحظه اول بی‌اهمیت به نظر برسند.
در پایان، بخشی از همان سرنخ‌های اولیه باید معنای جدید و ترسناک پیدا کنند.

درباره ترس:
از جامپ‌اسکر و جملات کلیشه‌ای مثل «ناگهان روح ظاهر شد» بیش از حد استفاده نکن.
ترس باید بیشتر از این چیزها ساخته شود:
- انتظار
- سکوت
- ناشناخته بودن
- صداهای نامعلوم
- رفتار غیرطبیعی یک انسان
- جزئیات اشتباه
- تکرار یک اتفاق
- چیزی که نباید وجود داشته باشد
- چیزی که نباید حرکت کند اما حرکت می‌کند
- چیزی که شخصیت فکر می‌کند دیده ولی مطمئن نیست
- خاطره‌ای که با واقعیت جور درنمی‌آید
- یک قانون عجیب که شخصیت دیر متوجه آن می‌شود
- یا فهمیدن اینکه شخصیت از ابتدا چیزی را اشتباه فهمیده است.

منطق داستان:
حتی اگر داستان کاملاً ماورایی باشد، اتفاقات آن باید درون جهان داستان منطق داشته باشند.
از اتفاقات تصادفی و بی‌دلیل برای جلو بردن داستان استفاده نکن.
اگر موجود یا نیرویی وجود دارد، برای رفتار آن دلیل و قانون مشخص داشته باش.
اگر پایان یک پیچش داستانی دارد، باید سرنخ‌هایی از آن قبل از افشاگری وجود داشته باشد.
از اغراق‌های غیرضروری، توضیحات علمی بی‌اساس و اتفاقات غیرمنطقی که فقط برای ترساندن مخاطب اضافه شده‌اند خودداری کن.

شخصیت‌ها:
شخصیت‌ها باید مثل آدم‌های واقعی رفتار کنند، نه مثل شخصیت‌های فیلم ترسناک.
آن‌ها ممکن است بترسند، شک کنند، اشتباه کنند، دروغ بگویند یا تصمیم اشتباه بگیرند.
دیالوگ‌ها کاملاً طبیعی و متناسب با فرهنگ و زبان محاوره‌ای ایرانی باشند.

پایان:
پایان باید یکی از این حالت‌ها باشد:
- یک افشاگری بزرگ
- یک پیچش داستانی
- فهمیدن حقیقتی وحشتناک
- پایان مبهم اما قابل تفسیر
- پیروزی ظاهری که مشخص می‌شود شکست بوده
- یا پایان باز که نشان دهد اتفاق هنوز تمام نشده است.
اما پایان نباید بی‌دلیل عجیب یا نامفهوم باشد.
آخرین چند خط باید بیشترین اثر را روی مخاطب بگذارند.
ترجیحاً آخرین جمله کوتاه، ساده و ماندگار باشد؛ جمله‌ای که بعد از تمام شدن داستان، مخاطب چند ثانیه به آن فکر کند.

قانون تنوع:
هر بار که این دستور اجرا می‌شود، ایده، مکان، شخصیت‌ها، نوع تهدید، ساختار و پایان را تغییر بده.
اگر داستان قبلی جنایی بود، داستان بعدی الزاماً جنایی نباشد.
اگر داستان قبلی درباره جن بود، دوباره همان الگو را تکرار نکن.
از تکرار خانه متروکه، روح زن، قتل قدیمی و صدای بچه در تاریکی به عنوان فرمول ثابت خودداری کن.
مخاطب نباید بعد از چند داستان بتواند الگوی داستان بعدی را حدس بزند.
""".trimIndent()

        val PERSIAN_MONTHS = listOf(
            "فروردین", "اردیبهشت", "خرداد",
            "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )

        const val DEFAULT_GRIM_FORTUNE_PROMPT =
            "تو حافظ شیرازی در دنیای تاریک و گوتیک عمارت وحشت هستی. برای تمام ۱۲ ماه سال خورشیدی (از شماره ۱ فروردین تا ۱۲ اسفند) یک فال و طالع شوم، بسیار مرموز، ادبی، هولناک و جذاب بنویس. " +
            "برای صرفه‌جویی در توکن، هر ۱۲ ماه را در همین یک پیام و دقیقاً با فرمت زیر شماره‌گذاری کن:\n\n" +
            "===1===\n" +
            "عنوان: [عنوان کوتاه و شوم طالع]\n" +
            "شعر: [یک بیت شعر شوم و گوتیک سبک حافظ]\n" +
            "طالع: [تفسیر سرنوشت، هشدار ماورایی و طالع شوم متولدین این ماه]\n" +
            "درجه: [شوم / بسیار شوم / نفرین ابدی]\n\n" +
            "و به همین ترتیب تا ===12=== برای اسفند ادامه بده."

        const val DEFAULT_SCENARIO_PROMPT =
            "تو طراح ارشد بازی‌های تعاملی بقا، ماجراجویی‌های شاخه‌ای و سناریوهای ماورایی عمارت وحشت هستی. برای هر سناریو، یک داستان پرکشش همراه با صحنه‌های چندگانه و پاسخ‌های کاملاً اختصاصی کاربر برای هر صحنه بنویس. در هر صحنه، داستان آن صحنه و پاسخ‌های کاربر دقیقاً بر اساس همان صحنه و موقعیت نوشته شود به طوری که با عوض شدن صحنه‌ها، گزینه‌ها و پاسخ‌های مربوط به همان صحنه در اختیار کاربر قرار گیرد.\n\n" +
            "فرمت خروجی دقیقاً به شکل زیر باشد:\n\n" +
            "عنوان: [نام جذاب و وهم‌آلود سناریو]\n\n" +
            "---صحنه ۱---\n" +
            "روایت: [داستان و توصیف دلهره‌آور شروع ماجرا و خطر اولیه پیش روی کاربر در صحنه اول]\n" +
            "گزینه ۱: [پاسخ یا تصمیم اول کاربر متناسب با اتفاقات صحنه اول] -> [نتیجه یا هدایت به صحنه بعد]\n" +
            "گزینه ۲: [پاسخ یا تصمیم دوم کاربر متناسب با اتفاقات صحنه اول] -> [نتیجه یا هدایت به صحنه بعد]\n" +
            "گزینه ۳: [پاسخ یا تصمیم سوم کاربر متناسب با اتفاقات صحنه اول (تله یا اقدام مرگبار)] -> [پیامد شوم یا مرگ]\n\n" +
            "---صحنه ۲---\n" +
            "روایت: [داستان و توصیف دگرگونی محیط و اتفاقات صحنه دوم بر اساس پیشروی ماجرا]\n" +
            "گزینه ۱: [پاسخ یا تصمیم اول کاربر متناسب با وقایع جدید صحنه دوم] -> [نتیجه یا هدایت به صحنه بعد]\n" +
            "گزینه ۲: [پاسخ یا تصمیم دوم کاربر متناسب با وقایع جدید صحنه دوم] -> [نتیجه یا هدایت به صحنه بعد]\n" +
            "گزینه ۳: [پاسخ یا تصمیم سوم کاربر متناسب با وقایع جدید صحنه دوم] -> [پیامد خطرناک]\n\n" +
            "---صحنه ۳---\n" +
            "روایت: [داستان صحنه سوم و مواجهه سرنوشت‌ساز پایانی با طلسم عمارت]\n" +
            "گزینه ۱: [پاسخ هوشمندانه برای شکستن طلسم و نجات] -> [بقا و رهایی پیروزمندانه از عمارت]\n" +
            "گزینه ۲: [پاسخ جسورانه یا مادی کاربر] -> [کشف گنجینه و فرار موفقیت‌آمیز]\n" +
            "گزینه ۳: [پاسخ اشتباه، تسلیم یا طمع مرگبار] -> [فرجام شوم و مرگ]"
    }

    private val _appMode = MutableStateFlow(AppMode.USER)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Gemini API & Admin Preferences State
    private val _geminiApiKey = MutableStateFlow(prefs.getString(PREF_GEMINI_KEY, "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _selectedGeminiModel = MutableStateFlow(
        prefs.getString(PREF_GEMINI_MODEL, "gemini-3.5-flash-lite") ?: "gemini-3.5-flash-lite"
    )
    val selectedGeminiModel: StateFlow<String> = _selectedGeminiModel.asStateFlow()

    private val _grimFortunePrompt = MutableStateFlow(prefs.getString(PREF_GRIM_FORTUNE_PROMPT, DEFAULT_GRIM_FORTUNE_PROMPT) ?: DEFAULT_GRIM_FORTUNE_PROMPT)
    val grimFortunePrompt: StateFlow<String> = _grimFortunePrompt.asStateFlow()

    private val _scenarioPrompt = MutableStateFlow(prefs.getString(PREF_SCENARIO_PROMPT, DEFAULT_SCENARIO_PROMPT) ?: DEFAULT_SCENARIO_PROMPT)
    val scenarioPrompt: StateFlow<String> = _scenarioPrompt.asStateFlow()

    private val _aiStoryPrompt = MutableStateFlow(
        prefs.getString(PREF_AI_STORY_PROMPT, null).let { saved ->
            if (saved.isNullOrBlank() || saved.startsWith("تو کاتب باستانی") || saved.length < 150) {
                DEFAULT_AI_STORY_PROMPT
            } else {
                saved
            }
        }
    )
    val aiStoryPrompt: StateFlow<String> = _aiStoryPrompt.asStateFlow()

    private val PREF_FONT_FAMILY = "pref_font_family"
    private val PREF_FONT_SIZE = "pref_font_size"

    private val _selectedFontIndex = MutableStateFlow(prefs.getInt(PREF_FONT_FAMILY, 0))
    val selectedFontIndex: StateFlow<Int> = _selectedFontIndex.asStateFlow()

    private val _fontSize = MutableStateFlow(prefs.getFloat(PREF_FONT_SIZE, 16f))
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    fun setFontFamily(index: Int) {
        _selectedFontIndex.value = index
        prefs.edit().putInt(PREF_FONT_FAMILY, index).apply()
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
        prefs.edit().putFloat(PREF_FONT_SIZE, size).apply()
    }

    data class AiGenQueueState(
        val isGenerating: Boolean = false,
        val totalRequested: Int = 0,
        val completedCount: Int = 0,
        val statusMessage: String = ""
    )

    private val _aiGenQueueState = MutableStateFlow(AiGenQueueState())
    val aiGenQueueState: StateFlow<AiGenQueueState> = _aiGenQueueState.asStateFlow()

    fun setAiStoryPrompt(prompt: String) {
        _aiStoryPrompt.value = prompt
        prefs.edit().putString(PREF_AI_STORY_PROMPT, prompt).apply()
        if (SupabaseClientProvider.isConfigured) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    api.upsertAiPrompt(
                        mapOf(
                            "prompt_key" to "AI_STORY_PROMPT",
                            "prompt_text" to prompt
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("SupabaseError", "Error updating AI_STORY_PROMPT", e)
                }
            }
        }
    }

    private var hasAttemptedPromptSeeding = false

    // Supabase Connection Settings
    private val _supabaseUrl = MutableStateFlow(
        prefs.getString(PREF_SUPABASE_URL, SupabaseClientProvider.supabaseUrl) ?: SupabaseClientProvider.supabaseUrl
    )
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow(
        prefs.getString(PREF_SUPABASE_ANON_KEY, SupabaseClientProvider.supabaseAnonKey) ?: SupabaseClientProvider.supabaseAnonKey
    )
    val supabaseAnonKey: StateFlow<String> = _supabaseAnonKey.asStateFlow()

    private val _isSupabaseConnected = MutableStateFlow(SupabaseClientProvider.isConfigured)
    val isSupabaseConnected: StateFlow<Boolean> = _isSupabaseConnected.asStateFlow()

    // User Data States
    private val _grimFortunesList = MutableStateFlow<List<GrimFortune>>(emptyList())
    val grimFortunesList: StateFlow<List<GrimFortune>> = _grimFortunesList.asStateFlow()

    private val _realStoriesList = MutableStateFlow<List<RealStory>>(emptyList())
    val realStoriesList: StateFlow<List<RealStory>> = _realStoriesList.asStateFlow()

    private val _userSubmissionsList = MutableStateFlow<List<UserStorySubmission>>(emptyList())
    val userSubmissionsList: StateFlow<List<UserStorySubmission>> = _userSubmissionsList.asStateFlow()

    private val _aiStoriesList = MutableStateFlow<List<AiStory>>(emptyList())
    val aiStoriesList: StateFlow<List<AiStory>> = _aiStoriesList.asStateFlow()

    // Admin Management States
    private val _adminGrimFortunes = MutableStateFlow<List<GrimFortune>>(emptyList())
    val adminGrimFortunes: StateFlow<List<GrimFortune>> = _adminGrimFortunes.asStateFlow()

    private val _adminRealStories = MutableStateFlow<List<RealStory>>(emptyList())
    val adminRealStories: StateFlow<List<RealStory>> = _adminRealStories.asStateFlow()

    private val _adminSubmissions = MutableStateFlow<List<UserStorySubmission>>(emptyList())
    val adminSubmissions: StateFlow<List<UserStorySubmission>> = _adminSubmissions.asStateFlow()

    private val _adminAiStories = MutableStateFlow<List<AiStory>>(emptyList())
    val adminAiStories: StateFlow<List<AiStory>> = _adminAiStories.asStateFlow()

    // Automation States
    private val _automationConfigs = MutableStateFlow<List<AutomationConfig>>(emptyList())
    val automationConfigs: StateFlow<List<AutomationConfig>> = _automationConfigs.asStateFlow()

    private val _automationLogs = MutableStateFlow<List<AutomationLog>>(emptyList())
    val automationLogs: StateFlow<List<AutomationLog>> = _automationLogs.asStateFlow()

    private val _storyReports = MutableStateFlow<List<StoryReport>>(emptyList())
    val storyReports: StateFlow<List<StoryReport>> = _storyReports.asStateFlow()

    fun loadAutomationData() {
        viewModelScope.launch {
            try {
                val configs = repository.getAutomationConfigs()
                if (configs.isNotEmpty()) {
                    _automationConfigs.value = configs
                } else {
                    _automationConfigs.value = listOf(
                        AutomationConfig(id = "AUTO_GRIM_FORTUNES", is_active = false, frequency = "DAILY", schedule_hour_1 = 0, schedule_minute_1 = 0),
                        AutomationConfig(id = "AUTO_SCENARIOS", is_active = false, frequency = "DAILY", schedule_hour_1 = 14, schedule_minute_1 = 0, schedule_hour_2 = 22, schedule_minute_2 = 0, batch_count = 1)
                    )
                }
                val logs = repository.getAutomationLogs()
                _automationLogs.value = logs

                val settings = repository.getAppSettings()
                val remoteKey = settings.find { it.key == "GEMINI_API_KEY" }?.value
                if (!remoteKey.isNullOrBlank() && _geminiApiKey.value.isBlank()) {
                    _geminiApiKey.value = remoteKey
                    prefs.edit().putString(PREF_GEMINI_KEY, remoteKey).apply()
                }
                val remoteModel = settings.find { it.key == "GEMINI_MODEL" }?.value
                if (!remoteModel.isNullOrBlank()) {
                    _selectedGeminiModel.value = remoteModel
                    prefs.edit().putString(PREF_GEMINI_MODEL, remoteModel).apply()
                }
            } catch (e: Exception) {
                android.util.Log.e("HorrorViewModel", "loadAutomationData error: ${e.message}")
            }
        }
    }

    fun saveAutomationConfig(config: AutomationConfig, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val success = repository.saveAutomationConfig(config)
            if (success) {
                val updated = _automationConfigs.value.filter { it.id != config.id } + config
                _automationConfigs.value = updated
            }
            onResult?.invoke(success)
        }
    }

    fun loadStoryReports() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val list = repository.getStoryReports()
                _storyReports.value = list
            } catch (e: Exception) {
                android.util.Log.e("HorrorViewModel", "loadStoryReports failed: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun submitStoryReport(report: StoryReport, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.createStoryReport(report)
            onResult(success)
        }
    }

    fun deleteStoryReport(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteStoryReport(id)
            if (success) {
                _storyReports.value = _storyReports.value.filter { it.id != id }
            }
            onResult(success)
        }
    }

    fun deleteStoryReportsBulk(ids: List<String>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            var allSuccess = true
            for (id in ids) {
                val success = repository.deleteStoryReport(id)
                if (!success) {
                    allSuccess = false
                }
            }
            loadStoryReports()
            onResult(allSuccess)
        }
    }

    fun triggerEdgeFunction(functionName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = repository.triggerEdgeFunction(functionName)
                if (res.first) {
                    loadAutomationData()
                                onResult(true, res.second)
                } else {
                    // Fallback to local AI / DB execution if Edge Function is not deployed or returns 404
                    when (functionName) {
                        "auto-grim-fortunes" -> {
                            val gfConfig = _automationConfigs.value.find { it.id == "AUTO_GRIM_FORTUNES" }
                            generateGrimFortunesWithAI(gfConfig?.custom_prompt) { success, msg, count ->
                                val logMsg = if (success) "✅ طالع ۱۲ ماه سال با موفقیت تولید و در پایگاه داده ثبت شد." else "خطا در تولید طالع: $msg"
                                viewModelScope.launch {
                                    repository.insertAutomationLog("AUTO_GRIM_FORTUNES", if (success) "SUCCESS" else "FAILED", logMsg)

                                    loadAutomationData()
                                    loadUserData()
                                }
                                onResult(success, logMsg)
                            }
                        }
                        "auto-scenarios", "auto-ai-stories" -> {
                            val aiConfig = _automationConfigs.value.find { it.id == "AUTO_AI_STORIES" || it.id == "AUTO_SCENARIOS" }
                            val count = aiConfig?.batch_count ?: 3
                            generateAiStoriesWithAI(customPrompt = aiConfig?.custom_prompt, count = count) { success, msg, genCount ->
                                val logMsg = if (success) "✅ $genCount داستان هوش مصنوعی با موفقیت خلق و در پایگاه داده ثبت شد." else "خطا در تولید داستان: $msg"
                                viewModelScope.launch {
                                    repository.insertAutomationLog("AUTO_AI_STORIES", if (success) "SUCCESS" else "FAILED", logMsg)

                                    loadAutomationData()
                                    loadUserData()
                                }
                                onResult(success, logMsg)
                            }
                        }
                        else -> onResult(false, res.second)
                    }
                }
            } catch (e: Exception) {
                onResult(false, "استثنا در اجرا: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    private val _aiPrompts = MutableStateFlow<List<AiPrompt>>(emptyList())
    val aiPrompts: StateFlow<List<AiPrompt>> = _aiPrompts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Automation is handled 100% server-side in Supabase Cloud via PostgreSQL pg_cron and Edge Functions.
     * The phone never executes automated generation in the background, eliminating any dependency
     * on the phone being unlocked or online at the scheduled time.
     */
    fun refreshAutomationStatus() {
        viewModelScope.launch {
            if (NetworkUtils.isOnline(getApplication())) {
                loadAutomationData()
            }
        }
    }

    init {
        // Observe network state continuously
        viewModelScope.launch {
            NetworkUtils.observeNetworkState(application).collect { isConnected ->
                _isNetworkOnline.value = isConnected
                if (isConnected && (_realStoriesList.value.isEmpty() || _grimFortunesList.value.isEmpty())) {
                    loadUserData()
                }
            }
        }

        // Restore Supabase Config from prefs if saved
        val savedUrl = prefs.getString(PREF_SUPABASE_URL, null)
        val savedKey = prefs.getString(PREF_SUPABASE_ANON_KEY, null)
        if (!savedUrl.isNullOrBlank() && !savedKey.isNullOrBlank()) {
            SupabaseClientProvider.configure(savedUrl, savedKey)
        }
        _isSupabaseConnected.value = SupabaseClientProvider.isConfigured

        loadUserData()
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
        if (mode == AppMode.ADMIN_PANEL) {
            prefs.edit().putBoolean("is_admin", true).apply()
            loadAdminData()
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            if (!NetworkUtils.isOnline(getApplication())) {
                _errorMessage.value = "⚠️ اتصال اینترنت برقرار نیست. لطفاً اتصال خود را بررسی کنید."
                // Load previously confirmed local data only if available, without mock fallback mutations
                val gfs = repository.getGrimFortunes(false)
                if (gfs.isNotEmpty()) _grimFortunesList.value = gfs
                val rs = repository.getRealStories(false)
                if (rs.isNotEmpty()) _realStoriesList.value = rs
                val subs = repository.getUserSubmissions(false)
                if (subs.isNotEmpty()) _userSubmissionsList.value = subs
                val ai = repository.getAiStories(false)
                if (ai.isNotEmpty()) _aiStoriesList.value = ai.filter { it.status == "PUBLISHED" }
                _loading.value = false
                return@launch
            }

            _loading.value = true
            try {
                // Fetch strictly from remote database (Supabase)
                var gfs = repository.getGrimFortunes(true)
                if (gfs.isEmpty() && SupabaseClientProvider.isConfigured) {
                    val mockGfs = getMockGrimFortunes()
                    repository.upsertGrimFortunes(mockGfs)
                    gfs = repository.getGrimFortunes(true)
                }
                _grimFortunesList.value = gfs

                var rs = repository.getRealStories(true)
                if (rs.isEmpty() && SupabaseClientProvider.isConfigured) {
                    val mockRs = getMockRealStories()
                    mockRs.forEach { story ->
                        try { repository.saveRealStory(story) } catch (_: Exception) {}
                    }
                    rs = repository.getRealStories(true)
                }
                _realStoriesList.value = rs

                var subs = repository.getUserSubmissions(true)
                _userSubmissionsList.value = subs

                var ai = repository.getAiStories(true)
                _aiStoriesList.value = ai.filter { it.status == "PUBLISHED" }

                if (SupabaseClientProvider.isConfigured) {
                    try {
                        val promptResp = api.getAiPrompts()
                        if (promptResp.isSuccessful && promptResp.body() != null) {
                            syncAndSeedPrompts(promptResp.body()!!)
                        } else if (promptResp.code() == 404 || (promptResp.isSuccessful && promptResp.body().isNullOrEmpty())) {
                            syncAndSeedPrompts(emptyList())
                        }
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در دریافت داده‌ها از سرور: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Static Pre-populated Persian Gothic Mock Data Fallbacks
    private fun getMockGrimFortunes(): List<GrimFortune> {
        val monthNames = PERSIAN_MONTHS
        val mockData = listOf(
            Triple("سقوط ستاره خونین", "ز غوغای جهان فارغ نشین در خلوت ظلمات / که بوی مرگ می‌آید ز خاکسترنشینان شب هجران", "متولدین فروردین در حصار طلسمی کهن گرفتار خواهند شد. هر تصمیمی پیش از نیمه‌شب، بهایی خونین به همراه خواهد داشت."),
            Triple("سایه‌های مه‌آلود اردیبهشت", "در این شب‌های ظلمانی مرو در وادی خاموش / که بر لب‌های خاموشان هزاران راز مدفون است", "راز مدفونی در خاندان شما سر باز خواهد کرد. ندای غیبی از دیوارهای کهنه عمارت به شما هشدار می‌دهد."),
            Triple("طالع بادهای سوزان", "نهیب باد پاییزی به گوش خفتگان آمد / که پایان شب تاریک آغاز تباهی‌هاست", "بادهای شوم، طالع شما را به سوی گذرگاهی بی‌پایان می‌کشانند. به هیچ غریبه‌ای در غروب آفتاب اعتماد مکن."),
            Triple("مکاشفه در مرداب خاکستر", "چو شب بر دشت افکند چادر تاریکی و وحشت / به گوش آید نوای شیون ارواح سرگردان", "روحی ناآرام در مرداب خاکستر به انتظار گام‌های شما نشسته است. نشانه‌های تاریک را جدی بگیرید."),
            Triple("طنین ناقوس ارواح", "چه سازم با دل پرخون در این ویرانه ظلمت / که از هر گوشه‌اش بانگ عذاب و بیم می‌آید", "ناقوس کهن برای متولدین مرداد به صدا درمی‌آید. آینه‌های خانه در شب تاریک حقیقت مرگباری را بازگو می‌کنند."),
            Triple("نفرین کتاب مهر و موم", "بخوان این سطر خونین را ز دیوان فراموشی / که هر کس خواند این خط را نصیبش جز ندامت نیست", "کتاب نفرین‌شده‌ای به دست شما خواهد رسید؛ هرگز صفحه سیزدهم آن را مگشایید."),
            Triple("رقص ارواح در پاییز سیاه", "هوا ابری، زمین خاموش و صحرا پر ز وحشت‌ها / خزان با خود به یغما برده جان بینوایان را", "بادهای پاییزی بوی خاک گورستان به همراه دارند. سفری ناخواسته در پیش دارید که راه برگشتی ندارد."),
            Triple("طلسم کلاغ‌های آبان", "به هر شاخه کلاغی نوحه مرگ و فنا سر داد / که دوران سیاهی بر سر این بوم گسترده‌ست", "کلاغ‌های معبد حافظ بر فراز بام شما به پرواز درآمده‌اند؛ هشداری برای حادثه‌ای هولناک در روزهای پایانی ماه."),
            Triple("آتش خاموش در سرداب", "در آن تاریک سردابه صدایی جز فغان ناید / که می‌سوزد در آتش جان بی‌پناه گرفتاران", "آتشی در سرداب زیرین عمارت برپا خواهد شد که خاکستر آن تا سال‌ها پاک نمی‌شود."),
            Triple("سرمای ابدی دی‌ماه", "زمستان آمد و بر بست راه کاروان‌ها را / درون برف می‌بینم نشان پای ارواح", "سرمایی استخوان‌سوز و طالعی منجمد در انتظار شماست. از تنهایی در شب‌های برفی دوری گزینید."),
            Triple("زمزمه‌های چاه عتیق", "شنیدم ناله‌ای در قعر چاهی بی‌سر و سامان / که می‌گفت ای فلک داد از جفای چرخ نافرجام", "زمزمه‌ای ناشناس شما را به اعماق تاریکی فرا می‌خواند. طلسم محافظتی خود را هرگز فراموش نکنید."),
            Triple("انعکاس شوم در آب روان", "نگاهی کن در این جویبار تاریک و گل‌آلود / که تصویر تو دیگر نیست تصویر همیشگی", "در آخرین روزهای سال، با چهره‌ای از دنیای مردگان در انعکاس آب روبرو خواهید شد. طالع شما دگرگون می‌شود.")
        )

        return (1..12).map { index ->
            val data = mockData[index - 1]
            GrimFortune(
                id = java.util.UUID.nameUUIDFromBytes("gf-$index".toByteArray()).toString(),
                month_index = index,
                month_name = monthNames[index - 1],
                title = data.first,
                omen_poem = data.second,
                fortune_text = data.third,
                doom_level = if (index % 3 == 0) "نفرین ابدی" else if (index % 2 == 0) "بسیار شوم" else "شوم",
                status = "PUBLISHED",
                createdAt = null,
                updatedAt = null
            )
        }
    }

    private fun getMockRealStories(): List<RealStory> {
        return listOf(
            RealStory(
                id = java.util.UUID.nameUUIDFromBytes("story-1".toByteArray()).toString(),
                title = "نجواهای عمارت قاجاری",
                content = "در زمستان سال ۱۳۲۰، پدربزرگم عمارتی قدیمی در حاشیه باغ‌های شمیران خرید. شب اول صدای کوبیده شدن پنجره‌ها قطع نمی‌شد تا اینکه ساعت ۳ نیمه‌شب در آینه قدی متوجه سایه‌ای با کلاه دوره قاجار شد که به او خیره شده بود و زمزمه می‌کرد: این خانه هرگز خالی نخواهد شد...",
                author = "سهراب کاتب",
                source = "خاطرات شفاهی تهران قدیم",
                cover_image_url = null,
                tags = "واقعی, تهران قدیم, عمارت تسخیر شده",
                status = "PUBLISHED",
                rating = 4.9f,
                rating_count = 42,
                view_count = 1250,
                createdAt = null,
                updatedAt = null
            ),
            RealStory(
                id = java.util.UUID.nameUUIDFromBytes("story-2".toByteArray()).toString(),
                title = "چاه نفرین‌شده روستای سیاه چشمه",
                content = "اهالی روستا می‌گفتند هر کس بعد از غروب خورشید کنار چاه قدیمی برود، صدای فریاد آب را خواهد شنید. سال گذشته دو مسافر تصمیم گرفتند درون چاه را با چراغ قوه تماشا کنند اما چیزی که از آب بالا آمد هرگز در هیچ کتابی توصیف نشده بود. تنها یک دفترچه خون‌آلود در کنار چاه پیدا شد...",
                author = "راوی ناشناس",
                source = "روایات کهن آذربایجان",
                cover_image_url = null,
                tags = "روستا, طلسم, چاه",
                status = "PUBLISHED",
                rating = 4.7f,
                rating_count = 31,
                view_count = 980,
                createdAt = null,
                updatedAt = null
            ),
            RealStory(
                id = java.util.UUID.nameUUIDFromBytes("story-3".toByteArray()).toString(),
                title = "طلسم آینه سیاه تالار آیینه",
                content = "در کاخ متروکه حاشیه زاگرس، آینه‌ای از جنس سنگ ابسیدین سیاه نصب شده که انعکاس افراد را با پنج دقیقه تاخیر نشان می‌دهد. نگهبان عمارت اعتراف کرد شبی انعکاس خود را دیده که خنجری در دست داشته در حالی که دست خودش خالی بوده است...",
                author = "استاد اردوان",
                source = "کتاب خطی عجایب‌المخلوقات",
                cover_image_url = null,
                tags = "آینه, باستانی, طلسم",
                status = "PUBLISHED",
                rating = 4.8f,
                rating_count = 58,
                view_count = 2140,
                createdAt = null,
                updatedAt = null
            )
        )
    }

    private fun getMockUserSubmissions(): List<UserStorySubmission> {
        return emptyList()
    }

    fun getUserVote(storyId: String): Float {
        val p = getApplication<Application>().getSharedPreferences("horror_house_user_ratings", android.content.Context.MODE_PRIVATE)
        val v = p.getFloat("rating_$storyId", -1f)
        if (v != -1f) return v
        val pOld = getApplication<Application>().getSharedPreferences("horror_user_votes", android.content.Context.MODE_PRIVATE)
        return pOld.getFloat("vote_$storyId", -1f)
    }

    fun rateStory(storyId: String, userRating: Float) {
        viewModelScope.launch {
            val p = getApplication<Application>().getSharedPreferences("horror_house_user_ratings", android.content.Context.MODE_PRIVATE)
            val existingVote = p.getFloat("rating_$storyId", -1f)
            if (existingVote != -1f) {
                _errorMessage.value = "شما قبلاً به این داستان رای داده‌اید (${existingVote.toInt()} ستاره)."
                return@launch
            }
            p.edit().putFloat("rating_$storyId", userRating).apply()

            val currentStories = _realStoriesList.value
            val target = currentStories.find { it.id == storyId }

            // Immediate optimistic UI update
            if (target != null) {
                val safeCount = target.rating_count.coerceAtLeast(0)
                val safeRating = if (safeCount == 0) 0f else target.rating
                val newCount = safeCount + 1
                val newRating = if (safeCount == 0) userRating else (((safeRating * safeCount) + userRating) / newCount)
                val roundedRating = kotlin.math.round(newRating * 10f) / 10.0f
                val updatedTarget = target.copy(rating = roundedRating, rating_count = newCount)

                _realStoriesList.value = currentStories.map { if (it.id == storyId) updatedTarget else it }
                _adminRealStories.value = _adminRealStories.value.map { if (it.id == storyId) updatedTarget else it }
            }

            try {
                val success = repository.submitStoryRatingRemote(storyId, userRating)
                val refreshed = repository.getRealStories(success)
                if (refreshed.isNotEmpty()) {
                    _realStoriesList.value = refreshed
                    _adminRealStories.value = refreshed
                }
                if (!success) {
                    _errorMessage.value = "خطا در ثبت رای در سرور."
                }
            } catch (e: Exception) {
                android.util.Log.e("RatingError", "Error submitting story rating remote: ${e.localizedMessage}")
            }
        }
    }

    fun incrementStoryViews(storyId: String) {
        viewModelScope.launch {
            val currentStories = _realStoriesList.value
            val target = currentStories.find { it.id == storyId }
            if (target != null) {
                val newCount = target.view_count + 1
                val updatedTarget = target.copy(view_count = newCount)

                _realStoriesList.value = currentStories.map { if (it.id == storyId) updatedTarget else it }
                _adminRealStories.value = _adminRealStories.value.map { if (it.id == storyId) updatedTarget else it }
            }

            try {
                val success = repository.incrementStoryViewRemote(storyId)
                if (success) {
                    val refreshed = repository.getRealStories(true)
                    if (refreshed.isNotEmpty()) {
                        _realStoriesList.value = refreshed
                        _adminRealStories.value = refreshed
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun rateUserSubmission(submissionId: String, userRating: Float) {
        viewModelScope.launch {
            val p = getApplication<Application>().getSharedPreferences("horror_house_user_ratings", android.content.Context.MODE_PRIVATE)
            val existingVote = p.getFloat("rating_$submissionId", -1f)
            if (existingVote != -1f) {
                _errorMessage.value = "شما قبلاً به این روایت رای داده‌اید (${existingVote.toInt()} ستاره)."
                return@launch
            }
            p.edit().putFloat("rating_$submissionId", userRating).apply()

            val currentSubmissions = _userSubmissionsList.value
            val target = currentSubmissions.find { it.id == submissionId }
            if (target != null) {
                val safeCount = target.rating_count.coerceAtLeast(0)
                val safeRating = if (safeCount == 0) 0f else target.rating
                val newCount = safeCount + 1
                val newRating = if (safeCount == 0) userRating else (((safeRating * safeCount) + userRating) / newCount)
                val roundedRating = kotlin.math.round(newRating * 10f) / 10.0f
                val updatedTarget = target.copy(rating = roundedRating, rating_count = newCount)

                _userSubmissionsList.value = currentSubmissions.map { if (it.id == submissionId) updatedTarget else it }
                _adminSubmissions.value = _adminSubmissions.value.map { if (it.id == submissionId) updatedTarget else it }
            }

            try {
                val success = repository.submitSubmissionRatingRemote(submissionId, userRating)
                val refreshed = repository.getUserSubmissions(success)
                if (refreshed.isNotEmpty()) {
                    _userSubmissionsList.value = refreshed
                    _adminSubmissions.value = refreshed
                }
                if (!success) {
                    _errorMessage.value = "خطا در ثبت رای در سرور."
                }
            } catch (e: Exception) {
                android.util.Log.e("RatingError", "Error submitting submission rating remote: ${e.localizedMessage}")
            }
        }
    }

    fun incrementSubmissionViews(submissionId: String) {
        viewModelScope.launch {
            val currentSubmissions = _userSubmissionsList.value
            val target = currentSubmissions.find { it.id == submissionId }
            if (target != null) {
                val newCount = target.view_count + 1
                val updatedTarget = target.copy(view_count = newCount)

                _userSubmissionsList.value = currentSubmissions.map { if (it.id == submissionId) updatedTarget else it }
                _adminSubmissions.value = _adminSubmissions.value.map { if (it.id == submissionId) updatedTarget else it }
            }

            try {
                val success = repository.incrementSubmissionViewRemote(submissionId)
                if (success) {
                    val refreshed = repository.getUserSubmissions(true)
                    if (refreshed.isNotEmpty()) {
                        _userSubmissionsList.value = refreshed
                        _adminSubmissions.value = refreshed
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key.trim()
        prefs.edit().putString(PREF_GEMINI_KEY, key.trim()).apply()
        viewModelScope.launch {
            try {
                repository.saveAppSetting("GEMINI_API_KEY", key.trim(), "Google Gemini API Key for Edge Functions and App")
            } catch (_: Exception) {}
        }
    }

    fun setSelectedGeminiModel(model: String) {
        _selectedGeminiModel.value = model
        prefs.edit().putString(PREF_GEMINI_MODEL, model).apply()
        viewModelScope.launch {
            try {
                repository.saveAppSetting("GEMINI_MODEL", model, "Selected Gemini Model")
            } catch (_: Exception) {}
        }
    }

    private fun syncAndSeedPrompts(prompts: List<AiPrompt>) {
        _aiPrompts.value = prompts
        
        // Sync local settings from fetched prompts
        val gfPrompt = prompts.find { it.prompt_key == "GRIM_FORTUNE_PROMPT" }?.prompt_text
        if (gfPrompt != null) {
            _grimFortunePrompt.value = gfPrompt
            prefs.edit().putString(PREF_GRIM_FORTUNE_PROMPT, gfPrompt).apply()
        }
        val scPrompt = prompts.find { it.prompt_key == "WRONG_CHOICE_PROMPT" }?.prompt_text
        if (scPrompt != null) {
            _scenarioPrompt.value = scPrompt
            prefs.edit().putString(PREF_SCENARIO_PROMPT, scPrompt).apply()
        }

        val aiPrompt = prompts.find { it.prompt_key == "AI_STORY_PROMPT" }?.prompt_text
        if (aiPrompt != null) {
            _aiStoryPrompt.value = aiPrompt
            prefs.edit().putString(PREF_AI_STORY_PROMPT, aiPrompt).apply()
        }

        // Seeding logic if missing on remote
        if (SupabaseClientProvider.isConfigured && !hasAttemptedPromptSeeding) {
            hasAttemptedPromptSeeding = true
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val hasGf = prompts.any { it.prompt_key == "GRIM_FORTUNE_PROMPT" }
                    if (!hasGf) {
                        api.upsertAiPrompt(mapOf(
                            "prompt_key" to "GRIM_FORTUNE_PROMPT",
                            "prompt_text" to DEFAULT_GRIM_FORTUNE_PROMPT
                        ))
                    }
                    val hasSc = prompts.any { it.prompt_key == "WRONG_CHOICE_PROMPT" }
                    if (!hasSc) {
                        api.upsertAiPrompt(mapOf(
                            "prompt_key" to "WRONG_CHOICE_PROMPT",
                            "prompt_text" to DEFAULT_SCENARIO_PROMPT
                        ))
                    }
                    val hasAi = prompts.any { it.prompt_key == "AI_STORY_PROMPT" }
                    if (!hasAi) {
                        api.upsertAiPrompt(mapOf(
                            "prompt_key" to "AI_STORY_PROMPT",
                            "prompt_text" to DEFAULT_AI_STORY_PROMPT
                        ))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SupabaseError", "Error seeding default prompts", e)
                }
            }
        }
    }

    fun setGrimFortunePrompt(prompt: String) {
        _grimFortunePrompt.value = prompt
        prefs.edit().putString(PREF_GRIM_FORTUNE_PROMPT, prompt).apply()
        if (SupabaseClientProvider.isConfigured) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    api.updateAiPrompt(
                        keyEq = "eq.GRIM_FORTUNE_PROMPT",
                        item = mapOf("prompt_text" to prompt)
                    )
                } catch (e: Exception) {
                    android.util.Log.e("SupabaseError", "Error updating GRIM_FORTUNE_PROMPT", e)
                }
            }
        }
    }

    fun setScenarioPrompt(prompt: String) {
        _scenarioPrompt.value = prompt
        prefs.edit().putString(PREF_SCENARIO_PROMPT, prompt).apply()
        if (SupabaseClientProvider.isConfigured) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    api.updateAiPrompt(
                        keyEq = "eq.WRONG_CHOICE_PROMPT",
                        item = mapOf("prompt_text" to prompt)
                    )
                } catch (e: Exception) {
                    android.util.Log.e("SupabaseError", "Error updating WRONG_CHOICE_PROMPT", e)
                }
            }
        }
    }

    fun saveSupabaseConfig(url: String, key: String, onResult: (Boolean, String) -> Unit) {
        val cleanUrl = url.trim()
        val cleanKey = key.trim()
        _supabaseUrl.value = cleanUrl
        _supabaseAnonKey.value = cleanKey

        prefs.edit()
            .putString(PREF_SUPABASE_URL, cleanUrl)
            .putString(PREF_SUPABASE_ANON_KEY, cleanKey)
            .apply()

        SupabaseClientProvider.configure(cleanUrl, cleanKey)
        _isSupabaseConnected.value = SupabaseClientProvider.isConfigured

        testSupabaseConnection { success, msg ->
            if (success) {
                loadAdminData()
                loadUserData()
            }
            onResult(success, msg)
        }
    }

    fun testSupabaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.getGrimFortunes(status = null)
                if (resp.isSuccessful) {
                    _isSupabaseConnected.value = true
                    onResult(true, "اتصال به پایگاه داده Supabase با موفقیت برقرار شد (کد ${resp.code()})")
                } else {
                    onResult(false, "پاسخ از سرور دریافت شد اما با خطا: کد ${resp.code()} - ${resp.message()}")
                }
            } catch (e: Exception) {
                onResult(false, "خطا در برقراری اتصال به Supabase: ${e.localizedMessage ?: "عدم دسترسی به اینترنت یا آدرس اشتباه"}")
            }
        }
    }

    fun getEffectiveGeminiApiKey(): String {
        val customKey = _geminiApiKey.value
        if (customKey.isNotBlank()) return customKey
        return BuildConfig.GEMINI_API_KEY
    }

    fun loadAdminData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Load from Room local DB first
                val localGf = repository.getAllGrimFortunesAdmin()
                if (localGf.isNotEmpty()) _adminGrimFortunes.value = localGf

                val localRs = repository.getAllRealStoriesAdmin()
                if (localRs.isNotEmpty()) _adminRealStories.value = localRs

                val localSubs = repository.getAllUserSubmissionsAdmin()
                if (localSubs.isNotEmpty()) _adminSubmissions.value = localSubs

                val localAi = repository.getAllAiStoriesAdmin()
                if (localAi.isNotEmpty()) _adminAiStories.value = localAi

                // If Supabase is configured, also fetch latest remote
                if (SupabaseClientProvider.isConfigured) {
                    val gfResp = api.getGrimFortunes()
                    if (gfResp.isSuccessful && gfResp.body() != null) {
                        _adminGrimFortunes.value = gfResp.body()!!
                    }

                    val rsResp = api.getRealStories()
                    if (rsResp.isSuccessful && rsResp.body() != null) {
                        _adminRealStories.value = rsResp.body()!!
                    }

                    val subResp = api.getUserSubmissions()
                    if (subResp.isSuccessful && subResp.body() != null) {
                        _adminSubmissions.value = subResp.body()!!
                    }

                    val aiResp = api.getAiStories()
                    if (aiResp.isSuccessful && aiResp.body() != null) {
                        _adminAiStories.value = aiResp.body()!!
                    }

                    val promptResp = api.getAiPrompts()
                    if (promptResp.isSuccessful && promptResp.body() != null) {
                        syncAndSeedPrompts(promptResp.body()!!)
                    } else if (promptResp.code() == 404 || (promptResp.isSuccessful && promptResp.body().isNullOrEmpty())) {
                        syncAndSeedPrompts(emptyList())
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "بارگذاری محلی انجام شد: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loginAdmin(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (!SupabaseClientProvider.isConfigured) {
            onResult(false, "اتصال Supabase تنظیم نشده است. ابتدا آدرس و کلید را وارد کنید.")
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                val authResp = api.loginAdmin(
                    apiKey = SupabaseClientProvider.supabaseAnonKey,
                    body = AuthRequest(email, pass)
                )
                if (authResp.isSuccessful && authResp.body() != null) {
                    val authData = authResp.body()!!
                    val token = authData.accessToken
                    val userId = authData.user?.id
                    val userEmail = authData.user?.email
                    if (token != null && userId != null) {
                        SupabaseClientProvider.currentAuthToken = token
                        
                        // Query user profile to verify role
                        val profileResp = api.getProfile(idEq = "eq.$userId")
                        if (profileResp.isSuccessful && profileResp.body() != null) {
                            val profiles = profileResp.body()!!
                            val matchedProfile = profiles.firstOrNull()
                            if (matchedProfile != null && matchedProfile.role == "ADMIN") {
                                _currentUserEmail.value = userEmail
                                _currentUserId.value = userId
                                _currentUserRole.value = "ADMIN"
                                setAppMode(AppMode.ADMIN_PANEL)
                                onResult(true, null)
                            } else {
                                SupabaseClientProvider.currentAuthToken = null
                                onResult(false, "خطای دسترسی: شما ادمین نیستید. نقش شما: ${matchedProfile?.role ?: "نامشخص"}")
                            }
                        } else {
                            SupabaseClientProvider.currentAuthToken = null
                            val errStr = profileResp.errorBody()?.string() ?: ""
                            android.util.Log.e("SupabaseError", "Profile query failed: ${profileResp.code()} - $errStr")
                            onResult(false, "خطا در استعلام مشخصات کاربری از سرور.")
                        }
                    } else {
                        onResult(false, "توکن دریافت نشد.")
                    }
                } else {
                    val errStr = authResp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "Auth login failed: ${authResp.code()} - $errStr")
                    onResult(false, "ایمیل یا رمز عبور اشتباه است.")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "loginAdmin exception", e)
                onResult(false, "خطای ارتباط با سرور: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun submitUserStory(title: String, content: String, author: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (!NetworkUtils.isOnline(getApplication())) {
                _errorMessage.value = "❌ اتصال اینترنت برقرار نیست! ارسال روایت نیازمند اتصال فعال به اینترنت است."
                onResult(false)
                return@launch
            }
            val newSub = UserStorySubmission(
                id = java.util.UUID.randomUUID().toString(),
                title = title.trim(),
                content = content.trim(),
                author_name = author.ifBlank { "ناشناس" }.trim(),
                status = "PENDING",
                admin_notes = null,
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                updatedAt = null
            )
            _loading.value = true
            try {
                val saved = repository.createUserSubmission(newSub)
                _adminSubmissions.value = listOf(saved) + _adminSubmissions.value

                onResult(true)
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ارسال داستان به سرور: ${e.localizedMessage}"
                onResult(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun saveGrimFortune(
        monthIndex: Int,
        title: String,
        poem: String?,
        fortuneText: String,
        doomLevel: String?,
        status: String = "PUBLISHED",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val existing = _adminGrimFortunes.value.find { it.month_index == monthIndex }
                val id = if (existing?.id != null && !existing.id.startsWith("gf-")) existing.id else java.util.UUID.randomUUID().toString()
                val monthName = PERSIAN_MONTHS.getOrElse(monthIndex - 1) { "ماه $monthIndex" }
                val newGf = GrimFortune(
                    id = id,
                    month_index = monthIndex,
                    month_name = monthName,
                    title = title.trim(),
                    omen_poem = poem?.trim(),
                    fortune_text = fortuneText.trim(),
                    doom_level = doomLevel ?: "شوم",
                    status = status,
                    createdAt = null,
                    updatedAt = null
                )
                val saved = repository.saveGrimFortune(newGf)
                _adminGrimFortunes.value = _adminGrimFortunes.value.filter { it.month_index != monthIndex } + saved
                if (status == "PUBLISHED") {
                    _grimFortunesList.value = _grimFortunesList.value.filter { it.month_index != monthIndex } + saved
                } else {
                    _grimFortunesList.value = _grimFortunesList.value.filter { it.month_index != monthIndex }
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ذخیره‌سازی طالع: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateGrimFortuneStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val item = _adminGrimFortunes.value.find { it.id == id }
                if (item != null) {
                    val updated = item.copy(status = newStatus)
                    val saved = repository.saveGrimFortune(updated)
                    _adminGrimFortunes.value = _adminGrimFortunes.value.map { if (it.id == id) saved else it }
                    if (newStatus == "PUBLISHED") {
                        _grimFortunesList.value = _grimFortunesList.value.filter { it.id != id } + saved
                    } else {
                        _grimFortunesList.value = _grimFortunesList.value.filter { it.id != id }
                    }
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در تغییر وضعیت طالع: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteGrimFortune(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteGrimFortune(id)
                _adminGrimFortunes.value = _adminGrimFortunes.value.filter { it.id != id }
                _grimFortunesList.value = _grimFortunesList.value.filter { it.id != id }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در حذف طالع: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateSubmissionStatus(id: String, newStatus: String, adminNotes: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val sub = _adminSubmissions.value.find { it.id == id }
                if (sub != null) {
                    val updated = sub.copy(status = newStatus, admin_notes = adminNotes)
                    val saved = repository.saveUserSubmission(updated)
                    _adminSubmissions.value = _adminSubmissions.value.map { if (it.id == id) saved else it }
                    if (newStatus == "PUBLISHED") {
                        _userSubmissionsList.value = listOf(saved) + _userSubmissionsList.value.filter { it.id != id }
                    } else {
                        _userSubmissionsList.value = _userSubmissionsList.value.filter { it.id != id }
                    }
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در تغییر وضعیت ارسالی: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteSubmission(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteUserSubmission(id)
                _adminSubmissions.value = _adminSubmissions.value.filter { it.id != id }
                _userSubmissionsList.value = _userSubmissionsList.value.filter { it.id != id }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در حذف ارسالی: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun publishSubmissionAsRealStory(
        submission: UserStorySubmission,
        coverUrl: String,
        editedTitle: String? = null,
        editedContent: String? = null,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val title = editedTitle?.ifBlank { null } ?: submission.title
            val content = editedContent?.ifBlank { null } ?: submission.content
            _loading.value = true
            try {
                // Update submission as PUBLISHED with cover image, tags, and edited content
                val updatedSub = submission.copy(
                    title = title,
                    content = content,
                    cover_image_url = coverUrl.ifBlank { null },
                    tags = "روایت کاربر, اعترافات",
                    status = "PUBLISHED",
                    admin_notes = "تایید و منتشر شده در بخش روایات کاربران"
                )
                val savedSub = repository.saveUserSubmission(updatedSub)
                
                // Update admin submissions and public user submissions lists
                _adminSubmissions.value = _adminSubmissions.value.map { if (it.id == submission.id) savedSub else it }
                val currentList = _userSubmissionsList.value.filter { it.id != submission.id }
                _userSubmissionsList.value = listOf(savedSub) + currentList
                
                onComplete(true)
            } catch (e: Exception) {
                _errorMessage.value = "خطا در انتشار داستان کاربر: ${e.localizedMessage}"
                onComplete(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun createRealStory(title: String, content: String, author: String, source: String, coverUrl: String, tags: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val newStory = RealStory(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    author = author,
                    source = source,
                    cover_image_url = coverUrl.ifBlank { null },
                    tags = tags,
                    status = status,
                    createdAt = null,
                    updatedAt = null
                )
                val saved = repository.saveRealStory(newStory)
                _adminRealStories.value = listOf(saved) + _adminRealStories.value
                if (status == "PUBLISHED") {
                    _realStoriesList.value = listOf(saved) + _realStoriesList.value
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ساخت داستان: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createRealStoriesBulk(stories: List<RealStory>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                stories.forEach { story ->
                    try {
                        repository.saveRealStory(story)
                    } catch (e: Exception) {
                        android.util.Log.e("BulkAdd", "Failed to save story: ${story.title}", e)
                    }
                }
                val refreshed = repository.getAllRealStoriesAdmin()
                _adminRealStories.value = refreshed
                _realStoriesList.value = repository.getRealStories(true)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در افزودن گروهی داستان‌ها: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun publishRealStoriesBulk(ids: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                ids.forEach { id ->
                    val story = _adminRealStories.value.find { it.id == id }
                    if (story != null && story.status != "PUBLISHED") {
                        try {
                            val updated = story.copy(status = "PUBLISHED")
                            repository.saveRealStory(updated)
                        } catch (e: Exception) {
                            android.util.Log.e("BulkPublish", "Failed to publish story: $id", e)
                        }
                    }
                }
                val refreshed = repository.getAllRealStoriesAdmin()
                _adminRealStories.value = refreshed
                _realStoriesList.value = repository.getRealStories(true)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در انتشار گروهی داستان‌ها: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun draftRealStoriesBulk(ids: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                ids.forEach { id ->
                    val story = _adminRealStories.value.find { it.id == id }
                    if (story != null && story.status == "PUBLISHED") {
                        try {
                            val updated = story.copy(status = "DRAFT")
                            repository.saveRealStory(updated)
                        } catch (e: Exception) {
                            android.util.Log.e("BulkDraft", "Failed to draft story: $id", e)
                        }
                    }
                }
                val refreshed = repository.getAllRealStoriesAdmin()
                _adminRealStories.value = refreshed
                _realStoriesList.value = repository.getRealStories(true)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در پیش‌نویس کردن گروهی داستان‌ها: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteRealStoriesBulk(ids: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                ids.forEach { id ->
                    try {
                        repository.deleteRealStory(id)
                    } catch (e: Exception) {
                        android.util.Log.e("BulkDelete", "Failed to delete story: $id", e)
                    }
                }
                val refreshed = repository.getAllRealStoriesAdmin()
                _adminRealStories.value = refreshed
                _realStoriesList.value = repository.getRealStories(true)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در حذف گروهی داستان‌ها: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun publishSubmissionsBulk(ids: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                ids.forEach { id ->
                    val sub = _adminSubmissions.value.find { it.id == id }
                    if (sub != null && sub.status != "PUBLISHED") {
                        try {
                            val updated = sub.copy(status = "PUBLISHED")
                            repository.saveUserSubmission(updated)
                        } catch (e: Exception) {
                            android.util.Log.e("BulkPublishSub", "Failed to publish submission: $id", e)
                        }
                    }
                }
                val refreshed = repository.getAllUserSubmissionsAdmin()
                _adminSubmissions.value = refreshed
                _userSubmissionsList.value = repository.getUserSubmissions(true)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در انتشار گروهی ارسالی‌ها: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun draftSubmissionsBulk(ids: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                ids.forEach { id ->
                    val sub = _adminSubmissions.value.find { it.id == id }
                    if (sub != null && sub.status == "PUBLISHED") {
                        try {
                            val updated = sub.copy(status = "PENDING")
                            repository.saveUserSubmission(updated)
                        } catch (e: Exception) {
                            android.util.Log.e("BulkDraftSub", "Failed to draft submission: $id", e)
                        }
                    }
                }
                val refreshed = repository.getAllUserSubmissionsAdmin()
                _adminSubmissions.value = refreshed
                _userSubmissionsList.value = repository.getUserSubmissions(true)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در پیش‌نویس کردن گروهی ارسالی‌ها: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteSubmissionsBulk(ids: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                ids.forEach { id ->
                    try {
                        repository.deleteUserSubmission(id)
                    } catch (e: Exception) {
                        android.util.Log.e("BulkDeleteSub", "Failed to delete submission: $id", e)
                    }
                }
                val refreshed = repository.getAllUserSubmissionsAdmin()
                _adminSubmissions.value = refreshed
                _userSubmissionsList.value = repository.getUserSubmissions(true)
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در حذف گروهی ارسالی‌ها: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateRealStory(id: String, title: String, content: String, author: String, source: String, coverUrl: String, tags: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val current = _adminRealStories.value.find { it.id == id }
                val updated = RealStory(
                    id = id,
                    title = title,
                    content = content,
                    author = author,
                    source = source,
                    cover_image_url = coverUrl.ifBlank { null },
                    tags = tags,
                    status = status,
                    rating = current?.rating ?: 0f,
                    rating_count = current?.rating_count ?: 0,
                    view_count = current?.view_count ?: 0,
                    createdAt = null,
                    updatedAt = null
                )
                val saved = repository.saveRealStory(updated)
                _adminRealStories.value = _adminRealStories.value.map { if (it.id == id) saved else it }
                if (status == "PUBLISHED") {
                    _realStoriesList.value = _realStoriesList.value.filter { it.id != id } + listOf(saved)
                } else {
                    _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در بروزرسانی داستان: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateRealStoryStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val story = _adminRealStories.value.find { it.id == id }
                if (story != null) {
                    val updated = story.copy(status = newStatus)
                    val saved = repository.saveRealStory(updated)
                    _adminRealStories.value = _adminRealStories.value.map { if (it.id == id) saved else it }
                    if (newStatus == "PUBLISHED") {
                        if (_realStoriesList.value.none { it.id == id }) {
                            _realStoriesList.value = listOf(saved) + _realStoriesList.value
                        }
                    } else {
                        _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
                    }
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در تغییر وضعیت داستان: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteRealStory(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteRealStory(id)
                _adminRealStories.value = _adminRealStories.value.filter { it.id != id }
                _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در حذف داستان: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun testGeminiModel(key: String, model: String, onResult: (Boolean, String) -> Unit) {
        val apiKey = key.ifBlank { getEffectiveGeminiApiKey() }
        if (apiKey.isBlank()) {
            onResult(false, "کلید API معتبر نیست یا خالی است.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val client = OkHttpClient.Builder()
                .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", "پاسخ بسیار کوتاه دو کلمه‌ای در زمینه وحشت بده.")
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val actualModel = model.trim()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$actualModel:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                val latency = System.currentTimeMillis() - startTime
                val bodyStr = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val responseObj = JSONObject(bodyStr)
                    val candidates = responseObj.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    val generatedText = parts.getJSONObject(0).getString("text")
                    withContext(Dispatchers.Main) {
                        onResult(true, "اتصال با مدل $model موفق بود ($latency ms):\n${generatedText.trim()}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, "خطا در مدل $model (کد ${response.code}): $bodyStr")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "خطای ارتباط با $model: ${e.localizedMessage}")
                }
            }
        }
    }

    fun generateAILore(prompt: String, model: String? = null, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = getEffectiveGeminiApiKey()
            if (apiKey.isBlank()) {
                withContext(Dispatchers.Main) {
                    onResult("خطا: کلید هوش مصنوعی یافت نشد. لطفاً در تنظیمات پنل ادمین کلید Gemini API را وارد و ذخیره کنید.")
                }
                return@launch
            }

            val actualModel = model?.trim() ?: _selectedGeminiModel.value.trim()
            val client = OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val systemInstructionObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "تو کاتب باستانی و نگهبان ارواح عمارت وحشت گوتیک هستی. لحن تو باید کاملاً ادبی، رازآلود، گوتیک، مهیج و فوق‌العاده ترسناک باشد. فقط به زبان فارسی روان و شکیل پاسخ بنویس.")
                        })
                    }
                    put("parts", partsArray)
                }
                put("systemInstruction", systemInstructionObj)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$actualModel:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                val bodyStr = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val responseObj = JSONObject(bodyStr)
                    val candidates = responseObj.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    val generatedText = parts.getJSONObject(0).getString("text")
                    withContext(Dispatchers.Main) {
                        onResult(generatedText.trim())
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult("خطا در پاسخ هوش مصنوعی (${response.code}): $bodyStr")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("خطا در اتصال به هوش مصنوعی: ${e.localizedMessage}")
                }
            }
        }
    }

    fun generateGrimFortunesWithAI(customPrompt: String? = null, onResult: (Boolean, String, Int) -> Unit) {
        val basePrompt = customPrompt?.ifBlank { null } ?: _grimFortunePrompt.value
        val prompt = "$basePrompt\n\n" +
                "پاسخ خود را دقیقاً و صرفاً به صورت یک ساختار معتبر JSON فارسی با ساختار زیر بازگردان. هیچ توضیح اضافی قبل یا بعد از JSON ارائه نده:\n" +
                "{\n" +
                "  \"fortunes\": [\n" +
                "    {\n" +
                "      \"month_index\": 1,\n" +
                "      \"month_name\": \"فروردین\",\n" +
                "      \"title\": \"یک عنوان حماسی و شوم ترسناک\",\n" +
                "      \"fortune_text\": \"تفسیر طالع و هشدار تاریک و پیش‌گویی هولناک مخصوص متولدین این ماه\",\n" +
                "      \"omen_poem\": \"یک تک‌بیت شعر فال تاریک ملهم از حافظ شیرازی\",\n" +
                "      \"doom_level\": \"بسیار شوم\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "باید برای هر ۱۲ ماه سال (فروردین تا اسفند) یعنی month_index های ۱ تا ۱۲ دقیقاً یک آیتم وجود داشته باشد."

        generateAILore(prompt) { responseText ->
            if (responseText.startsWith("خطا")) {
                onResult(false, responseText, 0)
                return@generateAILore
            }

            try {
                val startIndex = responseText.indexOf("{")
                val endIndex = responseText.lastIndexOf("}")
                if (startIndex == -1 || endIndex == -1) {
                    throw Exception("ساختار پاسخ هوش مصنوعی قالب معتبر JSON ندارد.")
                }
                val jsonStr = responseText.substring(startIndex, endIndex + 1)
                val root = org.json.JSONObject(jsonStr)
                val array = root.getJSONArray("fortunes")
                val fortunesList = mutableListOf<GrimFortune>()

                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val mIndex = item.getInt("month_index")
                    val mName = item.optString("month_name", PERSIAN_MONTHS.getOrElse(mIndex - 1) { "ماه $mIndex" })
                    val title = item.optString("title", "طالع ماه $mName")
                    val poem = item.optString("omen_poem", "")
                    val text = item.optString("fortune_text", "")
                    val doom = item.optString("doom_level", "شوم")

                    if (mIndex in 1..12 && text.isNotBlank()) {
                        fortunesList.add(
                            GrimFortune(
                                id = java.util.UUID.randomUUID().toString(),
                                month_index = mIndex,
                                month_name = mName,
                                title = title,
                                omen_poem = poem.ifBlank { null },
                                fortune_text = text,
                                doom_level = doom,
                                status = "PUBLISHED",
                                createdAt = null,
                                updatedAt = null
                            )
                        )
                    }
                }

                if (fortunesList.isEmpty()) {
                    throw Exception("هیچ طالع معتبری در پاسخ یافت نشد.")
                }

                viewModelScope.launch {
                    _loading.value = true
                    try {
                        val success = repository.upsertGrimFortunes(fortunesList)
                        if (success) {
                            val updatedAdmin = _adminGrimFortunes.value.filter { adminItem ->
                                fortunesList.none { it.month_index == adminItem.month_index }
                            } + fortunesList
                            _adminGrimFortunes.value = updatedAdmin
                            _grimFortunesList.value = fortunesList
                            onResult(true, "تعداد ${fortunesList.size} ماه طالع با موفقیت تولید و در سرور Supabase ذخیره گردید.", fortunesList.size)
                        } else {
                            onResult(false, "خطا در همگام‌سازی طالع‌ها با Supabase.", 0)
                        }
                    } catch (e: Exception) {
                        onResult(false, "خطای ذخیره‌سازی: ${e.localizedMessage}", 0)
                    } finally {
                        _loading.value = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "Parsing JSON batch fortunes failed", e)
                onResult(false, "خطا در پردازش پاسخ هوش مصنوعی: ${e.localizedMessage}", 0)
            }
        }
    }

    fun generateGrimFortuneForSingleMonth(monthIndex: Int, customPrompt: String? = null, onResult: (Boolean, String, String?, String, String?) -> Unit) {
        val monthName = PERSIAN_MONTHS[monthIndex - 1]
        val prompt = "تو حافظ شیرازی در دنیای تاریک و گوتیک عمارت وحشت هستی. یک فال و طالع شوم، بسیار ادبی، هولناک و جذاب مخصوص متولدین ماه $monthName بنویس.\n" +
                "فرمت پاسخ:\n" +
                "عنوان: [یک عنوان کوتاه دلهره‌آور]\n" +
                "شعر: [یک بیت شعر فال شوم سبک حافظ]\n" +
                "طالع: [تفسیر سرنوشت و هشدار ماورایی]\n" +
                "درجه: [شوم / بسیار شوم / نفرین ابدی]"

        generateAILore(prompt) { text ->
            if (text.startsWith("خطا")) {
                onResult(false, "", null, text, null)
                return@generateAILore
            }
            val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
            var title = "طالع ماه $monthName"
            var poem: String? = null
            var fortuneText = text
            var doomLevel: String? = "شوم"

            for (line in lines) {
                when {
                    line.contains("عنوان:") -> title = line.replace("عنوان:", "").replace("#", "").replace("*", "").trim()
                    line.contains("شعر:") -> poem = line.replace("شعر:", "").replace("#", "").replace("*", "").trim()
                    line.contains("طالع:") -> fortuneText = line.replace("طالع:", "").replace("#", "").replace("*", "").trim()
                    line.contains("درجه:") -> doomLevel = line.replace("درجه:", "").replace("#", "").replace("*", "").trim()
                }
            }
            onResult(true, title, poem, fortuneText, doomLevel)
        }
    }

    // ==========================================
    // AI STORIES LOGIC & MANAGEMENT
    // ==========================================

    fun rateAiStory(storyId: String, userRating: Float) {
        viewModelScope.launch {
            val p = getApplication<Application>().getSharedPreferences("horror_ai_story_ratings", android.content.Context.MODE_PRIVATE)
            val existingVote = p.getFloat("rating_$storyId", -1f)
            if (existingVote != -1f) {
                _errorMessage.value = "شما قبلاً به این داستان امتیاز داده‌اید (${existingVote.toInt()} ستاره)."
                return@launch
            }
            p.edit().putFloat("rating_$storyId", userRating).apply()

            val currentStories = _aiStoriesList.value
            val target = currentStories.find { it.id == storyId }

            if (target != null) {
                val safeCount = target.rating_count.coerceAtLeast(0)
                val safeRating = if (safeCount == 0) 0f else target.rating
                val newCount = safeCount + 1
                val newRating = if (safeCount == 0) userRating else (((safeRating * safeCount) + userRating) / newCount)
                val roundedRating = kotlin.math.round(newRating * 10f) / 10.0f
                val updatedTarget = target.copy(rating = roundedRating, rating_count = newCount)

                _aiStoriesList.value = currentStories.map { if (it.id == storyId) updatedTarget else it }
                _adminAiStories.value = _adminAiStories.value.map { if (it.id == storyId) updatedTarget else it }
            }

            try {
                val success = repository.submitAiStoryRatingRemote(storyId, userRating)
                val refreshed = repository.getAiStories(success)
                if (refreshed.isNotEmpty()) {
                    _aiStoriesList.value = refreshed.filter { it.status == "PUBLISHED" }
                    _adminAiStories.value = refreshed
                }
            } catch (e: Exception) {
                android.util.Log.e("AiStoryRating", "Error submitting AI story rating: ${e.localizedMessage}")
            }
        }
    }

    fun incrementAiStoryViews(storyId: String) {
        viewModelScope.launch {
            val currentStories = _aiStoriesList.value
            val target = currentStories.find { it.id == storyId }
            if (target != null) {
                val newCount = target.view_count + 1
                val updatedTarget = target.copy(view_count = newCount)
                _aiStoriesList.value = currentStories.map { if (it.id == storyId) updatedTarget else it }
                _adminAiStories.value = _adminAiStories.value.map { if (it.id == storyId) updatedTarget else it }
            }

            try {
                val success = repository.incrementAiStoryViewRemote(storyId)
                if (success) {
                    val refreshed = repository.getAiStories(true)
                    if (refreshed.isNotEmpty()) {
                        _aiStoriesList.value = refreshed.filter { it.status == "PUBLISHED" }
                        _adminAiStories.value = refreshed
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AiStoryViews", "Error incrementing AI story views: ${e.localizedMessage}")
            }
        }
    }

    fun updateAiStoryStatus(id: String, newStatus: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val current = _adminAiStories.value.find { it.id == id }
                if (current != null) {
                    val updated = current.copy(status = newStatus)
                    repository.upsertAiStories(listOf(updated))
                    _adminAiStories.value = _adminAiStories.value.map { if (it.id == id) updated else it }
                    if (newStatus == "PUBLISHED") {
                        if (_aiStoriesList.value.none { it.id == id }) {
                            _aiStoriesList.value = listOf(updated) + _aiStoriesList.value
                        }
                    } else {
                        _aiStoriesList.value = _aiStoriesList.value.filter { it.id != id }
                    }
                }
                onResult(true, "وضعیت داستان به ${if (newStatus == "PUBLISHED") "منتشر شده" else "پیش‌نویس"} تغییر یافت.")
            } catch (e: Exception) {
                onResult(false, "خطا در تغییر وضعیت: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteAiStory(id: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteAiStory(id)
                _adminAiStories.value = _adminAiStories.value.filter { it.id != id }
                _aiStoriesList.value = _aiStoriesList.value.filter { it.id != id }
                onResult(true, "داستان هوش مصنوعی با موفقیت حذف گردید.")
            } catch (e: Exception) {
                onResult(false, "خطا در حذف داستان: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun bulkUpdateAiStoriesStatus(ids: List<String>, newStatus: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val toUpdate = _adminAiStories.value.filter { ids.contains(it.id) }.map { it.copy(status = newStatus) }
                repository.upsertAiStories(toUpdate)
                val idSet = ids.toSet()
                _adminAiStories.value = _adminAiStories.value.map { if (idSet.contains(it.id)) it.copy(status = newStatus) else it }
                if (newStatus == "PUBLISHED") {
                    val currentNonUpdated = _aiStoriesList.value.filter { !idSet.contains(it.id) }
                    _aiStoriesList.value = toUpdate + currentNonUpdated
                } else {
                    _aiStoriesList.value = _aiStoriesList.value.filter { !idSet.contains(it.id) }
                }
                onResult(true, "${ids.size} داستان به وضعیت ${if (newStatus == "PUBLISHED") "منتشر شده" else "پیش‌نویس"} منتقل شدند.")
            } catch (e: Exception) {
                onResult(false, "خطا در عملیات گروهی: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun bulkDeleteAiStories(ids: List<String>, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                ids.forEach { repository.deleteAiStory(it) }
                val idSet = ids.toSet()
                _adminAiStories.value = _adminAiStories.value.filter { !idSet.contains(it.id) }
                _aiStoriesList.value = _aiStoriesList.value.filter { !idSet.contains(it.id) }
                onResult(true, "${ids.size} داستان با موفقیت حذف شدند.")
            } catch (e: Exception) {
                onResult(false, "خطا در حذف گروهی: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateAiStory(story: AiStory, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.upsertAiStories(listOf(story))
                _adminAiStories.value = _adminAiStories.value.map { if (it.id == story.id) story else it }
                if (story.status == "PUBLISHED") {
                    _aiStoriesList.value = _aiStoriesList.value.map { if (it.id == story.id) story else it }
                } else {
                    _aiStoriesList.value = _aiStoriesList.value.filter { it.id != story.id }
                }
                onResult(true, "داستان با موفقیت ویرایش و ذخیره شد.")
            } catch (e: Exception) {
                onResult(false, "خطا در ویرایش داستان: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun generateAiStoriesWithAI(
        customPrompt: String? = null,
        count: Int = 3,
        genre: String? = null,
        onResult: (Boolean, String, Int) -> Unit
    ) {
        val safeCount = count.coerceIn(1, 20)
        _aiGenQueueState.value = AiGenQueueState(
            isGenerating = true,
            totalRequested = safeCount,
            completedCount = 0,
            statusMessage = "آغاز صف تولید $safeCount داستان..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val horrorPosters = listOf(
                "img_ai_story_poster_1",
                "img_ai_story_poster_2",
                "img_poster_1",
                "img_poster_2",
                "img_poster_3",
                "img_dark_hafez_banner",
                "img_dark_sorcerer_banner",
                "img_sorcery_temple",
                "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=600&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=600&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1514533450685-4493e01d1fdc?w=600&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80"
            )

            val chunkSize = 2
            var totalGenerated = 0
            val allCreatedStories = mutableListOf<AiStory>()

            var remaining = safeCount
            while (remaining > 0) {
                val batchCount = minOf(remaining, chunkSize)
                _aiGenQueueState.value = _aiGenQueueState.value.copy(
                    statusMessage = "در حال تولید دسته ${totalGenerated + 1} تا ${totalGenerated + batchCount} از $safeCount..."
                )

                val basePrompt = customPrompt?.ifBlank { null } ?: _aiStoryPrompt.value
                val genreText = if (!genre.isNullOrBlank() && genre != "همه") {
                    "در ژانر مشخص وحشت «$genre»"
                } else {
                    "با تنوع کامل در ژانرها، فضاها و سناریوهای مختلف (مطابق با قانون تنوع و فهرست ژانرها در دستورالعمل بالا: وحشت روانشناختی، ماورایی، جنایی-معمایی، موجودات ناشناخته، فولکلور و باورهای عامیانه ایرانی، فضاهای شهری/روستایی/جاده‌ای)"
                }

                val prompt = "$basePrompt\n\n" +
                        "دستور تولید داستان:\n" +
                        "دقیقاً تعداد $batchCount داستان ترسناک و دلهره‌آور تازه، غافلگیرکننده، عمیق و پرتعلیق $genreText خلق کن.\n" +
                        "تمام اصول دستورالعمل بالا (فضاسازی ملموس ایرانی، دوری از کلیشه‌ها، کاشت سرنخ، پیچش‌های داستانی و پایان تکان‌دهنده) باید در داستان‌ها رعایت شود.\n" +
                        "پاسخ خود را دقیقاً و صرفاً به صورت یک JSON استاندارد با ساختار زیر ارائه کن (بدون هیچ کلمه اضافی، مارک‌داون یا توضیحات دیگر):\n" +
                        "{\n" +
                        "  \"stories\": [\n" +
                        "    {\n" +
                        "      \"title\": \"یک عنوان جذاب، غافلگیرکننده و خوفناک\",\n" +
                        "      \"genre\": \"ژانر دقیق داستان (مثلاً: روانشناختی، ماورایی، فولکلور ایرانی، جنایی، موجودات ناشناخته، شهری)\",\n" +
                        "      \"content\": \"متن کامل، منسجم و سینمایی داستان با توصیف فضاسازی قوی، تعلیق نفس‌گیر و پایان شگفت‌آور\",\n" +
                        "      \"synopsis\": \"خلاصه داستان در ۲ یا ۳ جمله کنجکاوی‌برانگیز\",\n" +
                        "      \"doom_score\": 85\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n" +
                        "تعداد داستان‌های خروجی در آرایه stories باید دقیقاً $batchCount عدد باشد."

                var batchSuccess = false
                var batchErrorMsg = ""

                try {
                    val apiKey = getEffectiveGeminiApiKey()
                    if (apiKey.isBlank()) {
                        batchErrorMsg = "کلید هوش مصنوعی یافت نشد."
                    } else {
                        val actualModel = _selectedGeminiModel.value.trim()
                        val client = OkHttpClient.Builder()
                            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                            .build()

                        val requestJson = JSONObject().apply {
                            val contentsArray = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("parts", JSONArray().apply {
                                        put(JSONObject().apply { put("text", prompt) })
                                    })
                                })
                            }
                            put("contents", contentsArray)
                            put("systemInstruction", JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", "تو کاتب باستانی و نگهبان ارواح عمارت وحشت گوتیک هستی. لحن تو باید کاملاً ادبی، رازآلود، گوتیک، مهیج و فوق‌العاده ترسناک باشد. فقط به زبان فارسی روان و شکیل پاسخ بنویس.")
                                    })
                                })
                            })
                        }

                        val request = Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/$actualModel:generateContent?key=$apiKey")
                            .post(requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                            .build()

                        val response = client.newCall(request).execute()
                        val bodyStr = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            val responseObj = JSONObject(bodyStr)
                            val candidates = responseObj.getJSONArray("candidates")
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.getJSONObject("content")
                            val parts = content.getJSONArray("parts")
                            val responseText = parts.getJSONObject(0).getString("text").trim()

                            val startIndex = responseText.indexOf("{")
                            val endIndex = responseText.lastIndexOf("}")
                            if (startIndex != -1 && endIndex != -1) {
                                val jsonStr = responseText.substring(startIndex, endIndex + 1)
                                val root = org.json.JSONObject(jsonStr)
                                val array = root.getJSONArray("stories")

                                for (i in 0 until array.length()) {
                                    val item = array.getJSONObject(i)
                                    val title = item.optString("title", "روایت هوش مصنوعی")
                                    val storyGenre = item.optString("genre", genre ?: "ماورایی")
                                    val contentText = item.optString("content", "")
                                    val synopsis = item.optString("synopsis", contentText.take(120))
                                    val doomScore = item.optInt("doom_score", (75..98).random())
                                    val poster = horrorPosters.random()

                                    if (contentText.isNotBlank()) {
                                        val pseudoId = java.util.UUID.randomUUID().toString()
                                        val story = AiStory(
                                            id = pseudoId,
                                            title = title,
                                            content = contentText,
                                            genre = storyGenre,
                                            synopsis = synopsis,
                                            cover_image_url = poster,
                                            tags = "$storyGenre, هوش مصنوعی",
                                            status = "PUBLISHED",
                                            rating = 0f,
                                            rating_count = 0,
                                            view_count = 0,
                                            createdAt = null,
                                            updatedAt = null
                                        )
                                        allCreatedStories.add(story)
                                        repository.upsertAiStories(listOf(story))
                                    }
                                }
                                batchSuccess = true
                            }
                        } else {
                            val errBody = bodyStr
                            batchErrorMsg = if (response.code == 429 || errBody.contains("RESOURCE_EXHAUSTED") || errBody.contains("quota")) {
                                "سهمیه رایگان کلید Gemini به اتمام رسیده است (Resource Exhausted). لطفاً کلید API معتبر دیگری در تنظیمات وارد کنید."
                            } else {
                                "خطای سرور گوگل (${response.code})"
                            }
                        }
                    }
                } catch (e: Exception) {
                    batchErrorMsg = e.localizedMessage ?: "خطای ناشناخته"
                }

                if (!batchSuccess && allCreatedStories.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _aiGenQueueState.value = AiGenQueueState(isGenerating = false, totalRequested = safeCount, completedCount = totalGenerated, statusMessage = "خطا در صف: $batchErrorMsg")
                        onResult(false, "خطا در تولید داستان‌ها: $batchErrorMsg", totalGenerated)
                    }
                    return@launch
                }

                totalGenerated += batchCount
                remaining -= batchCount
                withContext(Dispatchers.Main) {
                    _aiGenQueueState.value = _aiGenQueueState.value.copy(
                        completedCount = totalGenerated,
                        statusMessage = "تکمیل شده: $totalGenerated از $safeCount داستان"
                    )
                    _adminAiStories.value = allCreatedStories + _adminAiStories.value
                    _aiStoriesList.value = allCreatedStories + _aiStoriesList.value
                }

                kotlinx.coroutines.delay(800)
            }

            withContext(Dispatchers.Main) {
                _aiGenQueueState.value = AiGenQueueState(
                    isGenerating = false,
                    totalRequested = safeCount,
                    completedCount = allCreatedStories.size,
                    statusMessage = "صف تولید با موفقیت کامل شد! (${allCreatedStories.size} داستان اضافه شد)"
                )
                onResult(true, "تعداد ${allCreatedStories.size} داستان با موفقیت تولید و منتشر شدند.", allCreatedStories.size)
            }
        }
    }

    fun getMockAiStories(): List<AiStory> {
        return listOf(
            AiStory(
                id = "ai-mock-1",
                title = "زمزمه‌های زیرزمین عمارت قاجاری",
                content = "در انتهای کوچه بن‌بست سرچشمه رشت، عمارتی با ستون‌های گچ‌بری شده و پنجره‌های رنگی سال‌هاست متروکه مانده است. اهالی محل می‌گفتند آخرین صاحب خانه، تاجر ابریشمی بود که پس از مفقود شدن ناگهانی همسرش، درهای زیرزمین را با آجر و ساروج مسدود کرد و دیگر هرگز کسی خروج او را ندید...\n\nشبی بارانی، دو دانشجوی معماری برای نقشه‌برداری غیرقانونی از پنجره شکسته شاه‌نشین وارد خانه شدند. بوی نم کهنه و چوب سوخته در فضا موج می‌زد. وقتی به راه‌پله نمور زیرزمین رسیدند، متوجه شدند دیواره ساروجی شکسته شده و روزنه‌ای به تاریکی مطلق باز شده است. از درون روزنه، صدای ضربات ملایم و یکنواخت چرخ خیاطی دستی به گوش می‌رسید؛ گویی کسی در ظلمت بی‌پایان هنوز برای عروسی ناکام ابریشم می‌بافت...\n\nوقتی نور چراغ‌قوه را به درون تاباندند، صندلی خالی چوبی به آرامی تکان می‌خورد و رد پاهای خیس و گلی از تاریکی ژرف به سمت دهانه روزنه کشیده شده بود. همان لحظه، در چوبی پشت سرشان با صدای مهیبی قفل شد.",
                synopsis = "دو دانشجو در شبی بارانی وارد عمارت متروکه قاجاری می‌شوند؛ جایی که صدای چرخ خیاطی از زیرزمین مسدود شده به گوش می‌رسد...",
                genre = "ماورایی",
                cover_image_url = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=600&auto=format&fit=crop&q=80",
                tags = "ماورایی, عمارت",
                status = "PUBLISHED",
                rating = 4.8f,
                rating_count = 18,
                view_count = 142,
                createdAt = null,
                updatedAt = null
            ),
            AiStory(
                id = "ai-mock-2",
                title = "تماس از باجه خاموش گورستان ابن‌بابویه",
                content = "ساعت از ۲ نیمه‌شب گذشته بود که کیهان، شیفت شب تاکسی اینترنتی، درخواستی از مبدأ درب جنوبی گورستان ابن‌بابویه دریافت کرد. کرایه پیشنهاد شده سه برابر نرخ معمول بود. به محض رسیدن، باجه تلفن همگانی زرد و زنگ‌زده‌ای را دید که سال‌ها پیش کابل‌هایش قطع شده بود، اما گوشی آن به آرامی تاب می‌خورد.\n\nتلفن همراه کیهان زنگ خورد: «مسافر شما منتظر است». صدای پشت خط زنی بود با لحنی آرام و منجمد که گفت: «لطفاً سوار شو... پشت سرت نشسته‌ام». کیهان با وحشت به آینه نگاه کرد؛ صندلی عقب خالی بود، اما بوی تند خاک خیس و گلاب فضا را پر کرد و دمای داخل کابین خودرو ناگهان به زیر صفر سقوط کرد...\n\nروی شیشه بخار گرفته سمت شاگرد، دستی نامرئی کلماتی را حک کرد: «کرایه این مسیر، جان توست». خودرو ناگهان خودبخود روشن شد و پدال گاز تا انتها فشرده شد، در حالی که فرمان در دستان کیهان قفل شده بود.",
                synopsis = "یک راننده شیفت شب، درخواستی عجیب از درب گورستان قدیمی دریافت می‌کند؛ مسافری که پیش از سوار شدن، در ماشین حضور دارد...",
                genre = "روانشناختی",
                cover_image_url = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
                tags = "روانشناختی, گورستان",
                status = "PUBLISHED",
                rating = 4.6f,
                rating_count = 12,
                view_count = 98,
                createdAt = null,
                updatedAt = null
            ),
            AiStory(
                id = "ai-mock-3",
                title = "نجوای چاه قلعه دیو اردبیل",
                content = "در ارتفاعات مه گرفته سبلان، بقایای قلعه‌ای مخوف به نام قلعه دیو قرار دارد که بومیان از رفتن به آنجا پس از غروب آفتاب شدیداً پرهیز می‌کنند. افسانه‌ها می‌گویند در ژرفای چاه سنگی حیاط قلعه، موجودی نامیرا به بند کشیده شده که با تکرار نام قربانیان، آنان را به خواب مغناطیسی فرو می‌برد.\n\nتیم کوهنوردی پنج نفره تهرانی، به رغم هشدارهای روستاییان، چادر خود را در کنار دهانه چاه برپا کردند. نیمه‌شب، یکی از اعضا با چشمانی باز اما بی‌فروغ از چادر بیرون رفت. وقتی دوستانش بیدار شدند، او را ایستاده بر لبه باریک چاه دیدند که با لبخندی بی‌روح نام تک‌تک اعضا را با ریتمی مرگبار نجوا می‌کرد...\n\nقبل از آنکه دست کسی به او برسد، دستانی سیاه و استخوانی از اعماق چاه بیرون جهیدند و او را در سیاهی بی‌انتها فرو کشیدند. تنها چیزی که از او باقی ماند، صدای خنده‌های پژواک‌یافته در ژرفای تاریکی بود.",
                synopsis = "یک گروه کوهنوردی در نزدیکی قلعه نفرین‌شده باستانی چادر می‌زنند؛ جایی که چاه سنگی صداهای آشنا را زمزمه می‌کند...",
                genre = "افسانه ایرانی",
                cover_image_url = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=600&auto=format&fit=crop&q=80",
                tags = "افسانه ایرانی, قلعه",
                status = "PUBLISHED",
                rating = 4.9f,
                rating_count = 24,
                view_count = 215,
                createdAt = null,
                updatedAt = null
            )
        )
    }
}

