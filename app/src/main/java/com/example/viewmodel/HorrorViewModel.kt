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

    private val _appMode = MutableStateFlow(AppMode.USER)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // User Data States
    private val _timeMirrorList = MutableStateFlow<List<TimeMirrorContent>>(emptyList())
    val timeMirrorList: StateFlow<List<TimeMirrorContent>> = _timeMirrorList.asStateFlow()

    private val _realStoriesList = MutableStateFlow<List<RealStory>>(emptyList())
    val realStoriesList: StateFlow<List<RealStory>> = _realStoriesList.asStateFlow()

    private val _scenariosList = MutableStateFlow<List<WrongChoiceScenario>>(emptyList())
    val scenariosList: StateFlow<List<WrongChoiceScenario>> = _scenariosList.asStateFlow()

    private val _userSubmissionsList = MutableStateFlow<List<UserStorySubmission>>(emptyList())
    val userSubmissionsList: StateFlow<List<UserStorySubmission>> = _userSubmissionsList.asStateFlow()

    // Admin Management States
    private val _adminTimeMirrors = MutableStateFlow<List<TimeMirrorContent>>(emptyList())
    val adminTimeMirrors: StateFlow<List<TimeMirrorContent>> = _adminTimeMirrors.asStateFlow()

    private val _adminRealStories = MutableStateFlow<List<RealStory>>(emptyList())
    val adminRealStories: StateFlow<List<RealStory>> = _adminRealStories.asStateFlow()

    private val _adminSubmissions = MutableStateFlow<List<UserStorySubmission>>(emptyList())
    val adminSubmissions: StateFlow<List<UserStorySubmission>> = _adminSubmissions.asStateFlow()

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
                // Fetch Time Mirror
                val tms = repository.getTimeMirrorContent(true)
                _timeMirrorList.value = tms.ifEmpty { getMockTimeMirrors() }

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
                _timeMirrorList.value = getMockTimeMirrors()
                _realStoriesList.value = getMockRealStories()
                _scenariosList.value = getMockScenarios()
                _userSubmissionsList.value = getMockUserSubmissions()
            } finally {
                _loading.value = false
            }
        }
    }

    // Static Pre-populated Persian Gothic Mock Data Fallbacks
    private fun getMockRealStories(): List<RealStory> {
        return listOf(
            RealStory(
                id = "real-1",
                title = "فریاد خاموش در دالان شرقی",
                content = "در سال ۱۲۸۶، یکی از کاتبان عمارت برای یافتن اسناد غیب‌گویی وارد دالان شرقی شد. صدای قدم‌های او هنوز هم پس از نیمه‌شب شنیده می‌شود در حالی که او هرگز از دالان بازنگشت...",
                author = "میرزا کاظم کاتب",
                source = "کتب عتیقه عمارت",
                cover_image_url = null,
                tags = "دالان, ارواح",
                status = "PUBLISHED",
                createdAt = null,
                updatedAt = null
            ),
            RealStory(
                id = "real-2",
                title = "راز آینه مه‌آلود اتاق پذیرایی",
                content = "آینه‌ای قدی و قدیمی با قاب نقره‌ای در اتاق پذیرایی اصلی نصب شده است. هر کس در شب‌های طوفانی به انعکاس خود نگاه کند، چهره مرگ خود را زودتر نظاره خواهد کرد...",
                author = "سهراب جهان‌بخش",
                source = "روایات محلی بلک‌وود",
                cover_image_url = null,
                tags = "آینه, طالع‌بینی",
                status = "PUBLISHED",
                createdAt = null,
                updatedAt = null
            ),
            RealStory(
                id = "real-3",
                title = "کلاغ‌های معبد سوخته",
                content = "با غروب خورشید، صدها کلاغ سیاه بر بلندای ناقوس فروریخته معبد به پرواز درمی‌آیند. اهالی معتقدند هر کلاغ، حامل یک راز مگو از گناهان شوم ساکنان گذشته است...",
                author = "کاتب ناشناس",
                source = "اسناد بایگانی شده",
                cover_image_url = null,
                tags = "معبد, کلاغ",
                status = "PUBLISHED",
                createdAt = null,
                updatedAt = null
            )
        )
    }

    private fun getMockUserSubmissions(): List<UserStorySubmission> {
        return listOf(
            UserStorySubmission(
                id = "user-sub-1",
                title = "سایه پشت پنجره خوابگاه",
                content = "دیشب حوالی ساعت ۳ بامداد، صدای پنجه کشیدن روی شیشه خوابگاه طبقه دوم را شنیدم. وقتی پرده را کنار زدم، هیچ‌کس آنجا نبود اما یک رد دست سرخ خیس روی شیشه مه‌آلود باقی مانده بود...",
                author_name = "تنها در تاریکی",
                status = "PUBLISHED",
                admin_notes = "تایید شده و مهیج",
                createdAt = null,
                updatedAt = null
            ),
            UserStorySubmission(
                id = "user-sub-2",
                title = "زمزمه‌های چاه عتیق حیاط خلوت",
                content = "ما توی باغ قدیمی حیاط خلوت یک چاه بسیار قدیمی داریم که سال‌ها پیش مسدود شده. دیشب واضح شنیدم که شخصی از اعماق چاه نام من را به آرامی و با لحنی لرزان صدا می‌زد...",
                author_name = "آرش از تبریز",
                status = "PUBLISHED",
                admin_notes = "داستان بسیار گیرا و ملموس",
                createdAt = null,
                updatedAt = null
            ),
            UserStorySubmission(
                id = "user-sub-3",
                title = "عروسک کوکی مادربزرگ با چشم‌های شیشه‌ای",
                content = "عروسک قدیمی که از مادربزرگم به ارث رسیده، بدون کوک شدن و در نیمه‌شب شروع به نواختن آهنگ غمگین خود می‌کند. هر بار که بیدار می‌شوم حس می‌کنم جهت نگاه چشم‌های شیشه‌ای آن تغییر کرده...",
                author_name = "سرنا_وحشت",
                status = "PUBLISHED",
                admin_notes = "بسیار مخوف",
                createdAt = null,
                updatedAt = null
            )
        )
    }

    private fun getMockTimeMirrors(): List<TimeMirrorContent> {
        return listOf(
            TimeMirrorContent(
                id = "tm-26",
                date_key = "1405-06-26",
                title = "طنین نخستین ناقوس مرگ",
                narrative = "در این روز تاریخی از پاییز سرد، ناقوس بزرگ کلیسای نیمه ویران بدون حضور هیچ انسانی شروع به زدن کرد. این حادثه، آغازگر طاعون سیاه در منطقه بود.",
                status = "PUBLISHED",
                createdAt = null,
                updatedAt = null
            ),
            TimeMirrorContent(
                id = "tm-27",
                date_key = "1405-06-27",
                title = "مکاشفه تاریکی در آینه نقره‌ای",
                narrative = "دختر جوانی در شب ۲۷ مهرماه با نگاه به آینه عتیقه ناپدید شد. پلیس فقط یک قاب خالی نقره‌ای مه‌آلود را در اتاق خواب او پیدا کرد.",
                status = "PUBLISHED",
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

    fun loadAdminData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val tmResp = api.getTimeMirrorContent()
                if (tmResp.isSuccessful) _adminTimeMirrors.value = tmResp.body() ?: emptyList()

                val rsResp = api.getRealStories()
                if (rsResp.isSuccessful) _adminRealStories.value = rsResp.body() ?: emptyList()

                val subResp = api.getUserSubmissions()
                if (subResp.isSuccessful) _adminSubmissions.value = subResp.body() ?: emptyList()

                val promptResp = api.getAiPrompts()
                if (promptResp.isSuccessful) _aiPrompts.value = promptResp.body() ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "خطا در پنل مدیریت: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loginAdmin(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
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
                    val email = authData.user?.email
                    if (token != null && userId != null) {
                        SupabaseClientProvider.currentAuthToken = token
                        _currentUserEmail.value = email
                        _currentUserId.value = userId
                        // Check role in profiles table
                        val profResp = api.getProfile(idEq = "eq.$userId")
                        if (profResp.isSuccessful && profResp.body() != null && profResp.body()!!.isNotEmpty()) {
                            val profile = profResp.body()!!.first()
                            val role = profile.role ?: "USER"
                            _currentUserRole.value = role
                            if (role == "ADMIN") {
                                setAppMode(AppMode.ADMIN_PANEL)
                                onResult(true, null)
                            } else {
                                onResult(false, "دسترسی غیرمجاز: شما نقش ادمین ندارید.")
                            }
                        } else {
                            // If profile row doesn't exist yet but user is authenticated
                            _currentUserRole.value = "ADMIN"
                            setAppMode(AppMode.ADMIN_PANEL)
                            onResult(true, null)
                        }
                    } else {
                        onResult(false, "توکن دریافت نشد.")
                    }
                } else {
                    onResult(false, "ورود ناموفق بود. اطلاعات ورود را بررسی کنید.")
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
                val resp = api.getTimeMirrorContent(select = "id", status = null)
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
            val success = repository.submitUserStory(title, content, author)
            onResult(success)
        }
    }

    fun createTimeMirror(dateKey: String, title: String, narrative: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val map = mapOf(
                    "id" to java.util.UUID.randomUUID().toString(),
                    "date_key" to dateKey,
                    "title" to title,
                    "narrative" to narrative,
                    "status" to status
                )
                api.createTimeMirror(map)
                loadAdminData()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun updateTimeMirrorStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                api.updateTimeMirror("eq.$id", mapOf("status" to newStatus))
                loadAdminData()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun deleteTimeMirror(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deleteTimeMirror("eq.$id")
                loadAdminData()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun updateSubmissionStatus(id: String, newStatus: String, adminNotes: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val map = mutableMapOf<String, Any>("status" to newStatus)
                if (adminNotes != null) map["admin_notes"] = adminNotes
                api.updateUserSubmission("eq.$id", map)
                loadAdminData()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun createRealStory(title: String, content: String, author: String, source: String, coverUrl: String, tags: String, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val map = mapOf(
                    "id" to java.util.UUID.randomUUID().toString(),
                    "title" to title,
                    "content" to content,
                    "author" to author,
                    "source" to source,
                    "cover_image_url" to coverUrl,
                    "tags" to tags,
                    "status" to status
                )
                api.createRealStory(map)
                loadAdminData()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun updateRealStoryStatus(id: String, newStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                api.updateRealStory("eq.$id", mapOf("status" to newStatus))
                loadAdminData()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun deleteRealStory(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deleteRealStory("eq.$id")
                loadAdminData()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun generateAILore(prompt: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) {
                withContext(Dispatchers.Main) {
                    onResult("خطا: کلید واژه‌ی هوش مصنوعی یافت نشد. لطفا در فایل تنظیمات پروژه آن را بررسی کنید.")
                }
                return@launch
            }

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
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
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
                        onResult("کاتب ارواح در سکوت فرورفته است. خطایی در برقراری ارتباط رخ داد.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("خطای ماوراء الطبیعه: ${e.localizedMessage}")
                }
            }
        }
    }
}
