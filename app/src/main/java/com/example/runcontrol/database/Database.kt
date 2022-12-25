package com.example.runcontrol.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runcontrol.database.dao.RunDao
import com.example.runcontrol.database.entities.RunEntity

@Database(
    entities = [RunEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class Database: RoomDatabase() {

    abstract fun runDao(): RunDao

}