package com.medtrackpro.demo.data.repository

import android.content.Context
import com.medtrackpro.demo.data.local.MedTrackDatabase
import com.medtrackpro.demo.data.local.SymptomCategoryCount
import com.medtrackpro.demo.data.local.SymptomDao
import com.medtrackpro.demo.data.local.SymptomEntity
import kotlinx.coroutines.flow.Flow

class SymptomRepository(context: Context) {
    private val symptomDao: SymptomDao =
        MedTrackDatabase.getDatabase(context.applicationContext).symptomDao()

    suspend fun insertSymptom(symptom: SymptomEntity) {
        symptomDao.insert(symptom)
    }

    suspend fun insertSymptoms(symptoms: List<SymptomEntity>) {
        symptomDao.insertAll(symptoms)
    }

    fun getSymptomsForPatient(patientId: String): Flow<List<SymptomEntity>> {
        return symptomDao.getSymptomsByPatientId(patientId)
    }

    suspend fun getRecentSymptomsForPatient(
        patientId: String,
        limit: Int
    ): List<SymptomEntity> {
        return symptomDao.getRecentSymptomsByPatientId(patientId, limit)
    }

    suspend fun getAverageSymptomSeverity(): Double? {
        return symptomDao.getAverageSymptomSeverity()
    }

    suspend fun getMostCommonSymptomCategory(): SymptomCategoryCount? {
        return symptomDao.getMostCommonSymptomCategory()
    }
}
