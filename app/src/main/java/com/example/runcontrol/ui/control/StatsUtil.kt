package com.example.runcontrol.ui.control

import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.extensions.round
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

object StatsUtil {

    fun daysOfLastWeek(): MutableList<String> {
        val days = mutableListOf<String>()
        val now = LocalDate.now()
        for (i in 1..7) {
            val day = now.minusDays((7 - i).toLong()).dayOfWeek
            days.add(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
        }
        return days
    }

    fun mapDistancesToLastWeek(runs: List<RunEntity>, days: List<String>): List<Double> {
        val week = runs.filter {
            it.isFromThisWeek()
        }
        val distancesOfWeek: MutableMap<String, Double> = mutableMapOf()
        days.forEach {
            distancesOfWeek[it] = 0.0
        }
        week.forEach {
            distancesOfWeek[it.dateToDayOfWeek()] =
                distancesOfWeek[it.dateToDayOfWeek()]!! + it.distanceMeters
        }
        return distancesOfWeek.values.toMutableList().map { (it/1000).round(2) }
    }

    fun sumKilometers(runs: List<RunEntity>): Double {
        var sum = 0.0
        runs.forEach {
            sum += it.distanceMeters
        }
        return sum
    }

    fun sumBurned(runs: List<RunEntity>): Int {
        var sum = 0
        runs.forEach {
            sum += it.burnedKcal
        }
        return sum
    }

    fun bestPace(runs: List<RunEntity>): Int {
        var bestPace = 0
        runs.forEach {
            val max = it.paceTimes.max()
            if( max > bestPace) bestPace = max
        }
        return bestPace
    }

}