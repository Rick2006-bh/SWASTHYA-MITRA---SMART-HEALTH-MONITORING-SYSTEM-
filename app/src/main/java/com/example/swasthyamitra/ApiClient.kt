package com.example.swasthyamitra

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Request Body for /chat
data class ChatRequest(
    val email: String,
    val message: String? = null,
    @SerializedName("heart_rate") val heartRate: Int? = null,
    val temperature: Double? = null,
    val ecg: String? = null,
    @SerializedName("user_profile") val userProfile: UserProfile? = null
)

// Detailed Health Profile with robust defaults
data class UserProfile(
    val name: String? = "User",
    val gender: String? = "Not Specified",
    val age: Int? = 0,
    val weight: Int? = 0,
    val height: Int? = 0,
    val bmi: Double? = 0.0,
    @SerializedName("diet_type") val dietType: String? = "Balanced",
    @SerializedName("activity_level") val activityLevel: String? = "Moderate",
    @SerializedName("sleep_quality") val sleepQuality: String? = "Good",
    @SerializedName("blood_pressure") val bloodPressure: String? = "120/80",
    @SerializedName("blood_sugar") val bloodSugar: String? = "90",
    @SerializedName("stress_level") val stressLevel: String? = "Low",
    val allergies: String? = "None",
    val smoking: String? = "No",
    val alcohol: String? = "No",
    @SerializedName("chronic_conditions") val chronicConditions: String? = "None",
    @SerializedName("recent_health_concerns") val recentHealthConcerns: String? = "None"
)

// Response from /chat
data class ChatResponse(
    val reply: String?
)

// Request for /reset
data class ResetRequest(
    val email: String
)

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

    @POST("reset")
    suspend fun resetChat(@Body request: ResetRequest): Response<Unit>
}

object ApiClient {
    private const val BASE_URL = "https://swasthyamitra-gpsw.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS) // Handle Render cold start
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
