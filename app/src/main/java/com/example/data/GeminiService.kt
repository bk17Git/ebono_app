package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getBookRecommendation(
        readingHistory: List<String>,
        favoriteCategories: List<String>,
        userPrompt: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Returning offline recommendation.")
            return@withContext getOfflineRecommendation(favoriteCategories, userPrompt)
        }

        val prompt = buildPrompt(readingHistory, favoriteCategories, userPrompt)

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful && responseBody.isNotEmpty()) {
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "")
                    }
                }
            }
            Log.e(TAG, "Gemini API failed with code: ${response.code}, body: $responseBody")
            return@withContext getOfflineRecommendation(favoriteCategories, userPrompt)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
            return@withContext getOfflineRecommendation(favoriteCategories, userPrompt)
        }
    }

    private fun buildPrompt(history: List<String>, categories: List<String>, custom: String?): String {
        return """
            You are Ebono Books AI, a highly literary, cultured librarian recommendation engine.
            The user is asking for custom book recommendations.
            Favorite Categories: ${categories.joinToString()}
            Recently Read Books: ${history.joinToString()}
            ${if (!custom.isNullOrBlank()) "Specific User Request: $custom" else ""}
            
            Please suggest 3 specific books (either classics or beautifully descriptive simulated titles) that match this profile.
            Provide:
            1. Title and Author
            2. Genre and Description (why they would love it)
            3. A matching Ebono library category (Fiction, Sci-Fi, Mystery, Philosophy, Biography, Self-Help).
            Keep the response formatting clean, sophisticated, using double line-breaks between books. Avoid markdown lists. Keep it poetic yet practical.
        """.trimIndent()
    }

    private fun getOfflineRecommendation(categories: List<String>, custom: String?): String {
        val category = categories.firstOrNull() ?: "Fiction"
        return """
            Ebono Curated Recommendations (Offline Mode)

            Based on your interest in "$category", we have prepared these selections:

            1. "The Shadow of the Wind" by Carlos Ruiz Zafón
            Genre: Mystery / Fiction
            A gorgeous, gothic tale of a young boy who finds a mysterious book in a secret library in Barcelona. Perfect for book lovers who cherish the magic of paper and ink.

            2. "Silence in the Age of Noise" by Erling Kagge
            Genre: Philosophy / Self-Help
            An explorer’s elegant reflection on the power of shutting out the world. Deeply atmospheric, pairing perfectly with Ebono's distraction-free Focus Mode.

            3. "Zen and the Art of Motorcycle Maintenance" by Robert M. Pirsig
            Genre: Philosophy
            A beautiful, philosophical journey exploring the concept of Quality, duty, and deep attention. It helps anchor our minds in a busy digital world.
        """.trimIndent()
    }
}
