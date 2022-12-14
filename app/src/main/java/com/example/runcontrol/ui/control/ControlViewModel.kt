package com.example.runcontrol.ui.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.runcontrol.database.Repository
import com.example.runcontrol.extensions.toPairs
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.entriesOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    internal val chartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    fun setChartData(distances: List<Double>) {
        viewModelScope.launch {
            chartEntryModelProducer.setEntries(entriesOf(*distances.toPairs()))
        }
    }
}