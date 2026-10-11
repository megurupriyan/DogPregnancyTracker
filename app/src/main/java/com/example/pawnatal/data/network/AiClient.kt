package com.example.pawnatal.data.network

import android.util.Log
import com.example.pawnatal.data.model.AiProvider
import com.example.pawnatal.data.model.AppSettings
import com.example.pawnatal.data.model.DamProfile
import com.example.pawnatal.data.model.ObstetricCarePlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
) {

    suspend fun synthesizeCarePlan(
        dam: DamProfile,
        settings: AppSettings
    ): Result<ObstetricCarePlan> = withContext(Dispatchers.IO) {
        try {
            val apiKey = settings.getActiveApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("No API key configured for ${settings.provider.displayName}. Please configure your API key in Settings.")
                )
            }

            val systemPrompt = AiPromptBuilder.buildSystemInstruction()
            val userPrompt = AiPromptBuilder.buildUserPrompt(dam)

            Log.d("AiClient", "Starting care plan synthesis for ${dam.name} (${dam.breed}) via ${settings.provider.displayName} [${settings.activeModelId}]")
            val rawContent = when (settings.provider) {
                AiProvider.GEMINI -> callGeminiApi(apiKey, settings.activeModelId, systemPrompt, userPrompt)
                else -> callOpenAiCompatibleApi(
                    baseUrl = when (settings.provider) {
                        AiProvider.OPENAI -> "https://api.openai.com/v1/chat/completions"
                        AiProvider.GROQ -> "https://api.groq.com/openai/v1/chat/completions"
                        AiProvider.OPENROUTER -> "https://openrouter.ai/api/v1/chat/completions"
                        AiProvider.CUSTOM -> settings.customBaseUrl
                        else -> "https://api.openai.com/v1/chat/completions"
                    },
                    apiKey = apiKey,
                    modelId = settings.activeModelId,
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt
                )
            }

            Log.d("AiClient", "Raw completion received (${rawContent.length} chars). Parsing JSON...")
            val sanitizedJson = sanitizeJsonText(rawContent)
            val parsedPlan = json.decodeFromString<ObstetricCarePlan>(sanitizedJson)
            Log.d("AiClient", "Care plan successfully parsed for ${dam.name}!")
            Result.success(parsedPlan)
        } catch (e: Exception) {
            Log.e("AiClient", "Synthesis failure: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun testConnection(settings: AppSettings): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = settings.getActiveApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("API key is blank."))
            }

            val testMessage = "Reply with 'OK' if you can read this."
            val reply = when (settings.provider) {
                AiProvider.GEMINI -> callGeminiApi(apiKey, settings.activeModelId, "", testMessage)
                else -> callOpenAiCompatibleApi(
                    baseUrl = when (settings.provider) {
                        AiProvider.OPENAI -> "https://api.openai.com/v1/chat/completions"
                        AiProvider.GROQ -> "https://api.groq.com/openai/v1/chat/completions"
                        AiProvider.OPENROUTER -> "https://openrouter.ai/api/v1/chat/completions"
                        AiProvider.CUSTOM -> settings.customBaseUrl
                        else -> "https://api.openai.com/v1/chat/completions"
                    },
                    apiKey = apiKey,
                    modelId = settings.activeModelId,
                    systemPrompt = "You are a connectivity tester.",
                    userPrompt = testMessage
                )
            }
            Result.success("Connected successfully! Response: ${reply.take(100).trim()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callOpenAiCompatibleApi(
        baseUrl: String,
        apiKey: String,
        modelId: String,
        systemPrompt: String,
        userPrompt: String,
        includeResponseFormat: Boolean = true,
        roleStyle: Int = 0 // 0 = system, 1 = developer, 2 = merged into user
    ): String {
        val messagesArray = buildJsonArray {
            when (roleStyle) {
                0 -> {
                    if (systemPrompt.isNotBlank()) {
                        add(buildJsonObject {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                    }
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }
                1 -> {
                    if (systemPrompt.isNotBlank()) {
                        add(buildJsonObject {
                            put("role", "developer")
                            put("content", systemPrompt)
                        })
                    }
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }
                else -> {
                    val merged = if (systemPrompt.isNotBlank()) "$systemPrompt\n\n$userPrompt" else userPrompt
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", merged)
                    })
                }
            }
        }

        val requestJson = buildJsonObject {
            put("model", modelId)
            put("messages", messagesArray)
            // Note: Omit "temperature" to adhere to modern reasoning and standard models (which default to 1.0)
            // and prevent 400 "Unsupported value: 'temperature'" errors on models like o1, o3, sol, gpt-5/6, etc.
            if (includeResponseFormat) {
                put("response_format", buildJsonObject { put("type", "json_object") })
            }
        }.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                // Auto-fallback 1: if response_format is rejected by the model
                if (response.code == 400 && includeResponseFormat && bodyString.contains("response_format", ignoreCase = true)) {
                    return callOpenAiCompatibleApi(
                        baseUrl = baseUrl,
                        apiKey = apiKey,
                        modelId = modelId,
                        systemPrompt = systemPrompt,
                        userPrompt = userPrompt,
                        includeResponseFormat = false,
                        roleStyle = roleStyle
                    )
                }
                // Auto-fallback 2: if system or developer role is rejected by the model
                if (response.code == 400 && roleStyle < 2 && (bodyString.contains("system", ignoreCase = true) || bodyString.contains("developer", ignoreCase = true) || bodyString.contains("role", ignoreCase = true))) {
                    return callOpenAiCompatibleApi(
                        baseUrl = baseUrl,
                        apiKey = apiKey,
                        modelId = modelId,
                        systemPrompt = systemPrompt,
                        userPrompt = userPrompt,
                        includeResponseFormat = includeResponseFormat,
                        roleStyle = roleStyle + 1
                    )
                }
                throw IllegalStateException("API call failed (code ${response.code}): $bodyString")
            }

            val rootJson = json.parseToJsonElement(bodyString).jsonObject
            val choices = rootJson["choices"]?.jsonArray
                ?: throw IllegalStateException("Unexpected response format: no 'choices' array.")
            if (choices.isEmpty()) {
                throw IllegalStateException("No completion returned by AI model.")
            }
            val firstChoice = choices[0].jsonObject
            val messageObj = firstChoice["message"]?.jsonObject
                ?: throw IllegalStateException("No 'message' object found in completion.")
            return messageObj["content"]?.jsonPrimitive?.content.orEmpty()
        }
    }

    private fun callGeminiApi(
        apiKey: String,
        modelId: String,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"

        val combinedPrompt = if (systemPrompt.isNotBlank()) "$systemPrompt\n\n$userPrompt" else userPrompt

        val requestJson = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", combinedPrompt)
                        })
                    })
                })
            })
            put("generationConfig", buildJsonObject {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini API call failed (code ${response.code}): $bodyString")
            }

            val rootJson = json.parseToJsonElement(bodyString).jsonObject
            val candidates = rootJson["candidates"]?.jsonArray
                ?: throw IllegalStateException("No 'candidates' returned by Gemini.")
            if (candidates.isEmpty()) {
                throw IllegalStateException("Empty candidates list from Gemini.")
            }
            val parts = candidates[0].jsonObject["content"]?.jsonObject?.get("parts")?.jsonArray
                ?: throw IllegalStateException("No parts found in Gemini response.")
            return parts[0].jsonObject["text"]?.jsonPrimitive?.content.orEmpty()
        }
    }

    private fun sanitizeJsonText(raw: String): String {
        var text = raw.trim()
        if (text.contains("```json")) {
            val after = text.substringAfter("```json")
            text = after.substringBefore("```").trim()
        } else if (text.contains("```")) {
            val after = text.substringAfter("```")
            text = after.substringBefore("```").trim()
        }
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1).trim()
        }
        return text
    }
}
