package com.example.runcontrol.ui.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.runcontrol.database.Repository
import com.example.runcontrol.database.entities.RunEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    val readRuns: LiveData<List<RunEntity>> = repository.local.readRuns().asLiveData()
    var dateStarted = ""
    var wasSaved = false

    fun insertRun(runEntity: RunEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertRun(runEntity)
        }
        wasSaved = true
    }
}