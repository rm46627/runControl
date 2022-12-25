package com.example.runcontrol.database.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runcontrol.utils.Constants
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Parcelize
@Entity(tableName = Constants.RUN_TABLE)
class RunEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var date: Long,
    var runTime: Int,
    var distanceMeters: Double,
    var avgPace: Double,
    var paceTimes: List<Int>,
    var burnedKcal: Int,
    var locations: List<LatLng>
): Parcelable {

    private fun getFormattedDate(pattern: String): String {
        val zone = ZoneId.of("Europe/Warsaw")
        val instant = Instant.ofEpochMilli(this.date)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, zone)
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return zonedDateTime.format(formatter)
    }

    fun dateToCalendar(): String {
        return getFormattedDate("dd/MM/yyyy")
    }

    fun dateToClockTime(): String {
        return getFormattedDate("HH:mm:ss")
    }

    fun dateToDayTime(): String {
        val hourTime = this.dateToClockTime().take(2).toInt()
        return when (hourTime) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }
    }

    fun dateToFullDateTime(): String {
        return getFormattedDate("HH:mm:ss dd/MM/yyyy")
    }
}