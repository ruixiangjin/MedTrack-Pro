package com.medtrackpro.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(patient: PatientEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(patients: List<PatientEntity>)

    @Query("UPDATE patients SET password = :password WHERE patientId = :patientId")
    suspend fun updatePassword(patientId: String, password: String)

    @Query("SELECT * FROM patients WHERE patientId = :patientId LIMIT 1")
    suspend fun getPatientById(patientId: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE patientId = :patientId LIMIT 1")
    fun getPatientByIdFlow(patientId: String): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getPatientByPhone(phoneNumber: String): PatientEntity?

    @Query("SELECT * FROM patients ORDER BY patientId ASC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int
}
