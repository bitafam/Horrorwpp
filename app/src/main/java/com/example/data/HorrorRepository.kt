package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class HorrorRepository(context: Context) {
    private val dao = HorrorDatabase.getDatabase(context).horrorDao()

    private val api: SupabaseApi
        get() = SupabaseClientProvider.api

    init {
        if (!SupabaseClientProvider.isConfigured) {
            val prefs = context.getSharedPreferences("horror_house_admin_prefs", Context.MODE_PRIVATE)
            val savedUrl = prefs.getString("supabase_url", null)
            val savedKey = prefs.getString("supabase_anon_key", null)
            if (!savedUrl.isNullOrBlank() && !savedKey.isNullOrBlank()) {
                SupabaseClientProvider.configure(savedUrl, savedKey)
            }
        }
    }

    // GRIM FORTUNES
    suspend fun getGrimFortunes(forceRefresh: Boolean = false): List<GrimFortune> = withContext(Dispatchers.IO) {
        val cached = dao.getPublishedGrimFortunes()
        if (cached.isNotEmpty() && !forceRefresh) {
            return@withContext cached.map {
                GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
            }
        }
        
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getGrimFortunes(status = "eq.PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertGrimFortunes(list.map {
                        CachedGrimFortune(it.id, it.month_index, it.month_name, it.title, it.omen_poem, it.fortune_text, it.doom_level, it.status)
                    })
                    return@withContext list
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getGrimFortunes failed: $code - $errorBody")
                    if (forceRefresh) throw Exception("خطا در دریافت طالع‌ها از سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getGrimFortunes exception: ${e.message}", e)
                if (forceRefresh) throw e
            }
        }
        
        if (cached.isNotEmpty()) {
            cached.map {
                GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
            }
        } else {
            emptyList()
        }
    }

    suspend fun getAllGrimFortunesAdmin(): List<GrimFortune> = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getGrimFortunes()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertGrimFortunes(list.map {
                        CachedGrimFortune(it.id, it.month_index, it.month_name, it.title, it.omen_poem, it.fortune_text, it.doom_level, it.status)
                    })
                    return@withContext list
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getAllGrimFortunesAdmin failed: $code - $errorBody")
                    throw Exception("خطا در دریافت لیست ادمین طالع‌ها: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAllGrimFortunesAdmin exception: ${e.message}", e)
                throw e
            }
        }
        dao.getAllGrimFortunes().map {
            GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
        }
    }

    private fun isValidUuid(id: String?): Boolean {
        if (id.isNullOrBlank()) return false
        return try {
            java.util.UUID.fromString(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveGrimFortunesLocalOnly(fortunes: List<GrimFortune>) = withContext(Dispatchers.IO) {
        dao.upsertGrimFortunes(fortunes.map {
            CachedGrimFortune(it.id, it.month_index, it.month_name, it.title, it.omen_poem, it.fortune_text, it.doom_level, it.status)
        })
    }

    suspend fun saveRealStoriesLocalOnly(stories: List<RealStory>) = withContext(Dispatchers.IO) {
        dao.upsertRealStories(stories.map {
            CachedRealStory(it.id, it.title, it.content, it.author, it.source, it.cover_image_url, it.tags, it.status, it.rating, it.rating_count, it.view_count)
        })
    }

    suspend fun saveScenariosLocalOnly(scenarios: List<WrongChoiceScenario>) = withContext(Dispatchers.IO) {
        dao.upsertScenarios(scenarios.map {
            CachedScenario(it.id, it.title, it.description, it.status, it.initial_scene_id)
        })
    }

    suspend fun saveUserSubmissionsLocalOnly(submissions: List<UserStorySubmission>) = withContext(Dispatchers.IO) {
        dao.upsertUserSubmissions(submissions.map {
            CachedUserSubmission(
                it.id,
                it.title,
                it.content,
                it.author_name,
                it.cover_image_url,
                it.tags,
                it.status,
                it.admin_notes,
                it.rating,
                it.rating_count,
                it.view_count,
                it.createdAt
            )
        })
    }

    suspend fun saveGrimFortune(fortune: GrimFortune): GrimFortune = withContext(Dispatchers.IO) {
        val validId = if (isValidUuid(fortune.id)) fortune.id else java.util.UUID.randomUUID().toString()
        val preparedFortune = fortune.copy(id = validId)

        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "id" to validId,
                "month_index" to preparedFortune.month_index,
                "month_name" to preparedFortune.month_name,
                "title" to preparedFortune.title,
                "fortune_text" to preparedFortune.fortune_text,
                "status" to preparedFortune.status
            )
            if (preparedFortune.omen_poem != null) map["omen_poem"] = preparedFortune.omen_poem
            if (preparedFortune.doom_level != null) map["doom_level"] = preparedFortune.doom_level

            val resp = api.upsertGrimFortunes(items = listOf(map))
            if (resp.isSuccessful && !resp.body().isNullOrEmpty()) {
                val returned = resp.body()!!.first()
                dao.upsertGrimFortune(
                    CachedGrimFortune(
                        returned.id,
                        returned.month_index,
                        returned.month_name,
                        returned.title,
                        returned.omen_poem,
                        returned.fortune_text,
                        returned.doom_level,
                        returned.status
                    )
                )
                return@withContext returned
            } else {
                val code = resp.code()
                val errorBody = resp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "saveGrimFortune failed: $code - $errorBody")
                throw Exception("خطا در ذخیره‌سازی طالع در سرور: $code")
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun upsertGrimFortunes(fortunes: List<GrimFortune>): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            val list = fortunes.map {
                val validId = if (isValidUuid(it.id)) it.id else java.util.UUID.randomUUID().toString()
                val map = mutableMapOf<String, Any>(
                    "id" to validId,
                    "month_index" to it.month_index,
                    "month_name" to it.month_name,
                    "title" to it.title,
                    "fortune_text" to it.fortune_text,
                    "status" to it.status
                )
                if (it.omen_poem != null) map["omen_poem"] = it.omen_poem
                if (it.doom_level != null) map["doom_level"] = it.doom_level
                map
            }
            try {
                val resp = api.upsertGrimFortunes(items = list)
                if (resp.isSuccessful && resp.body() != null) {
                    val returned = resp.body()!!
                    dao.upsertGrimFortunes(returned.map {
                        CachedGrimFortune(it.id, it.month_index, it.month_name, it.title, it.omen_poem, it.fortune_text, it.doom_level, it.status)
                    })
                    return@withContext true
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "upsertGrimFortunes failed: $code - $errorBody")
                    throw Exception("خطا در ثبت گروهی طالع‌ها: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "upsertGrimFortunes exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun deleteGrimFortune(id: String) = withContext(Dispatchers.IO) {
        if (!isValidUuid(id)) {
            dao.deleteGrimFortune(id)
            return@withContext
        }
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.deleteGrimFortune(idEq = "eq.$id")
                if (resp.isSuccessful) {
                    dao.deleteGrimFortune(id)
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "deleteGrimFortune failed: $code - $errorBody")
                    throw Exception("خطا در حذف طالع از سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "deleteGrimFortune exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    // REAL STORIES
    suspend fun getRealStories(forceRefresh: Boolean = false): List<RealStory> = withContext(Dispatchers.IO) {
        val cached = dao.getPublishedRealStories()
        if (cached.isNotEmpty() && !forceRefresh) {
            return@withContext cached.map {
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, cleanTagsString(it.tags), it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        }
        
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getRealStories(status = "eq.PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    val cleanedList = list.map { story ->
                        story.copy(tags = cleanTagsString(story.tags))
                    }
                    dao.upsertRealStories(cleanedList.map {
                        CachedRealStory(
                            id = it.id,
                            title = it.title,
                            content = it.content,
                            author = it.author,
                            source = it.source,
                            coverImageUrl = it.cover_image_url,
                            tags = it.tags,
                            status = it.status,
                            rating = it.rating,
                            ratingCount = it.rating_count,
                            viewCount = it.view_count
                        )
                    })
                    return@withContext cleanedList
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getRealStories failed: $code - $errorBody")
                    if (forceRefresh) throw Exception("خطا در دریافت داستان‌ها از سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getRealStories exception: ${e.message}", e)
                if (forceRefresh) throw e
            }
        }
        
        if (cached.isNotEmpty()) {
            cached.map {
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, cleanTagsString(it.tags), it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        } else {
            emptyList()
        }
    }

    suspend fun getAllRealStoriesAdmin(): List<RealStory> = withContext(Dispatchers.IO) {
        val cached = dao.getAllRealStories()
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getRealStories()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    val cleanedList = list.map { story ->
                        story.copy(tags = cleanTagsString(story.tags))
                    }
                    dao.upsertRealStories(cleanedList.map {
                        CachedRealStory(
                            id = it.id,
                            title = it.title,
                            content = it.content,
                            author = it.author,
                            source = it.source,
                            coverImageUrl = it.cover_image_url,
                            tags = it.tags,
                            status = it.status,
                            rating = it.rating,
                            ratingCount = it.rating_count,
                            viewCount = it.view_count
                        )
                    })
                    return@withContext cleanedList
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getAllRealStoriesAdmin failed: $code - $errorBody")
                    throw Exception("خطا در دریافت داستان‌های ادمین: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAllRealStoriesAdmin exception: ${e.message}", e)
                throw e
            }
        }
        dao.getAllRealStories().map {
            RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, cleanTagsString(it.tags), it.status, it.rating, it.ratingCount, it.viewCount, null, null)
        }
    }

    suspend fun saveRealStory(story: RealStory): RealStory = withContext(Dispatchers.IO) {
        val validId = if (isValidUuid(story.id)) story.id else java.util.UUID.randomUUID().toString()
        val cleanTags = cleanTagsString(story.tags)

        val preparedStory = story.copy(
            id = validId,
            author = story.author?.ifBlank { "راوی عمارت" } ?: "راوی عمارت",
            source = story.source?.ifBlank { "روایات واقعی" } ?: "روایات واقعی",
            tags = cleanTags
        )

        if (SupabaseClientProvider.isConfigured) {
            val fullMap = mutableMapOf<String, Any>(
                "id" to validId,
                "title" to preparedStory.title,
                "content" to preparedStory.content,
                "author" to (preparedStory.author ?: "راوی عمارت"),
                "source" to (preparedStory.source ?: "روایات واقعی"),
                "status" to preparedStory.status,
                "rating" to preparedStory.rating,
                "rating_count" to preparedStory.rating_count,
                "view_count" to preparedStory.view_count
            )
            if (!cleanTags.isNullOrBlank()) fullMap["tags"] = cleanTags
            if (!preparedStory.cover_image_url.isNullOrBlank()) fullMap["cover_image_url"] = preparedStory.cover_image_url

            try {
                val resp = api.upsertRealStory(item = fullMap)
                if (resp.isSuccessful) {
                    val returned = resp.body()?.firstOrNull() ?: preparedStory
                    val returnedCleaned = returned.copy(tags = cleanTagsString(returned.tags))
                    dao.upsertRealStory(
                        CachedRealStory(
                            returnedCleaned.id,
                            returnedCleaned.title,
                            returnedCleaned.content,
                            returnedCleaned.author,
                            returnedCleaned.source,
                            returnedCleaned.cover_image_url,
                            returnedCleaned.tags,
                            returnedCleaned.status,
                            rating = returnedCleaned.rating,
                            ratingCount = returnedCleaned.rating_count,
                            viewCount = returnedCleaned.view_count
                        )
                    )
                    return@withContext returnedCleaned
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "saveRealStory failed: $code - $errorBody")
                    throw Exception("خطا در ذخیره‌سازی داستان واقعی در سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "saveRealStory exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun saveRealStoryLocalOnly(story: RealStory) = withContext(Dispatchers.IO) {
        dao.upsertRealStory(
            CachedRealStory(
                story.id,
                story.title,
                story.content,
                story.author,
                story.source,
                story.cover_image_url,
                story.tags,
                story.status,
                story.rating,
                story.rating_count,
                story.view_count
            )
        )
    }

    suspend fun deleteRealStory(id: String) = withContext(Dispatchers.IO) {
        if (!isValidUuid(id)) {
            dao.deleteRealStory(id)
            return@withContext
        }
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.deleteRealStory(idEq = "eq.$id")
                if (resp.isSuccessful) {
                    dao.deleteRealStory(id)
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "deleteRealStory failed: $code - $errorBody")
                    throw Exception("خطا در حذف داستان از سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "deleteRealStory exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    // NOTIFICATIONS
    suspend fun getAllNotifications(): List<CachedAppNotification> = withContext(Dispatchers.IO) {
        val localList = dao.getAllNotifications()
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getAppNotifications()
                if (resp.isSuccessful && resp.body() != null) {
                    val dtos = resp.body()!!
                    val remoteList = dtos.map { it.toCached() }
                    for (item in remoteList) {
                        dao.upsertNotification(item)
                    }
                    val combined = (remoteList + localList).distinctBy { it.id }.sortedByDescending { it.timestamp }
                    val now = System.currentTimeMillis()
                    // Filter out pending future scheduled notifications for regular users
                    return@withContext combined.filter { 
                        it.status == "PUBLISHED" || (!it.isScheduled && (it.scheduledAt == null || it.scheduledAt <= now)) 
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAllNotifications exception", e)
            }
        }
        localList
    }

    suspend fun getAllNotificationsAdmin(): List<CachedAppNotification> = withContext(Dispatchers.IO) {
        val localList = dao.getAllNotificationsAdmin()
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getAppNotifications()
                if (resp.isSuccessful && resp.body() != null) {
                    val dtos = resp.body()!!
                    val remoteList = dtos.map { it.toCached() }
                    for (item in remoteList) {
                        dao.upsertNotification(item)
                    }
                    val combined = (remoteList + localList).distinctBy { it.id }.sortedByDescending { it.timestamp }
                    return@withContext combined
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAllNotificationsAdmin exception", e)
            }
        }
        localList
    }

    suspend fun upsertNotification(notification: CachedAppNotification) = withContext(Dispatchers.IO) {
        dao.upsertNotification(notification)
        if (SupabaseClientProvider.isConfigured) {
            try {
                val body = mutableMapOf<String, Any>(
                    "id" to notification.id,
                    "title" to notification.title,
                    "message" to notification.message,
                    "image_url" to (notification.imageUrl ?: ""),
                    "timestamp" to notification.timestamp,
                    "is_scheduled" to notification.isScheduled,
                    "status" to notification.status,
                    "trigger_condition" to (notification.triggerCondition ?: "")
                )
                if (notification.scheduledAt != null) {
                    body["scheduled_at"] = notification.scheduledAt
                }
                api.createAppNotification(body)
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "upsertNotification exception", e)
            }
        }
    }

    suspend fun deleteNotification(id: String) = withContext(Dispatchers.IO) {
        dao.deleteNotification(id)
        if (SupabaseClientProvider.isConfigured) {
            try {
                api.deleteAppNotification(idEq = "eq.$id")
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "deleteNotification exception", e)
            }
        }
    }

    // SYSTEM / APP SETTINGS (Stored in Supabase DB)
    suspend fun getAppSettings(): List<AppSetting> = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getAppSettings()
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAppSettings exception", e)
            }
        }
        emptyList()
    }

    suspend fun saveAppSetting(key: String, value: String, description: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val item = mapOf(
                    "key" to key,
                    "value" to value,
                    "description" to (description ?: "")
                )
                val resp = api.upsertAppSetting(item)
                return@withContext resp.isSuccessful
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "saveAppSetting exception", e)
            }
        }
        false
    }

    // AUTOMATION CONFIGS
    suspend fun getAutomationConfigs(): List<AutomationConfig> = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getAutomationConfigs()
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAutomationConfigs exception", e)
            }
        }
        emptyList()
    }

    suspend fun saveAutomationConfig(config: AutomationConfig): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val item = mutableMapOf<String, Any>(
                    "id" to config.id,
                    "is_active" to config.is_active,
                    "frequency" to config.frequency,
                    "schedule_hour_1" to config.schedule_hour_1,
                    "schedule_hour_2" to config.schedule_hour_2,
                    "batch_count" to config.batch_count
                )
                config.custom_prompt?.let { item["custom_prompt"] = it }
                val resp = api.upsertAutomationConfig(item)
                return@withContext resp.isSuccessful
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "saveAutomationConfig exception", e)
            }
        }
        false
    }

    // AUTOMATION LOGS
    suspend fun getAutomationLogs(): List<AutomationLog> = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getAutomationLogs()
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAutomationLogs exception", e)
            }
        }
        emptyList()
    }

    suspend fun insertAutomationLog(taskType: String, status: String, message: String): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val item = mapOf<String, Any>(
                    "task_type" to taskType,
                    "status" to status,
                    "message" to message
                )
                val resp = api.insertAutomationLog(item)
                return@withContext resp.isSuccessful
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "insertAutomationLog exception", e)
            }
        }
        false
    }

    // TRIGGER EDGE FUNCTIONS
    suspend fun triggerEdgeFunction(functionName: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured) {
            return@withContext Pair(false, "تنظیمات Supabase برقرار نیست.")
        }
        try {
            val resp = when (functionName) {
                "scheduled-notifications" -> api.triggerScheduledNotifications()
                "auto-grim-fortunes" -> api.triggerAutoGrimFortunes()
                "auto-scenarios" -> api.triggerAutoScenarios()
                else -> return@withContext Pair(false, "فانکشن ناشناخته است.")
            }
            if (resp.isSuccessful) {
                val text = resp.body()?.string() ?: "عملیات با موفقیت فراخوانی شد."
                return@withContext Pair(true, text)
            } else {
                val err = resp.errorBody()?.string() ?: "خطای کد ${resp.code()}"
                return@withContext Pair(false, "خطا در فراخوانی فانکشن ($err)")
            }
        } catch (e: Exception) {
            return@withContext Pair(false, "استثنا در ارتباط: ${e.localizedMessage}")
        }
    }

    // ATOMIC RATING & VIEWS
    suspend fun incrementStoryViewRemote(storyId: String): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured) return@withContext false

        try {
            val resp = api.incrementStoryView(mapOf("story_id" to storyId))
            if (resp.isSuccessful) {
                getRealStories(forceRefresh = true)
                return@withContext true
            } else {
                val code = resp.code()
                val err = resp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "increment_story_view RPC failed: $code - $err")
                return@withContext false
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", "incrementStoryViewRemote exception", e)
            return@withContext false
        }
    }

    suspend fun submitStoryRatingRemote(storyId: String, rating: Float): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured) return@withContext false

        try {
            val resp = api.submitStoryRating(
                mapOf(
                    "story_id" to storyId,
                    "new_rating" to rating
                )
            )
            if (resp.isSuccessful) {
                getRealStories(forceRefresh = true)
                return@withContext true
            } else {
                val code = resp.code()
                val err = resp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "submit_story_rating RPC failed: $code - $err")
                return@withContext false
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", "submitStoryRatingRemote exception", e)
            return@withContext false
        }
    }

    // SCENARIOS
    suspend fun getScenarios(forceRefresh: Boolean = false): List<WrongChoiceScenario> = withContext(Dispatchers.IO) {
        val cached = dao.getPublishedScenarios()
        if (cached.isNotEmpty() && !forceRefresh) {
            return@withContext cached.map {
                WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
            }
        }
        
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getScenarios(status = "eq.PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertScenarios(list.map {
                        CachedScenario(it.id, it.title, it.description, it.status, it.initial_scene_id)
                    })
                    return@withContext list
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getScenarios failed: $code - $errorBody")
                    if (forceRefresh) throw Exception("خطا در دریافت سناریوهای سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getScenarios exception: ${e.message}", e)
                if (forceRefresh) throw e
            }
        }
        
        if (cached.isNotEmpty()) {
            cached.map {
                WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
            }
        } else {
            emptyList()
        }
    }

    suspend fun getAllScenariosAdmin(): List<WrongChoiceScenario> = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getScenarios()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertScenarios(list.map {
                        CachedScenario(it.id, it.title, it.description, it.status, it.initial_scene_id)
                    })
                    return@withContext list
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getAllScenariosAdmin failed: $code - $errorBody")
                    throw Exception("خطا در دریافت سناریوهای ادمین: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAllScenariosAdmin exception: ${e.message}", e)
                throw e
            }
        }
        dao.getAllScenarios().map {
            WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
        }
    }

    suspend fun saveScenario(scenario: WrongChoiceScenario): WrongChoiceScenario = withContext(Dispatchers.IO) {
        val validId = if (isValidUuid(scenario.id)) scenario.id else java.util.UUID.randomUUID().toString()
        val validInitialSceneId = if (isValidUuid(scenario.initial_scene_id)) scenario.initial_scene_id else null
        val preparedScenario = scenario.copy(id = validId, initial_scene_id = validInitialSceneId)

        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "id" to validId,
                "title" to preparedScenario.title,
                "description" to preparedScenario.description,
                "status" to preparedScenario.status
            )
            if (validInitialSceneId != null) {
                map["initial_scene_id"] = validInitialSceneId
            }
            try {
                // 1. Try Upsert with List
                val resp = api.upsertScenarios(items = listOf(map))
                if (resp.isSuccessful && !resp.body().isNullOrEmpty()) {
                    val returned = resp.body()!!.first()
                    dao.upsertScenario(
                        CachedScenario(
                            returned.id,
                            returned.title,
                            returned.description,
                            returned.status,
                            returned.initial_scene_id
                        )
                    )
                    return@withContext returned
                }

                // 2. Try Create with List
                val createResp = api.createScenarios(items = listOf(map))
                if (createResp.isSuccessful && !createResp.body().isNullOrEmpty()) {
                    val returned = createResp.body()!!.first()
                    dao.upsertScenario(
                        CachedScenario(
                            returned.id,
                            returned.title,
                            returned.description,
                            returned.status,
                            returned.initial_scene_id
                        )
                    )
                    return@withContext returned
                }

                // 3. Fallback: Update existing row without "id" in PATCH body
                val patchMap = map.toMutableMap().apply { remove("id") }
                val updateResp = api.updateScenario(idEq = "eq.$validId", item = patchMap)
                if (updateResp.isSuccessful && !updateResp.body().isNullOrEmpty()) {
                    val returned = updateResp.body()!!.first()
                    dao.upsertScenario(
                        CachedScenario(
                            returned.id,
                            returned.title,
                            returned.description,
                            returned.status,
                            returned.initial_scene_id
                        )
                    )
                    return@withContext returned
                }

                val code = if (resp.code() != 200 && resp.code() != 0) resp.code() else if (createResp.code() != 200 && createResp.code() != 0) createResp.code() else updateResp.code()
                val errorBody = resp.errorBody()?.string() ?: createResp.errorBody()?.string() ?: updateResp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "saveScenario failed: $code - $errorBody")
                throw Exception("خطا در ذخیره‌سازی سناریو در سرور: $code")
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "saveScenario exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun deleteScenario(id: String) = withContext(Dispatchers.IO) {
        if (!isValidUuid(id)) {
            dao.deleteScenario(id)
            return@withContext
        }
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.deleteScenario(idEq = "eq.$id")
                if (resp.isSuccessful) {
                    dao.deleteScenario(id)
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "deleteScenario failed: $code - $errorBody")
                    throw Exception("خطا در حذف سناریو از سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "deleteScenario exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    // USER SUBMISSIONS
    suspend fun getUserSubmissions(forceRefresh: Boolean = false): List<UserStorySubmission> = withContext(Dispatchers.IO) {
        val cached = dao.getPublishedUserSubmissions()
        if (cached.isNotEmpty() && !forceRefresh) {
            return@withContext cached.map {
                UserStorySubmission(
                    it.id,
                    it.title,
                    it.content,
                    it.authorName,
                    it.coverImageUrl,
                    it.tags,
                    it.status,
                    it.adminNotes,
                    it.rating,
                    it.ratingCount,
                    it.viewCount,
                    it.createdAt
                )
            }
        }
        
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getUserSubmissions(status = "eq.PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertUserSubmissions(list.map {
                        CachedUserSubmission(
                            it.id,
                            it.title,
                            it.content,
                            it.author_name,
                            it.cover_image_url,
                            it.tags,
                            it.status,
                            it.admin_notes,
                            it.rating,
                            it.rating_count,
                            it.view_count,
                            it.createdAt
                        )
                    })
                    return@withContext list
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getUserSubmissions failed: $code - $errorBody")
                    if (forceRefresh) throw Exception("خطا در دریافت ارسالی‌های کاربران: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getUserSubmissions exception: ${e.message}", e)
                if (forceRefresh) throw e
            }
        }
        
        if (cached.isNotEmpty()) {
            cached.map {
                UserStorySubmission(
                    it.id,
                    it.title,
                    it.content,
                    it.authorName,
                    it.coverImageUrl,
                    it.tags,
                    it.status,
                    it.adminNotes,
                    it.rating,
                    it.ratingCount,
                    it.viewCount,
                    it.createdAt
                )
            }
        } else {
            emptyList()
        }
    }

    suspend fun getAllUserSubmissionsAdmin(): List<UserStorySubmission> = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getUserSubmissions()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertUserSubmissions(list.map {
                        CachedUserSubmission(
                            it.id,
                            it.title,
                            it.content,
                            it.author_name,
                            it.cover_image_url,
                            it.tags,
                            it.status,
                            it.admin_notes,
                            it.rating,
                            it.rating_count,
                            it.view_count,
                            it.createdAt
                        )
                    })
                    return@withContext list
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "getAllUserSubmissionsAdmin failed: $code - $errorBody")
                    throw Exception("خطا در دریافت لیست ارسالی‌های کاربران ادمین: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getAllUserSubmissionsAdmin exception: ${e.message}", e)
                throw e
            }
        }
        dao.getAllUserSubmissions().map {
            UserStorySubmission(
                it.id,
                it.title,
                it.content,
                it.authorName,
                it.coverImageUrl,
                it.tags,
                it.status,
                it.adminNotes,
                it.rating,
                it.ratingCount,
                it.viewCount,
                it.createdAt
            )
        }
    }

    suspend fun createUserSubmission(sub: UserStorySubmission): UserStorySubmission = withContext(Dispatchers.IO) {
        val validId = if (isValidUuid(sub.id)) sub.id else java.util.UUID.randomUUID().toString()
        val preparedSub = sub.copy(id = validId, status = "PENDING")

        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "id" to validId,
                "title" to preparedSub.title,
                "content" to preparedSub.content,
                "author_name" to preparedSub.author_name,
                "status" to "PENDING"
            )
            try {
                val resp = api.submitUserStory(item = map)
                if (resp.isSuccessful) {
                    dao.upsertUserSubmission(
                        CachedUserSubmission(
                            preparedSub.id,
                            preparedSub.title,
                            preparedSub.content,
                            preparedSub.author_name,
                            preparedSub.cover_image_url,
                            preparedSub.tags,
                            preparedSub.status,
                            preparedSub.admin_notes,
                            preparedSub.rating,
                            preparedSub.rating_count,
                            preparedSub.view_count,
                            preparedSub.createdAt
                        )
                    )
                    return@withContext preparedSub
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "createUserSubmission failed: $code - $errorBody")
                    throw Exception("خطا در ثبت داستان در سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "createUserSubmission exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun saveUserSubmission(sub: UserStorySubmission): UserStorySubmission = withContext(Dispatchers.IO) {
        val validId = if (isValidUuid(sub.id)) sub.id else java.util.UUID.randomUUID().toString()
        val safeRating = kotlin.math.round((sub.rating.coerceIn(0f, 5f)) * 10f) / 10.0
        val preparedSub = sub.copy(id = validId, rating = safeRating.toFloat())

        if (SupabaseClientProvider.isConfigured) {
            val baseMap = mutableMapOf<String, Any>(
                "id" to validId,
                "title" to preparedSub.title,
                "content" to preparedSub.content,
                "author_name" to preparedSub.author_name,
                "status" to preparedSub.status
            )
            if (!preparedSub.cover_image_url.isNullOrBlank()) baseMap["cover_image_url"] = preparedSub.cover_image_url
            if (!preparedSub.tags.isNullOrBlank()) baseMap["tags"] = preparedSub.tags
            if (preparedSub.admin_notes != null) baseMap["admin_notes"] = preparedSub.admin_notes

            val fullMap = baseMap.toMutableMap().apply {
                put("rating", safeRating)
                put("rating_count", preparedSub.rating_count)
                put("view_count", preparedSub.view_count)
            }

            try {
                // 1. Try Upsert
                val resp = api.upsertUserSubmission(item = fullMap)
                if (resp.isSuccessful) {
                    dao.upsertUserSubmission(
                        CachedUserSubmission(
                            preparedSub.id,
                            preparedSub.title,
                            preparedSub.content,
                            preparedSub.author_name,
                            preparedSub.cover_image_url,
                            preparedSub.tags,
                            preparedSub.status,
                            preparedSub.admin_notes,
                            preparedSub.rating,
                            preparedSub.rating_count,
                            preparedSub.view_count,
                            preparedSub.createdAt
                        )
                    )
                    return@withContext preparedSub
                }

                // 2. Try Update with full map (strip id from PATCH body)
                val patchMap = fullMap.toMutableMap().apply { remove("id") }
                val updateResp = api.updateUserSubmissionMinimal(idEq = "eq.$validId", item = patchMap)
                if (updateResp.isSuccessful) {
                    dao.upsertUserSubmission(
                        CachedUserSubmission(
                            preparedSub.id,
                            preparedSub.title,
                            preparedSub.content,
                            preparedSub.author_name,
                            preparedSub.cover_image_url,
                            preparedSub.tags,
                            preparedSub.status,
                            preparedSub.admin_notes,
                            preparedSub.rating,
                            preparedSub.rating_count,
                            preparedSub.view_count,
                            preparedSub.createdAt
                        )
                    )
                    return@withContext preparedSub
                }

                // 3. Fallback to base map without ratings columns
                val basePatchMap = baseMap.toMutableMap().apply { remove("id") }
                val updateBaseResp = api.updateUserSubmissionMinimal(idEq = "eq.$validId", item = basePatchMap)
                if (updateBaseResp.isSuccessful) {
                    dao.upsertUserSubmission(
                        CachedUserSubmission(
                            preparedSub.id,
                            preparedSub.title,
                            preparedSub.content,
                            preparedSub.author_name,
                            preparedSub.cover_image_url,
                            preparedSub.tags,
                            preparedSub.status,
                            preparedSub.admin_notes,
                            preparedSub.rating,
                            preparedSub.rating_count,
                            preparedSub.view_count,
                            preparedSub.createdAt
                        )
                    )
                    return@withContext preparedSub
                }

                // 4. Ultimate Fallback: Try with only the 4 guaranteed core fields (title, content, author_name, status)
                val safePatchMap = mapOf(
                    "title" to preparedSub.title,
                    "content" to preparedSub.content,
                    "author_name" to preparedSub.author_name,
                    "status" to preparedSub.status
                )
                val updateSafeResp = api.updateUserSubmissionMinimal(idEq = "eq.$validId", item = safePatchMap)
                if (updateSafeResp.isSuccessful) {
                    dao.upsertUserSubmission(
                        CachedUserSubmission(
                            preparedSub.id,
                            preparedSub.title,
                            preparedSub.content,
                            preparedSub.author_name,
                            null, // coverImageUrl (since it couldn't be saved on remote)
                            null, // tags (since it couldn't be saved on remote)
                            preparedSub.status,
                            null, // adminNotes
                            preparedSub.rating,
                            preparedSub.rating_count,
                            preparedSub.view_count,
                            preparedSub.createdAt
                        )
                    )
                    return@withContext preparedSub.copy(cover_image_url = null, tags = null, admin_notes = null)
                }

                val code = if (updateSafeResp.code() != 0) updateSafeResp.code() else (if (updateResp.code() != 0) updateResp.code() else resp.code())
                val errorBody = updateSafeResp.errorBody()?.string() ?: updateResp.errorBody()?.string() ?: resp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "saveUserSubmission failed: $code - $errorBody")
                throw Exception("خطا در ذخیره‌سازی ارسالی در سرور ($code): $errorBody")
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "saveUserSubmission exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun incrementSubmissionViewRemote(submissionId: String): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured) return@withContext false

        try {
            val resp = api.incrementSubmissionView(mapOf("submission_id" to submissionId))
            if (resp.isSuccessful) {
                getUserSubmissions(forceRefresh = true)
                return@withContext true
            } else {
                val code = resp.code()
                val err = resp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "increment_submission_view RPC failed: $code - $err")
                return@withContext false
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", "incrementSubmissionViewRemote exception", e)
            return@withContext false
        }
    }

    suspend fun submitSubmissionRatingRemote(submissionId: String, rating: Float): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseClientProvider.isConfigured) return@withContext false

        try {
            val resp = api.submitSubmissionRating(
                mapOf(
                    "submission_id" to submissionId,
                    "new_rating" to rating
                )
            )
            if (resp.isSuccessful) {
                getUserSubmissions(forceRefresh = true)
                return@withContext true
            } else {
                val code = resp.code()
                val err = resp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "submit_submission_rating RPC failed: $code - $err")
                return@withContext false
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseError", "submitSubmissionRatingRemote exception", e)
            return@withContext false
        }
    }

    suspend fun deleteUserSubmission(id: String) = withContext(Dispatchers.IO) {
        if (!isValidUuid(id)) {
            dao.deleteUserSubmission(id)
            return@withContext
        }
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.deleteUserSubmission(idEq = "eq.$id")
                if (resp.isSuccessful) {
                    dao.deleteUserSubmission(id)
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "deleteUserSubmission failed: $code - $errorBody")
                    throw Exception("خطا در حذف ارسالی کاربر از سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "deleteUserSubmission exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun getStoryReports(): List<StoryReport> = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getStoryReports()
                if (resp.isSuccessful && resp.body() != null) {
                    return@withContext resp.body()!!
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "getStoryReports exception", e)
            }
        }
        emptyList()
    }

    suspend fun createStoryReport(report: StoryReport): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val body = mapOf<String, Any>(
                    "id" to report.id,
                    "story_id" to report.story_id,
                    "story_title" to report.story_title,
                    "story_author" to report.story_author,
                    "story_type" to report.story_type,
                    "reason" to report.reason
                )
                val resp = api.createStoryReport(body)
                return@withContext resp.isSuccessful
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "createStoryReport exception", e)
            }
        }
        false
    }

    suspend fun deleteStoryReport(id: String): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.deleteStoryReport(idEq = "eq.$id")
                return@withContext resp.isSuccessful
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "deleteStoryReport exception", e)
            }
        }
        false
    }
}

// Helper function to clean tags string
fun cleanTagsString(tags: String?): String? {
    if (tags.isNullOrBlank()) return null
    val tagsOnly = tags.split("| STATS_v1:")[0].trim()
    return tagsOnly.ifBlank { null }
}

