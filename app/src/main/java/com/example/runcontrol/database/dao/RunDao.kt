package com.example.foodfoodapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.foodfoodapp.data.database.entities.RunEntity

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRun(runEntity: RunEntity)

}