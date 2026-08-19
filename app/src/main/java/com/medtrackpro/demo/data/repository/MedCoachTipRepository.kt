package com.medtrackpro.demo.data.repository

import android.content.Context
import com.medtrackpro.demo.BuildConfig
import com.medtrackpro.demo.data.local.MedCoachTipDao
import com.medtrackpro.demo.data.local.MedCoachTipEntity
import com.medtrackpro.demo.data.local.MedTrackDatabase
import com.medtrackpro.demo.data.local.MedicationEntity
import com.medtrackpro.demo.data.local.SymptomEntity
import com.medtrackpro.demo.data.remote.GeminiContent
import com.medtrackpro.demo.data.remote.GeminiGenerateContentRequest
import com.medtrackpro.demo.data.remote.GeminiPart
import com.medtrackpro.demo.data.remote.GeminiRetrofitClient
import kotlinx.coroutines.flow.Flow

class MedCoachTipRepository(context: Context) {
    private val medCoachTipDao: MedCoachTipDao =
        MedTrackDatabase.getDatabase(context.applicationContext).medCoachTipDao()
    private val geminiApi = GeminiRetrofitClient.api

    fun getTipsForPatient(patientId: String): Flow<List<MedCoachTipEntity>> {
        return medCoachTipDao.getTipsByPatientId(patientId)
    }

    suspend fun getRecentTipsForPatient(
        patientId: String,
        limit: Int
    ): List<MedCoachTipEntity> {
        return medCoachTipDao.getRecentTipsByPatientId(patientId, limit)
    }

    suspend fun saveTip(patientId: String, tipText: String) {
        medCoachTipDao.insert(
            MedCoachTipEntity(
                patientId = patientId,
                tipText = tipText,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun generateMedicationTip(
        patientName: String?,
        medications: List<MedicationEntity>,
        recentSymptoms: List<SymptomEntity>
    ): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank()) {
            return null
        }

        return try {
            val request = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(
                                text = buildPersonalisedTipPrompt(
                                    patientName = patientName,
                                    medications = medications,
                                    recentSymptoms = recentSymptoms
                                )
                            )
                        )
                    )
                )
            )

            geminiApi
                .generateContent(
                    model = BuildConfig.GEMINI_MODEL_NAME,
                    apiKey = apiKey,
                    request = request
                )
                .candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildPersonalisedTipPrompt(
        patientName: String?,
        medications: List<MedicationEntity>,
        recentSymptoms: List<SymptomEntity>
    ): String {
        val patientLine = patientName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { "First name: $it" }
            ?: "First name: Not available"
        val medicationLine = if (medications.isEmpty()) {
            "No current medications are recorded."
        } else {
            medications.joinToString("; ") { medication ->
                buildString {
                    append(medication.name)
                    append(" ")
                    append(medication.dosage)
                    append(", ")
                    append(medication.frequency)
                    append(" at ")
                    append(medication.scheduledTime)
                    if (medication.notes.isNotBlank()) {
                        append(", notes: ")
                        append(medication.notes)
                    }
                }
            }
        }
        val symptomLine = if (recentSymptoms.isEmpty()) {
            "No recent symptoms are recorded."
        } else {
            recentSymptoms.joinToString("; ") { symptom ->
                buildString {
                    append(symptom.category)
                    append(" severity ")
                    append(symptom.severity)
                    append(" on ")
                    append(symptom.dateTime)
                    if (symptom.notes.isNotBlank()) {
                        append(", notes: ")
                        append(symptom.notes)
                    }
                }
            }
        }

        return "Generate a short encouraging message to help someone stay on top of their medication schedule.\n\n" +
            "Use the patient-specific data below so the message feels personalised rather than generic.\n\n" +
            "Patient:\n" +
            "$patientLine\n\n" +
            "Medication list:\n" +
            "$medicationLine\n\n" +
            "Symptom history:\n" +
            "$symptomLine\n\n" +
            "Keep the message to one or two sentences. " +
            "Do not tell the patient to change dosage, stop medication, or treat symptoms. " +
            "Do not provide medical diagnosis."
    }

}
