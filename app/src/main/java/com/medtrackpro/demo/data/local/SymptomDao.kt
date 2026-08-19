package com.medtrackpro.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Insert
    suspend fun insert(symptom: SymptomEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(symptoms: List<SymptomEntity>)

    @Query(
        "SELECT * FROM symptoms " +
            "WHERE patientId = :patientId " +
            "ORDER BY dateTime DESC"
    )
    fun getSymptomsByPatientId(patientId: String): Flow<List<SymptomEntity>>

    @Query(
        "SELECT * FROM symptoms " +
            "WHERE patientId = :patientId " +
            "ORDER BY dateTime DESC " +
            "LIMIT :limit"
    )
    suspend fun getRecentSymptomsByPatientId(
        patientId: String,
        limit: Int
    ): List<SymptomEntity>

    @Query("SELECT AVG(severity) FROM symptoms")
    suspend fun getAverageSymptomSeverity(): Double?

    @Query(
        "SELECT category, COUNT(*) AS count " +
            "FROM symptoms " +
            "GROUP BY category " +
            "ORDER BY count DESC, category COLLATE NOCASE ASC " +
            "LIMIT 1"
    )
    suspend fun getMostCommonSymptomCategory(): SymptomCategoryCount?
}

data class SymptomCategoryCount(
    val category: String,
    val count: Int
)
