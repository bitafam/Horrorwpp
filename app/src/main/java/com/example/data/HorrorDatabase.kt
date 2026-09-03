package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert

@Entity(tableName = "cached_grim_fortunes")
data class CachedGrimFortune(
    @PrimaryKey val id: String,
    val monthIndex: Int,
    val monthName: String,
    val title: String,
    val omenPoem: String?,
    val fortuneText: String,
    val doomLevel: String?,
    val status: String
)

@Entity(tableName = "cached_real_stories")
data class CachedRealStory(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val author: String?,
    val source: String?,
    val coverImageUrl: String?,
    val tags: String?,
    val status: String,
    val rating: Float = 4.8f,
    val ratingCount: Int = 18,
    val viewCount: Int = 340
)

@Entity(tableName = "cached_scenarios")
data class CachedScenario(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val initialSceneId: String?
)

@Entity(tableName = "cached_user_submissions")
data class CachedUserSubmission(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val authorName: String,
    val coverImageUrl: String? = null,
    val tags: String? = null,
    val status: String,
    val adminNotes: String? = null,
    val rating: Float = 5.0f,
    val ratingCount: Int = 1,
    val viewCount: Int = 1,
    val createdAt: String? = null
)

@Dao
interface HorrorDao {
    // Grim Fortunes
    @Query("SELECT * FROM cached_grim_fortunes ORDER BY monthIndex ASC")
    suspend fun getAllGrimFortunes(): List<CachedGrimFortune>

    @Query("SELECT * FROM cached_grim_fortunes WHERE status = 'PUBLISHED' ORDER BY monthIndex ASC")
    suspend fun getPublishedGrimFortunes(): List<CachedGrimFortune>

    @Upsert
    suspend fun upsertGrimFortunes(items: List<CachedGrimFortune>)

    @Upsert
    suspend fun upsertGrimFortune(item: CachedGrimFortune)

    @Query("DELETE FROM cached_grim_fortunes WHERE id = :id")
    suspend fun deleteGrimFortune(id: String)

    // Real Stories
    @Query("SELECT * FROM cached_real_stories")
    suspend fun getAllRealStories(): List<CachedRealStory>

    @Query("SELECT * FROM cached_real_stories WHERE status = 'PUBLISHED'")
    suspend fun getPublishedRealStories(): List<CachedRealStory>

    @Upsert
    suspend fun upsertRealStories(items: List<CachedRealStory>)

    @Upsert
    suspend fun upsertRealStory(item: CachedRealStory)

    @Query("DELETE FROM cached_real_stories WHERE id = :id")
    suspend fun deleteRealStory(id: String)

    // Scenarios
    @Query("SELECT * FROM cached_scenarios")
    suspend fun getAllScenarios(): List<CachedScenario>

    @Query("SELECT * FROM cached_scenarios WHERE status = 'PUBLISHED'")
    suspend fun getPublishedScenarios(): List<CachedScenario>

    @Upsert
    suspend fun upsertScenarios(items: List<CachedScenario>)

    @Upsert
    suspend fun upsertScenario(item: CachedScenario)

    @Query("DELETE FROM cached_scenarios WHERE id = :id")
    suspend fun deleteScenario(id: String)

    // User Submissions
    @Query("SELECT * FROM cached_user_submissions ORDER BY id DESC")
    suspend fun getAllUserSubmissions(): List<CachedUserSubmission>

    @Query("SELECT * FROM cached_user_submissions WHERE id = :id LIMIT 1")
    suspend fun getUserSubmissionById(id: String): CachedUserSubmission?

    @Query("SELECT * FROM cached_user_submissions WHERE status = 'PUBLISHED' ORDER BY id DESC")
    suspend fun getPublishedUserSubmissions(): List<CachedUserSubmission>

    @Upsert
    suspend fun upsertUserSubmissions(items: List<CachedUserSubmission>)

    @Upsert
    suspend fun upsertUserSubmission(item: CachedUserSubmission)

    @Query("DELETE FROM cached_user_submissions WHERE id = :id")
    suspend fun deleteUserSubmission(id: String)
}

@Database(
    entities = [
        CachedGrimFortune::class,
        CachedRealStory::class,
        CachedScenario::class,
        CachedUserSubmission::class
    ],
    version = 9,
    exportSchema = false
)
abstract class HorrorDatabase : RoomDatabase() {
    abstract fun horrorDao(): HorrorDao

    companion object {
        @Volatile
        private var INSTANCE: HorrorDatabase? = null

        fun getDatabase(context: Context): HorrorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HorrorDatabase::class.java,
                    "horror_offline_cache.db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
