package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Backend API DTOs
data class AnalyzeRequest(
    @Json(name = "text") val text: String,
    @Json(name = "model") val model: String? = null
)

data class SentenceScoreDto(
    @Json(name = "sentence") val sentence: String,
    @Json(name = "ai_score") val aiScore: Int,
    @Json(name = "classification") val classification: String,
    @Json(name = "reasoning") val reasoning: String
)

data class AnalyzeResponse(
    @Json(name = "ai_percentage") val aiPercentage: Int,
    @Json(name = "human_percentage") val humanPercentage: Int,
    @Json(name = "confidence") val confidence: String,
    @Json(name = "overall_assessment") val overallAssessment: String,
    @Json(name = "detailed_explanation") val detailedExplanation: String,
    @Json(name = "sentence_analyses") val sentenceAnalyses: List<SentenceScoreDto> = emptyList(),
    @Json(name = "detection_method") val detectionMethod: String = "Backend Detection API",
    @Json(name = "reliability_limitation") val reliabilityLimitation: String = ""
)

data class HumanizeRequest(
    @Json(name = "text") val text: String,
    @Json(name = "style") val style: String,
    @Json(name = "tone") val tone: String = "Balanced",
    @Json(name = "reading_difficulty") val readingDifficulty: String = "Standard",
    @Json(name = "sentence_length") val sentenceLength: String = "Varied",
    @Json(name = "output_length") val outputLength: String = "Similar",
    @Json(name = "vocabulary_simplicity") val vocabularySimplicity: String = "Natural",
    @Json(name = "custom_instructions") val customInstructions: String = ""
)

data class HumanizeResponse(
    @Json(name = "original_text") val originalText: String,
    @Json(name = "humanized_text") val humanizedText: String,
    @Json(name = "style") val style: String,
    @Json(name = "word_count_before") val wordCountBefore: Int = 0,
    @Json(name = "word_count_after") val wordCountAfter: Int = 0,
    @Json(name = "changes_summary") val changesSummary: String = ""
)

data class AiStyleRequest(
    @Json(name = "text") val text: String,
    @Json(name = "style") val style: String,
    @Json(name = "formality") val formality: String = "High",
    @Json(name = "detail") val detail: String = "Detailed",
    @Json(name = "vocabulary") val vocabulary: String = "Technical",
    @Json(name = "output_length") val outputLength: String = "Standard"
)

data class AiStyleResponse(
    @Json(name = "original_text") val originalText: String,
    @Json(name = "ai_styled_text") val aiStyledText: String,
    @Json(name = "style") val style: String,
    @Json(name = "word_count_before") val wordCountBefore: Int = 0,
    @Json(name = "word_count_after") val wordCountAfter: Int = 0
)

data class ImproveRequest(
    @Json(name = "text") val text: String,
    @Json(name = "tool_type") val toolType: String,
    @Json(name = "target_language") val targetLanguage: String = "English"
)

data class ImproveResponse(
    @Json(name = "original_text") val originalText: String,
    @Json(name = "improved_text") val improvedText: String,
    @Json(name = "tool_type") val toolType: String,
    @Json(name = "notes") val notes: String = ""
)

data class HealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "version") val version: String,
    @Json(name = "provider") val provider: String = "Configured"
)

interface BackendApiService {
    @POST("api/analyze")
    suspend fun analyzeText(@Body request: AnalyzeRequest): Response<AnalyzeResponse>

    @POST("api/humanize")
    suspend fun humanizeText(@Body request: HumanizeRequest): Response<HumanizeResponse>

    @POST("api/ai-style")
    suspend fun convertAiStyle(@Body request: AiStyleRequest): Response<AiStyleResponse>

    @POST("api/improve")
    suspend fun improveText(@Body request: ImproveRequest): Response<ImproveResponse>

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    companion object {
        fun create(baseUrl: String): BackendApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .build()

            val formattedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            return Retrofit.Builder()
                .baseUrl(formattedBaseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(BackendApiService::class.java)
        }
    }
}
