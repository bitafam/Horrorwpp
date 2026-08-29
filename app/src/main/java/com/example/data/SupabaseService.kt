package com.example.data

import okhttp3.Interceptor
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

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    @com.squareup.moshi.Json(name = "access_token") val accessToken: String?,
    @com.squareup.moshi.Json(name = "token_type") val tokenType: String?,
    val user: SupabaseUser?
)

data class SupabaseUser(
    val id: String,
    val email: String?
)

data class AiGenerationRequest(
    val provider: String,
    val model: String,
    val prompt: String,
    val count: Int,
    val section: String
)

data class AiGenerationResponse(
    val result: String,
    val parsedData: List<Map<String, Any>>?
)

interface SupabaseApi {
    @GET("rest/v1/time_mirror_content")
    suspend fun getTimeMirrorContent(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
    ): Response<List<TimeMirrorContent>>

    @POST("rest/v1/time_mirror_content")
    @Headers("Prefer: return=representation")
    suspend fun createTimeMirror(
        @Body item: Map<String, Any>
    ): Response<List<TimeMirrorContent>>

    @PATCH("rest/v1/time_mirror_content")
    @Headers("Prefer: return=representation")
    suspend fun updateTimeMirror(
        @Query("id") idEq: String,
        @Body item: Map<String, Any>
    ): Response<List<TimeMirrorContent>>

    @DELETE("rest/v1/time_mirror_content")
    suspend fun deleteTimeMirror(
        @Query("id") idEq: String
    ): Response<ResponseBody>

    @GET("rest/v1/real_stories")
    suspend fun getRealStories(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
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

    @GET("rest/v1/user_story_submissions")
    suspend fun getUserSubmissions(
        @Query("select") select: String = "*",
        @Query("status") status: String? = null
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

    val api: SupabaseApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("apikey", supabaseAnonKey)
                if (currentAuthToken != null) {
                    requestBuilder.header("Authorization", "Bearer $currentAuthToken")
                } else {
                    requestBuilder.header("Authorization", "Bearer $supabaseAnonKey")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        // Ensure URL has trailing slash
        val baseUrl = if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}
