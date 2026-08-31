package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HorrorRepository(context: Context) {
    private val dao = HorrorDatabase.getDatabase(context).horrorDao()

    private val api: SupabaseApi
        get() = SupabaseClientProvider.api

    // GRIM FORTUNES
    suspend fun getGrimFortunes(forceRefresh: Boolean = false): List<GrimFortune> = withContext(Dispatchers.IO) {
        try {
            val cached = dao.getPublishedGrimFortunes()
            if (cached.isNotEmpty() && !forceRefresh) {
                return@withContext cached.map {
                    GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
                }
            }
            // Fetch remote
            if (SupabaseClientProvider.isConfigured || forceRefresh) {
                val resp = api.getGrimFortunes(status = "PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertGrimFortunes(list.map {
                        CachedGrimFortune(it.id, it.month_index, it.month_name, it.title, it.omen_poem, it.fortune_text, it.doom_level, it.status)
                    })
                    return@withContext list
                }
            }
            if (cached.isNotEmpty()) {
                cached.map {
                    GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            dao.getPublishedGrimFortunes().map {
                GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
            }
        }
    }

    suspend fun getAllGrimFortunesAdmin(): List<GrimFortune> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured) {
                val resp = api.getGrimFortunes()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertGrimFortunes(list.map {
                        CachedGrimFortune(it.id, it.month_index, it.month_name, it.title, it.omen_poem, it.fortune_text, it.doom_level, it.status)
                    })
                    return@withContext list
                }
            }
            dao.getAllGrimFortunes().map {
                GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
            }
        } catch (e: Exception) {
            dao.getAllGrimFortunes().map {
                GrimFortune(it.id, it.monthIndex, it.monthName, it.title, it.omenPoem, it.fortuneText, it.doomLevel, it.status, null, null)
            }
        }
    }

    suspend fun saveGrimFortune(fortune: GrimFortune) = withContext(Dispatchers.IO) {
        dao.upsertGrimFortune(
            CachedGrimFortune(
                fortune.id,
                fortune.month_index,
                fortune.month_name,
                fortune.title,
                fortune.omen_poem,
                fortune.fortune_text,
                fortune.doom_level,
                fortune.status
            )
        )
    }

    suspend fun deleteGrimFortune(id: String) = withContext(Dispatchers.IO) {
        dao.deleteGrimFortune(id)
    }

    // REAL STORIES
    suspend fun getRealStories(forceRefresh: Boolean = false): List<RealStory> = withContext(Dispatchers.IO) {
        try {
            val cached = dao.getPublishedRealStories()
            if (cached.isNotEmpty() && !forceRefresh) {
                return@withContext cached.map {
                    RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
                }
            }
            if (SupabaseClientProvider.isConfigured || forceRefresh) {
                val resp = api.getRealStories(status = "PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertRealStories(list.map {
                        CachedRealStory(it.id, it.title, it.content, it.author, it.source, it.cover_image_url, it.tags, it.status, it.rating, it.rating_count, it.view_count)
                    })
                    return@withContext list
                }
            }
            if (cached.isNotEmpty()) {
                cached.map {
                    RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            dao.getPublishedRealStories().map {
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        }
    }

    suspend fun getAllRealStoriesAdmin(): List<RealStory> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured) {
                val resp = api.getRealStories()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertRealStories(list.map {
                        CachedRealStory(it.id, it.title, it.content, it.author, it.source, it.cover_image_url, it.tags, it.status, it.rating, it.rating_count, it.view_count)
                    })
                    return@withContext list
                }
            }
            dao.getAllRealStories().map {
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        } catch (e: Exception) {
            dao.getAllRealStories().map {
                RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, it.rating, it.ratingCount, it.viewCount, null, null)
            }
        }
    }

    suspend fun saveRealStory(story: RealStory) = withContext(Dispatchers.IO) {
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
        try {
            if (SupabaseClientProvider.isConfigured) {
                val map = mapOf(
                    "rating" to story.rating,
                    "rating_count" to story.rating_count,
                    "view_count" to story.view_count
                )
                api.updateRealStory(idEq = "eq.${story.id}", item = map)
            }
        } catch (e: Exception) {
            // Log or ignore network sync issues
        }
    }

    suspend fun deleteRealStory(id: String) = withContext(Dispatchers.IO) {
        dao.deleteRealStory(id)
    }

    // SCENARIOS
    suspend fun getScenarios(forceRefresh: Boolean = false): List<WrongChoiceScenario> = withContext(Dispatchers.IO) {
        try {
            val cached = dao.getPublishedScenarios()
            if (cached.isNotEmpty() && !forceRefresh) {
                return@withContext cached.map {
                    WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
                }
            }
            if (SupabaseClientProvider.isConfigured || forceRefresh) {
                val resp = api.getScenarios(status = "PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertScenarios(list.map {
                        CachedScenario(it.id, it.title, it.description, it.status, it.initial_scene_id)
                    })
                    return@withContext list
                }
            }
            if (cached.isNotEmpty()) {
                cached.map {
                    WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            dao.getPublishedScenarios().map {
                WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
            }
        }
    }

    suspend fun getAllScenariosAdmin(): List<WrongChoiceScenario> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured) {
                val resp = api.getScenarios()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertScenarios(list.map {
                        CachedScenario(it.id, it.title, it.description, it.status, it.initial_scene_id)
                    })
                    return@withContext list
                }
            }
            dao.getAllScenarios().map {
                WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
            }
        } catch (e: Exception) {
            dao.getAllScenarios().map {
                WrongChoiceScenario(it.id, it.title, it.description, it.status, it.initialSceneId, null)
            }
        }
    }

    suspend fun saveScenario(scenario: WrongChoiceScenario) = withContext(Dispatchers.IO) {
        dao.upsertScenario(
            CachedScenario(
                scenario.id,
                scenario.title,
                scenario.description,
                scenario.status,
                scenario.initial_scene_id
            )
        )
    }

    suspend fun deleteScenario(id: String) = withContext(Dispatchers.IO) {
        dao.deleteScenario(id)
    }

    // USER SUBMISSIONS
    suspend fun getUserSubmissions(forceRefresh: Boolean = false): List<UserStorySubmission> = withContext(Dispatchers.IO) {
        try {
            val cached = dao.getPublishedUserSubmissions()
            if (cached.isNotEmpty() && !forceRefresh) {
                return@withContext cached.map {
                    UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
                }
            }
            if (SupabaseClientProvider.isConfigured || forceRefresh) {
                val resp = api.getUserSubmissions(status = "PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertUserSubmissions(list.map {
                        CachedUserSubmission(it.id, it.title, it.content, it.author_name, it.status, it.admin_notes, it.createdAt)
                    })
                    return@withContext list
                }
            }
            if (cached.isNotEmpty()) {
                cached.map {
                    UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            dao.getPublishedUserSubmissions().map {
                UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
            }
        }
    }

    suspend fun getAllUserSubmissionsAdmin(): List<UserStorySubmission> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured) {
                val resp = api.getUserSubmissions()
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertUserSubmissions(list.map {
                        CachedUserSubmission(it.id, it.title, it.content, it.author_name, it.status, it.admin_notes, it.createdAt)
                    })
                    return@withContext list
                }
            }
            dao.getAllUserSubmissions().map {
                UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
            }
        } catch (e: Exception) {
            dao.getAllUserSubmissions().map {
                UserStorySubmission(it.id, it.title, it.content, it.authorName, it.status, it.adminNotes, it.createdAt)
            }
        }
    }

    suspend fun submitUserStory(title: String, content: String, authorName: String): Boolean = withContext(Dispatchers.IO) {
        val id = java.util.UUID.randomUUID().toString()
        val submission = CachedUserSubmission(
            id = id,
            title = title,
            content = content,
            authorName = authorName,
            status = "PENDING",
            adminNotes = null,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        )
        // Always persist locally first so submission is never lost
        dao.upsertUserSubmission(submission)

        try {
            if (SupabaseClientProvider.isConfigured) {
                val map = mapOf(
                    "id" to id,
                    "title" to title,
                    "content" to content,
                    "author_name" to authorName,
                    "status" to "PENDING"
                )
                val resp = api.submitUserStory(map)
                resp.isSuccessful
            } else {
                true // Stored in local DB successfully
            }
        } catch (e: Exception) {
            true // Persisted locally successfully
        }
    }

    suspend fun saveUserSubmission(sub: UserStorySubmission) = withContext(Dispatchers.IO) {
        dao.upsertUserSubmission(
            CachedUserSubmission(sub.id, sub.title, sub.content, sub.author_name, sub.status, sub.admin_notes, sub.createdAt)
        )
    }

    suspend fun deleteUserSubmission(id: String) = withContext(Dispatchers.IO) {
        dao.deleteUserSubmission(id)
    }
}

