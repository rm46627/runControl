package com.example.foodfoodapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.foodfoodapp.data.database.dao.RunDao
import com.example.foodfoodapp.data.database.entities.RunEntity

@Database(
    entities = [RunEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class Database: RoomDatabase() {

    abstract fun runDao(): RunDao

}