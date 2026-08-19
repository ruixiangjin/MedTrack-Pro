package com.medtrackpro.demo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey
    val patientId: String,
    val phoneNumber: String,
    val name: String,
    val password: String? = null
)
