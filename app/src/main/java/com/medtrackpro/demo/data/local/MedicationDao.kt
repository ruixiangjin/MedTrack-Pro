package com.medtrackpro.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(medications: List<MedicationEntity>)

    @Query(
        "SELECT * FROM medications " +
            "WHERE patientId = :patientId " +
            "ORDER BY scheduledTime ASC, name COLLATE NOCASE ASC"
    )
    fun getMedicationsByPatientId(patientId: String): Flow<List<MedicationEntity>>

    @Query(
        "SELECT * FROM medications " +
            "WHERE patientId = :patientId " +
            "ORDER BY scheduledTime ASC, name COLLATE NOCASE ASC"
    )
    suspend fun getMedicationsByPatientIdOnce(patientId: String): List<MedicationEntity>

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getMedicationCount(): Int
}
