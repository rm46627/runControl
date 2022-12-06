package com.example.foodfoodapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.runcontrol.util.Constants
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline

@Entity(tableName = Constants.RUN_TABLE)
class RunEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var date: String,
    var time: Int,
    var distance: Int,
    var burnedKcal: Int,
    var locations: List<LatLng>,
    var polylines: List<Polyline>
)