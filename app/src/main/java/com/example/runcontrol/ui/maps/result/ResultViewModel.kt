package com.example.runcontrol.ui.maps.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    var dateStarted = ""
    var wasSaved = false

    fun insertRun(runEntity: RunEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertRun(runEntity)
        }
        wasSaved = true
    }
}