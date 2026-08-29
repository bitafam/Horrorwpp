package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HorrorRepository(context: Context) {
    private val dao = HorrorDatabase.getDatabase(context).horrorDao()
    private val api = SupabaseClientProvider.api

    suspend fun getTimeMirrorContent(forceRefresh: Boolean = false): List<TimeMirrorContent> = withContext(Dispatchers.IO) {
        try {
            if (forceRefresh) {
                val resp = api.getTimeMirrorContent(status = "PUBLISHED")
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()!!
                    dao.upsertTimeMirrors(list.map { CachedTimeMirror(it.id, it.date_key, it.title, it.narrative, it.status) })
                    return@withContext list
                }
            }
            val cached = dao.getTimeMirrors()
            if (cached.isNotEmpty() && !forceRefresh) {
                return@withContext cached.map { TimeMirrorContent(it.id, it.dateKey, it.title, it.narrative, it.status, null, null) }
            }
            // fallback fetch
            val resp = api.getTimeMirrorContent(status = "PUBLISHED")
            if (resp.isSuccessful && resp.body() != null) {
                val list = resp.body()!!
                dao.upsertTimeMirrors(list.map { CachedTimeMirror(it.id, it.date_key, it.title, it.narrative, it.status) })
                list
            } else {
                cached.map { TimeMirrorContent(it.id, it.dateKey, it.title, it.narrative, it.status, null, null) }
            }
        } catch (e: Exception) {
            val cached = dao.getTimeMirrors()
            cached.map { TimeMirrorContent(it.id, it.dateKey, it.title, it.narrative, it.status, null, null) }
        }
    }

    suspend fun getRealStories(forceRefresh: Boolean = false): List<RealStory> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getRealStories(status = "PUBLISHED")
            if (resp.isSuccessful && resp.body() != null) {
                val list = resp.body()!!
                dao.upsertRealStories(list.map { CachedRealStory(it.id, it.title, it.content, it.author, it.source, it.cover_image_url, it.tags, it.status) })
                list
            } else {
                dao.getRealStories().map { RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, null, null) }
            }
        } catch (e: Exception) {
            dao.getRealStories().map { RealStory(it.id, it.title, it.content, it.author, it.source, it.coverImageUrl, it.tags, it.status, null, null) }
        }
    }

    suspend fun submitUserStory(title: String, content: String, authorName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val map = mapOf(
                "id" to java.util.UUID.randomUUID().toString(),
                "title" to title,
                "content" to content,
                "author_name" to authorName,
                "status" to "PENDING"
            )
            val resp = api.submitUserStory(map)
            resp.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
