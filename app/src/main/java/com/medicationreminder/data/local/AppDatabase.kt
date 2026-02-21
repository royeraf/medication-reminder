package com.medicationreminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.medicationreminder.data.local.dao.DoseLogDao
import com.medicationreminder.data.local.dao.MedicationDao
import com.medicationreminder.data.local.dao.ScheduleDao
import com.medicationreminder.data.local.entity.DoseLogEntity
import com.medicationreminder.data.local.entity.MedicationEntity
import com.medicationreminder.data.local.entity.ScheduleEntity

@Database(
    entities = [
        MedicationEntity::class,
        ScheduleEntity::class,
        DoseLogEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun doseLogDao(): DoseLogDao

    companion object {
        const val DATABASE_NAME = "medication_reminder.db"
    }
}
