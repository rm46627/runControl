package com.example.runcontrol.ui.history.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.runcontrol.database.Repository
import com.example.runcontrol.extensions.toPairs
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.entriesOf
import com.patrykandpatryk.vico.core.util.RandomEntriesGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    private val generator = RandomEntriesGenerator(
        xRange = 0..5,
        yRange = 2..20,
    )

    internal val chartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    init {
        viewModelScope.launch {
            chartEntryModelProducer.setEntries(generator.generateRandomEntries())
        }
    }

    fun setChartData(paceTimes: List<Int>) {
        viewModelScope.launch {
            chartEntryModelProducer.setEntries(entriesOf(*paceTimes.toPairs()))
        }
    }
}