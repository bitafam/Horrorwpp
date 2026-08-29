package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                _timeMirrorList.value = repository.getTimeMirrorContent(true)
                _realStoriesList.value = repository.getRealStories(true)
                val resp = api.getScenarios(status = "PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    _scenariosList.value = resp.body()!!
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطا در بارگذاری اطلاعات: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
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
}
