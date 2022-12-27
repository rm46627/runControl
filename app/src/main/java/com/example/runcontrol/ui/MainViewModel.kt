package com.example.runcontrol.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.runcontrol.database.Repository
import com.example.runcontrol.database.entities.RunEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    var canChangeFragment: MutableLiveData<Boolean> = MutableLiveData()

    val readRuns: LiveData<List<RunEntity>> = repository.local.readRuns().asLiveData()
}