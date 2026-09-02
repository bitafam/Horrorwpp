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

enum class AppMode {
    USER, ADMIN_LOGIN, ADMIN_PANEL, NOTIFICATIONS
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

    // Notification States
    private val _notificationsList = MutableStateFlow<List<CachedAppNotification>>(emptyList())
    val notificationsList: StateFlow<List<CachedAppNotification>> = _notificationsList.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            val list = repository.getAllNotifications()
            _notificationsList.value = list
            val context = getApplication<Application>()
            for (notification in list) {
                if (!com.example.util.NotificationHelper.isNotificationShown(context, notification.id)) {
                    com.example.util.NotificationHelper.showSystemNotification(
                        context = context,
                        notificationId = notification.id.hashCode(),
                        title = notification.title,
                        message = notification.message,
                        imageUrl = notification.imageUrl
                    )
                    com.example.util.NotificationHelper.markNotificationAsShown(context, notification.id)
                }
            }
        }
    }

    fun scheduleNotificationSync() {
        try {
            val context = getApplication<Application>()
            com.example.util.NotificationHelper.createNotificationChannel(context)
            
            // Schedule AlarmManager repeating fallback
            com.example.util.NotificationSyncReceiver.scheduleNextAlarm(context)

            // Schedule WorkManager
            val workManager = androidx.work.WorkManager.getInstance(context)

            val oneTimeRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.util.NotificationWorker>().build()
            workManager.enqueueUniqueWork(
                "ImmediateNotificationSync",
                androidx.work.ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )

            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val periodicRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.util.NotificationWorker>(
                15, java.util.concurrent.TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            workManager.enqueueUniquePeriodicWork(
                "PeriodicNotificationSync",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("HorrorViewModel", "Error scheduling WorkManager: ${e.message}")
        }
    }

    fun upsertNotification(notification: CachedAppNotification) {
        viewModelScope.launch {
            repository.upsertNotification(notification)
            val context = getApplication<Application>()
            com.example.util.NotificationHelper.showSystemNotification(
                context = context,
                notificationId = notification.id.hashCode(),
                title = notification.title,
                message = notification.message,
                imageUrl = notification.imageUrl
            )
            com.example.util.NotificationHelper.markNotificationAsShown(context, notification.id)
            loadNotifications()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            loadNotifications()
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

    private var realtimeNotificationJob: kotlinx.coroutines.Job? = null

    fun startRealtimeNotificationObserver() {
        realtimeNotificationJob?.cancel()
        realtimeNotificationJob = viewModelScope.launch {
            while (true) {
                if (NetworkUtils.isOnline(getApplication())) {
                    try {
                        val list = repository.getAllNotifications()
                        _notificationsList.value = list
                        val context = getApplication<Application>()
                        for (notification in list) {
                            if (!com.example.util.NotificationHelper.isNotificationShown(context, notification.id)) {
                                com.example.util.NotificationHelper.showSystemNotification(
                                    context = context,
                                    notificationId = notification.id.hashCode(),
                                    title = notification.title,
                                    message = notification.message,
                                    imageUrl = notification.imageUrl
                                )
                                com.example.util.NotificationHelper.markNotificationAsShown(context, notification.id)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HorrorViewModel", "Realtime notification poll error: ${e.message}")
                    }
                }
                kotlinx.coroutines.delay(8_000) // Poll every 8 seconds while app is active
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
        scheduleNotificationSync()
        loadNotifications()
        startRealtimeNotificationObserver()
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
        if (mode == AppMode.ADMIN_PANEL) {
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
                val scens = repository.getScenarios(false)
                if (scens.isNotEmpty()) _scenariosList.value = scens
                val subs = repository.getUserSubmissions(false)
                if (subs.isNotEmpty()) _userSubmissionsList.value = subs
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

                var scens = repository.getScenarios(true)
                if (scens.isEmpty() && SupabaseClientProvider.isConfigured) {
                    val mockScens = getMockScenarios()
                    mockScens.forEach { scen ->
                        try { repository.saveScenario(scen) } catch (_: Exception) {}
                    }
                    scens = repository.getScenarios(true)
                }
                _scenariosList.value = scens

                var subs = repository.getUserSubmissions(true)
                _userSubmissionsList.value = subs

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

    private fun getMockScenarios(): List<WrongChoiceScenario> {
        return listOf(
            WrongChoiceScenario(
                id = java.util.UUID.nameUUIDFromBytes("scen-1".toByteArray()).toString(),
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
                initial_scene_id = null,
                createdAt = null
            ),
            WrongChoiceScenario(
                id = java.util.UUID.nameUUIDFromBytes("scen-2".toByteArray()).toString(),
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
                initial_scene_id = null,
                createdAt = null
            )
        )
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
    }

    fun setSelectedGeminiModel(model: String) {
        _selectedGeminiModel.value = model
        prefs.edit().putString(PREF_GEMINI_MODEL, model).apply()
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
                            val updated = sub.copy(status = "DRAFT")
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

    fun createScenario(title: String, description: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val scen = WrongChoiceScenario(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    status = status,
                    initial_scene_id = null,
                    createdAt = null
                )
                val saved = repository.saveScenario(scen)
                _adminScenarios.value = listOf(saved) + _adminScenarios.value
                if (status == "PUBLISHED") {
                    _scenariosList.value = listOf(saved) + _scenariosList.value
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در ساخت سناریو: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateScenarioStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val s = _adminScenarios.value.find { it.id == id }
                if (s != null) {
                    val updated = s.copy(status = newStatus)
                    val saved = repository.saveScenario(updated)
                    _adminScenarios.value = _adminScenarios.value.map { if (it.id == id) saved else it }
                    if (newStatus == "PUBLISHED") {
                        if (_scenariosList.value.none { it.id == id }) {
                            _scenariosList.value = listOf(saved) + _scenariosList.value
                        }
                    } else {
                        _scenariosList.value = _scenariosList.value.filter { it.id != id }
                    }
                }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در تغییر وضعیت سناریو: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteScenario(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteScenario(id)
                _adminScenarios.value = _adminScenarios.value.filter { it.id != id }
                _scenariosList.value = _scenariosList.value.filter { it.id != id }
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در حذف سناریو: ${e.localizedMessage}"
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

    fun generateScenariosWithAI(count: Int, customPrompt: String? = null, onResult: (Boolean, String) -> Unit) {
        val basePrompt = customPrompt?.ifBlank { null } ?: _scenarioPrompt.value
        val fullPrompt = "$basePrompt\n\nلطفاً تعداد $count سناریوی جداگانه با مراحل کامل تولید کن. برای هر سناریو، دقیقاً با پیشوند '###سناریو###' سناریوها را از هم جدا کن."

        generateAILore(fullPrompt) { text ->
            if (text.startsWith("خطا")) {
                onResult(false, text)
                return@generateAILore
            }

            viewModelScope.launch {
                _loading.value = true
                try {
                    val blocks = text.split("###سناریو###")
                        .map { it.trim() }
                        .filter { it.length > 20 }

                    val savedList = mutableListOf<WrongChoiceScenario>()
                    if (blocks.isNotEmpty()) {
                        for (block in blocks.take(count)) {
                            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                            var title = "سناریوی تعاملی عمارت وحشت"
                            if (lines.isNotEmpty()) {
                                val firstLine = lines[0]
                                if (firstLine.contains("عنوان:")) {
                                    title = firstLine.replace("عنوان:", "").replace("#", "").replace("*", "").trim()
                                }
                            }
                            val scen = WrongChoiceScenario(
                                id = java.util.UUID.randomUUID().toString(),
                                title = title,
                                description = block,
                                status = "PUBLISHED",
                                initial_scene_id = null,
                                createdAt = null
                            )
                            val saved = repository.saveScenario(scen)
                            savedList.add(saved)
                        }
                        
                        _adminScenarios.value = savedList + _adminScenarios.value
                        _scenariosList.value = savedList + _scenariosList.value
                        onResult(true, "$count سناریوی چندمرحله‌ای با موفقیت توسط هوش مصنوعی خلق و در پایگاه داده ذخیره شد.")
                    } else {
                        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        val title = if (lines.isNotEmpty() && lines[0].contains("عنوان:")) {
                            lines[0].replace("عنوان:", "").replace("#", "").replace("*", "").trim()
                        } else "سناریوی تعاملی عمارت وحشت"
                        val scen = WrongChoiceScenario(
                            id = java.util.UUID.randomUUID().toString(),
                            title = title,
                            description = text,
                            status = "PUBLISHED",
                            initial_scene_id = null,
                            createdAt = null
                        )
                        val saved = repository.saveScenario(scen)
                        _adminScenarios.value = listOf(saved) + _adminScenarios.value
                        _scenariosList.value = listOf(saved) + _scenariosList.value
                        onResult(true, "۱ سناریوی چندمرحله‌ای با موفقیت در پایگاه داده ذخیره شد.")
                    }
                } catch (e: java.lang.Exception) {
                    _errorMessage.value = "خطا در ذخیره‌سازی سناریوها: ${e.localizedMessage}"
                    onResult(false, "خطا در ذخیره‌سازی سناریوها: ${e.localizedMessage}")
                } finally {
                    _loading.value = false
                }
            }
        }
    }
}
