package com.example.runcontrol.ui.maps

import com.example.runcontrol.service.TrackerService
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat

object MapUtil {

    fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun calculateElapsedTime(startTime: Long, stopTime: Long): String {
        val elapsedTime = startTime - stopTime
        val seconds = (elapsedTime / 1000).toInt() % 60
        val minutes = (elapsedTime / (1000 * 60) % 60)
        val hours = (elapsedTime / (1000 * 60 * 60) % 24)
        return "$hours:$minutes:$seconds"
    }

    fun calculateDistance(locationList: MutableList<LatLng>, distance: Double): Double {
        if (locationList.size > 1) {
            return distance + SphericalUtil.computeDistanceBetween(
                locationList[locationList.size - 2],
                locationList.last()
            )
        }
        return 0.0
    }

    fun formatDistance(distance: Double): String {
        if (distance > 999){
            return "${DecimalFormat("#.##").format(distance/1000)} km"
        }
        return "${distance.toInt()} m"
    }
}