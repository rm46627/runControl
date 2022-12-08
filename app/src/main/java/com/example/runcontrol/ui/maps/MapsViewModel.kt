package com.example.runcontrol.ui.maps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class RunStatus {
    CLEAN, STARTED, ENDED
}

@HiltViewModel
class MapsViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    var currentRunState = RunStatus.CLEAN

    fun clean(){
        currentRunState = RunStatus.CLEAN
    }

    fun started() {
        currentRunState = RunStatus.STARTED
    }

    fun ended() {
        currentRunState = RunStatus.ENDED
    }

}