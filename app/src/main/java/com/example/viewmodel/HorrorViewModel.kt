package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
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

enum class AppMode {
    USER, ADMIN_LOGIN, ADMIN_PANEL
}

class HorrorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HorrorRepository(application)
    private val api = SupabaseClientProvider.api
    private val prefs = application.getSharedPreferences("horror_admin_prefs", android.content.Context.MODE_PRIVATE)

    companion object {
        const val PREF_GEMINI_KEY = "pref_gemini_api_key"
        const val PREF_GEMINI_MODEL = "pref_gemini_model"
        const val PREF_GRIM_FORTUNE_PROMPT = "pref_grim_fortune_prompt"
        const val PREF_SCENARIO_PROMPT = "pref_scenario_prompt"

        val SUPPORTED_GEMINI_MODELS = listOf(
            "gemini-3.5-flash",
            "gemini-3.6-flash",
            "gemini-3.7-flash",
            "gemini-3.5-flash-lite"
        )

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
            "یک سناریوی بازی تعاملی ترسناک چندگزینه‌ای (تصمیم‌گیری برای بقا در عمارت وحشت گوتیک) به زبان فارسی تولید کن. فرمت پاسخ باید دارای یک عنوان جذاب در خط اول و توصیف دلهره‌آور موقعیت و خطرات آن در خطوط بعدی باشد."
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

    private val _selectedGeminiModel = MutableStateFlow(prefs.getString(PREF_GEMINI_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val selectedGeminiModel: StateFlow<String> = _selectedGeminiModel.asStateFlow()

    private val _grimFortunePrompt = MutableStateFlow(prefs.getString(PREF_GRIM_FORTUNE_PROMPT, DEFAULT_GRIM_FORTUNE_PROMPT) ?: DEFAULT_GRIM_FORTUNE_PROMPT)
    val grimFortunePrompt: StateFlow<String> = _grimFortunePrompt.asStateFlow()

    private val _scenarioPrompt = MutableStateFlow(prefs.getString(PREF_SCENARIO_PROMPT, DEFAULT_SCENARIO_PROMPT) ?: DEFAULT_SCENARIO_PROMPT)
    val scenarioPrompt: StateFlow<String> = _scenarioPrompt.asStateFlow()

    // User Data States
    private val _grimFortunesList = MutableStateFlow<List<GrimFortune>>(emptyList())
    val grimFortunesList: StateFlow<List<GrimFortune>> = _grimFortunesList.asStateFlow()

    private val _realStoriesList = MutableStateFlow<List<RealStory>>(emptyList())
    val realStoriesList: StateFlow<List<RealStory>> = _realStoriesList.asStateFlow()

    private val _scenariosList = MutableStateFlow<List<WrongChoiceScenario>>(emptyList())
    val scenariosList: StateFlow<List<WrongChoiceScenario>> = _scenariosList.asStateFlow()

    private val _userSubmissionsList = MutableStateFlow<List<UserStorySubmission>>(emptyList())
    val userSubmissionsList: StateFlow<List<UserStorySubmission>> = _userSubmissionsList.asStateFlow()

    // Admin Management States
    private val _adminGrimFortunes = MutableStateFlow<List<GrimFortune>>(emptyList())
    val adminGrimFortunes: StateFlow<List<GrimFortune>> = _adminGrimFortunes.asStateFlow()

    private val _adminRealStories = MutableStateFlow<List<RealStory>>(emptyList())
    val adminRealStories: StateFlow<List<RealStory>> = _adminRealStories.asStateFlow()

    private val _adminSubmissions = MutableStateFlow<List<UserStorySubmission>>(emptyList())
    val adminSubmissions: StateFlow<List<UserStorySubmission>> = _adminSubmissions.asStateFlow()

    private val _adminScenarios = MutableStateFlow<List<WrongChoiceScenario>>(emptyList())
    val adminScenarios: StateFlow<List<WrongChoiceScenario>> = _adminScenarios.asStateFlow()

    private val _aiPrompts = MutableStateFlow<List<AiPrompt>>(emptyList())
    val aiPrompts: StateFlow<List<AiPrompt>> = _aiPrompts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserData()
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
        if (mode == AppMode.ADMIN_PANEL) {
            loadAdminData()
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Fetch Grim Fortunes
                val gfs = repository.getGrimFortunes(true)
                _grimFortunesList.value = gfs.ifEmpty { getMockGrimFortunes() }

                // Fetch Real Stories
                val rs = repository.getRealStories(true)
                _realStoriesList.value = rs.ifEmpty { getMockRealStories() }

                // Fetch Scenarios
                val resp = api.getScenarios(status = "PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    _scenariosList.value = resp.body()!!.ifEmpty { getMockScenarios() }
                } else {
                    _scenariosList.value = getMockScenarios()
                }

                // Fetch User Submissions/Confessions
                val subResp = api.getUserSubmissions()
                if (subResp.isSuccessful && subResp.body() != null) {
                    _userSubmissionsList.value = subResp.body()!!.filter { it.status == "PUBLISHED" }.ifEmpty { getMockUserSubmissions() }
                } else {
                    _userSubmissionsList.value = getMockUserSubmissions()
                }
            } catch (e: Exception) {
                _grimFortunesList.value = getMockGrimFortunes()
                _realStoriesList.value = getMockRealStories()
                _scenariosList.value = getMockScenarios()
                _userSubmissionsList.value = getMockUserSubmissions()
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
                id = "gf-$index",
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
                id = "story-1",
                title = "نجواهای عمارت قاجاری",
                content = "در زمستان سال ۱۳۲۰، پدربزرگم عمارتی قدیمی در حاشیه باغ‌های شمیران خرید. شب اول صدای کوبیده شدن پنجره‌ها قطع نمی‌شد تا اینکه ساعت ۳ نیمه‌شب در آینه قدی متوجه سایه‌ای با کلاه دوره قاجار شد که به او خیره شده بود...",
                author = "سهراب کاتب",
                source = "خاطرات شفاهی تهران قدیم",
                cover_image_url = null,
                tags = "واقعی, تهران قدیم, عمارت تسخیر شده",
                status = "PUBLISHED",
                createdAt = null,
                updatedAt = null
            ),
            RealStory(
                id = "story-2",
                title = "چاه نفرین‌شده روستای سیاه چشمه",
                content = "اهالی روستا می‌گفتند هر کس بعد از غروب خورشید کنار چاه قدیمی برود، صدای فریاد آب را خواهد شنید. سال گذشته دو مسافر تصمیم گرفتند درون چاه را با چراغ قوه تماشا کنند اما چیزی که از آب بالا آمد هرگز در هیچ کتابی توصیف نشده بود...",
                author = "راوی ناشناس",
                source = "روایات کهن آذربایجان",
                cover_image_url = null,
                tags = "روستا, طلسم, چاه",
                status = "PUBLISHED",
                createdAt = null,
                updatedAt = null
            )
        )
    }

    private fun getMockUserSubmissions(): List<UserStorySubmission> {
        return listOf(
            UserStorySubmission(
                id = "sub-1",
                title = "صدای قدم‌ها در اتاق زیرشیروانی",
                content = "هر شب دقیقاً رأس ساعت ۳:۱۵ بامداد، صدای کشیده شدن صندلی چوبی روی کف اتاق زیرشیروانی خانه ما شنیده می‌شود، در حالی که در آن اتاق سال‌هاست قفل است.",
                author_name = "مریم از تبریز",
                status = "PENDING",
                admin_notes = null,
                createdAt = null,
                updatedAt = null
            )
        )
    }

    private fun getMockScenarios(): List<WrongChoiceScenario> {
        return listOf(
            WrongChoiceScenario(
                id = "scen-1",
                title = "گذرگاه دالان شرقی",
                description = "شما در آستانه ورود به دالان شرقی عمارت هستید. دیوارهای دالان مه‌آلود و سرد است. کدام مسیر را برای زنده ماندن انتخاب می‌کنید؟",
                status = "PUBLISHED",
                initial_scene_id = "scene-1-1",
                createdAt = null
            ),
            WrongChoiceScenario(
                id = "scen-2",
                title = "کلاغ‌های معبد سوخته",
                description = "برج ناقوس قدیمی لرزان معبد پیش روی شماست. صدها کلاغ بالای سر شما پرواز می‌کنند. صدای نجوایی از بالای پله‌ها می‌آید...",
                status = "PUBLISHED",
                initial_scene_id = "scene-2-1",
                createdAt = null
            ),
            WrongChoiceScenario(
                id = "scen-3",
                title = "ملاقات با کاتب ارواح",
                description = "یک در سنگین چوبی با تزیینات عتیقه در انتهای سالن قرار دارد. دستگیره در داغ و سرخ‌رنگ است. آیا وارد می‌شوید یا عقب می‌نشینید؟",
                status = "PUBLISHED",
                initial_scene_id = "scene-3-1",
                createdAt = null
            )
        )
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key.trim()
        prefs.edit().putString(PREF_GEMINI_KEY, key.trim()).apply()
    }

    fun setSelectedGeminiModel(model: String) {
        _selectedGeminiModel.value = model
        prefs.edit().putString(PREF_GEMINI_MODEL, model).apply()
    }

    fun setGrimFortunePrompt(prompt: String) {
        _grimFortunePrompt.value = prompt
        prefs.edit().putString(PREF_GRIM_FORTUNE_PROMPT, prompt).apply()
    }

    fun setScenarioPrompt(prompt: String) {
        _scenarioPrompt.value = prompt
        prefs.edit().putString(PREF_SCENARIO_PROMPT, prompt).apply()
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
                val gfResp = api.getGrimFortunes()
                if (gfResp.isSuccessful && gfResp.body() != null) {
                    _adminGrimFortunes.value = gfResp.body()!!
                } else if (_adminGrimFortunes.value.isEmpty()) {
                    _adminGrimFortunes.value = _grimFortunesList.value
                }

                val rsResp = api.getRealStories()
                if (rsResp.isSuccessful && rsResp.body() != null) {
                    _adminRealStories.value = rsResp.body()!!
                } else if (_adminRealStories.value.isEmpty()) {
                    _adminRealStories.value = _realStoriesList.value
                }

                val subResp = api.getUserSubmissions()
                if (subResp.isSuccessful && subResp.body() != null) {
                    _adminSubmissions.value = subResp.body()!!
                } else if (_adminSubmissions.value.isEmpty()) {
                    _adminSubmissions.value = _userSubmissionsList.value
                }

                val scenResp = api.getScenarios()
                if (scenResp.isSuccessful && scenResp.body() != null) {
                    _adminScenarios.value = scenResp.body()!!
                } else if (_adminScenarios.value.isEmpty()) {
                    _adminScenarios.value = _scenariosList.value
                }

                val promptResp = api.getAiPrompts()
                if (promptResp.isSuccessful && promptResp.body() != null) {
                    _aiPrompts.value = promptResp.body()!!
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در پنل مدیریت: ${e.localizedMessage}"
                if (_adminGrimFortunes.value.isEmpty()) _adminGrimFortunes.value = _grimFortunesList.value
                if (_adminRealStories.value.isEmpty()) _adminRealStories.value = _realStoriesList.value
                if (_adminSubmissions.value.isEmpty()) _adminSubmissions.value = _userSubmissionsList.value
                if (_adminScenarios.value.isEmpty()) _adminScenarios.value = _scenariosList.value
            } finally {
                _loading.value = false
            }
        }
    }

    fun loginAdmin(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (!SupabaseClientProvider.isConfigured) {
            // Check master offline credentials for convenience if Supabase is unconfigured in current build
            if (email.trim().lowercase() == "admin@gothic.com" && pass == "admin123") {
                _currentUserEmail.value = email
                _currentUserId.value = "master-admin"
                _currentUserRole.value = "ADMIN"
                setAppMode(AppMode.ADMIN_PANEL)
                onResult(true, null)
                return
            }
            onResult(false, "تنظیمات اتصال Supabase در این نسخه تنظیم نشده است. می‌توانید با ایمیل admin@gothic.com و رمز admin123 وارد حالت آزمایشی شوید یا سکرت‌های مخزن را تنظیم کنید.")
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
                        _currentUserEmail.value = userEmail
                        _currentUserId.value = userId
                        _currentUserRole.value = "ADMIN"
                        setAppMode(AppMode.ADMIN_PANEL)
                        onResult(true, null)
                    } else {
                        onResult(false, "توکن دریافت نشد.")
                    }
                } else {
                    onResult(false, "ورود ناموفق بود. ایمیل یا رمز عبور اشتباه است.")
                }
            } catch (e: Exception) {
                onResult(false, "خطای ارتباط: ${e.localizedMessage}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun testConnection(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val resp = api.getGrimFortunes(select = "id", status = null)
                if (resp.isSuccessful) {
                    onComplete(true, "اتصال به Supabase موفقیت‌آمیز بود (کد وضعیت: ${resp.code()})")
                } else {
                    onComplete(false, "خطا در پاسخ سرور: ${resp.errorBody()?.string() ?: resp.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, "خطای اتصال: ${e.localizedMessage}")
            }
        }
    }

    fun submitUserStory(title: String, content: String, author: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val newSub = UserStorySubmission(
                id = java.util.UUID.randomUUID().toString(),
                title = title.trim(),
                content = content.trim(),
                author_name = author.ifBlank { "ناشناس" }.trim(),
                status = "PENDING",
                admin_notes = null,
                createdAt = null,
                updatedAt = null
            )
            // Add immediately to local admin and user lists so it's visible without delay
            _adminSubmissions.value = listOf(newSub) + _adminSubmissions.value

            val success = repository.submitUserStory(newSub.title, newSub.content, newSub.author_name)
            onResult(true)
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
            val monthName = PERSIAN_MONTHS.getOrElse(monthIndex - 1) { "ماه $monthIndex" }
            val existing = _adminGrimFortunes.value.find { it.month_index == monthIndex }
            val id = existing?.id ?: "gf-$monthIndex"
            val newGf = GrimFortune(
                id = id,
                month_index = monthIndex,
                month_name = monthName,
                title = title,
                omen_poem = poem,
                fortune_text = fortuneText,
                doom_level = doomLevel ?: "شوم",
                status = status,
                createdAt = null,
                updatedAt = null
            )
            _adminGrimFortunes.value = _adminGrimFortunes.value.filter { it.month_index != monthIndex } + newGf
            if (status == "PUBLISHED") {
                _grimFortunesList.value = _grimFortunesList.value.filter { it.month_index != monthIndex } + newGf
            }
            try {
                val map = mapOf(
                    "id" to newGf.id,
                    "month_index" to monthIndex,
                    "month_name" to monthName,
                    "title" to title,
                    "omen_poem" to (poem ?: ""),
                    "fortune_text" to fortuneText,
                    "doom_level" to (doomLevel ?: "شوم"),
                    "status" to status
                )
                api.createGrimFortune(map)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun updateGrimFortuneStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminGrimFortunes.value = _adminGrimFortunes.value.map {
                if (it.id == id) it.copy(status = newStatus) else it
            }
            try {
                api.updateGrimFortune("eq.$id", mapOf("status" to newStatus))
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun deleteGrimFortune(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminGrimFortunes.value = _adminGrimFortunes.value.filter { it.id != id }
            _grimFortunesList.value = _grimFortunesList.value.filter { it.id != id }
            try {
                api.deleteGrimFortune("eq.$id")
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun updateSubmissionStatus(id: String, newStatus: String, adminNotes: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminSubmissions.value = _adminSubmissions.value.map {
                if (it.id == id) it.copy(status = newStatus, admin_notes = adminNotes) else it
            }
            try {
                val map = mutableMapOf<String, Any>("status" to newStatus)
                if (adminNotes != null) map["admin_notes"] = adminNotes
                api.updateUserSubmission("eq.$id", map)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun deleteSubmission(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminSubmissions.value = _adminSubmissions.value.filter { it.id != id }
            try {
                api.deleteUserSubmission("eq.$id")
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
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
            val author = "کاربر: ${submission.author_name}"
            createRealStory(
                title = title,
                content = content,
                author = author,
                source = "اعترافات و رازهای دریافتی کاربران",
                coverUrl = coverUrl,
                tags = "اعترافات, راز کاربران",
                status = "PUBLISHED"
            ) {
                updateSubmissionStatus(submission.id, "PUBLISHED", "منتشر شده به عنوان داستان واقعی") {
                    onComplete(true)
                }
            }
        }
    }

    fun createRealStory(title: String, content: String, author: String, source: String, coverUrl: String, tags: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
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
            _adminRealStories.value = listOf(newStory) + _adminRealStories.value
            if (status == "PUBLISHED") {
                _realStoriesList.value = listOf(newStory) + _realStoriesList.value
            }
            try {
                val map = mapOf(
                    "id" to newStory.id,
                    "title" to title,
                    "content" to content,
                    "author" to author,
                    "source" to source,
                    "cover_image_url" to coverUrl,
                    "tags" to tags,
                    "status" to status
                )
                api.createRealStory(map)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun updateRealStory(id: String, title: String, content: String, author: String, source: String, coverUrl: String, tags: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminRealStories.value = _adminRealStories.value.map {
                if (it.id == id) it.copy(
                    title = title,
                    content = content,
                    author = author,
                    source = source,
                    cover_image_url = coverUrl.ifBlank { null },
                    tags = tags,
                    status = status
                ) else it
            }
            if (status == "PUBLISHED") {
                _realStoriesList.value = _realStoriesList.value.map {
                    if (it.id == id) it.copy(
                        title = title,
                        content = content,
                        author = author,
                        source = source,
                        cover_image_url = coverUrl.ifBlank { null },
                        tags = tags,
                        status = status
                    ) else it
                }
            } else {
                _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
            }
            try {
                val map = mapOf(
                    "title" to title,
                    "content" to content,
                    "author" to author,
                    "source" to source,
                    "cover_image_url" to coverUrl,
                    "tags" to tags,
                    "status" to status
                )
                api.updateRealStory("eq.$id", map)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun updateRealStoryStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminRealStories.value = _adminRealStories.value.map {
                if (it.id == id) it.copy(status = newStatus) else it
            }
            if (newStatus == "PUBLISHED") {
                val story = _adminRealStories.value.find { it.id == id }
                if (story != null && _realStoriesList.value.none { it.id == id }) {
                    _realStoriesList.value = listOf(story) + _realStoriesList.value
                }
            } else {
                _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
            }
            try {
                api.updateRealStory("eq.$id", mapOf("status" to newStatus))
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun deleteRealStory(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminRealStories.value = _adminRealStories.value.filter { it.id != id }
            _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
            try {
                api.deleteRealStory("eq.$id")
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun createScenario(title: String, description: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val scen = WrongChoiceScenario(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                description = description,
                status = status,
                initial_scene_id = null,
                createdAt = null
            )
            _adminScenarios.value = listOf(scen) + _adminScenarios.value
            if (status == "PUBLISHED") {
                _scenariosList.value = listOf(scen) + _scenariosList.value
            }
            try {
                val map = mapOf(
                    "id" to scen.id,
                    "title" to title,
                    "description" to description,
                    "status" to status
                )
                api.createScenario(map)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun updateScenarioStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminScenarios.value = _adminScenarios.value.map {
                if (it.id == id) it.copy(status = newStatus) else it
            }
            if (newStatus == "PUBLISHED") {
                val s = _adminScenarios.value.find { it.id == id }
                if (s != null && _scenariosList.value.none { it.id == id }) {
                    _scenariosList.value = listOf(s) + _scenariosList.value
                }
            } else {
                _scenariosList.value = _scenariosList.value.filter { it.id != id }
            }
            try {
                api.updateScenario("eq.$id", mapOf("status" to newStatus))
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun deleteScenario(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminScenarios.value = _adminScenarios.value.filter { it.id != id }
            _scenariosList.value = _scenariosList.value.filter { it.id != id }
            try {
                api.deleteScenario("eq.$id")
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
            onComplete()
        }
    }

    fun testGeminiModel(key: String, model: String, onResult: (Boolean, String) -> Unit) {
        val apiKey = key.ifBlank { getEffectiveGeminiApiKey() }
        if (apiKey.isBlank()) {
            onResult(false, "کلید API یافت نشد. لطفاً کلید معتبر خود را وارد کنید.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val client = OkHttpClient.Builder()
                .connectTimeout(25, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(25, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", "یک پیام کوتاه تأیید اتصال به زبان فارسی بگو.")
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                val latency = System.currentTimeMillis() - startTime
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful && bodyStr.isNotEmpty()) {
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

            val targetModel = model ?: _selectedGeminiModel.value
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
                .url("https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful && bodyStr.isNotEmpty()) {
                    val responseObj = JSONObject(bodyStr)
                    val candidates = responseObj.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    val generatedText = parts.getJSONObject(0).getString("text")
                    withContext(Dispatchers.Main) {
                        onResult(generatedText)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult("کاتب ارواح در سکوت فرورفته است. خطا: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("خطای ماوراء الطبیعه: ${e.localizedMessage}")
                }
            }
        }
    }

    fun generateGrimFortunesWithAI(customPrompt: String? = null, onResult: (Boolean, String, Int) -> Unit) {
        val basePrompt = customPrompt?.ifBlank { null } ?: _grimFortunePrompt.value
        generateAILore(basePrompt) { responseText ->
            if (responseText.startsWith("خطا")) {
                onResult(false, responseText, 0)
                return@generateAILore
            }

            var generatedCount = 0
            // Parse response for ===1=== to ===12=== or 1. to 12.
            for (monthIdx in 1..12) {
                val marker = "===$monthIdx==="
                val nextMarker = "===${monthIdx + 1}==="
                
                var block = ""
                if (responseText.contains(marker)) {
                    val start = responseText.indexOf(marker) + marker.length
                    val end = if (monthIdx < 12 && responseText.contains(nextMarker)) {
                        responseText.indexOf(nextMarker, start)
                    } else {
                        responseText.length
                    }
                    if (start in 0 until end) {
                        block = responseText.substring(start, end).trim()
                    }
                }

                if (block.isBlank()) {
                    // Alternative parsing pattern: e.g. "ماه 1" or "[1]" or numbered list
                    val altMarker = "ماه $monthIdx"
                    val nextAltMarker = "ماه ${monthIdx + 1}"
                    if (responseText.contains(altMarker)) {
                        val start = responseText.indexOf(altMarker) + altMarker.length
                        val end = if (monthIdx < 12 && responseText.contains(nextAltMarker)) {
                            responseText.indexOf(nextAltMarker, start)
                        } else {
                            responseText.length
                        }
                        if (start in 0 until end) {
                            block = responseText.substring(start, end).trim()
                        }
                    }
                }

                if (block.isNotBlank()) {
                    val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    var title = "طالع شوم ماه ${PERSIAN_MONTHS[monthIdx - 1]}"
                    var poem: String? = null
                    var fortuneText = block
                    var doomLevel: String? = "شوم"

                    for (line in lines) {
                        when {
                            line.contains("عنوان:") || line.startsWith("عنوان") -> {
                                title = line.replace("عنوان:", "").replace("عنوان", "").replace("#", "").replace("*", "").trim()
                            }
                            line.contains("شعر:") || line.startsWith("شعر") -> {
                                poem = line.replace("شعر:", "").replace("شعر", "").replace("#", "").replace("*", "").trim()
                            }
                            line.contains("طالع:") || line.contains("تفسیر:") || line.startsWith("طالع") -> {
                                fortuneText = line.replace("طالع:", "").replace("طالع", "").replace("تفسیر:", "").replace("#", "").replace("*", "").trim()
                            }
                            line.contains("درجه:") || line.contains("سطح:") -> {
                                doomLevel = line.replace("درجه:", "").replace("سطح:", "").replace("#", "").replace("*", "").trim()
                            }
                        }
                    }

                    saveGrimFortune(
                        monthIndex = monthIdx,
                        title = title.ifBlank { "طالع ماه ${PERSIAN_MONTHS[monthIdx - 1]}" },
                        poem = poem,
                        fortuneText = fortuneText.ifBlank { block },
                        doomLevel = doomLevel,
                        status = "PUBLISHED"
                    ) {}
                    generatedCount++
                }
            }

            if (generatedCount > 0) {
                onResult(true, "تعداد $generatedCount طالع شوم برای ماه‌های سال با موفقیت با یک درخواست AI تولید و ذخیره شدند.", generatedCount)
            } else {
                // If structured delimiters weren't strictly matched, save whole text into month 1 or distribute
                saveGrimFortune(
                    monthIndex = 1,
                    title = "طالع شوم فروردین",
                    poem = "ز غوغای جهان فارغ نشین در خلوت ظلمات",
                    fortuneText = responseText,
                    doomLevel = "بسیار شوم",
                    status = "PUBLISHED"
                ) {}
                onResult(true, "طالع هوش مصنوعی دریافت و ذخیره شد.", 1)
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

    fun generateScenariosWithAI(count: Int, customPrompt: String? = null, onResult: (Boolean, String) -> Unit) {
        val basePrompt = customPrompt?.ifBlank { null } ?: _scenarioPrompt.value
        val fullPrompt = "$basePrompt\nلطفاً تعداد $count سناریوی جداگانه تولید کن. برای هر سناریو، دقیقاً با پیشوند '---' سناریوها را از هم جدا کن. در خط اول هر سناریو عبارت 'عنوان: [نام سناریو]' و در خطوط بعدی توصیف کامل و گزینه‌ها را قرار بده."

        generateAILore(fullPrompt) { text ->
            if (text.startsWith("خطا")) {
                onResult(false, text)
                return@generateAILore
            }
            val blocks = text.split("---").map { it.trim() }.filter { it.length > 20 }
            if (blocks.isNotEmpty()) {
                blocks.take(count).forEach { block ->
                    val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    var title = "سناریوی عمارت وحشت"
                    var desc = block
                    if (lines.isNotEmpty()) {
                        val firstLine = lines[0]
                        title = firstLine.replace("عنوان:", "").replace("عنوان", "").replace("#", "").replace("*", "").trim()
                        desc = lines.drop(1).joinToString("\n")
                    }
                    createScenario(title, desc.ifBlank { block }, "PUBLISHED") {}
                }
                onResult(true, "$count سناریوی جدید با موفقیت توسط هوش مصنوعی خلق و به پایگاه اضافه شد.")
            } else {
                // Single block
                val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
                val title = if (lines.isNotEmpty()) lines[0].replace("عنوان:", "").replace("#", "").replace("*", "").trim() else "سناریوی عمارت وحشت"
                val desc = if (lines.size > 1) lines.drop(1).joinToString("\n") else text
                createScenario(title, desc, "PUBLISHED") {}
                onResult(true, "۱ سناریوی جدید با موفقیت اضافه شد.")
            }
        }
    }
}
