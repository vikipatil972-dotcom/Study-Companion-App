package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- Gemini Content Request Classes ---
data class GeminiRequest(
    @Json(name = "contents") val contents: List<ContentPart>
)

data class ContentPart(
    @Json(name = "parts") val parts: List<TextPart>
)

data class TextPart(
    @Json(name = "text") val text: String
)

// --- Gemini Content Response Classes ---
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

data class Candidate(
    @Json(name = "content") val content: ContentPart? = null
)

object GeminiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing or invalid. Please setup your GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        val requestObj = GeminiRequest(
            contents = listOf(
                ContentPart(
                    parts = listOf(
                        TextPart(text = prompt)
                    )
                )
            )
        )

        val jsonAdapter = moshi.adapter(GeminiRequest::class.java)
        val jsonBody = jsonAdapter.toJson(requestObj)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: Request failed with status code ${response.code}. Please ensure your API key contains valid permissions."
                }
                val responseBody = response.body?.string() ?: return@withContext "Empty response received."
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val responseObj = responseAdapter.fromJson(responseBody)
                val textResponse = responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                textResponse ?: "Could not extract text from response model."
            }
        } catch (e: Exception) {
            "API Call failed: ${e.localizedMessage}"
        }
    }
}
