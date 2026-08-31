package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class AuthRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "token_type") val tokenType: String?,
    val user: SupabaseUser?
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String?
)

@JsonClass(generateAdapter = true)
data class AiGenerationRequest(
    val provider: String,
    val model: String,
    val prompt: String,
    val count: Int,
    val section: String
)

@JsonClass(generateAdapter = true)
data class AiGenerationResponse(
    val result: String,
    val parsedData: List<Map<String, Any>>? = null
)

@kotlin.jvm.JvmSuppressWildcards
interface SupabaseApi {
    @GET("rest/v1/grim_fortunes")
    suspend fun getGrimFortunes(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
    ): Response<List<GrimFortune>>

    @POST("rest/v1/grim_fortunes?on_conflict=month_index")
    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    suspend fun upsertGrimFortunes(
        @Body items: List<Map<String, Any>>
    ): Response<List<GrimFortune>>

    @POST("rest/v1/grim_fortunes")
    @Headers("Prefer: return=representation")
    suspend fun createGrimFortune(
        @Body item: Map<String, Any>
    ): Response<List<GrimFortune>>

    @PATCH("rest/v1/grim_fortunes")
    @Headers("Prefer: return=representation")
    suspend fun updateGrimFortune(
        @Query("id") idEq: String,
        @Body item: Map<String, Any>
    ): Response<List<GrimFortune>>

    @DELETE("rest/v1/grim_fortunes")
    suspend fun deleteGrimFortune(
        @Query("id") idEq: String
    ): Response<ResponseBody>

    @GET("rest/v1/real_stories")
    suspend fun getRealStories(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
    ): Response<List<RealStory>>

    @POST("rest/v1/real_stories?on_conflict=id")
    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    suspend fun upsertRealStory(
        @Body item: Map<String, Any>
    ): Response<List<RealStory>>

    @POST("rest/v1/real_stories")
    @Headers("Prefer: return=representation")
    suspend fun createRealStory(
        @Body item: Map<String, Any>
    ): Response<List<RealStory>>

    @PATCH("rest/v1/real_stories")
    @Headers("Prefer: return=representation")
    suspend fun updateRealStory(
        @Query("id") idEq: String,
        @Body item: Map<String, Any>
    ): Response<List<RealStory>>

    @DELETE("rest/v1/real_stories")
    suspend fun deleteRealStory(
        @Query("id") idEq: String
    ): Response<ResponseBody>

    @POST("rest/v1/rpc/increment_story_view")
    suspend fun incrementStoryView(
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    @POST("rest/v1/rpc/submit_story_rating")
    suspend fun submitStoryRating(
        @Body body: Map<String, Any>
    ): Response<ResponseBody>

    @GET("rest/v1/user_story_submissions")
    suspend fun getUserSubmissions(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
    ): Response<List<UserStorySubmission>>

    @POST("rest/v1/user_story_submissions?on_conflict=id")
    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    suspend fun upsertUserSubmission(
        @Body item: Map<String, Any>
    ): Response<List<UserStorySubmission>>

    @POST("rest/v1/user_story_submissions")
    @Headers("Prefer: return=representation")
    suspend fun submitUserStory(
        @Body item: Map<String, Any>
    ): Response<List<UserStorySubmission>>

    @PATCH("rest/v1/user_story_submissions")
    @Headers("Prefer: return=representation")
    suspend fun updateUserSubmission(
        @Query("id") idEq: String,
        @Body item: Map<String, Any>
    ): Response<List<UserStorySubmission>>

    @DELETE("rest/v1/user_story_submissions")
    suspend fun deleteUserSubmission(
        @Query("id") idEq: String
    ): Response<ResponseBody>

    @GET("rest/v1/wrong_choice_scenarios")
    suspend fun getScenarios(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
    ): Response<List<WrongChoiceScenario>>

    @POST("rest/v1/wrong_choice_scenarios?on_conflict=id")
    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    suspend fun upsertScenario(
        @Body item: Map<String, Any>
    ): Response<List<WrongChoiceScenario>>

    @POST("rest/v1/wrong_choice_scenarios")
    @Headers("Prefer: return=representation")
    suspend fun createScenario(
        @Body item: Map<String, Any>
    ): Response<List<WrongChoiceScenario>>

    @PATCH("rest/v1/wrong_choice_scenarios")
    @Headers("Prefer: return=representation")
    suspend fun updateScenario(
        @Query("id") idEq: String,
        @Body item: Map<String, Any>
    ): Response<List<WrongChoiceScenario>>

    @DELETE("rest/v1/wrong_choice_scenarios")
    suspend fun deleteScenario(
        @Query("id") idEq: String
    ): Response<ResponseBody>

    @GET("rest/v1/ai_prompts")
    suspend fun getAiPrompts(): Response<List<AiPrompt>>

    @PATCH("rest/v1/ai_prompts")
    @Headers("Prefer: return=representation")
    suspend fun updateAiPrompt(
        @Query("prompt_key") keyEq: String,
        @Body item: Map<String, Any>
    ): Response<List<AiPrompt>>

    @GET("rest/v1/ai_providers")
    suspend fun getAiProviders(): Response<List<AiProviderSetting>>

    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id") idEq: String,
        @Query("select") select: String = "*"
    ): Response<List<UserProfile>>

