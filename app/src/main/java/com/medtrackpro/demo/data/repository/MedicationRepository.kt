package com.medtrackpro.demo.data.repository

import android.content.Context
import com.medtrackpro.demo.data.local.MedTrackDatabase
import com.medtrackpro.demo.data.local.MedicationDao
import com.medtrackpro.demo.data.local.MedicationEntity
import com.medtrackpro.demo.data.local.TakenStatusDao
import com.medtrackpro.demo.data.local.TakenStatusEntity
import kotlinx.coroutines.flow.Flow

class MedicationRepository(context: Context) {
    private val database = MedTrackDatabase.getDatabase(context.applicationContext)
    private val medicationDao: MedicationDao = database.medicationDao()
    private val takenStatusDao: TakenStatusDao = database.takenStatusDao()

    suspend fun insertMedication(medication: MedicationEntity) {
        medicationDao.insert(medication)
    }

    suspend fun insertMedications(medications: List<MedicationEntity>) {
        medicationDao.insertAll(medications)
    }

    fun getMedicationsForPatient(patientId: String): Flow<List<MedicationEntity>> {
        return medicationDao.getMedicationsByPatientId(patientId)
    }

    suspend fun getMedicationsForPatientOnce(patientId: String): List<MedicationEntity> {
        return medicationDao.getMedicationsByPatientIdOnce(patientId)
    }

    suspend fun getMedicationCount(): Int {
        return medicationDao.getMedicationCount()
    }

    fun getTakenStatusesForDate(
        patientId: String,
        date: String
    ): Flow<List<TakenStatusEntity>> {
        return takenStatusDao.getTakenStatusesForDate(patientId, date)
    }

    suspend fun getTakenStatusesForDateOnce(
        patientId: String,
        date: String
    ): List<TakenStatusEntity> {
        return takenStatusDao.getTakenStatusesForDateOnce(patientId, date)
    }

    suspend fun setMedicationTaken(
        patientId: String,
        medicationId: Int,
        date: String,
        isTaken: Boolean
    ) {
        val existingStatus = takenStatusDao.getTakenStatus(
            patientId = patientId,
            medicationId = medicationId,
            date = date
        )

        if (existingStatus == null) {
            takenStatusDao.insert(
                TakenStatusEntity(
                    patientId = patientId,
                    medicationId = medicationId,
                    date = date,
                    isTaken = isTaken
                )
            )
        } else {
            takenStatusDao.updateTakenStatus(
                patientId = patientId,
                medicationId = medicationId,
                date = date,
                isTaken = isTaken
            )
        }
    }
}
