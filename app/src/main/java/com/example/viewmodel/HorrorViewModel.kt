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
    private val prefs = application.getSharedPreferences("horror_admin_prefs", android.content.Context.MODE_PRIVATE)

    private val api: SupabaseApi
        get() = SupabaseClientProvider.api

    companion object {
        const val PREF_GEMINI_KEY = "pref_gemini_api_key"
        const val PREF_GEMINI_MODEL = "pref_gemini_model"
        const val PREF_GRIM_FORTUNE_PROMPT = "pref_grim_fortune_prompt"
        const val PREF_SCENARIO_PROMPT = "pref_scenario_prompt"
        const val PREF_SUPABASE_URL = "pref_supabase_url"
        const val PREF_SUPABASE_ANON_KEY = "pref_supabase_anon_key"

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
            "تو طراح بازی‌های تعاملی بقا و داستان‌های شاخه‌ای وحشت در عمارت گوتیک هستی. یک سناریوی تعاملی چندمرحله‌ای هیجان‌انگیز بنویس که انتخاب‌های بازیکن به سرنوشت‌ها و فرجام‌های کاملاً متفاوت ختم شود (فقط مرگ نباشد؛ شامل فرار با گنجینه کهن، کشف راز جاودانگی، اسارت در آینه، پیروزی بر ساحر، و سرنوشت‌های گوناگون).\n" +
            "فرمت پاسخ باید دقیقاً ساختاریافته به شکل زیر باشد:\n\n" +
            "عنوان: [نام سناریو]\n\n" +
            "---مرحله ۱---\n" +
            "روایت: [توصیف دلهره‌آور موقعیت و خطرات آغازین بازیکن]\n" +
            "گزینه ۱: [متن دکمه اول] -> [نتیجه یا ادامه]\n" +
            "گزینه ۲: [متن دکمه دوم] -> [نتیجه یا ادامه]\n" +
            "گزینه ۳: [متن دکمه سوم] -> [نتیجه یا ادامه]\n\n" +
            "---مرحله ۲---\n" +
            "روایت: [توصیف صحنه بعدی بر اساس پیشروی بازیکن]\n" +
            "گزینه ۱: [متن دکمه اول مرحله دو] -> [نتیجه یا ادامه]\n" +
            "گزینه ۲: [متن دکمه دوم مرحله دو] -> [نتیجه یا ادامه]\n\n" +
            "---مرحله ۳---\n" +
            "روایت: [مواجهه نهایی با طلسم و چند سرنوشت متفاوت]\n" +
            "گزینه ۱: [راه فرار پیروزمندانه] -> [بقا و رهایی از طلسم]\n" +
            "گزینه ۲: [انتخاب جسورانه یا فریبنده] -> [کشف گنجینه رازآلود و فرار]\n" +
            "گزینه ۳: [تسلیم یا اشتباه مرگبار] -> [اسارت ابدی روح یا مرگ]"
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
            loadAdminData()
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // 1. Fetch Grim Fortunes (Local first, then remote)
                val gfs = repository.getGrimFortunes(false)
                if (gfs.isNotEmpty()) {
                    _grimFortunesList.value = gfs
                } else {
                    val mockGfs = getMockGrimFortunes()
                    _grimFortunesList.value = mockGfs
                    // Cache mock data so Room has initial entities
                    mockGfs.forEach { repository.saveGrimFortune(it) }
                }

                // 2. Fetch Real Stories (Local first, then remote)
                val rs = repository.getRealStories(false)
                if (rs.isNotEmpty()) {
                    _realStoriesList.value = rs
                } else {
                    val mockRs = getMockRealStories()
                    _realStoriesList.value = mockRs
                    mockRs.forEach { repository.saveRealStory(it) }
                }

                // 3. Fetch Scenarios (Local first, then remote)
                val scens = repository.getScenarios(false)
                if (scens.isNotEmpty()) {
                    _scenariosList.value = scens
                } else {
                    val mockScens = getMockScenarios()
                    _scenariosList.value = mockScens
                    mockScens.forEach { repository.saveScenario(it) }
                }

                // 4. Fetch User Submissions/Confessions
                val subs = repository.getUserSubmissions(false)
                if (subs.isNotEmpty()) {
                    _userSubmissionsList.value = subs
                } else {
                    val mockSubs = getMockUserSubmissions()
                    _userSubmissionsList.value = mockSubs
                    mockSubs.forEach { repository.saveUserSubmission(it) }
                }

                // Background sync from Supabase if configured
                if (SupabaseClientProvider.isConfigured) {
                    launch(Dispatchers.IO) {
                        try {
                            val refreshedGf = repository.getGrimFortunes(true)
                            if (refreshedGf.isNotEmpty()) _grimFortunesList.value = refreshedGf

                            val refreshedRs = repository.getRealStories(true)
                            if (refreshedRs.isNotEmpty()) _realStoriesList.value = refreshedRs

                            val refreshedSc = repository.getScenarios(true)
                            if (refreshedSc.isNotEmpty()) _scenariosList.value = refreshedSc

                            val refreshedSubs = repository.getUserSubmissions(true)
                            if (refreshedSubs.isNotEmpty()) _userSubmissionsList.value = refreshedSubs
                        } catch (e: Exception) {
                            // Offline fallback silently retained
                        }
                    }
                }
            } catch (e: Exception) {
                if (_grimFortunesList.value.isEmpty()) _grimFortunesList.value = getMockGrimFortunes()
                if (_realStoriesList.value.isEmpty()) _realStoriesList.value = getMockRealStories()
                if (_scenariosList.value.isEmpty()) _scenariosList.value = getMockScenarios()
                if (_userSubmissionsList.value.isEmpty()) _userSubmissionsList.value = getMockUserSubmissions()
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
                id = "story-2",
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
                id = "story-3",
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
        return listOf(
            UserStorySubmission(
                id = "sub-1",
                title = "صدای قدم‌ها در اتاق زیرشیروانی",
                content = "هر شب دقیقاً رأس ساعت ۳:۱۵ بامداد، صدای کشیده شدن صندلی چوبی روی کف اتاق زیرشیروانی خانه ما شنیده می‌شود، در حالی که در آن اتاق سال‌هاست قفل است.",
                author_name = "مریم از تبریز",
                status = "PUBLISHED",
                admin_notes = "تطهیر شده و مورد تایید",
                createdAt = null,
                updatedAt = null
            )
        )
    }

    private fun getMockScenarios(): List<WrongChoiceScenario> {
        return listOf(
            WrongChoiceScenario(
                id = "scen-1",
                title = "گذرگاه دالان شرقی عمارت",
                description = "---مرحله ۱---\n" +
                        "روایت: شما در آستانه ورود به دالان شرقی عمارت هستید. دیوارهای دالان مه‌آلود و سرد است و از انتهای راهرو صدای برخورد زنجیر به گوش می‌رسد.\n" +
                        "گزینه ۱: مشعل دیواری را بردار و آرام پیشروی کن -> ورود به تالار آینه‌ها\n" +
                        "گزینه ۲: در تاریکی مطلق روی زمین سینه‌خیز شو -> ورود به سرداب مخفی\n" +
                        "گزینه ۳: از پله‌های چوبی سمت راست بالا برو -> رسیدن به اتاق ساعت کهن\n\n" +
                        "---مرحله ۲---\n" +
                        "روایت: در تالار آینه‌ها یا سرداب، کتیبه‌ای زرین پیدا می‌کنید که به زبان فارسی کهن راه‌های نجات را نگاشته است.\n" +
                        "گزینه ۱: ورد تطهیر را زمزمه کن و کلید را بردار -> رسیدن به دروازه رهایی\n" +
                        "گزینه ۲: صندقچه جواهرات طلسم‌شده را باز کن -> کشف گنجینه باستانی\n" +
                        "گزینه ۳: آینه مرکزی را با خنجر بشکن -> فرار از طریق پنجره مخفی\n\n" +
                        "---مرحله ۳---\n" +
                        "روایت: به نقطه اوج ماجرا رسیدید؛ سایه ساحر کهن پدیدار می‌شود و درگاه‌های سرنوشت گشوده می‌شوند.\n" +
                        "گزینه ۱: طلسم را با نور مشعل بسوزان -> رهایی پیروزمندانه از عمارت وحشت (بقا)\n" +
                        "گزینه ۲: گنجینه را بردار و از دریچه مخفی باغ بگریز -> فرار با ثروت طلسم‌شده (بقا و ثروت)\n" +
                        "گزینه ۳: با ساحر پیمان خونی ببند -> تبدیل شدن به شاگرد جاودان تاریکی (سرنوشت ساحری)",
                status = "PUBLISHED",
                initial_scene_id = "scene-1-1",
                createdAt = null
            ),
            WrongChoiceScenario(
                id = "scen-2",
                title = "کلاغ‌های معبد سوخته",
                description = "---مرحله ۱---\n" +
                        "روایت: برج ناقوس قدیمی لرزان معبد پیش روی شماست. صدها کلاغ سیاه روی سقف نشسته‌اند و ناقوس بی‌دلیل به صدا درمی‌آید.\n" +
                        "گزینه ۱: به داخل محراب پناه ببر -> رسیدن به کتاب مقدسات سیاه\n" +
                        "گزینه ۲: به سمت گورستان اطراف معبد بدو -> پناه گرفتن در مقبره سنگی\n" +
                        "گزینه ۳: طناب ناقوس را با شمشیر ببر -> خاموش کردن صدای شوم و باز شدن سرداب\n\n" +
                        "---مرحله ۲---\n" +
                        "روایت: درون محراب، کتابی با جلد چرم باستانی روی پایه‌ای سنگی قرار دارد که صفحات آن خودبه‌خود ورق می‌خورند.\n" +
                        "گزینه ۱: صفحه حاوی طلسم محافظت را با صدای رسا بخوان -> تشکیل هاله نورانی\n" +
                        "گزینه ۲: بخور معطر روی سنگدان را روشن کن -> آرام شدن ارواح معبد\n\n" +
                        "---مرحله ۳---\n" +
                        "روایت: دروازه‌های معبد در شرف فروریختن است و مه غلیظی فضای محراب را در بر گرفته است.\n" +
                        "گزینه ۱: کتاب را در آغوش بگیر و به سوی روشنایی بدو -> رهایی به همراه اسرار کهن (بقا)\n" +
                        "گزینه ۲: از تونل زیرزمینی مقبره خارج شو -> رسیدن به جنگل آرامش (فرار موفق)\n" +
                        "گزینه ۳: جام نوشداروی ارواح را بنوش -> کسب قدرت دیدن دنیای ماوراء (بقای ماورایی)",
                status = "PUBLISHED",
                initial_scene_id = "scene-2-1",
                createdAt = null
            )
        )
    }

    fun rateStory(storyId: String, userRating: Float) {
        viewModelScope.launch {
            val currentStories = _realStoriesList.value
            val target = currentStories.find { it.id == storyId } ?: return@launch
            val updatedCount = target.rating_count + 1
            val calculatedRating = ((target.rating * target.rating_count) + userRating) / updatedCount
            val updatedStory = target.copy(
                rating = String.format(java.util.Locale.US, "%.1f", calculatedRating).toFloat(),
                rating_count = updatedCount
            )

            _realStoriesList.value = _realStoriesList.value.map { if (it.id == storyId) updatedStory else it }
            _adminRealStories.value = _adminRealStories.value.map { if (it.id == storyId) updatedStory else it }
            repository.saveRealStory(updatedStory)
        }
    }

    fun incrementStoryViews(storyId: String) {
        viewModelScope.launch {
            val currentStories = _realStoriesList.value
            val target = currentStories.find { it.id == storyId } ?: return@launch
            val updatedStory = target.copy(view_count = target.view_count + 1)

            _realStoriesList.value = _realStoriesList.value.map { if (it.id == storyId) updatedStory else it }
            _adminRealStories.value = _adminRealStories.value.map { if (it.id == storyId) updatedStory else it }
            repository.saveRealStory(updatedStory)
        }
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
                val resp = api.getGrimFortunes(select = "id", status = null)
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

                val localScens = repository.getAllScenariosAdmin()
                if (localScens.isNotEmpty()) _adminScenarios.value = localScens

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

                    val scenResp = api.getScenarios()
                    if (scenResp.isSuccessful && scenResp.body() != null) {
                        _adminScenarios.value = scenResp.body()!!
                    }

                    val promptResp = api.getAiPrompts()
                    if (promptResp.isSuccessful && promptResp.body() != null) {
                        _aiPrompts.value = promptResp.body()!!
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
        // Master offline credentials check
        if (email.trim().lowercase() == "admin@gothic.com" && pass == "admin123") {
            _currentUserEmail.value = email
            _currentUserId.value = "master-admin"
            _currentUserRole.value = "ADMIN"
            setAppMode(AppMode.ADMIN_PANEL)
            onResult(true, null)
            return
        }

        if (!SupabaseClientProvider.isConfigured) {
            onResult(false, "اتصال Supabase تنظیم نشده است. می‌توانید با ایمیل admin@gothic.com و رمز admin123 وارد شوید یا در تنظیمات آدرس Supabase را وارد کنید.")
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

    fun submitUserStory(title: String, content: String, author: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
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
            // Immediately persist in Room local DB
            repository.saveUserSubmission(newSub)

            // Update UI State
            _adminSubmissions.value = listOf(newSub) + _adminSubmissions.value

            // Attempt remote sync
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        val map = mapOf(
                            "id" to newSub.id,
                            "title" to newSub.title,
                            "content" to newSub.content,
                            "author_name" to newSub.author_name,
                            "status" to "PENDING"
                        )
                        api.submitUserStory(map)
                    }
                } catch (e: Exception) {
                    // Stored in Room
                }
            }

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

            // Persist to Room local DB immediately
            repository.saveGrimFortune(newGf)

            _adminGrimFortunes.value = _adminGrimFortunes.value.filter { it.month_index != monthIndex } + newGf
            if (status == "PUBLISHED") {
                _grimFortunesList.value = _grimFortunesList.value.filter { it.month_index != monthIndex } + newGf
            }

            // Sync to Supabase in background
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
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
                    }
                } catch (e: Exception) {
                    // Saved locally
                }
            }

            onComplete()
        }
    }

    fun updateGrimFortuneStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminGrimFortunes.value = _adminGrimFortunes.value.map {
                if (it.id == id) it.copy(status = newStatus) else it
            }
            val item = _adminGrimFortunes.value.find { it.id == id }
            if (item != null) {
                repository.saveGrimFortune(item)
            }
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        api.updateGrimFortune("eq.$id", mapOf("status" to newStatus))
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun deleteGrimFortune(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminGrimFortunes.value = _adminGrimFortunes.value.filter { it.id != id }
            _grimFortunesList.value = _grimFortunesList.value.filter { it.id != id }
            repository.deleteGrimFortune(id)
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        api.deleteGrimFortune("eq.$id")
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun updateSubmissionStatus(id: String, newStatus: String, adminNotes: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminSubmissions.value = _adminSubmissions.value.map {
                if (it.id == id) it.copy(status = newStatus, admin_notes = adminNotes) else it
            }
            val sub = _adminSubmissions.value.find { it.id == id }
            if (sub != null) {
                repository.saveUserSubmission(sub)
                if (newStatus == "PUBLISHED") {
                    _userSubmissionsList.value = listOf(sub) + _userSubmissionsList.value.filter { it.id != id }
                } else {
                    _userSubmissionsList.value = _userSubmissionsList.value.filter { it.id != id }
                }
            }
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        val map = mutableMapOf<String, Any>("status" to newStatus)
                        if (adminNotes != null) map["admin_notes"] = adminNotes
                        api.updateUserSubmission("eq.$id", map)
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun deleteSubmission(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminSubmissions.value = _adminSubmissions.value.filter { it.id != id }
            _userSubmissionsList.value = _userSubmissionsList.value.filter { it.id != id }
            repository.deleteUserSubmission(id)
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        api.deleteUserSubmission("eq.$id")
                    }
                } catch (e: Exception) { }
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
            // Persist to Room local DB immediately
            repository.saveRealStory(newStory)

            _adminRealStories.value = listOf(newStory) + _adminRealStories.value
            if (status == "PUBLISHED") {
                _realStoriesList.value = listOf(newStory) + _realStoriesList.value
            }

            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
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
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun updateRealStory(id: String, title: String, content: String, author: String, source: String, coverUrl: String, tags: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val updated = RealStory(
                id = id,
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
            repository.saveRealStory(updated)

            _adminRealStories.value = _adminRealStories.value.map { if (it.id == id) updated else it }
            if (status == "PUBLISHED") {
                _realStoriesList.value = _realStoriesList.value.filter { it.id != id } + listOf(updated)
            } else {
                _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
            }

            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
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
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun updateRealStoryStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminRealStories.value = _adminRealStories.value.map {
                if (it.id == id) it.copy(status = newStatus) else it
            }
            val story = _adminRealStories.value.find { it.id == id }
            if (story != null) {
                repository.saveRealStory(story)
                if (newStatus == "PUBLISHED") {
                    if (_realStoriesList.value.none { it.id == id }) {
                        _realStoriesList.value = listOf(story) + _realStoriesList.value
                    }
                } else {
                    _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
                }
            }
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        api.updateRealStory("eq.$id", mapOf("status" to newStatus))
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun deleteRealStory(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminRealStories.value = _adminRealStories.value.filter { it.id != id }
            _realStoriesList.value = _realStoriesList.value.filter { it.id != id }
            repository.deleteRealStory(id)
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        api.deleteRealStory("eq.$id")
                    }
                } catch (e: Exception) { }
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
            repository.saveScenario(scen)

            _adminScenarios.value = listOf(scen) + _adminScenarios.value
            if (status == "PUBLISHED") {
                _scenariosList.value = listOf(scen) + _scenariosList.value
            }

            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        val map = mapOf(
                            "id" to scen.id,
                            "title" to title,
                            "description" to description,
                            "status" to status
                        )
                        api.createScenario(map)
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun updateScenarioStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminScenarios.value = _adminScenarios.value.map {
                if (it.id == id) it.copy(status = newStatus) else it
            }
            val s = _adminScenarios.value.find { it.id == id }
            if (s != null) {
                repository.saveScenario(s)
                if (newStatus == "PUBLISHED") {
                    if (_scenariosList.value.none { it.id == id }) {
                        _scenariosList.value = listOf(s) + _scenariosList.value
                    }
                } else {
                    _scenariosList.value = _scenariosList.value.filter { it.id != id }
                }
            }
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        api.updateScenario("eq.$id", mapOf("status" to newStatus))
                    }
                } catch (e: Exception) { }
            }
            onComplete()
        }
    }

    fun deleteScenario(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _adminScenarios.value = _adminScenarios.value.filter { it.id != id }
            _scenariosList.value = _scenariosList.value.filter { it.id != id }
            repository.deleteScenario(id)
            launch(Dispatchers.IO) {
                try {
                    if (SupabaseClientProvider.isConfigured) {
                        api.deleteScenario("eq.$id")
                    }
                } catch (e: Exception) { }
            }
            onComplete()
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

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
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
        generateAILore(basePrompt) { responseText ->
            if (responseText.startsWith("خطا")) {
                onResult(false, responseText, 0)
                return@generateAILore
            }

            var parsedCount = 0
            val pattern = Regex("===(\\d+)===\\s*\\n?([\\s\\S]*?)(?====\\d+===|$)")
            val matches = pattern.findAll(responseText).toList()

            if (matches.isNotEmpty()) {
                for (match in matches) {
                    val monthNumStr = match.groupValues[1]
                    val content = match.groupValues[2].trim()
                    val monthIndex = monthNumStr.toIntOrNull() ?: continue
                    if (monthIndex in 1..12) {
                        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        var title = "طالع ماه ${PERSIAN_MONTHS[monthIndex - 1]}"
                        var poem: String? = null
                        var fortuneText = content
                        var doomLevel: String? = "شوم"

                        for (line in lines) {
                            when {
                                line.contains("عنوان:") -> title = line.replace("عنوان:", "").replace("#", "").replace("*", "").trim()
                                line.contains("شعر:") -> poem = line.replace("شعر:", "").replace("#", "").replace("*", "").trim()
                                line.contains("طالع:") -> fortuneText = line.replace("طالع:", "").replace("#", "").replace("*", "").trim()
                                line.contains("درجه:") -> doomLevel = line.replace("درجه:", "").replace("#", "").replace("*", "").trim()
                            }
                        }

                        saveGrimFortune(
                            monthIndex = monthIndex,
                            title = title,
                            poem = poem,
                            fortuneText = fortuneText,
                            doomLevel = doomLevel,
                            status = "PUBLISHED"
                        ) {}
                        parsedCount++
                    }
                }
                onResult(true, "تعداد $parsedCount ماه با موفقیت تولید و در پایگاه داده ذخیره شد.", parsedCount)
            } else {
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
        val fullPrompt = "$basePrompt\n\nلطفاً تعداد $count سناریوی جداگانه با مراحل کامل تولید کن. برای هر سناریو، دقیقاً با پیشوند '###سناریو###' سناریوها را از هم جدا کن."

        generateAILore(fullPrompt) { text ->
            if (text.startsWith("خطا")) {
                onResult(false, text)
                return@generateAILore
            }

            val blocks = text.split("###سناریو###")
                .map { it.trim() }
                .filter { it.length > 20 }

            if (blocks.isNotEmpty()) {
                blocks.take(count).forEachIndexed { idx, block ->
                    val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    var title = "سناریوی تعاملی عمارت وحشت ${idx + 1}"
                    if (lines.isNotEmpty()) {
                        val firstLine = lines[0]
                        if (firstLine.contains("عنوان:")) {
                            title = firstLine.replace("عنوان:", "").replace("#", "").replace("*", "").trim()
                        }
                    }
                    createScenario(title, block, "PUBLISHED") {}
                }
                onResult(true, "$count سناریوی چندمرحله‌ای با موفقیت توسط هوش مصنوعی خلق و در پایگاه داده ذخیره شد.")
            } else {
                val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
                val title = if (lines.isNotEmpty() && lines[0].contains("عنوان:")) {
                    lines[0].replace("عنوان:", "").replace("#", "").replace("*", "").trim()
                } else "سناریوی تعاملی عمارت وحشت"
                createScenario(title, text, "PUBLISHED") {}
                onResult(true, "۱ سناریوی چندمرحله‌ای با موفقیت در پایگاه داده ذخیره شد.")
            }
        }
    }
}
