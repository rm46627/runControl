package com.example.runcontrol.ui.maps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    var currentRunState = RunStatus.CLEAN

    fun clean(){
        currentRunState = RunStatus.CLEAN
    }

    fun ready() {
        currentRunState = RunStatus.READY
    }

    fun started() {
        currentRunState = RunStatus.STARTED
    }

    fun ended() {
        currentRunState = RunStatus.ENDED
    }

}

enum class RunStatus {
    CLEAN, READY,  STARTED, ENDED
}