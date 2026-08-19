package com.medtrackpro.demo.data.repository

import android.content.Context
import com.medtrackpro.demo.data.local.MedTrackDatabase
import com.medtrackpro.demo.data.local.PatientDao
import com.medtrackpro.demo.data.local.PatientEntity
import kotlinx.coroutines.flow.Flow

class PatientRepository(context: Context) {
    private val patientDao: PatientDao =
        MedTrackDatabase.getDatabase(context.applicationContext).patientDao()

    suspend fun insertPatient(patient: PatientEntity) {
        patientDao.insert(patient)
    }

    suspend fun insertPatients(patients: List<PatientEntity>) {
        patientDao.insertAll(patients)
    }

    suspend fun updatePatientPassword(patientId: String, password: String) {
        patientDao.updatePassword(patientId, password)
    }

    suspend fun getPatientById(patientId: String): PatientEntity? {
        return patientDao.getPatientById(patientId)
    }

    fun getPatientByIdFlow(patientId: String): Flow<PatientEntity?> {
        return patientDao.getPatientByIdFlow(patientId)
    }

    suspend fun getPatientByPhone(phoneNumber: String): PatientEntity? {
        return patientDao.getPatientByPhone(phoneNumber)
    }

    fun getAllPatients(): Flow<List<PatientEntity>> {
        return patientDao.getAllPatients()
    }

    suspend fun getPatientCount(): Int {
        return patientDao.getPatientCount()
    }
}
