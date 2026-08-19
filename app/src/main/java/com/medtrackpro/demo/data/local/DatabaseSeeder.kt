package com.medtrackpro.demo.data.local

import android.content.Context
import com.google.gson.Gson
import com.medtrackpro.demo.data.repository.MedicationRepository
import com.medtrackpro.demo.data.repository.PatientRepository
import com.medtrackpro.demo.data.repository.SymptomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    private const val PREFS_NAME = "medtrack_prefs"
    private const val DB_SEEDED_KEY = "db_seeded"
    private const val SAVED_USERS_KEY = "users"
    private const val SAVED_MEDICATIONS_KEY = "medications"
    private val CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

    suspend fun seedIfNeeded(context: Context) {
        val appContext = context.applicationContext
        val sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        withContext(Dispatchers.IO) {
            val patientRepository = PatientRepository(appContext)
            val medicationRepository = MedicationRepository(appContext)
            val symptomRepository = SymptomRepository(appContext)
            val isSeeded = sharedPreferences.getBoolean(DB_SEEDED_KEY, false)
            val patientCount = patientRepository.getPatientCount()

            if (isSeeded && patientCount > 0) {
                return@withContext
            }

            if (patientCount > 0) {
                sharedPreferences
                    .edit()
                    .putBoolean(DB_SEEDED_KEY, true)
                    .apply()
                return@withContext
            }

            patientRepository.insertPatients(
                readPatientsFromCsv(appContext) + readSavedUsers(appContext)
            )
            medicationRepository.insertMedications(
                readMedicationsFromCsv(appContext) + readSavedMedications(appContext)
            )
            symptomRepository.insertSymptoms(readSymptomsFromCsv(appContext))

            sharedPreferences
                .edit()
                .putBoolean(DB_SEEDED_KEY, true)
                .apply()
        }
    }

    private fun readPatientsFromCsv(context: Context): List<PatientEntity> {
        return readCsvRows(context, "patients.csv").mapNotNull { row ->
            val patientId = row.value("PatientID", "PID")
            val phoneNumber = row.value("PhoneNumber")
            val name = row.value("Name")

            if (patientId.isBlank() || phoneNumber.isBlank() || name.isBlank()) {
                null
            } else {
                PatientEntity(
                    patientId = patientId,
                    phoneNumber = phoneNumber,
                    name = name,
                    password = row.value("Password").ifBlank { null }
                )
            }
        }
    }

    private fun readMedicationsFromCsv(context: Context): List<MedicationEntity> {
        return readCsvRows(context, "medications.csv").mapNotNull { row ->
            val patientId = row.value("PatientID", "PID")
            val name = row.value("MedicationName", "Name")
            val dosage = row.value("Dosage")
            val frequency = row.value("Frequency")
            val scheduledTime = row.value("ScheduledTime", "Time")

            if (
                patientId.isBlank() ||
                name.isBlank() ||
                dosage.isBlank() ||
                frequency.isBlank() ||
                scheduledTime.isBlank()
            ) {
                null
            } else {
                MedicationEntity(
                    patientId = patientId,
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    scheduledTime = scheduledTime,
                    medicationType = row.value("MedicationType", "Type"),
                    notes = row.value("Notes")
                )
            }
        }
    }

    private fun readSymptomsFromCsv(context: Context): List<SymptomEntity> {
        return readCsvRows(context, "symptoms.csv").mapNotNull { row ->
            val patientId = row.value("PatientID", "PID")
            val category = row.value("Category")
            val severity = row.value("Severity").toIntOrNull()
            val dateTime = row.value("DateTime")

            if (
                patientId.isBlank() ||
                category.isBlank() ||
                severity == null ||
                severity !in 1..10 ||
                dateTime.isBlank()
            ) {
                null
            } else {
                SymptomEntity(
                    patientId = patientId,
                    category = category,
                    severity = severity,
                    notes = row.value("Notes"),
                    dateTime = dateTime
                )
            }
        }
    }

    private fun readSavedUsers(context: Context): List<PatientEntity> {
        val usersJson = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SAVED_USERS_KEY, null)

        if (usersJson.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            Gson()
                .fromJson(usersJson, Array<SavedUserMigration>::class.java)
                .mapNotNull { user ->
                    if (
                        user.patientId.isBlank() ||
                        user.phoneNumber.isBlank() ||
                        user.name.isBlank() ||
                        user.password.isBlank()
                    ) {
                        null
                    } else {
                        PatientEntity(
                            patientId = user.patientId,
                            phoneNumber = user.phoneNumber,
                            name = user.name,
                            password = user.password
                        )
                    }
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readSavedMedications(context: Context): List<MedicationEntity> {
        val medicationsJson = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SAVED_MEDICATIONS_KEY, null)

        if (medicationsJson.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            Gson()
                .fromJson(medicationsJson, Array<SavedMedicationMigration>::class.java)
                .mapNotNull { medication ->
                    if (
                        medication.patientId.isBlank() ||
                        medication.name.isBlank() ||
                        medication.dosage.isBlank() ||
                        medication.frequency.isBlank() ||
                        medication.scheduledTime.isBlank()
                    ) {
                        null
                    } else {
                        MedicationEntity(
                            patientId = medication.patientId,
                            name = medication.name,
                            dosage = medication.dosage,
                            frequency = medication.frequency,
                            scheduledTime = medication.scheduledTime,
                            medicationType = medication.medicationType,
                            notes = medication.notes
                        )
                    }
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readCsvRows(context: Context, fileName: String): List<CsvRow> {
        return try {
            context.assets.open(fileName).bufferedReader().useLines { lines ->
                val rows = lines.filter { it.isNotBlank() }.toList()

                if (rows.isEmpty()) {
                    emptyList()
                } else {
                    val headers = splitCsvLine(rows.first())

                    rows.drop(1).map { line ->
                        CsvRow(
                            headers = headers,
                            values = splitCsvLine(line)
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        return line
            .split(CSV_SPLIT_REGEX)
            .map { value ->
                value.trim().removeSurrounding("\"")
            }
    }
}

private data class CsvRow(
    val headers: List<String>,
    val values: List<String>
) {
    fun value(vararg names: String): String {
        val index = headers.indexOfFirst { header ->
            names.any { name -> header.equals(name, ignoreCase = true) }
        }

        return if (index == -1) {
            ""
        } else {
            values.getOrNull(index).orEmpty()
        }
    }
}

private data class SavedUserMigration(
    val patientId: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val password: String = ""
)

private data class SavedMedicationMigration(
    val patientId: String = "",
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val scheduledTime: String = "",
    val medicationType: String = "",
    val notes: String = ""
)
