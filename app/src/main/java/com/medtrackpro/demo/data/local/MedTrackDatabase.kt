package com.medtrackpro.demo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PatientEntity::class,
        MedicationEntity::class,
        SymptomEntity::class,
        MedCoachTipEntity::class,
        TakenStatusEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MedTrackDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao

    abstract fun medicationDao(): MedicationDao

    abstract fun symptomDao(): SymptomDao

    abstract fun medCoachTipDao(): MedCoachTipDao

    abstract fun takenStatusDao(): TakenStatusDao

    companion object {
        @Volatile
        private var instance: MedTrackDatabase? = null

        fun getDatabase(context: Context): MedTrackDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    "medtrack_database"
                )
                    .build()
                    .also { instance = it }
            }
        }
    }
}
