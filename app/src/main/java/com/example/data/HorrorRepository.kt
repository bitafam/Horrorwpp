package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class HorrorRepository(context: Context) {
    private val dao = HorrorDatabase.getDatabase(context).horrorDao()

    private val api: SupabaseApi
        get() = SupabaseClientProvider.api

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
        val cachedMap = cached.associateBy { it.id }
        if (cached.isNotEmpty() && !forceRefresh) {
            return@withContext cached.map {
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        }
        
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getRealStories(status = "eq.PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertRealStories(list.map {
                        val cachedItem = cachedMap[it.id]
                        CachedRealStory(
                            it.id,
                            it.title,
                            it.content,
                            it.author,
                            it.source,
                            it.cover_image_url,
                            it.tags,
                            it.status,
                            rating = cachedItem?.rating ?: it.rating,
                            ratingCount = cachedItem?.ratingCount ?: it.rating_count,
                            viewCount = cachedItem?.viewCount ?: it.view_count
                        )
                    })
                    return@withContext list.map {
                        val cachedItem = cachedMap[it.id]
                        it.copy(
                            rating = cachedItem?.rating ?: it.rating,
                            rating_count = cachedItem?.ratingCount ?: it.rating_count,
                            view_count = cachedItem?.viewCount ?: it.view_count
                        )
                    }
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
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        } else {
            emptyList()
        }
    }

    suspend fun getAllRealStoriesAdmin(): List<RealStory> = withContext(Dispatchers.IO) {
        val cached = dao.getAllRealStories()
        val cachedMap = cached.associateBy { it.id }
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getRealStories()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertRealStories(list.map {
                        val cachedItem = cachedMap[it.id]
                        CachedRealStory(
                            it.id,
                            it.title,
                            it.content,
                            it.author,
                            it.source,
                            it.cover_image_url,
                            it.tags,
                            it.status,
                            rating = cachedItem?.rating ?: it.rating,
                            ratingCount = cachedItem?.ratingCount ?: it.rating_count,
                            viewCount = cachedItem?.viewCount ?: it.view_count
                        )
                    })
                    return@withContext list.map {
                        val cachedItem = cachedMap[it.id]
                        it.copy(
                            rating = cachedItem?.rating ?: it.rating,
                            rating_count = cachedItem?.ratingCount ?: it.rating_count,
                            view_count = cachedItem?.viewCount ?: it.view_count
                        )
                    }
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
            RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
        }
    }

    suspend fun saveRealStory(story: RealStory): RealStory = withContext(Dispatchers.IO) {
        val validId = if (isValidUuid(story.id)) story.id else java.util.UUID.randomUUID().toString()
        val safeRating = kotlin.math.round((story.rating.coerceIn(0f, 5f)) * 10f) / 10.0
        val preparedStory = story.copy(
            id = validId,
            rating = safeRating.toFloat(),
            author = story.author?.ifBlank { "راوی عمارت" } ?: "راوی عمارت",
            source = story.source?.ifBlank { "روایات واقعی" } ?: "روایات واقعی"
        )

        if (SupabaseClientProvider.isConfigured) {
            val baseMap = mutableMapOf<String, Any>(
                "id" to validId,
                "title" to preparedStory.title,
                "content" to preparedStory.content,
                "author" to (preparedStory.author ?: "راوی عمارت"),
                "source" to (preparedStory.source ?: "روایات واقعی"),
                "status" to preparedStory.status
            )
            if (!preparedStory.cover_image_url.isNullOrBlank()) baseMap["cover_image_url"] = preparedStory.cover_image_url
            if (!preparedStory.tags.isNullOrBlank()) baseMap["tags"] = preparedStory.tags

            val fullMap = baseMap.toMutableMap().apply {
                put("rating", safeRating)
                put("rating_count", preparedStory.rating_count)
                put("view_count", preparedStory.view_count)
            }

            try {
                // 1. Try Upsert with fullMap
                val resp = api.upsertRealStory(item = fullMap)
                if (resp.isSuccessful) {
                    val returned = resp.body()?.firstOrNull() ?: preparedStory
                    dao.upsertRealStory(
                        CachedRealStory(
                            returned.id,
                            returned.title,
                            returned.content,
                            returned.author,
                            returned.source,
                            returned.cover_image_url,
                            returned.tags,
                            returned.status,
                            returned.rating,
                            returned.rating_count,
                            returned.view_count
                        )
                    )
                    return@withContext returned
                }

                // 2. Try minimal insert with fullMap
                val minRespFull = api.insertRealStoryMinimal(item = fullMap)
                if (minRespFull.isSuccessful) {
                    dao.upsertRealStory(
                        CachedRealStory(
                            preparedStory.id,
                            preparedStory.title,
                            preparedStory.content,
                            preparedStory.author,
                            preparedStory.source,
                            preparedStory.cover_image_url,
                            preparedStory.tags,
                            preparedStory.status,
                            preparedStory.rating,
                            preparedStory.rating_count,
                            preparedStory.view_count
                        )
                    )
                    return@withContext preparedStory
                }

                // 3. Try minimal insert with baseMap (excludes optional rating/view columns)
                val minRespBase = api.insertRealStoryMinimal(item = baseMap)
                if (minRespBase.isSuccessful) {
                    dao.upsertRealStory(
                        CachedRealStory(
                            preparedStory.id,
                            preparedStory.title,
                            preparedStory.content,
                            preparedStory.author,
                            preparedStory.source,
                            preparedStory.cover_image_url,
                            preparedStory.tags,
                            preparedStory.status,
                            preparedStory.rating,
                            preparedStory.rating_count,
                            preparedStory.view_count
                        )
                    )
                    return@withContext preparedStory
                }

                // 4. Try minimal update (PATCH without id)
                val patchMap = baseMap.toMutableMap().apply { remove("id") }
                val updateResp = api.updateRealStoryMinimal(idEq = "eq.$validId", item = patchMap)
                if (updateResp.isSuccessful) {
                    dao.upsertRealStory(
                        CachedRealStory(
                            preparedStory.id,
                            preparedStory.title,
                            preparedStory.content,
                            preparedStory.author,
                            preparedStory.source,
                            preparedStory.cover_image_url,
                            preparedStory.tags,
                            preparedStory.status,
                            preparedStory.rating,
                            preparedStory.rating_count,
                            preparedStory.view_count
                        )
                    )
                    return@withContext preparedStory
                }

                val lastCode = if (minRespBase.code() != 0) minRespBase.code() else if (minRespFull.code() != 0) minRespFull.code() else resp.code()
                val lastError = minRespBase.errorBody()?.string() ?: minRespFull.errorBody()?.string() ?: resp.errorBody()?.string() ?: ""
                android.util.Log.e("SupabaseError", "saveRealStory failed: $lastCode - $lastError")
                throw Exception("خطا در ذخیره‌سازی داستان در سرور ($lastCode): $lastError")
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

    // ATOMIC RATING & VIEWS RPC (With robust REST PATCH fallbacks)
    suspend fun incrementStoryViewRemote(storyId: String, currentCount: Int): Boolean = withContext(Dispatchers.IO) {
        val newCount = currentCount + 1
        // Always save locally first so it is guaranteed to persist and show in UI immediately
        val cached = dao.getAllRealStories().find { it.id == storyId }
        if (cached != null) {
            dao.upsertRealStory(cached.copy(viewCount = newCount))
        }

        if (SupabaseClientProvider.isConfigured) {
            try {
                // Try RPC first
                val resp = api.incrementStoryView(mapOf("story_id" to storyId))
                if (resp.isSuccessful) {
                    return@withContext true
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "incrementStoryView RPC failed: $code - $err. Trying direct PATCH.")
                    // Fallback to direct PATCH
                    api.updateRealStory(idEq = "eq.$storyId", item = mapOf("view_count" to newCount))
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "incrementStoryView exception. Fallback handled.", e)
                try {
                    api.updateRealStory(idEq = "eq.$storyId", item = mapOf("view_count" to newCount))
                } catch (ex: Exception) {
                    android.util.Log.e("SupabaseError", "incrementStoryView direct PATCH failed", ex)
                }
            }
        }
        true
    }

    suspend fun submitStoryRatingRemote(storyId: String, rating: Float, currentRating: Float, currentCount: Int): Boolean = withContext(Dispatchers.IO) {
        val newCount = currentCount + 1
        val newRating = ((currentRating * currentCount) + rating) / newCount
        
        // Always save locally first so it is guaranteed to persist and show in UI immediately
        val cached = dao.getAllRealStories().find { it.id == storyId }
        if (cached != null) {
            dao.upsertRealStory(cached.copy(rating = newRating, ratingCount = newCount))
        }

        if (SupabaseClientProvider.isConfigured) {
            try {
                // Try RPC first
                val resp = api.submitStoryRating(mapOf("story_id" to storyId, "new_rating" to rating))
                if (resp.isSuccessful) {
                    return@withContext true
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "submitStoryRating RPC failed: $code - $err. Trying direct PATCH.")
                    // Fallback to direct PATCH
                    api.updateRealStory(
                        idEq = "eq.$storyId",
                        item = mapOf("rating" to newRating, "rating_count" to newCount)
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "submitStoryRating exception. Fallback handled.", e)
                try {
                    api.updateRealStory(
                        idEq = "eq.$storyId",
                        item = mapOf("rating" to newRating, "rating_count" to newCount)
                    )
                } catch (ex: Exception) {
                    android.util.Log.e("SupabaseError", "submitStoryRating direct PATCH failed", ex)
                }
            }
        }
        true
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

    suspend fun incrementSubmissionViewRemote(submissionId: String, currentCount: Int): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                // Try RPC first
                val resp = api.incrementSubmissionView(mapOf("submission_id" to submissionId))
                if (resp.isSuccessful) {
                    return@withContext true
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "incrementSubmissionView RPC failed: $code - $err. Falling back to direct PATCH.")
                    val newCount = currentCount + 1
                    val patchResp = api.updateUserSubmissionMinimal(idEq = "eq.$submissionId", item = mapOf("view_count" to newCount))
                    return@withContext patchResp.isSuccessful
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "incrementSubmissionView exception. Falling back to direct PATCH.", e)
                try {
                    val newCount = currentCount + 1
                    val patchResp = api.updateUserSubmissionMinimal(idEq = "eq.$submissionId", item = mapOf("view_count" to newCount))
                    return@withContext patchResp.isSuccessful
                } catch (ex: Exception) {
                    android.util.Log.e("SupabaseError", "incrementSubmissionView direct PATCH failed", ex)
                }
            }
        }
        false
    }

    suspend fun submitSubmissionRatingRemote(submissionId: String, rating: Float, currentRating: Float, currentCount: Int): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                // Try RPC first
                val resp = api.submitSubmissionRating(mapOf("submission_id" to submissionId, "new_rating" to rating))
                if (resp.isSuccessful) {
                    return@withContext true
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "submitSubmissionRating RPC failed: $code - $err. Falling back to direct PATCH.")
                    val newCount = currentCount + 1
                    val newRating = ((currentRating * currentCount) + rating) / newCount
                    val patchResp = api.updateUserSubmissionMinimal(
                        idEq = "eq.$submissionId",
                        item = mapOf("rating" to newRating, "rating_count" to newCount)
                    )
                    return@withContext patchResp.isSuccessful
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "submitSubmissionRating exception. Falling back to direct PATCH.", e)
                try {
                    val newCount = currentCount + 1
                    val newRating = ((currentRating * currentCount) + rating) / newCount
                    val patchResp = api.updateUserSubmissionMinimal(
                        idEq = "eq.$submissionId",
                        item = mapOf("rating" to newRating, "rating_count" to newCount)
                    )
                    return@withContext patchResp.isSuccessful
                } catch (ex: Exception) {
                    android.util.Log.e("SupabaseError", "submitSubmissionRating direct PATCH failed", ex)
                }
            }
        }
        false
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
}
