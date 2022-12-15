package com.example.runcontrol.database.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runcontrol.utils.Constants
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Constants.RUN_TABLE)
class RunEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var date: String,
    var time: Int,
    var distanceMeters: Double,
    var pace: Double,
    var burnedKcal: Int,
    var locations: List<LatLng>
): Parcelable