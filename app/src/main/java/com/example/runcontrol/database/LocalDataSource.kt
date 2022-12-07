package com.example.runcontrol.database

import com.example.runcontrol.database.dao.RunDao
import com.example.runcontrol.database.entities.RunEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val runDao: RunDao
){

    suspend fun insertRun(runEntity: RunEntity) {
        runDao.insertRun(runEntity)
    }

    suspend fun deleteRun(runEntity: RunEntity) {
        runDao.deleteRun(runEntity)
    }

    fun readRuns(): Flow<List<RunEntity>>{
        return runDao.readRuns()
    }

}