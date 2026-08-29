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

@Entity(tableName = "cached_time_mirror")
data class CachedTimeMirror(
    @PrimaryKey val id: String,
    val dateKey: String,
    val title: String,
    val narrative: String,
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
    val status: String
)

@Entity(tableName = "cached_scenarios")
data class CachedScenario(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val initialSceneId: String?
)

@Dao
interface HorrorDao {
    @Query("SELECT * FROM cached_time_mirror WHERE status = 'PUBLISHED'")
    suspend fun getTimeMirrors(): List<CachedTimeMirror>

    @Upsert
    suspend fun upsertTimeMirrors(items: List<CachedTimeMirror>)

    @Query("SELECT * FROM cached_real_stories WHERE status = 'PUBLISHED'")
    suspend fun getRealStories(): List<CachedRealStory>

    @Upsert
    suspend fun upsertRealStories(items: List<CachedRealStory>)

    @Query("SELECT * FROM cached_scenarios WHERE status = 'PUBLISHED'")
    suspend fun getScenarios(): List<CachedScenario>

    @Upsert
    suspend fun upsertScenarios(items: List<CachedScenario>)
}

@Database(entities = [CachedTimeMirror::class, CachedRealStory::class, CachedScenario::class], version = 1, exportSchema = false)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
