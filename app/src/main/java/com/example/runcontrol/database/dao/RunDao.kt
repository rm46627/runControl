package com.example.runcontrol.database.dao

import androidx.room.*
import com.example.runcontrol.database.entities.RunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRun(runEntity: RunEntity)

    @Delete
    suspend fun deleteRun(runEntity: RunEntity)

    @Query("SELECT * FROM run_table ORDER BY id DESC")
    fun readRuns(): Flow<List<RunEntity>>

}