    @POST("auth/v1/token?grant_type=password")
    suspend fun loginAdmin(
        @Header("apikey") apiKey: String,
        @Body body: AuthRequest
    ): Response<AuthResponse>

    @POST("functions/v1/ai-generator")
    suspend fun generateAiContent(
        @Header("Authorization") authHeader: String,
        @Body body: AiGenerationRequest
    ): Response<AiGenerationResponse>
}

object SupabaseClientProvider {
    var supabaseUrl: String = try {
        val url = com.example.BuildConfig.SUPABASE_URL
        if (url.isNotBlank() && !url.contains("your-project")) url else "https://your-project.supabase.co"
    } catch (e: Exception) {
        "https://your-project.supabase.co"
    }

    var supabaseAnonKey: String = try {
        val key = com.example.BuildConfig.SUPABASE_PUBLISHABLE_KEY
        if (key.isNotBlank() && !key.contains("your-supabase")) key else "your-supabase-publishable-key"
    } catch (e: Exception) {
        "your-supabase-publishable-key"
    }

    var currentAuthToken: String? = null

    val isConfigured: Boolean
        get() {
            return supabaseUrl.isNotBlank() && !supabaseUrl.contains("your-project") && !supabaseUrl.contains("placeholder") &&
                   supabaseAnonKey.isNotBlank() && !supabaseAnonKey.contains("your-supabase")
        }

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Volatile
    private var cachedApi: SupabaseApi? = null

    val api: SupabaseApi
        get() {
            val existing = cachedApi
            if (existing != null) return existing
            return synchronized(this) {
                cachedApi ?: buildApi().also { cachedApi = it }
            }
        }

    fun configure(url: String, key: String) {
        synchronized(this) {
            supabaseUrl = url.trim()
            supabaseAnonKey = key.trim()
            cachedApi = null // Invalidates cache so next call builds with new credentials
        }
    }

    private fun buildApi(): SupabaseApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("apikey", supabaseAnonKey)
                val token = currentAuthToken
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                } else {
                    requestBuilder.header("Authorization", "Bearer $supabaseAnonKey")
                }
                
                val response = chain.proceed(requestBuilder.build())
                if (response.code == 401 && token != null) {
                    synchronized(this) {
                        if (currentAuthToken == token) {
                            currentAuthToken = null
                        }
                    }
                    response.close()
                    val fallbackRequest = original.newBuilder()
                        .header("apikey", supabaseAnonKey)
                        .header("Authorization", "Bearer $supabaseAnonKey")
                        .build()
                    chain.proceed(fallbackRequest)
                } else {
                    response
                }
            }
            .build()

        val baseUrl = if (supabaseUrl.isNotBlank() && (supabaseUrl.startsWith("http://") || supabaseUrl.startsWith("https://"))) {
            if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"
        } else {
            "https://placeholder.supabase.co/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }
}
