package com.example.runcontrol.ui.control.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    var selectedChip: Int = 0
    var listDesc: Boolean = true
}