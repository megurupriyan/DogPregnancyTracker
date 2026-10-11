package com.example.pawnatal.data.model

import kotlinx.serialization.Serializable

enum class AiProvider(val displayName: String, val defaultModel: String, val defaultBaseUrl: String) {
    OPENAI("OpenAI", "sol", "https://api.openai.com/v1/chat/completions"),
    GEMINI("Google Gemini (Free Tier)", "gemini-2.5-flash", "https://generativelanguage.googleapis.com/v1beta/models/"),
    GROQ("Groq Cloud (Fast & Free Tier)", "llama-3.3-70b-versatile", "https://api.groq.com/openai/v1/chat/completions"),
    OPENROUTER("OpenRouter", "meta-llama/llama-3.3-70b-instruct:free", "https://openrouter.ai/api/v1/chat/completions"),
    CUSTOM("Custom OpenAI-Compatible", "sol", "http://10.0.2.2:11434/v1/chat/completions")
}

enum class TempUnit(val symbol: String, val label: String) {
    FAHRENHEIT("°F", "Fahrenheit"),
    CELSIUS("°C", "Celsius")
}

enum class WeightUnit(val symbol: String, val label: String) {
    LBS("lbs", "Pounds"),
    KG("kg", "Kilograms")
}

@Serializable
data class AppSettings(
    val activeProvider: String = AiProvider.OPENAI.name,
    val openAiApiKey: String = "",
    val geminiApiKey: String = "",
    val groqApiKey: String = "",
    val openRouterApiKey: String = "",
    val customApiKey: String = "",
    val customModelId: String = "sol",
    val customBaseUrl: String = "https://api.openai.com/v1/chat/completions",
    val tempUnit: String = TempUnit.CELSIUS.name,
    val weightUnit: String = WeightUnit.KG.name,
    val notificationsEnabled: Boolean = true,
    val morningReminderTime: String = "08:00",
    val eveningReminderTime: String = "20:00",
    val tempDropAlarmEnabled: Boolean = true
) {
    val provider: AiProvider
        get() = try {
            AiProvider.valueOf(activeProvider)
        } catch (e: Exception) {
            AiProvider.OPENAI
        }

    fun getActiveApiKey(): String = when (provider) {
        AiProvider.OPENAI -> openAiApiKey
        AiProvider.GEMINI -> geminiApiKey
        AiProvider.GROQ -> groqApiKey
        AiProvider.OPENROUTER -> openRouterApiKey
        AiProvider.CUSTOM -> customApiKey
    }

    val activeModelId: String
        get() = if (customModelId.isNotBlank()) customModelId else provider.defaultModel
}
