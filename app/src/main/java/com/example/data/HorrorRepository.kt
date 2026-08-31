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

    suspend fun saveGrimFortune(fortune: GrimFortune): GrimFortune = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "month_index" to fortune.month_index,
                "month_name" to fortune.month_name,
                "title" to fortune.title,
                "fortune_text" to fortune.fortune_text,
                "status" to fortune.status
            )
            if (fortune.id.isNotBlank() && !fortune.id.startsWith("gf-")) {
                map["id"] = fortune.id
            }
            if (fortune.omen_poem != null) map["omen_poem"] = fortune.omen_poem
            if (fortune.doom_level != null) map["doom_level"] = fortune.doom_level

            val resp = api.upsertGrimFortunes(items = listOf(map))
            if (resp.isSuccessful && resp.body() != null) {
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
                val map = mutableMapOf<String, Any>(
                    "month_index" to it.month_index,
                    "month_name" to it.month_name,
                    "title" to it.title,
                    "fortune_text" to it.fortune_text,
                    "status" to it.status
                )
                if (it.id.isNotBlank() && !it.id.startsWith("gf-")) {
                    map["id"] = it.id
                }
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
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        }
        
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getRealStories(status = "eq.PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertRealStories(list.map {
                        CachedRealStory(it.id, it.title, it.content, it.author, it.source, it.cover_image_url, it.tags, it.status, it.rating, it.rating_count, it.view_count)
                    })
                    return@withContext list
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
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getRealStories()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertRealStories(list.map {
                        CachedRealStory(it.id, it.title, it.content, it.author, it.source, it.cover_image_url, it.tags, it.status, it.rating, it.rating_count, it.view_count)
                    })
                    return@withContext list
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
        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "id" to story.id,
                "title" to story.title,
                "content" to story.content,
                "author" to (story.author ?: ""),
                "source" to (story.source ?: ""),
                "cover_image_url" to (story.cover_image_url ?: ""),
                "tags" to (story.tags ?: ""),
                "status" to story.status,
                "rating" to story.rating,
                "rating_count" to story.rating_count,
                "view_count" to story.view_count
            )
            try {
                val resp = api.upsertRealStory(item = map)
                if (resp.isSuccessful && resp.body() != null) {
                    val returned = resp.body()!!.first()
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
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "saveRealStory failed: $code - $errorBody")
                    throw Exception("خطا در ذخیره‌سازی داستان در سرور: $code")
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
        if (SupabaseClientProvider.isConfigured) {
            try {
                // Try RPC first
                val resp = api.incrementStoryView(mapOf("story_id" to storyId))
                if (resp.isSuccessful) {
                    return@withContext true
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "incrementStoryView RPC failed: $code - $err. Falling back to direct PATCH.")
                    // Fallback to direct PATCH
                    val newCount = currentCount + 1
                    val patchResp = api.updateRealStory(idEq = "eq.$storyId", item = mapOf("view_count" to newCount))
                    return@withContext patchResp.isSuccessful
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "incrementStoryView exception. Falling back to direct PATCH.", e)
                try {
                    val newCount = currentCount + 1
                    val patchResp = api.updateRealStory(idEq = "eq.$storyId", item = mapOf("view_count" to newCount))
                    return@withContext patchResp.isSuccessful
                } catch (ex: Exception) {
                    android.util.Log.e("SupabaseError", "incrementStoryView direct PATCH failed", ex)
                }
            }
        }
        false
    }

    suspend fun submitStoryRatingRemote(storyId: String, rating: Float, currentRating: Float, currentCount: Int): Boolean = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            try {
                // Try RPC first
                val resp = api.submitStoryRating(mapOf("story_id" to storyId, "new_rating" to rating))
                if (resp.isSuccessful) {
                    return@withContext true
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "submitStoryRating RPC failed: $code - $err. Falling back to direct PATCH.")
                    // Fallback to direct PATCH
                    val newCount = currentCount + 1
                    val newRating = ((currentRating * currentCount) + rating) / newCount
                    val patchResp = api.updateRealStory(
                        idEq = "eq.$storyId",
                        item = mapOf("rating" to newRating, "rating_count" to newCount)
                    )
                    return@withContext patchResp.isSuccessful
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "submitStoryRating exception. Falling back to direct PATCH.", e)
                try {
                    val newCount = currentCount + 1
                    val newRating = ((currentRating * currentCount) + rating) / newCount
                    val patchResp = api.updateRealStory(
                        idEq = "eq.$storyId",
                        item = mapOf("rating" to newRating, "rating_count" to newCount)
                    )
                    return@withContext patchResp.isSuccessful
                } catch (ex: Exception) {
                    android.util.Log.e("SupabaseError", "submitStoryRating direct PATCH failed", ex)
                }
            }
        }
        false
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
        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "id" to scenario.id,
                "title" to scenario.title,
                "description" to scenario.description,
                "status" to scenario.status
            )
            if (scenario.initial_scene_id != null) {
                map["initial_scene_id"] = scenario.initial_scene_id
            }
            try {
                val resp = api.upsertScenario(item = map)
                if (resp.isSuccessful && resp.body() != null) {
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
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "saveScenario failed: $code - $errorBody")
                    throw Exception("خطا در ذخیره‌سازی سناریو در سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "saveScenario exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun deleteScenario(id: String) = withContext(Dispatchers.IO) {
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
                UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
            }
        }
        
        if (SupabaseClientProvider.isConfigured) {
            try {
                val resp = api.getUserSubmissions(status = "eq.PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertUserSubmissions(list.map {
                        CachedUserSubmission(it.id, it.title, it.content, it.author_name, it.status, it.admin_notes, it.createdAt)
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
                UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
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
                        CachedUserSubmission(it.id, it.title, it.content, it.author_name, it.status, it.admin_notes, it.createdAt)
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
            UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
        }
    }

    suspend fun createUserSubmission(sub: UserStorySubmission): UserStorySubmission = withContext(Dispatchers.IO) {
        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "id" to sub.id,
                "title" to sub.title,
                "content" to sub.content,
                "author_name" to sub.author_name,
                "status" to sub.status
            )
            try {
                val resp = api.submitUserStory(item = map)
                if (resp.isSuccessful && resp.body() != null) {
                    val returned = resp.body()!!.first()
                    dao.upsertUserSubmission(
                        CachedUserSubmission(
                            returned.id,
                            returned.title,
                            returned.content,
                            returned.author_name,
                            returned.status,
                            returned.admin_notes,
                            returned.createdAt
                        )
                    )
                    return@withContext returned
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
        if (SupabaseClientProvider.isConfigured) {
            val map = mutableMapOf<String, Any>(
                "id" to sub.id,
                "title" to sub.title,
                "content" to sub.content,
                "author_name" to sub.author_name,
                "status" to sub.status
            )
            if (sub.admin_notes != null) map["admin_notes"] = sub.admin_notes
            try {
                val resp = api.upsertUserSubmission(item = map)
                if (resp.isSuccessful && resp.body() != null) {
                    val returned = resp.body()!!.first()
                    dao.upsertUserSubmission(
                        CachedUserSubmission(
                            returned.id,
                            returned.title,
                            returned.content,
                            returned.author_name,
                            returned.status,
                            returned.admin_notes,
                            returned.createdAt
                        )
                    )
                    return@withContext returned
                } else {
                    val code = resp.code()
                    val errorBody = resp.errorBody()?.string() ?: ""
                    android.util.Log.e("SupabaseError", "saveUserSubmission failed: $code - $errorBody")
                    throw Exception("خطا در ذخیره‌سازی ارسالی در سرور: $code")
                }
            } catch (e: Exception) {
                android.util.Log.e("SupabaseError", "saveUserSubmission exception", e)
                throw e
            }
        } else {
            throw Exception("اتصال به Supabase تنظیم نشده است.")
        }
    }

    suspend fun deleteUserSubmission(id: String) = withContext(Dispatchers.IO) {
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
