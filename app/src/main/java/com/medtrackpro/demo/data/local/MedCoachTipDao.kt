package com.medtrackpro.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedCoachTipDao {
    @Insert
    suspend fun insert(tip: MedCoachTipEntity)

    @Query(
        "SELECT * FROM medcoach_tips " +
            "WHERE patientId = :patientId " +
            "ORDER BY timestamp DESC"
    )
    fun getTipsByPatientId(patientId: String): Flow<List<MedCoachTipEntity>>

    @Query(
        "SELECT * FROM medcoach_tips " +
            "WHERE patientId = :patientId " +
            "ORDER BY timestamp DESC " +
            "LIMIT :limit"
    )
    suspend fun getRecentTipsByPatientId(
        patientId: String,
        limit: Int
    ): List<MedCoachTipEntity>
}
