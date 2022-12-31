package com.example.runcontrol.ui.control.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.runcontrol.database.Repository
import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.extensions.toPairs
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.entriesOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    internal val chartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    fun setChartData(paceTimes: List<Int>) {
        viewModelScope.launch {
            chartEntryModelProducer.setEntries(entriesOf(*paceTimes.toPairs()))
        }
    }

    suspend fun removeFromDb(runEntity: RunEntity) {
        repository.local.deleteRun(runEntity)
    }
}