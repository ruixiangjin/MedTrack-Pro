package com.medtrackpro.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TakenStatusDao {
    @Insert
    suspend fun insert(status: TakenStatusEntity)

    @Query(
        "SELECT * FROM taken_statuses " +
            "WHERE patientId = :patientId AND date = :date"
    )
    fun getTakenStatusesForDate(
        patientId: String,
        date: String
    ): Flow<List<TakenStatusEntity>>

    @Query(
        "SELECT * FROM taken_statuses " +
            "WHERE patientId = :patientId AND date = :date"
    )
    suspend fun getTakenStatusesForDateOnce(
        patientId: String,
        date: String
    ): List<TakenStatusEntity>

    @Query(
        "SELECT * FROM taken_statuses " +
            "WHERE patientId = :patientId " +
            "AND medicationId = :medicationId " +
            "AND date = :date " +
            "LIMIT 1"
    )
    suspend fun getTakenStatus(
        patientId: String,
        medicationId: Int,
        date: String
    ): TakenStatusEntity?

    @Query(
        "UPDATE taken_statuses " +
            "SET isTaken = :isTaken " +
            "WHERE patientId = :patientId " +
            "AND medicationId = :medicationId " +
            "AND date = :date"
    )
    suspend fun updateTakenStatus(
        patientId: String,
        medicationId: Int,
        date: String,
        isTaken: Boolean
    )
}
