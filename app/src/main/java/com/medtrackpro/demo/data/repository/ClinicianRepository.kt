package com.medtrackpro.demo.data.repository

import com.medtrackpro.demo.BuildConfig
import com.medtrackpro.demo.data.remote.GeminiContent
import com.medtrackpro.demo.data.remote.GeminiGenerateContentRequest
import com.medtrackpro.demo.data.remote.GeminiPart
import com.medtrackpro.demo.data.remote.GeminiRetrofitClient
import java.util.Locale

data class ClinicianInsightStats(
    val totalPatients: Int,
    val totalMedications: Int,
    val averageMedicationsPerPatient: Double,
    val mostCommonSymptomCategory: String,
    val mostCommonSymptomCount: Int,
    val averageSymptomSeverity: Double?
)

class ClinicianRepository {
    private val geminiApi = GeminiRetrofitClient.api

    suspend fun generateInsights(stats: ClinicianInsightStats): List<String>? {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank()) {
            return null
        }

        return try {
            val responseText = geminiApi
                .generateContent(
                    model = BuildConfig.GEMINI_MODEL_NAME,
                    apiKey = apiKey,
                    request = GeminiGenerateContentRequest(
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(
                                    GeminiPart(text = buildInsightPrompt(stats))
                                )
                            )
                        )
                    )
                )
                .candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()

            responseText
                ?.takeIf { it.isNotBlank() }
                ?.let { parseInsights(it) }
                ?.takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildInsightPrompt(stats: ClinicianInsightStats): String {
        val averageMedications = String.format(
            Locale.US,
            "%.1f",
            stats.averageMedicationsPerPatient
        )
        val averageSeverity = stats.averageSymptomSeverity?.let {
            String.format(Locale.US, "%.1f", it)
        } ?: "not available"
        val commonSymptom = if (stats.mostCommonSymptomCount == 0) {
            "not available"
        } else {
            "${stats.mostCommonSymptomCategory} with ${stats.mostCommonSymptomCount} records"
        }

        return "Find Patterns: the clinician dashboard sends aggregated patient data to a GenAI model. " +
            "The model returns 3 interesting patterns or observations about the data.\n\n" +
            "Using only the aggregated patient data below, write 3 observations a clinician might actually find useful. " +
            "Do not simply repeat the dashboard numbers. Each observation should connect the numbers, point out what stands out, " +
            "or suggest what may be worth reviewing next. Write exactly 3 lines, one sentence per line, with no numbering or bullet points.\n\n" +
            "Aggregated patient data:\n" +
            "Total patients: ${stats.totalPatients}\n" +
            "Total medications: ${stats.totalMedications}\n" +
            "Average medications per patient: $averageMedications\n" +
            "Most common symptom category: $commonSymptom\n" +
            "Average symptom severity: $averageSeverity out of 10\n\n" +
            "Do not make medical diagnoses, imply causal relationships, or recommend treatment changes."
    }

    private fun parseInsights(responseText: String): List<String> {
        return responseText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(3)
    }
}
